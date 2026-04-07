# Simplified P2P Chat Architecture Plan

## Overview

This document describes a **simplified** architecture for a console-based P2P chat application in Java. The goal is to minimize the number of classes while maintaining all required functionality.

## Current vs Simplified Architecture

| Current (10 classes) | Simplified (5 classes) |
|---------------------|------------------------|
| Type.java | (use byte constants) |
| Message.java | Message.java |
| History.java | History.java |
| PeerInfo.java | Peer.java |
| PeerNode.java | Node.java |
| TcpServer.java | (merged into Node.java) |
| UdpBroadcastListener.java | (merged into Node.java) |
| PeerConnectionHandler.java | (merged into Node.java) |
| Log.java | Console.java |
| Main.java | Main.java |

## Simplified Class Design

### 1. Main.java - Entry Point

**Responsibility:** Parse command line arguments and start the node.

```
+-------------------+
|      Main         |
+-------------------+
| - args: String[]  |
+-------------------+
| + main(args)      |
| - parseArgs(args) |
+-------------------+
```

**Command line parameters:**
- `args[0]` - IP address to bind to (e.g., 127.0.0.1)
- `args[1]` - Username (e.g., Alice)

### 2. Node.java - Main Peer Logic

**Responsibility:** 
- Start TCP server and UDP listener threads
- Handle peer discovery via UDP broadcast
- Manage TCP connections to peers
- Send/receive messages
- Handle graceful disconnection

```
+-------------------------------+
|           Node                |
+-------------------------------+
| - name: String                |
| - ip: String                  |
| - tcpPort: int                |
| - udpPort: int                |
| - peers: Map<String, Peer>    |
| - history: History            |
| - console: Console            |
| - running: boolean            |
+-------------------------------+
| + start()                     |
| + stop()                      |
| + sendMessage(text)           |
| - broadcastPresence()         |
| - connectToPeer(ip, port)     |
| - handleIncomingMessage(msg)  |
| - handleDisconnect(peer)      |
| - startTcpServer()            |
| - startUdpListener()          |
+-------------------------------+
```

### 3. Peer.java - Peer Connection

**Responsibility:** 
- Hold peer connection data (name, IP, socket, output stream)
- Provide methods to send messages via binary protocol

```
+-------------------------------+
|           Peer                |
+-------------------------------+
| - name: String                |
| - ip: String                  |
| - port: int                   |
| - socket: Socket              |
| - out: OutputStream           |
+-------------------------------+
| + Peer(socket)                |
| + send(type, data)            |
| + close()                     |
| + getKey()                    |
+-------------------------------+
```

### 4. Message.java - Binary Protocol

**Responsibility:** 
- Encode/decode messages in binary format
- Support message types: NAME(2), MESSAGE(1), CONNECTED(3), DISCONNECTED(4)

**Binary format:**
```
Offset 0: Message type (1=message, 2=name, 3=connected, 4=disconnected)
Offset 1: Message length (n bytes)
Offset 2..n+1: Message content (UTF-8 encoded)
```

```
+-------------------------------+
|          Message              |
+-------------------------------+
| + TYPE_MESSAGE: byte = 1      |
| + TYPE_NAME: byte = 2         |
| + TYPE_CONNECTED: byte = 3    |
| + TYPE_DISCONNECTED: byte = 4 |
+-------------------------------+
| + encode(type, content): byte[]|
| + decode(data): Message       |
| + getType(): byte             |
| + getContent(): String        |
+-------------------------------+
```

### 5. History.java - Event Log

**Responsibility:** 
- Store events chronologically with timestamps
- Events: incoming messages, sent messages, peer discovery, peer disconnection

```
+-------------------------------+
|          History              |
+-------------------------------+
| - entries: List<Entry>        |
+-------------------------------+
| + addMessage(name, ip, text)  |
| + addSentMessage(text)        |
| + addPeerConnected(name, ip)  |
| + addPeerDisconnected(name, ip)|
| + getAll(): List<String>      |
+-------------------------------+
```

### 6. Console.java - User Interface

**Responsibility:** 
- Read keyboard input
- Display event history
- Handle /exit command

```
+-------------------------------+
|          Console              |
+-------------------------------+
| - node: Node                  |
| - history: History            |
| - scanner: Scanner            |
+-------------------------------+
| + start()                     |
| + display(message)            |
| - readInput()                 |
+-------------------------------+
```

## Message Flow Diagrams

### 1. Startup Flow

```mermaid
sequenceDiagram
    participant U as User
    participant N as Node
    participant T as TCP Server Thread
    participant D as UDP Listener Thread
    
    U->>N: java Main 127.0.0.1 Alice
    N->>T: Start TCP server on port 9000
    N->>D: Start UDP listener on port 8888
    N->>N: Broadcast presence via UDP
    Note over N: Ready to receive connections
```

### 2. Peer Discovery Flow

```mermaid
sequenceDiagram
    participant N1 as Node1 - Existing
    participant N2 as Node2 - New
    
    Note over N2: Node2 starts
    N2->>N2: Broadcast UDP: Alice:127.0.0.1:9000
    N2-->>N1: UDP broadcast received
    N1->>N1: Parse: name, ip, tcpPort
    N1->>N2: TCP connect to 127.0.0.1:9000
    N1->>N1: Create Peer object
    N1->>N2: Send NAME message: Bob
    N2->>N2: Create Peer for Bob
    N2->>N2: Add to history: Bob connected
    N1->>N1: Add to history: Alice connected
```

