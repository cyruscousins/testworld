/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package art;

import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.influencers.ParticleInfluencer;
import com.jme3.font.BitmapFont;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;
import com.jme3.util.SkyFactory;
import java.nio.ByteBuffer;
import java.util.Random;
import math.Noise;
import math.SVF2D;
import systems.MaterialData;

/**
 *
 * @author ccousins
 */
public class Texturer {
    
    public static AssetManager assetManager;
    
    public static BitmapFont font;
    
    public static void init(AssetManager assetManager, BitmapFont font){
        Texturer.assetManager = assetManager;
        Texturer.font = font;
    }
    
    //Materials: 
    
    /*
     * SAND material, creates a desert or beach like sand, with
     * Specular, normal, parallax, and diffuse maps.
     */
    
    public static Material[] sand(int width){
        SVF2D colorNoise =   Noise.noiseSVF(width, width, 0, 32.999f);
//        SVF2D featureNoise = Noise.noiseSVF(width, width, 0, 255.999f);
        
        byte[] diffuse = new byte[width * width * 4];
        byte[] specular = new byte[width * width];
        byte[] bump = new byte[width * width];
        
        for(int y = 0; y < width; y++){
            for(int x = 0; x < width; x++){
                int i = x + y * width;
                
                int intensity = (int)(colorNoise.value(x, y) + 32);
                diffuse[i * 3] = (byte)(intensity * 4 - 1);
                diffuse[i * 3 + 1] = (byte)(intensity * 4 - 1);
                diffuse[i * 3 + 2] = (byte)(intensity * 2);
                
                specular[i] = (byte)(colorNoise.value(x * 2, y * 2) * 2 + FastMath.rand.nextInt(64));
                bump[i] = (byte)(colorNoise.value(y, x) * 4 - 1);
            }
        }
        
        
        Texture2D diffuseMap = new Texture2D(new Image(Image.Format.RGB8, width, width, BufferUtils.createByteBuffer(diffuse)));
        diffuseMap.setWrap(Texture.WrapMode.Repeat);
        
        Texture2D specularMap = new Texture2D(new Image(Image.Format.Luminance8, width, width, BufferUtils.createByteBuffer(specular)));
        specularMap.setWrap(Texture.WrapMode.Repeat);
        
        Texture2D normalParallaxMap = normalParallaxMap(bump, width, 12.0f);
        normalParallaxMap.setWrap(Texture.WrapMode.Repeat);
        
        return createMaterialGroup(diffuseMap, specularMap, normalParallaxMap, 16f, 16f);
    }
    
    /*
     * BRICK material, diffuse specular parallax and normal maps.
     */
    
    /*
    //bricks are created and staggered by row.  Height is half of width.  Intervening grout is put in a depression of the bricks, provided by the parallax/normal map.  
    //Coloring is managed by an algorithm that produces red, yellow, white, and black bricks of various combinations of the aforementioned colors.  
    //Width must be a power of 2.  bricks must divide evenly into width (also a power of 2).
    //borderFraction is around the fraction of the image that is borders.
    //stagger is whether or not to offset every second brick.
    //roundedness is a value [0, 1], 0 being unrounded corners, 1 being rounded corners.
    //dirtiness has to do with the color of the borders and how much of the brick face is covered by grout/grime.
    public static Material brick(int width, int hBricks, int vBricks, float borderFraction, boolean stagger, float roundedness, float dirtiness){
        Random rand = FastMath.rand;
        
        
        int brickWidth = width /  hBricks;
        int brickHeight = width / vBricks;
        
        //these variables are calculated now to save calculations in the loop.  Every little bit counts in a 1024 * 1024 texture.
        int halfBrickWidth = brickWidth / 2;
        int brickMaxH = hBricks - 1; //used to determine if we are on the last brick
        float invWidth = 1f / width;
        float oneMinusRoundedness = 1 - roundedness;
        
        int border = (int)(brickHeight * borderFraction);
        float invBorder = 1f / border;
        int borderEdgeX = brickWidth - border;
        int borderEdgeY = brickHeight - border;
        
        int halfWidth = brickWidth / 2;
        int halfHeight = brickHeight / 2;
        
        float invHalfWidth = 1f / halfWidth;
        float invHalfHeight = 1f / halfHeight;
        
        int groutColor = (int)((1 - dirtiness) * 128);
        
        //we want our maps to tile to save the processor quite a bit of work, but the tiling should not overlap.  Differences in brick coloring and nonoverlapping tiling should mask this.  
        int diffuseMapWidth = width / 4;
        int heightMapWidth = width / 8;
        int diffuseScale = 3;
        int heightScale = 4;
        byte[] diffuseNoise = genNoise2(diffuseMapWidth, 4, .75f, 8, .1f);
        byte[] heightNoise = genNoise2(heightMapWidth, 5, .8f, 12, .2f);
        
        byte[] diffuse = new byte[width * width * 3];
        byte[] heights = new byte[width * width];
        
        for(int yBrick = 0; yBrick < vBricks; yBrick++)
            for (int xBrick = 0; xBrick < hBricks; xBrick++){
                //individual brick level
                boolean thisLayerStagger = stagger && ((yBrick & 1) == 1);
                int x0 = xBrick * brickWidth + (thisLayerStagger ? halfWidth : 0);
                int y0 = yBrick * brickHeight;
                //coloring algorithm
                int br = rand.nextInt(255);
                int bg = rand.nextInt(br + 1) >> rand.nextInt(3);//can add enough green to make a yellow brick
                int bb = rand.nextInt(bg + 1);//can add enough blue to make a white brick
                
                for(int subY = 0; subY < brickHeight; subY++){
                    int y = (subY + y0);
                    int yIndex = y * width;
                    float yFrac = y * invWidth;
                    for(int subX = 0; subX < brickWidth; subX++){
                        //individual textel level
                        int x = subX + x0;
                        float xFrac = x * invWidth;
                        if(stagger && xBrick == brickMaxH && thisLayerStagger) x %= width;
                        
                        float diffuseNoiseVal = noiseFunction(diffuseNoise, xFrac * diffuseScale, yFrac * diffuseScale, diffuseMapWidth);
                        float heightNoiseVal = noiseFunction(heightNoise, xFrac * heightScale, yFrac * heightScale, heightMapWidth);
                        int heightIndex = (x + yIndex);
                        int diffuseIndex = heightIndex * 3;

//                        int g0 = gap + (int)(noiseVal * border);
//                        int xt = x + xOff;
//                        int yt = y + yOff;
                        
                        //variables related to the protrusion of the blocks.
                        float xPro = 0;
                        float yPro = 0;
                        float protrusionFraction = 0;
                        
                        if(subX < border) xPro = subX * invBorder;
                        else if(subX > borderEdgeX) xPro = (brickWidth - subX) * invBorder;
                        else xPro = 1;
                        
                        if(subY < border) yPro = subY * invBorder;
                        else if(subY > borderEdgeY) yPro = (brickHeight - subY) * invBorder;
                        else yPro = 1;
                        
                        protrusionFraction = (xPro * yPro) * roundedness + (Math.min(xPro, yPro) * oneMinusRoundedness);
                        
                        float protrusion = protrusionFraction * .75f + heightNoiseVal * .25f;
                        if (protrusion < .5f || heightNoiseVal < dirtiness){ //3/8
                            diffuse[diffuseIndex    ] = 
                            diffuse[diffuseIndex + 1] = 
                            diffuse[diffuseIndex + 2] = (byte)(groutColor + diffuseNoiseVal * groutColor);
                        }
                        else{
                            float intensity = .25f + .75f * diffuseNoiseVal;
                            diffuse[diffuseIndex    ] = (byte)(br * intensity);
                            diffuse[diffuseIndex + 1] = (byte)(bg * intensity);
                            diffuse[diffuseIndex + 2] = (byte)(bb * intensity);
                        }
                        if(protrusion < .4f)heights[heightIndex] = (byte)(protrusion * 127); //allow some bric texture into the crack so the slope renders properly.  
                        else if (protrusion < .4f) heights[heightIndex] = (byte)((protrusion - .4f) * 500 + .4f * 127);
                        else heights[heightIndex] = (byte)((protrusion - .5f) * 100 + .4f * 127 + .1f * 500);
                    }
                }
            }
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        
        Texture diffuseMap = new Texture2D(new Image(Image.Format.RGB8, width, width, BufferUtils.createByteBuffer(diffuse)));
        diffuseMap.setWrap(Texture.WrapMode.Repeat);
        
        Texture normalParallaxMap = normalParallaxMap(heights, width);
        normalParallaxMap.setWrap(Texture.WrapMode.Repeat);
        
        mat.setTexture("DiffuseMap", diffuseMap);
        mat.setTexture("NormalMap", normalParallaxMap);
        mat.setBoolean("PackedNormalParallax", true);
        //mat.setFloat("ParallaxHeight", 3);
        
        mat.setFloat("Shininess", 1);
        return mat;
    }
    
    */
    
