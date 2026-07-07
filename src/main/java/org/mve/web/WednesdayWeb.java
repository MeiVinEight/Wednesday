package org.mve.web;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.mve.Configuration;
import org.mve.ConnectionManager;
import org.mve.logging.FileLogger;
import org.mve.logging.WednesdayLogger;
import org.mve.orange.event.OrangeEvent;
import org.mve.uni.Array;
import org.mve.uni.Cookie;
import org.mve.uni.HTTP;
import org.mve.uni.Hexadecimal;
import org.mve.uni.Json;
import org.mve.uni.MD5;
import org.mve.uni.Mirroring;
import org.mve.web.service.*;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

public class WednesdayWeb implements HttpHandler, WebService
{
	public static final WednesdayLogger LOGGER = new WednesdayLogger("WEB", Configuration.LOG_LEVEL);
	public static final Map<String, Map<String, WebService>> API = new HashMap<>();
	private static final Pattern PATTERN_TOKEN = Pattern.compile("^[0-9A-F]{32}$");
	private static final String DATA_WEBUI = Configuration.DATA_DIR + "/WEBUI.DAT";
	private static final String DATA_CONN = Configuration.DATA_DIR + "/CONNECTION.DB";
	private static final int SIGNATURE_WEBUI = 2141410369;
	public static final Random RANDOM = new SecureRandom();
	public static final byte[] WEBUI_TOKEN = new byte[32];
	public static final byte[] JSESSIONID = new byte[32];
	private final HttpServer server;
	public final ConnectionManager connection;
	private boolean handling = false;
	private boolean stop = false;

	public WednesdayWeb(InetSocketAddress address) throws IOException
	{
		this.server = HttpServer.create(address, 0);
		server.createContext("/", this);
		server.start();
		WednesdayWeb.LOGGER.info("Web API start at http://{}:{}", address.getHostString(), address.getPort());
		this.connection = new ConnectionManager(WednesdayWeb.DATA_CONN);
		WednesdayWeb.registerAPI(HTTP.METHOD_POST, WebAPI.API_LOGIN, this);
		WednesdayWeb.registerAPI(HTTP.METHOD_POST, WebAPI.API_STOP, new StopWeb(this));
		WednesdayWeb.registerAPI(HTTP.METHOD_POST, WebAPI.API_CONN, new ConnGet(this.connection));
		WednesdayWeb.registerAPI(HTTP.METHOD_POST, WebAPI.API_CONN, new ConnPost(this.connection));
		WednesdayWeb.registerAPI(HTTP.METHOD_CONN, WebAPI.API_CONN, new ConnConn(this.connection));
		WednesdayWeb.registerAPI(HTTP.METHOD_DISCONN, WebAPI.API_CONN, new ConnDisconn(this.connection));
		WednesdayWeb.registerAPI(HTTP.METHOD_GET, WebAPI.API_CONFIG, new WebConfigGet());
	}

	@Override
	public void handle(HttpExchange exchange)
	{
		this.handling = true;
		try
		{
			this.handle0(exchange);
		}
		catch (Throwable e)
		{
			exchange.close();
			LOGGER.error(e);
		}
		this.handling = false;
		if (this.stop)
			this.close();
	}

	@Override
	public Object service(Json exchange)
	{
		Json body = WebAPI.code(new Json(), WebAPI.CODE_OK);
		String token = exchange.string(WebAPI.KEY_TOKEN);
		if (token == null)
			return WebAPI.code(body, WebAPI.CODE_MISSING_PARAM, (Object) WebAPI.KEY_TOKEN);
		if (!token.equalsIgnoreCase(new String(WednesdayWeb.WEBUI_TOKEN)))
			return WebAPI.code(body, WebAPI.CODE_INVALID_TOKEN);

		MessageDigest md5 = MD5.get();
		byte[] bytes = new byte[32];
		WednesdayWeb.RANDOM.nextBytes(bytes);
		md5.update(bytes);
		md5.update(WednesdayWeb.WEBUI_TOKEN);
		System.arraycopy(Hexadecimal.encode(md5.digest()), 0, WednesdayWeb.JSESSIONID, 0, 32);
		WednesdayWeb.saving();

		String dig = new String(WednesdayWeb.JSESSIONID);
		body.set(WebAPI.KEY_DATA, new Json()
			.set(WebAPI.KEY_SESSION, dig)
		);
		return body;
	}

