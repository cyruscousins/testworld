package combat;

import actor.Combatable;
import art.Texturer;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import combat.ElementalVector;
import combat.WorldAttack;
import combat.equipment.SwordData;
import world.World;
import world.WorldStatic;
import world.WorldStaticDestructible;

/**
 * Represents a physical sword in the actual world
 */
public class Sword extends Geometry implements WorldAttack {
    SwordData data;

    Combatable owner;
    ElementalVector atk;
    
    public float time;
    
    public float maxTime = .5f;
    
    public Sword(SwordData data, Combatable owner){
        this.data = data;
        this.owner = owner;
        
        atk = new ElementalVector(0, 16);
        
//        setMesh(new Box(.1f, data.length, .1f));
        setMesh(new Box(.2f, data.length + 4, .2f));
//        setMesh(new Cylinder());
        
//        Material mat = new Material(Texturer.assetManager, "Common/MatDefs/Light/Lighting.j3md"); //TODO arrow texture.
//        mat.setTexture("DiffuseMap", Texturer.solidColor(data.blade.getColor()));
//        
//        mat.setFloat("Shininess", data.blade.getParameter("Color Specular"));
//        
//        setMaterial(mat);
        
        setMaterial(Texturer.genMatTex
                (32, data.blade));
        
    }
    
    public Combatable getOwner() {
        return owner;
    }

    @Override
    public ElementalVector getElement() {
        return atk;
    }

    @Override
    public void collideWithSpatial(Spatial s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    World world;
    @Override
    public void addToWorld(World world, Node parent) {
        parent.attachChild(this);
        this.world = world;
    }

    @Override
    public void removeFromWorld() {
        parent.detachChild(this);
    }

    Vector3f dir = new Vector3f();
    
    @Override
    public void update(float tpf) {
        
        time += tpf;
        
        //Set angle
        
        //Ray collision detection.
        Ray fwd = new Ray(getLocalTranslation(), dir);
        fwd.limit = data.length;
        
        if(detectCollisions(world.collidables, fwd)){
            time = maxTime;
        }
    }
    
    Vector3f tv = new Vector3f();
    Quaternion tq = new Quaternion();
    public void updateFacing(Vector3f p){
        
        float theta = FastMath.atan2(p.z, p.x);
        float mag = FastMath.sqrt(p.x * p.x + p.z * p.z);
        
        theta += (time / maxTime - .5f) * FastMath.HALF_PI;

        dir.set(FastMath.cos(theta) * mag, p.y, FastMath.sin(theta) * mag);

        tq.fromAngleAxis(dir.angleBetween(Vector3f.UNIT_Y), dir.cross(0, -1, 0, tv));
        setLocalRotation(tq);
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
                   System.out.println("Sword collided with a combatable");
                   ((Combatable)closestGeom).takeProjectile(this);
               }
               else if(closestGeom instanceof WorldStatic){
                   
                    if(closestGeom instanceof WorldStaticDestructible){
                        if(((WorldStaticDestructible)closestGeom).takeDamage(2)){ //TODO damage model
                            return false;
                        }
                    }
                    
                    //TODO take damage.
                   System.out.println("Sword collision non combatable");
               }
               return true;
//           }
           
        }
        return false;
    }
}