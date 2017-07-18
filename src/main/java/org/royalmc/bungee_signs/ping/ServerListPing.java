package org.royalmc.bungee_signs.ping;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ServerListPing {

	private final static Gson gson = new Gson();

	private InetSocketAddress host;
	private int timeout = 2000;

	public ServerListPing(InetSocketAddress host) {
		this.host = host;
	}

	public ServerStatus fetchData() throws IOException {
		try (Socket socket = new Socket()) {

			socket.setSoTimeout(timeout);
			socket.connect(host, timeout);

			try (OutputStream outputStream = socket.getOutputStream();
					DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
					InputStream inputStream = socket.getInputStream();
					DataInputStream dataInputStream = new DataInputStream(inputStream)) {

				sendPacket(dataOutputStream, prepareHandshake());
				sendPacket(dataOutputStream, new byte[] {0x00});

				return receiveResponse(dataInputStream);
			}
		}
	}

	private ServerStatus receiveResponse(DataInputStream dataInputStream) throws IOException {
		readVarInt(dataInputStream);
		int packetId = readVarInt(dataInputStream);

		if (packetId != 0x00) {
			throw new IOException("Invalid packetId");
		}

		int stringLength = readVarInt(dataInputStream);

		if (stringLength < 1) {
			throw new IOException("Invalid string length.");
		}

		byte[] responseData = new byte[stringLength];
		dataInputStream.readFully(responseData);
		String jsonString = new String(responseData, "UTF-8");
		return gson.fromJson(jsonString, ServerStatus.class);
	}

	private void sendPacket(DataOutputStream out, byte[] data) throws IOException {
		writeVarInt(out, data.length);
		out.write(data);
	}

	private byte[] prepareHandshake() throws IOException {
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); DataOutputStream handshake = new DataOutputStream(byteArrayOutputStream)) {
			byteArrayOutputStream.write(0x00);
			writeVarInt(handshake, 4);
			writeString(handshake, host.getHostString());
			handshake.writeShort(host.getPort());
			writeVarInt(handshake, 1);
			return byteArrayOutputStream.toByteArray();
		}
	}

	public void writeString(DataOutputStream out, String string) throws IOException {
		writeVarInt(out, string.length());
		out.write(string.getBytes("UTF-8"));
		out.close();
	}

	public int readVarInt(DataInputStream in) throws IOException {
		int i = 0;
		int j = 0;
		while (true) {
			int k = in.readByte();
			i |= (k & 0x7F) << j++ * 7;
			if (j > 5) {
				throw new RuntimeException("VarInt too big");
			}
			if ((k & 0x80) != 128) {
				break;
			}
		}
		return i;
	}

	public void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
		while (true) {
			if ((paramInt & 0xFFFFFF80) == 0) {
				out.write(paramInt);
				return;
			}

			out.write(paramInt & 0x7F | 0x80);
			paramInt >>>= 7;
		}
	}
}