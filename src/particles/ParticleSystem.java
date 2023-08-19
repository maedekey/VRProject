package particles;

import org.lwjgl.util.vector.Vector3f;
import renderEngine.DisplayManager;

/**
 * Class that defines a particle system. It generates particles, defines their speed, direction, time to live and their
 * compliance to gravity. It continually emits particles in a certain way.
 */
public class ParticleSystem {
    private final float pps;
    private final float speed;
    private final float gravityComplient;
    private final float lifeLength;
    private final ParticleTexture texture;

    public ParticleSystem(ParticleTexture texture, float pps, float speed, float gravityComplient, float lifeLength) {
        this.pps = pps; //number of particles per second that are emitted
        this.speed = speed; //speed of the particles
        this.gravityComplient = gravityComplient;   //how much gravity affects the particles
        this.lifeLength = lifeLength; //how long the particles stay alive for
        this.texture = texture;
    }

    /**
     * Method that is called every frame, generates particles per second and sets their parameters by calling another
     * method.
     * @param origin 3D pt in the world from where particles should be emitted.
     */
    public void generateParticles(Vector3f origin){
        float delta = DisplayManager.getFrameTimeSeconds();
        float particlesToCreate = pps * delta;
        int count = (int) Math.floor(particlesToCreate);
        float partialParticle = particlesToCreate % 1;
        for(int i=0;i<count;i++){
            emitParticle(origin);
        }
        if(Math.random() < partialParticle){
            emitParticle(origin);
        }
    }

    /**
     * Method that defines the direction of the particles (here, random), determines their speed and creates entities
     * of particles with these parameters and a texture.
     * @param origin 3D pt in the world from where particles should be emitted.
     */
    private void emitParticle(Vector3f origin){
        float dirX = (float) Math.random() * 2f - 1f;
        float dirZ = (float) Math.random() * 2f - 1f;
        Vector3f velocity = new Vector3f(dirX, 1, dirZ);
        velocity.normalise();
        velocity.scale(speed);
        new Particle(texture, new Vector3f(origin), velocity, gravityComplient, lifeLength, 0, 1);
    }

}
