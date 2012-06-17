package com.textuality.sensplore;

public interface TiltListener {

    /**
     * Callback with tilt values.
     * 
     *  @param tilt tilt, in radians; 0 means all the way to the left, pi/2 straight up, pi all the way right 
     */
    public void setTilt(double tilt);

}
