import socket
import select
import threading
import time 
import _thread

valueX=[]
valueY=[]
valueZ=[]

def sendStatus():
    while True:
        if len(valueX)>=8:
            print("value x"+str(valueX))
            print("value Y"+str(valueY))
            print("value Z"+str(valueZ))
        valueX.clear()
        valueY.clear()
        valueZ.clear()
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
while True:
    message = conn.recv(2048).decode()
    # print('"'+str(message) + '" from user')
    #buat thread yang ngirim status tiap 1 detik
    hasil = message.split(" ")
    for result in hasil:
        if result=="":
            continue
        nilaiX,nilaiY,nilaiZ,flag=result.split(";",3)
        valueX.append(nilaiX)
        valueY.append(nilaiY)
        valueZ.append(nilaiZ)
        
    #WAJIB ADA '#' NYA AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
conn.close()
