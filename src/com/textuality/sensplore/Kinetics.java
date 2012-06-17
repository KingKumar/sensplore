package com.textuality.sensplore;

import java.util.ArrayList;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Kinetics {

    public static final int FLIP_UP = 0;
    public static final int FLIP_DOWN = 1;

    private static final double THRESHOLD_ANGLE = Math.toRadians(5.0);
    private static final int THRESHOLD_MOVING_DURATION = 2;
    private static final int THRESHOLD_REST_DURATION = 4;

    private final KineticListener mCustomer;
    private final SensorManager mManager;
    private final ArrayList<Listener> mListeners = new ArrayList<Listener>();
    private Flipper mFlipper = new Flipper();

    private final static int STATE_AT_REST = 0;
    private final static int STATE_MOVING_FORWARD = 1;
    private final static int STATE_MOVING_BACKWARD = 2;
    private static int mState;
    private static int mDuration;

    public Kinetics(Context context, KineticListener listener) {
        mCustomer = listener;
        mManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    public void start() {
        Sensor rvSensor = mManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mState = STATE_AT_REST;
        mDuration = 0;
        mManager.registerListener(getListener(), rvSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public void stop() {
        for (Listener listener : mListeners) {
            mManager.unregisterListener(listener);
        }
    }

    private void send(int kinetic) {
        stop();
        if (!mCustomer.kineticRecognized(kinetic)) {
            start();
        }
    }

    private Listener getListener() {
        Listener listener = new Listener();
        mListeners.add(listener);
        return listener;
    }

    private class Listener implements SensorEventListener {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() != Sensor.TYPE_ROTATION_VECTOR) {
                return;
            }

            final float[] last = mFlipper.last(), next = mFlipper.next();
            mFlipper.flip();
            SensorManager.getRotationMatrixFromVector(next, event.values);
            if (last == null) {
                return;
            }

            final float[] values = new float[3];
            SensorManager.getAngleChange(values, next, last);
            final double deltaX = values[1]; // [z,x,y]
            final boolean plusX = (deltaX > THRESHOLD_ANGLE), minusX = (deltaX < -THRESHOLD_ANGLE);

            switch (mState) {
            case STATE_AT_REST:
                if (mDuration < THRESHOLD_REST_DURATION) {
                    mDuration++;    // ignore first few events when arriving at rest; stabilizing after a gesture
                } else if (minusX) {
                    mState = STATE_MOVING_FORWARD;
                    mDuration = 1;
                } else if (plusX) {
                    mState = STATE_MOVING_BACKWARD;
                    mDuration = 1;
                } else {
                    mDuration++;
                }
                break;
                
            case STATE_MOVING_BACKWARD:
                if (plusX) {
                    mDuration++;
                } else {
                    if (mDuration > THRESHOLD_MOVING_DURATION)
                        send(FLIP_DOWN);
                    mState = STATE_AT_REST;
                    mDuration = 1;
                }
                break;
                
            case STATE_MOVING_FORWARD:
                if (minusX) {
                    mDuration++;
                } else {
                    if (mDuration > THRESHOLD_MOVING_DURATION) {
                        send(FLIP_UP);
                    }
                    mState = STATE_AT_REST;
                    mDuration = 1;
                }
                break;
            }
        }
    }
    
    /**
     * There are a few places in sensor-land where you get the delta of two 9x9 float[] matrices. This pre-allocates two and 
     *  flips them back and forth between "next" and "last", so you don�t have to keep allocating new ones.
     */
    private class Flipper {
        private final float[] mR1 = new float[9], mR2 = new float[9];
        private final float[][] mR = { mR1,  mR2 };
        private int mLast = -1;

        public float[] last() {
            if (mLast == -1) {
                mLast = 0;
                return null;
            } else {
                return mR[mLast];
            }
        }

        public float[] next() {
            return mR[mLast ^ 1];
        }

        public void flip() {
            mLast ^= 1;
        }
    }

}