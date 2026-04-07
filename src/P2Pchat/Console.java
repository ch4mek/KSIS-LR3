package P2Pchat;

import java.util.Scanner;

public class Console {
    private final Node node;
    private final History history;
    private final Scanner scanner;

    public Console(Node node, History history) {
        this.node = node;
        this.history = history;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println();
        System.out.println("=== P2P Чат запущен ===");
        System.out.println("Введите сообщение и нажмите Enter для отправки.");
        System.out.println("Введите /exit для выхода.");
        System.out.println("========================");
        System.out.println();

        while (node.isRunning()) {
            try {
                String line = scanner.nextLine();
                
                if (line == null || line.equalsIgnoreCase("/exit")) {
                    break;
                }
                
                if (!line.trim().isEmpty()) {
                    node.broadcastMessage(line.trim());
                }
            } catch (Exception e) {
                if (node.isRunning()) {
                    System.err.println("Console error: " + e.getMessage());
                }
                break;
            }
        }
    }

    public void display(String message) {
        System.out.println(message);
    }
}
