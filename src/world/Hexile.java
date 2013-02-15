/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package world;

import art.Texturer;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.control.LodControl;
import com.jme3.util.BufferUtils;
import com.jme3.util.TangentBinormalGenerator;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import math.SVF2D;

/**
 *
 * @author ccousins
 */
public class Hexile extends Geometry{
    public Vector3f center;
    float detail;
    int a, b; //coordinates in hex space
    int highestLod; //the highest lod this model has available.
    
    RigidBodyControl body;
    
    //TODO would be better to use more buffers and fewer dynamic arrays
    public Hexile(Vector3f center, int a, int b, SVF2D heightFunction, float hScale, int maxLayers){
        this.center = center;
        this.a = a;
        this.b = b;
        
        //Grab horizontal information about hexiles
        world.HexileStaticInfo gen = world.HexileStaticInfo.getInfo(maxLayers);
        
        //Calculate the first two detail levels
        float[] pts   = new float[gen.modelPtsCt[maxLayers - 1] * 3]; //TODO reuse these buffers
        float[] norms = new float[gen.modelPtsCt[maxLayers - 1] * 3]; 
        calcPts(center, heightFunction, hScale, pts, gen.pts, norms, 0, gen.modelPtsCt[1]);
        
        //Now then assess the detail constant, decide how many layers of detail we need, and calculate the rest of the detail levels.
        calcDetail(norms);
        highestLod = (int) (detail * maxLayers);
        if(highestLod < maxLayers / 2) highestLod = (highestLod + maxLayers) / 2; //not detailed enough.
        else if (highestLod >= maxLayers) highestLod = maxLayers - 1; //too detailed (layers is the max)
        
        calcPts(center, heightFunction, hScale, pts, gen.pts, norms, gen.modelPtsCt[1], gen.modelPtsCt[maxLayers - 1]);
        
        float texScale = 1f / hScale; //One square of a texture just envelops the hex
        float[] texCoords = new float[gen.modelPtsCt[highestLod] * 3];
        for(int i = 0, l = gen.modelPtsCt[highestLod]; i < l; i++){
            texCoords[i * 2 + 0] = pts[i * 3 + 0] * texScale;
            texCoords[i * 2 + 1] = pts[i * 3 + 2] * texScale;
        }
        
        int[][] tris = new int[highestLod + 1][];
        for(int i = 0; i <= highestLod; i++){
            tris[i] = new int[gen.modelTrisCt[i] * 3];
            System.arraycopy(gen.tris[i], 0, tris[i], 0, gen.modelTrisCt[i] * 3);
        }
        
        VertexBuffer[] lods = new VertexBuffer[highestLod + 1];
        for(int i = 0; i <= highestLod; i++){
            lods[i] = new VertexBuffer(VertexBuffer.Type.Index);
            lods[i].setupData(VertexBuffer.Usage.Static, 1, VertexBuffer.Format.UnsignedInt, BufferUtils.createIntBuffer(tris[i]));
        }
        
        mesh = new Mesh();
        
        FloatBuffer position = BufferUtils.createFloatBuffer(gen.modelPtsCt[highestLod] * 3);
        position.put(pts, 0, gen.modelPtsCt[highestLod] * 3);
        
        FloatBuffer normal = BufferUtils.createFloatBuffer(gen.modelPtsCt[highestLod] * 3);
        normal.put(norms, 0, gen.modelPtsCt[highestLod] * 3);
        
        mesh.setBuffer(VertexBuffer.Type.Position, 3, position);
        mesh.setBuffer(VertexBuffer.Type.Normal, 3, normal);
        mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoords));
        mesh.setBuffer(VertexBuffer.Type.Index, 1, (IntBuffer)lods[highestLod].getData()); //set the highest so the tangentbinormalgenerator works.
        mesh.updateBound();
        
        mesh.setLodLevels(lods);
        
        //TODO do we need to call this, or are the normals enough?
        TangentBinormalGenerator.generate(mesh, false);
        
        
        //Test code
