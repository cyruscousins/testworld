/*
 * Contains code to generate models on the fly.
 */

package art;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.influencers.ParticleInfluencer;
import com.jme3.font.BitmapFont;
import com.jme3.light.Light;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.shader.VarType;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import java.nio.FloatBuffer;
import java.util.Random;


import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Format;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.VertexBuffer.Usage;
import com.jme3.scene.control.LodControl;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Torus;
import com.jme3.texture.Texture3D;
import com.jme3.texture.TextureCubeMap;
import com.jme3.util.BufferUtils;
import com.jme3.util.SkyFactory;
import com.jme3.util.TangentBinormalGenerator;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import systems.MaterialData;
import world.WorldStatic;

/**
 *
 * @author Cyrus Cousins
 */
public class Modeller{
    //universal float, turns filters on and off and adjusts polygon depth.
    public static float graphicalQuality = .5f;
    
    //keep track of Materials that need to be updated

    public static BitmapFont font;
    public static AssetManager assetManager;
    public static int screenWidth;

    public static float gQuality = .8f; // [0, 1], used in a variety of graphical quality variables.  
    
    public static void initialize(AssetManager assetManager, BitmapFont font, int screenWidth) {
        Modeller.assetManager = assetManager;
        Modeller.font = font;
        Modeller.screenWidth = screenWidth;
    }

    /*
    public static WorldObject cityBuilding(float swidth, float storyHeight, int stories) {
        Mesh box = new Box(swidth, swidth, stories * storyHeight * .5f);
        box.scaleTextureCoordinates(new Vector2f(stories, 16));
        Geometry model = new Geometry("Building", box); 
        //model.setQueueBucket(Bucket.Transparent); // enables back-to-front sorting



//        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
//        mat.setTexture("DiffuseMap", window);
//        mat.setTexture("SpecularMap", specStreak(128));
//        //mat.setTextureParam(null, VarType.Int, window);
        //mat.setFloat("TexScale", swidth); // 2 windows per story

        model.setMaterial(buildingMaterial(512));
        model.setLocalTranslation(new Vector3f(0, 0, stories * storyHeight * .5f));

        WorldObject building = new WorldObject("Building", model, 0);
        return building;
    }
    */

    /*
    public static WorldObject brickShack(Vector3f normal, boolean roof, float width, float height, float textureScale) {
        Mesh mesh = new Box(width, height, width);//getBuildingMesh(normal, roof, width * .5f, height, width * .5f);
        TangentBinormalGenerator.generate(mesh);
        mesh.scaleTextureCoordinates(new Vector2f(textureScale, textureScale));
        Geometry model = new Geometry("Building", mesh);
       // model.setLocalScale(new Vector3f(width, width, height));
        //if(brickMat == null) brickMat = brick(1024, 8);
        brickMat = brick(256, 8, 16, (1 + FastMath.rand.nextFloat()) * .125f, FastMath.rand.nextBoolean(), FastMath.rand.nextFloat(), FastMath.rand.nextFloat());
        //brickMat.setTexture("DiffuseMap", rainbowTex(1024));
        model.setMaterial(brickMat);
        model.setModelBound(new BoundingBox(new Vector3f(-width * .5f, -width * .5f, 0), new Vector3f(width * .5f, width * .5f, height)));
        //model.setLocalTranslation(new Vector3f(0, 0, height * .5f));

        WorldObject building = new WorldObject("Brick Shack", model, new RigidBodyControl(new BoxCollisionShape(new Vector3f(width * .5f, height, width * .5f)), 0), 0);
        return building;
    }
    */
    
    public static Texture windowTex(int width) {
        //generate a texture
        int xPix = width;
        int yPix = width;
        byte[] diffuse = new byte[xPix * yPix * 3];//type RGB byte
        //create a random number generator.
        Random random = new Random();

        int border = 8;
        for (int x = 0; x < xPix; x++) {
            for (int y = 0; y < yPix; y++) { //gritty background
                if (x % (xPix - border) / border == 0 || y % (yPix - border) / border == 0) {
                    int intensity = random.nextInt(32);
                    diffuse[(x + y * xPix) * 3] = (byte) (intensity);
                    diffuse[(x + y * xPix) * 3 + 1] = (byte) (intensity);
                    diffuse[(x + y * xPix) * 3 + 2] = (byte) (intensity);
                } else {
                    int intensity = 96 + random.nextInt(32);
                    diffuse[(x + y * xPix) * 3] = (byte) (intensity);
                    diffuse[(x + y * xPix) * 3 + 1] = (byte) (intensity);
                    diffuse[(x + y * xPix) * 3 + 2] = (byte) (intensity);
                }
            }
        }

        return new Texture2D(new Image(Image.Format.RGB8, xPix, yPix, com.jme3.util.BufferUtils.createByteBuffer(diffuse)));
    }
    //streaks, for use with specular maps

    public static Material buildingMaterial(int width) {
        int border = 8;
        Random random = FastMath.rand;
        byte[] diffuse = new byte[width * width * 4];
        byte[] specular = new byte[width * width];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < width; y++) { //gritty background
                int intensity = 0;
                if (x % (width - border) / border == 0 || y % (width - border) / border == 0) {
                    intensity = random.nextInt(32);
                    diffuse[(x + y * width) * 4 + 3] = (byte) (255);
                } else {
                    intensity = 96 + random.nextInt(32);
                    specular[(x + y * width)] = (byte)(128 + random.nextInt(64) + (int)((FastMath.sin(x * .1f + FastMath.sin(y * .025f))) * 64));
                    diffuse[(x + y * width) * 4 + 3] = (byte) (128 + random.nextInt(12));
                }
                    diffuse[(x + y * width) * 4] = (byte) (intensity);
                    diffuse[(x + y * width) * 4 + 1] = (byte) (intensity);
                    diffuse[(x + y * width) * 4 + 2] = (byte) (intensity);
                    //specular[(x + y * width)] = specular[(x + y * width) + 1] = specular[(x + y * width) + 2] = (byte)(255);
            }
        }

        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        //mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha); // enables alpha blending mode

        
        Texture2D diffuseMap = new Texture2D(new Image(Image.Format.RGBA8, width, width, com.jme3.util.BufferUtils.createByteBuffer(diffuse)));
        //diffuseMap = rust(128);
        diffuseMap.setWrap(Texture.WrapMode.Repeat);
        mat.setTexture("DiffuseMap", diffuseMap);
        
        Texture2D specularMap = new Texture2D(new Image(Image.Format.Luminance8, width, width, com.jme3.util.BufferUtils.createByteBuffer(specular)));
        specularMap.setWrap(Texture.WrapMode.Repeat);
        mat.setTexture("SpecularMap", specularMap);
        
        TextureCubeMap envMap = nightMap(512, .02f);
        envMap.setWrap(Texture.WrapMode.Repeat);
        mat.setTexture("EnvMap", envMap);
        
        mat.setFloat("Shininess", 100f);
        mat.setVector3("FresnelParams", new Vector3f(0.05f, 0.18f, 0.11f));
        //mat.setFloat("ReflectionPower", 1);//the spec map is raised to that power, the higher this value the more the shiny part will reflect and the less the not shiny part will do
        //mat.setFloat("ReflectionIntensity", 1);
        return mat;
    }

    public static Texture asphault(int width) {
        //generate a texture
        byte[] image = new byte[width * width * 3];//type RGB byte
        //create a random number generator.
        Random random = new Random();

        int stripeWidth = width / 16;
        int stripeLength = width / 4;
        for (int i = 0; i < width * width; i++) {
            byte intensity = (byte) random.nextInt(32);
            image[i * 3] = (byte) (intensity);
            image[i * 3 + 1] = (byte) (intensity);
            image[i * 3 + 2] = (byte) (intensity);
        }
        for (int x = width / 2 - stripeWidth / 2; x < width / 2 + stripeWidth / 2; x++) {
            for (int y = width / 2 - stripeLength / 2; y < width / 2 + stripeLength / 2; y++) {
                byte intensity = (byte) 96;
                image[(x + y * width) * 3] += (intensity);
                image[(x + y * width) * 3 + 1] += intensity;
            }
        }
        return new Texture2D(new Image(Image.Format.RGB8, width, width, com.jme3.util.BufferUtils.createByteBuffer(image)));
    }

    /*
    public static WorldObject chainLinkFence(float width, float height, float heading) {
        Vector3f[] coords = new Vector3f[]{
            new Vector3f(-.5f, 0, 0), new Vector3f(.5f, 0, 0), new Vector3f(.5f, 0, 1), new Vector3f(-.5f, 0, 1)
        };
        Vector2f[] texCoords = new Vector2f[]{
            new Vector2f(0, 0), new Vector2f(1, 0), new Vector2f(0, 1), new Vector2f(1, 1)
        };
        int[] indexes = {0, 1, 3, 1, 2, 3, 3, 1, 0, 3, 2, 1};
        float[] normals = new float[]{0, 1, 0,  0, 1, 0, 0, 1, 0, 0, 1, 0};
        Mesh mesh = new Mesh();

        mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(coords));
        mesh.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(normals));
        mesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoords));
        mesh.setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(indexes));
        mesh.updateBound();

        Geometry geom = new Geometry("Fence", mesh);
        geom.scale(width, 1, height);
        geom.rotate(new Quaternion().fromAngleAxis(heading, new Vector3f(0, 0, 1)));

        //model.move(0, 0, 10);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha); // enables alpha blending mode
        geom.setQueueBucket(Bucket.Transparent); // enables back-to-front sorting

        //mat.setTexture("ColorMap", window);

        mat.setTexture("ColorMap", fenceTexture((int) width * 4, 16));

        //mat.setTextureParam(null, VarType.Int, window);
        //mat.setFloat("TexScale", swidth); // 2 windows per story

        geom.setMaterial(mat);

        WorldObject fence = new WorldObject("Fence", geom, 0);
        return fence;
    }
    */
    
//    public static WorldObject streetLight(float height, Vector3f position) {
//        Node model = new Node();
//
//        Geometry lamp = new Geometry("Streetlight", new Torus(5, 5, 1, 2));
//        lamp.setLocalTranslation(0, 0, 5);
//        ColorRGBA color = ColorRGBA.Yellow;
//
//        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
//        mat.setColor("Color", color);
//        //mat.setTexture("ColorMap", generateGround(4, TERRAIN_LIGHT, 0));
//        //mat.setColor("GlowColor", color);
//        lamp.setMaterial(mat);
//
//        model.attachChild(lamp);
//
//        CollisionShape shape = new CylinderCollisionShape(new Vector3f(.5f, .5f, 5), 2);
//        position.z += 5;
//
//        SpotLight light = new SpotLight();
//        light.setDirection(new Vector3f(0, 0, -1));
//        light.setColor(color);
//        light.setSpotInnerAngle(0);
//        light.setSpotOuterAngle(FastMath.PI * .5f);
//        light.setSpotRange(100f);
//        light.setPosition(position);
//
//
//        WorldObject streetLight = new LightWorldObject(lamp, shape, 0, light);
//        streetLight.body.setPhysicsLocation(position);
//        return streetLight;
//    }

    public static Texture2D fenceTexture(int width, int linkSize) {
        //generate a texture
        byte[] image = new byte[width * width * 4];//type RGBA byte
        //create a random number generator.
        float semiLink = linkSize * .5f;
        Random random = new Random();
        int c = width * linkSize;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < width; y++) {
                float a = Math.max((c + x - y) % linkSize, (c + x + y) % linkSize);
                a -= semiLink;
                a /= semiLink;
                a *= a;
                if (a > .75) {
                    int i = (x + y * width) * 4;
                    int intensity = random.nextInt(32);
                    image[i] = (byte) (intensity * 4);
                    image[i + 1] = (byte) (intensity * 3);
                    image[i + 2] = (byte) (intensity * 2);
                    image[i + 3] = (byte) (256 * a);
                }
            }
        }
//        int stripeWidth = width / 16;
//        int stripeLength = width / 4;
//        for (int i = 0; i < width * width; i++){
//            byte intensity = (byte)random.nextInt(32);
//                            image[i * 3] = (byte) (intensity);
//                            image[i * 3 + 1] = (byte) (intensity);
//                            image[i * 3 + 2] = (byte) (intensity);            
//        }
//        for (int x = width / 2 - stripeWidth / 2; x < width / 2 + stripeWidth / 2; x++){
//            for (int y = width / 2 - stripeLength / 2; y < width / 2 + stripeLength / 2; y++){
//                byte intensity = (byte) 96;
//                image[(x + y * width) * 3] += (intensity);
//                image[(x + y * width) * 3 + 1] += intensity;                
//            }            
//        }
        return new Texture2D(new Image(Image.Format.RGBA8, width, width, com.jme3.util.BufferUtils.createByteBuffer(image)));

    }
    private static Mesh barrels;

