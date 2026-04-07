package P2Pchat;

import java.io.IOException;
import java.net.*;

import static P2Pchat.Main.UDP_PORT;

public class UdpServer {
    private final DatagramSocket socket;
    private final String bindIp;
    private final String username;
    private final Node node;

    public UdpServer(String bindIp, String username, Node node) throws IOException {
        this.bindIp = bindIp;
        this.username = username;
        this.node = node;
        InetAddress bindAddress = InetAddress.getByName(bindIp);
        this.socket = new DatagramSocket(UDP_PORT, bindAddress);
        this.socket.setBroadcast(true);
    }

    public void start() {
        Thread listenerThread = new Thread(this::listenLoop, "UDP-Listener");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void listenLoop() {
        byte[] buffer = new byte[256];
        while (node.isRunning()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String fromIp = packet.getAddress().getHostAddress();
                String message = new String(packet.getData(), 0, packet.getLength());

                if (fromIp.equals(bindIp)) {
                    continue;
                }

                node.onUdpBroadcast(fromIp, message);
            } catch (IOException e) {
                if (node.isRunning()) {
                    System.err.println("Ошибка UDP приёма: " + e.getMessage());
                }
            }
        }
    }

    public void broadcastPresence() {
        try {
            String message = username;
            byte[] data = message.getBytes();

            DatagramPacket packet = new DatagramPacket(
                data, data.length,
                InetAddress.getByName("255.255.255.255"), UDP_PORT
            );
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("Не удалось разослать присутствие: " + e.getMessage());
        }
    }

    public void close() {
        socket.close();
    }

    public String getBindIp() {
        return bindIp;
    }
}