	@Override
	public boolean auth()
	{
		return false;
	}

	public void handle0(HttpExchange exchange) throws Throwable
	{
		URI requestURI = exchange.getRequestURI();
		LOGGER.info("{} {}", exchange.getRequestMethod(), requestURI.getPath());
		Object bodyObject;
		int code = HttpURLConnection.HTTP_OK;
		Json request;
		try (InputStream bodyin = exchange.getRequestBody())
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream(bodyin.available());
			bodyin.transferTo(out);
			Array array = new Array(out.size());
			array.put(out.toByteArray());
			if (array.length() == 0)
				request = new Json();
			else
				request = Json.resolve(array);
		}

		WEB_SERVICE:
		try
		{
			WebService service = null;
			Map<String, WebService> serviceMap = WednesdayWeb.API.get(exchange.getRequestMethod());
			if (serviceMap != null)
				service = serviceMap.get(requestURI.getPath());
			if (service == null)
			{
				bodyObject = WebAPI.code(new Json(), WebAPI.CODE_NOT_FOUND);
				code = HttpURLConnection.HTTP_NOT_FOUND;
				break WEB_SERVICE;
			}
			if (service.auth() && !this.authenticate(exchange))
			{
				bodyObject = WebAPI.code(new Json(), WebAPI.CODE_UNAUTHORIZED);
				break WEB_SERVICE;
			}
			bodyObject = service.service(request);
			if (bodyObject == null)
				bodyObject = WebAPI.code(new Json(), WebAPI.CODE_OK);
		}
		catch (APIException ae)
		{
			bodyObject = WebAPI.code(new Json(), ae.code, ae.message);
		}
		catch (Throwable e)
		{
			bodyObject = e;
		}

		try
		{
			InputStream in = exchange.getRequestBody();
			int ava = in.available();
			int length = 0;
			byte[] buffer = new byte[4096];
			int read;
			while (length < ava && (read = in.read(buffer)) != -1)
				length += read;
		}
		catch (IOException e)
		{
			LOGGER.error(e);
		}

		if (bodyObject instanceof Throwable e)
		{
			Json stacktrace = new Json(Json.TYPE_ARRAY);
			bodyObject = WebAPI.code(new Json(), WebAPI.CODE_ERROR)
				.set(WebAPI.KEY_DATA, new Json()
					.set(WebAPI.KEY_TYPE, e.getClass().getName())
					.set(WebAPI.KEY_MESSAGE, e.getMessage())
					.set(WebAPI.KEY_STACKTRACE, stacktrace
					)
				);
			for (StackTraceElement element : e.getStackTrace())
				stacktrace.add(element.toString());
		}

		if (bodyObject instanceof Json || bodyObject instanceof JsonObject)
		{
			String contentType = "application/json";
			String text = bodyObject.toString();
			try (exchange)
			{
				exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
				exchange.getResponseHeaders().set("Content-Type", contentType);
				byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
				exchange.sendResponseHeaders(code, bytes.length);
				exchange.getResponseBody().write(bytes);
			}
			catch (Throwable t)
			{
				LOGGER.error(t);
			}

			return;
		}

