package com.textuality.sensplore;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class FlipActivity extends Activity {

    private Kinetics mKinetics;
    private TextView mReadout;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flip);
        mReadout = (TextView) findViewById(R.id.flip_readout);
        mKinetics = new Kinetics(this, new KineticListener() {
            
            @Override
            public boolean kineticRecognized(int kinetic) {
                switch (kinetic) {
                case Kinetics.FLIP_UP:
                    mReadout.setText(FlipActivity.this.getText(R.string.flip_up));
                    break;
                case Kinetics.FLIP_DOWN:
                    mReadout.setText(FlipActivity.this.getText(R.string.flip_down));
                    break;
                }
                return false;
            }
        });
        mKinetics.start();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mKinetics.stop();
    }
    
}