    /*
     * Test Materials
     */
    public static Material wireframe(){
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
//        mat.setTexture("ColorMap", diffuseMap);
        mat.setColor("Color", ColorRGBA.Gray);
        mat.getAdditionalRenderState().setWireframe(true);
        return mat;
    }
    
    public static Material[] wireframeLods(){
        ColorRGBA[] colors = new ColorRGBA[]{
            new ColorRGBA(.5f, 0, 0, 0),
            new ColorRGBA(1f, 0, 0, 0),
            new ColorRGBA(0, .5f, 0, 0),
            new ColorRGBA(0, 1, 0, 0),
            new ColorRGBA(0, 0, .5f, 0),
            new ColorRGBA(0, 0, 1f, 0)
        };
        
        Material[] mats = new Material[colors.length];
        
        for(int i = 0; i < mats.length; i++){
            mats[i] = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mats[i].setColor("Color", colors[i]);
            mats[i].getAdditionalRenderState().setWireframe(true);
        }
        return mats;
    }
    
    
    /*
     * Helper functions, used to make materials
     */ 
    
    /*
     * Create a group of materials of varying quality
     */
    public static Material[] createMaterialGroup(Texture2D diffuseMap, Texture2D specularMap, Texture2D normalParallaxMap, float shininess, float parallaxHeight){
        
        Material[] mat = new Material[3];
        for(int i = 0; i < mat.length; i++){
            mat[i] = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            mat[i].setTexture("DiffuseMap", diffuseMap);
            if(i > 1){
                //specular map on
                mat[i].setTexture("SpecularMap", specularMap);
                mat[i].setFloat("Shininess", shininess);
//                mat[i].setVector3("FresnelParams", new Vector3f(0.05f, 0.18f, 0.11f));
                
                //normal map on
                mat[i].setTexture("NormalMap", normalParallaxMap);
            }
            if(i > 2){
                //turn parallax on
                mat[i].setBoolean("PackedNormalParallax", true); 
                mat[i].setFloat("ParallaxHeight", parallaxHeight);
                mat[i].setBoolean("SteepParallax", true);
            }
        }
        return mat;
    }
    
    /*
     * Create a normal and parallax map from heightmap data.
     */
    
    //TODO update shader and function to not use the - hack (ie we can store negatives in the shader's float.
    //the shader expects r : x, g : y, b : z, a : depth (parallax)
    public static Texture2D normalParallaxMap(byte[] heights, int width, float depth){
        ByteBuffer bb = com.jme3.util.BufferUtils.createByteBuffer(width * width * 4);
        //float invByte = 1 / 256f;
        int widthMinus1 = width - 1;
        
        int wSqr = width * width;
        int wSqrMinusWidth = wSqr - width;
        
        float invByte = 1f / 256;
        
        for(int y = 0; y < width; y++){
            int by = y * width;
            for(int x = 0; x < width; x++){
                int i = x + by;
                //the best way to calculate normals is with cross products.  
                float dydx = ((heights[(x + 1) % width + by] & 0xFF) - (heights[(x + widthMinus1) % width + by] & 0xFF)) * depth * invByte;//take the x derivative, accounting for the edges of the map, then convert to an unsigned byte with &0xFF, then express as a floating point fraction of 1.
                float dydz = ((heights[(x + by + width) % wSqr] & 0xFF) - (heights[(x + by + wSqrMinusWidth) % wSqr] & 0xFF)) * depth * invByte;
                
                //we now have two perpendicular lines, the cross product of which is the desired normal.
                //the way it works out is as follows.
                float nX = -dydz; //*** This is a different coordinate system from the rest of jme3
                float nY = -dydx;
                float nZ = 1;
                //now we must normalize and convert it to the image format, wherin 0 = -1 and 1 = 1, linearly.
                float scale = FastMath.fastInvSqrt(nX * nX + nY * nY + nZ * nZ) * 128;
                
//                bb.put((byte)FastMath.rand.nextInt());
//                bb.put((byte)FastMath.rand.nextInt());
//                bb.put((byte)FastMath.rand.nextInt());
//                bb.put((byte)FastMath.rand.nextInt());
                
                bb.put((byte)(127 + nX * scale));
                bb.put((byte)(127 + nY * scale));
                bb.put((byte)(127 + nZ * scale));
                bb.put(heights[i]);//Parallax Map
            }
        }
        return new Texture2D(new Image(Image.Format.RGBA8, width, width, bb));
        /*
        ByteBuffer bb = com.jme3.util.BufferUtils.createByteBuffer(width * width * 4);

        int wm1 = width - 1;
        int hi;
        
        float nx;
        float ny;
        float nz;
        
        float s;
        
        float invDepth = 1f / depth;
        float idSqr = invDepth * invDepth;
        
        //tl corner
        
        nx = heights[1] - heights[width - 1];
        ny = heights[width] - heights[(width - 1) * width])
        nz = invDepth;
        
        s = (nx * nx + ny * ny + idSqr) + 128;
        
        bb.put((byte)(127 + s * nx));
        bb.put((byte)(127 + s * ny);
        bb.put((byte)(127 + s * nz);
        bb.put(heights[0]);
        
        //top
        
        //tr corner
        
        //body
        for(int y = 1; y < wm1; y++){
            //left side
            //interior
            for(int x = 1; x < wm1; x++){
                
            }
            //right side
        }
        
        //bl corner
        
        //bottom
        
        //br corner
        
        
        //float invByte = 1 / 256f;
        int widthMinus1 = width - 1;
        
        int wSqr = width * width;
        int wSqrMinusWidth = wSqr - width;
        
        float invByte = 1f / 256;
        
        for(int y = 0; y < width; y++){
            int by = y * width;
            for(int x = 0; x < width; x++){
                int i = x + by;
                //the best way to calculate normals is with cross products.  
                float dydx = ((heights[(x + 1) % width + by] & 0xFF) - (heights[(x + widthMinus1) % width + by] & 0xFF)) * depth * invByte;//take the x derivative, accounting for the edges of the map, then convert to an unsigned byte with &0xFF, then express as a floating point fraction of 1.
                float dydz = ((heights[(x + by + width) % wSqr] & 0xFF) - (heights[(x + by + wSqrMinusWidth) % wSqr] & 0xFF)) * depth * invByte;
                
                //we now have two perpendicular lines, the cross product of which is the desired normal.
                //the way it works out is as follows.
                float nX = -dydz; //*** This is a different coordinate system from the rest of jme3
                float nY = -dydx;
                float nZ = 1;
                //now we must normalize and convert it to the image format, wherin 0 = -1 and 1 = 1, linearly.
                float scale = FastMath.fastInvSqrt(nX * nX + nY * nY + nZ * nZ) * 128;
                
//                bb.put((byte)FastMath.rand.nextInt());
//                bb.put((byte)FastMath.rand.nextInt());
//                bb.put((byte)FastMath.rand.nextInt());
//                bb.put((byte)FastMath.rand.nextInt());
                
                bb.put((byte)(127 + nX * scale));
                bb.put((byte)(127 + nY * scale));
                bb.put((byte)(127 + nX * scale));
                bb.put(heights[i]);//Parallax Map
            }
        }
        return new Texture2D(new Image(Image.Format.RGBA8, width, width, bb));
        */
    }    
    
