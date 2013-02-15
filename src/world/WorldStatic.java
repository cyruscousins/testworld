/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package world;

import art.Texturer;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.util.TangentBinormalGenerator;
import systems.MaterialData;

/**
 *
 * @author ccousins
 */
public class WorldStatic extends Geometry{
    public float radius = 4, distHalf = 32;
    public MaterialData matdat;
    
    public RigidBodyControl body;
    public World world;
    
    public WorldStatic(String name, Mesh mesh, Material material, RigidBodyControl body, MaterialData matDat){
        super(name);
        setMesh(mesh);
        setMaterial(material);
        
        this.body = body;
        
        addControl(body);
        
        this.matdat = matDat;
    }
    
    public void addToWorld(World world, Node parent){
        this.world = world;
        
        world.space.add(body);
        
        parent.attachChild(this);
        
        
        //DEBUG ONLY
        
//        setMaterial(Texturer.wireframe());
//        Geometry debug = new Geometry(
//                "Debug Hexile",
//                TangentBinormalGenerator.genTbnLines(mesh, .5f)
//        );
//        Material debugMat = art.Texturer.assetManager.loadMaterial("Common/Materials/VertexColor.j3m");
//        debug.setMaterial(debugMat);
//        debug.setCullHint(Spatial.CullHint.Never);
//        debug.getLocalTranslation().set(getLocalTranslation());
//        parent.attachChild(debug);
//        debug.addControl(body);
    }
    
    public void removeFromWorld(){
        if(parent != null){
            world.space.remove(this);
            parent.detachChild(this);
        }
    }
    
    public void update(float tpf, Vector3f cam){
        updateLOD(cam);
    }
    
    public void updateLOD(Vector3f cam){
        int maxLOD = getMesh().getNumLodLevels() - 1;
        float dist = cam.distance(getLocalTranslation()) - radius;
        if(dist < 0){
            setLodLevel(maxLOD);
        }
        else{
            int curlod = getLodLevel();
            int lod = (int) (1f + FastMath.log(dist / distHalf, 2)); //can be negative, add 1 to normalize so that at distHalf it equals 1.
            if(lod < 0) lod = 0;
            if(lod > maxLOD) lod = maxLOD;
            setLodLevel(lod);
            if(curlod != lod) System.out.println(name + " lod: " + curlod + " -> " + lod);
        }
    }
}
