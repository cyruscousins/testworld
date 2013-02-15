package world.cloud;

import com.jme3.bounding.BoundingBox;
import com.jme3.effect.Particle;
import com.jme3.effect.influencers.DefaultParticleInfluencer;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.shapes.EmitterPointShape;
import com.jme3.effect.shapes.EmitterShape;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.util.TempVars;
import java.io.IOException;

/**
 * CloudManager manages the creation, destruction, and movement of clouds.
 * CloudManager is a node, and clouds are collections of GLPoints
 */
public class CloudManager extends Geometry {
    private boolean enabled = true;
    private CloudManager.CloudManagerControl control;
    private ParticleMesh particleMesh;
    
    //Maintain particle info.  
    int particleCount;
    private Vector3f[] locations;
    private Vector3f[] velocities;
    private float[] sizes;
    private float[] masses;
    private int[] imageIndices;
    
    private int firstUnUsed;
    private int lastUsed;

    private boolean randomAngle;
    private boolean selectRandomImage;
    private boolean facingVelocity;
    private float particlesPerSec = 20;
    private float timeDifference = 0;
    private float lowLife = 3f;
    private float highLife = 7f;
    
    //Animation Stuff
    //TODO use Tex3D for animations
    private int imagesX = 1;
    private int imagesY = 1;
    
    //Physics Stuff
    private Vector3f gravity = new Vector3f(0.0f, 0.1f, 0.0f);
    private float rotateSpeed;
    private Vector3f faceNormal = new Vector3f(Vector3f.NAN);
   
    //Start/End stuff
    //TODO redo this.  Interpolate between an arbitrary number of such variables.
    private ColorRGBA startColor = new ColorRGBA(0.4f, 0.4f, 0.4f, 0.5f);
    private ColorRGBA endColor = new ColorRGBA(0.1f, 0.1f, 0.1f, 0.0f);
    private float startSize = 0.2f;
    private float endSize = 2f;
    private boolean worldSpace = true;
    //variable that helps with computations
    private transient Vector3f temp = new Vector3f();

    public static class CloudManagerControl implements Control {

        CloudManager parentEmitter;

        public CloudManagerControl() {
            
        }

        public CloudManagerControl(CloudManager parentEmitter) {
            this.parentEmitter = parentEmitter;
        }

        public Control cloneForSpatial(Spatial spatial) {
            return this; // WARNING: Sets wrong control on spatial. Will be
            // fixed automatically by CloudManager.clone() method.
        }

        public void setSpatial(Spatial spatial) {
        }

        public void setEnabled(boolean enabled) {
            parentEmitter.setEnabled(enabled);
        }

        public boolean isEnabled() {
            return parentEmitter.isEnabled();
        }

        public void update(float tpf) {
            parentEmitter.updateFromControl(tpf);
        }

        public void render(RenderManager rm, ViewPort vp) {
            parentEmitter.renderFromControl(rm, vp);
        }

        public void write(JmeExporter ex) throws IOException {
        }

        public void read(JmeImporter im) throws IOException {
        }
    }

    /*
    @Override
    public CloudManager clone() {
        return clone(true);
    }

    @Override
    public CloudManager clone(boolean cloneMaterial) {
        CloudManager clone = (CloudManager) super.clone(cloneMaterial);

        // Reinitialize particle list
        clone.setNumParticles(particleCount);

        clone.faceNormal = faceNormal.clone();
        clone.startColor = startColor.clone();
        clone.endColor = endColor.clone();

        // remove wrong control
        clone.controls.remove(control);

        // put correct control
        clone.controls.add(new CloudManager.CloudManagerControl(clone));

        // Reinitialize particle mesh
        clone.particleMesh = new ParticleMesh(clone.particles.length);
        clone.setMesh(particleMesh);
        clone.particleMesh.setImagesXY(clone.imagesX, clone.imagesY);

        return clone;
    }

    public CloudManager(String name, int numParticles) {
        super(name);

        // ignore world transform, unless user sets inLocalSpace
        this.setIgnoreTransform(true);

        // particles neither receive nor cast shadows
        this.setShadowMode(ShadowMode.Off);

        // particles are usually transparent
        this.setQueueBucket(Bucket.Transparent);

        control = new CloudManager.CloudManagerControl(this);
        controls.add(control);

        particleMesh = new ParticleMesh(numParticles);
        setMesh(particleMesh);
        
        this.setNumParticles(numParticles);
//        particleMesh.initParticleData(this, particles.length);
    }
    */
    
