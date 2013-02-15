/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package world;

import art.Texturer;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.shader.VarType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import math.SVF2D;

/**
 *
 * @author ccousins
 */
public class GroundManager {
    public static final float RAD3 = FastMath.sqrt(3);
    public static final float RAD3OVER2 = RAD3 * .5f;
    
    //Coordinate System
    /*  __
     * /  \__
     * \__/  \__
     * /  \__/  \
     * \__/  \__/
     * 
     *   / \    \
     *  A |   B _\|
     */
    public static final float AX = 0, AZ = RAD3, BX = 1.5f, BZ = -RAD3OVER2;
    
    public static final int WORLDMAX = 4096;
    
    public SVF2D heightMap;
    
    //TODO materials system
    public Material[] materials;
    
    //A complicated algorithm deals with culling and detail levels.
    //Based on the bumpiness of the hexile and its distance, a detail level is picked.
    //Based on the distance, a tile can be culled if it is too far away.  If it is below the height
    //of the camera, it is less likely to be culled. 
    
    //How far a hex tile must be to possibly be culled
    int hexDrawDistance;
    
    //How far a hexile must be to definitely be culled
    int hexLongRangeDistance;
    
    //How far a hex tile must be to be uncached
    int hexCacheDistance;
    
    Node groundNode;
    PhysicsSpace space;
    
    float hexSize, invHexSize;
    
    int lodMax;
    
    //TODO write a hash class to deal with this without object waste.
    
//    List<Hexile> visibleHexiles = new ArrayList<Hexile>(128);
//    List<Hexile> cachedHexiles = new ArrayList<Hexile>(256);
    
    HashMap<Long, Hexile> visibleHexiles = new HashMap<Long, Hexile>(128);
    HashMap<Long, Hexile> cachedHexiles = new HashMap<Long, Hexile>(256);
    
    List<Long> visibleTrash = new ArrayList<Long>(8);
    List<Long> cacheTrash = new ArrayList<Long>(8);
    
    //Slave threads build hexiles.
    List<GroundSlave> slaves = new LinkedList<GroundSlave>();
    List<GroundSlave> slaveTrash = new ArrayList<GroundSlave>();
    
    static Spatial fire;

    public GroundManager(SVF2D heightMap, int lodMax, float hexSize, int hexDrawDistance, int hexLongRangeDistance, int hexCacheDistance, Node groundNode, PhysicsSpace space, Material[] materials) {
        this.heightMap = heightMap;
        this.lodMax = lodMax;
        this.hexSize = hexSize;
        this.invHexSize = 1f / hexSize;
        
        this.hexDrawDistance = hexDrawDistance;
        this.hexLongRangeDistance = hexLongRangeDistance;
        this.hexCacheDistance = hexCacheDistance;
                
        this.groundNode = groundNode;
        this.space = space;
        
        this.materials = materials;
        
        
        //TEST
        fire = Texturer.getFire();
        groundNode.attachChild(fire);
    }
    
    private void addHexiles(int a0, int b0, int radius, boolean threaded){
        //Center line
        for(int a = -radius; a <= radius; a++){
            addHexile(a0 + a, b0, threaded);
        }
        // h -->
        for(int h = 1; h <= radius; h++){
            int aa = a0 - radius + h;
            int ab = a0 + radius;
            for(int a = aa; a <= ab; a++){
                addHexile(a, b0 + h, threaded);
            }
            aa = a0 - radius;
            ab = a0 + radius - h;
            for(int a = aa; a <= ab; a++){
                addHexile(a, b0 - h, threaded);
            }
        }
    }
    
    //Creates a hexile and adds it to the world.
    private void addHexile(int a, int b, boolean threaded){
        Long hash = hexHash(a, b);
        if(!visibleHexiles.containsKey(hash)){
            Hexile hex = cachedHexiles.get(hash);
            if(hex == null){
                if(threaded){ //make the hexile in a new thread.  Return
                    GroundSlave slave = new GroundSlave(this, a, b);
                    new Thread(slave).start();
                    slaves.add(slave);
                    return;
                }
                //not in cache.  build and cache it.
                hex = new Hexile(new Vector3f((AX * a + BX * b) * hexSize, 0, (AZ * a + BZ * b) * hexSize), a, b, heightMap, hexSize, lodMax);
                cachedHexiles.put(hash, hex);
            //TODO cache cleaning if full
            }
            hex.setMaterial(materials[0]);
            hex.addToWorld(groundNode, space);
            System.out.println("Adding (" + a + ", " + b + ") " + hash.longValue());
            visibleHexiles.put(hash, hex);
        }
        else System.out.println("Skipping (" + a + ", " + b + ") " + hash.longValue());
    }
    
