package combat;

import combat.equipment.Bow;
import actor.Combatable;
import actor.Player;
import art.Texturer;
import com.bulletphysics.linearmath.QuaternionUtil;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.shader.VarType;
import combat.equipment.ArrowData;

import world.World;
import world.WorldStatic;
import world.WorldStaticDestructible;

/**
 * @author ccousins
 * Arrow objects are Projectiles, and contain information used to calculate damage and other attributes.
 */
public class Arrow extends Geometry implements WorldAttack{
    
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
    
    //PHYSICS info
    World world;
    RigidBodyControl body;
    
    //ARROW info
    ArrowData data;
    
    //dynamic arrow info
    public static final int COCKED = 1, FLYING = 2, EMBEDDED = 3;
    int state;
    
    //Constructor: Handles initialization of variables, mesh and texture generation only

    public Arrow(Combatable owner, ArrowData a) {
        this.owner = owner;
        this.data = a;
        
        float radius = .1f;
//        setMesh(new Cylinder(2, 6, .1f, .5f));
        setMesh(new Box(radius, data.length, radius));

//        setMesh(new Cylinder(2, 6, 1, 3));
//        setMesh((new Sphere(4, 4, 8)));
//        setMaterial(Texturer.wireframe());
        
        
//        Material mat = new Material(Texturer.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
//        mat.setTexture("ColorMap", Texturer.createSun(128, .1f, .2f, ColorRGBA.Black));
        

        
//        mat.setBoolean("", true);
        
//        mat.setBoolean("UseMaterialColors", true);
//        mat.setColor("Color", data.headMat.getColor());

//        Material mat = new Material(Texturer.assetManager, "Common/MatDefs/Light/Lighting.j3md"); //TODO arrow texture.
//        mat.setTexture("DiffuseMap", Texturer.solidColor(data.headMat.getColor()));
//        
//        mat.setFloat("Shininess", data.headMat.getParameter("Color Specular"));
//        
//        setMaterial(mat);
        
        setMaterial(Texturer.genMatTex(16, data.headMat));
        
        
        body = new RigidBodyControl(new CylinderCollisionShape(new Vector3f(.1f, .1f, data.length)), data.weight);
//        body = new RigidBodyControl(new SphereCollisionShape(.1f), 1);
        addControl(body);
        
        name = "Arrow: " + name;
    }
    
    
    
    /*
     * Calculate the amount of damage done by an arrow 
     * Arrows deal blunt impact damage, based on data.weight, and pierce damage, based on data.sharpness.
     * Target provides its hardness and element, which is used in the calculation.
     */
    public float calcDamage(float hardness, int element){
        return (data.sharpness / hardness + data.weight) ; /* * Game.getElementalDamage(this.element, element); */
    }
    
    public boolean calcBreak(float hardness){
        if(data.durability < FastMath.rand.nextFloat() * hardness){
            //break code
            //change model
            return true;
        }
        //else damage the arrow
        //TODO good arrow damage function, effect more stuff.
        data.durability *= 1f - hardness * .1f;
        return false;
    }
    
    
    public void fireArrow(World world, Vector3f fireLoc, Vector3f direction, Vector3f firerVelocity, Bow bow, float energy){
        Vector3f loc = direction.mult(data.length);
        loc.addLocal(fireLoc);
        
        //TODO tweak direction based on bow accuracy & trueness.  Use good physics
        float inaccuracyMax = bow.accuracy + data.trueness;

        Vector3f arrowVel = direction.mult(energy / data.weight);
        arrowVel.addLocal(inaccuracyMax + (FastMath.rand.nextFloat() * 2 - 1), inaccuracyMax + (FastMath.rand.nextFloat() * 2 - 1), inaccuracyMax + (FastMath.rand.nextFloat() * 2 - 1));
        arrowVel.normalizeLocal();
        arrowVel.multLocal(energy / data.weight);
        arrowVel.addLocal(firerVelocity);
        
        world.space.add(body);
        world.addProjectile(this);
        
        body.setLinearVelocity(arrowVel); //TODO accurate physics.
        body.setPhysicsLocation(loc);
        updateRotation();
        
        System.out.println("Arrow fired at " + loc + ", vel " + arrowVel);
        
        state = FLYING;
        
        element = new ElementalVector(0, arrowVel.length() * data.sharpness); //TODO better damage model, should factor relative speeds in, so this is unnecessary.
    }
    
