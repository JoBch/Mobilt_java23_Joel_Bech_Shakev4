package com.example.shakev4;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView textView;
    private ProgressBar progressBarX, progressBarZ, progressBarY;
    private ImageView imageView;

    private SensorManager sensorManager;
    private Sensor accelerometer, lightSensor;

    private long lastToastTime = 0;

    private final float[] gravity = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.tV1);
        progressBarZ = findViewById(R.id.pBZ);
        progressBarY = findViewById(R.id.pBY);
        progressBarX = findViewById(R.id.pBX);
        imageView = findViewById(R.id.iV1);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (lightSensor == null) {
            Toast.makeText(this, "LightSensor not available!", Toast.LENGTH_LONG).show();
        }
        if (accelerometer == null){
            Toast.makeText(this, "Accelerometer not available!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerActions(event);
        }
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            lightSensorActions(event);
        }
    }

    public void accelerometerActions(SensorEvent event) {
        long currentTime = System.currentTimeMillis();
        final float alpha = 0.8f;  //Smoothing out the values in movement

        //Isolate the force of gravity with the low-pass filter
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        float accelX = gravity[0];
        float accelY = gravity[1];
        float accelZ = gravity[2];

        //Calculate pitch, roll and tilt using gravity data, borrowed these calculation from the web
        float xAxis = (float) Math.toDegrees(Math.atan2(accelY, accelZ)); //Pitch (up and down)
        float yAxis = (float) Math.toDegrees(Math.atan2(-accelX, Math.sqrt(accelY * accelY + accelZ * accelZ))); //Roll (left and right)
        float zAxis = (float) Math.toDegrees(Math.atan2(accelX, accelY));//Tilt (left and right standing up)

        textView.setText(String.format("Z-axis: %.2f°\nY-axis: %.2f°\nX-axis: %.2f°", zAxis, yAxis, xAxis));
        progressBarZ.setProgress((int) zAxis);
        progressBarX.setProgress((int) xAxis);
        progressBarY.setProgress((int) yAxis);

        progressBarX.setRotation(zAxis);
        progressBarZ.setRotation(zAxis);
        progressBarY.setRotation(zAxis);
        textView.setRotation(zAxis);
        imageView.setRotation(zAxis);

        //Using my raw data to calculate if a movement is fast, like a shake
        float deviceMovement = (float) Math.sqrt(gravity[0] * gravity[0] + gravity[1] * gravity[1] + gravity[2] * gravity[2]);

        //Checking to see if the user shook the device and display a toast if true
        int toastDelay = 2000;
        if (deviceMovement > 15.0) {
            if (currentTime - lastToastTime > toastDelay) {
                Toast.makeText(this, "STOP SHAKING ME!!!", Toast.LENGTH_SHORT).show();
                lastToastTime = currentTime;
            }
        }

        Log.i("AccelData", String.format("Z-axis: %.2f°\nY-axis: %.2f°\nX-axis: %.2f°", zAxis, yAxis, xAxis));
    }

    public void lightSensorActions(SensorEvent event) {

        //Light level in lux
        float lightLevel = event.values[0];
        //Convert light level to opacity
        float opacity = lightLevel / 40000f;
        imageView.setAlpha(opacity);

        Log.i("LightSensor", "Opacity: " + opacity + "Raw LightSensorData: " + lightLevel);

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