    /**
     * For serialization only. Do not use.
     */
    public CloudManager() {
        super();
    }

    //TODO what is this?
    /**
     * Returns true if particles should spawn in world space. 
     * 
     * @return true if particles should spawn in world space. 
     * 
     * @see CloudManager#setInWorldSpace(boolean) 
     */
    public boolean isInWorldSpace() {
        return worldSpace;
    }

    /**
     * Set to true if particles should spawn in world space. 
     * 
     * <p>If set to true and the particle emitter is moved in the scene,
     * then particles that have already spawned won't be effected by this
     * motion. If set to false, the particles will emit in local space
     * and when the emitter is moved, so are all the particles that
     * were emitted previously.
     * 
     * @param worldSpace true if particles should spawn in world space. 
     */
    public void setInWorldSpace(boolean worldSpace) {
        this.setIgnoreTransform(worldSpace);
        this.worldSpace = worldSpace;
    }

    /**
     * Returns the number of visible particles (spawned but not dead).
     * 
     * @return the number of visible particles
     */
    public int getNumVisibleParticles() {
//        return unusedIndices.size() + next;
        return lastUsed + 1;
    }

    /**
     * Set the maximum amount of particles that
     * can exist at the same time with this emitter.
     * Calling this method many times is not recommended.
     * 
     * @param numParticles the maximum amount of particles that
     * can exist at the same time with this emitter.
     */
    public final void setNumParticles(int numParticles) {
        if(numParticles > this.particleCount){ //Allocate more memory if necessary.
            locations = new Vector3f[numParticles];
            velocities = new Vector3f[numParticles];
            masses = new float[numParticles];
            imageIndices = new int[numParticles];
        }
        this.particleCount = numParticles;
        
        //We have to reinit the mesh's buffers with the new size
        particleMesh.setNumParticles(particleCount);
        particleMesh.setImagesXY(this.imagesX, this.imagesY);
        firstUnUsed = 0;
        lastUsed = -1;
    }

    public int getMaxNumParticles() {
        return particleCount;
    }

    /**
     * Returns true if every particle spawned should get a random
     * image.
     * 
     * @return True if every particle spawned should get a random
     * image.
     * 
     * @see CloudManager#setSelectRandomImage(boolean) 
     */
    public boolean isSelectRandomImage() {
        return selectRandomImage;
    }

    /**
     * Set to true if every particle spawned
     * should get a random image from a pool of images constructed from
     * the texture, with X by Y possible images.
     * 
     * <p>By default, X and Y are equal
     * to 1, thus allowing only 1 possible image to be selected, but if the
     * particle is configured with multiple images by using {@link CloudManager#setImagesX(int) }
     * and {#link CloudManager#setImagesY(int) } methods, then multiple images
     * can be selected. Setting to false will cause each particle to have an animation
     * of images displayed, starting at image 1, and going until image X*Y when
     * the particle reaches its end of life.
     * 
     * @param selectRandomImage True if every particle spawned should get a random
     * image.
     */
    public void setSelectRandomImage(boolean selectRandomImage) {
        this.selectRandomImage = selectRandomImage;
    }

    /**
     * Check if particles spawned should face their velocity.
     * 
     * @return True if particles spawned should face their velocity.
     * 
     * @see CloudManager#setFacingVelocity(boolean) 
     */
    public boolean isFacingVelocity() {
        return facingVelocity;
    }

    /**
     * Get the end color of the particles spawned.
     * 
     * @return the end color of the particles spawned.
     * 
     * @see CloudManager#setEndColor(com.jme3.math.ColorRGBA) 
     */
    public ColorRGBA getEndColor() {
        return endColor;
    }

    /**
     * Set the end color of the particles spawned.
     * 
     * <p>The
     * particle color at any time is determined by blending the start color
     * and end color based on the particle's current time of life relative
     * to its end of life.
     * 
     * @param endColor the end color of the particles spawned.
     */
    public void setEndColor(ColorRGBA endColor) {
        this.endColor.set(endColor);
    }

