package world;

import art.Texturer;
import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.influencers.ParticleInfluencer;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.Filter;
import com.jme3.post.FilterPostProcessor;
import com.jme3.scene.Node;
import com.jme3.texture.Texture2D;

/**
 *
 * @author Cyrus Cousins
 */
public class Water {
    
    World world;
    
    float waterHeight;
    float waveHeight;
    
    float currentWaterHeight;
    float targetWaterHeight;
    
    Node splashNode;
    AssetManager assetManager;
    
    //water     
    WaterFilter water;
    
    //splash
    ParticleEmitter splashEmitter;
    ParticleInfluencer splashInfluencer;
    
    public Water(World world, float initialHeight, Node rootNode, AssetManager assetManager, Vector3f light){
        this.world = world;
        
        this.splashNode = rootNode;
        this.assetManager = assetManager;
        this.currentWaterHeight = targetWaterHeight;
        
        //set up the water post processor
        water = new WaterFilter(rootNode, light);
        
//        Texture2D tex = Texturer.testTex();
//        
//        water.setCausticsTexture(tex);
//        water.setFoamTexture(tex);
//        water.setHeightTexture(tex);
//        water.setNormalTexture(tex);
        
//        water.setHeightTexture(null);
//        water.setNormalTexture(null);
        
        water.setUseFoam(true);
        water.setUseRipples(false);
        water.setDeepWaterColor(ColorRGBA.Brown);
        water.setWaterColor(ColorRGBA.Brown.mult(2.0f));
        water.setWaterTransparency(0.2f);
        water.setMaxAmplitude(0.3f);
        water.setWaveScale(0.08f);
        water.setSpeed(0.7f);
        water.setShoreHardness(1.0f);
        water.setRefractionConstant(0.2f);
        water.setShininess(0.3f);
        water.setSunScale(1.0f);
        water.setColorExtinction(new Vector3f(10.0f, 20.0f, 30.0f));
        
        waterHeight = 0;
        setWaterHeight(waterHeight);
        
        //set up the splash particle emitter
        Material debris_mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        debris_mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/Debris.png"));
        
        splashEmitter = new ParticleEmitter("Splash", ParticleMesh.Type.Triangle, 128);
        splashEmitter.setMaterial(debris_mat);
        splashEmitter.setImagesX(3); splashEmitter.setImagesY(3); // 3x3 texture animation
        splashEmitter.setRotateSpeed(4);
        splashEmitter.setSelectRandomImage(true);
        splashEmitter.setStartColor(new ColorRGBA(1f, 1f, 1f, 1f));
        splashEmitter.setStartSize(.1f); splashEmitter.setEndSize(0);
        splashEmitter.setGravity(world.GRAV);
        
        splashInfluencer = splashEmitter.getParticleInfluencer();
        splashInfluencer.setVelocityVariation(1f);
        
        splashNode.attachChild(splashEmitter);
    }
    
    public void setWaterTargetHeight(float target){
        targetWaterHeight = target;
    }    
    
    public void setWaterHeight(float waterHeight){
        water.setWaterHeight(waterHeight);
    }
    public float getWaterHeight(){
        return water.getWaterHeight();
    }
    
    public void addToWorld(Node node, FilterPostProcessor fpp){
        node.attachChild(splashNode);
        fpp.addFilter(water);
    }
    
    public void update(float tpf){
        if (Math.abs(currentWaterHeight - targetWaterHeight) < .01) return;
        //exponential function
        float dwdt = currentWaterHeight - targetWaterHeight;
        currentWaterHeight += dwdt * tpf;
        if (dwdt > 0 && currentWaterHeight > targetWaterHeight || dwdt < 0 && currentWaterHeight < targetWaterHeight) currentWaterHeight = targetWaterHeight; //prevent an overshot.
        setWaterHeight(waterHeight);
    }
    public void splash(Vector3f location, Vector3f velocity, float radius){
        float magnitude = velocity.length();
        velocity.y = 0;
        location.y = currentWaterHeight;
        splashEmitter.setLocalTranslation(location);
        splashInfluencer.setInitialVelocity(new Vector3f(0, magnitude * .25f, 0).addLocal(velocity));
        splashEmitter.setParticlesPerSec(magnitude * 50);
    }
    
    public Filter getFilter(){
        return water;
    }
}
