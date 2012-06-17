package com.textuality.sensplore;

import java.util.HashMap;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

public class Poll extends Activity {

    private static final String[] SENSOR_NAMES = {
        "Accelerometer",
        "Ambient temperature",
        "Gravity",
        "Gyroscope",
        "Light",
        "Linear acceleration",
        "Magnetic field",
        "Pressure",
        "Proximity",
        "Relative humidity",
        "Rotation vector"
    };
    private static final int[] SENSOR_NUMBERS = {
        Sensor.TYPE_ACCELEROMETER,
        Sensor.TYPE_AMBIENT_TEMPERATURE,
        Sensor.TYPE_GRAVITY,
        Sensor.TYPE_GYROSCOPE,
        Sensor.TYPE_LIGHT,
        Sensor.TYPE_LINEAR_ACCELERATION,
        Sensor.TYPE_MAGNETIC_FIELD,
        Sensor.TYPE_PRESSURE,
        Sensor.TYPE_PROXIMITY,
        Sensor.TYPE_RELATIVE_HUMIDITY,
        Sensor.TYPE_ROTATION_VECTOR
    };
    private static final HashMap<String, Integer> sSensors = new HashMap<String, Integer>();

    static {
        for (int i = 0; i < SENSOR_NAMES.length; i++) {
            sSensors.put(SENSOR_NAMES[i], SENSOR_NUMBERS[i]);
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poll);
        StringBuilder s = new StringBuilder();
        SensorManager manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        for (String sname : SENSOR_NAMES) {
            s.append(sname).append(": ");
            Sensor sensor = manager.getDefaultSensor(sSensors.get(sname));
            s.append((sensor == null) ? "NO" : "YES").append("\n");
        }
        ((TextView) findViewById(R.id.poll_list)).setText(s);
    }
}