    /**
     * Get the end size of the particles spawned.
     * 
     * @return the end size of the particles spawned.
     * 
     * @see CloudManager#setEndSize(float) 
     */
    public float getEndSize() {
        return endSize;
    }

    /**
     * Set the end size of the particles spawned.
     * 
     * <p>The
     * particle size at any time is determined by blending the start size
     * and end size based on the particle's current time of life relative
     * to its end of life.
     * 
     * @param endSize the end size of the particles spawned.
     */
    public void setEndSize(float endSize) {
        this.endSize = endSize;
    }

    /**
     * Get the gravity vector.
     * 
     * @return the gravity vector.
     * 
     * @see CloudManager#setGravity(com.jme3.math.Vector3f) 
     */
    public Vector3f getGravity() {
        return gravity;
    }

    /**
     * This method sets the gravity vector.
     * 
     * @param gravity the gravity vector
     */
    public void setGravity(Vector3f gravity) {
        this.gravity.set(gravity);
    }

    /**
     * Sets the gravity vector.
     * 
     * @param x the x component of the gravity vector
     * @param y the y component of the gravity vector
     * @param z the z component of the gravity vector
     */
    public void setGravity(float x, float y, float z) {
        this.gravity.x = x;
        this.gravity.y = y;
        this.gravity.z = z;
    }

    /**
     * Get the high value of life.
     * 
     * @return the high value of life.
     * 
     * @see CloudManager#setHighLife(float) 
     */
    public float getHighLife() {
        return highLife;
    }

    /**
     * Set the high value of life.
     * 
     * <p>The particle's lifetime/expiration
     * is determined by randomly selecting a time between low life and high life.
     * 
     * @param highLife the high value of life.
     */
    public void setHighLife(float highLife) {
        this.highLife = highLife;
    }

    /**
     * Get the number of images along the X axis (width).
     * 
     * @return the number of images along the X axis (width).
     * 
     * @see CloudManager#setImagesX(int) 
     */
    public int getImagesX() {
        return imagesX;
    }

    /**
     * Set the number of images along the X axis (width).
     * 
     * <p>To determine
     * how multiple particle images are selected and used, see the
     * {@link CloudManager#setSelectRandomImage(boolean) } method.
     * 
     * @param imagesX the number of images along the X axis (width).
     */
    public void setImagesX(int imagesX) {
        this.imagesX = imagesX;
        particleMesh.setImagesXY(this.imagesX, this.imagesY);
    }

    /**
     * Get the number of images along the Y axis (height).
     * 
     * @return the number of images along the Y axis (height).
     * 
     * @see CloudManager#setImagesY(int) 
     */
    public int getImagesY() {
        return imagesY;
    }

    /**
     * Set the number of images along the Y axis (height).
     * 
     * <p>To determine how multiple particle images are selected and used, see the
     * {@link CloudManager#setSelectRandomImage(boolean) } method.
     * 
     * @param imagesY the number of images along the Y axis (height).
     */
    public void setImagesY(int imagesY) {
        this.imagesY = imagesY;
        particleMesh.setImagesXY(this.imagesX, this.imagesY);
    }

    /**
     * Get the low value of life.
     * 
     * @return the low value of life.
     * 
     * @see CloudManager#setLowLife(float) 
     */
    public float getLowLife() {
        return lowLife;
    }

    /**
     * Set the low value of life.
     * 
     * <p>The particle's lifetime/expiration
     * is determined by randomly selecting a time between low life and high life.
     * 
     * @param lowLife the low value of life.
     */
    public void setLowLife(float lowLife) {
        this.lowLife = lowLife;
    }

    /**
     * Get the number of particles to spawn per
     * second.
     * 
     * @return the number of particles to spawn per
     * second.
     * 
     * @see CloudManager#setParticlesPerSec(float) 
     */
    public float getParticlesPerSec() {
        return particlesPerSec;
    }

    /**
     * Set the number of particles to spawn per
     * second.
     * 
     * @param particlesPerSec the number of particles to spawn per
     * second.
     */
    public void setParticlesPerSec(float particlesPerSec) {
        this.particlesPerSec = particlesPerSec;
        timeDifference = 0;
    }
    