//    public static WorldObject getLODBarrel(float radius, float height) {
//        if (barrels == null) {
//            barrels = barrelMesh(6);
//        }
//        Geometry geom = new Geometry("Barrel", barrels);
//
//        LodControl control = new LodControl();
//        control.setTrisPerPixel(.001f);
//        //geom.addControl(control);
//
//        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
//
//        //mat.setTexture("ColorMap", window);
//
//        mat.setTexture("ColorMap", rust((int) radius * 128));
//
//        geom.setMaterial(mat);
//        geom.setLocalScale(radius, radius, height);
//
//        CollisionShape shape = new CylinderCollisionShape(new Vector3f(radius, radius, height), 2);
//
//
//        return new WorldObject("LOD Barrel", geom, new RigidBodyControl(shape, 0), 0);
//    }

    private static Mesh barrelMesh(int detailLevels) {
        int radialSamples = 3 * (1 << (detailLevels - 1));
        //precalculate some stuff
        float circFrac = 1f / (radialSamples);
        float radPerVert = FastMath.TWO_PI * circFrac;

        //set up arrays for buffers
        Vector3f[] coords = new Vector3f[radialSamples * 2 + 2];
        //Vector3f[] normals = new Vector3f[radialSamples * 2 + 2];
        float[] normals = new float[(radialSamples * 2 + 2) * 3];
        Vector2f[] texCoords = new Vector2f[radialSamples * 2 + 2];
        for (int i = 0; i <= radialSamples; i++) {
            float x = FastMath.cos(radPerVert * i);
            float y = FastMath.sin(radPerVert * i);
            coords[i * 2] = new Vector3f(x, y, 0);
            coords[i * 2 + 1] = new Vector3f(x, y, 1);
            //normals[i * 2] = normals[i * 2 + 1] = new Vector3f(x, y, 0);
            normals[i * 6] = normals [i * 6 + 3] = x;
            normals[i * 6 + 1] = normals[i * 6 + 4] = y;
            texCoords[i * 2] = new Vector2f(i * circFrac, 0);
            texCoords[i * 2 + 1] = new Vector2f(i * circFrac, 1);
        }
        VertexBuffer[] lods = new VertexBuffer[detailLevels];
        for(int i = 0; i < detailLevels; i++){
            int levelSamples = 3 * (1 << (detailLevels - 1 - i));
            int factor = 1 << i;
            int[] indexes = new int[12 * levelSamples];
            for (int j = 0; j < levelSamples; j++) {
                //outer
                indexes[j * 12    ] = (j * 2 + 2) * factor;
                indexes[j * 12 + 1] = (j * 2    ) * factor + 1;
                indexes[j * 12 + 2] = (j * 2    ) * factor;
                indexes[j * 12 + 3] = (j * 2 + 2) * factor + 1;
                indexes[j * 12 + 4] = (j * 2    ) * factor + 1;
                indexes[j * 12 + 5] = (j * 2 + 2) * factor;

                //inner
                indexes[j * 12 +  6] = (j * 2    ) * factor;
                indexes[j * 12 +  7] = (j * 2    ) * factor + 1;
                indexes[j * 12 +  8] = (j * 2 + 2) * factor;
                indexes[j * 12 +  9] = (j * 2 + 2) * factor;
                indexes[j * 12 + 10] = (j * 2    ) * factor + 1;
                indexes[j * 12 + 11] = (j * 2 + 2) * factor + 1;
            }
            lods[i] = new VertexBuffer(Type.Index);
            lods[i].setupData(Usage.Static, 1, Format.UnsignedInt, BufferUtils.createIntBuffer(indexes));
        }
        Mesh mesh = new Mesh();
        
        mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(coords));
        
        mesh.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(normals));
        //mesh.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(normals));
        
        mesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoords));
        mesh.setBuffer(Type.Index, 1, (IntBuffer)lods[detailLevels - 1].getData());
        mesh.updateBound();
        
        //TangentBinormalGenerator.generate(mesh, true);
        mesh.setLodLevels(lods);

        return mesh;
    }

