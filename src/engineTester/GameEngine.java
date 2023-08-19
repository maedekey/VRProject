package engineTester;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import models.RawModel;
import models.TexturedModel;
import normalMappingObjConverter.NormalMappedObjLoader;
import objConverter.OBJFileLoader;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import particles.ParticleMaster;
import particles.ParticleSystem;
import particles.ParticleTexture;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import terrains.Terrain;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;

public class GameEngine {

	public static void main(String[] args) throws LWJGLException {

		DisplayManager.createDisplay();
		Loader loader = new Loader();

		// _____________________TERRAIN____________________

		TerrainTexturePack texturePack = generateTerrainTexture(loader);
		TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap"));
		Terrain terrain = new Terrain(0, -1, loader, texturePack, blendMap, "heightmap");
		List<Terrain> terrains = new ArrayList<>();
		terrains.add(terrain);

		// ________________MODELS_________________________

		ModelTexture fernTextureAtlas = new ModelTexture(loader.loadTexture("fernatlas"));
		fernTextureAtlas.setNumberOfRows(2);
		RawModel fernModel = OBJLoader.loadObjModel("fern", loader);
		TexturedModel fern = new TexturedModel(fernModel, fernTextureAtlas);
		fern.getTexture().setHasTransparency(true);
		fern.getTexture().setUseFakeLighting(true);

		TexturedModel pine = generateTransparentModels("pine", new ModelTexture(loader.loadTexture("pine")), loader);
		TexturedModel lamp = new TexturedModel(OBJLoader.loadObjModel("lamp", loader), new ModelTexture(loader.loadTexture("lamp")));
		lamp.getTexture().setUseFakeLighting(true);

		TexturedModel mainCharacter = new TexturedModel(OBJLoader.loadObjModel("steve", loader), new ModelTexture(loader.loadTexture("steve")));

		//__________________NORMAL MAP MODELS____________________

		TexturedModel barrelModel = generateNormalModel("barrel", loader, "barrelNormal");
		TexturedModel crateModel = generateNormalModel("crate", loader, "crateNormal");
		TexturedModel boulderModel = generateNormalModel("boulder", loader, "boulderNormal");

		//___________________ENTITIES___________________________

		List<Entity> entities = new ArrayList<>();
		List<Entity> normalEntities = new ArrayList<>();

		placeNormalEntitiesOnTerrain(terrain, barrelModel, crateModel, boulderModel, normalEntities);
		placeEntitiesOnTerrain(terrain, fern, pine, entities);
		Player player = new Player(mainCharacter, new Vector3f(75, 5, -75), 0, 100, 0, 10f);
		entities.add(player);

		//_______________________LIGHTS__________________________

		List<Light> lights = new ArrayList<>();
		generateLights(terrain, lamp, entities, lights);

		//_____________________PARTICLES_____________________

		MasterRenderer renderer = new MasterRenderer(loader);
		ParticleSystem particleSystem = generateParticles(loader, renderer);

		//_____________________CAMERA____________________
		Camera camera = new Camera(player);

		//_________________________GAME LOOP__________________________

		while (!Display.isCloseRequested()) {
			renderAndDisplay(terrain, terrains, entities, normalEntities, player, lights, renderer, particleSystem, camera);
		}

		cleanup(loader, renderer);
	}

	/**
	 * Method where all objects are updated at every frame, then rendered
	 * @param terrain generated terrain on which the player moves. We need to know its height so that the player can stay on the terrain and not go in it
	 * @param terrains list of terrains that have to be rendered
	 * @param entities list of entities which
	 * @param normalEntities list of normal entities (with bump mapping) that have to be rendered
	 * @param player main character, player that can be moved on the terrain
	 * @param lights lights that affect the scene (both the terrain and the player)
	 * @param renderer responsible for rendering, showing objects on the screen after their position was updated
	 * @param particleSystem particles that show on the screen. Here, they are generated at the player positions, to give a certain effect when the player moves.
	 * @param camera camera responsible for how we see the scene. It follows the player and can be turned around him.
	 */
	private static void renderAndDisplay(Terrain terrain, List<Terrain> terrains, List<Entity> entities, List<Entity> normalEntities, Player player, List<Light> lights, MasterRenderer renderer, ParticleSystem particleSystem, Camera camera) {
		player.move(terrain);
		camera.move();
		particleSystem.generateParticles(player.getPosition());
		ParticleMaster.update(camera);
		GL11.glEnable(GL30.GL_CLIP_DISTANCE0); //allows to not render things that are outside of sight. Increases performances
		renderer.renderScene(entities, normalEntities, terrains, lights, camera, new Vector4f(0, -1, 0, 100000));
		ParticleMaster.renderParticles(camera);
		DisplayManager.updateDisplay();
	}