    /**
     * Get the start color of the particles spawned.
     * 
     * @return the start color of the particles spawned.
     * 
     * @see CloudManager#setStartColor(com.jme3.math.ColorRGBA) 
     */
    public ColorRGBA getStartColor() {
        return startColor;
    }

    /**
     * Set the start color of the particles spawned.
     * 
     * <p>The particle color at any time is determined by blending the start color
     * and end color based on the particle's current time of life relative
     * to its end of life.
     * 
     * @param startColor the start color of the particles spawned
     */
    public void setStartColor(ColorRGBA startColor) {
        this.startColor.set(startColor);
    }

    /**
     * Get the start color of the particles spawned.
     * 
     * @return the start color of the particles spawned.
     * 
     * @see CloudManager#setStartSize(float) 
     */
    public float getStartSize() {
        return startSize;
    }

    /**
     * Set the start size of the particles spawned.
     * 
     * <p>The particle size at any time is determined by blending the start size
     * and end size based on the particle's current time of life relative
     * to its end of life.
     * 
     * @param startSize the start size of the particles spawned.
     */
    public void setStartSize(float startSize) {
        this.startSize = startSize;
    }

    //TODO this
    private int emitParticle(Vector3f min, Vector3f max, Vector3f position) {
        int idx = lastUsed + 1;
        if (idx >= particleCount) {
            return -1;
        }

        if (selectRandomImage) {
            imageIndices[idx] = FastMath.nextRandomInt(0, imagesY - 1) * imagesX + FastMath.nextRandomInt(0, imagesX - 1);
        }

        masses[idx] = .1f + FastMath.rand.nextFloat();
        velocities[idx] = new Vector3f(FastMath.rand.nextFloat() - .5f, FastMath.rand.nextFloat() - .5f, FastMath.rand.nextFloat() - .5f);
        
        if (worldSpace) {
            worldTransform.transformVector(locations[idx], locations[idx]);
            worldTransform.getRotation().mult(velocities[idx], velocities[idx]);
            // TODO: Make scale relevant somehow??
        }
        
        locations[idx].set(position);

        temp.set(locations[idx]).addLocal(sizes[idx], sizes[idx], sizes[idx]);
        max.maxLocal(temp);
        temp.set(locations[idx]).subtractLocal(sizes[idx], sizes[idx], sizes[idx]);
        min.minLocal(temp);

        ++lastUsed;
        firstUnUsed = idx + 1;
        return idx;
    }

    /**
     * Instantly emits all the particles possible to be emitted. Any particles
     * which are currently inactive will be spawned immediately.
     */
    public void emitAllParticles() {
        // Force world transform to update
        this.getWorldTransform();

        TempVars vars = TempVars.get();

        BoundingBox bbox = (BoundingBox) this.getMesh().getBound();

        Vector3f min = vars.vect1;
        Vector3f max = vars.vect2;
        
//        System.out.println("MIN: " + min);
//        System.out.println("Max: " + max);
//        System.exit(1);

        bbox.getMin(min);
        bbox.getMax(max);
        
        min.x = min.z = -512;
        max.x = max.z = 512;
        
        min.y = 0;
        max.y = 256;
        

        if (!Vector3f.isValidVector(min)) {
            min.set(Vector3f.POSITIVE_INFINITY);
        }
        if (!Vector3f.isValidVector(max)) {
            max.set(Vector3f.NEGATIVE_INFINITY);
        }

        Vector3f pos = new Vector3f();
        do{
            pos.x = FastMath.rand.nextFloat() * 512 - 1024;
            pos.y = FastMath.rand.nextFloat() * 512 + 512;
            pos.z = FastMath.rand.nextFloat() * 512 - 1024;
        }
        while (emitParticle(min, max, pos) != -1);

        bbox.setMinMax(min, max);
        
//        System.out.println("MIN: " + bbox.getMin(new Vector3f()));
//        System.out.println("MAX: " + bbox.getMax(new Vector3f()));
//        System.exit(0);
        
        this.setBoundRefresh();

        vars.release();
    }

    /**
     * Instantly kills all active particles, after this method is called, all
     * particles will be dead and no longer visible.
     */
    public void killAllParticles() {
        //TODO
//        for (int i = 0; i < particleCount; ++i) {
//            if (particles[i].life > 0) {
//                this.freeParticle(i);
//            }
//        }
    }
    