    /*
     * Alternatively, use the following for split height/normal maps
     */
    public static byte[] genRandHeightMapBytes(int width){
        byte[] data = new byte[width * width];
        SVF2D noise = math.Noise.noiseSVF(width, width, 0, 255.9f);
        for(int y = 0; y < width; y++){
            for(int x = 0; x < width; x++){
                data[x + y * width] = (byte)noise.value(x, y);
            }
        }
        return data;
    }
    public static Texture2D genHeightMap(byte[] bytes, int width){
        return new Texture2D(new Image(Image.Format.Luminance8, width, width, BufferUtils.createByteBuffer(bytes)));
    }
    public static Texture2D genNormalMap(byte[] heights, int width, float depth){
        ByteBuffer bb = com.jme3.util.BufferUtils.createByteBuffer(width * width * 4);
        //float invByte = 1 / 256f;
        int widthMinus1 = width - 1;
        
        int wSqr = width * width;
        int wSqrMinusWidth = wSqr - width;
        
        float invByte = 1f / 256;
        
        for(int y = 0; y < width; y++){
            int by = y * width;
            for(int x = 0; x < width; x++){
                int i = x + by;
                //the best way to calculate normals is with cross products.  
                float dydx = ((heights[(x + 1) % width + by] & 0xFF) - (heights[(x + widthMinus1) % width + by] & 0xFF)) * depth * invByte;//take the x derivative, accounting for the edges of the map, then convert to an unsigned byte with &0xFF, then express as a floating point fraction of 1.
                float dydz = ((heights[(x + by + width) % wSqr] & 0xFF) - (heights[(x + by + wSqrMinusWidth) % wSqr] & 0xFF)) * depth * invByte;
                
                //we now have two perpendicular lines, the cross product of which is the desired normal.
                //the way it works out is as follows.
                float nX = -dydz; //*** This is a different coordinate system from the rest of jme3
                float nY = -dydx;
                float nZ = 1;
                //now we must normalize and convert it to the image format, wherin 0 = -1 and 1 = 1, linearly.
                float scale = FastMath.fastInvSqrt(nX * nX + nY * nY + nZ * nZ) * 128;
                
//                bb.put((byte)FastMath.rand.nextInt());
//                bb.put((byte)FastMath.rand.nextInt());
//                bb.put((byte)FastMath.rand.nextInt());
//                bb.put((byte)FastMath.rand.nextInt());

                bb.put((byte)(127 + nZ * scale));                
                bb.put((byte)(127 + nY * scale));
                bb.put((byte)(127 + nX * scale));
            }
        }
        return new Texture2D(new Image(Image.Format.BGR8, width, width, bb));
    }
    
    
    //bricks are created and staggered by row.  Height is half of width.  Intervening grout is put in a depression of the bricks, provided by the parallax/normal map.  
    //Coloring is managed by an algorithm that produces red, yellow, white, and black bricks of various combinations of the aforementioned colors.  
    //Width must be a power of 2.  bricks must divide evenly into width (also a power of 2).
    //borderFraction is around the fraction of the image that is borders.
    //stagger is whether or not to offset every second brick.
    //roundedness is a value [0, 1], 0 being unrounded corners, 1 being rounded corners.
    //dirtiness has to do with the color of the borders and how much of the brick face is covered by grout/grime.
    public static Material brick(int width, int hBricks, int vBricks, float borderFraction, boolean stagger, float roundedness, float dirtiness){
        Random rand = FastMath.rand;
        
        SVF2D diffuseNoise =   Noise.noiseSVF(width / 4, width, 0, 1);
        SVF2D heightNoise =   Noise.noiseSVF(width / 8, width, .7f, 1);
        
        int brickWidth = width /  hBricks;
        int brickHeight = width / vBricks;
        
        //these variables are calculated now to save calculations in the loop.  Every little bit counts in a 1024 * 1024 texture.
        int halfBrickWidth = brickWidth / 2;
        int brickMaxH = hBricks - 1; //used to determine if we are on the last brick
        float invWidth = 1f / width;
        float oneMinusRoundedness = 1 - roundedness;
        
        int border = (int)(brickHeight * borderFraction);
        float invBorder = 1f / border;
        int borderEdgeX = brickWidth - border;
        int borderEdgeY = brickHeight - border;
        
        int halfWidth = brickWidth / 2;
        int halfHeight = brickHeight / 2;
        
        float invHalfWidth = 1f / halfWidth;
        float invHalfHeight = 1f / halfHeight;
        
        int groutColor = (int)((1 - dirtiness) * 128);
        
        //we want our maps to tile to save the processor quite a bit of work, but the tiling should not overlap.  Differences in brick coloring and nonoverlapping tiling should mask this.  
        int diffuseMapWidth = width / 4;
        int heightMapWidth = width / 8;
        int diffuseScale = 3;
        int heightScale = 4;
        
        byte[] diffuse = new byte[width * width * 3];
        byte[] heights = new byte[width * width];
        
        for(int yBrick = 0; yBrick < vBricks; yBrick++){
            for (int xBrick = 0; xBrick < hBricks; xBrick++){
                //individual brick level
                boolean thisLayerStagger = stagger && ((yBrick & 1) == 1);
                int x0 = xBrick * brickWidth + (thisLayerStagger ? halfWidth : 0);
                int y0 = yBrick * brickHeight;
                //coloring algorithm
                int br = rand.nextInt(255);
                int bg = rand.nextInt(br + 1) >> rand.nextInt(3);//can add enough green to make a yellow brick
                int bb = rand.nextInt(bg + 1);//can add enough blue to make a white brick
                
                for(int subY = 0; subY < brickHeight; subY++){
                    int y = (subY + y0);
                    int yIndex = y * width;
                    for(int subX = 0; subX < brickWidth; subX++){
                        //individual textel level
                        int x = subX + x0;
                        if(stagger && xBrick == brickMaxH && thisLayerStagger) x %= width;
                        
                        float diffuseNoiseVal = diffuseNoise.value(x, y); //noiseFunction(diffuseNoise, xFrac * diffuseScale, yFrac * diffuseScale, diffuseMapWidth);
                        float heightNoiseVal = heightNoise.value(x, y); //noiseFunction(heightNoise, xFrac * heightScale, yFrac * heightScale, heightMapWidth);
                        
                        int heightIndex = (x + yIndex);
                        int diffuseIndex = heightIndex * 3;

                        //variables related to the protrusion of the blocks.
                        float xPro, yPro, protrusionFraction;
                        
                        if(subX < border) xPro = subX * invBorder;
                        else if(subX > borderEdgeX) xPro = (brickWidth - subX) * invBorder;
                        else xPro = 1;
                        
                        if(subY < border) yPro = subY * invBorder;
                        else if(subY > borderEdgeY) yPro = (brickHeight - subY) * invBorder;
                        else yPro = 1;
                        
                        protrusionFraction = (xPro * yPro * roundedness) + (Math.min(xPro, yPro) * oneMinusRoundedness);
                        
                        //We want an curve for protrusion, so the edges of bricks may be eroded.
                        //         ___
                        //       _/
                        //      /
                        //    _/
                        //___/
                        
//                        if(protrusionFraction > .75f) protrusionFraction = (1f + protrusionFraction) * .5f;
//                        else if(protrusionFraction > .5f) protrusionFraction = (protrusionFraction - .5f) * 2 + .25f;
//                        else protrusionFraction = protrusionFraction * protrusionFraction;
                        
                        if(protrusionFraction < .5f) protrusionFraction = protrusionFraction * protrusionFraction * 2;
                        else protrusionFraction = 1 - (1 - protrusionFraction) * (1 - protrusionFraction);
//                        System.out.println("BRICKHEIGHT: " + heightNoiseVal);
                        protrusionFraction *= heightNoiseVal;
                        
                        heights[heightIndex] = (byte)((protrusionFraction) * 255);

//                        System.out.println(protrusionFraction);
//                        float protrusion = protrusionFraction * .75f + heightNoiseVal * .25f;
                        if (protrusionFraction < .65f){ //3/8
                            diffuse[diffuseIndex    ] = 
                            diffuse[diffuseIndex + 1] = 
                            diffuse[diffuseIndex + 2] = (byte)(groutColor + diffuseNoiseVal * groutColor);
                        }
                        else{
                            float intensity = .25f + .75f * diffuseNoiseVal;
                            diffuse[diffuseIndex    ] = (byte)(br * intensity);
                            diffuse[diffuseIndex + 1] = (byte)(bg * intensity);
                            diffuse[diffuseIndex + 2] = (byte)(bb * intensity);
                        }
                        
//                                                diffuse[diffuseIndex] = heights[heightIndex];
//                                                diffuse[diffuseIndex + 1] = (byte)(protrusionFraction * 255);
                        
//                        heights[heightIndex] = (byte)(protrusion * 255);
//                        if(protrusion < .45f)heights[heightIndex] = (byte)(protrusion * 127); //allow some brick texture into the crack so the slope renders properly.  
//                        else if (protrusion < .4f) heights[heightIndex] = (byte)((protrusion - .4f) * 500 + .4f * 127);
//                        else heights[heightIndex] = (byte)((protrusion - .5f) * 100 + .4f * 127 + .1f * 500);
                    }
                }
            }
        }
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        
        Texture diffuseMap = new Texture2D(new Image(Image.Format.RGB8, width, width, BufferUtils.createByteBuffer(diffuse)));
        diffuseMap.setWrap(Texture.WrapMode.Repeat);
        
        Texture normalParallaxMap = normalParallaxMap(heights, width, 3.0f);
        normalParallaxMap.setWrap(Texture.WrapMode.Repeat);
        
//        mat.setTexture("DiffuseMap", new Texture2D(new Image(Image.Format.Luminance8, width, width, BufferUtils.createByteBuffer(heights))));
        mat.setTexture("DiffuseMap", diffuseMap);
        mat.setTexture("NormalMap", normalParallaxMap);
        mat.setBoolean("PackedNormalParallax", true);
        mat.setFloat("ParallaxHeight", .025f);
        
        mat.setFloat("Shininess", 1);
        return mat;
    }
    
