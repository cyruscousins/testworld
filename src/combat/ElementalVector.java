/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package combat;

/**
 *
 * @author ccousins
 */
public class ElementalVector {
    //Elemental Damage contains a vector of elements from the attacker.  
    //This is dotted with the recievers resistance vector to obtain damage.
    
    float[] elements;
    int mainElementIndex;
    
    public ElementalVector(){
        elements = new float[]{1};
        mainElementIndex = 0;
    }
    
    public ElementalVector(float[] elements){
        this.elements = elements;
        calcMainElementIndex();
    }
    
    public ElementalVector(int element, float damage){
        elements = new float[element + 1];
        elements[element] = damage;
    }
    
    private void calcMainElementIndex(){
        int mi = 0;
        for(int i = 1; i < elements.length; i++){
            if(elements[i] > elements[mi]){
                mi = i;
            }
        }
        mainElementIndex = mi;
    }
    
    
    
    public float getDamage(ElementalVector resistance){
        float dmg = 0;
        float[] r = resistance.elements;
        int max = Math.min(r.length, elements.length);
        for(int i = 0; i < max; i++){
            dmg += elements[i] * r[i];
        }
        return dmg;
    }
}
