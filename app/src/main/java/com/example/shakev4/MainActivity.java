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

    private long lastUpdateTime = 0;

    private float[] gravity = new float[3];

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

        if (lightSensor == null){
            Toast.makeText(this,"LightSensor not availible!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long currentTime = System.currentTimeMillis();

        //Update interval
        if ((currentTime - lastUpdateTime) > 200) {
            lastUpdateTime = currentTime;

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                final float alpha = 0.8f;  //Smoothing out the values in movement

                //Isolate the force of gravity with the low-pass filter
                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

                float accelX = gravity[0];
                float accelY = gravity[1];
                float accelZ = gravity[2];

                //Calculate pitch and roll using gravity data, borrowed these calculation from the web
                float xAxis = (float) Math.toDegrees(Math.atan2(accelY, accelZ)); //Pitch (up and down)
                float yAxis = (float) Math.toDegrees(Math.atan2(-accelX, Math.sqrt(accelY * accelY + accelZ * accelZ))); //Roll (left and right)
                float zAxis = (float) Math.toDegrees(Math.atan2(accelX, accelY));//Tilt (left and right standing up)

                textView.setText(String.format("Z-axis: %.2f°\nY-axis: %.2f°\nX-axis: %.2f°",zAxis, yAxis, xAxis));
                progressBarZ.setProgress((int) zAxis);
                progressBarX.setProgress((int) xAxis);
                progressBarY.setProgress((int) yAxis);

                progressBarX.setRotation(zAxis);
                progressBarZ.setRotation(zAxis);
                progressBarY.setRotation(zAxis);
                textView.setRotation(zAxis);
                imageView.setRotation(zAxis);

                if (yAxis < -30 || yAxis > 30) {
                    Toast.makeText(this, "SLUTA LUTA", Toast.LENGTH_SHORT).show();
                }

                Log.i("AccelData", String.format("Z-axis: %.2f°\nY-axis: %.2f°\nX-axis: %.2f°",zAxis, yAxis, xAxis));
            }

            else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {

                float lightLevel = event.values[0]; //Light level in lux
                Log.i("RawLightSensorData", "raw lightlevel: " + lightLevel);
                float opacity = lightLevel / 40000f;
                imageView.setAlpha(opacity);

                Log.i("LightSensor", "Opacity: " + opacity);
            }
        }
    }


/*    public void accelerometerActions(SensorEvent event){
        final float alpha = 0.8f;  //Smoothing out the values in movement

        //Isolate the force of gravity with the low-pass filter
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        float accelX = gravity[0];
        float accelY = gravity[1];
        float accelZ = gravity[2];

        //Calculate pitch and roll using gravity data, borrowed these calculation from the web
        float xAxis = (float) Math.toDegrees(Math.atan2(accelY, accelZ)); //Pitch (up and down)
        float yAxis = (float) Math.toDegrees(Math.atan2(-accelX, Math.sqrt(accelY * accelY + accelZ * accelZ))); //Roll (left and right)
        float zAxis = (float) Math.toDegrees(Math.atan2(accelX, accelY));//Tilt (left and right standing up)

        textView.setText(String.format("Z-axis: %.2f°\nY-axis: %.2f°\nX-axis: %.2f°",zAxis, yAxis, xAxis));
        progressBarZ.setProgress((int) zAxis);
        progressBarX.setProgress((int) xAxis);
        progressBarY.setProgress((int) yAxis);

        progressBarX.setRotation(zAxis);
        progressBarZ.setRotation(zAxis);
        progressBarY.setRotation(zAxis);
        textView.setRotation(zAxis);
        imageView.setRotation(zAxis);

        if (yAxis < -30 || yAxis > 30) {
            Toast.makeText(this, "SLUTA LUTA", Toast.LENGTH_SHORT).show();
        }

        Log.i("AccelData", String.format("Z-axis: %.2f°\nY-axis: %.2f°\nX-axis: %.2f°",zAxis, yAxis, xAxis));
    }

    public void lightSensorActions(SensorEvent event){

            float lightLevel = event.values[0]; // Light level in lux
        Log.i("RawLightSensorData", "raw lightlevel: " + lightLevel);

            // Convert light level to opacity (0.0 to 1.0)
            float opacity = lightLevel / 40000f; // Adjust the divisor to fit your range

            // Clamp opacity value between 0 and 1
            imageView.setAlpha(opacity);

            Log.i("LightSensor", "Opacity: " + opacity);

    }*/

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
