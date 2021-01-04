package com.hzzzey.apphp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    double longitude, latitude;
    //    PermissionsListener permissionsListener;
    Boolean record = false, kill = false;
    float valueSensorAccelX, valueSensorAccelY, valueSensorAccelZ;
    SensorManager mSensorManager;
    private Sensor mSensorLight;
    private Sensor mAcceloMeter;
    private Socket mSocket;
    private PrintWriter output;
    BufferedReader in;
    private boolean responded = true;

    // Variabel untuk offloading
    ArrayList<Float> sensoryDatasetsX = new ArrayList<>();
    ArrayList<Float> sensoryDatasetsY = new ArrayList<>();
    ArrayList<Float> sensoryDatasetsZ = new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= 23) {
            if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001);
            }
            if (!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1002);
            }
        }
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mAcceloMeter = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
        new Thread(new ClientThread()).start();//SOCKET HARUS THREAD

    }

    class ClientThread implements Runnable {
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
                output = new PrintWriter(out);
                output.write("Hello from Android");
                output.flush();
//                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            Log.d("coordinate", longitude + " " + latitude);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    class sendThread implements Runnable {
        @Override
        public void run() {
            while (record) {
                sensoryDatasetsX.add(valueSensorAccelX);
                sensoryDatasetsY.add(valueSensorAccelY);
                sensoryDatasetsZ.add(valueSensorAccelZ);

                while (sensoryDatasetsX.size() > 20) {
                    Log.d("result", "pop depan");
                    sensoryDatasetsX.remove(0);
                    sensoryDatasetsY.remove(0);
                    sensoryDatasetsZ.remove(0);
                }
                if (sensoryDatasetsX.size() == 20 && responded) {
                    ArrayList<Float> rerata20Data
                            = dataAverage(sensoryDatasetsX, sensoryDatasetsY, sensoryDatasetsZ);
//                    output.write(String.format("%f;%f;%f; ",valueSensorAccelX,valueSensorAccelY,valueSensorAccelZ));
                    output.write(String.format(Locale.ENGLISH, "%f;%f;%f;%f;%f;%f;%f;%f;%f;%f;%f\n",
                            rerata20Data.get(0),
                            rerata20Data.get(1),
                            rerata20Data.get(2),
                            rerata20Data.get(3),
                            rerata20Data.get(4),
                            rerata20Data.get(5),
                            rerata20Data.get(6),
                            rerata20Data.get(7),
                            rerata20Data.get(8),
                            latitude,
                            longitude
                            ));   // Tolong saya dipaksa ngoding spaget
                    output.flush();
                    Log.d("result", "waiting");
                    responded = false;
                }
                SystemClock.sleep(100);
            }
        }
    }

    ArrayList<Float> dataAverage(ArrayList<Float> sensoryX, ArrayList<Float> sensoryY, ArrayList<Float> sensoryZ) {
        float averageX = 0, averageY = 0, averageZ = 0;
        ArrayList<Float> averageJoe = new ArrayList<>();
        // 6 data pertama
        for (int i = 0; i < 6; i++) {
            averageX += sensoryX.get(i);
            averageY += sensoryY.get(i);
            averageZ += sensoryZ.get(i);
        }
        averageX /= 6;
        averageY /= 6;
        averageZ /= 6;
        averageJoe.add(averageX);
        averageJoe.add(averageY);
        averageJoe.add(averageZ);

        // 7 data berikutnya
        // Reset variabel
        averageX = 0;
        averageY = 0;
        averageZ = 0;
        for (int i = 6; i < 13; i++) {
            averageX += sensoryX.get(i);
            averageY += sensoryY.get(i);
            averageZ += sensoryZ.get(i);
        }
        averageX /= 7;
        averageY /= 7;
        averageZ /= 7;
        averageJoe.add(averageX);
        averageJoe.add(averageY);
        averageJoe.add(averageZ);

        // 7 data terakhir
        // Reset variabel
        averageX = 0;
        averageY = 0;
        averageZ = 0;
        for (int i = 13; i < 20; i++) {
            averageX += sensoryX.get(i);
            averageY += sensoryY.get(i);
            averageZ += sensoryZ.get(i);
        }
        averageX /= 7;
        averageY /= 7;
        averageZ /= 7;
        averageJoe.add(averageX);
        averageJoe.add(averageY);
        averageJoe.add(averageZ);

        return averageJoe;
    }

    class recieveThread implements Runnable {
        @Override
        public void run() {
            while (record) {
                Log.d("result", "waiting");
                String response = null;
                try {
                    response = in.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (response != null) {
                    Log.d("result", response);
                } else
                    Log.d("result", "Eror");
                SystemClock.sleep(1000);
                Log.d("result", "get");
                responded = true;
            }
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
                    valueSensorAccelX = event.values[0];
                    valueSensorAccelY = event.values[1];
                    valueSensorAccelZ = event.values[2];
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
            case 101:
                if (grantResults.length > 0 && grantResults[0]
                        == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "Anda harus mengijinkan aplikasi menulis sebelum melanjutkan.", Toast.LENGTH_SHORT).show();
                }
            case 1002:

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
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
        if(record) {
            record=false;
        }
        else {

            Thread threadSend = new Thread(new sendThread());//SOCKET HARUS THREAD
            Thread threadRecieve = new Thread(new recieveThread());//SOCKET HARUS THREAD
            threadSend.start();
            threadRecieve.start();
            record = true;
        }
    }
}