	/**
	 * Method that generates particles on the screen. First, it initializes the particle master, then loads a particle
	 * texture and determines its parameters (amount of particles per second, their speed, how much they're affected by
	 * gravity and how long they stay on the screen).
	 * @param loader object allowing to load textures to be rendered
	 * @param renderer responsible for showing the particles on the screen
	 * @return the particle system with a texture and parameters
	 */
	private static ParticleSystem generateParticles(Loader loader, MasterRenderer renderer) {
		ParticleMaster.init(loader, renderer.getProjectionMatrix());
		ParticleTexture particleTexture = new ParticleTexture(loader.loadTexture("particleAtlas"), 4);
		return new ParticleSystem(particleTexture, 50,25, 0.3f, 4);
	}

	/**
	 * Method that generates a lamp with a point light in it. First, we retrieve the height of the terrain where we add
	 * the lamp, then we retrieve a lamp model which has a lamp texture, and finally, we add a point light in function of
	 * its position.
	 * @param terrain on which the lamp should stand
	 * @param worldX X position of the lamp on the world
	 * @param worldZ Z position of the lamp on the world
	 * @param entities list of all the current entities in the world. We keep track of them to remove them later when they're not rendered anymore for optimisation concerns
	 * @param lamp lamp model to add to the world
	 * @param lights list of lights. We keep track of them to remove them later when they're not rendered anymore for optimisation concerns
	 * @param colour vector of 3 floats containing the rgb indices for the colour of the light
	 * @param attenuation vector of 3 floats containing indices of attenuation
	 */
	private static void generateLamp(Terrain terrain, int worldX, int worldZ,List<Entity> entities, TexturedModel lamp,List<Light> lights, Vector3f colour, Vector3f attenuation){
		float y;
		y = terrain.getHeightOfTerrain(worldX,worldZ);
		entities.add(new Entity(lamp, new Vector3f(worldX,y,worldZ), 0f, 0f, 0f, 1));
		lights.add(new Light(new Vector3f(worldX,y+15,worldZ), colour, attenuation));
	}

	/**
	 * Method that generates a main light to the scene, that is far away, bright that will be our sun. Then, it adds
	 * lamps to the scene
	 * @param terrain on which the lights have to be put
	 * @param lamp model to use to generate lamps on the screen
	 * @param entities list of all the entities in the world
	 * @param lights list of all the lights in the world
	 */
	private static void generateLights(Terrain terrain, TexturedModel lamp, List<Entity> entities, List<Light> lights) {
		Light sun = new Light(new Vector3f(10000, 10000, -10000), new Vector3f(1.3f, 1.3f, 1.3f));
		lights.add(sun);
		generateLamp(terrain, 80,-80, entities, lamp, lights,new Vector3f(2,0,0), new Vector3f(1,0.01f,0.002f));
		generateLamp(terrain, 110,-110, entities, lamp, lights,new Vector3f(0,2,2), new Vector3f(1,0.01f,0.002f));
		generateLamp(terrain, 125,-125, entities, lamp, lights,new Vector3f(2,2,0), new Vector3f(1,0.01f,0.002f));
	}

