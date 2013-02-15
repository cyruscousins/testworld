/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package art;

import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.ColorOverlayFilter;
import com.jme3.post.filters.DepthOfFieldFilter;
import com.jme3.post.filters.GammaCorrectionFilter;
import com.jme3.post.filters.LightScatteringFilter;
import com.jme3.post.filters.RadialBlurFilter;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.renderer.ViewPort;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author ccousins
 */
public class FilterManager {
    FilterPostProcessor fpp;
    ViewPort view;
    
    BloomFilter bloom;
    SSAOFilter ssao;
    DepthOfFieldFilter dof;
    
    ColorRGBA col;
    ColorOverlayFilter colorFilter;
    
    GammaCorrectionFilter gamma;
    LightScatteringFilter scatter;
    
            
//    List<FilterPostProcessor> filters; //TODO use a ptr hash table
    
    boolean bloomOn;
    boolean ssaoOn;
    boolean dofOn;
    boolean colorFilterOn;
    
    boolean gammaOn;
    boolean scatterOn;
    
    public FilterManager(ViewPort view, AssetManager assetManager){
        this.view = view;
        fpp = new FilterPostProcessor(assetManager);
//        filters = new LinkedList<FilterPostProcessor>();
    }
    
    public void init(){
        view.addProcessor(fpp);
        
        //Todo: Configure filters
        bloom = new BloomFilter();
        bloom.setDownSamplingFactor(2);
        bloom.setBlurScale(3f);
        bloom.setExposurePower(3.30f);
        bloom.setExposureCutOff(0.2f);
        bloom.setBloomIntensity(2.45f);
        
        ssao = new SSAOFilter(0.92f, 2.2f, 0.46f, 0.2f);
        
        dof = new DepthOfFieldFilter();
        dof.setFocusDistance(0);
        dof.setFocusRange(50);
        dof.setBlurScale(1.4f);
        
        col = new ColorRGBA(0, 0, 0, 0);
        colorFilter = new ColorOverlayFilter(col);
        
//        fpp.addFilter(new RadialBlurFilter());
        
//        gamma = new GammaCorrectionFilter(2.5f);
//        fpp.addFilter(gamma);
        
//        scatter = new LightScatteringFilter();
//        fpp.addFilter(scatter);
        
//        SSAOFilter ssao = new SSAOFilter(12.940201f, 43.9284635f, 0.32999992f, 0.6059958f);
//        SSAOFilter ssao = new SSAOFilter();
//        fpp.addFilter(ssao);
        
    }
    
    public void update(Vector3f cam, float tpf){
        fpp.getFilterTexture();
       // fpp.setNumSamples(4);
        
        //Remove this when ambient light is low
//        if(!ssaoOn){
//            fpp.addFilter(ssao);
//            ssaoOn = true;
//        }
        
        //Remove this when inside / nothing is far away.
        if(!dofOn){
            fpp.addFilter(dof);
            dofOn = true;
        }
        else if (false){ //INSIDE
            fpp.removeFilter(dof);
            dofOn = false;
        }
        
        //TODO code to detect low brightness, turn bloom off
        if(!bloomOn){
            fpp.addFilter(bloom);
            bloomOn = true;
        }
        
        
        
        /*
         * TODO color filter alpha does not behave as expected.
         */
        
//        if(!colorFilterOn){
//            if(true){ //enough brightness
//                fpp.addFilter(colorFilter);
//                col.a = 0;
//                colorFilterOn = true;
//            }
//        }
//        else{
//            if(false){ //enough darkness
//                fpp.removeFilter(colorFilter);
//                colorFilterOn = false;
//            }
//            //adjust color
//            float brightness = .5f;
//            col.set(brightness, brightness, brightness, 1);
//            colorFilter.setColor(col);
//            //TODO base this on actual brightness.
//            
//            //TODO 2 can we use - colors to damp certain colors?
//        }
    }
    
    public FilterPostProcessor getFPP(){
        return fpp;
    }
}
