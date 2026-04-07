package P2Pchat;

public class Main {
    public static final int TCP_PORT = 9000;
    public static final int UDP_PORT = 8888;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Использование: java Main <ip_адрес> <имя_пользователя>");
            System.err.println("Пример: java Main 127.0.0.1 Alice");
            System.exit(1);
        }

        String bindIp = args[0];
        String username = args[1];

        System.out.println("Запуск P2P Чата...");
        System.out.println("IP адрес: " + bindIp);
        System.out.println("Имя пользователя: " + username);
        System.out.println("TCP порт: " + TCP_PORT);
        System.out.println("UDP порт: " + UDP_PORT);
        System.out.println();

        try {
            Node node = new Node(bindIp, username);
            node.start();
        } catch (Exception e) {
            System.err.println("Ошибка запуска узла: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
