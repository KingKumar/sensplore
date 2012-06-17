package com.textuality.sensplore;

import android.app.Activity;
import android.os.Bundle;

public class TiltActivity extends Activity {

    private Tilt mTilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.orientation);
        mTilter = new Tilt(this);
        mTilter.start((TiltView) findViewById(R.id.level));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTilter.stop();
    }

}
