package org.mve.coffee;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.mve.Configuration;
import org.mve.Json;
import org.mve.logging.LoggerManager;
import org.mve.logging.WednesdayLogger;
import org.mve.uni.Hexadecimal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.LogManager;

public class Coffee implements HttpHandler
{
	public static final WednesdayLogger LOGGER = LoggerManager.create(null, Configuration.LOG_LEVEL);
	private final HttpServer server;

	public Coffee() throws IOException
	{
		this.server = HttpServer.create(new InetSocketAddress(Configuration.COFFEE_HOST, Configuration.COFFEE_PORT), 0);
		server.createContext("/", this);
		server.start();
		Coffee.LOGGER.info("Coffee start at http://{}:{}", Configuration.COFFEE_HOST, Configuration.COFFEE_PORT);
	}

	@Override
	public void handle(HttpExchange exchange)
	{
		URI requestURI = exchange.getRequestURI();
		String requestMethod = exchange.getRequestMethod();
		Coffee.LOGGER.info("{} {} {}", exchange.getProtocol(), requestMethod, requestURI);
		String path = requestURI.getPath();

		int code = 200;
		String responseMessage = null;
		Json body = new Json();

		boolean notFound = true;
		boolean stopping = false;

		HANLE_REQUEST:
		{
			if ("/stop".equals(path))
			{
				notFound = false;
				stopping = true;
				responseMessage = "OK";
			}

			if ("/upload".equals(path))
			{
				notFound = false;
				String downloadUrl = exchange.getRequestHeaders().getFirst("Download-From");
				if (downloadUrl == null)
				{
					code = 400;
					responseMessage = "Download-From is required in header";
					break HANLE_REQUEST;
				}
				Coffee.LOGGER.debug("<--{}", downloadUrl);

				String downloadToPath = exchange.getRequestHeaders().getFirst("Download-To");
				if (downloadToPath == null)
				{
					code = 400;
					responseMessage = "Download-To is required in header";
					break HANLE_REQUEST;
				}

				String downloadHeaders = exchange.getRequestHeaders().getFirst("Download-Headers");

				code = 500;

				File downloadTo = new File(Configuration.COFFEE_UPLOAD, downloadToPath);
				Coffee.LOGGER.debug("-->{}", downloadTo.getAbsolutePath());
				if (downloadTo.isDirectory())
				{
					responseMessage = "Path is directory";
					break HANLE_REQUEST;
				}

				MessageDigest md5;
				MessageDigest sha1;
				try
				{
					md5 = MessageDigest.getInstance("MD5");
					sha1 = MessageDigest.getInstance("SHA1");
				}
				catch (NoSuchAlgorithmException e)
				{
					responseMessage = e.toString();
					Coffee.LOGGER.debug(e);
					break HANLE_REQUEST;
				}

				if (downloadTo.isFile())
				{
					code = 600;
					responseMessage = "File already exists";
					Json data = new Json(Json.TYPE_OBJECT);

					String md5Value = "";
					String shaValue = "";
					String exception = "";
					long fileSize = 0;
					try (FileInputStream fin = new FileInputStream(downloadTo))
					{
						byte[] buffer = new byte[4096];
						int read;
						while ((read = fin.read(buffer, 0, buffer.length)) >= 0)
						{
							md5.update(buffer, 0, read);
							sha1.update(buffer, 0, read);
							fileSize += read;
						}
						md5Value = new String(Hexadecimal.encode(md5.digest()));
						shaValue = new String(Hexadecimal.encode(sha1.digest()));
						md5.reset();
						sha1.reset();
					}
					catch (IOException e)
					{
						exception = e.toString();
						Coffee.LOGGER.debug(e);
					}
					data.set("size", fileSize);
					data.set("MD5", md5Value);
					data.set("SHA1", shaValue);
					data.set("exception", exception);
					body.set("data", data);
					break HANLE_REQUEST;
				}

				File parentFile = downloadTo.getParentFile();
				boolean ignored = parentFile.mkdirs();
				if (!parentFile.isDirectory())
				{
					responseMessage = "Unable to create directory";
					break HANLE_REQUEST;
				}

				URL url;

				try
				{
					url = new URL(downloadUrl);
				}
				catch (MalformedURLException e)
				{
					responseMessage = e.toString();
					Coffee.LOGGER.debug(e);
					break HANLE_REQUEST;
				}

				URLConnection connection;
				try
				{
					connection = url.openConnection();
					if (downloadHeaders != null)
					{
						Json downHeaders = Json.resolve(downloadHeaders);
						Coffee.LOGGER.debug("HEADERS:");
						downHeaders.foreach((k, v) ->
						{
							Coffee.LOGGER.debug("  {}: {}", k, v.string());
							connection.setRequestProperty(k, v.string());
						});
					}
				}
				catch (IOException e)
				{
					responseMessage = e.toString();
					Coffee.LOGGER.debug(e);
					break HANLE_REQUEST;
				}

				long fileSize = 0;
				try (InputStream in = connection.getInputStream(); FileOutputStream out = new FileOutputStream(downloadTo))
				{
					byte[] buffer = new byte[4096];
					int read;
					while ((read = in.read(buffer, 0, buffer.length)) >= 0)
					{
						out.write(buffer, 0, read);
						md5.update(buffer, 0, read);
						sha1.update(buffer, 0, read);
						fileSize += read;
					}
					// fileSize = in.transferTo(out);
				}
				catch (IOException e)
				{
					responseMessage = e.toString();
					Coffee.LOGGER.debug(e);
					break HANLE_REQUEST;
				}

				code = 200;
				responseMessage = "OK";
				Json data = new Json(Json.TYPE_OBJECT);
				data.set("size", fileSize);
				data.set("MD5", new String(Hexadecimal.encode(md5.digest())));
				data.set("SHA1", new String(Hexadecimal.encode(sha1.digest())));
				body.set("data", data);
			}
		}

		try
		{
			if (notFound)
			{
				String notFoundResp = "<h1>404 Not Found</h1>";
				exchange.getResponseHeaders().set("Content-Type", "text/html");
				exchange.sendResponseHeaders(404, notFoundResp.length());
				exchange.getResponseBody().write(notFoundResp.getBytes());
				exchange.close();
				Coffee.LOGGER.debug("RESPONSE: {} {}", 404, notFoundResp);
				return;
			}

			body.set("code", code);
			body.set("message", responseMessage);
			String bodyValue = body.stringify();
			exchange.getResponseHeaders().set("Content-Type", "application/json");
			exchange.sendResponseHeaders(code, bodyValue.length());
			exchange.getResponseBody().write(bodyValue.getBytes(StandardCharsets.UTF_8));
			exchange.close();
			Coffee.LOGGER.debug("RESPONSE: {} {}", code, bodyValue);
		}
		catch (IOException e)
		{
			LOGGER.warn("Error occurred while response", e);
		}

		if (stopping)
			this.server.stop(0);
	}

	public static void main(String[] args)
	{
		try
		{
			LogManager.getLogManager();
			new Coffee();
		}
		catch (IOException e)
		{
			Coffee.LOGGER.error(e);
		}
	}
}
