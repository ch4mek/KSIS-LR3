package P2Pchat;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static P2Pchat.Main.TCP_PORT;


public class Node {
    private final String bindIp;
    private final String username;
    private final ConcurrentHashMap<String, Peer> peers = new ConcurrentHashMap<>();
    private final History history = new History();
    private final AtomicBoolean running = new AtomicBoolean(true);
    
    private TcpServer tcpServer;
    private UdpServer udpServer;

    public Node(String bindIp, String username) throws IOException {
        this.bindIp = bindIp;
        this.username = username;
    }


    public void start() {
        try {
            tcpServer = new TcpServer(bindIp, TCP_PORT, this);
            tcpServer.start();

            udpServer = new UdpServer(bindIp, username, this);
            udpServer.start();
            
            System.out.println("Узел запущен на " + bindIp);

            udpServer.broadcastPresence();

            Console console = new Console(this, history);
            console.start();

            stop();
        } catch (IOException e) {
            System.err.println("Ошибка запуска узла: " + e.getMessage());
        }
    }

    public void stop() {
        running.set(false);

        for (Peer peer : peers.values()) {
            peer.close();
        }
        peers.clear();

        if (tcpServer != null) {
            tcpServer.close();
        }
        if (udpServer != null) {
            udpServer.close();
        }
        
        System.out.println("Узел остановлен.");
    }

    public void broadcastMessage(String text) {
        byte[] data = Message.encode(Message.TYPE_MESSAGE, text);
        for (Peer peer : peers.values()) {
            peer.sendMessage(data);
        }
        history.addSentMessage(text);
    }

    public String getUsername() {
        return username;
    }

    public boolean isRunning() {
        return running.get();
    }

    public void onUdpBroadcast(String fromIp, String name) {

        String key = fromIp + ":" + TCP_PORT;
        if (peers.containsKey(key)) {
            return;
        }
        
        try {

            Peer peer = new Peer(fromIp, TCP_PORT, bindIp);
            peer.setName(name);

            byte[] nameMsg = Message.encode(Message.TYPE_NAME, username);
            peer.sendMessage(nameMsg);

            sendHistoryToPeer(peer);

            peers.put(key, peer);

            startPeerReader(peer);
            
            history.addPeerDiscovered(name, fromIp);
        } catch (IOException e) {
            System.err.println("Не удалось подключиться к узлу " + fromIp + ": " + e.getMessage());
        }
    }

    public void onTcpConnection(Socket socket, String name, String peerIp) {
        try {
            Peer peer = new Peer(socket);
            peer.setName(name);
            
            String key = peer.getKey();

            if (peers.containsKey(key)) {
                peer.close();
                return;
            }

            peers.put(key, peer);

            startPeerReader(peer);
            
            history.addPeerConnected(peer.getName(), peer.getIp());
        } catch (IOException e) {
            System.err.println("Ошибка при создании соединения с узлом: " + e.getMessage());
        }
    }

    private void sendHistoryToPeer(Peer peer) {
        List<String> chatHistory = history.getChatMessagesForHistory(username, bindIp);
        for (String entry : chatHistory) {
            byte[] historyMsg = Message.encode(Message.TYPE_HISTORY, entry);
            peer.sendMessage(historyMsg);
        }
    }

    private void startPeerReader(Peer peer) {
        Thread readerThread = new Thread(() -> peerReaderLoop(peer), "Peer-Reader-" + peer.getKey());
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private void peerReaderLoop(Peer peer) {
        try {
            InputStream in = peer.getSocket().getInputStream();
            byte[] header = new byte[2];
            
            while (running.get()) {
                int read = in.read(header);
                if (read == -1) {
                    break;
                }
                if (read < 2) {
                    readFully(in, header, read);
                }
                
                byte type = Message.getType(header);
                int length = Message.getLength(header);

                byte[] content = new byte[length];
                readFully(in, content);

                byte[] fullData = new byte[2 + length];
                System.arraycopy(header, 0, fullData, 0, 2);
                System.arraycopy(content, 0, fullData, 2, length);
                
                onPeerMessageReceived(peer, fullData);
            }
        } catch (IOException e) {
        } finally {
            onPeerDisconnected(peer);
        }
    }

    private void onPeerMessageReceived(Peer peer, byte[] data) {
        byte type = Message.getType(data);
        String content = Message.getContent(data);
        
        switch (type) {
            case Message.TYPE_MESSAGE:
                history.addMessage(peer.getName(), peer.getIp(), content);
                break;
            case Message.TYPE_NAME:
                peer.setName(content);
                break;
            case Message.TYPE_HISTORY:
                history.addHistoryEntry(content);
                break;
            case Message.TYPE_DISCONNECTED:
                onPeerDisconnected(peer);
                break;
        }
    }

    private void onPeerDisconnected(Peer peer) {
        String key = peer.getKey();
        if (peers.remove(key) != null) {
            history.addPeerDisconnected(peer.getName(), peer.getIp());
            peer.close();
        }
    }

    private void readFully(InputStream in, byte[] buffer, int offset) throws IOException {
        int remaining = buffer.length - offset;
        while (remaining > 0) {
            int read = in.read(buffer, offset, remaining);
            if (read == -1) {
                throw new IOException("Unexpected end of stream");
            }
            offset += read;
            remaining -= read;
        }
    }

    private void readFully(InputStream in, byte[] buffer) throws IOException {
        readFully(in, buffer, 0);
    }
}