    /**
     * Kills the particle at the given index.
     * 
     * @param index The index of the particle to kill
     * @see #getParticles() 
     */
    public void killParticle(int index){
        freeParticle(index);
    }

    private void freeParticle(int idx) {
        
        sizes[idx] = 0f;

        //DO THIS!
        if (idx == lastUsed) {
            while (lastUsed >= 0 && particles[lastUsed].life == 0) {
                lastUsed--;
            }
        }
        if (idx < firstUnUsed) {
            firstUnUsed = idx;
        }
    }

//TODO
//    private void swap(int idx1, int idx2) {
//        Particle p1 = particles[idx1];
//        particles[idx1] = particles[idx2];
//        particles[idx2] = p1;
//    }

    private void updateParticle(int idx, float tpf, Vector3f min, Vector3f max){
        // applying gravity
        velocities[idx].x -= gravity.x * tpf;
        velocities[idx].y -= gravity.y * tpf;
        velocities[idx].z -= gravity.z * tpf;
        temp.set(velocities[idx]).multLocal(tpf);
        locations[idx].addLocal(temp);

        // affecting color, size and angle
        float b = (p.startlife - p.life) / p.startlife;
        p.color.interpolate(startColor, endColor, b);
        sizes[idx] = FastMath.interpolateLinear(b, startSize, endSize);
        p.angle += p.rotateSpeed * tpf;

        // Computing bounding volume
        temp.set(locations[idx]).addLocal(sizes[idx], sizes[idx], sizes[idx]);
        max.maxLocal(temp);
        temp.set(locations[idx]).subtractLocal(sizes[idx], sizes[idx], sizes[idx]);
        min.minLocal(temp);

        if (!selectRandomImage) {
            imageIndices[idx] = (int) (b * imagesX * imagesY);
        }
    }
    
    private void updateParticleState(float tpf) {
        // Force world transform to update
        this.getWorldTransform();

        TempVars vars = TempVars.get();

        Vector3f min = vars.vect1.set(Vector3f.POSITIVE_INFINITY);
        Vector3f max = vars.vect2.set(Vector3f.NEGATIVE_INFINITY);

        for (int i = 0; i < particleCount; ++i) {
            Particle p = particles[i];
            if (p.life == 0) { // particle is dead
//                assert i <= firstUnUsed;
                continue;
            }

            p.life -= tpf;
            if (p.life <= 0) {
                this.freeParticle(i);
                continue;
            }

            updateParticle(p, tpf, min, max);

            if (firstUnUsed < i) {
                this.swap(firstUnUsed, i);
                if (i == lastUsed) {
                    lastUsed = firstUnUsed;
                }
                firstUnUsed++;
            }
        }
        
        // Spawns particles within the tpf timeslot with proper age
        float interval = 1f / particlesPerSec;
        tpf += timeDifference;
        while (tpf > interval){
            tpf -= interval;
            Particle p = emitParticle(min, max, Vector3f.ZERO);
            if (p != null){
                p.life -= tpf;
                if (p.life <= 0){
                    freeParticle(lastUsed);
                }else{
                    updateParticle(p, tpf, min, max);
                }
            }
        }
        timeDifference = tpf;

        BoundingBox bbox = (BoundingBox) this.getMesh().getBound();
        bbox.setMinMax(min, max);
        this.setBoundRefresh();

        vars.release();
    }

    /**
     * Set to enable or disable the particle emitter
     * 
     * <p>When a particle is
     * disabled, it will be "frozen in time" and not update.
     * 
     * @param enabled True to enable the particle emitter
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Check if a particle emitter is enabled for update.
     * 
     * @return True if a particle emitter is enabled for update.
     * 
     * @see CloudManager#setEnabled(boolean) 
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Callback from Control.update(), do not use.
     * @param tpf 
     */
    public void updateFromControl(float tpf) {
        if (enabled) {
            this.updateParticleState(tpf);
        }
    }

