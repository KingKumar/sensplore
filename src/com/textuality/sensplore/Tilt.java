package com.textuality.sensplore;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Tilt {

    private SensorManager mManager;
    private final Average mAverage = new Average(5);
    private final ArrayList<Listener> mListeners = new ArrayList<Listener>();
    private TiltListener mCustomer;

    public Tilt(Activity activity) {
        mManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
    }

    public boolean start(TiltListener listener) {
        Sensor gSensor = mManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mCustomer = listener;
        if (gSensor != null) {
            mManager.registerListener(getListener(), gSensor, SensorManager.SENSOR_DELAY_GAME);
            return true;
        } else {
            return false;
        }    
    }

    public void stop() {
        for (Listener listener : mListeners) {
            mManager.unregisterListener(listener);
        }
    }

    private class Listener implements SensorEventListener {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {            
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
            case Sensor.TYPE_GRAVITY:
                process(event.values);
                break;
            }
        }
    }

    private void process(float[] values) {

        // avoid NaN's provoked by wonky sensor readings
        double gy = values[1] / SensorManager.GRAVITY_EARTH;
        if (gy > 1) {
            gy = 1;
        }

        // correct the range so it goes smoothly from 0 to 180 degrees. The X value 
        //  tells you, roughly speaking, whether you’re leaning left or right.  The Y value goes from
        //  0 at the top to +90 whichever way you lean the device
        double ay = Math.acos(gy);
        if (values[0] < 0)
            ay = (Math.PI/2) - ay;
        else 
            ay = (Math.PI/2) + ay;
        mCustomer.setTilt(mAverage.add(ay));
    }

    private Listener getListener() {
        Listener listener = new Listener();
        mListeners.add(listener);
        return listener;
    }

    // smooth out the data, the acos function is jumpy passing through zero values
    //
    private class Average {
        private final int mSize;
        private final double[] mBuffer;
        private int mIndex = 0;
        public Average(int size) {
            mSize = size;
            mBuffer = new double[mSize];
            init();
        }
        private void init() {
            for (int i = 0; i < mSize; i++)
                mBuffer[i] = 0;
        }
        public double add(double value) {
            mBuffer[mIndex++] = value;
            if (mIndex == mSize) {
                mIndex = 0;
            }
            double total = 0;
            for (double v : mBuffer) {
                total += v;
            }
            return total / mSize;
        }
    }
}
