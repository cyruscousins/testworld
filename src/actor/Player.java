package actor;

import combat.Sword;
import combat.Firework;
import art.Resources;
import art.Texturer;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.influencers.ParticleInfluencer;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;
import combat.Arrow;
import combat.equipment.Bow;
import combat.ElementalVector;
import combat.PhysicsProjectile;
import combat.equipment.ArrowData;
import combat.equipment.Equipment;
import combat.equipment.Quiver;
import combat.equipment.SwordData;
import java.util.ArrayList;
import java.util.List;

//import character.*;
import math.MappedSVF2D;
import systems.MaterialData;
import world.*;

/**
 *
 * @author Cyrus Cousins
 */
public class Player extends Combatable implements Collector{
    public static final int MODE_RUN = 1, MODE_AIM = 2, MODE_OVERHEAD = 3, MODE_CINEMATIC = 4;
    public static int mode = MODE_RUN;
    
    //Aim Mode Items
    public float phi;
    public float zoom = 1;
    
    public static final float AIM_ROT_SPEED = .25f;
    
    public float height = 1.8f;
    public float sightFocus = 35; //a complicated variable that influences the camera;
    public float runSpeed;
    public float turnSpeed;
    public float runTimer;
    
    public float heading;
    
    public RigidBodyControl body;
    
    boolean onGround;
    boolean inWater;
    boolean isRunning;
    
    GroundManager ground;
    
    boolean[] keys;
    
    float footforceconstant = 1000f;
    float maxVelocity = 10f;
    float mass = 65;
    
    public static final float MASS = 65f;
        /** Uses Texture from jme3-test-data library! */
    ParticleEmitter fire;
    
    SpotLight headLight;
    World world;
    
    int currency;
    
    Quiver quiver;
    Bow bow;
    float bowPwr;
    ArrowData adat;
    
    Sword sword;
    SwordData swordData;
    
    List<Equipment> equipment = new ArrayList<Equipment>();
    
    float[] waterConversionFactors = new float[]{.5f, .5f, .125f, .25f};
    float[] recoverySpeeds = new float[]{.125f, .0625f, .5f, .25f};
    
    public HUD hud;
    
    float pulseTime; //pulse is effected by energy, which is linked to movement and stuff.
    //function basically takes pulseTime as an x and converts it to a usable repeating function every 1.
    float getPulseValue(){
        float f = pulseTime - (int) pulseTime;
        if(f > .25f){
            if(f < .5f) return f * 2;
            if(f < .75) return (1 - f) * 2;
        }
        return 0;
    }
    //in heartbeats per second.
    float getPulse(){
        return 1.8f - getStatFraction(ENERGY);
    }
    