    public static Material stone(int width, int hBricks, int vBricks, float borderFraction, boolean stagger, float roundedness, float dirtiness){
        Random rand = FastMath.rand;
        
        SVF2D diffuseNoise =   Noise.noiseSVF(width / 4, width, 0, 1);
        SVF2D heightNoise =   Noise.noiseSVF(width / 8, width, .7f, 1);
        
        int brickWidth = width /  hBricks;
        int brickHeight = width / vBricks;
        
        //these variables are calculated now to save calculations in the loop.  Every little bit counts in a 1024 * 1024 texture.
        int halfBrickWidth = brickWidth / 2;
        int brickMaxH = hBricks - 1; //used to determine if we are on the last brick
        float invWidth = 1f / width;
        float oneMinusRoundedness = 1 - roundedness;
        
        int border = (int)(brickHeight * borderFraction);
        float invBorder = 1f / border;
        int borderEdgeX = brickWidth - border;
        int borderEdgeY = brickHeight - border;
        
        int halfWidth = brickWidth / 2;
        int halfHeight = brickHeight / 2;
        
        float invHalfWidth = 1f / halfWidth;
        float invHalfHeight = 1f / halfHeight;
        
        int groutColor = (int)((1 - dirtiness) * 128);
        
        //we want our maps to tile to save the processor quite a bit of work, but the tiling should not overlap.  Differences in brick coloring and nonoverlapping tiling should mask this.  
        int diffuseMapWidth = width / 4;
        int heightMapWidth = width / 8;
        int diffuseScale = 3;
        int heightScale = 4;
        
        byte[] diffuse = new byte[width * width * 3];
        byte[] heights = new byte[width * width];
        
        for(int yBrick = 0; yBrick < vBricks; yBrick++){
            for (int xBrick = 0; xBrick < hBricks; xBrick++){
                //individual brick level
                boolean thisLayerStagger = stagger && ((yBrick & 1) == 1);
                int x0 = xBrick * brickWidth + (thisLayerStagger ? halfWidth : 0);
                int y0 = yBrick * brickHeight;
                //coloring algorithm
                int br = 128 + rand.nextInt(128);
                int bg = br - rand.nextInt(32);//can add enough green to make a yellow brick
                int bb = br - rand.nextInt(32);//can add enough blue to make a white brick
                
                for(int subY = 0; subY < brickHeight; subY++){
                    int y = (subY + y0);
                    int yIndex = y * width;
                    for(int subX = 0; subX < brickWidth; subX++){
                        //individual textel level
                        int x = subX + x0;
                        if(stagger && xBrick == brickMaxH && thisLayerStagger) x %= width;
                        
                        float diffuseNoiseVal = diffuseNoise.value(x, y); //noiseFunction(diffuseNoise, xFrac * diffuseScale, yFrac * diffuseScale, diffuseMapWidth);
                        float heightNoiseVal = heightNoise.value(x, y); //noiseFunction(heightNoise, xFrac * heightScale, yFrac * heightScale, heightMapWidth);
                        
                        int heightIndex = (x + yIndex);
                        int diffuseIndex = heightIndex * 3;

                        //variables related to the protrusion of the blocks.
                        float xPro, yPro, protrusionFraction;
                        
                        if(subX < border) xPro = subX * invBorder;
                        else if(subX > borderEdgeX) xPro = (brickWidth - subX) * invBorder;
                        else xPro = 1;
                        
                        if(subY < border) yPro = subY * invBorder;
                        else if(subY > borderEdgeY) yPro = (brickHeight - subY) * invBorder;
                        else yPro = 1;
                        
                        protrusionFraction = (xPro * yPro * roundedness) + (Math.min(xPro, yPro) * oneMinusRoundedness);
                        
                        //We want an curve for protrusion, so the edges of bricks may be eroded.
                        //         ___
                        //       _/
                        //      /
                        //    _/
                        //___/
                        
//                        if(protrusionFraction > .75f) protrusionFraction = (1f + protrusionFraction) * .5f;
//                        else if(protrusionFraction > .5f) protrusionFraction = (protrusionFraction - .5f) * 2 + .25f;
//                        else protrusionFraction = protrusionFraction * protrusionFraction;
                        
                        if(protrusionFraction < .5f) protrusionFraction = protrusionFraction * protrusionFraction * 2;
                        else protrusionFraction = 1 - (1 - protrusionFraction) * (1 - protrusionFraction);
//                        System.out.println("BRICKHEIGHT: " + heightNoiseVal);
                        protrusionFraction *= heightNoiseVal;
                        
                        heights[heightIndex] = (byte)((protrusionFraction) * 255);

//                        System.out.println(protrusionFraction);
//                        float protrusion = protrusionFraction * .75f + heightNoiseVal * .25f;
                        if (protrusionFraction < .65f){ //3/8
                            diffuse[diffuseIndex    ] = 
                            diffuse[diffuseIndex + 1] = 
                            diffuse[diffuseIndex + 2] = (byte)(groutColor + diffuseNoiseVal * groutColor);
                        }
                        else{
                            float intensity = .25f + .75f * diffuseNoiseVal;
                            diffuse[diffuseIndex    ] = (byte)(br * intensity);
                            diffuse[diffuseIndex + 1] = (byte)(bg * intensity);
                            diffuse[diffuseIndex + 2] = (byte)(bb * intensity);
                        }
                        
//                                                diffuse[diffuseIndex] = heights[heightIndex];
//                                                diffuse[diffuseIndex + 1] = (byte)(protrusionFraction * 255);
                        
//                        heights[heightIndex] = (byte)(protrusion * 255);
//                        if(protrusion < .45f)heights[heightIndex] = (byte)(protrusion * 127); //allow some brick texture into the crack so the slope renders properly.  
//                        else if (protrusion < .4f) heights[heightIndex] = (byte)((protrusion - .4f) * 500 + .4f * 127);
//                        else heights[heightIndex] = (byte)((protrusion - .5f) * 100 + .4f * 127 + .1f * 500);
                    }
                }
            }
        }
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        
        Texture diffuseMap = new Texture2D(new Image(Image.Format.RGB8, width, width, BufferUtils.createByteBuffer(diffuse)));
        diffuseMap.setWrap(Texture.WrapMode.Repeat);
        
        Texture normalParallaxMap = normalParallaxMap(heights, width, 3.0f);
        normalParallaxMap.setWrap(Texture.WrapMode.Repeat);
        
//        mat.setTexture("DiffuseMap", new Texture2D(new Image(Image.Format.Luminance8, width, width, BufferUtils.createByteBuffer(heights))));
        mat.setTexture("DiffuseMap", diffuseMap);
        mat.setTexture("NormalMap", normalParallaxMap);
        mat.setBoolean("PackedNormalParallax", true);
        mat.setFloat("ParallaxHeight", .1f);
        
        mat.setFloat("Shininess", 1);
        return mat;
    }
    
