package P2Pchat;


import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer {
    private final ServerSocket serverSocket;
    private final String bindIp;
    private final int port;
    private final Node node;

    public TcpServer(String bindIp, int port, Node node) throws IOException {
        this.bindIp = bindIp;
        this.port = port;
        this.node = node;
        InetAddress bindAddress = InetAddress.getByName(bindIp);
        this.serverSocket = new ServerSocket(port, 50, bindAddress);
    }


    public void start() {
        Thread acceptorThread = new Thread(this::acceptLoop, "TCP-Acceptor");
        acceptorThread.setDaemon(true);
        acceptorThread.start();
    }


    private void acceptLoop() {
        while (node.isRunning()) {
            try {
                Socket socket = serverSocket.accept();
                handleConnection(socket);
            } catch (IOException e) {
                if (node.isRunning()) {
                    System.err.println("Ошибка TCP приёма: " + e.getMessage());
                }
            }
        }
    }

    private void handleConnection(Socket socket) {
        try {
            String peerIp = socket.getInetAddress().getHostAddress();
            int peerPort = socket.getPort();
            String key = peerIp + ":" + peerPort;

            InputStream in = socket.getInputStream();
            byte[] header = new byte[2];
            readFully(in, header);

            byte type = Message.getType(header);
            int length = Message.getLength(header);

            String name = null;
            if (type == Message.TYPE_NAME) {
                byte[] content = new byte[length];
                readFully(in, content);
                name = new String(content);
            }

            node.onTcpConnection(socket, name, peerIp);
        } catch (IOException e) {
            System.err.println("Ошибка при обработке входящего соединения: " + e.getMessage());
        }
    }

    public void close() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            // Ignore
        }
    }

    private void readFully(InputStream in, byte[] buffer) throws IOException {
        int remaining = buffer.length;
        int offset = 0;
        while (remaining > 0) {
            int read = in.read(buffer, offset, remaining);
            if (read == -1) {
                throw new IOException("Unexpected end of stream");
            }
            offset += read;
            remaining -= read;
        }
    }

    public String getBindIp() {
        return bindIp;
    }

    public int getPort() {
        return port;
    }
}
