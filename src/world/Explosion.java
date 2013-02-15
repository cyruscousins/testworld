/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package world;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author ccousins
 */
public class Explosion {
    
    public Explosion(){
        
    }
    
    public void apply(World world, Vector3f center, Node collidables, Node effects){
        float radius = 16;
        
        //darken the ground around the explosion.  This could be done incrementally on updates.
        Vector3f dir = new Vector3f();
        Ray ray = new Ray();
        for(int i = 0; i < 16; i++){
            dir.x = FastMath.rand.nextFloat() * 2 - 1;
            dir.y = FastMath.rand.nextFloat() * 2 - 1;
            dir.z = FastMath.rand.nextFloat() * 2 - 1;
            if(dir.lengthSquared() > 1) continue; //not in the sphere
            
            dir.normalize();
            
            ray.setDirection(dir);
            ray.setOrigin(center);
            ray.setLimit(radius);
            
         // Reset results list.
         CollisionResults results = new CollisionResults();
         collidables.collideWith(ray, results);
         // Print the results so we see what is going on

         
//         for (int i = 0; i < results.size(); i++) {
//           // For each “hit”, we know distance, impact point, geometry.
//           float dist = results.getCollision(i).getDistanwce();
//           Vector3f pt = results.getCollision(i).getContactPoint();
//           String target = results.getCollision(i).getGeometry().getName();
//           System.out.println("Selection #" + i + ": " + target + " at " + pt + ", " + dist + " WU away.");
//         }
         
         
         // 5. Use the results -- we rotate the selected geometry.
         if (results.size() > 0) {
           CollisionResult closest = results.getClosestCollision();
           Vector3f collisionPt = closest.getContactPoint();
           if(collisionPt.distanceSquared(loc) < vel.lengthSquared()){
               // The closest result is the target that the player picked:
               Geometry closestGeom = closest.getGeometry();
               if(closestGeom instanceof Combatable){
                   System.out.println("Collided with a combatable");
                   ((Combatable)closestGeom).takeProjectile(this);
               }
               else{
                   System.out.println("Firework collision non combatable");
               }
               return true;
           }
           
        }
        return false;
            
        }
    }
}