//    public static WorldObject burningBarrel(float radius, float height) {
//        if (barrels == null) {
//            barrels = barrelMesh(6);
//        }
//        
//        Geometry geom = new Geometry("Barrel", barrels);
//
//        LodControl control = new LodControl();
//        control.setTrisPerPixel(.001f);
//        geom.addControl(control);
//
//        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
//
//        //mat.setTexture("ColorMap", window);
//
//        mat.setTexture("DiffuseMap", rust((int) 128));
//
//        geom.setMaterial(mat);
//        geom.setLocalScale(radius, radius, height);
//
////        SpotLight light = new SpotLight();
////        light.setDirection(new Vector3f(0, 0, -1));
////        light.setColor(ColorRGBA.Yellow.mult(3));
////        light.setSpotInnerAngle(FastMath.PI * .5f);
////        light.setSpotOuterAngle(FastMath.PI * .9f);
////        light.setSpotRange(100f);
////        light.setPosition(new Vector3f(0, 0, height));
//        PointLight light = new PointLight();
//        light.setColor(new ColorRGBA(1.2f, 1.0f, .3f, 1).mult(4));
//        light.setRadius(10f);
//        light.setPosition(new Vector3f(0, 0, height));
//        
//        CollisionShape shape = new CylinderCollisionShape(new Vector3f(radius, radius, height), 2);
//
//        Node node = new Node();
//
//        node.attachChild(geom);
//
//        /** Uses Texture from jme3-test-data library! */
//        ParticleEmitter fire = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
//        Material mat_red = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
//        mat_red.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));
//        fire.setMaterial(mat_red);
//        fire.setImagesX(2);
//        fire.setImagesY(2); // 2x2 texture animation
//        fire.setEndColor(new ColorRGBA(1f, 0f, 0f, 1f));   // red
//        fire.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)); // yellow
//        fire.setLocalTranslation(new Vector3f(0, 0, height));
//        ParticleInfluencer fireInfluencer = fire.getParticleInfluencer();
//        fireInfluencer.setInitialVelocity(new Vector3f(0, 0, 2));
//        fire.setStartSize(0.6f * radius);
//        fire.setEndSize(0.1f * radius);
//        //fireInfluencer.setGravity(0);
//        fire.setLowLife(0.5f);
//        fire.setHighLife(3f);
//        fireInfluencer.setVelocityVariation(0.3f);
//        node.attachChild(fire);
//
//
//        WorldObject barrel = new LightWorldObject(node, shape, 0, light);
//        return barrel;
//
//    }

    public static Texture2D rust(int width) {

        byte[] image = new byte[width * width * 3];//type RGB byte
        //create a random number generator.
        Random random = new Random();
        for (int i = 0; i < width * width; i++) {
            int intensity = random.nextInt(64);
            image[i * 3    ] = (byte) (intensity * 4);
            image[i * 3 + 1] = (byte) (intensity * 3);
            image[i * 3 + 2] = (byte) (intensity * 2);
        }

        return new Texture2D(new Image(Image.Format.RGB8, width, width, com.jme3.util.BufferUtils.createByteBuffer(image)));
    }
    public static Texture2D treeTex2D(int width){
        
        
        byte[] image = new byte[width * width * 3];//type RGB byte
        //create a random number generator.
        Random random = FastMath.rand; //might as well reuse an existing Random object.
        //we want pseudorandomly distributed continuous vertical stripes.  
        for(int x = 0; x < width; x++){
            int cycles = random.nextInt(16) + 1;
            float trigConstant = FastMath.TWO_PI / cycles;
            //float xFactor = random.nextFloat() * .5f - .25f;
            for(int y = 0; y < width; y++){
                int intensity = (int)(8 * (random.nextFloat() + 6 + fastSine(y * trigConstant))); //[64, 64]
                int i = x + y * width;
                image[i * 3    ] = (byte) (intensity * 4);
                image[i * 3 + 1] = (byte) (intensity * 3);
                image[i * 3 + 2] = (byte) (intensity * 2);
            }
        }
        return new Texture2D(new Image(Image.Format.RGB8, width, width, com.jme3.util.BufferUtils.createByteBuffer(image)));
    }
    public static Texture2D noise2Tex(){
        if(noise3 == null) noise3 = genNoise3(noiseWidth, .75f, 16, .1f);
        byte[] raw = noise3;//getNoise2();
        ByteBuffer bb = com.jme3.util.BufferUtils.createByteBuffer(noiseWidth * noiseWidth * 4);
        byte one = (byte) 1;
        for(int i = 0; i < noiseWidth * noiseWidth; i++){
            bb.put(raw[i]);
            bb.put(raw[i]);
            bb.put(raw[i]);
            bb.put(one);
        }
        return new Texture2D(new Image(Image.Format.RGBA8, noiseWidth, noiseWidth, bb));
    }
    
    //maps a noisemap to colors, r = 0, g = 128, b = 255, interpolate.
    public static Texture2D rainbowTex(int width){
        byte[] noise = genNoise2(width, 0, 6f, 8, 0);
        byte[] tex = new byte[width * width * 3];
        for(int y = 0; y < width; y++){
            int yBase = y * width;
            for(int x = 0; x < width; x++){
                int i = (yBase + x); //now i is for the noise function, which is of one color
                byte r = (byte) (255 - 2 * noise[i]);
                if(r < 0) r = 0;
                byte b = (byte) (noise[i] * 2 - 255);
                if(b < 0) b = 0;
                byte g = (byte) (255 - r - b);
                i *= 3; //now for the three color output
                tex[i] = r;
                tex[i + 1] = g;
                tex[i + 2] = b;
            }
        }
        return new Texture2D(new Image(Image.Format.RGB8, width, width, BufferUtils.createByteBuffer(tex)));
    }
    
    public static Mesh building; //Roofless building, texture mapped and oriented for a z up orientation
    public static float[] brickNormalsNoRoof = new float[]{
                    0,-1,0, 0,-1,0, 0,-1,0, 0,-1,0,
                    -1,0,0, -1,0,0, -1,0,0, -1,0,0,
                    0,1,0,  0,1,0,  0,1,0,  0,1,0,
                    1,0,0,  1,0,0,  1,0,0,  1,0,0
    };
    public static float[] brickNormalsRoof = new float[]{
                    0,0,-1, 0,0,-1, 0,0,-1, 0,-1,0,
                    -1,0,0, -1,0,0, -1,0,0, -1,0,0,
                    0,0,1,  0,0,1,  0,0,1,  0,1,0,
                    1,0,0,  1,0,0,  1,0,0,  1,0,0,
                    0,1,0,  0,1,0,  0,1,0,  0,1,0
    };
    public static Mesh getBuildingMesh(Vector3f normal, boolean roof, float xHalf, float height, float zHalf){
        Mesh mesh = new Mesh();
        Vector3f[] coords = null;
        Vector2f[] texCoords = null;
        if(roof){
            coords = new Vector3f[]{
                new Vector3f(-xHalf, 0, -zHalf), new Vector3f(-xHalf, height, -zHalf),
                new Vector3f(xHalf, 0, -zHalf),  new Vector3f(xHalf, height, -zHalf),
                new Vector3f(xHalf, 0, -zHalf),  new Vector3f(xHalf, height, -zHalf),
                new Vector3f(xHalf, 0, zHalf),   new Vector3f(xHalf, height, zHalf),
                new Vector3f(xHalf, 0, zHalf),   new Vector3f(xHalf, height, zHalf),
                new Vector3f(-xHalf, 0, zHalf),  new Vector3f(-xHalf, height, zHalf),
                new Vector3f(-xHalf, 0, zHalf),  new Vector3f(-xHalf, height, zHalf),
                new Vector3f(-xHalf, 0, -zHalf), new Vector3f(-xHalf, height, -zHalf),//wrap around
                //and now the roof
                new Vector3f(-xHalf, height, -zHalf),
                new Vector3f(-xHalf, height, zHalf),
                new Vector3f(xHalf, height, zHalf),
                new Vector3f(xHalf, height, -zHalf)
            };
            texCoords = new Vector2f[]{
                new Vector2f(0, 0), new Vector2f(0, 1),
                new Vector2f(1, 0), new Vector2f(1, 1),
                new Vector2f(1, 0), new Vector2f(1, 1),
                new Vector2f(0, 0), new Vector2f(0, 1),
                new Vector2f(0, 0), new Vector2f(0, 1),
                new Vector2f(1, 0), new Vector2f(1, 1),
                new Vector2f(1, 0), new Vector2f(1, 1),
                new Vector2f(0, 0), new Vector2f(0, 1),//wrap around
                //and now the roof
                new Vector2f(0, 0), 
                new Vector2f(1, 0), 
                new Vector2f(1, 1), 
                new Vector2f(0, 1)
            };
        }
        else{
            coords = new Vector3f[]{
                new Vector3f(-xHalf, 0, -zHalf), new Vector3f(-xHalf, height, -zHalf),
                new Vector3f(xHalf, 0, -zHalf),  new Vector3f(xHalf, height, -zHalf),
                new Vector3f(xHalf, 0, -zHalf),  new Vector3f(xHalf, height, -zHalf),
                new Vector3f(xHalf, 0, zHalf),   new Vector3f(xHalf, height, zHalf),
                new Vector3f(xHalf, 0, zHalf),   new Vector3f(xHalf, height, zHalf),
                new Vector3f(-xHalf, 0, zHalf),  new Vector3f(-xHalf, height, zHalf),
                new Vector3f(-xHalf, 0, zHalf),  new Vector3f(-xHalf, height, zHalf),
                new Vector3f(-xHalf, 0, -zHalf), new Vector3f(-xHalf, height, -zHalf)//wrap around
            };
            texCoords = new Vector2f[]{
                new Vector2f(0, 0), new Vector2f(0, 1),
                new Vector2f(1, 0), new Vector2f(1, 1),
                new Vector2f(1, 0), new Vector2f(1, 1),
                new Vector2f(0, 0), new Vector2f(0, 1),
                new Vector2f(0, 0), new Vector2f(0, 1),
                new Vector2f(1, 0), new Vector2f(1, 1),
                new Vector2f(1, 0), new Vector2f(1, 1),
                new Vector2f(0, 0), new Vector2f(0, 1)//wrap around
            };
        }
            float dydx = -normal.x / normal.y;
            float dydz = -normal.z / normal.y;
            float invHeight = 1f / height;
//            for(int i = 0; i < 16; i+= 2){
//                float heightChange = coords[i].x * dydx + coords[i].z * dydz;
//                coords[i].y += heightChange;
//                texCoords[i].y -= heightChange * invHeight;
//            }
            int[] indexes = new int[coords.length * 3 / 2];
            for(int i = 0; i < 4; i++){
                indexes[i * 6    ] = i * 4 + 1;
                indexes[i * 6 + 1] = i * 4 + 2;
                indexes[i * 6 + 2] = i * 4;
                indexes[i * 6 + 3] = i * 4 + 1;
                indexes[i * 6 + 4] = i * 4 + 3;
                indexes[i * 6 + 5] = i * 4 + 2;
            }
            mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(coords));
            mesh.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(roof ? brickNormalsRoof : brickNormalsNoRoof));
            mesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoords));
            mesh.setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(indexes));
            mesh.updateBound();
            //TangentBinormalGenerator.generate(mesh, true);
        return mesh;
    }
    public static Mesh sphere;
    public static Mesh getSphere(){
        if (sphere == null){
            sphere = new Sphere(8, 8, 1); //geosphere must be rewritten.  
            TangentBinormalGenerator.generate(sphere, true);
        }
        return sphere;
    }
    /**
     * The beginning of the code for the LODTree
     */

    /*
    public static WorldObject getLODTree(float radius, float height) {
        if (tree == null) {
            tree = simpleTreeMesh(6);
        }
        Geometry geom = new Geometry("Tree", tree);

        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");


        //mat.setTexture("ColorMap", window);

        mat.setTexture("DiffuseMap", rust((int) radius * 128));

        geom.setMaterial(mat);
        geom.setLocalScale(radius, height, radius);
        geom.setModelBound(new BoundingBox(new Vector3f(-radius, -radius, 0), new Vector3f(radius, radius, height)));
        
//        LodControl control = new LodControl();
//        control.setTrisPerPixel(.5f);

        //geom.addControl(new LODWorldObjectControl(Main.camera, 100000f / (screenWidth * radius)));

        CollisionShape shape = new CylinderCollisionShape(new Vector3f(radius, radius, height), 2);
//        Material leafMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
//        leafMat.setTexture("ColorMap", leaves((int) (radius * 16)));
//        leafMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha); // enables alpha blending mode

        WorldObject tree = new WorldObject("LODTree", geom, new RigidBodyControl(shape, 0), 0);
        tree.setLodLevel(6);
        tree.setLodData(6, radius, 512 / (radius * screenWidth) * graphicalQuality);

        return tree;
    }
    public static WorldObject getComplexTree(int detail, Vector3f groundNormal, float radius, float height, float curvature){
        Mesh mesh = complexTreeMesh(detail, groundNormal, radius, height, curvature);
        //mesh.setMode(Mesh.Mode.LineStrip);
        //mesh.setLineWidth(1.5f);
        
        Geometry geom = new Geometry("Complex Tree", mesh);

        //Material should be predefined, a 3D texture for all trees of a type.  
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");

        mat.setTexture("DiffuseMap", treeTex2D(128));//treeTex2D((int) radius * 128));

        geom.setMaterial(mat);
        geom.setModelBound(new BoundingBox(new Vector3f(-radius, 0, -radius), new Vector3f(radius, height, radius)));
        
//        LodControl control = new LodControl();
//        control.setTrisPerPixel(.5f);

        //geom.addControl(new LODWorldObjectControl(Main.camera, 100000f / (screenWidth * radius)));

        CollisionShape shape = new CylinderCollisionShape(new Vector3f(radius, radius, height), 1);
//        Material leafMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
//        leafMat.setTexture("ColorMap", leaves((int) (radius * 16)));
//        leafMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha); // enables alpha blending mode

        WorldObject tree = new WorldObject("Complex Tree", geom, new RigidBodyControl(shape, 0), 0);
        tree.setLodLevel(detail);
        tree.setLodData(detail, radius, 128 / (radius * screenWidth));
        
        geom = new Geometry("leaves", getLeaves(radius * 2, .5f, groundNormal.mult(height)));
        geom.setModelBound(new BoundingBox(new Vector3f(-radius, 0, -radius), new Vector3f(radius, height, radius)));
        //geom.setQueueBucket(Bucket.Translucent);
        mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setTexture("DiffuseMap", leaves(128));//treeTex2D((int) radius * 128));
        mat.setBoolean("UseAlpha",true);
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        //mat.getAdditionalRenderState().setDepthWrite(false);
        
        geom.setMaterial(mat);
        geom.setQueueBucket(Bucket.Transparent);
        tree.attachChild(geom);

        return tree;
    }
    */

    public static Mesh cactusMesh;
    public static WorldStatic getCactus(float radius, float height){
        if (cactusMesh == null){
            cactusMesh = cactusMesh(2);
        }

//        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
//
//        mat.setTexture("DiffuseMap", rust((int) radius * 128));

        CollisionShape shape = new CylinderCollisionShape(new Vector3f(radius, height, radius), 1);
        MaterialData matDat = MaterialData.getRandomMaterial("Wood");
        
        WorldStatic cactus = new WorldStatic("Cactus", cactusMesh, Texturer.genMatTex(128, matDat), new RigidBodyControl(shape, 0), matDat);
//        WorldStatic cactus = new WorldStatic("Cactus", cactusMesh, cactusMat(), new RigidBodyControl(shape, 0), MaterialData.getRandomMaterial("Wood"));
//        WorldStatic cactus = new WorldStatic("Cactus", cactusMesh, Texturer.wireframe(), new RigidBodyControl(shape, 0), MaterialData.getRandomMaterial("Wood"));

        cactus.scale(radius, height, radius);
        
        return cactus;
    }
    public static WorldStatic getSpire(MaterialData material, float radius, float height){

        Mesh mesh = spireMesh(3, 1.5f, 4);
        CollisionShape shape = new CylinderCollisionShape(new Vector3f(radius, height, radius), 1);

        WorldStatic spire = new WorldStatic("Spire", mesh, Texturer.genMatTex(128, material), new RigidBodyControl(shape, 0), material);
        spire.scale(radius, height, radius);
        
        return spire;
    }
    
    public static Mesh cactusMesh(int maxLOD){
        int radialSamples = 3 * 1 << maxLOD;
        int heightSamples = 1 << maxLOD;

        float radsPerSample = FastMath.TWO_PI / radialSamples;
        float heightPerSample = 1f / heightSamples;
        
        int rPtsCt = radialSamples + 1; //1 extra for joining edge.
        int hPtsCt = heightSamples + 1; //1 extra for top pt.


        int pointCount = rPtsCt * hPtsCt;
        float[] coords    = new float[3 * pointCount];
        float[] texCoords = new float[2 * pointCount];
        
        for (int rSamp = 0; rSamp < rPtsCt; rSamp++) {//+1 for clarity and continuity.
            //minimize trig calculations by putting them in the outer loop.
            float theta = rSamp * radsPerSample;
            float cos = FastMath.cos(theta);    
            float sin = FastMath.sin(theta);
            for (int hSamp = 0; hSamp < hPtsCt; hSamp++) {
                float height = hSamp * heightPerSample;
                float radius = (.75f + .25f * Math.abs(FastMath.sin(4 * theta))) * (.5f + height * .5f - height * height);
                
                int i = (rSamp + hSamp * rPtsCt);
                int index = i * 3;
                int texIndex = i * 2;

                coords   [index + 0] = radius * cos;
                coords   [index + 1] = height;
                coords   [index + 2] = radius * sin;
                
                texCoords[texIndex + 0] = theta / FastMath.TWO_PI;
                texCoords[texIndex + 1] = height;
            }
        }
        
        float[] normals = calcNormalsFromStandardSurface(coords, rPtsCt, hPtsCt);
        
//        int[] indices = new int[radialSamples * heightSamples * 3 * 2];
//        for (int r = 0; r < radialSamples; r++) {
//            for (int h = 0; h < heightSamples; h++) {
//                int index = (r + h * radialSamples) * 6;
//                indices[index + 2] = r + h * rPtsCt;
//                indices[index + 1] = r + h * rPtsCt + 1;
//                indices[index + 0] = r + h * rPtsCt + rPtsCt;
//
//                indices[index + 5] = r + h * rPtsCt + 1;
//                indices[index + 4] = r + h * rPtsCt + 1 + rPtsCt;
//                indices[index + 3] = r + h * rPtsCt + rPtsCt;
//                if(indices[index + 4] >= pointCount){
//                    System.err.println("Index " + indices[index + 4] + " exceeds max of " + pointCount + " at r: " + r + " / " + radialSamples + ", h " + h + " / " + heightSamples);
//                    System.exit(1);
//                }
//            }
//        }
//        for(int i = 0; i < indices.length; i++){
//            if(i % 3 == 0) System.out.print("\t");
//            System.out.print(indices[i]+ " ");
//        }

        Mesh mesh = generateStandardMesh(coords, texCoords, rPtsCt, hPtsCt, maxLOD);

        
        return mesh;
    }

    public static Mesh spireMesh(int divisions, float twistedness, int maxLOD){
        int radialSamples = 3 * 1 << maxLOD;
        int heightSamples = 1 << maxLOD;

        float radsPerSample = FastMath.TWO_PI / radialSamples;
        float heightPerSample = 1f / heightSamples;
        
        int rPtsCt = radialSamples + 1; //1 extra for joining edge.
        int hPtsCt = heightSamples + 1; //1 extra for top pt.


        int pointCount = rPtsCt * hPtsCt;
        float[] coords    = new float[3 * pointCount];
        float[] texCoords = new float[2 * pointCount];
        
        float twist = twistedness * FastMath.TWO_PI;
        
        for (int rSamp = 0; rSamp < rPtsCt; rSamp++) {//+1 for clarity and continuity.
            //minimize trig calculations by putting them in the outer loop.
            float theta = rSamp * radsPerSample;
            float cos = FastMath.cos(theta);    
            float sin = FastMath.sin(theta);
            
            for (int hSamp = 0; hSamp < hPtsCt; hSamp++) {
                float height = hSamp * heightPerSample;
                float radius = (.5f + .5f * Math.abs(FastMath.sin(divisions * theta + height * twist))) * (.5f + height * .5f - height * height);
                
                int i = (rSamp + hSamp * rPtsCt);
                int index = i * 3;
                int texIndex = i * 2;

                coords   [index + 0] = radius * cos;
                coords   [index + 1] = height;
                coords   [index + 2] = radius * sin;
                
                texCoords[texIndex + 0] = theta / FastMath.TWO_PI;
                texCoords[texIndex + 1] = height;
            }
        }
        
        float[] normals = calcNormalsFromStandardSurface(coords, rPtsCt, hPtsCt);
        
//        int[] indices = new int[radialSamples * heightSamples * 3 * 2];
//        for (int r = 0; r < radialSamples; r++) {
//            for (int h = 0; h < heightSamples; h++) {
//                int index = (r + h * radialSamples) * 6;
//                indices[index + 2] = r + h * rPtsCt;
//                indices[index + 1] = r + h * rPtsCt + 1;
//                indices[index + 0] = r + h * rPtsCt + rPtsCt;
//
//                indices[index + 5] = r + h * rPtsCt + 1;
//                indices[index + 4] = r + h * rPtsCt + 1 + rPtsCt;
//                indices[index + 3] = r + h * rPtsCt + rPtsCt;
//                if(indices[index + 4] >= pointCount){
//                    System.err.println("Index " + indices[index + 4] + " exceeds max of " + pointCount + " at r: " + r + " / " + radialSamples + ", h " + h + " / " + heightSamples);
//                    System.exit(1);
//                }
//            }
//        }
//        for(int i = 0; i < indices.length; i++){
//            if(i % 3 == 0) System.out.print("\t");
//            System.out.print(indices[i]+ " ");
//        }

        Mesh mesh = generateStandardMesh(coords, texCoords, rPtsCt, hPtsCt, maxLOD);

        
        return mesh;
    }

    //Given coordinates and texture coordinates, generate a mesh with normals and lods for a surface of 2 variables, divided into rPtsCt by hPtsCt sections.
    public static Mesh generateStandardMesh(float[] points, float[] texCoords, int rPtsCt, int hPtsCt, int maxLOD){
        Mesh mesh = new Mesh();
        
        int radialSamples = rPtsCt - 1;
        int heightSamples = hPtsCt - 1;
        
        VertexBuffer[] lods = new VertexBuffer[maxLOD];
        for (int i = 0; i < maxLOD; i++) {
            int stepSize = 1 << i;
            int[] indices = new int[radialSamples * heightSamples * 6];
            for (int r = 0; r < radialSamples / stepSize; r++) {
                for (int h = 0; h < heightSamples / stepSize; h++) { 
                    
                    int index = (r + h * radialSamples) * 6;

                    indices[index + 0] = (r + h * rPtsCt + 1) * stepSize;
                    indices[index + 1] = (r + h * rPtsCt) * stepSize;
                    indices[index + 2] = (r + h * rPtsCt + rPtsCt) * stepSize;

                    indices[index + 3] = (r + h * rPtsCt + 1 + rPtsCt) * stepSize;
                    indices[index + 4] = (r + h * rPtsCt + 1) * stepSize;
                    indices[index + 5] = (r + h * rPtsCt + rPtsCt) * stepSize;
                }
            }
            
            IntBuffer data = BufferUtils.createIntBuffer(indices);
            
            lods[i] = new VertexBuffer(Type.Index);
            lods[i].setupData(Usage.Static, 1, Format.UnsignedInt, data);
            if(i == 0){        
                mesh.setBuffer(Type.Index, 1, data); //TODO reuse above buffer
            }
        }
                        
        mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(points));
        mesh.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(calcNormalsFromStandardSurface(points, rPtsCt, hPtsCt)));

        mesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoords));
        
        mesh.setLodLevels(lods);

        mesh.updateBound();
        TangentBinormalGenerator.generate(mesh, false);
        
        return mesh;
    }
    public static float[] calcNormalsFromStandardSurface(float[] pts, int rSamps, int hSamps){
        float[] norms = new float[pts.length];
        
        int maxR = rSamps - 1;
        int maxH = hSamps - 1;
        
        //CENTER
        for(int r = 0; r < rSamps; r++)
            for(int h = 0; h < hSamps; h++){
                int ptIndex = (r + h * rSamps);
                int index = ptIndex * 3;
                
                //Cross Product.
                int lft = ptIndex - 1;
                int rgt = ptIndex + 1;
                int upp = ptIndex + rSamps;
                int dwn = ptIndex - rSamps;
                
                if(r == 0){
                    lft = ptIndex;
                }
                else if(r == maxR){
                    rgt = ptIndex;
                }
                
                if(h == 0){
                    dwn = ptIndex;
                }
                else if(h == maxH){
                    upp = ptIndex;
                }
                        
                norms[index + 0] = -
                        (pts[lft * 3 + 1] - pts[rgt * 3 + 1]) * //y1
                        (pts[dwn * 3 + 2] - pts[upp * 3 + 2]) - //z2
                        (pts[lft * 3 + 2] - pts[rgt * 3 + 2]) * //z1
                        (pts[dwn * 3 + 1] - pts[upp * 3 + 1]) ; //y2
                
                norms[index + 1] = 
                        (pts[lft * 3 + 0] - pts[rgt * 3 + 0]) * //x1
                        (pts[dwn * 3 + 2] - pts[upp * 3 + 2]) - //z2
                        (pts[lft * 3 + 2] - pts[rgt * 3 + 2]) * //z1
                        (pts[dwn * 3 + 0] - pts[upp * 3 + 0]) ; //x2
                
                norms[index + 2] = -
                        (pts[lft * 3 + 0] - pts[rgt * 3 + 0]) * //x1
                        (pts[dwn * 3 + 1] - pts[upp * 3 + 1]) - //y2
                        (pts[lft * 3 + 1] - pts[rgt * 3 + 1]) * //y1
                        (pts[dwn * 3 + 0] - pts[upp * 3 + 0]) ; //x2
            
                float scalar = FastMath.invSqrt(norms[index] * norms[index] + norms[index + 1] * norms[index + 1] + norms[index + 2] * norms[index + 2]);
                norms[index + 0] *= scalar;
                norms[index + 1] *= scalar;
                norms[index + 2] *= scalar;
                
            }
        return norms;
    }
    
    public static void main(String[] args){
        cactusMesh(1);
        
        
    }
    
    
    
    /*
    public static Mesh cactusMesh(int maxLOD){
        int radialSamples = 4 * 1 << maxLOD;
        int heightSamples = 1 << maxLOD;

        float radsPerSample = FastMath.TWO_PI / radialSamples;
        float heightPerSample = 1f / heightSamples;
        
        radialSamples++; //2pi
        heightSamples++; //top


        int pointCount = radialSamples * heightSamples;
        Vector3f[] coords = new Vector3f[pointCount];//radialSamples are +1 so that it wraps around.
        Vector3f[] normals = new Vector3f[pointCount];
        Vector2f[] texCoords = new Vector2f[pointCount];
        for (int rSamp = 0; rSamp < radialSamples; rSamp++) {//+1 for clarity and continuity.
            //minimize trig calculations by putting them in the outer loop.
            float theta = rSamp * radsPerSample;
            float cos = fastCosine(theta);    
            float sin = fastSine(theta);
            for (int hSamp = 0; hSamp < heightSamples; hSamp++) {
                float height = hSamp * heightPerSample;
                //r(h, theta) = (.5 + abs(sin(16 * pi * theta))) * (.5 + h * .5f - h * h)
                float radius = (.75f + .25f * Math.abs(FastMath.sin(4 * theta))) * (.5f + height * .5f - height * height);
                int index = rSamp + hSamp * radialSamples;
                coords   [index] = new Vector3f(radius * cos, height, radius * sin);
                normals  [index] = new Vector3f(cos, 0, sin);
                texCoords[index] = new Vector2f(theta / FastMath.TWO_PI, height);
            }
        }
        int[] indexes = new int[radialSamples * heightSamples * 3 * 2];
        for (int r = 0; r < radialSamples; r++) {
            for (int h = 0; h < heightSamples; h++) {
                int index = (r + h * radialSamples) * 6;
                indexes[index] = r + h * radialSamples;
                indexes[index + 1] = r + h * radialSamples + 1;
                indexes[index + 2] = r + h * radialSamples + radialSamples;

                indexes[index + 3] = r + h * radialSamples + 1;
                indexes[index + 4] = r + h * radialSamples + 1 + radialSamples;
                indexes[index + 5] = r + h * radialSamples + radialSamples;
            }
        }

        Mesh mesh = new Mesh();

        mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(coords));
        mesh.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(normals));
        mesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoords));
        mesh.setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(indexes));

        VertexBuffer[] lods = new VertexBuffer[maxLOD];
        for (int i = 0; i < maxLOD; i++) {
            int stepSize = 1 << i;
            int[] indices = new int[(radialSamples - 1) * (heightSamples - 1) * 6];
            for (int r = 0; r < (radialSamples - 1) / stepSize; r++) {
                for (int h = 0; h < (heightSamples - 1) / stepSize; h++) { 
                    
                    int index = (r + h * radialSamples) * 6;

                    indices[(r + h * (radialSamples - 1)) * 6] = (r + h * (radialSamples - 1)) * stepSize;
                    indices[(r + h * (radialSamples - 1)) * 6 + 1] = (r + h * (radialSamples - 1) + 1) * stepSize;
                    indices[(r + h * (radialSamples - 1)) * 6 + 2] = (r + h * (radialSamples - 1) + (radialSamples - 1)) * stepSize;

                    indices[(r + h * (radialSamples - 1)) * 6 + 3] = (r + h * (radialSamples - 1) + 1) * stepSize;
                    indices[(r + h * (radialSamples - 1)) * 6 + 4] = (r + h * (radialSamples - 1) + 1 + (radialSamples - 1)) * stepSize;
                    indices[(r + h * (radialSamples - 1)) * 6 + 5] = (r + h * (radialSamples - 1) + (radialSamples - 1)) * stepSize;
                }
            }
            lods[i] = new VertexBuffer(Type.Index);
            lods[i].setupData(Usage.Static, 1, Format.UnsignedInt, BufferUtils.createIntBuffer(indices));
        }

//        mesh.setLodLevels(lods);
        mesh.updateBound();
        TangentBinormalGenerator.generate(mesh);

        return mesh;
    }
     */
    public static Material cactusMat(){
//        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
//        mat.setColor("Diffuse", ColorRGBA.Green);
//        mat.setColor("Specular", ColorRGBA.White);
//        mat.setFloat("Shininess", 100);
//        mat.setBoolean("UseMaterialColors", true);
//        return mat;
        
        
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Green);
        return mat;
    }
    public static Material shellMat(){
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setColor("Diffuse", new ColorRGBA(1, .9f, .95f, 1));
        mat.setColor("Specular", ColorRGBA.White);
        mat.setFloat("Shininess", 100);
        mat.setBoolean("UseMaterialColors", true);
        
//        mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
//        mat.setColor("Color", new ColorRGBA(1, .9f, .95f, 1));
        return mat;
    }

    /*
     * Returns a simple tree mesh, with sample counts according to those provided.
     * Base is cylindrical and level, sample counts are the same for each h, and normals are approximate.
     */
    
    public static Mesh simpleTreeMesh(int maxLOD) {
        int radialSamples = 3 * (1 << maxLOD);
        int heightSamples = 1 << maxLOD;

        
        float radsPerSample = FastMath.TWO_PI / radialSamples;
        float heightPerSample = 1f / heightSamples;
        
        radialSamples++; //2pi
        heightSamples++; //top
        

        int pointCount = radialSamples * heightSamples;
        Vector3f[] coords = new Vector3f[pointCount];//radialSamples are +1 so that it wraps around.
        Vector3f[] normals = new Vector3f[pointCount];
        Vector2f[] texCoords = new Vector2f[pointCount];
        for (int rSamp = 0; rSamp < radialSamples; rSamp++) {//+1 for clarity and continuity.
            float theta = rSamp * radsPerSample;
            for (int hSamp = 0; hSamp < heightSamples; hSamp++) {
                float height = hSamp * heightPerSample;
                float radius = r(theta, height);
                int index = rSamp + hSamp * radialSamples;
                float cos = fastCosine(theta);
                float sin = fastSine(theta);
                coords[index] = new Vector3f(radius * cos, height, -radius * sin); //x = cos, z = -sin.  This is so theta + is counterclockwise.  
                normals[index] = new Vector3f(cos, 0, -sin); //close but not quite accurate.  y derivative + normalize necessary.
                texCoords[index] = new Vector2f(theta / FastMath.TWO_PI, height);
            }
        }
//        int[] indexes = new int[radialSamples * heightSamples * 3 * 2];
//        for (int r = 0; r < radialSamples; r++) {
//            for (int h = 0; h < heightSamples; h++) {
//                int index = (r + h * radialSamples) * 6;
//                
//                indexes[index    ] = r + h * radialSamples;
//                indexes[index + 1] = r + h * radialSamples + 1;
//                indexes[index + 2] = r + h * radialSamples + radialSamples;
//
//                indexes[index + 3] = r + h * radialSamples + 1;
//                indexes[index + 4] = r + h * radialSamples + 1 + radialSamples;
//                indexes[index + 5] = r + h * radialSamples + radialSamples;
//            }
//        }

        Mesh mesh = new Mesh();
        //mesh.setMode(Mesh.Mode.LineStrip);
        
        mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(coords));
        mesh.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(normals));
        mesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoords));
        
        VertexBuffer[] lods = new VertexBuffer[maxLOD + 1];
        for (int i = 0; i <= maxLOD; i++) {
            //for low detail models, stepsize is increased, to skip vertices both horiontally and vertically.  
            //For model detail 0 (highest detail), stepSize is 1, and as it is only multiplied and divided, it essentially does nothing.
            int stepSize = 1 << i;
            int[] indices = new int[(radialSamples) * (heightSamples) * 6 / (stepSize)]; //should it be /ss squared?
            for (int r = 0; r < (radialSamples - 1) / stepSize; r++) {
                for (int h = 0; h < (heightSamples - 1) / stepSize; h++) {

                    int i0 = (r + h * (radialSamples - 1)) * 6;//base index for this cycle of the loop.
                    
                    indices[i0    ] = (r +     (h    ) * radialSamples) * stepSize;
                    indices[i0 + 1] = (r + 1 + (h    ) * radialSamples) * stepSize;
                    indices[i0 + 2] = (r +     (h + 1) * radialSamples) * stepSize;

                    indices[i0 + 3] = (r + 1 + (h    ) * radialSamples) * stepSize;
                    indices[i0 + 4] = (r + 1 + (h + 1) * radialSamples) * stepSize;
                    indices[i0 + 5] = (r +     (h + 1) * radialSamples) * stepSize;
                    
                }
            }
            lods[i] = new VertexBuffer(Type.Index);
            lods[i].setupData(Usage.Static, 1, Format.UnsignedInt, BufferUtils.createIntBuffer(indices));
            
            if (i == 0) mesh.setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(indices));
        
        }
        

        mesh.setLodLevels(lods);
        mesh.updateBound();

        return mesh;
    }
    
    /*
     * A more complicated algorithm.  The base of the tree is bent,
     * the tree is curved slightly, and normals are accurate, calculated via the cross product method.
     * Higher curvature results in a more bent tree.   
     */
    public static Mesh complexTreeMesh(int maxLOD, Vector3f groundNormal, float radius, float height, float curvature) {
        //set up sample count, basic calculation and data buffers
        int radialSamples = 3 * (1 << maxLOD);
        int heightSamples = 1 << maxLOD;

        float radsPerSample = FastMath.TWO_PI / radialSamples;
        float heightPerSample = height / heightSamples;
        float noisePerSampleR = 1f / radialSamples; //this will be a good round number, if radialSamples < noiseWidth.
        
        radialSamples++; //2pi
        heightSamples++; //top
        
        int pointCount = radialSamples * heightSamples;
        
        //derivative calculations
        float dydx = -groundNormal.x / groundNormal.y;
        float dydz = -groundNormal.z / groundNormal.y;
        
        //and a noisemap
        byte[] noise = getNoise2();
        
        //data buffers for GLpoints
        Vector3f[] coords = new Vector3f[pointCount];//radialSamples are +1 so that it wraps around.
        Vector2f[] texCoords = new Vector2f[pointCount];
        for (int rSamp = 0; rSamp < radialSamples; rSamp++) {//+1 for clarity and continuity.
            float theta = rSamp * radsPerSample;
            for (int hSamp = 0; hSamp < heightSamples; hSamp++) {
                int index = rSamp + hSamp * radialSamples;

                
                float h = hSamp * heightPerSample;//height of a level tree.  will soon be adjusted
                float hFrac = h / height;
                float cos = fastCosine(theta);
                float sin = -fastSine(theta);//sin is - so theta + is counterclockwise, and polygon definition is intuitive. 
                float r = ((1 - hFrac) * (1 - hFrac) * (noiseFunction(noise, (rSamp * noisePerSampleR + hFrac * .06125f),(hFrac * .125f), noiseWidth)) * .5f + .5f) * radius;
                h += (height - h) / height * r *  (cos * dydx + sin * dydz);//correct for curved ground.  Correction occurs linearly more for the lower part of the tree.  
                float curvatureFactor = radius * FastMath.sqrt(Math.max(0, h / height)) * curvature;
                float x = r * cos + curvatureFactor * groundNormal.x;//standard circular coordinates, plus bend according to the derivative obtained by the normal. (dx/dy = -dy/dx
                float z = r * sin + curvatureFactor * groundNormal.z;
                
                coords[index] = new Vector3f(x, h, z); //x = cos, z = -sin.  This is so theta + is counterclockwise.  
                texCoords[index] = new Vector2f(theta / FastMath.TWO_PI, h / height);
            }
        }

        //calculate the normals based on calculus and cross products (method works on "smooth" RH surfaces of 2 variables).
        Vector3f[] normals = calcNormalsRH(coords, radialSamples, heightSamples);
    

        Mesh mesh = new Mesh();
        //mesh.setMode(Mesh.Mode.LineStrip);
        
        mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(coords));
        mesh.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(normals));
        mesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoords));
        
        VertexBuffer[] lods = new VertexBuffer[maxLOD + 1];
        for (int i = 0; i <= maxLOD; i++) {
            //for low detail models, stepsize is increased, to skip vertices both horiontally and vertically.  
            //For model detail 0 (highest detail), stepSize is 1, and as it is only multiplied and divided, it essentially does nothing.
            int stepSize = 1 << i;
            int[] indices = new int[(radialSamples) * (heightSamples) * 6 / (stepSize)]; //should it be /ss squared?
            for (int r = 0; r < (radialSamples - 1) / stepSize; r++) {
                for (int h = 0; h < (heightSamples - 1) / stepSize; h++) {

                    int i0 = (r + h * (radialSamples - 1)) * 6;//base index for this cycle of the loop.
                    
                    indices[i0    ] = (r +     (h    ) * radialSamples) * stepSize;
                    indices[i0 + 1] = (r + 1 + (h    ) * radialSamples) * stepSize;
                    indices[i0 + 2] = (r +     (h + 1) * radialSamples) * stepSize;

                    indices[i0 + 3] = (r + 1 + (h    ) * radialSamples) * stepSize;
                    indices[i0 + 4] = (r + 1 + (h + 1) * radialSamples) * stepSize;
                    indices[i0 + 5] = (r +     (h + 1) * radialSamples) * stepSize;
                    
                }
            }
            lods[i] = new VertexBuffer(Type.Index);
            lods[i].setupData(Usage.Static, 1, Format.UnsignedInt, BufferUtils.createIntBuffer(indices));
            
            if (i == 0) mesh.setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(indices));
        
        }
        

        mesh.setLodLevels(lods);
        mesh.updateBound();

        return mesh;
    }
    public static Mesh getLeaves(float radius, float hScale, Vector3f center){
        float leafSize = 15f;
        float leafSizeHalf = leafSize * .5f;
        radius -= leafSizeHalf;
        if(radius < 0) radius = 0;
        float radius2 = radius * radius;
        float rSqr = radius * radius;
        hScale *= radius;

        //a collection of 2 sided triangles
        Mesh mesh = new Mesh();
        int tris = 48;
        Vector3f[] coords = new Vector3f[tris * 6];
        Vector2f[] texCoords = new Vector2f[tris * 6];
        Vector3f[] normals = new Vector3f[tris * 6];
        int[] points = new int[tris * 6];
        Vector3f temp1 = new Vector3f(); Vector3f temp2 = new Vector3f();
        
        Vector2f tex0 = new Vector2f(0, 0); Vector2f tex1 = new Vector2f(1, 0); Vector2f tex2 = new Vector2f(0, 1);
        for(int i = 0; i < tris; i++){
            float theta = FastMath.rand.nextFloat() * FastMath.TWO_PI;
            float phi = FastMath.rand.nextFloat() * FastMath.PI;
            float sinePhi = fastSine(phi) * radius;
            //pick a random point wihin the spheree
            do temp1.set(FastMath.rand.nextFloat() * radius2 - radius, FastMath.rand.nextFloat() * radius2 - radius, FastMath.rand.nextFloat() * radius2 - radius);
            while(temp1.lengthSquared() > rSqr);
            coords[i * 3    ] = coords[i * 3 + 3] = new Vector3f(fastCosine(theta) * sinePhi * radius + center.x, fastCosine(phi) * hScale + center.y, fastSine(theta) * sinePhi + center.z);
            temp1.set(FastMath.rand.nextFloat() * leafSize - leafSizeHalf, FastMath.rand.nextFloat() * leafSize - leafSizeHalf, FastMath.rand.nextFloat() * leafSize - leafSizeHalf);
            temp2.set(FastMath.rand.nextFloat() * leafSize - leafSizeHalf, FastMath.rand.nextFloat() * leafSize - leafSizeHalf, FastMath.rand.nextFloat() * leafSize - leafSizeHalf);
            coords[i * 3 + 1] = coords[i * 3 + 4] = coords[i * 3].add(temp1);
            coords[i * 3 + 2] = coords[i * 3 + 5] = coords[i * 3].add(temp2);
            
            
            //calculate normals by cross products
            coords[i * 3 + 2].subtract(coords[i * 3 + 1], temp1);
            coords[i * 3 + 1].subtract(coords[i * 3    ], temp2);
            
            normals[i * 3    ] = normals[i * 3 + 1] = normals[i * 3 + 2] = temp1.cross(temp2);
            normals[i * 3 + 3] = normals[i * 3 + 4] = normals[i * 3 + 4] = normals[i * 2].mult(-1);
            
            points[i * 6    ] = i * 3    ;
            points[i * 6 + 1] = i * 3 + 1;
            points[i * 6 + 2] = i * 3 + 2;
            points[i * 6 + 3] = i * 3    ;
            points[i * 6 + 4] = i * 3 + 2;
            points[i * 6 + 5] = i * 3 + 1;
            
            texCoords[i * 3    ] = texCoords[i * 3 + 3] = tex0;
            texCoords[i * 3 + 1] = texCoords[i * 3 + 4] = tex1;
            texCoords[i * 3 + 2] = texCoords[i * 3 + 5] = tex2;
        }
       
        mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(coords));
        mesh.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(normals));
        mesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoords));
        mesh.setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(points));
        mesh.updateBound();
        return mesh;
    }
    //test rh cylinder
    public static void testCylinder(int radialSamples, int heightSamples){
        
        float radsPerSample = FastMath.TWO_PI / radialSamples;
        float heightPerSample = 1f / heightSamples;
        
        radialSamples++; //2pi
        heightSamples++; //top
        
        int pointCount = radialSamples * heightSamples;
        
        
        //data buffers for GLpoints
        Vector3f[] coords = new Vector3f[pointCount];//radialSamples are +1 so that it wraps around.
        Vector2f[] texCoords = new Vector2f[pointCount];
        for (int rSamp = 0; rSamp < radialSamples; rSamp++) {//+1 for clarity and continuity.
            float theta = rSamp * radsPerSample;
            for (int hSamp = 0; hSamp < heightSamples; hSamp++) {
                int index = rSamp + hSamp * radialSamples;

                float h = hSamp * heightPerSample;//height of a level tree.  will soon be adjusted
                float cos = fastCosine(theta);
                float sin = -fastSine(theta);//sin is - so theta + is counterclockwise, and polygon definition is intuitive. 
                  
                coords[index] = new Vector3f(cos, h, sin); //x = cos, z = -sin.  This is so theta + is counterclockwise.  
                texCoords[index] = new Vector2f(theta / FastMath.TWO_PI, h);
            }
        }
        Vector3f[] normals = calcNormalsRH(coords, radialSamples, heightSamples);
        for(int i = 0; i < pointCount; i++){
            if(i % radialSamples == 0) System.out.println("");
            System.out.println("normal: " + normals[i]);
            //System.out.println("point: " + coords[i] + "\t normal: " + normals[i]);
        }
    }
    //calculates the normals of an rSamp x hSamp curved surface of 2 variables using cross products, r wrapping around.
    public static Vector3f[] calcNormalsRH(Vector3f[] vertices, int radialSamples, int heightSamples){
        Vector3f[] normals = new Vector3f[vertices.length];
        //take the best approximate derivative possible, using vertices on one or both sides of the desired vertex as normals, and taking their cross products.
        //initialize temporary variables (this way it is done only once, save the GC and the VM (quite) a few clock cycles.
        Vector3f dr;
        Vector3f dh;
        
        //there are 4 special "corner cases."  R vertices wrap around, so we get equivalent accuracy for them, but the top and bottom of the h normals are slightly less accurate.
        //when r = 0 and r = 2pi, we have the same point, so the same normal.
        //r = 0 or 2pi h = 0 corner case (bottom corners):
        dr = vertices[radialSamples - 2].subtract(vertices[1]);// r wraps around
        dh = vertices[0].subtract(vertices[radialSamples]);
        normals[0] = normals[radialSamples - 1] = dr.crossLocal(dh).normalizeLocal();

        //r = 0 or 2pi, h = heightSamples - 1 corner case (top corners):
        dr = vertices[heightSamples * radialSamples - 2].subtract(vertices[radialSamples * (heightSamples - 1) + 1]); //r wraps around
        dh = vertices[(heightSamples - 2) * radialSamples].subtract(vertices[(heightSamples - 1) * radialSamples]);
        normals[(heightSamples - 1) * radialSamples] = normals[heightSamples * radialSamples - 1]= dr.crossLocal(dh).normalizeLocal();
                
        for(int r = 1; r < radialSamples - 1; r++){
            //do the bottom and top edges, where previous h points are not available. 
            //bottom edge
            dr = vertices[r - 1].subtract(vertices[r + 1]);
            dh = vertices[r].subtract(vertices[r + radialSamples]);
            //we now have to perpendicular vectors, take their cross product and normalize it to get a normal vector.
           normals[r] = dr.crossLocal(dh).normalizeLocal();
           
           //top edge
           dr = vertices[(heightSamples - 1) * radialSamples + r - 1].subtract(vertices[(heightSamples - 1) * radialSamples + r + 1]);
           dh = vertices[(heightSamples - 2) * radialSamples + r].subtract(vertices[(heightSamples - 1) * radialSamples + r]);
           normals[(heightSamples - 1) * radialSamples + r] = dr.crossLocal(dh).normalizeLocal();
           
           //now do the bulk of the normals (not on the edges).  
           for(int h = 1; h < heightSamples - 1; h++){
                dr = vertices[r - 1 + h * radialSamples].subtract(vertices[r + 1 + h * radialSamples]);
                dh = vertices[r + (h - 1) * radialSamples].subtract(vertices[r + (h + 1) * radialSamples]);
                //we now have to perpendicular vectors, take their cross product and normalize it to get a normal vector.
                normals[r + h * radialSamples] = dr.crossLocal(dh).normalizeLocal();
           }
        }
        //All that remains is the r = 0 & r = radialSamples - 1 (2pi) edges (h being free).  They are the same, because r wraps around
        for(int h = 1; h < heightSamples - 1; h++){
            //h = 0 edge
            dr = vertices[h * (radialSamples + 1) - 2].subtract(vertices[h * radialSamples + 1]);
            dh = vertices[(h - 1) * radialSamples].subtract(vertices[(h + 1) * radialSamples]);
            
            
            //there is an error here.  fix it later.  
            normals[h * radialSamples] = Vector3f.UNIT_XYZ;//normals[(h + 1) * radialSamples - 1] = dr.crossLocal(dh).normalizeLocal();
        }
        return normals;
    }
    
    
