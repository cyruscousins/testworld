/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package actor;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import combat.ElementalVector;
import combat.WorldAttack;
import combat.equipment.ArrowData;

/**
 *
 * @author Cyrus Cousins
 */
public abstract class Combatable extends Geometry implements WorldDynamic{
    
    //static stats are stats that change only under status effects and level ups, such as strength and magic.
    public static final int STRENGTH = 0, DEFENCE = 1, ENDURANCE = 2, SPEED = 3, MAGIC = 4, STAT_STATS = 5;
    public float[] statStatCur;
    public float[] statStatMax;
    public float[] statStatStat;
    
    //dynamic stats are stats that change frequently like health
    public static final int HEALTH = 0, BODY = 1, ENERGY = 2, SOUL = 3, WATER = 4, DYN_STATS = 5;
    public float[] dynStatCur;
    public float[] dynStatMax;
    public float[] dynStatStat;
    
    public static final ColorRGBA[] dynStatColors = new ColorRGBA[]{
        new ColorRGBA(.85f, 0, 0, .4f),
        new ColorRGBA(.6f, .5f, .4f, .6f),
        new ColorRGBA(1, .8f, .2f, .5f),
        new ColorRGBA(.6f, .7f, 1f, .75f),
        new ColorRGBA(.4f, .6f, .8f, .3f)
    };
    //Element information
    ElementalVector resistance = new ElementalVector(0, 1);
    
    public static final int E_FIRE = 0, E_WATER = 1, E_EARTH = 2, E_AIR = 3;
    public static String[] elements = new String[]{"Fire", "Water", "Earth", "Air"};
    public static ColorRGBA[] elemColors = new ColorRGBA[]{
        new ColorRGBA(1, .2f, .1f, 1), new ColorRGBA(.1f, .2f, 1f, 1), 
        new ColorRGBA(.5f, .4f, .3f, 1),new ColorRGBA(.2f, 1f, .1f, 1),
    };
    public static float[][] elementMap = new float[][]{
        new float[]{.125f, .5f, 2f, 1f},
        new float[]{2f, .125f, .5f, 1f},
        new float[]{.5f, 2f, .5f, 1f},
        new float[]{1f, 1f, 1f, .125f}
    };
    
    public int element;
    public int level;
    public int experience;

    public Combatable(int element, int level, int experience) {
        name = "Combat: " + hashCode();
        this.element = element;
        this.level = level;
        this.experience = experience;
    }
    
    public void takeProjectile(WorldAttack p){
        takeRawDamage(p.getElement().getDamage(resistance), p.getOwner());
    }
    
    //returns true if the attack is fatal.  
    //performs calculations for element and defence
    public boolean takeRawDamage(float dmg, Combatable dealer){
        System.out.println("Hit: " + dynStatCur[HEALTH] + " / " + dynStatMax[HEALTH]);
        if(effectStat(HEALTH, -dmg / statStatCur[DEFENCE])){
            die();
            if(dealer instanceof Player) dealer.getExperience(this);
            return true;
        }
        return false;
    }
    //returns the current fraction of a stat
    public float statStatFrac(int stat){
        return statStatCur[stat] / statStatMax[stat];
    }
    public float dynStatFrac(int stat){
        return dynStatCur[stat] / dynStatMax[stat];
    }
    //effects a stat.  Returns true if the stat is emptied.  
    public boolean effectStat(int stat, float change){
        dynStatCur[stat] += change;
        if(dynStatCur[stat] > dynStatMax[stat]) dynStatCur[stat] = dynStatMax[stat];
        if(dynStatCur[stat] <= 0){
            dynStatCur[stat] = 0;
            return true;
        }
        return false;
    }
    
    public float getMagicAttack(){
        return statStatCur[MAGIC] * dynStatCur[SOUL] / dynStatMax[SOUL];
    }
    public float getPhysicalAttack(){
        return statStatCur[STRENGTH] * dynStatCur[BODY] * dynStatCur[ENERGY] / (dynStatMax[BODY] * dynStatMax[ENERGY]);
    }
    boolean isDead = false;
    //This method is called immediately upon death.  Used to change variables, start animation sequence, drop things, and such.  
    public void die(){
        if(!isDead){ //Protection from being killed twice
            isDead = true;
            System.out.println(name + " is dead.");
            removeFromWorld();
            
            
            Collectable spoils = new CollectableItem(CollectableItem.TYPE_ARROW, new ArrowData[]{ArrowData.randomArrow()}, 1, 1, ColorRGBA.Green);
        }
    }
    public void getExperience(Combatable c){
        experience += c.experience;
        if (experience > levelExperience(level + 1)) levelUp();
    }
    //increment stats.  Level ups are generally linear, but require exponentially increasing experience.  
    public void levelUp(){
        for(int i = 0; i < DYN_STATS; i++){
            float increase = .5f + .0625f * dynStatMax[i];
            dynStatCur[i] += increase;
            statStatCur[i] += increase;
        }
        for(int i = 0; i < STAT_STATS; i++){
            float increase = .25f + .0625f * statStatMax[i];
            statStatCur[i] += increase;
            statStatMax[i] += increase;
        }
    }
    //a slightly shifted exponential curve.
    public static int levelExperience(int level){
        return (4 + 1 << level);
    }
    //not quite 100% accurate.  
    public static int levelFromExperience(int experience){
        return (int)(.01f + FastMath.log(experience - 4, 2));
    }
}