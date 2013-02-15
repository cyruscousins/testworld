/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package math;

/**
 *
 * @author ccousins
 */
public class ConstSVF2D extends SVF2D{
    float val;
    
    public ConstSVF2D(float val){
        this.val = val;
    }
    
    public float value(float x, float y){
        return val;
    }

    //All partial derivatives are 0 in a constant scalar valued field.
    public float dydx(float x, float z){
        return 0;
    }
    public float dydz(float x, float z){
        return 0;
    }
}