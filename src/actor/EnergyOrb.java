/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package actor;

import art.Texturer;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import com.jme3.terrain.noise.Color;
import com.jme3.util.TangentBinormalGenerator;
import combat.ElementalVector;
import combat.Firework;
import combat.PhysicsProjectile;
import combat.WorldAttack;
import world.World;

/**
 *
 * @author Cyrus Cousins
 */
public class EnergyOrb extends Combatable{
    
    Vector3f baseLoc;
    float phase;
    float amplitude;
    float frequency;
            
    World world;
    
    RigidBodyControl body;
    
    public EnergyOrb(int element, World world, Vector3f loc){
        super(element, 1, 10);
        this.world = world;
//        Geometry geom = new Geometry(getName(), Modeller.getSphere());
        
        float radius = 2;
        setMesh(new Sphere(16, 16, radius));
        body = new RigidBodyControl(new SphereCollisionShape(radius), 0);
        addControl(body);
        
//        Material mat = new Material(Texturer.assetManager, "MatDefs/OrbShade.j3md");
        Material mat = new Material(Texturer.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
//        mat.setColor("m_Color", elemColors[element]);
//        mat.setTexture("m_Texture", Texturer.smokeTex(128, 16));
        mat.setTexture("ColorMap", Texturer.createSun(128, .1f, .2f, elemColors[element]));
//        mat.setFloat("Speed", .125f);
        
//        Material mat = Texturer.wireframe();
        
//        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha); // enables alpha blending mode
//        setQueueBucket(Bucket.Transparent); // enables back-to-front sorting
        
        body.setPhysicsLocation(loc);
        setMaterial(mat);
        
        //randomize some traits
        
        frequency = FastMath.rand.nextFloat() + .25f;
        amplitude = FastMath.rand.nextFloat() * 4 + 4;
        
        baseLoc = new Vector3f(loc.x, world.groundManager.heightMap.value(loc.x, loc.z) + FastMath.rand.nextFloat() * 4 + amplitude + radius + 1, loc.z);
        
        //set stats
        statStatCur = new float[]{1, 1, 1, 1, 1};
        statStatMax = new float[]{1, 1, 1, 1, 1};
        dynStatCur = new float[]{10, 10, 10, 10, 10};
        dynStatMax = new float[]{10, 10, 10, 10, 10};
        
    }
    
    public void addToWorld(World world, Node worldNode) {
        worldNode.attachChild(this);
//        this.parent = worldNode;
        
        world.addCombatable(this);
        
        world.space.add(body);
    }

    @Override
    public void removeFromWorld() {
        parent.detachChild(this);
        world.space.remove(body);
    }
    public void die(){
        if(parent != null){
            System.out.println("Dead");
            for(int i = 0; i < 10; i++){
    //           Collectable c = new Collectable(4, getLocalTranslation(), new Vector3f(FastMath.rand.nextFloat() - .5f, FastMath.rand.nextFloat() - .5f, FastMath.rand.nextFloat() + 1));
    //           c.addToWorld(world, parent);
            }
            removeFromWorld();//when removed, parent is set to null.
        }
    }

    public static final int UPDOWN = 0, CIRCLE = 1;
    int type = FastMath.rand.nextInt(2);
    
    @Override
    public void update(float tpf) {
        if(parent == null) return;
        if(world.player.getLocalTranslation().distanceSquared(getLocalTranslation()) < (10 * 10) && FastMath.rand.nextFloat() > .9f){
            //target the player
            Vector3f dir = world.player.getLocalTranslation
                    ().subtract(getLocalTranslation()).normalizeLocal().mult(16);
            WorldAttack proj = new Firework(this, new ElementalVector(0, 1), getLocalTranslation().clone(), dir, ColorRGBA.Black, 4, 2);
            proj.addToWorld(world, parent);
//            Vector3f dir = math.AdvancedMath.ballisticTarget(getLocalTranslation(), world.player.getLocalTranslation(), 10, 9.8f);
//            if(dir != null){ile(this, new ElementalVector(0, 1), getLocalTranslation(), dir, 2, 4);//PhysicsProjectile.generateTargetedProjectile(getLocalTranslation(), world.player.getLocalTranslation(), level, element);
//                proj.addToWorld(world, parent);
//                Projectile proj = new PhysicsProjectile(this, new ElementalVector(0, 1), getLocalTranslation(), dir, 2, 4);//PhysicsProjectile.generateTargetedProjectile(getLocalTranslation(), world.player.getLocalTranslation(), level, element);
//                proj.addToWorld(world, parent);
//            }
        }
        
        phase += tpf;
        
        Vector3f loc = body.getPhysicsLocation();
        
        switch(type){
            case UPDOWN:
                loc.y = baseLoc.y + amplitude * FastMath.sin(frequency * phase);
                break;
            case CIRCLE:
                loc.x = baseLoc.x + amplitude * FastMath.sin(frequency * phase);
                loc.z = baseLoc.z + amplitude * FastMath.cos(frequency * phase);
                loc.y = 4 + world.groundManager.heightMap.value(loc.x, loc.z);
                break;
        }
        body.setPhysicsLocation(loc);
    }

    @Override
    public Vector3f getLocation() {
        return getLocalTranslation();
    }

    @Override
    public void setLocation(Vector3f loc) {
        setLocalTranslation(loc);
    }
}