    // Material stone = Texturer.stone(1024, 4, 4, .125f, false, .25f, .3f);
    public static Material stoneTile(int width, int hBricks, int vBricks, float borderFraction, boolean stagger, float roundedness, float dirtiness){
        Random rand = FastMath.rand;
        
        SVF2D diffuseNoise =   Noise.noiseSVF(width / 4, width, 0, 1);
        SVF2D heightNoise =   Noise.noiseSVF(width / 8, width, .7f, 1);
        
        int brickWidth = width /  hBricks;
        int brickHeight = width / vBricks;
        
        //these variables are calculated now to save calculations in the loop.  Every little bit counts in a 1024 * 1024 texture.
        int halfBrickWidth = brickWidth / 2;
        int brickMaxH = hBricks - 1; //used to determine if we are on the last brick
        float invWidth = 1f / width;
        float oneMinusRoundedness = 1 - roundedness;
        
        int border = (int)(brickHeight * borderFraction);
        float invBorder = 1f / border;
        int borderEdgeX = brickWidth - border;
        int borderEdgeY = brickHeight - border;
        
        int halfWidth = brickWidth / 2;
        int halfHeight = brickHeight / 2;
        
        float invHalfWidth = 1f / halfWidth;
        float invHalfHeight = 1f / halfHeight;
        
        int groutColor = (int)((1 - dirtiness) * 128);
        
        //we want our maps to tile to save the processor quite a bit of work, but the tiling should not overlap.  Differences in brick coloring and nonoverlapping tiling should mask this.  
        int diffuseMapWidth = width / 4;
        int heightMapWidth = width / 8;
        int diffuseScale = 3;
        int heightScale = 4;
        
        byte[] diffuse = new byte[width * width * 3];
        byte[] heights = new byte[width * width];
        
        for(int yBrick = 0; yBrick < vBricks; yBrick++){
            for (int xBrick = 0; xBrick < hBricks; xBrick++){
                //individual brick level
                boolean thisLayerStagger = stagger && ((yBrick & 1) == 1);
                int x0 = xBrick * brickWidth + (thisLayerStagger ? halfWidth : 0);
                int y0 = yBrick * brickHeight;
                //coloring algorithm
                int br = 128 + rand.nextInt(64);
                int bg = br - rand.nextInt(32);//can add enough green to make a yellow brick
                int bb = br - rand.nextInt(32);//can add enough blue to make a white brick
                
                for(int subY = 0; subY < brickHeight; subY++){
                    int y = (subY + y0);
                    int yIndex = y * width;
                    for(int subX = 0; subX < brickWidth; subX++){
                        //individual textel level
                        int x = subX + x0;
                        if(stagger && xBrick == brickMaxH && thisLayerStagger) x %= width;
                        
                        float diffuseNoiseVal = diffuseNoise.value(x, y); //noiseFunction(diffuseNoise, xFrac * diffuseScale, yFrac * diffuseScale, diffuseMapWidth);
                        float heightNoiseVal = heightNoise.value(x, y); //noiseFunction(heightNoise, xFrac * heightScale, yFrac * heightScale, heightMapWidth);
                        
                        int heightIndex = (x + yIndex);
                        int diffuseIndex = heightIndex * 3;

                        //variables related to the protrusion of the blocks.
                        float xPro, yPro, protrusionFraction;
                        
                        if(subX < border) xPro = subX * invBorder;
                        else if(subX > borderEdgeX) xPro = (brickWidth - subX) * invBorder;
                        else xPro = 1;
                        
                        if(subY < border) yPro = subY * invBorder;
                        else if(subY > borderEdgeY) yPro = (brickHeight - subY) * invBorder;
                        else yPro = 1;
                        
                        protrusionFraction = (xPro * yPro * roundedness) + (Math.min(xPro, yPro) * oneMinusRoundedness);
                        
                        //We want an curve for protrusion, so the edges of bricks may be eroded.
                        //         ___
                        //       _/
                        //      /
                        //    _/
                        //___/
                        
//                        if(protrusionFraction > .75f) protrusionFraction = (1f + protrusionFraction) * .5f;
//                        else if(protrusionFraction > .5f) protrusionFraction = (protrusionFraction - .5f) * 2 + .25f;
//                        else protrusionFraction = protrusionFraction * protrusionFraction;
                        
                        if(protrusionFraction < .5f) protrusionFraction = protrusionFraction * protrusionFraction * 2;
                        else protrusionFraction = 1 - (1 - protrusionFraction) * (1 - protrusionFraction);
//                        System.out.println("BRICKHEIGHT: " + heightNoiseVal);
                        protrusionFraction *= heightNoiseVal;
                        
                        heights[heightIndex] = (byte)((protrusionFraction) * 255);

//                        System.out.println(protrusionFraction);
//                        float protrusion = protrusionFraction * .75f + heightNoiseVal * .25f;
                        if (protrusionFraction < .65f){ //3/8
                            diffuse[diffuseIndex    ] = 
                            diffuse[diffuseIndex + 1] = 
                            diffuse[diffuseIndex + 2] = (byte)(groutColor + diffuseNoiseVal * groutColor);
                        }
                        else{
                            float intensity = .25f + .75f * diffuseNoiseVal;
                            diffuse[diffuseIndex    ] = (byte)(br * intensity);
                            diffuse[diffuseIndex + 1] = (byte)(bg * intensity);
                            diffuse[diffuseIndex + 2] = (byte)(bb * intensity);
                        }
                        
//                                                diffuse[diffuseIndex] = heights[heightIndex];
//                                                diffuse[diffuseIndex + 1] = (byte)(protrusionFraction * 255);
                        
//                        heights[heightIndex] = (byte)(protrusion * 255);
//                        if(protrusion < .45f)heights[heightIndex] = (byte)(protrusion * 127); //allow some brick texture into the crack so the slope renders properly.  
//                        else if (protrusion < .4f) heights[heightIndex] = (byte)((protrusion - .4f) * 500 + .4f * 127);
//                        else heights[heightIndex] = (byte)((protrusion - .5f) * 100 + .4f * 127 + .1f * 500);
                    }
                }
            }
        }
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        
        Texture diffuseMap = new Texture2D(new Image(Image.Format.RGB8, width, width, BufferUtils.createByteBuffer(diffuse)));
        diffuseMap.setWrap(Texture.WrapMode.Repeat);
        
        Texture normalParallaxMap = normalParallaxMap(heights, width, 3.0f);
        normalParallaxMap.setWrap(Texture.WrapMode.Repeat);
        
//        mat.setTexture("DiffuseMap", new Texture2D(new Image(Image.Format.Luminance8, width, width, BufferUtils.createByteBuffer(heights))));
        mat.setTexture("DiffuseMap", diffuseMap);
        mat.setTexture("NormalMap", normalParallaxMap);
        mat.setBoolean("PackedNormalParallax", true);
        mat.setFloat("ParallaxHeight", .025f);
        
        mat.setFloat("Shininess", 1);
        return mat;
    }
    
    
    
