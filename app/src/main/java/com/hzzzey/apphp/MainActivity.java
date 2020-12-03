package com.hzzzey.apphp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener{

    Boolean record = false, kill = false;
    SensorManager mSensorManager;
    private Sensor mSensorLight;
    private Sensor mAcceloMeter;
    private Socket mSocket;
    private PrintWriter output;
    BufferedReader in;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 23) {
            if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001);
            }
        }
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mAcceloMeter = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        new Thread(new ClientThread()).start();//SOCKET HARUS THREAD

    }
    class ClientThread implements Runnable{
        @Override
        public void run() {
            try {
//            InetAddress serverAddr = InetAddress.getByName();
                mSocket = new Socket("192.168.100.164", 8081);
            } catch (SecurityException e1) {
                Log.d("tugas 2", "security exception ");
                e1.printStackTrace();
            } catch (IOException e) {
//            e.printStackTrace();
                Log.d("tugas 2", "failed socket");
            }
            OutputStream out = null;
            try {
                out = mSocket.getOutputStream();
                in = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                output= new PrintWriter(out);
                output.write("Hello from Android");
                output.flush();
//                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    class sendThread implements Runnable {
        @Override
        public void run() {
            output.write("asiap");
            output.flush();
            String response = null;
            try {
                response = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (response != null) {
                Log.d("result", response);
            }
            else
                Log.d("result", "Eror");
        }
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        float currentValue = event.values[0];
        switch (sensorType) {
            // Event came from the light sensor.
            case Sensor.TYPE_LIGHT:
                // Handle light sensor
                break;
            case Sensor.TYPE_ACCELEROMETER:
                if (record) {
                    SystemClock.sleep(100);
                }
                break;
            default:
                // do nothing
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mSensorLight != null) {
            mSensorManager.registerListener(this, mSensorLight,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mAcceloMeter != null) {
            mSensorManager.registerListener(this, mAcceloMeter,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 101 :
                if (grantResults.length > 0 && grantResults[0]
                        == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "Anda harus mengijinkan aplikasi menulis sebelum melanjutkan.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }


    private boolean checkPermission(String permission) {
        int check = ContextCompat.checkSelfPermission(this, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }
    @Override
    public void onClick(View v) {
        new Thread(new sendThread()).start();//SOCKET HARUS THREAD
    }
}