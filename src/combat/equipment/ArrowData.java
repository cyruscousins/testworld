/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package combat.equipment;

import com.jme3.math.FastMath;
import systems.MaterialData;

/**
 *
 * @author ccousins
 */
public class ArrowData {
    //Material info, can be used to calculate arrow data
    public MaterialData shaftMat;
    public MaterialData headMat;
    
    //effects damage and bow power to arrow velocity ratio.
    public float weight;
    //effects damage dealt
    public float sharpness;
    //effects probability of breaking on impact.
    public float durability;
    //effects drift
    public float trueness;
    //self explanatory
    public float length;
    
    public ArrowData(MaterialData head, MaterialData shaft, float length){
        this.headMat = head;
        this.shaftMat = shaft;
        recalculateStats();
        this.length = length;
    }
    
    public ArrowData(String head, String shaft, float length){
        this.headMat = MaterialData.getMaterial(head);
        this.shaftMat = MaterialData.getMaterial(shaft);
        recalculateStats();
        this.length = length;
    }
    
    //Calculates the arrow stats based on the materials.
    public void recalculateStats(){
        weight = .25f * (shaftMat.getParameter("Density") * length + headMat.getParameter("Density") * .4f);
        sharpness = headMat.getParameter("Malleability") + 1f / headMat.getParameter("Durability"); //Malleable objects can be sharp, as can fragile objects
//        fragility = 1f / (shaftMat.getParameter("Durability") * .5f + headMat.getParameter("Durability") * .5f);
        durability = (shaftMat.getParameter("Durability") * .5f + headMat.getParameter("Durability") * .5f);
        trueness = shaftMat.getParameter("Regularity") + headMat.getParameter("Regularity");
//        length = 2f;
    }
    
    public static ArrowData randomArrow(){
        return new ArrowData(MaterialData.getRandomMaterial("Metal"), MaterialData.getRandomMaterial("Wood"), 1 + FastMath.rand.nextFloat());
    }
}
