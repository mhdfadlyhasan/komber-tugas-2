import socket
import select
import threading
import time 
import _thread
import pandas as pd
import numpy as np
from sklearn import tree
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score


def sendStatus(hasil):
    datatest=[
        float(hasil[0]),
        float(hasil[1]),
        float(hasil[2]),
        float(hasil[3]),
        float(hasil[4]),
        float(hasil[5]),
        float(hasil[6]),
        float(hasil[7]),
        float(hasil[8])
        ]
    
    return Klasifikasi(tree,datatest)

def GenerateTree():
    #generate di awal aplikasi berjalan saja, biar gak berat
    df = pd.read_csv('DataTrain.csv', index_col='id') #load CSV
    df = df[['x1', 'y1', 'z1', 'x2', 'y2', 'z2', 'x3', 'y3', 'z3']] #Urutkan csv
    df = df.dropna() #drop data kosong
    model = tree.DecisionTreeClassifier() #generate model
    model.fit(df,df.index.values.tolist()) #generate tree
    return model #return tree

def Klasifikasi(tree, data):
    #start=time.time() #runtime mulai (uncomment if needed)
    a=np.array(data).reshape(1,-1)
    y_predict = tree.predict(a)
    y_predict = int(y_predict[0])
    if y_predict == 1:
        return 'lompat'
    elif y_predict == 2:
        return 'jalan'
    elif y_predict == 3:
        return 'diam'
    else: return 'salah'
    
tree = GenerateTree()
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
while True:
    message = conn.recv(2048).decode()
    print('"'+str(message) + '" from user')
    #buat thread yang ngirim status tiap 1 detik
    hasil = message.split(";")
    kimak = (sendStatus(hasil))
    print(kimak)
    conn.send((kimak + "\n").encode())
    # for result in hasil:
    #     if result=="":
    #         continue
    # nilaiX,nilaiY,nilaiZ,flag=result.split(";",3)
    #WAJIB ADA '#' NYA AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
conn.close()
