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
import sys
import mysql.connector
import flask
import json
from flask import jsonify

mydb = mysql.connector.connect(
  host="localhost",
  user="root",
  password="",
  database="database_komber"
)

list_of_clients = []

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
    try:
        a=np.array(data).reshape(1,-1)
        y_predict = tree.predict(a)
        y_predict = int(y_predict[0])
        if y_predict == 1:
            return 'lompat',True
        elif y_predict == 2:
            return 'jalan', False
        elif y_predict == 3:
            return 'diam', False
        else: return 'salah', False
    except:
        print("Something Error!")
        return 'diam', False

def pushKeDB(latitude, longitude):
    mycursor = mydb.cursor()
    print("Lompat", float(latitude), float(longitude))
    sql = ("Insert into coordinate (activity, latitude, longitude) values (%s, %s, %s)")
    val = ("Lompat", float(latitude), float(longitude))
    mydb.commit()
    print("1 record inserted, ID:", mycursor.lastrowid) 

def clientthread(conn,addr):
    while True:
        try:
            message = conn.recv(2048).decode()
            if message:
                print('"'+str(message) + '" from user')
                hasil = message.split(";")
                result, isSendToDB = (sendStatus(hasil))
                if(isSendToDB):
                    pushKeDB(hasil[9],hasil[10])
                print('selesai!')
                print(result)
                conn.send((result + "\n").encode())
                # message = conn.recv(2048).decode()
            else:
                remove(conn)
        except:
            continue
        
def remove(connection):
    print("removing connection!")
    if connection in list_of_clients:
        list_of_clients.remove(connection)
        connection.close()


def threadSocket(tree,a):
    print("App Ready!")
    server = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
    server.setsockopt(socket.SOL_SOCKET,socket.SO_REUSEADDR,1)
    ip_address = '192.168.100.164'
    port = 8081
    server.bind((ip_address,port))
    server.listen(100)
    try:
        print("App Ready!")
        while True:
            conn,addr = server.accept()
            message = conn.recv(2048).decode()
            print('"'+str(message) + '" from user')
            list_of_clients.append(conn)
            print(addr[0]+ ' connected')
            threading.Thread(target=clientthread,args=(conn,addr)).start()
        # for result in hasil:
        #     if result=="":
        #         continue
        # nilaiX,nilaiY,nilaiZ,flag=result.split(";",3)
        #WAJIB ADA '#' NYA AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
    except KeyboardInterrupt:
        sys.exit()
        print("exited")
        
tree = GenerateTree()
threading.Thread(target=threadSocket,args=(tree,"yes")).start()
app = flask.Flask(__name__)
app.config["DEBUG"] = True
print("api ready")
@app.route('/', methods=['GET'])
def home():
    mycursor = mydb.cursor(dictionary=True)
    mycursor.execute("SELECT id, activity,latitude,longitude FROM coordinate")
    myresult = mycursor.fetchall()
    return jsonify(myresult)
app.run()