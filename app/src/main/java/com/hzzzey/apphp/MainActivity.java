package com.hzzzey.apphp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
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
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;
import pub.devrel.easypermissions.EasyPermissions;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    SensorManager mSensorManager;
    private Sensor mSensorLight;
    private Sensor mAcceloMeter;
    BufferedReader in;

    // Variabel untuk offloading
    public ArrayList<Float> sensoryDatasetsX = new ArrayList<>();
    public ArrayList<Float> sensoryDatasetsY = new ArrayList<>();
    public ArrayList<Float> sensoryDatasetsZ = new ArrayList<>();
    SyncHttpClient client = new SyncHttpClient();

    private boolean responded = true, record = false, kill = false;;
    double longitude, latitude;
    float valueSensorAccelX, valueSensorAccelY, valueSensorAccelZ;

    private FusedLocationProviderClient fusedLocationClient;
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

    // Hubungkan view
    TextView tvStatus;

    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
//            longitude = location.getLongitude();
//            latitude = location.getLatitude();
//            Log.d("coordinate", longitude + " " + latitude);
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


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.status);

        // Meminta permission secara runtime
        if (Build.VERSION.SDK_INT >= 23) {
            if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001);
            }
            if (!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1002);

            }

            if (!checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1003);
            }

        }
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mAcceloMeter = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);

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
    private void requestCurrentLocation() {
        // Request permission
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            // Main code
            Task<Location> currentLocationTask = fusedLocationClient.getCurrentLocation(
                    PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.getToken()
            );

            currentLocationTask.addOnCompleteListener((new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    String result = "";
                    if (task.isSuccessful()) {
                        // Task completed successfully
                        Location location = task.getResult();
                        result = "Location (success): " +
                                location.getLatitude() +
                                ", " +
                                location.getLongitude();
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        Toast.makeText(MainActivity.this, location.getLatitude() +" " + location.getLongitude() , Toast.LENGTH_LONG).show();
                    } else {
                        // Task failed with an exception
                        Exception exception = task.getException();
                        result = "Exception thrown: " + exception;
                    }
                    Log.d("coordinateasdfasdf", "getCurrentLocation() result: " + result);
                }
            }));
        } else {
            // TODO: Request fine location permission
            Log.d("coordinate", "Request fine location permission.");
        }
    }
    @Override
    public void onClick(View v) {
        if(record) {
            record=false;
        }
        else {

            Thread threadSend = new Thread(new sendThread());//SOCKET HARUS THREAD
            Thread threadReceive = new Thread(new receiveThread());//SOCKET HARUS THREAD
            threadSend.start();
            threadReceive.start();
            record = true;
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
                    EasyPermissions.requestPermissions(this, "ACCESS_FINE_LOCATION", 3, android.Manifest.permission.ACCESS_FINE_LOCATION);
                    EasyPermissions.requestPermissions(this, "ACCESS_COARSE_LOCATION", 4, android.Manifest.permission.ACCESS_COARSE_LOCATION);
                    return;
                }
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
                break;
        }
    }

    // Cek jika ada perubahan sensor, fungsi dari implements SensorEventListener
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
                    requestCurrentLocation();
                    ArrayList<Float> rerata20Data
                            = dataAverage(sensoryDatasetsX, sensoryDatasetsY, sensoryDatasetsZ);

                    String result = String.format(Locale.ENGLISH, "%f;%f;%f;%f;%f;%f;%f;%f;%f;%f;%f\n",
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
                    );
                    sendToServer(result);
                    Log.d("result", "waiting");
                    responded = false;
                }
                SystemClock.sleep(100);
            }
        }
    }

    class receiveThread implements Runnable {
        @Override
        public void run() {
            while (record) {
                Log.d("result", "waiting");
                SystemClock.sleep(1000);
                Log.d("result", "get");
                responded = true;
            }
        }

    }

    // Memproses 3 buah array dengan masing - masing 20 data, menjadi sebuah array dengan 9 data.
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

    // Mengirim data sensor yang sudah diproses ke server.
    void sendToServer(String data) {
        client.post("https://fast-hamlet-17646.herokuapp.com/klasifikasi",
                new RequestParams("sensor_data", data),
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String result = new String(responseBody);

                        try {
                            JSONObject jsonResult = new JSONObject(result);
                            String status = jsonResult.getString("message");

                            Log.d("API", "onSuccess:\nstatus = " + status);

                            // Untuk mengubah UI, harus jalankan di UI Thread
                            runOnUiThread(() -> tvStatus.setText(status));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.e("API", "onFailure: " + statusCode, error);
                    }
                });
    }

    // ????
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    // Cek sebuah permission, apakah telah diijinkan oleh pengguna atau tidak.
    private boolean checkPermission(String permission) {
        int check = ContextCompat.checkSelfPermission(this, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }
}