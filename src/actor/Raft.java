package actor;

import art.Texturer;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.sun.xml.internal.ws.wsdl.writer.document.soap.Body;
import systems.MaterialData;
import world.World;

/**
 *
 * @author ccousins
 */
public class Raft extends Geometry implements WorldDynamic {
    MaterialData mat;
    RigidBodyControl body;
    
    Vector3f halfExtents;
    
    Vector3f[] corners;
    float maxCornerDistance; //the max distance from a corner to the center.
    
    float cornerDepth = .25f; //how deep a corner needs to be to have full float.
    
    
    
    float volume, volumePerCorner;
    
    public Raft(String name, float xSize, float ySize, float zSize, MaterialData mat){
        super(name);
        this.mat = mat;
        
        halfExtents = new Vector3f(xSize, ySize, zSize);
        
//        corners = new Vector3f[]{
////            new Vector3f(xSize, -ySize, zSize),
////            new Vector3f(-xSize, -ySize, zSize),
////            new Vector3f(xSize, -ySize, -zSize),
////            new Vector3f(-xSize, -ySize, -zSize)
//            new Vector3f(xSize, -0, zSize),
//            new Vector3f(-xSize, -0, zSize),
//            new Vector3f(xSize, -0, -zSize),
//            new Vector3f(-xSize, -0, -zSize)
//        };
        
        corners = new Vector3f[9];
        for(int i = 0, x = -1; x <= 1; x+=2){
            for(float y = -.9f; y < 1; y+= 1.8f){
                for(int z = -1; z <= 1; z+=2){
                    corners[i++] = new Vector3f(x * xSize, y * ySize, z * zSize);
                }
            }
        }
        corners[8] = Vector3f.ZERO;
        maxCornerDistance = corners[0].length();
        
        volume = xSize * ySize * zSize;
        volumePerCorner = volume / corners.length;
        
        body = new RigidBodyControl(new BoxCollisionShape(halfExtents), 
                volume * mat.getParameter("Density"));
        
        body.setDamping(.9f, .8f);
        
        addControl(body);
        
        setMesh(new Box(xSize, ySize, zSize));
        setMaterial(Texturer.genMatTex(32, mat));
    }

    @Override
    public void addToWorld(World world, Node parent) {
        parent.attachChild(this);
        world.space.add(body);
    }

    @Override
    public void removeFromWorld() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void update(float tpf) {
        //Rise up from bouyant force.
        float waterHeight = 0f;
        float waterDensity = 2.5f;
        float GRAV = 9.8f;
        
        float baseDepth = body.getPhysicsLocation().y - waterHeight;
        
        if(baseDepth > maxCornerDistance){
            //Not in water
        }
        else if(baseDepth < -maxCornerDistance){
            body.applyCentralForce(new Vector3f(0, waterDensity * volume * GRAV, 0));
        }
        else{
            //Partially in water
            Quaternion rotation = body.getPhysicsRotation();

            Vector3f temp = new Vector3f(); //TODO preserve temp

            for(Vector3f v : corners){
                rotation.mult(v, temp);

                float depth = -(baseDepth + temp.y); //height relative to water

                if(depth > 0){
                    
                    float amt = depth / cornerDepth;
                    if(amt > 1) amt = 1;

                    Vector3f force = new Vector3f(0, waterDensity * volumePerCorner * GRAV * amt, 0);
                    Vector3f floc = temp.clone();

                    body.applyForce(force, floc);

//                float floatFrac = 1; //base this on depth
////                Vector3f force = temp.set(0, h * waterDensity * volumePerCorner, 0);
//                Vector3f force = new Vector3f(0, floatFrac * waterDensity * volumePerCorner * GRAV, 0);
//                Vector3f floc = temp.clone();
////                floc.multLocal(.5f); //apply the force closer to the center of the raft.
//                body.applyForce(force, floc);
//                if(FastMath.rand.nextFloat() < .01f){
//                    System.out.println("by " + baseY + ", vn " + v + ", vr " + temp + ", f: " + force.y);
//                }
                //TODO cap at height
                }

            }
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
