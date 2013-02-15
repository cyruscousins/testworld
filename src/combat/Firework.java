/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package combat;

import actor.Combatable;
import combat.WorldAttack;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.influencers.ParticleInfluencer;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.HashMap;
import world.World;

/**
 *
 * @author Cyrus Cousins
 */
public class Firework implements WorldAttack{
    
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
    
    public static final int STAGE_FLY = 0, STAGE_EXPLODE = 1;
    float timer;
    int stage;
    float launchTime;
    float explodeTime;
    PointLight flash;
    ParticleEmitter trail;
    ParticleEmitter fire;
    Vector3f loc;
    Vector3f dir;
    Vector3f vel;
    
    ColorRGBA color;
    
    World world;
    Node parent;
    Node collidables;
    
    public Firework(Combatable owner, ElementalVector element, Vector3f pos, Vector3f dir, ColorRGBA color, int launchTime, int explodeTime) {
        
        this.element = new ElementalVector(0, 1);
        this.owner = owner;
        
        this.loc = pos;
        this.dir = dir;
        this.color = color;
        flash = new PointLight();
        flash.setColor(color);
        flash.setRadius(100);
        
        fire = new ParticleEmitter("Fire", ParticleMesh.Type.Point, 50);
        Material mat_red = new Material(art.Texturer.assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat_red.setTexture("Texture", art.Texturer.assetManager.loadTexture("Effects/Explosion/flame.png"));
        fire.setMaterial(mat_red);
        fire.setImagesX(2); fire.setImagesY(2); // 2x2 texture animation
        fire.setEndColor(  color);   // red
        fire.setStartColor(color); // yellow
        fire.setStartSize(1.5f);
        fire.setEndSize(0f);
        fire.setLowLife(0f);
        fire.setHighLife(5f);
        fire.setParticlesPerSec(0);
        
        ParticleInfluencer influencer = fire.getParticleInfluencer();
        influencer.setInitialVelocity(new Vector3f(4, 4, 4));
        influencer.setVelocityVariation(1);
        
        
        trail = new ParticleEmitter("Fire", ParticleMesh.Type.Triangle, 100);
//        Material mat_red = new Material(Modeller.assetManager, "Common/MatDefs/Misc/Particle.j3md");
//        mat_red.setTexture("Texture", Modeller.assetManager.loadTexture("Effects/Explosion/flame.png"));
        trail.setMaterial(mat_red);
        trail.setImagesX(2); trail.setImagesY(2); // 2x2 texture animation
        trail.setStartColor(new ColorRGBA(1,1,.8f,1f));
        trail.setEndColor(new ColorRGBA(.8f,.4f,.2f,.5f));
        trail.setStartSize(.2f);
        trail.setEndSize(.1f);
        trail.setLowLife(0f);
        trail.setHighLife(2f);
        trail.setParticlesPerSec(50);
        trail.setLocalTranslation(pos);
        
        influencer = trail.getParticleInfluencer();
        influencer.setInitialVelocity(dir);
        influencer.setVelocityVariation(.1f);
        
        this.launchTime = launchTime;
        this.explodeTime = explodeTime;
    }
    
    public void addToWorld(World world, Node node) {
        this.world = world;
        this.parent = node;
        
        this.collidables = world.collidables;
        
        parent.attachChild(trail);
        world.addProjectile(this);
        
        
        //space.add(body);
    }

    @Override
    public void removeFromWorld() {
        parent.removeLight(flash);
        parent.detachChild(fire);
        parent.detachChild(trail);
        //space.remove(pos)
        //.add(this);
    }

    //Collision stuff
    CollisionResults collisions = new CollisionResults();
    Ray ray = new Ray();
    
    @Override
    public void update(float tpf) {
        timer += tpf;
        switch(stage){
            case 0: //projectile stage
                //Calc where we are going
                vel = dir.mult(tpf, vel); //multiply into vel
                
                //Collision detection
                ray.setOrigin(loc);
                ray.setDirection(dir);
                ray.setLimit(vel.length());
                
                
                collisions.clear();
                if(detectCollisions(collidables, ray)){ //TODO keep track of vel separately from dir.
                    explode();
                }
                else{
                    //update position

                    loc.addLocal(vel);
                    trail.setLocalTranslation(loc);
                    //trail.
                    if(timer > launchTime){
                        explode();
                    }
                }
            break;
            case 1:
                flash.setColor(color.multLocal(1 - (timer / explodeTime)));
                if(timer > explodeTime) removeFromWorld();
            break;
        }
    }
    
    public void explode(){
        stage = STAGE_EXPLODE;

        trail.setParticlesPerSec(0);

        parent.attachChild(fire);
        fire.setLocalTranslation(loc);
        fire.emitAllParticles();

        flash.setPosition(loc);
        parent.addLight(flash);
        timer = 0;
    }

    @Override
    public Vector3f getLocation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setLocation(Vector3f loc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public boolean detectCollisions(Node objects, Ray ray){
         // Reset results list.
         CollisionResults results = new CollisionResults();
         objects.collideWith(ray, results);
         // Print the results so we see what is going on

         
//         for (int i = 0; i < results.size(); i++) {
//           // For each “hit”, we know distance, impact point, geometry.
//           float dist = results.getCollision(i).getDistance();
//           Vector3f pt = results.getCollision(i).getContactPoint();
//           String target = results.getCollision(i).getGeometry().getName();
//           System.out.println("Selection #" + i + ": " + target + " at " + pt + ", " + dist + " WU away.");
//         }
         
         
         // 5. Use the results -- we rotate the selected geometry.
         if (results.size() > 0) {
           CollisionResult closest = results.getClosestCollision();
           Vector3f collisionPt = closest.getContactPoint();
//           if(collisionPt.distanceSquared(loc) < vel.lengthSquared()){
               // The closest result is the target that the player picked:
               Geometry closestGeom = closest.getGeometry();
               if(closestGeom == owner) return false; //No collision with owner.
               if(closestGeom instanceof Combatable){
                   System.out.println("Collided with a combatable");
                   ((Combatable)closestGeom).takeProjectile(this);
               }
               else{
                   System.out.println("Firework collision non combatable");
               }
               return true;
//           }
           
        }
        return false;
    }

    @Override
    public void collideWithSpatial(Spatial s) {
        //NEVER HAPPENS
        Thread.dumpStack();
        System.exit(0);
    }
}
