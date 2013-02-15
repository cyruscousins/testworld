package world;

import actor.Combatable;
import actor.DumbEnemy;
import actor.EnergyOrb;
import combat.WorldAttack;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import art.Modeller;
import actor.Player;
import actor.Raft;
import actor.WorldDynamic;
import art.FilterManager;
import art.MusicManager;
import art.Texturer;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.terrain.heightmap.HeightMap;
import com.jme3.util.TangentBinormalGenerator;
import java.util.logging.Level;
import java.util.logging.Logger;
import math.SVF2D;
import math.TestSineSVF2D;
import systems.MaterialData;

/**
 * Creates and sometimes manages the ingame world.
 * @author Cyrus Cousins
 */
public class World implements PhysicsCollisionListener{
    Node rootNode;
    
    public Node collidables;
    
    public static Vector3f GRAV = new Vector3f(0, -9.8f, 0);
    BulletAppState bulletAppState;
    public PhysicsSpace space;
    
    boolean[] keys;
    
    public Player player;
    public GroundManager groundManager;
    public FilterManager filterManager;
    public Weather weather;
    public Water water;
    
    public MusicManager music;
    
    int[] menu;
    int[] overworld;
    int[] night;
    int[] funeral;
    int[] battle;
    
    Camera cam;
    
    //maintain lists of projectiles
    List<WorldAttack> projectiles = new LinkedList<WorldAttack>(); //frequent additions and subtractions, small list.
    List<WorldAttack> projTrash = new ArrayList<WorldAttack>(5); //only added to, accessed and cleared.
    List<WorldAttack> projNew = new ArrayList<WorldAttack>(5); //only added to, iterated, and cleared.  

