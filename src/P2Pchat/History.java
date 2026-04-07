package P2Pchat;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class History {
    private final List<String> entries = new ArrayList<>();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public synchronized void addMessage(String name, String ip, String text) {
        String entry = formatTime() + " " + name + " (" + ip + "): " + text;
        entries.add(entry);
        System.out.println(entry);
    }

    public synchronized void addSentMessage(String text) {
        String entry = formatTime() + " Вы: " + text;
        entries.add(entry);
        System.out.println(entry);
    }


    public synchronized void addPeerConnected(String name, String ip) {
        String entry = formatTime() + " Узел подключился: " + name + " (" + ip + ")";
        entries.add(entry);
        System.out.println(entry);
    }


    public synchronized void addPeerDisconnected(String name, String ip) {
        String entry = formatTime() + " Узел отключился: " + name + " (" + ip + ")";
        entries.add(entry);
        System.out.println(entry);
    }


    public synchronized void addPeerDiscovered(String name, String ip) {
        String entry = formatTime() + " Обнаружен узел: " + name + " (" + ip + ")";
        entries.add(entry);
        System.out.println(entry);
    }


    public synchronized void addEvent(String description) {
        String entry = formatTime() + " " + description;
        entries.add(entry);
        System.out.println(entry);
    }


    public synchronized List<String> getAll() {
        return new ArrayList<>(entries);
    }
    

    public synchronized List<String> getChatMessagesForHistory(String senderName, String senderIp) {
        List<String> chatMessages = new ArrayList<>();
        for (String entry : entries) {
            boolean containsParenColon = entry.contains("): ");
            boolean containsConnected = entry.contains("подключился");
            boolean containsDisconnected = entry.contains("отключился");
            boolean containsDiscovered = entry.contains("Обнаружен");
            boolean isOutgoing = entry.contains("] Вы: ");
            
             if ((containsParenColon || isOutgoing) && !containsConnected && !containsDisconnected && !containsDiscovered) {
               if (isOutgoing) {
                    entry = entry.replace("] Вы: ", "] " + senderName + " (" + senderIp + "): ");
                }
                chatMessages.add(entry);
            }
        }
       chatMessages.sort(Comparator.comparing(this::extractTimestamp));
        return chatMessages;
    }

    private String extractTimestamp(String entry) {
        if (entry != null && entry.length() >= 10 && entry.startsWith("[")) {
            int endBracket = entry.indexOf("]");
            if (endBracket > 0) {
                return entry.substring(1, endBracket);
            }
        }
        return "";
    }

    public synchronized void addHistoryEntry(String entry) {
        entries.add(entry);
        System.out.println(entry);
    }

    private String formatTime() {
        return "[" + LocalTime.now().format(timeFormatter) + "]";
    }
}