	/**
	 * Method that places randomly on the terrain ferns and pines. It generates a random number, which we multiply so
	 * that it is concordant with our coordinates and can be seen on the screen. Finally, we generate the height at this
	 * position and put an entity on it.
	 * @param terrain on which entities have to be put
	 * @param fern model used to generate a fern entity
	 * @param pine model used to generate a pine entity
	 * @param entities list of all the entities in the world
	 */
	private static void placeEntitiesOnTerrain(Terrain terrain, TexturedModel fern, TexturedModel pine, List<Entity> entities) {
		float y;
		Random random = new Random(5666778);
		for (int i = 0; i < 60; i++) {
			if (i % 3 == 0) {
				float x = random.nextFloat() * 400;
				float z = random.nextFloat() * -400;
				y = terrain.getHeightOfTerrain(x, z);
				Random index = new Random();

				int randomIndex = index.nextInt(4);
				entities.add(new Entity(fern, randomIndex, new Vector3f(x, y, z), 0, random.nextFloat() * 360, 0, 0.9f));

			}
			if (i % 2 == 0) {
				float x = random.nextFloat() * 400;
				float z = random.nextFloat() * -400;
				y = terrain.getHeightOfTerrain(x, z);
				entities.add(new Entity(pine, 1, new Vector3f(x, y, z), 0, random.nextFloat() * 360, 0, random.nextFloat() * 0.6f + 0.8f));

			}
		}
	}

	/**
	 * Method that places entities on which we performed bump mapping in the world.
	 * @param terrain terrain on which the entities have to be put
	 * @param barrelModel used to create a barrel object
	 * @param crateModel used to create a crate object
	 * @param boulderModel used to create a boulder object
	 * @param normalMapEntities map that allows to "simulate" the normal vectors of complex entities on entities which have simple surfaces
	 */
	private static void placeNormalEntitiesOnTerrain(Terrain terrain, TexturedModel barrelModel, TexturedModel crateModel, TexturedModel boulderModel, List<Entity> normalMapEntities) {
		float y = terrain.getHeightOfTerrain(55,-55);
		Entity barrel = new Entity(barrelModel, new Vector3f(55, y + 5, -55), 0, 90, 90, 1f);
		y = terrain.getHeightOfTerrain(78,-78);
		Entity boulder = new Entity(boulderModel, new Vector3f(110, y + 5, -88), 0, 0, 0, 1f);
		y = terrain.getHeightOfTerrain(110,-110);
		Entity crate = new Entity(crateModel, new Vector3f(97, y + 5, -98), 0, 0, 0, 0.04f);
		normalMapEntities.add(barrel);
		normalMapEntities.add(boulder);
		normalMapEntities.add(crate);
	}

	/**
	 * Method that cleans up the scene, so that we don't have the same entities to render again and again billions of
	 * times
	 * @param loader object loader to which we have to clean the VAOs
	 * @param renderer stops rendering all objects
	 */
	private static void cleanup(Loader loader, MasterRenderer renderer) {
		ParticleMaster.cleanup();
		renderer.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();
	}

	/**
	 * Method that generates normal entities. First, it retrieves the model and the texture, then use a normal map to
	 * "simulate" the normal vectors of a complex object, then set the shine damper and reflectivity parameters for the
	 * specular light to hit the object "naturally".
	 * @param modelName name of the model to use to generate the entity
	 * @param loader used to load the texture to create the entity
	 * @param modelNormal model of the normal entity
	 * @return the model with a normal texture on it
	 */
	private static TexturedModel generateNormalModel(String modelName, Loader loader, String modelNormal) {
		TexturedModel model = new TexturedModel(NormalMappedObjLoader.loadOBJ(modelName, loader), new ModelTexture(loader.loadTexture(modelName)));
		model.getTexture().setNormalMap(loader.loadTexture(modelNormal));
		model.getTexture().setShineDamper(10);
		model.getTexture().setReflectivity(0.5f);
		return model;
	}

	/**
	 * Method that generates a terrain with different textures on it.
	 * @param loader used to load the multiple textures of the terrain
	 * @return the terrain with the multiple textures on it
	 */
	private static TerrainTexturePack generateTerrainTexture(Loader loader) {
		TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grassy3"));
		TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("mud"));
		TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("grassy2"));
		TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("path"));

		return new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);
	}

	/**
	 * Method that generates models requiring transparency. First it generates the model with the texture, then it sets
	 * its transparency to true.
	 * @param objFileName name of the 3D model
	 * @param modelTexture name of the texture to apply to the model
	 * @param loader used to load a texture
	 * @return the model with the texture on it
	 */
	private static TexturedModel generateTransparentModels(String objFileName, ModelTexture modelTexture, Loader loader){
		TexturedModel model = new TexturedModel(OBJFileLoader.loadOBJ(objFileName, loader), modelTexture);
		model.getTexture().setHasTransparency(true);
		return model;
	}

}