    int lastA = Integer.MIN_VALUE;
    int lastB = Integer.MIN_VALUE;
    
    public void initializeWorld(Vector3f cam){
        int[] newCoords = getNearestHexCoords(cam.x, cam.z);
        addHexiles(newCoords[0], newCoords[1], 1, false);
        lastA = Integer.MIN_VALUE;
        lastB = Integer.MIN_VALUE;
    }
    
    public void update(Vector3f cam, float tpf){
        //Check slaves for completion
        if(!slaves.isEmpty()){
            for(GroundSlave slave : slaves){
                if(slave.finished){
                    Hexile hex = slave.result;
                    
                    hex.setMaterial(materials[0]);
                    hex.addToWorld(groundNode, space);
                    
                    Long hash = hexHash(hex);
                    visibleHexiles.put(hash, hex);
                    cachedHexiles.put(hash, hex);
                    
                    slaveTrash.add(slave);
                }
            }
            slaves.removeAll(slaveTrash);
        }
        else{
            int[] newCoords = getNearestHexCoords(cam.x, cam.z);
    //        if(FastMath.rand.nextFloat() < .1f) System.out.println(cam.x + ", " + cam.z + " -> " + newCoords[0] + ", " + newCoords[1]);
            if(lastA != newCoords[0] || lastB != newCoords[1]){
                lastA = newCoords[0];
                lastB = newCoords[1];
                System.out.println("ADDING");
                addHexiles(lastA, lastB, hexDrawDistance, true); //TODO initworld function
                fire.setLocalTranslation(new Vector3f((AX * lastA + BX * lastB) * hexSize, 2, (AZ * lastA + BZ * lastB) * hexSize));

                for(Hexile hex : visibleHexiles.values()){
                    //TODO
                    int distance = hexDistance(lastA, lastB, hex.a, hex.b);
                    if(distance > hexCacheDistance){
                        hex.removeFromWorld();
                        Long hash = hexHash(hex);
                        visibleTrash.add(hash);
                        cachedHexiles.remove(hash);
                    }
                    else if(distance > hexLongRangeDistance){
                        //if(hex.getCenterHeight() > cam.y) //remove
                        hex.removeFromWorld();
                        Long hash = hexHash(hex);
                        visibleTrash.add(hash);
                    }
                    else if(distance > hexDrawDistance){
                        //TODO
                        //Consider removing the hex
                        hex.removeFromWorld();
                        Long hash = hexHash(hex);
                        visibleTrash.add(hash);
                    }
                    else{
                        int lod = (int)((lodMax - distance) * (1 + hex.detail) / 2f);
                        if(lod >= lodMax) lod = lodMax - 1;
                        if(lod < 0) lod = 0;
                        hex.setLodLevel(lod);

                        int mat = materials.length - distance;
                        if(mat >= materials.length) mat = materials.length - 1;
                        else if(mat < 0) mat = 0;
                        hex.setMaterial(materials[mat]);

    //                    int lod = (int)((lodMax - (FastMath.log(distance, 2))) * hex.detail);
    //                    if(lod >= lodMax) lod = lodMax - 1;
    //                    if(lod < 0) lod = 0;
    //                    hex.setLodLevel(lod);
    //                    if(hex.getMaterial() == material){
    //                        hex.setMaterial(Texturer.sand(16));
    //                    }
    //                    hex.getMaterial().setParam("UseMaterialColors", VarType.Boolean, true);
    //                    hex.getMaterial().setParam("Diffuse", VarType.Vector4, new com.jme3.math.ColorRGBA(lod * .5f - FastMath.floor(lod * .5f), lod * .25f - FastMath.floor(lod * .25f), lod * .125f - FastMath.floor(lod * .125f), 1));
    //                    System.out.printf("Distance %d LOD %d\n", distance, lod);
                    }
                }
                for(Long l : visibleTrash){
                    visibleHexiles.remove(l);
                }
            }
        }
    }
    //A unit vector = (0, RAD3)
    //B unit vector = (1, -RAD3OVER2)
    int[] hexTmp = new int[2];
    //TODO figure out a way to do this return better
    public int[] getNearestHexCoords(float x, float z){
        //You are not expected to understand this code, whatever the fuck it does.
        x *= invHexSize;
        z *= invHexSize;
        
        float b = x / 1.5f;
        float a = z / RAD3 + .5f * b;
        
        int a1 = (int)FastMath.floor(a);
        int a2 = a1 + 1;
        int b1 = (int)FastMath.floor(b);
        int b2 = b1 + 1;
        
        float d, ld;
        int i;
        
        float aTemp;
        float bTemp;
        
        //a low b low
        aTemp = a - a1;
        bTemp = b - b1;
        
        ld = aTemp * aTemp + bTemp * bTemp;
        i = 0;
        
        //a high b low
        
        aTemp = a2 - a;
        bTemp = b - b1;
        
        d = aTemp * aTemp + bTemp * bTemp;
        if(d < ld){
            ld = d;
            i = 1;
        }
        
        //a low b high
        
        aTemp = a - a1;
        bTemp = b2 - b;
        
        d = aTemp * aTemp + bTemp * bTemp;
        if(d < ld){
            ld = d;
            i = 2;
        }
        
        //a high b high
        
        aTemp = a2 - a;
        bTemp = b2 - b;
        
        d = aTemp * aTemp + bTemp * bTemp;
        if(d < ld){
            ld = d; //TODO line not strictly necessary
            i = 3;
            hexTmp[0] = a2;
            hexTmp[1] = b2;
            return hexTmp;
            //TODO just return here
        }
        
        switch(i){
            case 0:
                //a1b1
                hexTmp[0] = a1;
                hexTmp[1] = b1;
                return hexTmp;
            case 1:
                //a2b1
                hexTmp[0] = a2;
                hexTmp[1] = b1;
                return hexTmp;
            case 2:
                //a1b2
                hexTmp[0] = a2;
                hexTmp[1] = b2;
                return hexTmp;
            case 3:
                //a2b2
            default:
                System.out.println("ERROR!!!");
                return hexTmp; //never get here
        }
    }
    /*
    
    public int[] getNearestHexCoords(float x, float z){
        
        //a UP
        //b down right
        //c down left
        
        x *= invHexSize;
        z *= invHexSize;
        
        float a = z / (RAD3);
        float b = 0;
        float c = 0;
        if(x > 0){
            float b = x / 1.5f;
            a += 
        }
        else if(x < 0){
            
        }
        
//        double rx, ry, rz;
//        int ix, iy, iz, s;
//
//        ix = (int)FastMath.floor(rx);
//        iy = (int)FastMath.floor(ry);
//        iz = FastMath.floor(rz);
//        s = ix + iy + iz;
//        if(s) {
//          double abs_dx = fabs(rx-x),
//                 abs_dy = fabs(ry-y), abs_dz = fabs(rz-z);
//
//          if(abs_dx >= abs_dy && abs_dx >= abs_dz) ix -= s;
//          else if(abs_dy >= abs_dx && abs_dy >= abs_dz)
//            iy -= s;
//          else iz-=s;
        }
    }
    */
    public static void main(String[] args){
        for(int i = 0; i < 10; i++){
            int x = (int)(Math.random() * 9 - 4);
            int y = (int)(Math.random() * 9 - 4);
            System.out.println("a " + x + ", b " + y + ": " + hexDistance(0, 0, x, y));
        }
    }
    
    //number of hexes a hex at a1, b1 is from a2, b2
    private static int hexDistance(int a1, int b1, int a2, int b2){
        a1 -= a2;
        b1 -= b2;
        if((a1 <= 0 && b1 >= 0) || (a1 >= 0 && b1 <= 0)){
            return Math.abs(a1 - b1);
        }
        else{ //Same sign
            if(a1 < 0){
                a1 *= -1;
                b1 *= -1;
            }
            return Math.max(a1, b1);
        }
    }
    private Long hexHash(Hexile hex){
        return hexHash(hex.a, hex.b);
    }
    private Long hexHash(int a, int b){
        return Long.valueOf((((long)(a + WORLDMAX)) << 32) | (b + WORLDMAX));
    }
}