    /**
     * Callback from Control.render(), do not use.
     * 
     * @param rm
     * @param vp 
     */
    private void renderFromControl(RenderManager rm, ViewPort vp) {
        Camera cam = vp.getCamera();

//        if (meshType == ParticleMesh.Type.Point) {
            float C = cam.getProjectionMatrix().m00;
            C *= cam.getWidth() * 0.5f;

            // send attenuation params
            this.getMaterial().setFloat("Quadratic", C);
//        }

        Matrix3f inverseRotation = Matrix3f.IDENTITY;  //TODO not necessary
        TempVars vars = null; //TODO not necessary
        
        if (!worldSpace) {
            vars = TempVars.get();

            inverseRotation = this.getWorldRotation().toRotationMatrix(vars.tempMat3).invertLocal(); //TODO is this line necessary?
        }
        
        particleMesh.updateParticleData(particles, cam);
        if (!worldSpace) { //TODO not necessary
            vars.release();
        }
    }

    public void preload(RenderManager rm, ViewPort vp) {
        this.updateParticleState(0);
        particleMesh.updateParticleData(particles, vp.getCamera());
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(enabled, "enabled", true);
        oc.write(particleCount, "numParticles", 0);
        oc.write(particlesPerSec, "particlesPerSec", 0);
        oc.write(lowLife, "lowLife", 0);
        oc.write(highLife, "highLife", 0);
        oc.write(gravity, "gravity", null);
        oc.write(imagesX, "imagesX", 1);
        oc.write(imagesY, "imagesY", 1);

        oc.write(startColor, "startColor", null);
        oc.write(endColor, "endColor", null);
        oc.write(startSize, "startSize", 0);
        oc.write(endSize, "endSize", 0);
        oc.write(worldSpace, "worldSpace", false);
        oc.write(facingVelocity, "facingVelocity", false);
        oc.write(faceNormal, "faceNormal", new Vector3f(Vector3f.NAN));
        oc.write(selectRandomImage, "selectRandomImage", false);
        oc.write(randomAngle, "randomAngle", false);
        oc.write(rotateSpeed, "rotateSpeed", 0);
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule ic = im.getCapsule(this);

        int numParticles = ic.readInt("numParticles", 0);


        enabled = ic.readBoolean("enabled", true);
        particlesPerSec = ic.readFloat("particlesPerSec", 0);
        lowLife = ic.readFloat("lowLife", 0);
        highLife = ic.readFloat("highLife", 0);
        gravity = (Vector3f) ic.readSavable("gravity", null);
        imagesX = ic.readInt("imagesX", 1);
        imagesY = ic.readInt("imagesY", 1);

        startColor = (ColorRGBA) ic.readSavable("startColor", null);
        endColor = (ColorRGBA) ic.readSavable("endColor", null);
        startSize = ic.readFloat("startSize", 0);
        endSize = ic.readFloat("endSize", 0);
        worldSpace = ic.readBoolean("worldSpace", false);
        this.setIgnoreTransform(worldSpace);
        facingVelocity = ic.readBoolean("facingVelocity", false);
        faceNormal = (Vector3f)ic.readSavable("faceNormal", new Vector3f(Vector3f.NAN));
        selectRandomImage = ic.readBoolean("selectRandomImage", false);
        randomAngle = ic.readBoolean("randomAngle", false);
        rotateSpeed = ic.readFloat("rotateSpeed", 0);

        particleMesh = new ParticleMesh();
        setMesh(particleMesh);
        this.setNumParticles(numParticles);
//        particleMesh.initParticleData(this, particleCount);
//        particleMesh.setImagesXY(imagesX, imagesY);

        if (im.getFormatVersion() == 0) {
            // compatibility before the control inside particle emitter
            // was changed:
            // find it in the controls and take it out, then add the proper one in
            for (int i = 0; i < controls.size(); i++) {
                Object obj = controls.get(i);
                if (obj instanceof CloudManager) {
                    controls.remove(i);
                    // now add the proper one in
                    controls.add(new CloudManager.CloudManagerControl(this));
                    break;
                }
            }

            // compatability before gravity was not a vector but a float
            if (gravity == null) {
                gravity = new Vector3f();
                gravity.y = ic.readFloat("gravity", 0);
            }
        } else {
            // since the parentEmitter is not loaded, it must be 
            // loaded separately
            control = getControl(CloudManager.CloudManagerControl.class);
            control.parentEmitter = this;

        }
    }
}
