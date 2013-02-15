/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package art;

import com.jme3.post.SceneProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.FrameBuffer.RenderBuffer;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;

/**
 *
 * @author ccousins
 */
public class BrightnessCheckProcessor implements SceneProcessor {

    @Override
    public void initialize(RenderManager rm, ViewPort vp) {
        
    }

    @Override
    public void reshape(ViewPort vp, int w, int h) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isInitialized() {
        return true;
    }

    @Override
    public void preFrame(float tpf) {
        //Here we check the brightness
    }

    @Override
    public void postQueue(RenderQueue rq) {
        
    }

    @Override
    public void postFrame(FrameBuffer out) {
        int count = out.getNumColorBuffers();
        
        RenderBuffer r = out.getColorBuffer(0);
        Texture2D t = (Texture2D) r.getTexture();
        Image i = t.getImage();
    }

    @Override
    public void cleanup() {
        
    }
    
}
