/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package math;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

/**
 *
 * @author ccousins
 */
public class AdvancedMath {
    
    //targets the given point.  Returns null if such targeting is impossible.  
    //v is velocity, g gravity.
    //TODO    trig function approximations
    public static Vector3f ballisticTarget(Vector3f pos, Vector3f target, float v, float g){
        //some simplification is possible
        float vSqr = v * v;
        float dx = target.x - pos.x;
        float dy = target.y - pos.y;
        float dh = FastMath.sqrt(dx * dx + dy * dy);
        float dz = target.z - pos.z;
        
        float inner = (vSqr * vSqr - g * (g * dh * dh + 2 * dz * vSqr));
        if (inner < 0) return null;  //no solutions
        float innerSqrt = FastMath.sqrt(inner);
        
        //float phi0 = FastMath.atan((vSqr + innerSqrt) / (g * dh));
        float phi1 = FastMath.atan((vSqr - innerSqrt) / (g * dh));
        
        float theta = FastMath.atan2(dy, dx);
        return new Vector3f(v * FastMath.cos(phi1) * FastMath.cos(theta), v * FastMath.cos(phi1) * FastMath.sin(theta), v * FastMath.sin(phi1));
        //please do not use trig functions for x & y components: ratios would be faster
        
//        float xScale = dx / dh;
//        float yScale = dy / dh;
    }
}
