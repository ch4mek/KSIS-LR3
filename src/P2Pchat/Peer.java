package P2Pchat;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Peer {
    private String name;
    private final String ip;
    private final int port;
    private final Socket socket;
    private final OutputStream out;

    public Peer(Socket socket) throws IOException {
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostAddress();
        this.port = socket.getPort();
        this.out = socket.getOutputStream();
        this.name = null;
    }

    public Peer(String ip, int port) throws IOException {
        this.ip = ip;
        this.port = port;
        this.socket = new Socket(ip, port);
        this.out = socket.getOutputStream();
        this.name = null;
    }

    public Peer(String remoteIp, int port, String localIp) throws IOException {
        this.ip = remoteIp;
        this.port = port;
        this.socket = new Socket();
        this.socket.bind(new java.net.InetSocketAddress(localIp, 0));
        this.socket.connect(new java.net.InetSocketAddress(remoteIp, port));
        this.out = socket.getOutputStream();
        this.name = null;
    }


    public synchronized void sendMessage(byte[] data) {
        try {
            out.write(data);
            out.flush();
        } catch (IOException e) {
            System.err.println("Не удалось отправить сообщение узлу " + ip + ": " + e.getMessage());
        }
    }


    public synchronized void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Ошибка при закрытии соединения с узлом: " + e.getMessage());
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getKey() {
        return ip + ":" + port;
    }

    @Override
    public String toString() {
        return name != null ? name + " (" + ip + ")" : ip;
    }
}
