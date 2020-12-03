import socket
import select
import threading

server = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
server.setsockopt(socket.SOL_SOCKET,socket.SO_REUSEADDR,1)
ip_address = '0.0.0.0'
port = 8081
server.bind((ip_address,port))
server.listen(100)

conn,addr = server.accept()
print(addr[0]+ ' connected')
message = conn.recv(2048).decode()
print(message)
conn.close()
