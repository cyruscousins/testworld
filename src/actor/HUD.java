/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package actor;

import art.Texturer;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;

/**
 *
 * @author ccousins
 */
public class HUD {
    
    //hud stuff
    int frameWidth;
    int frameHeight;
    float hudRadius;
    
    float hudAlpha;
    Picture bg;
    Picture[] stats;
    Texture2D[][] img;
    
    int imgDivisions = 64;
    int imgDivisionMax = imgDivisions - 1;
    
    Node guiNode;
    
    BitmapText textOut;

    BitmapText testOut;
    
    public HUD(int frameWidth, int frameHeight, float hudRadius, float hudAlpha, Node guiNode) {
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.hudRadius = hudRadius;
        this.hudAlpha = hudAlpha;
        this.guiNode = guiNode;
        
        
        
        stats = new Picture[Combatable.DYN_STATS];
        img = new Texture2D[Combatable.DYN_STATS][imgDivisions];
        for(int i = 0; i < Combatable.DYN_STATS; i++){
            img[i] = Texturer.bar(64 - 4 * i, imgDivisions, Combatable.dynStatColors[i]);
            
            stats[i] = new Picture("STATUS");
            stats[i].setPosition(16 + 2 * i, frameHeight - 64 - 16 + 2 * i);
            stats[i].move(0, 0, -1);
            stats[i].setWidth(64 - 4 * i);
            stats[i].setHeight(64 - 4 * i);
            guiNode.attachChild(stats[i]);
        }
        
        Picture p = new Picture("BG");
        Texture2D bg = Texturer.hudbg(72);
        p.setTexture(Texturer.assetManager, bg, true);
        p.setPosition(16 - 4, frameHeight - 64 - 16 - 4);
        p.move(0, 0, -2);
        p.setWidth(72);
        p.setHeight(72);
        guiNode.attachChild(p);
        
        textOut = new BitmapText(Texturer.font);
        textOut.setLocalTranslation(64 + 16 + 16, frameHeight - 64, 0);
        guiNode.attachChild(textOut);
        
        testOut = new BitmapText(Texturer.font);
        testOut.setLocalTranslation(64 + 16 + 16, frameHeight - 64 - 16, 0);
        guiNode.attachChild(testOut);
    }
    
    public void updateHUD(Combatable combat){
        for(int i = 0; i < Combatable.DYN_STATS; i++){
            stats[i].setTexture(Texturer.assetManager, img[i][Math.min(imgDivisionMax, (int)(combat.dynStatFrac(i) * imgDivisions))], true);
        }
    }
}
