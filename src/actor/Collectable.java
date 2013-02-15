package actor;

import com.jme3.scene.Node;
import world.World;

/**
 *
 * @author ccousins
 */
public interface Collectable {
    public int getType();
    public Object[] getData();
    public void pickUp(Collector collector);
    
//    public void addToWorld(World world, Node node);
//    public void removeFromWorld();
}