    public static Texture2D testRGB16f(){
        int width = 64;
        
        byte[] data = new byte[width * width * 6];
        
        for(int y = 0; y < width; y++){
            for(int x = 0; x < width; x++){
                int i = (y * width + x) * 6;
                
                short temp = FastMath.convertFloatToHalf(x / (float) width);
                data[i + 0] = (byte)(temp & 0xff);
                data[i + 1] = (byte)(temp >>> 8 & 0xff);
                
                temp = FastMath.convertFloatToHalf(y / (float) width);
                data[i + 2] = (byte)(temp & 0xff);
                data[i + 3] = (byte)(temp >>> 8 & 0xff);
                
                temp = FastMath.convertFloatToHalf(2 - (y + x) / (float) width);
                data[i + 4] = (byte)(temp & 0xff);
                data[i + 5] = (byte)(temp >>> 8 & 0xff);
            }
        }
        
        Texture2D north = new Texture2D(new Image(Image.Format.RGB16F, width, width, BufferUtils.createByteBuffer(data)));
        
        
        return north;
    }
    
    /*
     * Take a float array in RGBA format, convert to HDR RGB16, load into byte buffer, return buffer.
     */
    public static ByteBuffer floatsToRGB16f(float[] f){
        ByteBuffer b = BufferUtils.createByteBuffer(f.length * 2);
        for(int i = 0; i < f.length; i++){
            short s = FastMath.convertFloatToHalf(f[i]);
            b.put((byte)(s & 0xff));
            b.put((byte)((s >>> 8) & 0xff));
        }
        return b;
    }
    
    //Environment Textures
    /*
     * Skybox texture related stuff
     */
    
