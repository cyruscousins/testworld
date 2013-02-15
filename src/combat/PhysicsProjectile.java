package combat;

import actor.Combatable;
import art.Texturer;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.influencers.ParticleInfluencer;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.HashMap;
import world.World;

/**
 *
 * This type of projectile obeys physics, is contained in the physics space, and detects physics collisions.  
 */
public class PhysicsProjectile extends Node implements WorldAttack {
    //COMMON PROJECTILE HEADER: This is to get around java's lack of multiple inheritance
    Combatable owner;
    public Combatable getOwner(){
        return owner;
    }
    
    ElementalVector element;
    public ElementalVector getElement(){
        return element;
    }
    //END HEADER

    //Node parent;//we are a spatial, so use their parent.  
    World world;
    RigidBodyControl body;
    
    ParticleEmitter fire;
    PointLight light;
    ColorRGBA color;
    
    Node parent;
    
    public PhysicsProjectile(Combatable owner, ElementalVector element, Vector3f pos, Vector3f vel, float radius, float mass){
        
        this.owner = owner;
        this.element = element;
        
        color = Combatable.elemColors[element.mainElementIndex];
        
        body = new RigidBodyControl(new SphereCollisionShape(radius), mass);
        
        addControl(body);
        
        body.setPhysicsLocation(pos);
        body.setLinearVelocity(vel);
        
        System.out.println("START AT " + pos + " VEL" + vel);
        
        //graphics
        light = new PointLight();
        light.setColor(color);
        light.setRadius(100);
        
        fire = new ParticleEmitter("Fire", ParticleMesh.Type.Triangle, 50);
        Material mat_red = new Material(Texturer.assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat_red.setTexture("Texture", Texturer.assetManager.loadTexture("Effects/Explosion/flame.png"));
        fire.setMaterial(mat_red);
        fire.setImagesX(2); fire.setImagesY(2); // 2x2 texture animation
        fire.setEndColor(  color);   // red
        fire.setStartColor(color); // yellow
        fire.setStartSize(1f);
        fire.setEndSize(0f);
        fire.setLowLife(0f);
        fire.setHighLife(2f);
        fire.setParticlesPerSec(25);
        
        ParticleInfluencer influencer = fire.getParticleInfluencer();
        influencer.setInitialVelocity(new Vector3f(4, 4, 4));
        influencer.setVelocityVariation(1);
        
        attachChild(fire);
        
        setName("Physics Projectile");
    }
    
    public static PhysicsProjectile getTargetedProjectile(Vector3f pos, Vector3f target, float initialVelocity, int element, float damage){
//        Vector3f dif = target.subtract(pos);
//        
//        //directions of x and y coordinates.  
//        
//        float hDist = FastMath.sqrt(dif.x * dif.x + dif.y * dif.y);
//        float zDist = dif.z;
//        
//        
//        float xd = s * dif.x;
//        float yd = s * dif.y;
        return null;
    }
    
    public void detectCollisions(Node objects, HashMap<String, Combatable> combatables){ }

    public void addToWorld(World world, Node parent) {
        this.world = world;
        this.parent = parent;
        
        parent.attachChild(this);
        parent.addLight(light);
        
        world.addProjectile(this);
        world.space.add(body);
        
    }

    @Override
    public void removeFromWorld() {
        parent.detachChild(this);
        parent.removeLight(light);
        
        world.removeProjectile(this);
        world.space.remove(body);
    }

    float time;
    @Override
    public void update(float tpf) {
        
        Vector3f loc = body.getPhysicsLocation();
        
//        if(FastMath.rand.nextFloat() < .1f) 
            System.out.println("PHYSICS: " + loc + " -> " + body.getLinearVelocity() + " gh: " + world.groundManager.heightMap.value(loc.x, loc.z));
        
        if(world.groundManager.heightMap.value(loc.x, loc.z) > loc.y + 1){
            body.getLinearVelocity().y *= -1;
            removeFromWorld();
            System.out.printf("Physics proj below ground.  Removing. (loc: %s, gy: %f)", loc.toString(), world.groundManager.heightMap.value(loc.x, loc.z));
        }
        
        //we can probably use references to just set this once (using our location), it might stay attached.  
        light.setPosition(body.getPhysicsLocation());
        //fire.setLocalTranslation(body.getPhysicsLocation());
        
        time += tpf;
        if(time > 16){
            removeFromWorld();
        }
        
    }
    
    public void collideWithSpatial(Spatial s){
        if(s == owner){
            System.out.println("Collide with owner");
            return;
        }
        if(s instanceof Combatable)
        {
            System.out.println("PHYSICS HIT COMBATABLE");
            ((Combatable)s).takeProjectile(this);
            removeFromWorld();
        }
        else
        {
            System.out.println("PHYSICS HIT NONCOMBATABLE");
            removeFromWorld();
        }
    }

    @Override
    public Vector3f getLocation() {
        return body.getPhysicsLocation();
    }

    @Override
    public void setLocation(Vector3f loc) {
        body.setPhysicsLocation(loc);
    }
}