package org.mve.web;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.mve.Configuration;
import org.mve.ConnectionWednesday;
import org.mve.Wednesday;
import org.mve.WednesdayManager;
import org.mve.logging.FileLogger;
import org.mve.logging.WednesdayLogger;
import org.mve.uni.Cookie;
import org.mve.uni.Hexadecimal;
import org.mve.uni.Json;
import org.mve.uni.MD5;
import org.mve.uni.Mirroring;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class WednesdayWeb implements HttpHandler
{
	public static final int ERR_EXCEPTION = -1;
	public static final WednesdayLogger LOGGER = new WednesdayLogger("WEB", Configuration.LOG_LEVEL);
	public static final String COOKIE_JSESSIONID = "JSESSIONID";
	public static final String KEY_COD = "code";
	public static final String KEY_MSG = "message";
	public static final String KEY_DAT = "data";
	private static final Pattern PATTERN_TOKEN = Pattern.compile("^[0-9a-f]{32}$");
	private static final String DATA_WEBUI = Configuration.DATA_DIR + "/webui.dat";
	private static final int SIGNATURE_WEBUI = 2141410369;
	private static final String WEB_ROOT = "web";
	private static final Random RANDOM = new SecureRandom();
	private static final byte[] WEBUI_TOKEN = new byte[32];
	private static final byte[] SESSIONID = new byte[32];
	private final HttpServer server;
	private final WednesdayManager connection;

	public WednesdayWeb(InetSocketAddress address) throws IOException
	{
		this.server = HttpServer.create(address, 0);
		server.createContext("/", this);
		server.start();
		WednesdayWeb.LOGGER.info("Web ui start at http://{}:{}", address.getHostString(), address.getPort());
		this.connection = new WednesdayManager("data/conn.db");
	}

	@Override
	public void handle(HttpExchange exchange)
	{
		try
		{
			this.handle0(exchange);
		}
		catch (Throwable e)
		{
			LOGGER.error(e);
		}
	}

	public void handle0(HttpExchange exchange)
	{
		URI requestURI = exchange.getRequestURI();
		LOGGER.info("{} {}", exchange.getRequestMethod(), requestURI.getPath());
		Object bodyObject;
		int code = HttpURLConnection.HTTP_OK;
		String contentType = null;
		boolean stop = false;

		if (requestURI.getQuery() != null)
		{
			bodyObject = new File(WEB_ROOT, "400.html");
			code = HttpURLConnection.HTTP_BAD_REQUEST;
		}
		else
		{
			try
			{
				bodyObject = this.exchange(exchange);
				if (bodyObject == null)
				{
					code = 404;
					bodyObject = new File(WEB_ROOT, "404.html");
					contentType = "text/html";
				}
			}
			catch (Throwable e)
			{
				LOGGER.error(e);
				bodyObject = "<!DOCTYPE html>\n" +
					"<html lang=\"en\">\n" +
					"\t<head>\n" +
					"\t\t<meta charset=\"UTF-8\">\n" +
					"\t\t<title>500 Internal Server Error</title>\n" +
					"\t</head>\n" +
					"\t<body>\n" +
					"\t\t<div style=\"text-align: center;\"><h1>500 Internal Server Error</h1></div>\n" +
					"\t\t<hr>\n" +
					"\t\t<div style=\"text-align: center;\">Wednesday | " + e + "</div>\n" +
					"\t</body>\n" +
					"</html>";
				contentType = "text/html";
				code = 500;
			}
		}

		this.exhaust(exchange.getRequestBody());

		while (true)
		{
			if (bodyObject instanceof Throwable t)
			{
				Throwable cause = t;
				while (cause.getCause() != null)
					cause = cause.getCause();

				Json body = new Json();
				body.set(KEY_COD, ERR_EXCEPTION);
				body.set(KEY_MSG, cause.getLocalizedMessage());
				Json data = new Json();
				StringWriter sw = new StringWriter();
				t.printStackTrace(new PrintWriter(sw));
				data.set("stack", sw.toString());
				body.set(KEY_DAT, data);
				bodyObject = body;
			}

			if (bodyObject instanceof Stopping)
			{
				stop = true;
				bodyObject = "{\"code\":0,\"message\":\"OK\"}";
				contentType = "application/json";
			}

			if (bodyObject instanceof Json || bodyObject instanceof JsonObject)
			{
				contentType = "application/json";
				bodyObject = bodyObject.toString();
			}

			if (bodyObject instanceof File file)
			{
				if (contentType == null)
					contentType = URLConnection.guessContentTypeFromName(file.getName());
				try (FileInputStream fis = new FileInputStream(file); exchange)
				{
					if (contentType != null)
						exchange.getResponseHeaders().set("Content-Type", contentType);
					exchange.sendResponseHeaders(code, file.length());
					fis.transferTo(exchange.getResponseBody());
				}
				catch (Throwable t)
				{
					LOGGER.error(t);
				}
				break;
			}

			if (bodyObject instanceof String text)
			{
				if (contentType == null)
					contentType = "text/plain";
				try (exchange)
				{
					exchange.getResponseHeaders().set("Content-Type", contentType);
					byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
					exchange.sendResponseHeaders(code, bytes.length);
					exchange.getResponseBody().write(bytes);
				}
				catch (Throwable t)
				{
					LOGGER.error(t);
				}
				break;
			}

			if (bodyObject instanceof Redirection redir)
			{
				try (exchange)
				{
					exchange.getResponseHeaders().set("Location", redir.location);
					exchange.sendResponseHeaders(HttpURLConnection.HTTP_MOVED_TEMP, -1);
				}
				catch (Throwable t)
				{
					LOGGER.error(t);
				}
				break;
			}

			contentType = "text/html";
			code = 501;
			bodyObject = new File(WEB_ROOT, "501.html");
		}

		if (stop)
			this.server.stop(0);
	}

	public Object exchange(HttpExchange exchange)
	{
		String requestPath = exchange.getRequestURI().getPath();

		// Try web file
		TRY_RESPONSE_FILE:
		{
			File file = new File(WEB_ROOT, requestPath);
			if (!file.isFile())
				break TRY_RESPONSE_FILE;
			return file;
		}

		switch (requestPath)
		{
			case "/" ->
			{
				Redirection redirection = new Redirection();
				redirection.location = "/login";
				if (!this.authenticate(exchange))
					return redirection;
				return new File(WEB_ROOT, "index.html");
			}
			case "/login" ->
			{
				return new File(WEB_ROOT, "login.html");
			}
			case "/api/v1/login" ->
			{
				Json body = new Json();
				body.set(KEY_COD, 0);
				body.set(KEY_MSG, "OK");
				String token = exchange.getRequestHeaders().getFirst("Authorization");
				if (token == null)
				{
					body.set(KEY_COD, 400);
					body.set(KEY_MSG, "Bad Request: Authorization required");
					return body;
				}

				if (!token.equalsIgnoreCase(new String(WEBUI_TOKEN)))
				{
					body.set(KEY_COD, -1);
					body.set(KEY_MSG, "Wrong Token");
					return body;
				}

				MessageDigest md = MD5.get();

				byte[] bytes = new byte[32];
				RANDOM.nextBytes(bytes);
				md.update(bytes);
				md.update(token.getBytes());
				System.arraycopy(Hexadecimal.encode(md.digest()), 0, SESSIONID, 0, 32);
				String dig = new String(SESSIONID);
				Json data = new Json();
				data.set("session", dig);
				body.set(KEY_DAT, data);
				exchange.getResponseHeaders().set("Set-Cookie", COOKIE_JSESSIONID + '=' + dig + ";Path=/");
				try
				{
					saving();
				}
				catch (Throwable e)
				{
					LOGGER.error(e);
					return e;
				}
				return body;
			}
			case "/api/v1/stop" ->
			{
				if (!this.authenticate(exchange))
					return Json.resolve("{\"code\":\"-1\",\"message\":\"Unauthorized\"}");
				this.connection.close();
				Wednesday.SUBSCRIBE.cancel();
				Wednesday.SYNCHRONIZE.close();
				return new Stopping();
			}
			case "/api/v1/system/memory" ->
			{
				if (!this.authenticate(exchange))
					return Json.resolve("{\"code\":\"-1\",\"message\":\"Unauthorized\"}");
				String keyPid = "pid";
				String keyHeap = "heap";
				String keyNonHeap = "non-heap";
				String keyUsed = "used";
				String keyCommitted = "committed";
				String keyMax = "max";
				String keyRuntime = "runtime";
				String keyTotal = "total";
				String keyFree = "free";
				Json body = new Json();
				body.set(KEY_COD, 0);
				body.set(KEY_MSG, "OK");
				MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
				MemoryUsage heapUsage = memory.getHeapMemoryUsage();
				MemoryUsage nonHeapUsage = memory.getNonHeapMemoryUsage();
				Json data = new Json();
				Json heap = new Json();
				heap.set(keyUsed, heapUsage.getUsed());
				heap.set(keyCommitted, heapUsage.getCommitted());
				heap.set(keyMax, heapUsage.getMax());
				data.set(keyHeap, heap);
				Json nonHeap = new Json();
				nonHeap.set(keyUsed, nonHeapUsage.getUsed());
				nonHeap.set(keyCommitted, nonHeapUsage.getCommitted());
				nonHeap.set(keyMax, nonHeapUsage.getMax());
				data.set(keyNonHeap, nonHeap);
				Json runtime = new Json();
				runtime.set(keyTotal, Runtime.getRuntime().totalMemory());
				runtime.set(keyFree, Runtime.getRuntime().freeMemory());
				data.set(keyRuntime, runtime);
				data.set(keyPid, ProcessHandle.current().pid());
				body.set(KEY_DAT, data);

				Json pools = new Json(Json.TYPE_ARRAY);
				List<MemoryPoolMXBean> memoryPools = ManagementFactory.getMemoryPoolMXBeans();
				for (MemoryPoolMXBean memoryPool : memoryPools)
				{
					Json pool = new Json();
					pool.set("Name", memoryPool.getName());
					pool.set("type", memoryPool.getType().toString());
					MemoryUsage memoryUsage = memoryPool.getUsage();
					pool.set(keyUsed, memoryUsage.getUsed());
					pool.set(keyCommitted, memoryUsage.getCommitted());
					pool.set(keyMax, memoryUsage.getMax());
					pools.add(pool);
				}
				data.set("pools", pools);
				ThreadMXBean thread = ManagementFactory.getThreadMXBean();
				thread.getThreadInfo(0);
				return body;
			}
			case "/api/v1/system/token" ->
			{
				if (!this.authenticate(exchange))
					return Json.resolve("{\"code\":\"-1\",\"message\":\"Unauthorized\"}");
				Json body = new Json();
				body.set(KEY_COD, 0);
				body.set(KEY_MSG, "OK");
				String token = exchange.getRequestHeaders().getFirst("Authorization");
				if (token == null)
				{
					body.set(KEY_COD, 400);
					body.set(KEY_MSG, "Bad Request: Authorization required");
					return body;
				}

				if (!PATTERN_TOKEN.matcher(token).matches())
				{
					body.set(KEY_COD, -1);
					body.set(KEY_MSG, "Wrong Token");
					return body;
				}

				token = token.toUpperCase();
				System.arraycopy(token.getBytes(), 0, WEBUI_TOKEN, 0, WEBUI_TOKEN.length);
				try
				{
					saving();
				}
				catch (Throwable e)
				{
					LOGGER.error("Change Token: Saving Token Error", e);
					return e;
				}
				return body;
			}
			case "/api/v1/system/info" ->
			{
			}
			case "/api/v1/net/conn" ->
			{
				if (!this.authenticate(exchange))
					return Json.resolve("{\"code\":\"-1\",\"message\":\"Unauthorized\"}");
				Json body = new Json();
				body.set(KEY_COD, 0);
				body.set(KEY_MSG, "OK");

				Json request;
				try (InputStream bodyin = exchange.getRequestBody())
				{
					request = Json.resolve(bodyin);
				}
				catch (IOException e)
				{
					LOGGER.error(e);
					return e;
				}

				String action = request.string("action");
				request = request.get("data");

				if ("POST".equals(action))
				{
					if (request.string("name") == null || request.string("url") == null)
						return Json.resolve("{\"code\":-1,\"message\":\"Wrong Request\"}");
					ConnectionWednesday conn = ConnectionWednesday.resolve(request);
					this.connection.set(conn);
					return body;
				}
				else if ("GET".equals(action))
				{
					Json data = new Json(Json.TYPE_ARRAY);
					if (request.length() == 0)
						Stream.of(this.connection.all())
							.forEach(conn -> data.add(conn.data()));
					for (int i = 0; i < request.length(); i++)
					{
						ConnectionWednesday conn = this.connection.get(request.string(i));
						if (conn == null)
							continue;
						data.add(conn.data());
					}
					body.set(KEY_DAT, data);
					return body;
				}
				else if ("CONN".equals(action))
				{
					String name = request.string("name");
					if (name == null)
						return Json.resolve("{\"code\":-1,\"message\":\"Wrong Request\"}");
					ConnectionWednesday conn = this.connection.get(name);
					if (conn == null)
						return Json.resolve("{\"code\":-1,\"message\":\"Connection Not Found\"}");
					if (conn.connection != null)
						return body.set(KEY_DAT, conn.data());
					try
					{
						conn.connect(false);
						return body.set(KEY_DAT, conn.data());
					}
					catch (Throwable e)
					{
						LOGGER.error(e);
						return e;
					}
				}
				else if ("DISCONN".equals(action))
				{
					String name = request.string("name");
					if (name == null)
						return Json.resolve("{\"code\":-1,\"message\":\"Wrong Request\"}");
					ConnectionWednesday conn = this.connection.get(name);
					if (conn == null)
						return Json.resolve("{\"code\":-1,\"message\":\"Connection Not Found\"}");
					conn.close();
					return body;
				}
				else if ("DELETE".equals(action))
				{
					String name = request.string("name");
					if (name == null)
						return Json.resolve("{\"code\":-1,\"message\":\"Wrong Request\"}");
					ConnectionWednesday conn = this.connection.remove(name);
					if (conn == null)
						return Json.resolve("{\"code\":-1,\"message\":\"Connection Not Found\"}");
					return body.set(KEY_DAT, conn.data());
				}
			}
		}

		return null;
	}

	public boolean authenticate(HttpExchange exchange)
	{
		String cookieValue = exchange.getRequestHeaders().getFirst("Cookie");
		if (cookieValue == null)
			return false;
		Cookie cookie = new Cookie(cookieValue);
		String session = cookie.get(COOKIE_JSESSIONID);
		if (session == null || session.isEmpty())
			return false;
		return new String(SESSIONID).equals(session);
	}

	public void exhaust(InputStream in)
	{
		try
		{
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
	}

	public void close()
	{
		this.server.stop(0);
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
			dos.write(SESSIONID);
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
			web = new WednesdayWeb(new InetSocketAddress("0.0.0.0", 8000));
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
			RANDOM.nextBytes(SESSIONID);
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
			dis.readFully(SESSIONID);
		}
		catch (IOException e)
		{
			Mirroring.thrown(e);
			throw new RuntimeException(e);
		}
	}
}
