package actor;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import world.World;

/**
 * A WorldDynamic represents a moving world object that requires framely updates.  
 * WorldDynamics may or may not use Bullet, and may or may not respond to Bullet collision or custom collision events.
 * @author Cyrus Cousins
 */
public interface WorldDynamic{
    public abstract void addToWorld(World world, Node parent);
    public abstract void removeFromWorld();
    public abstract void update(float tpf);
    public abstract Vector3f getLocation();
    public abstract void setLocation(Vector3f loc);
}
