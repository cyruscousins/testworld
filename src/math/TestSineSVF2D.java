/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package math;

import com.jme3.math.FastMath;

/**
 *
 * @author ccousins
 */
public class TestSineSVF2D extends SVF2D{
    
    public static final float TCONST = 1 / (2 * FastMath.PI);
    
    float i;
    float o;
    public TestSineSVF2D(float inScale, float outScale){
        i = TCONST / inScale;
        o = outScale * .5f;
    }
    public float value(float x, float y){
        return o * FastMath.cos(x * TCONST) + FastMath.cos(y * TCONST);
    }
}
