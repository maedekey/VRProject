package particles;


import entities.Camera;
import entities.Player;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import renderEngine.DisplayManager;

/**
 * Class representing a particle showing on the screen.
 */
public class Particle {

    private final Vector3f position;
    private final Vector3f velocity; //direction(coordinates) and speed(length) of the particles
    private final float gravityEffect; //if = 1: the particles effect are affected normally by gravity, any values less than1
    private final float lifeLength; //how long does the particle exist
    private final float rotation;
    private final float scale;
    private final ParticleTexture texture;
    private final Vector2f textOffset1 = new Vector2f(); //One offset for the current texture in the texture atlas (current stage of the animation)
    private final Vector2f textOffset2 = new Vector2f(); //Second offset for the next texture in the texture atlas (next stage of the animation)
    private float blend; //blend factor saying how much the 1st texture has to be blent into the second texture
    private float elapsedTime = 0;//for how long the particle has been existing
    private float distance;

    public Particle(ParticleTexture texture, Vector3f position, Vector3f velocity, float gravityEffect, float lifeLength, float rotation, float scale) {
        this.texture = texture;
        this.position = position;
        this.velocity = velocity;
        this.gravityEffect = gravityEffect;
        this.lifeLength = lifeLength;
        this.rotation = rotation;
        this.scale = scale;
        ParticleMaster.addParticles(this); //everytime a particle is created, we add it to the list of particles
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public Vector2f getTextOffset1() {
        return textOffset1;
    }

    public Vector2f getTexOffset2() {
        return textOffset2;
    }

    public float getBlend() {
        return blend;
    }

    public ParticleTexture getTexture() {
        return texture;
    }

    public Vector3f getPosition() {
        return position;
    }

    public float getRotation() {
        return rotation;
    }

    public float getScale() {
        return scale;
    }

    /**
     * Method that updates the position of the particle on the screen. First we retrieve the time during which the
     * particle must be moving, as well as its velocity. Once we did that, we can find the position of where it should
     * end its animation. We also update the texture of the particle along its animation. Finally, we make the particle
     * disappear from the screen once its time to live has expired.
     * @param camera: the particle must always be facing the camera, as it's in 2D, we don't want to see it paper thin on another angle
     * @return boolean describing if the particle is still alive or not
     */
    protected boolean update(Camera camera){

        velocity.y += Player.GRAVITY * gravityEffect * DisplayManager.getFrameTimeSeconds(); //we multiply by frametimeseconds because it's an animation
        Vector3f change = new Vector3f(velocity); //how much the particle position should change each frame
        change.scale(DisplayManager.getFrameTimeSeconds()); //if we get the velocity and the time, we can find how far the particle goes. We can then move the particle by increasing its position
        Vector3f.add(change, position, position);
        distance = Vector3f.sub(camera.getPosition(), position, null).lengthSquared();
        updateTextureCoordInfo();
        elapsedTime += DisplayManager.getFrameTimeSeconds();
        return elapsedTime < lifeLength; //the particle stops existing once its life length is bigger than the elasped time
    }

    /**
     * Method that updates at every frame the stage of the animation of the particle. We do the animation with a texture
     * atlas. First, we compute with the time when we should be in the texture atlas. We obtain a number, from which we
     * can obtain the current stage, the next stage, and based on the decimal part of the number, we can operate the
     * transition smoothly.
     */
    private void updateTextureCoordInfo(){
        float lifeFactor = elapsedTime/lifeLength; //0: animation just started, 1: animation just finished
        int stageCount = texture.getNumberOfRows() * texture.getNumberOfRows();
        float atlasProgression = lifeFactor * stageCount; //we get a float. integer part: the box in which the animation is, and the float part is the progression in this box. This is the blend factor
        int index1 = (int) Math.floor(atlasProgression); //index of current stage
        int index2 = index1 < stageCount -1? index1 +1: index1; //index of nextStage, only if the current stage isn't the last stage
        this.blend = atlasProgression%1;
        setTextureOffset(textOffset1, index1);
        setTextureOffset(textOffset2, index2);
    }

    /**
     * Method which allows to retrieve the column and row of the texture in the texture atlas.
     * @param offset current and next stage of the animation
     * @param index index of the next stage
     */
    private void setTextureOffset(Vector2f offset, int index){
        int column = index % texture.getNumberOfRows();
        int row = index / texture.getNumberOfRows();
        offset.x = (float)column /texture.getNumberOfRows();
        offset.y = (float) row/texture.getNumberOfRows();
    }
}