    HashMap<String, Combatable> combatables = new HashMap<String, Combatable>(); //TODO no table necessary.
//    List<Combatable> combatables = new LinkedList<Combatable>();
    List<WorldStatic> statics = new ArrayList<WorldStatic>(256);
    List<WorldDynamic> dynamics = new ArrayList<WorldDynamic>(256);
    
    
    public World(Node rootNode, boolean[] keys, Camera cam, ViewPort viewPort, AppStateManager stateMan, AppSettings settings, Node guiNode, AssetManager assetManager){
        this.rootNode = rootNode;
        this.keys = keys;
        this.cam = cam;
        
        bulletAppState = new BulletAppState();
        stateMan.attach(bulletAppState);

        space = bulletAppState.getPhysicsSpace();
        space.addCollisionListener(this);
        space.setGravity(GRAV);
        
        collidables = new Node();
        
        rootNode.attachChild(collidables);
        
        //Init materialdata objects =what stuff is made of that effects parameters).
        MaterialData.init();
        
        Material[] sand = Texturer.sand(1024);
//        Material[] sand = Texturer.wireframeLods();
//        Material[] sand = new Material[]{Texturer.stone(4096, 16, 16,.125f, false, .25f, .3f)};
//        Material sand = Texturer.brick(512, 8, 16, .1f, true, .1f, .1f);
//        SVF2D heightMap = new TestSineSVF2D(16, 2);
        SVF2D heightMap = new GroundHeightSVF2D();
        
        boolean testing = true;
        if(testing){
            groundManager = new GroundManager(heightMap, 3, 32, 3, 8, 16, collidables, space, sand);
        }
        else{
            groundManager = new GroundManager(heightMap, 6, 48, 4, 8, 16, collidables, space, sand);
        }
        groundManager.initializeWorld(Vector3f.ZERO); //build the first few hexiles ahead of time.
        
        player = new Player(this, settings.getWidth(), settings.getHeight(), guiNode, groundManager, keys, assetManager);
        player.addToWorld(this, rootNode); //todo use collidables
        player.body.setPhysicsLocation(new Vector3f(0, groundManager.heightMap.value(0, 0) + player.height + 50, 0));
        
        //test loop
        for(int i = 0; i < 16; i++){
            
//            EnergyOrb orb = new EnergyOrb(i % 4, this, new Vector3f(FastMath.rand.nextInt(256) - 128, 0, FastMath.rand.nextInt(256) - 128));
//            orb.addToWorld(this, collidables);
            
            Vector3f loc = new Vector3f(FastMath.rand.nextInt(256) - 128, 0, FastMath.rand.nextInt(256) - 128);
            loc.y = groundManager.heightMap.value(loc.x, loc.z) + 4;
            DumbEnemy test = new DumbEnemy(i % 4, this, loc);
            
            test.addToWorld(this, collidables);
            
            loc = new Vector3f(FastMath.rand.nextInt(256) - 128, 0, FastMath.rand.nextInt(256) - 128);
            loc.y = groundManager.heightMap.value(loc.x, loc.z) + 4;
            if(loc.y < 0) loc.y = 0;
            
            Raft raft = new Raft("Raft", 5f * (1 + FastMath.rand.nextFloat()), 1f * (1 + FastMath.rand.nextFloat()), 5f * (1 + FastMath.rand.nextFloat()), MaterialData.getRandomMaterial("Wood"));
            raft.addToWorld(this, collidables);
            raft.setLocation(loc);
            dynamics.add(raft);
            
        }
        
        Material brick = Texturer.brick(1024, 8, 16, .175f, true, .3f, .2f);
        Material stone = Texturer.stone(1024, 4, 4, .125f, false, .25f, .3f);
        for(int i = 0; i < 16; i++){  
            Vector3f blockSize = new Vector3f(4 + FastMath.rand.nextFloat() * 4, 4 + FastMath.rand.nextFloat() * 4, 4 + FastMath.rand.nextFloat() * 4);
            Mesh mesh = new Box(blockSize.x, blockSize.y, blockSize.z);
            TangentBinormalGenerator.generate(mesh, false);
            
            Material mat;
            String name;
            MaterialData md = MaterialData.getRandomMaterial("Stone");
            int type = i & 4;
            switch(type){
                case 0:
                    name = "Brick block";
                    mat = brick;
                    break;
                case 1:
                    name = "Stone block";
                    mat = stone;
                    break;
                default:
                    name = md.name + " block";
                    mat = Texturer.genMatTex(256, md);
                    break;
            }
            
            RigidBodyControl body = new RigidBodyControl(new BoxCollisionShape(blockSize), 0);
            
            WorldStaticDestructible brickCube = new WorldStaticDestructible("Brick Box", mesh, mat, body, MaterialData.getRandomMaterial("Stone"), 10f);
            
            brickCube.addToWorld(this, collidables);
            
            Vector3f loc = new Vector3f(FastMath.rand.nextInt(256) - 128, 0, FastMath.rand.nextInt(256) - 128);
            loc.y = heightMap.value(loc.x, loc.z);
            
            body.setPhysicsLocation(loc); //Should use cube.setLocation
            
//            statics.add(brickCube);
        }
        
        for(int i = 0; i < 8; i++){
            Vector3f loc = new Vector3f(64 * (FastMath.rand.nextFloat() - .5f), 0, 64 * (FastMath.rand.nextFloat() - .5f));
            
            loc.y = heightMap.value(loc.x, loc.z);
            
            WorldStatic cactus = Modeller.getCactus(6, 8);
            
            cactus.addToWorld(this, collidables);
            cactus.body.setPhysicsLocation(loc);
            
            statics.add(cactus);
        }
        
                
        for(int i = 0; i < 8; i++){
            int expanse = 128;
            Vector3f loc = new Vector3f(expanse * (FastMath.rand.nextFloat() - .5f), 0, expanse * (FastMath.rand.nextFloat() - .5f));
            
            loc.y = heightMap.value(loc.x, loc.z) - 4;
            
            WorldStatic spire = Modeller.getSpire(MaterialData.getRandomMaterial("Stone"), 6, 32);
            
            spire.addToWorld(this, collidables);
            spire.body.setPhysicsLocation(loc);
            
            statics.add(spire);
        }
        
        //weather
        
        
        //weather = new Weather(main, rootNode, Weather.WEATHER_SNOW, 1);
        
        
        Environment environment = new Environment(Environment.DAY);
        environment.addToWorld(rootNode);
        
        filterManager = new FilterManager(viewPort, assetManager);
        filterManager.init();
        
        //Water
        //TODO 
        water = new Water(this, 0, rootNode, Texturer.assetManager, new Vector3f(0, 0, -1));
        water.addToWorld(rootNode, filterManager.getFPP());
        
//        water = new Water(this, 0, rootNode, assetManager, new Vector3f(0, -1, -.5f).normalize());
//        filterManager.getFPP().addFilter(water.water);
        
        music = new MusicManager("res/mus/bankinfo.csv", new String[]{"res/mus/bnk/bank1.csv"}, settings);

        menu = music.getUnreducedCharacteristicVector(new String[]{"Operatic",  "Funeral"}, new int[]{4, -8});
        overworld = music.getUnreducedCharacteristicVector(new String[]{"Orchestral", "Operatic", "Warlike", "Funeral"}, new int[]{4, -8, -2, -8});
        night = music.getUnreducedCharacteristicVector(new String[]{"Nocturne", "Piano", "Operatic", "Orchestral", "Downtempo", "Midtempo", "Uptempo", "Sonata", "Funeral"}, new int[]{16, 8, 4, -2, 4, 4, -3, 6, -16});
        funeral = music.getUnreducedCharacteristicVector(new String[]{"Funeral", "Piano", "Minor"}, new int[]{16, 3, 4});
        battle = music.getUnreducedCharacteristicVector("Warlike Orchestral Bitonal Wind Horns Drums Uptempo Piano Funeral".split(" "), new int[]{16, 8, 12, 4, 8, 10, 6, -4, -16});

        
        
        
        //Put this in for music
        /*
        while(music.songCount == 0){
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("WAITING ON MUSIC");
        }
        music.playFitSong(battle);
        */
        
    }
    float time;
    int supdate;
    public void update(float tpf){
        //statics
        statics.get(supdate++ % statics.size()).update(tpf, cam.getLocation());
        
        //projectiles
        projectiles.addAll(projNew);
        projNew.clear();
        for(WorldAttack proj : projectiles){
            proj.update(tpf);
        }
        projectiles.removeAll(projTrash);
        projTrash.clear();

        //Combatables
        for(Combatable comb : combatables.values()){
            comb.update(tpf);
            
        }
        
        for(WorldDynamic dynamic : dynamics){
            dynamic.update(tpf);
        }
        
//        WorldDynamic firework = new Firework(new Vector3f(FastMath.rand.nextFloat() * 128 - 64, FastMath.rand.nextFloat() * 128 - 64, 0), new Vector3f(0, 0, 8), ColorRGBA.randomColor(), 8, 8);
//        firework.addToWorld(rootNode, space);
//        projNew.add(firework);
        
        
        player.update(tpf);
        player.doCameraStuff(cam);

        
        groundManager.update(player.body.getPhysicsLocation(), tpf);
        
//        if(a){
//            cam.setLocation(new Vector3f(0, 128, 0));
//            cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Z);
//        }
//        a = !a;
        
        filterManager.update(cam.getLocation(), tpf);
    }
    