    public void embedArrow(){
        state = EMBEDDED;
        body.setMass(0); //hack so the arrow stays put.
        body.setCollisionShape(new SphereCollisionShape(2));
    }

    //PROJECTILE info

    @Override
    public void addToWorld(World world, Node parent) {
        
        System.out.println("NODE " + parent);
        parent.attachChild(this);
    
        fire = Texturer.getFire();
//        fire.setMaterial(null);
        parent.attachChild(fire);
        
        this.world = world;
        this.parent = parent;
    }
    Spatial fire;

    @Override
    public void removeFromWorld() {   
        if(parent != null){ //parent is nulled here, can't remove multiple times.
            parent.detachChild(fire); //test

            parent.detachChild(this);
            if(state != COCKED){
                world.space.remove(body);
            }
            world.removeProjectile(this);
        }
    }
    
    float time = 0;

    @Override
    public void update(float tpf) {
//        if(FastMath.rand.nextFloat() < .1f) System.out.println("ARROW " + body.getPhysicsLocation());

        //ROTATE the arrow
        if(state == FLYING){
            updateRotation();
        }
        
        time += tpf;
        if(time > 20){
            removeFromWorld();
        }
        
//        fire.setLocalTranslation(body.getPhysicsLocation());
    }
    
    Quaternion quat = new Quaternion();
    public void updateRotation(){
        Vector3f dir = body.getLinearVelocity().normalize();
//        quat.fromAngleAxis(dir.angleBetween(Vector3f.UNIT_Y), dir.crossLocal(Vector3f.UNIT_Y));
        quat.fromAngleAxis(dir.angleBetween(Vector3f.UNIT_Y), dir.crossLocal(0, -1, 0));
        body.setPhysicsRotation(quat);
    }
    
    @Override
    public Vector3f getLocation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setLocation(Vector3f loc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void collideWithSpatial(Spatial s) {
        if(s instanceof Combatable)
        {
            if(state == FLYING){
                if(s == owner){
                    System.out.println("Collide with owner");
                    return;
                }
                System.out.println("Arrow HIT COMBATABLE");
                ((Combatable)s).takeProjectile(this);
                removeFromWorld();
            }
            else if(state == EMBEDDED){
                if(s instanceof Player){
                    //pick up this arrow.
//                    System.out.println("PLAYER PICK UP ARROW");
                    if(((Player)s).addArrow(data)){
                        removeFromWorld();
                    }
                    //else quiver full, ignore
                }
            }
        }
        else if(s instanceof WorldStatic){
            if(state != EMBEDDED){
                //First do damage to object
                if(s instanceof WorldStaticDestructible){
                    if(((WorldStaticDestructible)s).takeDamage(2)){ //TODO damage model
                        return;
                    }
                }
                //TODO material collided with hardness
                if(calcBreak(((WorldStatic)s).matdat.getParameter("Hardness"))){
                    //TODO broken arrow model, collect for materials
                    world.removeProjectile(this);
                }
                else{
                    embedArrow();
                }
                
            }
        }
        else
        {
//            System.out.println("PHYSICS HIT NONCOMBATABLE");
            if(state != EMBEDDED){
                //TODO material collided with hardness
                if(calcBreak(.5f)){
                    //TODO broken arrow model, collect for materials
                    world.removeProjectile(this);
                }
                else{
                    embedArrow();
                }
            }
//            removeFromWorld();
            //Stick
        }
    }
}
