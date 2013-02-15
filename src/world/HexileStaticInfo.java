/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package world;

import com.jme3.math.FastMath;
import java.util.HashMap;

/**
 *
 * @author ccousins
 */
public class HexileStaticInfo {
    public static final float ONE = 1f;
    public static final float HALF = .5f;
    
    public static final float RAD3 = FastMath.sqrt(3);
    public static final float RAD3OVER2 = RAD3 / 2;
            
    public int dataSize = 128 * 128 * 3 * 2;
    public int layers;
    
    private HexileStaticInfo(int layers){
        this.layers = layers;
        
        pts = new float[dataSize];
        tris = new int[this.layers][dataSize];
        modelPtsCt = new int[this.layers];
        modelTrisCt = new int[this.layers];
    
    }
    
    private static HexileStaticInfo info;
    public static HexileStaticInfo getInfo(int layers){
        if(info == null || info.layers < layers){
            info = new world.HexileStaticInfo(layers);
            info.generate();
        }
        return info;
    }
    
    int curPtCt;
            
    public float[] pts;
    public int[][] tris;
    public int[] modelPtsCt;
    public int[] modelTrisCt;
    
    HashMap<Long, Integer> interpolateMap = new HashMap<Long, Integer>();
    
    public void generate(){
        
        //* non intuintive coordinate system, z+ is in, x+ is left, y+ is up.
        float[] pts0 = new float[]{
            0, 0, 0, 
            ONE, 0, 0,  
            HALF, 0, -RAD3OVER2, 
            -HALF, 0, -RAD3OVER2,
            -ONE, 0, 0,
            -HALF, 0, +RAD3OVER2,
            HALF, 0, +RAD3OVER2
        };
        
        System.arraycopy(pts0, 0, pts, 0, pts0.length);
        
        tris[0] = new int[]{
            0, 1, 2,
            0, 2, 3,
            0, 3, 4,
            0, 4, 5,
            0, 5, 6,
            0, 6, 1
        };
        
        
        modelPtsCt[0] = pts0.length / 3;
        modelTrisCt[0] = tris[0].length / 3;
        
        curPtCt = modelPtsCt[0];
        
        //guarantee that pts[7, 12] are the centers of the hexagons edges
        getInterpolatedPt(1, 6);
        for(int i = 1; i < 6; i++){
            getInterpolatedPt(i, i + 1);
        }
        
        //generate subsequent layers
        for(int layer = 1; layer < layers; layer++){
            int layerLessOne = layer - 1;
            int curTriCt = 0;
            for(int j = 0; j < modelTrisCt[layerLessOne]; j++){
                //ABC is the original tri, we want to find DEF, then create 
                //triangles ADF DBE FDE and FEC (all maintaining same angelar sign).
                /*
                 *     A
                 *      
                 *   D   F
                 * 
                 * B   E   C
                 * 
                 */
                
                int a = tris[layerLessOne][j * 3 + 0];
                int b = tris[layerLessOne][j * 3 + 1];
                int c = tris[layerLessOne][j * 3 + 2];
                
                int d = getInterpolatedPt(a, b);
                int e = getInterpolatedPt(b, c);
                int f = getInterpolatedPt(c, a);
                
                tris[layer][curTriCt * 3 + 0] = a;
                tris[layer][curTriCt * 3 + 1] = d;
                tris[layer][curTriCt * 3 + 2] = f;
                
                curTriCt++;
                
                tris[layer][curTriCt * 3 + 0] = d;
                tris[layer][curTriCt * 3 + 1] = b;
                tris[layer][curTriCt * 3 + 2] = e;
                
                curTriCt++;
                
                tris[layer][curTriCt * 3 + 0] = f;
                tris[layer][curTriCt * 3 + 1] = e;
                tris[layer][curTriCt * 3 + 2] = c;
                
                curTriCt++;
                
                tris[layer][curTriCt * 3 + 0] = f;
                tris[layer][curTriCt * 3 + 1] = d;
                tris[layer][curTriCt * 3 + 2] = e;
                
                curTriCt++;
            }
            modelPtsCt[layer] = curPtCt;
            modelTrisCt[layer] = curTriCt;
//            System.out.println("l" + layer + ": pts = " + curPtCt + ", tris = " + curTriCt);
        }
    }
    
    //For each unique pair of points shared by some triangle in the model, produce a unique interpolated pt.
    //IE, pts a and b are shared by triangles 1, 2, ..., n
    //getInterpolatedPt(index of a, index of b) will always return the index of c such that c = (a + b) / 2, AND the index will always be the same.
    public int getInterpolatedPt(int a, int b){
        int smaller = Math.min(a, b);
        int larger = Math.max(a, b);

        Long key = Long.valueOf((((long)smaller) << 32) | larger);
        Integer index = interpolateMap.get(key);
        if(index == null){
            //This point has not yet been encountered.

            //Interpolate the two points
            pts[curPtCt * 3 + 0] = (pts[smaller * 3 + 0] + pts[larger * 3 + 0]) * .5f;
            //pts[curPtCt * 3 + 1] = (pts[smaller * 3 + 1] + pts[smaller * 3 + 1]) * .5f; //no need to do y
            pts[curPtCt * 3 + 2] = (pts[smaller * 3 + 2] + pts[larger * 3 + 2]) * .5f;

//            if(pts[curPtCt * 3 + 0] == 0 && pts[curPtCt * 3 + 2] == 0){
//                System.err.println("ERROR AT PT: " + curPtCt);
//            }
            
            //And add the new pt to the table
            index = new Integer(curPtCt);
            interpolateMap.put(key, index);

            return curPtCt++;
        }
        else {
            return index;
        }
    }
}
