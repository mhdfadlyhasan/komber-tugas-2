import socket
import select
import threading
import time 
import _thread



def sendStatus():
    while True:
        print("sending")
        conn.send('berdiri\n'.encode())
        time.sleep(1)

    

server = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
server.setsockopt(socket.SOL_SOCKET,socket.SO_REUSEADDR,1)
ip_address = '192.168.100.164'
port = 8081
server.bind((ip_address,port))
server.listen(100)

conn,addr = server.accept()
print(addr[0]+ ' connected')
message = conn.recv(2048).decode()
print('"'+str(message) + '" from user')
_thread.start_new_thread(sendStatus,())
i=0
while True:
    message = conn.recv(2048).decode()
    print('"'+str(message) + '" from user')
    #buat thread yang ngirim status tiap 1 detik

    #WAJIB ADA '#' NYA AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
conn.close()
