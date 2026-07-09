package org.mve.web;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.mve.Configuration;
import org.mve.logging.Coffee;
import org.mve.orange.event.OrangeEvent;
import org.mve.uni.Array;
import org.mve.uni.Cookie;
import org.mve.uni.HTTP;
import org.mve.uni.Hexadecimal;
import org.mve.uni.Json;
import org.mve.uni.MD5;
import org.mve.uni.Mirroring;
import org.mve.web.service.Auth2FAStatus;
import org.mve.web.service.AuthCheck;
import org.mve.web.service.ChangeToken;
import org.mve.web.service.ConnEnable;
import org.mve.web.service.ConnGet;
import org.mve.web.service.ConnPost;
import org.mve.web.service.StopWeb;
import org.mve.web.service.Version;
import org.mve.web.service.WebConfigGet;
import org.mve.web.service.WebConfigPost;
import org.mve.web.service.WebPersistence;
import org.mve.web.service.WebSystemInfo;

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

public class WebAPI implements HttpHandler, WebService
{
	public static final Coffee LOGGER = new Coffee("WebAPI");
	public static final String KEY_CODE = "code";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_DATA = "data";
	public static final String KEY_TOKEN = "token";
	public static final String KEY_SESSION = "session";
	public static final String KEY_TYPE = "type";
	public static final String KEY_STACKTRACE = "stacktrace";
	public static final String KEY_NAME = "name";
	public static final String KEY_URL = "url";
	public static final String KEY_ENABLE_2FA = "enable2FA";
	public static final String KEY_HAS_SECRET = "hasSecret";
	public static final String KEY_HOST = "host";
	public static final String KEY_PORT = "port";
	public static final String KEY_LOGLEVEL = "loglevel";
	public static final String KEY_NETWORK = "network";
	public static final String KEY_WEBSOCKET_CLIENTS = "websocketClients";
	public static final String KEY_WEBSOCKET_SERVERS = "websocketServers";
	public static final String KEY_HTTP_CLIENTS = "httpClients";
	public static final String KEY_HTTP_SERVERS = "httpServers";
	public static final String KEY_DELETE = "delete";
	public static final String KEY_VERSION = "version";
	public static final String KEY_SYSTEM = "system";
	public static final String KEY_ACTION = "action";
	public static final String KEY_ENABLE = "enable";
	public static final String KEY_AUTH = "auth";

	public static final int CODE_OK             = 0;
	public static final int CODE_NOT_FOUND      = 1;
	public static final int CODE_ERROR          = 2;
	public static final int CODE_MISSING_PARAM  = 3;
	public static final int CODE_INVALID_TOKEN  = 4;
	public static final int CODE_UNAUTHORIZED   = 5;
	public static final int CODE_INVALID_PARAM  = 6;

	public static final String MSG_OK = "OK";
	public static final String MSG_NOT_FOUND = "API Not Found";
	public static final String MSG_ERROR = "Internal Server Error";
	public static final String MSG_MISSING_PARAM = "Missing param: %s";
	public static final String MSG_INVALID_TOKEN = "Invalid Token";
	public static final String MSG_UNAUTHORIZED = "Unauthorized";
	public static final String MSG_INVALID_PARAM = "Invalid parameter: %s";
	public static final String[] CODE_MSG;

	public static final String API_LOGIN = "/api/v1/login";
	public static final String API_AUTHCHK = "/api/v1/auth/check";
	public static final String API_TOKEN = "/api/v1/token";
	public static final String API_STOP = "/api/v1/stop";
	public static final String API_CONN = "/api/v1/conn";
	public static final String API_CONFIG = "/api/v1/config";
	public static final String API_AUTH_2FA_STATUS = "/api/v1/auth/2fa/status";
	public static final String API_VERSION = "/api/v1/version";
	public static final String API_SYSTEM_INFO = "/api/v1/system/info";
	public static final String API_PERSISTENCE = "/api/v1/persistence";
	public static final String API_CONN_ENABLE = "/api/v1/conn/enable";

	public static final String COOKIE_JSESSIONID = "JSESSIONID";


	public static final Map<String, Map<String, WebService>> API = new HashMap<>();
	private static final Pattern PATTERN_TOKEN = Pattern.compile("^[0-9A-F]{32}$");
	private static final String DATA_WEBUI = Configuration.DATA_DIR + "/WEBUI.DAT";
	private static final String DATA_CONN = Configuration.DATA_DIR + "/CONNECTION.DB";
	private static final int SIGNATURE_WEBUI = 2141410369;
	public static final Random RANDOM = new SecureRandom();
	public static final byte[] WEBUI_TOKEN = new byte[32];
	public static final byte[] JSESSIONID = new byte[32];
	public static WebAPI web = null;
	private final HttpServer server;
	public final ConnectionManager connection;
	private boolean handling = false;
	private boolean stop = false;

