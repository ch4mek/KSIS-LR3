# P2P Chat - Учебный проект

P2P консольный чат на Java с использованием TCP для обмена сообщениями и UDP для обнаружения узлов.

## Требования

- Java 8 или выше
- Поддержка multicast/broadcast в сети

## Компиляция

```bash
cd src
javac P2Pchat/*.java
```

## Запуск

```bash
java P2Pchat.Main <bind_ip> <username>
```

## Аргументы командной строки

| Параметр | Описание |
|----------|----------|
| `bind_ip` | IP-адрес для привязки сокетов (127.0.0.1, 127.0.0.2, etc.) |
| `username` | Имя пользователя в чате |

## Порты

| Порт | Назначение |
|------|------------|
| TCP 9000 | Обмен сообщениями |
| UDP 8888 | Обнаружение узлов |

## Протокол

```
Binary message format:
[type:1byte][length:1byte][content:utf-8 bytes]

Message types:
1 - Chat message
2 - Name exchange
3 - Peer connected
4 - Peer disconnected
```

## Функции

- Автоматическое обнаружение узлов в сети
- История сообщений с временными метками
- Корректная обработка отключений
- Команда `/exit` для выхода

## Архитектура

| Файл | Назначение |
|------|------------|
| [`Main.java`](src/P2Pchat/Main.java) | Entry point, парсинг аргументов |
| [`Node.java`](src/P2Pchat/Node.java) | Основная логика P2P узла |
| [`TcpServer.java`](src/P2Pchat/TcpServer.java) | TCP сервер для входящих соединений |
| [`UdpServer.java`](src/P2Pchat/UdpServer.java) | UDP сервер для обнаружения узлов |
| [`Peer.java`](src/P2Pchat/Peer.java) | Соединение с другим узлом |
| [`Message.java`](src/P2Pchat/Message.java) | Бинарный протокол |
| [`History.java`](src/P2Pchat/History.java) | Лог событий |
| [`Console.java`](src/P2Pchat/Console.java) | Пользовательский интерфейс |

## Тестирование на одном компьютере

Для тестирования можно запустить несколько экземпляров программы на разных loopback адресах:

### Шаг 1: Скомпилируйте проект
```bash
cd src
javac P2Pchat/*.java
```

### Шаг 2: Откройте несколько терминалов

**Терминал 1 (Alice):**
```bash
cd src
java P2Pchat.Main 127.0.0.1 Alice
```

**Терминал 2 (Bob):**
```bash
cd src
java P2Pchat.Main 127.0.0.2 Bob
```

**Терминал 3 (Charlie) - опционально:**
```bash
cd src
java P2Pchat.Main 127.0.0.3 Charlie
```

### Примечание

Loopback адреса (127.0.0.1, 127.0.0.2, 127.0.0.3 и т.д.) позволяют эмулировать несколько узлов на одном компьютере. Каждый узел будет использовать одинаковые порты (TCP 9000, UDP 8888), но разные IP-адреса.
