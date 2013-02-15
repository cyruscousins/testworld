package core;

import art.FilterManager;
import art.Modeller;
import art.Texturer;
import com.jme3.app.SimpleApplication;
import com.jme3.input.*;
import com.jme3.input.controls.*;
import com.jme3.input.event.*;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import java.util.logging.Level;
import java.util.logging.Logger;
import math.Noise;
import math.SVF2D;
import math.TestSineSVF2D;
import world.Environment;
import world.GroundManager;
import world.Hexile;
import world.World;

public class Main extends SimpleApplication implements ActionListener, RawInputListener {
    
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }
    
    World world;
    
    boolean[] keys = new boolean[256];
    
    
    GroundManager ground;
    Environment env;
    Geometry box;
    
    FilterManager filterManager;

    @Override
    public void simpleInitApp() {
        Logger.getLogger("").setLevel(Level.WARNING);
        
        Modeller.initialize(assetManager, fpsText.getFont(), 0);
//        Modeller.initialize(assetManager, fpsText.getFont(), viewPort.getOutputFrameBuffer().getWidth());
        Texturer.init(assetManager, fpsText.getFont());

        world = new World(rootNode, keys, cam, viewPort, getStateManager(), settings, guiNode, assetManager);
        
        inputManager.addRawInputListener(this);
         	
//        cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.5f, 1000f);
//      cam.setFrustumNear(.25f);
    }
    
    float time;

    @Override
    public void simpleUpdate(float tpf) {
        
        world.update(tpf);
        
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
    
    
        
    //input
    
        /*
     * Key Input Handling
     */
    public void onAction(String name, boolean isPressed, float tpf) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    public void beginInput() {
       // throw new UnsupportedOperationException("Not supported yet.");
    }
    public void endInput() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    public void onJoyAxisEvent(JoyAxisEvent evt) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    public void onJoyButtonEvent(JoyButtonEvent evt) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    public void onMouseMotionEvent(MouseMotionEvent evt) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    public void onMouseButtonEvent(MouseButtonEvent evt) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    public void onKeyEvent(KeyInputEvent evt) {
        //throw new UnsupportedOperationException("Not supported yet.");
        keys[evt.getKeyCode()] = evt.isPressed();
    }
    public void onTouchEvent(TouchEvent evt) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
}
