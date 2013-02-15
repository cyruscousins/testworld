package world;

import math.MappedSVF2D;
import math.SVF2D;

/**
 * A ground map takes a collection of SVFs and compounds them into a highly detailed ground map.
 * Specifically, a collection of modular SVF2Ds determine the extend to which a secondary set of functions is applied for each xz coord pair.
 * @author ccousins
 */
public class GroundHeightSVF2D extends SVF2D{
    public int svfCount;
    
    public SVF2D[] functions;
    public SVF2D[] modulators;
    
    public GroundHeightSVF2D(){
        svfCount = 6;
        
        functions  = new SVF2D[svfCount];
        modulators = new SVF2D[svfCount];
        
//        functions[0] = math.Noise.noiseSVF(256, 256, 0, 16);
//        modulators[0] = math.Noise.noiseSVF(32, 512, 0, 1);
        for(int i = 0; i < svfCount; i++){
            functions[i] = math.Noise.noiseSVF(128, 128 << i, -(2 << i), 2 << i);
            modulators[i] = math.Noise.noiseSVF(16, 512 << i, 0f, 1);
        }
    }
    
    @Override
    public float value(float x, float z) {
        float amt = 0;
        for(int i = 0; i < svfCount; i++){
            //TODO optimize this
            amt += modulators[i].value(x, z) * functions[i].value(x, z);
        }
        return amt;
    }
}
