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
from flask import jsonify, request
import psycopg2
import os

mydb = psycopg2.connect(
    host="ec2-3-216-181-219.compute-1.amazonaws.com",
    database="dcnt2fm9dfkunf",
    user="cgfpytafjjkpmw",
    password="10f216b9441ab2bba0d3ad8abda609825c43618f4f741a757ec826d37b972eab",
    port='5432',
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
    mycursor.execute(sql,val)
    mydb.commit()
    print("1 record inserted!")

tree = GenerateTree()
app = flask.Flask(__name__)
@app.route('/', methods=['GET'])
def home():
    mycursor = mydb.cursor()
    mycursor.execute("SELECT id, activity,latitude,longitude FROM coordinate")
    myresult = mycursor.fetchall()
    response = jsonify(myresult)
    response.headers.add("Access-Control-Allow-Origin", "*")
    return response

@app.route('/klasifikasi', methods=['POST'])
def get_klasifikasi():
    message = request.form.get('sensor_data')
    print('"'+str(message) + '" from user')
    hasil = message.split(";")
    result, isSendToDB = (sendStatus(hasil))
    print(result)
    if(isSendToDB):
        pushKeDB(hasil[9],hasil[10])
    return jsonify(message=result)

if __name__ == "__main__":
    print("api ready")
    port = int(os.environ.get("PORT", 5000))
    app.run(host='0.0.0.0', port=port)