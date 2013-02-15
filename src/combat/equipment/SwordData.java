/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package combat.equipment;

import systems.MaterialData;

/**
 *
 * @author ccousins
 */
public class SwordData {
    public MaterialData hilt;
    public MaterialData blade;
    
    public float length;
    public float weight;
    
    public SwordData(MaterialData hilt, MaterialData blade){
        this.hilt = hilt;
        this.blade = blade;
        
        System.out.println("New Sword");
        System.out.println(hilt.name);
        System.out.println(blade.name);
        
        recalculateStats();
    }
    
    public void recalculateStats()
    {
        length = 3;
        weight = hilt.getParameter("Density") + blade.getParameter("Density") * 2;
    }
}
