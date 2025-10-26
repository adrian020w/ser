#!/usr/bin/env python3
import socket
import threading
import logging
from datetime import datetime
import time

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - STEALTH - %(message)s',
    handlers=[
        logging.FileHandler('stealth_server.log'),
        logging.StreamHandler()
    ]
)

class StealthServer:
    def __init__(self, host='0.0.0.0', port=8080):
        self.host = host
        self.port = port
        self.clients = {}
        
    def start_server(self):
        server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        
        try:
            server_socket.bind((self.host, self.port))
            server_socket.listen(10)
            logging.info(f"🚀 STEALTH Server started on {self.host}:{self.port}")
            
            while True:
                client_socket, client_address = server_socket.accept()
                client_thread = threading.Thread(
                    target=self.handle_client,
                    args=(client_socket, client_address)
                )
                client_thread.daemon = True
                client_thread.start()
                
        except Exception as e:
            logging.error(f"Server error: {e}")
        finally:
            server_socket.close()

    def handle_client(self, client_socket, client_address):
        client_id = f"{client_address[0]}:{client_address[1]}"
        
        try:
            while True:
                data = client_socket.recv(1024).decode('utf-8').strip()
                if not data:
                    break
                    
                if data.startswith("STEALTH_CONNECT:"):
                    device_info = data.replace("STEALTH_CONNECT:", "")
                    logging.info(f"✅ STEALTH CONNECT: {device_info}")
                    
                    self.clients[client_id] = {
                        'device_info': device_info,
                        'connected_at': datetime.now(),
                        'last_seen': datetime.now()
                    }
                    
                    # Kirim acknowledgment
                    client_socket.send("STEALTH_ACK".encode('utf-8'))
                    
                elif data.startswith("HEARTBEAT:"):
                    if client_id in self.clients:
                        self.clients[client_id]['last_seen'] = datetime.now()
                    logging.info(f"💓 Heartbeat from {client_id}")
                    
        except Exception as e:
            logging.error(f"Client {client_id} error: {e}")
        finally:
            client_socket.close()
            if client_id in self.clients:
                del self.clients[client_id]
            logging.info(f"❌ Client {client_id} disconnected")

def main():
    server = StealthServer()
    server.start_server()

if __name__ == "__main__":
    main()