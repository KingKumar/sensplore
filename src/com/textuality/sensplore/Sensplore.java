/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.textuality.sensplore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

public class Sensplore extends Activity {

	private long mStartedAt = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sensplore);
		Button button = (Button) findViewById(R.id.button);
		button.setOnClickListener(mStarter);
	}

	View.OnClickListener mStopper = new View.OnClickListener() {
		public void onClick(View v) {
			mManager.unregisterListener(mAccelListener);
			mManager.unregisterListener(mRVListener);
			(new Writer()).execute();
		}
	};

	View.OnClickListener mStarter = new View.OnClickListener() {
		public void onClick(View v) {
			Button button = (Button) v;
			button.setText(R.string.stop_test);
			button.setOnClickListener(mStopper);
			(new FileHeader()).execute(Sensplore.this, null, null);
		}
	};

	private SensorManager mManager = null;
	private Listener mAccelListener = null;
	private Listener mRVListener = null;
	private ArrayList<Datum> mAccelCollector = new ArrayList<Datum>();
	private ArrayList<Datum> mAngleCollector = new ArrayList<Datum>();
	private Flipper mRVFlipper = new Flipper();

	private void runTest() {
		mManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		mAccelListener = new Listener();
		mRVListener = new Listener();
		Sensor accelSensor = mManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		Sensor rvSensor = mManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		mManager.registerListener(mRVListener, rvSensor, SensorManager.SENSOR_DELAY_GAME);
		mManager.registerListener(mAccelListener, accelSensor, SensorManager.SENSOR_DELAY_GAME);
	}

	private class Listener implements SensorEventListener {

		@Override
		public void onSensorChanged(SensorEvent sensorEvent) {

			int sensorType = sensorEvent.sensor.getType();
			switch (sensorType) {
			case Sensor.TYPE_ROTATION_VECTOR:
				float[] last = mRVFlipper.last(), next = mRVFlipper.next();
				SensorManager.getRotationMatrixFromVector(next, sensorEvent.values);
				if (last != null) {
					float[] values = new float[3];
					SensorManager.getAngleChange(values, next, last);
					mAngleCollector.add(new Datum(sensorEvent.timestamp, values));
				}
				mRVFlipper.flip();
				break;
			case Sensor.TYPE_ACCELEROMETER:
				mAccelCollector.add(new Datum(sensorEvent));
				break;
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int i) {
		}
	}

	private class Writer extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {

			try {
				PrintStream p = getFile(true);
				p.println("Accelerometer,,,,, Synth Angle Change,,,,, Real Angle Change,,,");
				p.println("t,Accel x, Accel y, Accel z,, " +
						"t,Angle x, Angle y, Angle z");
				int accelSize = mAccelCollector.size();
				int angleSize = mAngleCollector.size();

				int size = (accelSize > angleSize) ? accelSize : angleSize;
				for (int i = 0; i < size; i++) {
					String accel = (i < accelSize) ? mAccelCollector.get(i).toString() : "";
					String angle = (i < angleSize) ? mAngleCollector.get(i).toString() : "";
					p.println(accel + ",, " + angle);
				}
				p.close();
				sendOffData();

			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			return null;
		}

		private void sendOffData() {
			Intent i = new Intent(Intent.ACTION_SEND);
			String address = AccountManager.get(Sensplore.this).getAccountsByType("com.google")[0].name;

			i.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { address });
			i.putExtra(Intent.EXTRA_SUBJECT, "Sensplore: Test results");
			i.putExtra(Intent.EXTRA_TEXT, "(attached as CSV)");
			i.putExtra(Intent.EXTRA_STREAM, fileURI());
			i.setType("text/plain");
			startActivity(Intent.createChooser(i, "Send mail"));
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			finish();
		}
	}

	private class Datum {
		public long mWhen;
		public float[] mValues;

		// should be called AccelerometerDatum - values are x/y/z
		public Datum(SensorEvent event) {
			mWhen = deltaT(event.timestamp);
			mValues = event.values.clone();
		}

		// should be called AngularDatum - values are azimuth/pitch/roll or z/x/y
		public Datum(long when, float[] values) {
			mWhen = deltaT(when);
			float x = values[1], y = values[2], z = values[0];
			mValues = values;
			mValues[0] = x; mValues[1] = y; mValues[2] = z;
		}

		private long deltaT(long incoming) {
			if (mStartedAt == 0)
				mStartedAt = incoming;
			return incoming - mStartedAt;
		}

		public String toString() {
			long microseconds = (mWhen + 500) / 1000;
			float milliseconds = microseconds / 1000.0f;
			StringBuilder s = new StringBuilder(String.format("%1$.2f", milliseconds));
			for (float value : mValues) {
				s.append(", ").append(String.format("%1$.2f", value));
			}
			return new String(s);
		}
	}

	/// File stuff
	private File mOutputFile = null;

	public static File filename() {
		File ext = Environment.getExternalStorageDirectory();
		ext = new File(ext, "sensplore");
		if (!ext.exists()) {
			ext.mkdir();
		}

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		return new File(ext, "kinetics-" + df.format(new Date()) + ".csv");
	}

	public PrintStream getFile(boolean append) {
		PrintStream p = null;
		try {
			if (append) {
				p = new PrintStream(new FileOutputStream(mOutputFile, true));
			} else {
				mOutputFile = filename();
				// clean out all previous files
				for (File file : mOutputFile.getParentFile().listFiles()) {
					file.delete();
				}

				p = new PrintStream(new FileOutputStream(mOutputFile));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return p;
	}

	public Uri fileURI() {
		return Uri.fromFile(mOutputFile);
	}
	private class FileHeader extends AsyncTask<Context, Void, Void> {
		@Override
		protected Void doInBackground(Context... params) {
			try {
				Context context = params[0];
				PrintStream p = getFile(false);
				Account[] accounts = AccountManager.get(context).getAccountsByType("com.google");
				p.println("Reported by: " + accounts[0].name +
						" System: " + android.os.Build.MODEL +
						" (" + android.os.Build.DEVICE + "/" + android.os.Build.PRODUCT + ")" +
						",,,,,,,,");
				p.println(",,,,,,,,");
				p.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			runTest();
		}
	}

	private class Flipper {
		private final float[] mR1 = new float[16], mR2 = new float[16];
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
