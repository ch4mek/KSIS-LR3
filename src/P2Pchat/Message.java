package P2Pchat;

import java.nio.charset.StandardCharsets;

public class Message {
    public static final byte TYPE_MESSAGE = 1;      // Chat message
    public static final byte TYPE_NAME = 2;         // Name exchange
    public static final byte TYPE_CONNECTED = 3;    // Peer connected notification
    public static final byte TYPE_DISCONNECTED = 4; // Peer disconnected notification
    public static final byte TYPE_HISTORY = 5;      // Chat history entry

    public static byte[] encode(byte type, String content) {
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        if (contentBytes.length > 255) {
            throw new IllegalArgumentException("Содержимое слишком длинное (макс. 255 байт): " + contentBytes.length);
        }
        byte[] data = new byte[2 + contentBytes.length];
        data[0] = type;
        data[1] = (byte) contentBytes.length;
        System.arraycopy(contentBytes, 0, data, 2, contentBytes.length);
        return data;
    }

    public static byte getType(byte[] data) {
        return data[0];
    }

    public static int getLength(byte[] data) {
        return data[1] & 0xFF; // Convert to unsigned
    }

    public static String getContent(byte[] data) {
        int length = getLength(data);
        return new String(data, 2, length, StandardCharsets.UTF_8);
    }
}
