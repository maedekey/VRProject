package particles;

import entities.Camera;
import org.lwjgl.util.vector.Matrix4f;
import renderEngine.Loader;

import java.util.*;

/**
 * class that keeps track of all the particles in the scene. Updates all particles remove all particles, and ends those
 * particles off to be rendered.
 */
public class ParticleMaster {

    private static final Map<ParticleTexture, List<Particle>> particles = new HashMap<>();
    private static ParticleRenderer renderer;

    public static void init(Loader loader, Matrix4f projectionMatrix){
        renderer = new ParticleRenderer(loader, projectionMatrix);
    }

    /**
     * Method that removes particles that have to be removed from the scene, updates their position, and sorts them.
     * @param camera
     */
    public static void update(Camera camera){
        //iterates through each list of particles, and for each list of particles, updates each of the particles in it, removing any particle needed to be removed
        Iterator<Map.Entry<ParticleTexture, List<Particle>>> mapIterator = particles.entrySet().iterator(); //creates an iterator for every entry in the map
        while(mapIterator.hasNext()){
            List<Particle> list = mapIterator.next().getValue();

            //goes through all the particles in the scene and updates every single one of them
            Iterator<Particle> iterator = list.iterator(); //will remove all particles from the list while iterating through it
            while(iterator.hasNext()){
                Particle p = iterator.next();
                boolean stillAlive = p.update(camera); //returns a bool saying whether the particle is still alive or not
                if(!stillAlive) { //if particle not alive anymore, remove it
                    iterator.remove();
                    if(list.isEmpty()){
                        mapIterator.remove();
                    }
                }
            }
            InsertionSort.sortHighToLow(list);
        }
    }

    public static void renderParticles(Camera camera){
        renderer.render(particles, camera);
    }

    public static void cleanup(){
        renderer.cleanUp();
    }

    /**
     * Method that adds particles to a list. We keep track of them to make it easier to clean particles and remove them,
     * or even update them. If the list doesn't exist, it's because it's the first particle created, so we create the
     * list.
     * @param particle
     */
    public static void addParticles(Particle particle){
        List<Particle> list = particles.computeIfAbsent(particle.getTexture(), k -> new ArrayList<>());
        list.add(particle);
    }

}

