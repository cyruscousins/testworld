/*
 * Manages lighting and skybox
 */
package world;
import art.Texturer;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import java.util.LinkedList;
import java.util.List;
import world.cloud.CloudManager;

/**
 *
 * @author Cyrus Cousins
 * An environment is responsible for skybox, general lighting, and time
 */

public class Environment {
    public Spatial skybox;
    public List<Light> lights;
    int type;
    Node node;
    
    public static final int NIGHT = 0, DAY = 1, AURORA = 2;
    public Environment(int type){
        lights = new LinkedList<Light>();
        this.type = type;
        switch (type){
            case AURORA:
            {
                Light ambient = new AmbientLight();
                ambient.setColor(new ColorRGBA(.1f, .15f, .15f, 1f));
                lights.add(ambient);
                
                DirectionalLight moon = new DirectionalLight();
                moon.setColor(new ColorRGBA(.1f, .1f, .1f, 1f));
                moon.setDirection(new Vector3f(0f, -1f, 0f).normalizeLocal());
                lights.add(moon);
                
                
                DirectionalLight red = new DirectionalLight();
                red.setColor(new ColorRGBA(.2f, .05f, .05f, 1f));
                red.setDirection(new Vector3f(1f, -1f, 0f).normalizeLocal());
                lights.add(red);
                
                DirectionalLight green = new DirectionalLight();
                green.setColor(new ColorRGBA(.05f, .2f, .05f, 1f));
                green.setDirection(new Vector3f(1f, -1f, 1f).normalizeLocal());
                lights.add(green);
                
                DirectionalLight blue = new DirectionalLight();
                blue.setColor(new ColorRGBA(.05f, .05f, .2f, 1f));
                blue.setDirection(new Vector3f(1f, -1f, -2f).normalizeLocal());
                lights.add(blue);
                
                skybox = Texturer.createNightSkyBox(1024, true);
            }
            break;
            case NIGHT:
                {
                    Light ambient = new AmbientLight();
                    ambient.setColor(new ColorRGBA(.1f, .15f, .15f, 1f));
                    lights.add(ambient);

                    DirectionalLight moon = new DirectionalLight();
                    moon.setColor(new ColorRGBA(.1f, .15f, .15f, 1f));
                    moon.setDirection(new Vector3f(0f, -1f, 0f).normalizeLocal());
    //                moon.setDirection(new Vector3f(-.1f, -.2f, -.5f).normalizeLocal());
                    lights.add(moon);

                    skybox = (Texturer.createNightSkyBox(1024, false));
                }
            break;
            case DAY:
            {
                AmbientLight ambient = new AmbientLight();
                ambient.setColor(new ColorRGBA(.7f, .65f, .6f, 1f));
                lights.add(ambient);
                
                DirectionalLight sun = new DirectionalLight();
                sun.setColor(new ColorRGBA(.9f, .875f, .85f, 1).mult(.5f));
                sun.setDirection(new Vector3f(0f, -1f, 0f).normalizeLocal());
                lights.add(sun);
                
                skybox = (Texturer.createDaySkyBox(512));
//                PointLight light = new PointLight();
//                light.setColor(ColorRGBA.White.mult(10));
//                light.setPosition(new Vector3f(0, 0, 10));
//                light.setRadius(10f);              
//                lights.add(light);
            }
                break;
        }
    }
    public void addToWorld(Node rootNode){
        this.node = rootNode;
        node.attachChild(skybox);
//        Spatial test = new Geometry("Lighttest", new Sphere(4, 4, 4));
//        test.setMaterial(Modeller.sand(16));
//        test.setLocalTranslation(new Vector3f(0, 0, 10));
//        
//        node.attachChild(test);
        for(Light light: lights){
            node.addLight(light);
        }
        
//        CloudManager clouds = new CloudManager("Clouds", 1000);
//        
//        clouds.setLowLife(60);
//        clouds.setHighLife(60);
//        clouds.setImagesX(15);
//        clouds.setStartSize(15f);
//        clouds.setEndSize(15f);
//        clouds.setStartColor(ColorRGBA.White);
//        clouds.setEndColor(ColorRGBA.White);
//        clouds.setSelectRandomImage(true);
//        
//        clouds.emitAllParticles();
//        
//        
//        Material mat = new Material(Texturer.assetManager, "Common/MatDefs/Misc/Particle.j3md");
//        mat.setBoolean("PointSprite", true);
//        mat.setTexture("Texture", Texturer.assetManager.loadTexture("Effects/Smoke/Smoke.png"));
//        clouds.setMaterial(mat);
//        
//        node.attachChild(clouds);
    }
    void removeFromWorld(){
        node.detachChild(skybox);
        for(Light light: lights){
            node.removeLight(light);
        }
    }
}