### 3. Messaging Flow

```mermaid
sequenceDiagram
    participant U1 as User Alice
    participant N1 as Node1
    participant N2 as Node2
    participant U2 as User Bob
    
    U1->>N1: Type: Hello everyone
    N1->>N1: Add to history: sent message
    N1->>N2: Send MESSAGE: Hello everyone
    N2->>N2: Add to history: Alice: Hello everyone
    N2->>U2: Display: [12:30:45] Alice: Hello everyone
    
    U2->>N2: Type: Hi Alice
    N2->>N2: Add to history: sent message
    N2->>N1: Send MESSAGE: Hi Alice
    N1->>N1: Add to history: Bob: Hi Alice
    N1->>U1: Display: [12:30:50] Bob: Hi Alice
```

### 4. Disconnect Flow

```mermaid
sequenceDiagram
    participant N1 as Node1
    participant N2 as Node2
    
    Note over N1: User types /exit
    N1->>N1: Close all peer connections
    N1->>N2: TCP connection closed
    N2->>N2: Detect connection close
    N2->>N2: Remove peer from map
    N2->>N2: Add to history: Alice disconnected
    N2->>N2: Display disconnection
```

## Class Diagram

```mermaid
classDiagram
    class Main {
        +main(args)
    }
    
    class Node {
        -String name
        -String ip
        -int tcpPort
        -int udpPort
        -Map peers
        -History history
        -Console console
        +start()
        +stop()
        +sendMessage(text)
    }
    
    class Peer {
        -String name
        -String ip
        -int port
        -Socket socket
        -OutputStream out
        +send(type, data)
        +close()
        +getKey()
    }
    
    class Message {
        +TYPE_MESSAGE byte
        +TYPE_NAME byte
        +TYPE_CONNECTED byte
        +TYPE_DISCONNECTED byte
        +encode(type, content) byte[]
        +decode(data) Message
    }
    
    class History {
        -List entries
        +addMessage(name, ip, text)
        +addSentMessage(text)
        +addPeerConnected(name, ip)
        +addPeerDisconnected(name, ip)
        +getAll() List
    }
    
    class Console {
        -Node node
        -History history
        -Scanner scanner
        +start()
        +display(message)
    }
    
    Main --> Node : creates
    Node --> Peer : manages
    Node --> History : uses
    Node --> Console : uses
    Peer --> Message : sends
```

## Project Structure

```
KSIS-3LR/
├── src/
│   └── P2Pchat/
│       ├── Main.java           - Entry point, arg parsing
│       ├── Node.java           - Main peer logic, TCP/UDP handling
│       ├── Peer.java           - Peer connection wrapper
│       ├── Message.java        - Binary message encoding/decoding
│       ├── History.java        - Event history with timestamps
│       └── Console.java        - Console UI, input handling
├── plans/
│   └── p2p-chat-plan.md
└── README.md
```

## Testing

To test with multiple instances on the same machine using loopback addresses:

```bash
# Terminal 1 - Alice
java -cp out P2Pchat.Main 127.0.0.1 Alice

# Terminal 2 - Bob  
java -cp out P2Pchat.Main 127.0.0.2 Bob

# Terminal 3 - Charlie
java -cp out P2Pchat.Main 127.0.0.3 Charlie
```

## Implementation Notes

### Binary Protocol Details

**Message Encoding:**
```java
// Example: Encoding a text message
byte[] data = new byte[2 + content.getBytes().length];
data[0] = TYPE_MESSAGE;           // 1
data[1] = content.length;         // message length
System.arraycopy(content.getBytes(), 0, data, 2, content.length);
```

**Message Decoding:**
```java
// Example: Decoding a message
byte type = data[0];
int length = data[1];
String content = new String(data, 2, length);
```

### Thread Model

1. **Main Thread** - Console input loop
2. **TCP Server Thread** - Accepts incoming connections
3. **UDP Listener Thread** - Receives discovery broadcasts
4. **Per-Peer Thread** - Reads incoming messages from each connected peer

### Connection Deduplication

Use `ip:port` as unique key to prevent duplicate connections:
- Check if peer already exists before connecting
- Handle race condition when both nodes try to connect simultaneously

### History Format

```
[HH:mm:ss] Alice (127.0.0.1): Hello everyone
[HH:mm:ss] >>> Hello everyone
[HH:mm:ss] Bob (127.0.0.2) connected
[HH:mm:ss] Bob (127.0.0.2) disconnected
```

## Summary of Simplifications

1. **Removed Type.java** - Use byte constants in Message.java
2. **Merged TcpServer.java, UdpBroadcastListener.java, PeerConnectionHandler.java into Node.java** - All network handling in one class
3. **Renamed Log.java to Console.java** - Clearer naming
4. **Renamed PeerInfo.java to Peer.java** - Simpler naming
5. **Removed history transfer feature** - Not required for basic functionality
6. **Simplified command line args** - Only IP and name required (ports can be fixed)

**Result: 5 classes instead of 10, with clearer responsibilities and simpler interactions.**