	public WebAPI(InetSocketAddress address) throws IOException
	{
		if (WebAPI.web != null)
			throw new IllegalStateException("WebAPI started!");
		WebAPI.web = this;
		this.server = HttpServer.create(address, 0);
		server.createContext("/", this);
		server.start();
		WebAPI.LOGGER.info("Web API start at http://{}:{}", address.getHostString(), address.getPort());
		this.connection = new ConnectionManager(WebAPI.DATA_CONN);
		WebAPI.registerAPI(HTTP.METHOD_POST, WebAPI.API_LOGIN, this);
		WebAPI.registerAPI(HTTP.METHOD_POST, WebAPI.API_STOP, new StopWeb());
		WebAPI.registerAPI(HTTP.METHOD_GET, WebAPI.API_CONN, new ConnGet(this.connection));
		WebAPI.registerAPI(HTTP.METHOD_POST, WebAPI.API_CONN, new ConnPost(this.connection));
		WebAPI.registerAPI(HTTP.METHOD_GET, WebAPI.API_CONFIG, new WebConfigGet());
		WebAPI.registerAPI(HTTP.METHOD_GET, WebAPI.API_AUTH_2FA_STATUS, new Auth2FAStatus());
		WebAPI.registerAPI(HTTP.METHOD_POST, WebAPI.API_CONFIG, new WebConfigPost());
		WebAPI.registerAPI(HTTP.METHOD_POST, WebAPI.API_CONN_ENABLE, new ConnEnable(this.connection));
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
		if (!token.equalsIgnoreCase(new String(WebAPI.WEBUI_TOKEN)))
			return WebAPI.code(body, WebAPI.CODE_INVALID_TOKEN);

		MessageDigest md5 = MD5.get();
		byte[] bytes = new byte[32];
		WebAPI.RANDOM.nextBytes(bytes);
		md5.update(bytes);
		md5.update(WebAPI.WEBUI_TOKEN);
		System.arraycopy(Hexadecimal.encode(md5.digest()), 0, WebAPI.JSESSIONID, 0, 32);
		WebAPI.saving();

		String dig = new String(WebAPI.JSESSIONID);
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
			Map<String, WebService> serviceMap = WebAPI.API.get(exchange.getRequestMethod());
			if (serviceMap != null)
				service = serviceMap.get(requestURI.getPath());
			if (service == null)
			{
				bodyObject = WebAPI.code(new Json(), WebAPI.CODE_NOT_FOUND);
				code = HttpURLConnection.HTTP_NOT_FOUND;
				break WEB_SERVICE;
			}
			if (service.auth() && !WebAPI.authenticate(exchange))
			{
				bodyObject = WebAPI.code(new Json(), WebAPI.CODE_UNAUTHORIZED);
				break WEB_SERVICE;
			}
			bodyObject = service.service(exchange, request);
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
			WebAPI.LOGGER.error(e);
			Json stacktrace = new Json(Json.TYPE_ARRAY);
			bodyObject = WebAPI.code(new Json(), WebAPI.CODE_ERROR, e.getMessage())
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
		WebAPI.API.computeIfAbsent(method, e -> new HashMap<>()).put(api, service);
	}

	public static void token(String token)
	{
		if (token == null)
			throw new APIException(WebAPI.CODE_MISSING_PARAM, (Object) WebAPI.KEY_TOKEN);
		token = token.toUpperCase();
		if (!WebAPI.PATTERN_TOKEN.matcher(token).matches())
			throw new APIException(WebAPI.CODE_INVALID_TOKEN);

		System.arraycopy(token.getBytes(), 0, WebAPI.WEBUI_TOKEN, 0, WebAPI.WEBUI_TOKEN.length);
		WebAPI.JSESSIONID[0] = 0;
		WebAPI.saving();
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

	public static String version()
	{
		return "1.0.0-Lambda";
	}

	public static boolean authenticate(HttpExchange exchange)
	{
		String cookieValue = exchange.getRequestHeaders().getFirst("Cookie");
		if (cookieValue == null)
			return false;
		Cookie cookie = new Cookie(cookieValue);
		String session = cookie.get(WebAPI.COOKIE_JSESSIONID);
		if (session == null || session.isEmpty())
			return false;
		return new String(WebAPI.JSESSIONID).equals(session);
	}

	public static String message(String msg, Object... args)
	{
		return String.format(msg, args);
	}

	public static String message(int code, Object... args)
	{
		return message(CODE_MSG[code], args);
	}

	public static Json code(Json body, int code, String msg, Object... args)
	{
		msg = message(msg, args);
		return body.set(KEY_CODE, code)
			.set(KEY_MESSAGE, msg);
	}

	public static Json code(Json body, int code, Object... args)
	{
		return code(body, code, CODE_MSG[code], args);
	}

	public static void main(String[] args)
	{
		try
		{
			new WebAPI(new InetSocketAddress(Configuration.address(), Configuration.port()));
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
		CODE_MSG = new String[]{
			MSG_OK,
			MSG_NOT_FOUND,
			MSG_ERROR,
			MSG_MISSING_PARAM,
			MSG_INVALID_TOKEN,
			MSG_UNAUTHORIZED,
			MSG_INVALID_PARAM,
		};
		// READ DATA
		File dataFile = new File(DATA_WEBUI);
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

		WebAPI.registerAPI(HTTP.METHOD_POST, WebAPI.API_TOKEN, new ChangeToken());
		WebAPI.registerAPI(HTTP.METHOD_GET, WebAPI.API_AUTHCHK, new AuthCheck());
		WebAPI.registerAPI(HTTP.METHOD_GET, WebAPI.API_VERSION, new Version());
		WebAPI.registerAPI(HTTP.METHOD_GET, WebAPI.API_SYSTEM_INFO, new WebSystemInfo());
		WebAPI.registerAPI(HTTP.METHOD_POST, WebAPI.API_PERSISTENCE, new WebPersistence());
	}
}
