/*
 * Scalar valued field 2D
 */
package math;

import com.jme3.animation.ClonableTrack;

/**
 *
 * @author ccousins
 */
public abstract class SVF2D implements Cloneable{
    public static final float EPSILON = .001f;
    public static final float ONEOVER2EPSILON = (1 / (2 * EPSILON));
    public abstract float value(float x, float z);
    public float dydx(float x, float z){
        return (value(x + EPSILON, z) - value(x - EPSILON, z)) * ONEOVER2EPSILON;
    }
    public float dydz(float x, float z){
        return (value(x, z + EPSILON) - value(x, z - EPSILON)) * ONEOVER2EPSILON;
    }
    
    public SVF2D clone(){
        return clone();
    }
}
