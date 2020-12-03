import socket
import select
import sys
import msvcrt

server = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
server.setsockopt(socket.SOL_SOCKET,socket.SO_REUSEADDR,1)
ip_address = 'localhost'
port = 8081
server.connect((ip_address,port))

message = input()
# read_socket = select.select([server],[],[],0.001)[0]#0.001 artinya time out, 
print('message')
server.send(message.encode())
server.close()