    public Player(World world, int frameWidth, int frameHeight, Node gui, GroundManager ground, boolean[] keys, AssetManager assetManager){
        super(E_EARTH, 1, 0);
        this.world = world;
        this.ground = ground;
        this.keys = keys;
        
        hud = new HUD(frameWidth, frameHeight, 64, .5f, gui);
        
        body = new RigidBodyControl(new CapsuleCollisionShape(.2f, height, 2), mass);
        
        setMesh(new Sphere(8, 8, 1));
        setMaterial(Texturer.wireframe());
        
        //body.setFriction(100f);
        addControl(body);
        
//        fire = new ParticleEmitter("Fire", ParticleMesh.Type.Triangle, 30);
//        Material mat_red = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
//        mat_red.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));
//        fire.setMaterial(mat_red);
//        fire.setImagesX(2); fire.setImagesY(2); // 2x2 texture animation
//        fire.setEndColor(  new ColorRGBA(1f, 0f, 0f, 1f));   // red
//        fire.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)); // yellow
//        fire.setStartSize(0.6f);
//        fire.setEndSize(0.1f);
//        fire.setLowLife(0.5f);
//        fire.setHighLife(3f);
//        
//        ParticleInfluencer influencer = fire.getParticleInfluencer();
//        influencer.setInitialVelocity(new Vector3f(0, 2f, 0));
//        influencer.setVelocityVariation(1f);
        
//        bg = new Picture("statbar");
//        bg.setImage(assetManager, "Textures/barBG.png", true);
//        bg.setWidth(frameWidth / 4);
//        bg.setHeight(frameHeight / 4);
//        bg.setPosition(16, frameHeight - 16 - frameWidth / 4);
//        bg.move(0, 0, -2);
//        guiNode.attachChild(bg);
        
        
        //STATS
        
//        int barHeight = frameHeight / 4 / 6;
//        maxBarLength = frameWidth * 5 / 4 / 6;//5/6 of 1 / 4
//        String[] files = new String[]{"health", "body", "energy", "food", "water"};
//        stats = new Picture[DYN_STATS];
//        for(int i = 0; i < DYN_STATS; i++){
//            stats[i] = new Picture(files[i]);
//            stats[i].setImage(assetManager, "Textures/" + files[i] + ".png", true);
//            stats[i].setWidth(frameWidth);
//            stats[i].setHeight(barHeight);
//            stats[i].setPosition(16 + frameWidth / 12, frameHeight - 16 - frameHeight / 12 - barHeight * i);
//            stats[i].move(0,0,-1);
//            gui.attachChild(stats[i]);
//        }
        
        initializeStats();
        
//        equipment.add(new Equipment(Equipment.HEAD, MaterialData.getMaterial("Leather")));
//        equipment.add(new Equipment(Equipment.FOOT, MaterialData.getMaterial("Leather")));
//        equipment.add(new Equipment(Equipment.CHEST, MaterialData.getMaterial("Flaxen")));
        
        recalculateStats();
        
        statStatCur[DEFENCE] = .1f;
        
        //LIGHT
        
        headLight = new SpotLight();
        headLight.setColor(new ColorRGBA(.9f, .9f, 1, .5f));
        headLight.setSpotInnerAngle(.1f);
        headLight.setSpotOuterAngle(.15f);
        
        name = "PLAYER: " + name;
        
        quiver = Quiver.randQ();
        bow = new Bow(.9f, 5, .05f, 4);
        
        swordData = new SwordData(MaterialData.getRandomMaterial("Wood"), MaterialData.getRandomMaterial("Metal"));
        
        updateHUD();
    }
    PhysicsSpace space;
    Node rootNode;
    public void addToWorld(World world, Node node){
        rootNode = node;
        this.world = world;
        this.space = world.space;
        world.collidables.attachChild(this);
//        node.attachChild(fire);
        space.add(body);
//        node.addLight(headLight);
    }
    