    public static Spatial createDaySkyBox(int width){
        Texture2D[] sky = new Texture2D[6];
        
        float invWidth = 1f / width;
        
        float[] floats = new float[width * width * 3];
        
        SVF2D noise = math.Noise.noiseSVF(width / 2, width, -.5f, .5f);
        
        for(int y = 0; y < width; y++){
            float yFrac = y * invWidth;
                
            for(int x = 0; x < width; x++){
                int i = (y * width + x) * 3;
                
                float noiseVal = noise.value(x, y);
                if(yFrac < .5f) noiseVal = 0;
                else if(noiseVal > 0){
                    //we need no noise at the top and below the center.
                    noiseVal -= FastMath.pow(yFrac * 2 - 1, 5) * .5f; //remove top noise
                    noiseVal -= FastMath.pow((2 - yFrac * 2), 5) * .5f;
                }
                
                if(noiseVal > .1f){
                    floats[i + 0] = floats[i + 1] = floats[i + 2] = noiseVal + .5f;
                }
                else{
                    floats[i + 0] = yFrac * .125f + .125f;
                    floats[i + 1] = yFrac * .125f + .125f;
                    floats[i + 2] = yFrac * .25f + .75f;
//                if(y % 2 == 0) floats[i + 2] = y * invWidth * 1;
                }
                
            }
        }
        //sides
        sky[0] = sky[1] = sky[2] = sky[3] = new Texture2D(new Image(Image.Format.RGB16F, width, width, floatsToRGB16f(floats)));
        
        //top
        int semiWidth = width / 2;
        for(int y = 0; y < width; y++){
            for(int x = 0; x < width; x++){
                int i = (y * width + x) * 3;
                float d = (invWidth * Math.min(Math.min(x, width - x), Math.min(y, width - y)));
                float dcx = invWidth * (semiWidth - x);
                float dcy = invWidth * (semiWidth - y);
                
                floats[i + 0] = .25f + d * .125f;
                floats[i + 1] = .25f + d * .125f;
                floats[i + 2] =   1f + d * .25f;
                
                if(dcx * dcx + dcy * dcy < .01f){
                    floats[i + 0] += 2f;
                    floats[i + 1] += 1.5f;
                    floats[i + 2] += 1f;
                    
                    if(dcx * dcx + dcy * dcy < .005f){
                        floats[i + 0] += 5f;
                        floats[i + 1] += 4f;
                        floats[i + 2] += 2f;
                    }
                }
                
            }
        }
        
        sky[4] = new Texture2D(new Image(Image.Format.RGB16F, width, width, floatsToRGB16f(floats)));
        
        //bottom
        for(int y = 0; y < width; y++){
            for(int x = 0; x < width; x++){
                int i = (y * width + x) * 3;
                float d = (invWidth * Math.min(Math.min(x, width - x), Math.min(y, width - y)));
                float dcx = invWidth * (semiWidth - x);
                float dcy = invWidth * (semiWidth - y);
                
                floats[i + 0] = .125f - d * .125f;
                floats[i + 1] = .125f - d * .125f;
                floats[i + 2] = .75f - d * .125f;
            }
        }
        
        sky[5] = new Texture2D(new Image(Image.Format.RGB16F, width, width, floatsToRGB16f(floats)));
        
        
        Spatial skybox = SkyFactory.createSky(assetManager, sky[0], sky[1], sky[2], sky[3], sky[4], sky[5]);
        return skybox;
    }
    
    
    public static Spatial createNightSkyBox(int width, boolean aurora){
        Texture2D[] sky = new Texture2D[6];
        
        float invWidth = 1f / width;
        
        float[] floats = new float[width * width * 3];
        
        SVF2D starNoise = math.Noise.noiseSVF(width, width, 0f, 1f);
        float starLim = .2f;
        float starScalar = 3f;
        float starProb = .005f;
        
        SVF2D rNoise;
        SVF2D gNoise;
        SVF2D bNoise;
        
        float auroraIntensity = .5f;
        float auroraExp = 3.5f;
        float auroraMax = FastMath.pow(auroraIntensity, 1f / auroraExp);
        
        if(aurora){
            rNoise = math.Noise.noiseSVF(width / 4, width, 0f, auroraMax);
            gNoise = math.Noise.noiseSVF(width / 4, width, 0f, auroraMax);
            bNoise = math.Noise.noiseSVF(width / 4, width, 0f, auroraMax);
            
        }
        else{
            rNoise = gNoise = bNoise = null;
//            rNoise = new math.ConstSVF2D(0);
//            gNoise = new math.ConstSVF2D(0);
//            bNoise = new math.ConstSVF2D(0);
        }
        
        for(int y = 0; y < width; y++){
            float yFrac = y * invWidth;
                
            for(int x = 0; x < width; x++){
                int i = (y * width + x) * 3;
                
                float noiseVal = starNoise.value(x, y);
//                if(yFrac < .5f) noiseVal = 0;
//                else if(noiseVal > 0){
//                    //we need no noise at the top and below the center.
//                    noiseVal -= FastMath.pow(yFrac * 2 - 1, 5) * .5f; //remove top noise
//                    noiseVal -= FastMath.pow((2 - yFrac * 2), 5) * .5f;
//                }
                
                //stars
                if(noiseVal > starLim && FastMath.rand.nextFloat() < starProb){
                    float starR = noiseVal * starScalar;
                    float starG = noiseVal * starScalar;
                    float starB = noiseVal * starScalar;
                    floats[i + 0] = starR;
                    floats[i + 1] = starG;
                    floats[i + 2] = starB;
                }
                else{
                    noiseVal *= .05f;
                    floats[i + 0] = noiseVal;
                    floats[i + 1] = noiseVal;
                    floats[i + 2] = noiseVal;
                    
                    if(aurora){
                        floats[i + 0] += FastMath.pow(rNoise.value(x, y), auroraExp);
                        floats[i + 1] += FastMath.pow(gNoise.value(x, y), auroraExp);
                        floats[i + 2] += FastMath.pow(bNoise.value(x, y), auroraExp);
                    }
//                if(y % 2 == 0) floats[i + 2] = y * invWidth * 1;
                }
                
            }
        }
        //sides
        sky[0] = sky[1] = sky[2] = sky[3] = new Texture2D(new Image(Image.Format.RGB16F, width, width, floatsToRGB16f(floats)));
        
        //top
        int semiWidth = width / 2;
        for(int y = 0; y < width; y++){
            for(int x = 0; x < width; x++){
                int i = (y * width + x) * 3;
                float d = (invWidth * Math.min(Math.min(x, width - x), Math.min(y, width - y)));
                float dcx = invWidth * (semiWidth - x);
                float dcy = invWidth * (semiWidth - y);
                
                float noiseVal = starNoise.value(x, y);
                
                //Aurora
                
                float bNoiseVal = noiseVal * .05f;
                floats[i + 0] = bNoiseVal;
                floats[i + 1] = bNoiseVal;
                floats[i + 2] = bNoiseVal;
                
                if(aurora){
                    floats[i + 0] += FastMath.pow(rNoise.value(x, y), auroraExp);
                    floats[i + 1] += FastMath.pow(gNoise.value(x, y), auroraExp);
                    floats[i + 2] += FastMath.pow(bNoise.value(x, y), auroraExp);
                }
                
                //Moon
                if(dcx * dcx + dcy * dcy < .01f){
                    noiseVal = FastMath.sqrt(noiseVal);
                    floats[i + 0] += noiseVal;
                    floats[i + 1] += noiseVal;
                    floats[i + 2] += noiseVal;
                }
                //Stars
                else if(noiseVal > starLim && FastMath.rand.nextFloat() < starProb){
                    floats[i + 0] = floats[i + 1] = floats[i + 2] = noiseVal * starScalar;
                }
                
            }
        }
        
        sky[4] = new Texture2D(new Image(Image.Format.RGB16F, width, width, floatsToRGB16f(floats)));
        
        //bottom
        for(int y = 0; y < width; y++){
            for(int x = 0; x < width; x++){
                int i = (y * width + x) * 3;
                float d = (invWidth * Math.min(Math.min(x, width - x), Math.min(y, width - y)));
                float dcx = invWidth * (semiWidth - x);
                float dcy = invWidth * (semiWidth - y);
                
                floats[i + 0] = .125f - d * .125f;
                floats[i + 1] = .125f - d * .125f;
                floats[i + 2] = .75f - d * .125f;
            }
        }
        
        sky[5] = new Texture2D(new Image(Image.Format.RGB16F, width, width, floatsToRGB16f(floats)));
        
        
        Spatial skybox = SkyFactory.createSky(assetManager, sky[0], sky[1], sky[2], sky[3], sky[4], sky[5]);
        return skybox;
    }
    