		exchange.close();
		LOGGER.warning("Unknown response object type: " + bodyObject.getClass().getName());
	}

	public boolean authenticate(HttpExchange exchange)
	{
		String cookieValue = exchange.getRequestHeaders().getFirst("Cookie");
		if (cookieValue == null)
			return false;
		Cookie cookie = new Cookie(cookieValue);
		String session = cookie.get(WebAPI.COOKIE_JSESSIONID);
		if (session == null || session.isEmpty())
			return false;
		return new String(JSESSIONID).equals(session);
	}

	public void close()
	{
		this.stop = true;
		if (this.handling)
			return;
		this.server.stop(0);
		OrangeEvent.GLOBAL.shutdown();
	}

	public static void registerAPI(String method, String api, WebService service)
	{
		WednesdayWeb.API.computeIfAbsent(method, e -> new HashMap<>()).put(api, service);
	}

	public static void token(String token)
	{
		if (token == null)
			throw new APIException(WebAPI.CODE_MISSING_PARAM, (Object) WebAPI.KEY_TOKEN);
		token = token.toUpperCase();
		if (!WednesdayWeb.PATTERN_TOKEN.matcher(token).matches())
			throw new APIException(WebAPI.CODE_INVALID_TOKEN);

		System.arraycopy(token.getBytes(), 0, WednesdayWeb.WEBUI_TOKEN, 0, WednesdayWeb.WEBUI_TOKEN.length);
		WednesdayWeb.JSESSIONID[0] = 0;
		WednesdayWeb.saving();
	}

	public static void saving()
	{
		File dataFile = new File(DATA_WEBUI);
		if (dataFile.isDirectory())
			Mirroring.thrown(new FileNotFoundException(dataFile.getPath() + " is directory"));
		boolean ignored = dataFile.getParentFile().mkdirs();
		try (FileOutputStream out = new FileOutputStream(dataFile))
		{
			DataOutputStream dos = new DataOutputStream(out);
			dos.writeInt(SIGNATURE_WEBUI);
			dos.writeByte(1);
			dos.writeByte(0);
			dos.writeShort(0);
			dos.writeLong(0);
			dos.write(WEBUI_TOKEN);
			dos.write(JSESSIONID);
		}
		catch (IOException e)
		{
			Mirroring.thrown(e);
		}
	}

	public static void main(String[] args)
	{
		WednesdayWeb web;
		try
		{
			web = new WednesdayWeb(new InetSocketAddress(Configuration.ADDRESS, Configuration.PORT));
		}
		catch (IOException e)
		{
			LOGGER.error(e);
			return;
		}
		Runtime.getRuntime().addShutdownHook(new Thread(web::close));
	}

	static
	{
		LOGGER.consumation(FileLogger.INSTANCE);
		// READ DATA
		File dataFile = new File(DATA_WEBUI);
		if (dataFile.isDirectory())
			Mirroring.thrown(new FileNotFoundException(dataFile.getPath() + " is directory"));
		boolean ignored = dataFile.getParentFile().mkdirs();
		if (!dataFile.isFile())
		{
			byte[] randomBytes = new byte[6];
			RANDOM.nextBytes(randomBytes);
			randomBytes = Base64.getEncoder().encode(randomBytes);
			LOGGER.info("!!! WEBUI TOKEN: {}", new String(randomBytes));
			LOGGER.info("!!! THIS MESSAGE ONLY SHOW ONCE");
			MessageDigest md5 = MD5.get();
			md5.update(randomBytes);
			randomBytes = Hexadecimal.encode(md5.digest());
			System.arraycopy(randomBytes, 0, WEBUI_TOKEN, 0, 32);
			RANDOM.nextBytes(JSESSIONID);
			saving();
		}

		try (FileInputStream datain = new FileInputStream(dataFile))
		{
			DataInputStream dis = new DataInputStream(datain);
			int signature = dis.readInt();
			if (signature != SIGNATURE_WEBUI)
				throw new RuntimeException("Unknown format of webui data file");
			dis.readByte(); // version
			dis.skipBytes(11);
			dis.readFully(WEBUI_TOKEN);
			dis.readFully(JSESSIONID);
		}
		catch (IOException e)
		{
			Mirroring.thrown(e);
			throw new RuntimeException(e);
		}

		WednesdayWeb.registerAPI(HTTP.METHOD_POST, WebAPI.API_TOKEN, new ChangeToken());
		WednesdayWeb.registerAPI(HTTP.METHOD_POST, WebAPI.API_AUTHCHK, new AuthCheck());
	}
}
