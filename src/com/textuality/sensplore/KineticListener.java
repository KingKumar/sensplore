package com.textuality.sensplore;

public interface KineticListener {
    
    /**
     * Called by Kinetics.start to report detecting a kinetic gesture
     * 
     * @param gesture One of the public int values from the Kinetics class
     * @return true if the kinetic has been handled, and the Kinetics listener should stop listening
     */
    public boolean kineticRecognized(int gesture);
}
