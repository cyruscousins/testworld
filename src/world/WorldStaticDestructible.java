package world;

import com.jme3.bullet.control.PhysicsControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.scene.Mesh;
import systems.MaterialData;

/**
 *
 */
public class WorldStaticDestructible extends WorldStatic{

    float durability;
    public WorldStaticDestructible(String name, Mesh mesh, Material material, RigidBodyControl body, MaterialData matDat, float durability) {
        super(name, mesh, material, body, matDat);
        this.durability = durability;
    }
    
    public boolean takeDamage(float amt){
        durability -= amt;
        System.out.println("Destructible Object \"" + name + "\" has " + durability + " durability left.");
        if(durability < 0){
            removeFromWorld();
            return true;
        }
        return false;
    }
}