    public static final int GROUP_GROUND = PhysicsCollisionObject.COLLISION_GROUP_01;
    public static final int GROUP_COMBATABLE = PhysicsCollisionObject.COLLISION_GROUP_02;
    public static final int GROUP_ATTACK = PhysicsCollisionObject.COLLISION_GROUP_03;
    public static final int GROUP_COLLECTABLE = PhysicsCollisionObject.COLLISION_GROUP_04;
    
    public void collision(PhysicsCollisionEvent e) {
        //obviously in the future, the various objects you create will contain a method to deal with such collisions that accepts the other object as a parameter.  Perhaps it will even be a sassy sexy interface (Node o, float impulse) type deal... ;)
        
//        try{
//            System.out.println("Collision between " + e.getNodeA().getName() + " & " + e.getNodeB().getName());
//        }
//        catch(Exception ex){
//            System.out.println("NULLERROR: Collision between " + e.getNodeA() + " & " + e.getNodeB());
//            ex.printStackTrace();
//            System.exit(1);
//        }
        
        Spatial a = e.getNodeA();
        Spatial b = e.getNodeB();
        
        if(a instanceof WorldAttack){
            ((WorldAttack)a).collideWithSpatial(b);
//           System.out.println("PROJ COLLISION: proj " + a.getName() + " with " + b.getName());
        }
        
        if(b instanceof WorldAttack){
            ((WorldAttack)b).collideWithSpatial(a);
//           System.out.println("PROJ COLLISION: proj " + b.getName() + " with " + a.getName());
        }
        
        if(a instanceof Combatable && b instanceof Combatable){
            float aAtk = ((Combatable)a).getPhysicalAttack() * .25f;
            float bAtk = ((Combatable)b).getPhysicalAttack() * .25f;
            ((Combatable)a).takeRawDamage(bAtk, (Combatable)a); //TODO elem
            ((Combatable)b).takeRawDamage(aAtk, (Combatable)b);
        }
        
//        if(e.getNodeA() instanceof Node && e.getNodeB() instanceof Node){
//            Node a = (Node) e.getNodeA();
//            Node b = (Node) e.getNodeB();
//            
//            if(a instanceof Projectile){
//                
//            }
//            if(b == player){
//                Node c = a;
//                a = b;
//                b = c;
//            }
//            if (a == player){
////                if (b.getName().equals("Fire")){
////                    player.effectStat(Player.HEALTH, 1 / 60f);
////                }
//            }
//        }
//        else{
//            
//        }
    }
    
    //List handles
    public void addProjectile(WorldAttack proj){
        projNew.add(proj);
    }
    public void removeProjectile(WorldAttack proj){
        projTrash.add(proj);
    }
    
    public void addCombatable(Combatable comb){
        combatables.put(comb.getName(), comb);
    }
}