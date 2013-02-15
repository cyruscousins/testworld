package combat;

import actor.Combatable;
import actor.WorldDynamic;
import com.jme3.scene.Spatial;

/**
 *
 * Exists in the world and inflicts physical damage on Combatables.
 * 
 */
public interface WorldAttack extends WorldDynamic{
    public Combatable getOwner();
    public ElementalVector getElement();
    
    public void collideWithSpatial(Spatial s);
}