    /*
     * width: width of texture ( > 0 )
     * coronaSize : [0, 1), what fraction of the radius is taken up by an outer (dimmer) corona.
     * coronaIntensity : [0, 1], scalar applied to the corona (the corona is dimmer
     * maxColor : The color of the center of the sun.  Should be very bright ( >> 1), for bloom, and brightness dies off over the image.
     * The outside of the sun is generally about .5 * coronaIntensity as bright as the inner.
     */
    public static Texture2D createSun(int width, float coronaSize, float coronaIntensity, ColorRGBA maxColor){
        float invWidth = 1f / width;
        float invHalfWidth = invWidth * 2f;
        int semiWidth = width / 2;
        
        float[] floats = new float[width * width * 3];
        
        float coronaDLim = 1 - coronaSize;
        
        for(int y = 0; y < width; y++){
            for(int x = 0; x < width; x++){
                float xd = (x - semiWidth) * invHalfWidth;
                float yd = (y - semiWidth) * invHalfWidth;
                
                float d = xd * xd + yd * yd;
                
                if(d > 1){ //quick efficiency check, no need to sqrt > 1 distances, as they are clear;
                    //We would need to 0 this with a reused float buffer.
                    continue;
                }
                
                d = FastMath.sqrt(d);
                
                float intensity = (1 - d);
                if(d > coronaDLim){ //todo optimize out this variable.
                    intensity *= coronaIntensity;
                }
                
                //TODO we do not need translucency, but setting up transparacy would be very good.
                int i = (x + y * width) * 3;
                floats[i + 0] = intensity * maxColor.r;
                floats[i + 1] = intensity * maxColor.g;
                floats[i + 2] = intensity * maxColor.b;
            }
        }
        
        Texture2D sun = new Texture2D(new Image(Image.Format.RGB16F, width, width, floatsToRGB16f(floats)));
        return sun;
    }
    
    public static Texture2D testTex(){
        int width = 16;
        float invWidth = 1f / width;
        float[] data = new float[width * width * 4];
        for(int y = 0; y < width; y++){
            for(int x = 0; x < width; x++){
                int i = (y * width + x) * 4;
                data[i + 0] = x * invWidth;
                data[i + 1] = y * invWidth;
                data[i + 2] = 1 - (x * invWidth);
                data[i + 3] = 1 - (y * invWidth);
            }
        }
        Texture2D testTex = new Texture2D(new Image(Image.Format.RGB16F, width, width, floatsToRGB16f(data)));
        return testTex;
    }
    
    
    
    public static Spatial getFire(){

        ParticleEmitter fire = new ParticleEmitter("Fire", ParticleMesh.Type.Point, 30); //TODO Type.Point
        Material mat_red = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat_red.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));
        fire.setMaterial(mat_red);
        fire.setImagesX(2); fire.setImagesY(2); // 2x2 texture animation
        fire.setEndColor(  new ColorRGBA(1f, 0f, 0f, 1f));   // red
        fire.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)); // yellow
        fire.setStartSize(0.6f);
        fire.setEndSize(0.1f);
        fire.setLowLife(0.5f);
        fire.setHighLife(3f);
        
        ParticleInfluencer influencer = fire.getParticleInfluencer();
        influencer.setInitialVelocity(new Vector3f(0, 2f, 0));
        influencer.setVelocityVariation(1f);
        
        return fire;
    }
    
    public static Texture2D[] bar(int width, int count, ColorRGBA col){
        int halfWidth = width / 2;
        int outerRad = halfWidth;
        int innerRad = halfWidth * 7 / 8;
        
        Texture2D[] barImg = new Texture2D[count];
        int rSqrMax = outerRad * outerRad;
        int rSqrMin = innerRad * innerRad;
        for(int i = 0; i < count; i++){
            ByteBuffer img = BufferUtils.createByteBuffer(width * width * 4);
            byte r = (byte) Math.min(255, (int)(255 * col.r));
            byte g = (byte) Math.min(255, (int)(255 * col.g));
            byte b = (byte) Math.min(255, (int)(255 * col.b));
            float thetaMin = FastMath.TWO_PI - i * FastMath.TWO_PI / count;
            for(int y = 0; y < width; y++){
                for(int x = 0; x < width; x++){
                    byte a = 0;
                    
                    float cx = x - halfWidth;
                    float cy = y - halfWidth;
                    float rSqr = cx * cx + cy * cy;
                        if(rSqr > rSqrMin && rSqr < rSqrMax){
                        float theta = FastMath.atan2(cy, cx);
                        if(theta < 0) theta += FastMath.TWO_PI;
                        if(theta > thetaMin){
                            a = (byte)(col.a * 255.9f);
                        }
                    }
                    img.put(r);
                    img.put(g);
                    img.put(b);
                    img.put(a);
                }
            }
            barImg[i] = new Texture2D(new Image(Image.Format.RGBA8, width, width, img));
        }
        return barImg;
    }
    public static Texture2D hudbg(int width){
        int halfWidth = width / 2;
        float invhWidth = 1f / halfWidth;
        float rSqr = halfWidth * halfWidth;
        
        ByteBuffer img = BufferUtils.createByteBuffer(width * width * 4);
        
        for(int x = 0; x < width; x++){
            for(int y = 0; y < width; y++){
                float cx = x - halfWidth;
                float cy = y - halfWidth;
                float ls = cx * cx + cy * cy;
                if(ls < rSqr){
                    
                    img.put((byte)0);
                    img.put((byte)0);
                    img.put((byte)0);
                    img.put((byte)(FastMath.sqrt(ls) * invhWidth * 255));
                }
                else{
                    img.put((byte)0);
                    img.put((byte)0);
                    img.put((byte)0);
                    img.put((byte)0);
                }
            }
        }
        
        return new Texture2D(new Image(Image.Format.RGBA8, width, width, img));
    }
    
    public static Texture2D solidColor(ColorRGBA col){
        
        ByteBuffer img = BufferUtils.createByteBuffer(3);
        img.put((byte)(col.b * 255.9f));
        img.put((byte)(col.g * 255.9f));
        img.put((byte)(col.r * 255.9f));
        
        return new Texture2D(new Image(Image.Format.BGR8, 1, 1, img));
        
//        ByteBuffer img = BufferUtils.createByteBuffer(4);
//        img.put((byte)(col.r * 255.9));
//        img.put((byte)(col.b * 255.9));
//        img.put((byte)(col.g * 255.9));
//        img.put((byte)(255));
//        
//        return new Texture2D(new Image(Image.Format.RGBA8, 1, 1, img));
    }
    
    public static Material genMatTex(int size, MaterialData matDat){
        float evenness = matDat.getParameter("Evenness");
        float grainSize = matDat.getParameter("Grain Size");
        float impurity = 1 - matDat.getParameter("Purity");
        float r = matDat.getParameter("Color R");
        float g = matDat.getParameter("Color G");
        float b = matDat.getParameter("Color B");
        
        SVF2D difRand = Noise.noiseSVF(size, size, 255 * evenness, 255 * (1 - evenness));
        
        ByteBuffer img = BufferUtils.createByteBuffer(3 * size * size);
        
        for(int y = 0; y < size; y++){
            for(int x = 0; x < size; x++){
                float intensity = difRand.value(x, y);
                
                byte bByte = (byte)(b * intensity);
                byte gByte = (byte)(g * intensity);
                byte rByte = (byte)(r * intensity);
                
                if(impurity > 0){
                    bByte += difRand.value(y, x);
                    bByte += difRand.value(-x, y);
                    bByte += difRand.value(-y, x);
                }
        
                img.put(bByte);
                img.put(gByte);
                img.put(rByte);        
            }
        }
        
        Texture2D diffuse = new Texture2D(new Image(Image.Format.BGR8, size, size, img));
        
        Material mat;
        mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        
        mat.setTexture("DiffuseMap", diffuse);
        
        mat.setFloat("Shininess", matDat.getParameter("Color Specular"));
        
        return mat;
    }
}
