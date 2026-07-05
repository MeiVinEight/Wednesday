package org.mve.orange.ws;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.mve.orange.OrangeAPI;
import org.mve.orange.core.Orange;
import org.mve.uni.Array;
import org.mve.uni.Json;
import org.slf4j.Logger;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class OrangeWS extends WebSocketClient
{
	public final Orange orange;
	public final Logger logger;

	public OrangeWS(Orange orange, URI serverUri, String token, Logger logger)
	{
		super(serverUri, Map.of("Authorization", "Bearer " + token));
		this.logger = logger;
		this.orange = orange;
	}

	@Override
	public void onOpen(ServerHandshake handshakedata)
	{
		this.logger.info("已连接到服务器: [{}] {}", handshakedata.getHttpStatus(), handshakedata.getHttpStatusMessage());
		this.orange.open(handshakedata);
	}

	@Override
	public void onMessage(String message)
	{
		String log = "<-- " + message;
		Json json = Json.resolve(message);
		Json echoJson = json.get(OrangeAPI.KEY_ECHO);
		if (echoJson != null && echoJson.type == Json.TYPE_NUMBER)
			log = "[" + json.get(OrangeAPI.KEY_ECHO) + "] " + log;
		this.logger.debug(log);
		this.orange.message(json);
	}

	@Override
	public void onClose(int code, String reason, boolean remote)
	{
		this.orange.close(code, reason, remote);
	}

	@Override
	public void onError(Exception ex)
	{
		this.orange.error(ex);
	}

	@Override
	public void send(byte[] data)
	{
		Array array = new Array(data.length);
		array.put(data);
		Json json = Json.resolve(array);
		String log = "--> " + new String(data, StandardCharsets.UTF_8);
		if (json.contains(OrangeAPI.KEY_ECHO))
			log = "[" + json.get(OrangeAPI.KEY_ECHO) + "] " + log;
		this.logger.debug(log);
		super.send(data);
	}

	@Override
	public void send(String text)
	{
		this.send(text.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public void send(ByteBuffer bytes)
	{
		byte[] buf = new byte[bytes.remaining()];
		bytes.get(buf);
		this.send(buf);
	}
}