//    public static WorldObject getShell(int radius, int height, int loops){
//        Geometry geom = new Geometry("Shell", getShellMesh(0, loops));
//
//        geom.setMaterial(shellMat());
//        geom.setLocalScale(radius, radius, height);
//        //geom.setModelBound(new BoundingBox(new Vector3f(-radius, -radius, 0), new Vector3f(radius, radius, height)));
//        
//        LodControl control = new LodControl();
//        control.setTrisPerPixel(.5f);
//
//        //geom.addControl(new LODWorldObjectControl(Main.camera, 100000f / (screenWidth * radius)));
//
//        CollisionShape shape = new CylinderCollisionShape(new Vector3f(radius, radius, height), 2);
//
//        WorldObject shell = new WorldObject("Failed Shell", geom, new RigidBodyControl(shape, 0), 0);
//
//        return shell;
//    }
//    public static Mesh getShellMesh(int maxLOD, int loops){
//        
//        float slope = .35f;
//        float golden = 1.61803399f; //this exists in any universe.
//        
//        float zero = .05f; //theoretically, this should equal lim a a -> 0+  .05 works from a distance (5 cm).
//        
//        int radialSamples = loops * 3 * (1 << maxLOD);
//        float thetaPer = FastMath.TWO_PI / radialSamples * loops;
//        
//        float rScale = 1 / (/*zero **/ FastMath.pow(golden, loops)); //one divided by largest radius on this shell.  Effectively scales the shell to a radius of one. 
//        
//        radialSamples++;//loop around
//        
//        float normalScale = 1 / FastMath.sqrt(1 + slope * slope);
//        
//        int pointCount = radialSamples * 2 + 1;
//        Vector3f[] coords = new Vector3f[pointCount];//radialSamples are +1 so that it wraps around.
//        Vector3f[] normals = new Vector3f[pointCount];
//        Vector2f[] texCoords = new Vector2f[pointCount];
//        
//        for(int i = 0; i < radialSamples; i ++){
//            //calculate theta.  
//            float theta = i * thetaPer;
//            //calculate r and h for this value of theta.  This is the angular part of the shell.  
//            float r = rScale * /*zero **/ FastMath.pow(golden, theta / FastMath.TWO_PI); //exponential growth.  Each full loop will be *goldenratio larger than the previous.
//            float h = 1 - theta * slope;
//                    
//            int base = 2 * i;
//            coords[base] = new Vector3f(0, 0, 1 - theta * .1f);
//            coords[base + 1] = new Vector3f(FastMath.cos(theta) * r, FastMath.sin(theta) * r, h);
//            
//            normals[base] = normals[base + 1] = new Vector3f(FastMath.cos(theta) * normalScale, FastMath.sin(theta) * normalScale, slope * normalScale);
//            //top center spindle, outer point.
//        }
//        coords[pointCount - 1] = new Vector3f(0, 0, -4);
//        normals[pointCount - 1] = new Vector3f(0, 0, -1);
//        
//        Mesh mesh = new Mesh();
//        mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(coords));
//        mesh.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(normals));
////        mesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoords));
//
//        VertexBuffer[] lods = new VertexBuffer[maxLOD + 1];
//        
//        //only one level for now.
//        
//        //for (int i = 0; i < maxLOD; i++) {
//            int i = 0;
//            int stepSize = 1 << i;
//            int rIndices = 3 * 3;
//            int[] indices = new int[(radialSamples - 1) * rIndices];
//            for (int r = 0; r < (radialSamples - 1) / stepSize; r++) {
//                //top
//                indices[r * rIndices    ] = 2 * r;
//                indices[r * rIndices + 1] = 2 * r + 1;
//                indices[r * rIndices + 2] = 2 * r + 3;
//                
//                indices[r * rIndices + 3] = 2 * r;
//                indices[r * rIndices + 4] = 2 * r + 3;
//                indices[r * rIndices + 5] = 2 * r + 2;
//                
//                indices[r * rIndices + 6] = 2 * r + 1;
//                indices[r * rIndices + 7] = pointCount - 1;
//                indices[r * rIndices + 8] = 2 * r + 3;
////                for (int h = 0; h < (heightSamples - 1) / stepSize; h++) {
////
////                    indices[(r + h * (radialSamples - 1)) * 6] = (r + h * (radialSamples - 1)) * stepSize;
////                    indices[(r + h * (radialSamples - 1)) * 6 + 1] = (r + h * (radialSamples - 1) + 1) * stepSize;
////                    indices[(r + h * (radialSamples - 1)) * 6 + 2] = (r + h * (radialSamples - 1) + (radialSamples - 1)) * stepSize;
////
////                    indices[(r + h * (radialSamples - 1)) * 6 + 3] = (r + h * (radialSamples - 1) + 1) * stepSize;
////                    indices[(r + h * (radialSamples - 1)) * 6 + 4] = (r + h * (radialSamples - 1) + 1 + (radialSamples - 1)) * stepSize;
////                    indices[(r + h * (radialSamples - 1)) * 6 + 5] = (r + h * (radialSamples - 1) + (radialSamples - 1)) * stepSize;
////                }
//            }
//            lods[i] = new VertexBuffer(Type.Index);
//            lods[i].setupData(Usage.Static, 1, Format.UnsignedInt, BufferUtils.createIntBuffer(indices));
//            
//            if(i == 0) mesh.setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(indices));
//        //}
//        
////        mesh.setMode(Mesh.Mode.Points);
////        mesh.setPointSize(8);
//        
//        //mesh.setLodLevels(lods);
//        mesh.updateBound();
//
//        return mesh;
//    }

