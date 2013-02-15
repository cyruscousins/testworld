/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package combat.equipment;

import actor.Combatable;
import systems.MaterialData;

/**
 *
 * @author ccousins
 */
public class Equipment {
    
    //TODO sane system for equipment types.
    public static final int HEAD = 0, ARM = 1, HAND = 2, CHEST = 3, LEG = 4, FOOT = 5;
    public static final float[] importances = new float[]{1.5f, .75f, .5f, 2f, 1.5f, .5f};
    public static final float[] sizes = new float[]{.25f, .3f, .1f, 1f, 1f, .35f};
    public static String[] names = new String[]{"Hat", "Greaves", "Gloves", "Mail", "Leggings", "Boots"};
    
    int type; 
    
    public MaterialData material;

    public float[] statStatModifiers;
    public float[] dynStatModifiers;
    
    float weight, insulation;
    
    String name;
    
    public Equipment(int type, MaterialData material){
        this.type = type;
        
        statStatModifiers = new float[Combatable.STAT_STATS];
        dynStatModifiers = new float[Combatable.DYN_STATS];
        
        statStatModifiers[Combatable.DEFENCE] = material.getParameter("Hardness") * importances[type];
        
        weight = material.getParameter("Density") * sizes[type];
        insulation = material.getParameter("Insulation") * (sizes[type] + importances[type]);
        
        this.name = material.name + " " + names[type];
    }
}
