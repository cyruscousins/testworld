package actor;

import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import com.sun.xml.internal.ws.wsdl.writer.document.soap.Body;
import combat.equipment.ArrowData;
import world.World;

/**
 * A CollectableItem is a physics object until it comes to rest, then it just waits for someone to pick it up.
 */
public class CollectableItem extends Geometry implements WorldDynamic, Collectable{
    public static final int TYPE_ARROW = 0, TYPE_SWORD = 1, TYPE_EQUIPMENT = 2;
    public static final ColorRGBA[] typeColors = new ColorRGBA[]{ColorRGBA.Brown, ColorRGBA.Gray, ColorRGBA.Cyan};

    int type;
    Object[] collectables;
    
    float mass;
    float radius;
    ColorRGBA color;

    RigidBodyControl body;
    
    public static final int MODE_STILL = 0, MODE_PHYSICS = 1;
    public int mode;
    
    public World world;
    
    public CollectableItem(int type, Object[] collectables, float mass, float radius, ColorRGBA color) {
        this.type = type;
        this.collectables = collectables;
        this.mass = mass;
        this.radius = radius;
        this.color = color;
        
        setMesh(new Sphere(4, 4, radius));
        body = new RigidBodyControl(new SphereCollisionShape(radius));
        
        addControl(body);
    }
    
    public void addToWorld(World world, Node node){
        this.world = world;
        node.attachChild(this);
    }
    
    public void setMode(int newMode){
        
        if(newMode == MODE_PHYSICS){
            if(mode == MODE_STILL && parent != null){
                body.setPhysicsLocation(getLocalTranslation());
                world.space.add(this);
                this.mode = newMode;
            }
        }
        else if(newMode == MODE_STILL){
            if(mode == MODE_PHYSICS){
                world.space.remove(this);
                this.mode = newMode;
            }
        }
        
    }

    @Override
    public void removeFromWorld() {
        parent.detachChild(this);
        if(mode == MODE_PHYSICS){
            world.space.remove(this);
        }
    }

    @Override
    public void update(float tpf) {
        
        //Possibly rotate.
        
        //Stop movement.
        if(mode == MODE_PHYSICS){
            if(body.getLinearVelocity().lengthSquared() < .01f){
                setMode(MODE_STILL);
            }
        }
        
    }

    @Override
    public Vector3f getLocation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setLocation(Vector3f loc) {
        if(mode == MODE_PHYSICS){
            body.setPhysicsLocation(loc);
        }
        else{
            setLocalTranslation(loc);
        }
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public Object[] getData() {
        return collectables;
    }

    @Override
    public void pickUp(Collector collector) {
        switch (type){
            case TYPE_ARROW:
                for(ArrowData arrow : (ArrowData[]) collectables){
                    collector.addArrow(arrow); //todo listen to success.
                }
                break;
        }
    }
}