//        System.out.println("Printing Buffer");
//        IntBuffer b = (IntBuffer)mesh.getIndexBuffer().getBuffer();
//        System.out.println(b + " " + b.capacity() + " " + tris[0].length);
//        for(int i = 0; i < b.capacity(); i += 3){
//            System.out.println(b.get(i + 0) + " - " + b.get(i + 1) + " - " + b.get(i + 2));
//        }
//        
//        System.out.println("Printing array:");
//        
//        for(int i = 0; i < tris[0].length; i += 3){
//            System.out.println(tris[0][(i + 0)] + " - " + tris[0][(i + 1)] + " - " + tris[0][(i + 2)]);
//        }
        
        setMesh(mesh);
        setLodLevel(0);
        
        body = new RigidBodyControl(new MeshCollisionShape(mesh), 0);
        addControl(body);
        
        setName("Hexile (" + a + ", " + b + ")");
    }
    
    PhysicsSpace space;
    
    public void addToWorld(Node node, PhysicsSpace space){
//        this.parent = node;
        node.attachChild(this);
        this.space = space;
        space.add(body);
        
//        //DEBUG ONLY
//        
//        setMaterial(Texturer.wireframe());
//        Geometry debug = new Geometry(
//                "Debug Hexile",
//                TangentBinormalGenerator.genTbnLines(mesh, 0.5f)
//        );
//        Material debugMat = art.Texturer.assetManager.loadMaterial("Common/Materials/VertexColor.j3m");
//        debug.setMaterial(debugMat);
//        debug.setCullHint(Spatial.CullHint.Never);
//        debug.getLocalTranslation().set(getLocalTranslation());
//        node.attachChild(debug);
    }
    
    public void removeFromWorld(){
        parent.detachChild(this);
        space.remove(body);
    }
    
    //calculate the y coordinates and normals of the hexile, starting at pt index start and ending at index finish.
    void calcPts(Vector3f center, SVF2D heightFunction, float hScale, float[] pts, float[] ptsTemplate, float[] norms, int start, int finish){
//        System.out.printf("S %d F %d C %d\n", start * 3, finish * 3, (finish - start) * 3);
//        System.out.printf("T %d P %d\n", ptsTemplate.length, pts.length);
        System.arraycopy(ptsTemplate, start * 3, pts, start * 3, (finish - start) * 3);
        for(int i = start * 3; i < finish * 3; i += 3){
            pts[i + 0] = center.x + ptsTemplate[i + 0] * hScale;//x
            pts[i + 2] = center.z + ptsTemplate[i + 2] * hScale;//z
            
            pts[i + 1] = center.y + heightFunction.value(pts[i + 0], pts[i + 2]);
            //pts[i + 1] = center.y + (pts[i + 0] * pts[i + 0] + pts[i + 2] * pts[i + 2]) * 8 / (hScale * hScale);
            
//            System.out.println(i + " x: " + pts[i] + " y: " + pts[i + 1] + ", z: " + pts[i + 2]);
            //System.out.println("f("+ pts[i + 0] + ", " + pts[i + 2] + ") = " + (pts[i + 1] - center.y));
            
            //Normal = dxdz x dydz
            
            float dydx = heightFunction.dydx(pts[i + 0], pts[i + 2]);
            float dydz = heightFunction.dydz(pts[i + 0], pts[i + 2]);
        
            //zLine = 0, dydz, 1
            //xLine = 1, dydx, 0
            //cp = dydx, -1, dydz
            
            //set normals equal to the normalized cross product
            
            norms[i + 0] =  -dydz;
            norms[i + 1] =  1;
            norms[i + 2] =  -dydx;
            
            
            float s = 1f / FastMath.sqrt(norms[i + 0] * norms[i + 0] + norms[i + 1] * norms[i + 1] + norms[i + 2] * norms[i + 2]);
            
            norms[i + 0] *= s;
            norms[i + 1] *= s;
            norms[i + 2] *= s;
            
//            norms[i + 0] = 0;
//            norms[i + 1] = 1;
//            norms[i + 2] = 0;
        }
    }
    
    void calcDetail(float[] norms){
        float dotProductProduct = 1; //the product of the dot products
        for(int i = 1; i < 12; i++){  //of the center and each vertex of the hex AND center by the center of each edge of the hex
            dotProductProduct *=  norms[3 * i + 0] * norms[0] + norms[3 * i + 1] * norms[1] + norms[3 * i + 2] * norms[2];
            if(dotProductProduct < 0){
                dotProductProduct = 0;
                break;
            }
        }
       //normal
        
        detail = 1 - dotProductProduct;

    }
    //TEST CODE:
    
    public static void main(String[] args){
        //Different normals (varying slope, should have a high detail constant)
        SVF2D detailTest = new SVF2D(){
            public float value(float x, float z) {
                return (x * x + z * z) * .1f;
            }
        };
        //Same normals (constant slope, low detail constant)
        SVF2D simpleTest = new SVF2D(){
            public float value(float x, float z) {
                return x + z;
            }
        };
        //HEXILE DETAIL TEST
        Hexile hex = new Hexile(Vector3f.ZERO, 0, 0, simpleTest, 8, 2);
        System.out.println("Simple hex detail: " + hex.detail);
        hex = new Hexile(Vector3f.ZERO, 0, 0, detailTest, 8, 2);
        System.out.println("Complex hex detail: " + hex.detail);
        
    }
    public void setLodLevel(int lod){
        if(lod > highestLod) super.setLodLevel(highestLod);
        else super.setLodLevel(lod);
    }
}