//    /*
//     * Make a road.  + shape.  2 wide
//     */
//    private static WorldObject road;
//    public static WorldObject getRoad(){
//        if (road == null){
//            Vector3f[] basicCoords = new Vector3f[]{
//                new Vector3f(1f, .35f, 0), new Vector3f(1f,.33f,.1f), 
//                new Vector3f(.35f, .35f, 0), new Vector3f(.33f, .33f, .1f),
//                new Vector3f(.35f, 1f, 0), new Vector3f(.33f, 1f, 0)};
//            int[] transforms = new int[]{1,1, -1,1, -1,-1, 1,-1};
////            Vector3f[] transforms = new Vector3f[]{
////                new Vector3f(1, 1, 1), new Vector3f(-1, 1, 1), new Vector3f(-1, -1, 1), new Vector3f(1, -1, 1)};
//            //float[] coords = new float[]{-1f,.35f,0, -1f,.33f,.1f, -.35f, .35f, 
//            float[] coords = new float[3 * 6 * 4 + 3];
//            for(int t = 0; t < transforms.length; t++){
//                for(int i = 0; i < basicCoords.length; i++){
//                    int baseIndex = t * 6 + i;
//                    coords[baseIndex    ] = basicCoords[i].x * transforms[t * 2];
//                    coords[baseIndex + 1] = basicCoords[i].y * transforms[t * 2 + 1];
//                    coords[baseIndex + 2] = basicCoords[i].z;
//                }
//            }
//            coords[72] = 0;//middle point;
//            coords[73] = 0;
//            coords[74] = .1f;
//            //float[] texCoords = new float[]{0,0, 0,.1, .3};
//            
//        }
//        return road;
//    }
    
    public static Texture2D leaves(int width) {
        width = noiseWidth;
        byte[] image = new byte[width * width * 4];//type RGBA byte
        //get a noiseMap
        byte[] noiseMap = getNoise2();
        float converter = 1f / width;
        int borderSize = width / 8;
        float invBorder = 1f / borderSize;
        int diagThreshold = width - borderSize;
        for(int x = 0; x < width; x++)
            for(int y = 0; y < width; y++){
                int i = x + y * width;
                float noise = noiseFunction(noiseMap, (i % width) * converter, (i / width) * converter, width); 
            
                if(x < borderSize) noise -= (borderSize - x) * invBorder;
                if(y < borderSize) noise -= (borderSize - y) * invBorder;
                if(x + y > diagThreshold) noise -= (x + y - diagThreshold) * invBorder;
                
                if(noise > .5f){
                    image[i * 4] = (byte) (noise * 200);
                    image[i * 4 + 1] = (byte) (128 + noise * 128);
                    image[i * 4 + 2] = (byte) (64 + noise * 100);
                    image[i * 4 + 3] = (byte) (noise * 64 + 191);
                }
            }
        return new Texture2D(new Image(Image.Format.RGBA8, width, width, com.jme3.util.BufferUtils.createByteBuffer(image)));
    }

    //range contract is (0, 1], loops around every 1 unit.
    public static float contNoise1D(float x) {
        x -= (int) x;
        return x - x * x + FastMath.sin(x * FastMath.TWO_PI * 3) * .2f + FastMath.cos(x * FastMath.TWO_PI * 3) * .1f + .5377f;
    }

    /*
     * multi variable function.
     * Theta can be in the range [0, 2pi]
     * h must be in the range [0, 1]
     * returns r, a radius value [0, 1]
     */
    public static float r(float theta, float h) {
        return (1 - h * h) * contNoise1D(theta / FastMath.TWO_PI + h);
    }
    /**
     * LOD Tree code complete
     */
    
    
    //Emily's quest ground.
    
    public static final int TERRAIN_GRASS = 0, TERRAIN_FOREST = 1, TERRAIN_SAND = 2, TERRAIN_SWAMP = 3, TERRAIN_MOUNTAIN = 4, TERRAIN_MARBLE = 5, TERRAIN_FIRE = 6, TERRAIN_CLOUDS = 7, TERRAIN_CHAOS = 8,
            TERRAIN_CAVE = 9, TERRAIN_MAGES_TOWER = 10,
            TERRAIN_LUSH = 12, TERRAIN_ROAD = 13, TERRAIN_EMPTINESS = 14, TERRAIN_LIGHT = 15;
    public static final int SEASON_SPRING = 0, SEASON_SUMMER = 1, SEASON_AUTUMN = 2, SEASON_WINTER = 3;

    public static Texture generateGround(int width, int type, int season) {
        byte[] image = new byte[width * width * 3];//type RGB byte
        //create a random number generator.
        Random random = new Random();
        //setup image and graphics
        switch (type) {
            case TERRAIN_GRASS:
                if (season == SEASON_WINTER) {
                    renderGrittyBackground(image, width, random, 175, 55, .9f, .9f, 1f);
                    break; //render snow and we're done.  
                }
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < width; y++) { //gritty background
                        int intensity = (int) (fastSine(x / 200f) * 25 + 175 + random.nextInt(55));
                        image[(x + width * y) * 3] = (byte) (intensity / 2);
                        image[(x + width * y) * 3 + 1] = (byte) (intensity);
                        image[(x + width * y) * 3 + 2] = (byte) (intensity / 3);
                    }
                }
                break;
            case TERRAIN_SAND:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < width; y++) {
                        int intensity = (int) (fastSine(x / 50f + fastSine(y / 100f) / 2f) * 50 + 150 + random.nextInt(55));
                        //SORRY!  Bitwise or operators are necessary for the low level pixel manipulation.  
                        image[(x + width * y) * 3] = (byte) (intensity);
                        image[(x + width * y) * 3 + 1] = (byte) (intensity);
                        image[(x + width * y) * 3 + 2] = (byte) (intensity / 2);
                    }
                }
                break;
            case TERRAIN_MOUNTAIN:
                renderGrittyBackground(image, width, random, 175, 55, .9f, .9f, 1f);
                break; //render snow and we're done.  
            case TERRAIN_SWAMP:
                if (season == SEASON_WINTER) {
                    renderGrittyBackground(image, width, random, 175, 55, .9f, .9f, 1f);
                    break; //render snow and we're done.  
                }
                renderGrittyBackground(image, width, random, 100, 75, .6f, .5f, .6f);
                break;
            case TERRAIN_MARBLE: //doesn't look anything like a cloud, but it's cool.  
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < width; y++) {
                        double i0 = fastSine(fastSine(x / 256f) * 50 + fastSine(y / 256f) * 50 + 100);//use a sine squared.  
                        int intensity = (int) (i0 * i0 * 200);
                        //SORRY!  Bitwise or operators are necessary for the low level pixel manipulation.  
                        image[(x + width * y) * 3] = (byte) (intensity + random.nextInt(55));
                        image[(x + width * y) * 3 + 1] = (byte) (intensity + random.nextInt(55));
                        image[x * y + 2] = (byte) (intensity + random.nextInt(55));
                    }
                }
                break;
            case TERRAIN_FIRE:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < width; y++) {
                        int intensity = (int) (fastSine(x / 50f) * 50 + fastSine(y / 100f) * 50 + 100 + random.nextInt(55));

                        image[(x + width * y) * 3] = (byte) (intensity);
                        image[(x + width * y) * 3 + 1] = (byte) (intensity / 2);
                        image[(x + width * y) * 3 + 2] = (byte) (intensity / 2);
                    }
                }
                break;

            //this one looks really cool.  
            case TERRAIN_CHAOS:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < width; y++) {
                        double i0 = (fastSine(x / 128f) * 50 + fastSine(y / 128f) * 50 + 100);//use a sine squared.  
                        int intensity = (int) (i0 * i0);
                        //SORRY!  Bitwise or operators are necessary for the low level pixel manipulation.  
                        int color = (intensity + random.nextInt(55)) << 16 | (intensity + random.nextInt(55)) << 8 | (intensity + random.nextInt(55));

                        image[(x + width * y) * 3] = (byte) (intensity + random.nextInt(55));
                        image[(x + width * y) * 3 + 1] = (byte) (intensity + random.nextInt(55));
                        image[(x + width * y) * 3 + 2] = (byte) (intensity + random.nextInt(55));
                    }
                }
                break;

            case TERRAIN_ROAD: //long dark grass used around pools of water.  
                if (season == SEASON_WINTER) {
                    renderGrittyBackground(image, width, random, 175, 55, .9f, .9f, 1f);
                    break; //render snow and we're done.  
                }
                renderGrittyBackground(image, width, random, 175, 55, .6f, .5f, .3f);
                break;
            case TERRAIN_LIGHT: //pure white light.
                for (int i = 0; i < width * width * 3; i++) {
                    image[i] = (byte) 255;
                }
                break;
            default:
                int stripeWidth = width / 16;
                int stripeLength = width / 4;
                for (int i = 0; i < width * width; i++) {
                    byte intensity = (byte) random.nextInt(32);
                    image[i * 3] = (byte) (intensity);
                    image[i * 3 + 1] = (byte) (intensity);
                    image[i * 3 + 2] = (byte) (intensity);
                }
                for (int x = width / 2 - stripeWidth / 2; x < width / 2 + stripeWidth / 2; x++) {
                    for (int y = width / 2 - stripeLength / 2; y < width / 2 + stripeLength / 2; y++) {
                        byte intensity = (byte) 96;
                        image[(x + y * width) * 3] += (intensity);
                        image[(x + y * width) * 3 + 1] += intensity;
                    }
                }
                break;
        }
        return new Texture2D(new Image(Image.Format.RGB8, width, width, com.jme3.util.BufferUtils.createByteBuffer(image)));
    }

    public static void renderGrittyBackground(byte[] image, int width, Random random, int baseIntensity, int randomIntensity, float rFactor, float gFactor, float bFactor) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < width; y++) { //gritty background
                int intensity = baseIntensity + random.nextInt(randomIntensity);
                image[(x + width * y) * 3] = (byte) (intensity * rFactor);
                image[(x + width * y) * 3 + 1] = (byte) (intensity * gFactor);
                image[(x + width * y) * 3 + 2] = (byte) (intensity * bFactor);
            }
        }
    }

    public static Spatial daySkyBox(int width){
          byte[] noise = genNoise2(width / 2, 1, 8f, 8, .1f);
          int noiseWidth = width / 2;
          Texture side = daySkyTex(width, noise, noiseWidth, 0f, .8f);
          Texture top = daySkyTex(width, noise, noiseWidth, .8f, .8f);
          Texture bottom = daySkyTex(width, noise, noiseWidth, 0, 0);
//        Texture west = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_west.jpg");
//        Texture east = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_east.jpg");
//        Texture north = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_north.jpg");
//        Texture south = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_south.jpg");
//        Texture up = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_up.jpg");
//        Texture down = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_down.jpg");

        //Spatial sky = SkyFactory.createSky(assetManager, tex, tex, tex, tex, tex, tex);
          Spatial sky = SkyFactory.createSky(assetManager, side, side, side, side, top, bottom);
          return sky;
    }
    public static Texture2D daySkyTex(int width, byte[] noise, int noiseWidth, float bottomCloud, float topCloud){
        byte[] image = new byte[width * width * 3];
        float invWidth = 1f / width;
        
        int r = 100;
        int g = 100;
        int b = 255;
        
        for(int y = 0; y < width; y++){
            int yBase = y * width;
            float yFrac = y * invWidth;
            float oneMinusYFrac = 1 - yFrac;
            float cloudThreshold = bottomCloud * oneMinusYFrac + topCloud * yFrac;
            for(int x = 0; x < width; x++){
                int i = x + yBase;
                float cloudNoise = noiseFunction(noise, x * invWidth, yFrac, noiseWidth);
                if(cloudThreshold < cloudNoise){
                    image[i * 3] = image[i * 3 + 1] = image[i * 3 + 2] = (byte)(128 + 128 * cloudNoise);
                }
                else{
                    yFrac = 1;
                    image[i * 3    ] = (byte)(r * yFrac);
                    image[i * 3 + 1] = (byte)(g * yFrac);
                    image[i * 3 + 2] = (byte)(b * yFrac);
                }
            }
            
        }
        return new Texture2D(new Image(Image.Format.RGB8, width, width, com.jme3.util.BufferUtils.createByteBuffer(image)));
    }
    
    public static Spatial nightSkyBox(int quality) {
        int width = 2 << quality;

        Texture tex = nightSkyTex(width, .02f);
//        Texture west = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_west.jpg");
//        Texture east = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_east.jpg");
//        Texture north = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_north.jpg");
//        Texture south = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_south.jpg");
//        Texture up = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_up.jpg");
//        Texture down = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_down.jpg");

        Spatial sky = SkyFactory.createSky(assetManager, tex, tex, tex, tex, tex, tex);
        return sky;
    }

    public static Texture2D nightSkyTex(int width, float starDensity) {
        //generate a texture
        byte[] image = new byte[width * width * 3];//type RGB byte
        //create a random number generator.
        Random random = new Random();

        int stars = (int) (starDensity * width * width);
        for (int i = 0; i < stars; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(width);
            byte intensity = (byte) 96;
            image[(x + y * width) * 3] += (intensity);
            image[(x + y * width) * 3 + 1] += intensity;
            image[(x + y * width) * 3 + 2] += intensity;
        }
        
        return new Texture2D(new Image(Image.Format.RGB8, width, width, com.jme3.util.BufferUtils.createByteBuffer(image)));
    }
    
    public static TextureCubeMap nightMap(int width, float starDensity){
byte[] image = new byte[width * width * 3];//type RGB byte
        //create a random number generator.
        Random random = new Random();

        int stars = (int) (starDensity * width * width);
        for (int i = 0; i < stars; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(width);
            byte intensity = (byte) 96;
            image[(x + y * width) * 3] += (intensity);
            image[(x + y * width) * 3 + 1] += intensity;
            image[(x + y * width) * 3 + 2] += intensity;
        }
        ByteBuffer sky = com.jme3.util.BufferUtils.createByteBuffer(image);
    
        Image cubeImage = new Image(Image.Format.RGB8, width, width, null);
        cubeImage.addData(sky);
        cubeImage.addData(sky);
        cubeImage.addData(sky);
        cubeImage.addData(sky);
        cubeImage.addData(sky);
        cubeImage.addData(sky);
        return new TextureCubeMap(cubeImage);
        //from skyboxfactory
        /*
         *         Image cubeImage = new Image(westImg.getFormat(), westImg.getWidth(), westImg.getHeight(), null);

        cubeImage.addData(westImg.getData(0));
        cubeImage.addData(eastImg.getData(0));

        cubeImage.addData(downImg.getData(0));
        cubeImage.addData(upImg.getData(0));

        cubeImage.addData(southImg.getData(0));
        cubeImage.addData(northImg.getData(0));
        
        if (westImg.getEfficentData() != null){
            // also consilidate efficient data
            ArrayList<Object> efficientData = new ArrayList<Object>(6);
            efficientData.add(westImg.getEfficentData());
            efficientData.add(eastImg.getEfficentData());
            efficientData.add(downImg.getEfficentData());
            efficientData.add(upImg.getEfficentData());
            efficientData.add(southImg.getEfficentData());
            efficientData.add(northImg.getEfficentData());
            cubeImage.setEfficentData(efficientData);
        }

        TextureCubeMap cubeMap = new TextureCubeMap(cubeImage);
        cubeMap.setAnisotropicFilter(0);
        cubeMap.setMagFilter(Texture.MagFilter.Bilinear);
        cubeMap.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
        cubeMap.setWrap(Texture.WrapMode.EdgeClamp);

         */
    }
    public static Material smokeMat(int width, int frames){
        Material mat = new Material(assetManager, "MatDefs/Tex3DAnim.j3md");
        mat.setTexture("Texture", smokeTex(width, frames));
        return mat;
    }
    public static Texture3D smokeTex(int width, int frames){
//        byte[] data = new byte[frames * width * width];
//        
//        int wSqr = width * width;
//        int sWidth = width / 2;
//        int sFrames = frames / 2;
//        int sources = frames + width * width / 1024; //1 per every 32 pixel x 32 pixel region
//        
//        //sources = 1;
//        
//        int[] coords = new int[sources * 3];
//        float[] scales = new float[sources];
//        for(int i = 0; i < sources; i++){
//            coords[i * 3    ] = FastMath.rand.nextInt(width);
//            coords[i * 3 + 1] = FastMath.rand.nextInt(width);
//            coords[i * 3 + 2] = FastMath.rand.nextInt(frames);
//            scales[i] = 128f * (FastMath.rand.nextFloat() + 1f / width + .0125f);
//        }
//        for(int z = 0; z < frames; z++){
//            for(int x = 0; x < width; x++)
//                for(int y = 0; y < width; y++){
//                    int d = 128;
//                    for(int i = 0; i < sources; i++){
//                        int x0 = x - coords[i * 3    ];
//                        int t1 = x0 + width;
//                        int t2 = x0 - width;
//                        if(t1 * t1 < x0 * x0) x0 = t1;
//                        if(t2 * t2 < x0 * x0) x0 = t2;
//                       
//                        
//                        int y0 = y - coords[i * 3 + 1];
//                        t1 = y0 + width;
//                        t2 = y0 - width;
//                        if(t1 * t1 < y0 * y0) y0 = t1;
//                        if(t2 * t2 < y0 * y0) y0 = t2;
//                        
//                        int z0 = z - coords[i * 3 + 2];
//                        t1 = z0 + frames;
//                        t2 = z0 - frames;
//                        if(t1 * t1 < z0 * z0) z0 = t1;
//                        if(t2 * t2 < z0 * z0) z0 = t2;
//                        
//                        d = Math.min(d, (int)(fastSqrt(x0 * x0 + y0 * y0 + z0 * z0) * scales[i]));
//                    }
//                    if (d > 128) d = 128;
//                    data[x + y * width + z * wSqr] = (byte)(128 - d);
//                }
//        }
        
        ArrayList<ByteBuffer> buffers = new ArrayList<ByteBuffer>();
        buffers.add(BufferUtils.createByteBuffer(perlinNoise3(width, width, frames)));
        Texture3D tex = new Texture3D(new Image(Image.Format.Luminance8, width, width, frames, buffers));
        tex.setWrap(Texture.WrapMode.Repeat);
        return tex;
        /*
        byte[][] data = new byte[frames][width * width];
        ArrayList<ByteBuffer> buffers = new ArrayList<>();
        
        int wSqr = width * width;
        int sources = frames + width * width / 1024; //1 per every 32 pixel x 32 pixel region
        int[] coords = new int[sources * 3];
        for(int i = 0; i < sources; i += 3){
            coords[i * 3    ] = FastMath.rand.nextInt(width);
            coords[i * 3 + 1] = FastMath.rand.nextInt(width);
            coords[i * 3 + 2] = FastMath.rand.nextInt(frames);
        }
        for(int z = 0; z < frames; z++){
            for(int x = 0; x < width; x++)
                for(int y = 0; y < width; y++){
                    float d = 16;
                    for(int i = 0; i < sources; i++){
                        int x0 = x - coords[i * 3    ];
                        int temp = x0 + width;
                        if(temp < Math.abs(x0)) x0 = temp;
                        int y0 = y - coords[i * 3 + 1];
                        temp = y0 + width;
                        if(temp < Math.abs(y0)) y0 = temp;
                        int z0 = z - coords[i * 3 + 2];
                        temp = z0 + width;
                        if(temp < Math.abs(z0)) z0 = temp;
                        
                        d = Math.min(d, fastSqrt(x0 * x0 + y0 * y0 + z0 * z0) * .1f);
                    }
                    data[z][x + y * width] = (byte)(256 / (d * d));
                }
           buffers.add(BufferUtils.createByteBuffer(data[z]));
        }
        
        Texture3D tex = new Texture3D(new Image(Image.Format.Luminance8, width, width, frames, buffers));
        return tex;
        */
    }
    
    //noise functionality works by generating 3 dimensional noise maps as needed, and taking traces of a predefined 3D map for 2D and 1D maps.  
    //Experimentation has shown that the data storage capacity required for a sizable (128) 4 dim. noise map exceeds current acceptable ram use.  
    //Modern 64 bit architectures should soon have enough room to make this a possibility in the near future.
    //a convergent geometric series is used to weight successive noise vectors.
    public static int noiseWidth = 64;
    public static byte[] noise3;
    public static byte[] genNoise3(int width, float ratio, int trigFunctions, float randomNoise){
        int wSqr = width * width;
        byte[] noise = new byte[wSqr * width];
        
        
        //first deal with pure randomness
        int randomHalf = (int)(randomNoise * 64);
        int random = randomHalf * 2 + 1; //totally random noise, equivalent to all frequencies distributed evenly, essentially favors higher frequencies when compared to these algorithms.
        Random rand = FastMath.rand;
        
        int baseValue = 128 - randomHalf;
        
        //since we are not actually taking infinity functions, the series never quite reaches a/(1 - r) (although it usually converges quite quickly).
        //we can use the formula for the first n terms, a1(1 - r ^ n) / (1 - r)
        float seriesSum = (1 - FastMath.pow(ratio, trigFunctions)) / (1 - ratio);
        
        
        int[]vectors = new int[trigFunctions * 3]; 
        //create 3 dimensional vectors, corresponding to the coefficients of each trig function, then make sure all vectors are nonzero
        for(int i = 0; i < trigFunctions; i++){
            vectors[i * 3] = FastMath.rand.nextInt(i + 1);
            vectors[i * 3 + 1] = FastMath.rand.nextInt(i + 1);
            vectors[i * 3 + 2] = FastMath.rand.nextInt(i + 1);
            if(vectors[i * 3] == 0 &&
               vectors[i * 3 + 1] == 0 &&
               vectors[i * 3 + 2] == 0) 
                    vectors[i * 3 + FastMath.rand.nextInt(3)] = 1; //provide a unit vector if the 0 vector is given.
        }
        //form a series that converges to 128
        float[] scalars = new float[trigFunctions];
        scalars[0] = ((float)baseValue) / seriesSum;
        for(int i = 1; i < trigFunctions; i++){
            scalars[i] = scalars[i - 1] * ratio;
        }//the functions form a convergent series.
        
        float trigScalar = FastMath.TWO_PI / width;
        
            for(int z = 0; z < width; z++){
                int baseA = wSqr * z;
                for(int y = 0; y < width; y++){
                    int baseB = baseA + width * y;
                    for(int x = 0; x < width; x++){
                        int index = baseB + x;
                        //we now have an index and an x y z.
                        float noiseValue = 0;
                        for(int i = 0; i < trigFunctions; i++){
                            int baseVector = i * 3;
                            noiseValue += fastSine((
                                    x * vectors[baseVector    ] + 
                                    y * vectors[baseVector + 1] + 
                                    z * vectors[baseVector + 2]) *
                                    trigScalar) * scalars[i];
                            
                        }
                        noise[index] = (byte)(baseValue + noiseValue + rand.nextInt(random));
                    }
                }
            }
        return noise;
    }
    
    public static byte[] noise4;
    public static byte[] genNoise4(int width){
        int wSqr = width * width;
        int wCbe = wSqr * width;
        int wQrt = wCbe * width;
        byte[] noise = new byte[wQrt];
        
        int trigFunctions = 8;
        int[]vectors = new int[trigFunctions * 4]; //create 4 dimensional vectors, corresponding to the coefficients of each trig function.
        for(int i = 0; i < vectors.length; i++) vectors[i] = 1 + FastMath.rand.nextInt(i + 1);
        float[] scalars = new float[trigFunctions];
        scalars[0] = 64 / 8; //converge to 128
        for(int i = 1; i < trigFunctions; i++) scalars[i] = scalars[i - 1] * 1f;//the functions form a convergent series.
        
        float trigScalar = FastMath.TWO_PI / width;
        
        for(int w = 0; w < width; w++){
            int baseW = wCbe * w;
            for(int z = 0; z < width; z++){
                int baseZ = baseW + wSqr * z;
                for(int y = 0; y < width; y++){
                    int baseY = baseZ + width * y;
                    for(int x = 0; x < width; x++){
                        int index = baseY + x;
                        //we now have an index and an x y z.
                        float noiseValue = 0;
                        for(int i = 0; i < trigFunctions; i++){
                            int baseVector = i * 4;
                            noiseValue += fastSine((
                                    x * vectors[baseVector    ] + 
                                    y * vectors[baseVector + 1] + 
                                    z * vectors[baseVector + 2] + 
                                    w * vectors[baseVector + 3]) *
                                    trigScalar) * scalars[i];
                            
                        }
                        noise[index] = (byte)(128 + noiseValue);
                    }
                }
            }
        }
        return noise;
    }
    public static byte[] getNoise3(){
        //hold an arbitrary dimension constant and take a trace.
        if(noise4 == null) noise4 = genNoise4(128); //128 takes many megabytes
        int width = 128;
        byte[] noise = new byte[width * width * width];
        
        int xScale = 1; //by shuffling these values, we get rotations. 
        int yScale = width;
        int zScale = width * width;
        int w = FastMath.rand.nextInt(width);
        int traceConstant = w * width * width * width;
        for(int x = 0; x < width; x++){
            int bA = xScale * x;
            for(int y = 0; y < width; y++){
                int bB = bA + yScale * y;
                for(int z = 0; z < width; z++){
                    noise[bB + z] = noise4[bB + z + traceConstant];
                }
            }
        }
        return noise;
    }
    public static byte[] getNoise2(){
        //hold an arbitrary dimension constant and take a trace in 3 dimensional space.
        int width = noiseWidth;
        if(noise3 == null) noise3 = genNoise3(width, .75f, 16, .0625f); //128 takes many megabytes
        byte[] noise = new byte[width * width];

        //which dimension are we moving through in the 3D map that maps onto our new 2D map?
        int xDim = FastMath.rand.nextInt(3);
        int yDim = FastMath.rand.nextInt(2);
        if(yDim >= xDim) yDim++;  //xDim & yDim will be different but arbitrary dimensions.
        int constDimension = 0;  //one of the dimensions is held constant, and a trace is taken for that dimension.
        if(xDim == constDimension || yDim == constDimension) constDimension++;
        if(xDim == constDimension || yDim == constDimension) constDimension++;
        
        int xScale = intPower(width, xDim);
        int yScale = intPower(width, yDim);
        int trace = intPower(width, constDimension) * FastMath.rand.nextInt(width);
                
        for(int x = 0; x < width; x++){
            for(int y = 0; y < width; y++){
                noise[x + y * width] = noise3[x * xScale + y * yScale + trace];
            }
        }
        return noise;
    }
    
    //Generate a 2 dimensional noise map.  getNoise2 is preferable if a 3dim noise map with desired properties, but genNoise2 allows fine tuning of maps and control of map size.
    //Higher trig function counts will reduce periodicity, as will a lower ratio.  Ratios close to one will result in very random maps, wheras low ratios ~.6 will result in a soft sweeping painted appearance.  Higher trig function counts tend to result in better noise maps.
    //Random noise adds a random value to each point on the map, can be used to simulate pure white noise without using infinity functions.  Best in small amounts.  
    public static byte[] genNoise2(int width, int initialVectorComponentSize, float ratio, int trigFunctions, float randomNoise){
        byte[] noise = new byte[width * width];
        
        //first deal with pure randomness
        int randomHalf = (int)(randomNoise * 64);
        int random = randomHalf * 2 + 1; //totally random noise, equivalent to all frequencies distributed evenly, essentially favors higher frequencies when compared to these algorithms.
        Random rand = FastMath.rand;
        
        int baseValue = 128 - randomHalf;
        
        //since we are not actually taking infinity functions, the series never quite reaches a/(1 - r) (although it usually converges quite quickly).
        //we can use the formula for the first n terms, a1(1 - r ^ n) / (1 - r)
        float seriesSum = (1 - FastMath.pow(ratio, trigFunctions)) / (1 - ratio);
        
        
        int[]vectors = new int[trigFunctions * 2]; 
        //create 3 dimensional vectors, corresponding to the coefficients of each trig function, then make sure all vectors are nonzero
        for(int i = 0; i < trigFunctions; i++){
            int maxComponentSize = i + 1 + initialVectorComponentSize;
            vectors[i * 2] = FastMath.rand.nextInt(maxComponentSize);
            vectors[i * 2 + 1] = FastMath.rand.nextInt(maxComponentSize);
            if(vectors[i * 2] == 0 &&
               vectors[i * 2 + 1] == 0) 
                    vectors[i * 2 + FastMath.rand.nextInt(2)] = 1; //provide a unit vector if the 0 vector is given.
        }
        //form a series that converges to 128
        float[] scalars = new float[trigFunctions];
        scalars[0] = ((float)baseValue) / seriesSum;
        for(int i = 1; i < trigFunctions; i++){
            scalars[i] = scalars[i - 1] * ratio;
        }//the functions form a convergent series.
        

        
        float trigScalar = FastMath.TWO_PI / width;
        
                for(int y = 0; y < width; y++){
                    int yIndex = width * y;
                    for(int x = 0; x < width; x++){
                        int index = yIndex + x;
                        //we now have an index and an x y z.
                        float noiseValue = 0;
                        for(int i = 0; i < trigFunctions; i++){
                            int baseVector = i * 2;
                            noiseValue += fastSine((
                                    x * vectors[baseVector    ] + 
                                    y * vectors[baseVector + 1]) *
                                    trigScalar) * scalars[i];
                        }
                        noise[index] = (byte)(baseValue + noiseValue + rand.nextInt(random));
                    }
                }
        return noise;
    }
        
    public static byte[] noise1(int width){
        byte[] noise = new byte[width + 1];
        float trigConstant = FastMath.TWO_PI / width;
        for(int i = 0; i < width; i++){
            noise[i] = (byte)(fastSine(trigConstant * i * 3) * 8 + fastCosine(trigConstant * i * 5) * 16 + fastSine(trigConstant * i * 7) * 32 + 64 + FastMath.rand.nextInt(8));
        }
        return noise;
    }
    public static byte[] noise2(int width, int height){
        byte[] noise = new byte[width + 1];
        float tx = FastMath.TWO_PI / width;
        float ty = FastMath.TWO_PI / height;
        for(int x = 0; x < width; x++)
            for(int y = 0; y < height; y++)
                noise[x + y * width] = (byte)(fastSine(tx * x * 3) * 8 + fastCosine(tx * y * 5) * 16 + fastSine(ty * y * 7) * 16 + fastCosine(tx * x * 11) * 16 + 64 + FastMath.rand.nextInt(8));
        return noise;
    }
    
    //creates some interesting noise.  
    public static byte[] noise3(int xSize, int ySize, int zSize){
        int wh = xSize * ySize;
        byte[] noise = new byte[xSize * ySize * ySize];
        float tx = FastMath.TWO_PI / xSize;
        float ty = FastMath.TWO_PI / ySize;
        float tz = FastMath.TWO_PI / zSize;
        for(int x = 0; x < xSize; x++)
            for(int y = 0; y < ySize; y++)
                for(int z = 0; z < zSize; z++)
                noise[x + y * xSize + z * wh] = (byte)(
                        fastSine(tx * x * 3 + ty * y * 5) * 8 + 
                        fastCosine(ty * y * 5 - tz * z * 7) * 8 + 
                        fastSine(tz * z * 7 + tx * x * 11) * 8 + 
                        fastSine(tx * x * 11 - ty * y * 13) * 8 + 
                        fastCosine(ty * y * 13 + tz * z * 17) * 8 + 
                        fastSine(tz * z * 17 - tx * x * 19) * 8 + 
                        fastSine(tx * x * 19 + ty * y * 23) * 8 + 
                        fastCosine(ty * y * 23 - tz * z * 27) * 8 + 
                        fastSine(tz * z * 27 + tx * x * 29) * 8 + 
                        64 + 
                        FastMath.rand.nextInt(8));
        return noise;
    }
    public static byte[] perlinNoise3(int xSize, int ySize, int zSize){
        //build a 2d noise function sampler.
        int width = 32;
        int wPlus = width + 1;
        byte[] samples = new byte[wPlus * wPlus];
        for(int x = 0; x < width; x++)
            for(int y = 0; y < width; y++){
                samples[x + y * wPlus] = (byte)(FastMath.rand.nextInt() * 128);
            }
        for(int i = 0; i < width; i++){
            samples[width + wPlus * i] = samples[wPlus * i];
            samples[wPlus * width + i] = samples[i];//bottom
        }
        samples[wPlus * wPlus - 1] = samples[0];
        
        //initialize n 1d noise functions sampling out of created noise function
        int rFuncs = 6;
        float[] xFunc = new float[rFuncs];//scale<xFunc[i], yFunc[i]> is a vector that is stepped along to create samples.  It repeats smoothly every xSize
        float[] yFunc = new float[rFuncs];
        for(int i = 0; i < rFuncs; i++){
            xFunc[i] = FastMath.rand.nextFloat();
            yFunc[i] = FastMath.rand.nextFloat();
            float scale = FastMath.sqrt(xFunc[i] * xFunc[i] + yFunc[i] * yFunc[i]) / xSize;
            xFunc[i] *= scale; //|vector| = 1 / size.  Normalize and divide by size
            yFunc[i] *= scale; 
        }
        
        //use functions to build a 3d noise texture.  
        
        byte[] data = new byte[xSize * ySize * zSize];
        int xySize = xSize * ySize;
        for(int x = 0; x < xSize; x++)
            for(int y = 0; y < ySize; y++)
                for(int z = 0; z < zSize; z++){
                        float sampX = xFunc[0] * x;
                        float sampY = yFunc[0] * y;
                        float a = sampX + sampY;
                        int i1 = (int)a;
                        float xFrac = sampX - (int)sampX;
                        float yFrac = sampY - (int)sampY;
                        data[x + y * xSize + z * xySize] = (byte)((samples[i1] * (2 - xFrac - yFrac) + samples[i1 + 1] * xFrac + samples[i1 + wPlus] * yFrac) * .5f);
                }
        return data;
    }
    public static byte noiseFunction(byte[] noise, float pos){
        int index = (int)pos;
        index %= noise.length - 1;
        float frac = pos - (int)pos;
        return (byte)(noise[index] * frac + noise[index + 1] * (1 - frac));
    }
    
    
    public static float fastSine(float theta) {
        float sin = 0;
        if (theta > 6.28318531f) {
            theta %= 6.28318531f;
        }
        //always wrap input angle to -PI..PI
        if (theta < -3.14159265f) {
            theta += 6.28318531f;
        } else if (theta > 3.14159265f) {
            theta -= 6.28318531f;
        }

        //compute sine
        if (theta < 0) {
            sin = 1.27323954f * theta + .405284735f * theta * theta;
        } else {
            sin = 1.27323954f * theta - 0.405284735f * theta * theta;
        }
        return sin;

    }
    
    public static byte noiseFunction(byte[] b, int x, int y, int width){
        return b[(x + y * width) % (width * width)];
    }
    public static float byteToFraction = 1f/128;
    public static float byteToHalfFraction = 1f / 256;
    //returns the noise value, assuming input & output scaled between 0 & 1
    public static float noiseFunction(byte[] b, float x, float y, int width){
            //correct for negatives
            if(x < 0) x -= (int) (x - 1);
            if(y < 0) y -= (int) (y - 1);
            // 1/width should be stored somewhere so we can multiply.
            x *= width;
            y *= width;
            
            int wSqr = b.length;
            
            int xInt = (int) x;
            float xb = x - xInt;
            float xa = 1 - xb;
            int yInt = (int) y;
            float yb = y - yInt;
            float ya = 1 - yb;
            int bIndex = xInt + yInt * width;
            return (((b[(bIndex) % wSqr] & 0xff) * xa + (b[(bIndex + 1) % wSqr] & 0xff) * xb) * ya + 
                    ((b[(bIndex + width) % wSqr] & 0xff) * xa + (b[(bIndex + width + 1) % wSqr] & 0xff) * xb) * yb) * byteToHalfFraction;
    }

    public static float fastCosine(float theta) {
        return fastSine(theta += 1.57079632f);

    }
    public static int intPower(int base, int exponent){
        int val = 1;
        for(int i = 0; i < exponent; i++){
            val *= base;
        }
        return val;
    }

}