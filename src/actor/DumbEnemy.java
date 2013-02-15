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
import combat.Sword;
import combat.WorldAttack;
import combat.equipment.SwordData;
import systems.MaterialData;
import world.World;

/**
 *
 * @author Cyrus Cousins
 */
public class DumbEnemy extends Combatable {

    World world;
    RigidBodyControl body;

    Material angry, normal;
    
    
    Sword sword;
    SwordData swordData = new SwordData(MaterialData.getRandomMaterial("Wood"), MaterialData.getRandomMaterial("Metal"));
    
    public DumbEnemy(int element, World world, Vector3f loc) {
        super(element, 1, 10);
        this.world = world;
//        Geometry geom = new Geometry(getName(), Modeller.getSphere());

        float radius = 2;
        float weight = 4;
        
        setMesh(new Sphere(16, 16, radius));
        body = new RigidBodyControl(new SphereCollisionShape(radius), weight);
        addControl(body);

//        Material mat = new Material(Texturer.assetManager, "MatDefs/OrbShade.j3md");
        normal = new Material(Texturer.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        normal.setColor("Color", ColorRGBA.Blue);
        
        angry = new Material(Texturer.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        angry.setColor("Color", ColorRGBA.Red);

//        Material mat = Texturer.wireframe();

//        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha); // enables alpha blending mode
//        setQueueBucket(Bucket.Transparent); // enables back-to-front sorting

        body.setPhysicsLocation(loc);
        setMaterial(normal);

        //randomize some traits

        //set stats
        statStatCur = new float[]{1, 1, 1, 1, 1};
        statStatMax = new float[]{1, 1, 1, 1, 1};
        
        float dyn = 30;
        dynStatCur = new float[]{dyn, dyn, dyn, dyn, dyn};
        dynStatMax = new float[]{dyn, dyn, dyn, dyn, dyn};

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
        if(sword != null){
            world.removeProjectile(sword);
            sword.removeFromWorld();
            sword = null;
        }
    }

    public void die() {
        if (parent != null) {
            System.out.println("Dead");
            for (int i = 0; i < 10; i++) {
                //           Collectable c = new Collectable(4, getLocalTranslation(), new Vector3f(FastMath.rand.nextFloat() - .5f, FastMath.rand.nextFloat() - .5f, FastMath.rand.nextFloat() + 1));
                //           c.addToWorld(world, parent);
            }
            removeFromWorld();//when removed, parent is set to null.
        }
    }
    
    Combatable target;
    float angerTime = 0;
    float heading;

    @Override
    public void update(float tpf) {
        if (parent == null) {
            return;
        }
        
        angerTime -= tpf;
        if(angerTime < 0){
            angerTime = 0;
            target = null;
        }

        if (target != null) {
            Vector3f tl = target.getLocalTranslation();
            Vector3f dir = tl.subtract(getLocalTranslation());
            float distance = dir.length();
            dir.normalizeLocal();
            
            
            body.applyCentralForce(dir.multLocal(10 * body.getMass()));
//            System.out.println("Chasing " + target.getName());
            
            setMaterial(angry);
            
            //Swording
            
            //SWORD STUFF (temp)
            if(sword != null){
                if(sword.time > sword.maxTime){
                    world.removeProjectile(sword);
                    sword.removeFromWorld();
                    sword = null;
                }
                else{
                    Vector3f sloc = sword.getLocalTranslation();
                    sloc.set(getLocalTranslation());
//                    sloc.y += height * .5f;
                    
                    sword.setLocalTranslation(sloc);
                    
                    sword.updateFacing(dir);
                    sword.updateFacing(new Vector3f(0, 0, 1));
                }
            }
            else if(distance < 8){
                System.out.println("Swording");
                sword = new Sword(swordData, this);
                
                sword.addToWorld(world, parent);
                world.addProjectile(sword);
            }
            
            
        }
        else{
            setMaterial(normal);
        }
        
//         float forceScalar = tpf * 100 * body.getMass();
//        
//        if (target != null) {
//            Vector3f tl = target.getLocalTranslation();
//            Vector3f pos = getLocalTranslation();
//            float desiredHeading = FastMath.atan2(tl.z - pos.z, tl.x - pos.x);
//
//            heading = desiredHeading;
//            System.out.println("Chasing player");
//            forceScalar *= -10;
//        } else {
//            heading += (FastMath.rand.nextFloat() - .5f) * tpf;
//        }
//
//        body.applyCentralForce(new Vector3f(forceScalar * FastMath.sin(heading), 0, forceScalar * FastMath.cos(heading)));

        //chase player
//        if (world.player.getLocalTranslation().distanceSquared(getLocalTranslation()) < (10 * 10)) {
//            target = world.player;
//            if(angerTime < 1) angerTime = 1;
//            System.out.println("Player seen");
//        }

//        phase += tpf;
//
//        Vector3f loc = body.getPhysicsLocation();
//
//        switch (type) {
//            case UPDOWN:
//                loc.y = baseLoc.y + amplitude * FastMath.sin(frequency * phase);
//                break;
//            case CIRCLE:
//                loc.x = baseLoc.x + amplitude * FastMath.sin(frequency * phase);
//                loc.z = baseLoc.z + amplitude * FastMath.cos(frequency * phase);
//                loc.y = 4 + world.groundManager.heightMap.value(loc.x, loc.z);
//                break;
//        }
//        body.setPhysicsLocation(loc);
    }
    

    @Override
    public Vector3f getLocation() {
        return getLocalTranslation();
    }

    @Override
    public void setLocation(Vector3f loc) {
        setLocalTranslation(loc);
    }

    public boolean takeRawDamage(float dmg, Combatable dealer) {
        this.target = dealer;
        angerTime += 10;
        return super.takeRawDamage(dmg, dealer);
    }
    
    
}