    float time;
    public void update(float tpf){
        time += tpf;
        //calculate groundedness
        Vector3f loc = body.getPhysicsLocation();
        float desiredHeight = ground.heightMap.value(loc.x, loc.z) + height * .5f;
        Vector3f velocity = body.getLinearVelocity();
        
        //test
//        fire.setLocalTranslation(loc);
        
        //Keep player above the ground.
        if(loc.y < desiredHeight){
            loc.y = desiredHeight;
            body.setPhysicsLocation(new Vector3f(loc.x, desiredHeight, loc.z));
            velocity.y *= .5f;
        }
        onGround = loc.y - desiredHeight < .2f;
        inWater = loc.y < world.water.getWaterHeight();
        
//        System.out.printf("Water %s, height %f, water %f\n", inWater, loc.y, world.water.getWaterHeight());
        
        float drag = FastMath.pow(onGround ? (isRunning ? .4f : .125f) : .9f, tpf);
        //System.out.println(drag);
        velocity.multLocal(drag);
        body.setLinearVelocity(velocity);
       // onGround = false;
        //test for groundedness
        
//        Vector3f l0 = body.getPhysicsLocation();
//        Vector3f l1 = l0.add(body.getLinearVelocity().mult(1.1f));
//        l0.z -= height * .1f;
//        
//         CollisionResults results = new CollisionResults();
//        // 2. Aim the ray from cam loc to cam direction.
//        Ray ray = new Ray(l0, l1);
//        // 3. Collect intersections between Ray and Shootables in results list.
//        ground.terrain.collideWith(ray, results);
        // 4. Print results.
        //System.out.println("----- Collisions? " + results.size() + "-----");
//        for (int i = 0; i < results.size(); i++) {
//          // For each hit, we know distance, impact point, name of geometry.
//          float dist = results.getCollision(i).getDistance();
//          Vector3f pt = results.getCollision(i).getContactPoint();
//          String hit = results.getCollision(i).getGeometry().getName();
//          System.out.println("* Collision #" + i);
//          System.out.println("  You shot " + hit + " at " + pt + ", " + dist + " meters away.");
//        }
        // 5. Use the results (we mark the hit object)
//        if (results.size() > 0){
//          // The closest collision point is what was truly hit:
//          CollisionResult closest = results.getClosestCollision();
//          System.out.println("Hit " + results.getClosestCollision().getGeometry().getName());
//
//          Vector3f loc = body.getPhysicsLocation();
//          body.setPhysicsLocation(ground.getCoordinate(loc.x, loc.z));
//          Vector3f vel = body.getLinearVelocity();
//          vel.z = 0;
//          body.setAngularVelocity(vel);
//          Vector3f normal = closest.getContactNormal();
//          Quaternion rot = (new Quaternion().fromAngleAxis(FastMath.PI/2, new Vector3f(1,0,0)));
//          Matrix3f r = rot.toRotationMatrix();
//          r.multLocal(normal);
//                  
//          Vector3f fR = normal.mult(-100 / 60 * mass * normal.dot(body.getLinearVelocity()));
//          System.out.println("Contact Normal = " + normal);
//          System.out.println("Repellent Force = " + fR);
//          body.applyImpulse(fR, Vector3f.ZERO);//hopefully that will work...
//          fire.setLocalTranslation(closest.getContactPoint());
//        }
        
//        List<PhysicsRayTestResult> res = space.rayTest(l0, l1);
//        
//        for (PhysicsRayTestResult r : res){
//            //r.getCollisionObject().get
//            //System.out.println(r.getCollisionObject().getUserObject());
//            onGround = true;
//        }
        
        //MODE selection
        
        if (keys[KeyInput.KEY_LSHIFT]){
            keys[KeyInput.KEY_LSHIFT] = false;
            if(mode == MODE_AIM){
                mode = MODE_RUN;
            }
            else{
                mode = MODE_AIM;
            }
        }
        
        if (keys[KeyInput.KEY_TAB]){
            keys[KeyInput.KEY_TAB] = false;
            if(mode == MODE_OVERHEAD){
                mode = MODE_CINEMATIC;
            }
            else if(mode == MODE_CINEMATIC){
                mode = MODE_RUN;
            }
            else mode = MODE_OVERHEAD;
        }
        
        if(mode == MODE_AIM){
            if (keys[KeyInput.KEY_LEFT]){
                heading -= tpf * AIM_ROT_SPEED;
            }
            if (keys[KeyInput.KEY_RIGHT]){
                heading += tpf * AIM_ROT_SPEED;
            }
            
            if (keys[KeyInput.KEY_UP]){
                phi += tpf * AIM_ROT_SPEED;
                if(phi > FastMath.HALF_PI){
                    phi = FastMath.HALF_PI;
                }
            }
            if (keys[KeyInput.KEY_DOWN]){
                phi -= tpf * AIM_ROT_SPEED;
                if(phi < -FastMath.HALF_PI){
                    phi = -FastMath.HALF_PI;
                }
            }
            
            //Zoom
            if (keys[KeyInput.KEY_EQUALS]){
                zoom += .1f * tpf;
                if(zoom > 16) zoom = 16;
            }
            if (keys[KeyInput.KEY_MINUS]){
                zoom -= .1f * tpf;
                if(zoom < .5f) zoom = .5f;
            }
            
            if (keys[KeyInput.KEY_A]){
                keys[KeyInput.KEY_A] = false;
                Firework firework = new Firework(this, new ElementalVector(0, 2), getLocalTranslation().clone(), facing.scaleAdd(25, body.getLinearVelocity()).addLocal(0, height, 0), new ColorRGBA(1, .2f, .9f, 1), 4, 6);
                firework.addToWorld(world, rootNode);
            }
            if (keys[KeyInput.KEY_E]){
                keys[KeyInput.KEY_E] = false;
                if(dynStatCur[SOUL] > 1){
                    Vector3f dir = facing.scaleAdd(20, body.getLinearVelocity());
//                    Vector3f dir = MobiusMath.ballisticTarget(body.getPhysicsLocation(), focus, 20, world.GRAV.z);
                    if(dir != null){
                        System.out.println("Firing Physics from " + body.getPhysicsLocation());
                        effectStat(SOUL, -1);
                        Vector3f initialLoc = dir.mult(height / dir.length()).addLocal(body.getPhysicsLocation());
                        initialLoc.y += height;
                        PhysicsProjectile proj = new PhysicsProjectile(this, new ElementalVector(0, 1), initialLoc, dir, .25f, 6);
                        proj.addToWorld(world, rootNode);
                    }
                }
            }
            if(keys[KeyInput.KEY_U]){
//                keys[KeyInput.KEY_U] = false;
                //TEMP STUFF
                
                if(adat == null){ //not drawn yet
                    if(quiver.notEmpty()){
                        adat = quiver.popArrow();
                        updateHUD();
                    }
                }
                
                else{ //bow drawn
                    float drawAmt = 2f * statStatCur[STRENGTH] * tpf * bow.powerEfficiency * getStatFraction(ENERGY);
                    bowPwr += drawAmt;
                    effectStat(ENERGY, -drawAmt);
                    if(bowPwr > bow.maxPower) bowPwr = bow.maxPower;
                    updateHUD();
                }
            }
            else if(adat != null){
                System.out.println("FIRING FROM: " + body.getPhysicsLocation());
                Arrow a = new Arrow(this, adat);
                a.addToWorld(world, rootNode);

                Vector3f dir = facing.normalize();
                a.fireArrow(world, loc, dir, body.getLinearVelocity(), bow, 10 * FastMath.sqrt(bowPwr));

                adat = null;
                bowPwr = 0;
                
                updateHUD();
            }
        }

        
        if(mode == MODE_RUN){
            //Vector3f vel =  body.getLinearVelocity();
            //runTimer += tpf * vel.length();
            runSpeed = .2f * getStatFraction(ENERGY);

            //heartbeat
            pulseTime += getPulse() * tpf;

            effectStat(ENERGY, .499f * tpf);
            if (keys[KeyInput.KEY_LCONTROL]){
                runSpeed *= 2;

                effectStat(ENERGY, -.5f * tpf);
            }
            if (keys[KeyInput.KEY_UP]){
                effectStat(ENERGY, -.5f * tpf);
                Vector3f vel = body.getLinearVelocity();
                //so the way this works is dy/dx = a/y, a being a constant, y being speed, and x being time.  
                //nvm.. The dot product between the desired direction and the actual direction is taken.  This is the negative exponent of the force.  
                float xFactor = FastMath.cos(heading);
                float zFactor = FastMath.sin(heading);
                float ss = 1 / (1 + runSpeed * 64); //so this variable takes runspeed into account (a variable based on tiredness and speed) and scales how much current speed is a factor accordingly.
                float force = footforceconstant * FastMath.pow((float)Math.E, (-xFactor * vel.x - zFactor * vel.z) * ss);
                if (force > footforceconstant * 2) force = footforceconstant * 2; //cap it.  
                //System.out.println(force);
                //force = 100;
                body.applyCentralForce(new Vector3f(force * xFactor, 0, force * zFactor));
                //body.applyCentralForce(new Vector3f(runSpeed * FastMath.cos(heading), runSpeed * FastMath.sin(heading), 0));
                runTimer += (5f + FastMath.sqrt(runSpeed) * 10) * tpf;
                isRunning = true;
                
                effectStat(ENERGY, -runSpeed * .25f * tpf);
            }   
            else{
                //body.setWalkDirection(Vector3f.ZERO);
                runTimer = runSpeed = 0;
                isRunning = false;
            }

            turnSpeed = 0;
            if (keys[KeyInput.KEY_LEFT]){
                turnSpeed = -Math.min(FastMath.TWO_PI, 1 / runSpeed);
                heading += tpf * turnSpeed;
            }
            if (keys[KeyInput.KEY_RIGHT]){
                turnSpeed = + Math.min(FastMath.TWO_PI, 1 / runSpeed);
                heading += tpf * turnSpeed;
            }
            if (keys[KeyInput.KEY_SPACE]){
                if(onGround){
                    if(dynStatCur[ENERGY] > 2f){//if(getStatFraction(ENERGY) > 2f){
                        System.out.println("JUMPING");
                        effectStat(ENERGY, -2f);
                        body.applyImpulse(new Vector3f(0, 200, 0), Vector3f.ZERO);
        //                onGround = false;
                    }
                }
                else if(inWater){
                    effectStat(ENERGY, -.5f * tpf);
                    body.applyCentralForce(new Vector3f(0, 10 * mass, 0));
                }
                else{
                    System.out.println("No jumping in midair allowed.");
                }
            }
            
            float restoreFactor = getStatFraction(WATER) * .5f;
            //Deal with water
            if(inWater){
                //bouyant force
                body.applyCentralForce(new Vector3f(0, 5 * mass, 0));
                //Breath
                effectStat(ENERGY, -1 * tpf );
                effectStat(WATER, 2 * tpf);
                //less stat restoration under water
                restoreFactor *= .5f;
            }
            
            //water heals dynamic stats
            for(int i = 0; i < WATER; i++){
                float amt = (dynStatMax[i] - dynStatCur[i]) * restoreFactor * recoverySpeeds[i] * tpf;//FastMath.pow((dynStatMax[i] - dynStatCur[i]) * factor * recoverySpeeds[i], tpf);
                if(amt > 0){
                    effectStat(i, amt);
                    effectStat(WATER, -amt * waterConversionFactors[i]);
                }
            }
    //        if (keys[KeyInput.KEY_O]){
    //            keys[KeyInput.KEY_O] = false;
    //            if(dynStatCur[SOUL] > 1){
    //                effectStat(SOUL, -1);
    //                Vector3f dir = facing.scaleAdd(5, body.getLinearVelocity()).addLocal(0, 0, 5);
    //                PhysicsProjectile proj = new PhysicsProjectile(getLocalTranslation().add(dir), dir, 4, 6, element, getMagicAttack());
    //                proj.addToWorld(world, rootNode);
    //            }
    //        }
    //        
            if(keys[KeyInput.KEY_EQUALS]){
                sightFocus += tpf * 10;
                if(sightFocus > 100) sightFocus = 100;
            }
            else if (keys[KeyInput.KEY_MINUS]){
                sightFocus -= tpf * 10;
                if(sightFocus < 1) sightFocus = 1;
            }
            
            if(keys[KeyInput.KEY_A] && sword == null){
                System.out.println("Swording");
                sword = new Sword(swordData, this);
                
                sword.addToWorld(world, rootNode);
                world.addProjectile(sword);
            }
            
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
                    //sloc.y += height * .5f;
                    
                    sword.setLocalTranslation(sloc);
                    
                    sword.updateFacing(facing);
                }
            }
        }
        
        //Restore stats
        statReturnToNorm(tpf);
    }
    Vector3f facing;
    Vector3f focus = new Vector3f();  //where the camera is focused.
    public void doCameraStuff(Camera cam){
        hud.updateHUD(this);
        if(mode == MODE_RUN){
            float sinRunTimer = FastMath.sin(runTimer);

            Vector3f pos = body.getPhysicsLocation().clone();
            float footY = pos.y;
            pos.y += height * .5f + (onGround ? .15f * height * sinRunTimer : 0) + .0625f * getPulseValue();
            cam.setLocation(pos);

            float cosHead = FastMath.cos(heading);
            float sinHead = FastMath.sin(heading);

            float x = pos.x + sightFocus * cosHead;
            float z = pos.z + sightFocus * sinHead;

            float y = sightFocus * .05f;

            float gh = ground.heightMap.value(x, z);
            if(gh < footY) gh = (footY + gh) * .5f;
            y += gh;


            phi = (turnSpeed * .1f * runSpeed + sinRunTimer * .1f) * runSpeed * (.1f + .1f * getPulseValue());

            Vector3f loc = new Vector3f(x, y, z);

            float cosPhi = FastMath.cos(phi);
            float sinPhi = FastMath.sin(phi);

            //x and y coordinates seem strange because what we really want is pi/2 radians off of the heading.
            cam.lookAt(loc, new Vector3f(-sinHead * sinPhi, cosPhi, cosHead * sinPhi));
            focus.set(x, gh, z);
            //fire.setLocalTranslation(new Vector3f(x, y, gh));
            headLight.setPosition(pos);
            headLight.setDirection(loc.subtractLocal(pos).normalizeLocal());
            facing = loc;
        }
        else if (mode == MODE_AIM){
            float sinRunTimer = FastMath.sin(runTimer);

            Vector3f pos = body.getPhysicsLocation().clone();
            float footY = pos.y;
            pos.y += height * .5f + (onGround ? .15f * height * sinRunTimer : 0) + .0625f * getPulseValue();
            cam.setLocation(pos);

            float cosHead = FastMath.cos(heading);
            float sinHead = FastMath.sin(heading);

            float cosPhi = FastMath.cos(phi);
            float sinPhi = FastMath.sin(phi);

            float x = pos.x + cosHead * cosPhi;
            float z = pos.z + sinHead * cosPhi;

            float y = pos.y + sinPhi;
            Vector3f target = new Vector3f(x, y, z);

            //x and y coordinates seem strange because what we really want is pi/2 radians off of the heading.
            cam.lookAt(target, Vector3f.UNIT_Y);
            //fire.setLocalTranslation(new Vector3f(x, y, gh));
            headLight.setPosition(pos);
            headLight.setDirection(target.subtractLocal(pos).normalizeLocal()); //TODO no need to normalize
            facing = target;
        }
        else if (mode == MODE_OVERHEAD){
            Vector3f playerPos = body.getPhysicsLocation();
            Vector3f cameraPos = body.getPhysicsLocation().clone();
            cameraPos.y += 128 + 128;
            cam.setLocation(cameraPos);
            cam.lookAt(playerPos, Vector3f.UNIT_Y);
        }
        else if (mode == MODE_CINEMATIC){
            Vector3f playerPos = body.getPhysicsLocation();
            Vector3f cameraPos = body.getPhysicsLocation().clone();
            cameraPos.y += 64;
            cameraPos.x += FastMath.sin(time * .1f) * 16;
            cameraPos.z += FastMath.cos(time * .1f) * 16;
            cam.setLocation(cameraPos);
            cam.lookAt(playerPos, Vector3f.UNIT_Y);
        }
        
        
        //ZOOM
        //Code from flybycam
//        
//        // derive fovY value
//        float h = cam.getFrustumTop();
//        float w = cam.getFrustumRight();
//        float aspect = w / h;
//
//        float near = cam.getFrustumNear();
//
//        float fovY = FastMath.atan(h / near)
//                  / (FastMath.DEG_TO_RAD * .5f);
//        fovY += value * 0.1f;
//
//        h = FastMath.tan( fovY * FastMath.DEG_TO_RAD * .5f) * near;
//        w = h * aspect;
//
//        cam.setFrustumTop(h);
//        cam.setFrustumBottom(-h);
//        cam.setFrustumLeft(-w);
//        cam.setFrustumRight(w);
        
        
        // derive fovY value
        float h = cam.getFrustumTop();
        float w = cam.getFrustumRight();
        float aspect = w / h;

        float fovY = FastMath.atan(h)
                  / (FastMath.DEG_TO_RAD * .5f);
        fovY *= zoom;

        h = 1f / zoom;
//        h = FastMath.tan( fovY * FastMath.DEG_TO_RAD * .5f);
        w = h * aspect;

        cam.setFrustumTop(h);
        cam.setFrustumBottom(-h);
        cam.setFrustumLeft(-w);
        cam.setFrustumRight(w);

    }
    public void initializeStats(){
        dynStatStat = new float[DYN_STATS];
        dynStatMax = new float[DYN_STATS];
        dynStatCur = new float[DYN_STATS];
        for(int i = 0; i < DYN_STATS; i++){
            dynStatCur[i] = dynStatMax[i] = dynStatStat[i] = 16;
        }
        
        statStatStat = new float[STAT_STATS];
        statStatMax = new float[STAT_STATS];
        statStatCur = new float[STAT_STATS];
        for(int i = 0; i < STAT_STATS; i++){
            statStatCur[i] = statStatMax[i] = statStatStat[i] = 1;
        }
    }
    //Used to calculate stats after equipping stuff
    public void recalculateStats(){
        for(int i = 0; i < STAT_STATS; i++){
            statStatMax[i] = statStatStat[i];
        }
        for(Equipment e : equipment){
            for(int i = 0; i < STAT_STATS; i++){
                statStatMax[i] += e.statStatModifiers[i];
            }
            for(int i = 0; i < DYN_STATS; i++){
                statStatMax[i] += e.statStatModifiers[i];
            }
        }
    }
    public boolean effectStat(int stat, float change){
        dynStatCur[stat] += change;
        if(dynStatCur[stat] > dynStatMax[stat]) dynStatCur[stat] = dynStatMax[stat];
        //TODO render stat bars
//        stats[stat].setWidth(maxBarLength * getStatFraction(stat));
        if(dynStatCur[stat] <= 0){
            dynStatCur[stat] = 0;
            //stuff should probably happen here
            System.out.println("You are dead.");
            return true;
        }
//        if (hudAlpha < .5f) hudAlpha = .5f; //TOD hud alpha modification
        return false;
    }
    public float getStatFraction(int stat){
        return dynStatCur[stat] / dynStatMax[stat]; //should be replaced with multiplication.  
    }
    public void statReturnToNorm(float time){
        float statRestoreHalfLife = 2;
//        float statNormalThreshold = .125f;
        
        float curFrac = FastMath.pow(.5f, time / statRestoreHalfLife);
        float maxFrac = 1f - curFrac;
        for(int i = 0; i < STAT_STATS; i++){
            statStatCur[i] = statStatCur[i] * curFrac + statStatMax[i] * maxFrac;
        }
        for(int i = 0; i < DYN_STATS; i++){
            dynStatCur[i] = dynStatCur[i] * curFrac + dynStatMax[i] * maxFrac;
        }
        hud.testOut.setText(/*"(t" + time + " f" + curFrac + ")" + */"Defence: " + statStatCur[DEFENCE] + " / " + statStatMax[DEFENCE] + " / " + statStatStat[DEFENCE]);
    }

    @Override
    public void removeFromWorld() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public Vector3f getLocation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setLocation(Vector3f loc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void addCurrency(int value){
        currency += value;
        updateHUD();
    }
    public boolean addArrow(ArrowData a){
        boolean res = quiver.addArrow(a);
        updateHUD();
        return res;
    }
    public void updateHUD(){
        String text = ">--> " + quiver.arrowCount() + " ( ) " + currency;
        if(adat != null){
           text += "Arrow: " + adat.headMat.name + ", " + adat.shaftMat.name + ", Power: " + bowPwr + " / " + bow.maxPower;
        }
        hud.textOut.setText(text);
    }
}