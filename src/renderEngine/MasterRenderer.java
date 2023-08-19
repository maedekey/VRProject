package renderEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import models.TexturedModel;
import normalMappingRenderer.NormalMappingRenderer;
import shaders.StaticShader;
import shaders.TerrainShader;
import skybox.SkyboxRenderer;
import terrains.Terrain;

/**
 * Class that manages all the other renderers.
 */
public class MasterRenderer {

	private static final float FOV = 70;
	private static final float NEAR_PLANE = 0.1f;
	private static final float FAR_PLANE = 1000;

	public static final float RED = 0.5f;
	public static final float GREEN = 0.5f;
	public static final float BLUE = 0.5f;

	private Matrix4f projectionMatrix;

	private final StaticShader shader = new StaticShader();
	private final EntityRenderer renderer;

	private final TerrainRenderer terrainRenderer;
	private final TerrainShader terrainShader = new TerrainShader();
	
	private final NormalMappingRenderer normalMapRenderer;

	private final SkyboxRenderer skyboxRenderer;

	private final Map<TexturedModel, List<Entity>> entities = new HashMap<>();  //each texture will be mapped to the entities that use that specific texture model. So basically, we have a list of all the entities using that texture model
	private final Map<TexturedModel, List<Entity>> normalMapEntities = new HashMap<>();
	private final List<Terrain> terrains = new ArrayList<>();

	/**
	 * Constructor that passes the projection matrix to all the renderers.
	 * @param loader needed for the skybox to load to VAOs its vertices
	 */
	public MasterRenderer(Loader loader) {
		enableCulling();
		createProjectionMatrix();
		renderer = new EntityRenderer(shader, projectionMatrix);
		terrainRenderer = new TerrainRenderer(terrainShader, projectionMatrix);
		skyboxRenderer = new SkyboxRenderer(loader, projectionMatrix);
		normalMapRenderer = new NormalMappingRenderer(projectionMatrix);
	}

	public Matrix4f getProjectionMatrix() {
		return this.projectionMatrix;
	}

	/**
	 * Method that processes all things that need to be processed (added in their respective lists or hashmaps).
	 * @param entities list of entities that have to be processed
	 * @param normalEntities list of normal entities to be processed
	 * @param terrains list of terrains to be processed
	 * @param lights required to render
	 * @param camera required to render
	 * @param clipPlane required to render
	 */
	public void renderScene(List<Entity> entities, List<Entity> normalEntities, List<Terrain> terrains, List<Light> lights,
			Camera camera, Vector4f clipPlane) {
		for (Terrain terrain : terrains) {
			processTerrain(terrain);
		}
		for (Entity entity : entities) {
			processEntity(entity);
		}
		for(Entity entity : normalEntities){
			processNormalMapEntity(entity);
		}
		render(lights, camera, clipPlane);
	}

	/**
	 * Method that renders everything on the screen. First, it loads the plane, lights, sky colour and the view matrix.
	 * Then, it renders all the entities and normal entities. It does the same for the terrain, then stops the shader
	 * and clear everything.
	 * @param lights used to light the scene.
	 * @param camera used to watch the scene
	 * @param clipPlane used to not render the things that are outside of sight
	 */
	public void render(List<Light> lights, Camera camera, Vector4f clipPlane) {
		prepare();
		shader.start();
		shader.loadClipPlane(clipPlane);
		shader.loadSkyColour(RED, GREEN, BLUE);
		shader.loadLights(lights);
		shader.loadViewMatrix(camera);
		renderer.render(entities);
		shader.stop();
		normalMapRenderer.render(normalMapEntities, clipPlane, lights, camera);
		terrainShader.start();
		terrainShader.loadClipPlane(clipPlane);
		terrainShader.loadSkyColour(RED, GREEN, BLUE);
		terrainShader.loadLights(lights);
		terrainShader.loadViewMatrix(camera);
		terrainRenderer.render(terrains);
		terrainShader.stop();
		skyboxRenderer.render(camera, RED, GREEN, BLUE);
		terrains.clear();
		entities.clear();
		normalMapEntities.clear();
	}

	/**
	 * Method that enables culling, which avoids rendering the inside of our objects.
	 */
	public static void enableCulling() {
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
	}

	/**
	 * Method that disables culling, which makes so our transparent objects don't render well sometimes, so we have to
	 * disable culling for such models.
	 */
	public static void disableCulling() {
		GL11.glDisable(GL11.GL_CULL_FACE);
	}

	public void processTerrain(Terrain terrain) {
		terrains.add(terrain);
	}

	/**
	 * Method that allows to populate the hashmap of entities and their corresponding textures.
	 * @param entity to add in the hashmap
	 */
	public void processEntity(Entity entity) {
		TexturedModel entityModel = entity.getModel();
		List<Entity> batch = entities.get(entityModel); // we get the lists that correspond to that entity from the hashmap. The entities retrieved share the same texture.
		if (batch != null) {  //if a batch already exists for that entity, then we add it to the already existing list of entities
			batch.add(entity);
		} else {
			List<Entity> newBatch = new ArrayList<>();
			newBatch.add(entity);
			entities.put(entityModel, newBatch); //otherwise, we create a batch for this entity and add it to the hashmap
		}
	}
	/**
	 * Method that allows to populate the hashmap of normal entities and their corresponding textures.
	 * @param entity to add in the hashmap
	 */
	public void processNormalMapEntity(Entity entity) {
		TexturedModel entityModel = entity.getModel();
		List<Entity> batch = normalMapEntities.get(entityModel);
		if (batch != null) {
			batch.add(entity);
		} else {
			List<Entity> newBatch = new ArrayList<>();
			newBatch.add(entity);
			normalMapEntities.put(entityModel, newBatch);
		}
	}

	public void cleanUp() {
		shader.cleanUp();
		terrainShader.cleanUp();
		normalMapRenderer.cleanUp();
	}

	/**
	 * Method that prepares opengl to render the game. We have to tell how the triangles have to render: when a triangle
	 * is on top another, we have to specify which triangle shows upfront and which triangle is hidden.
	 */
	public void prepare() {
		GL11.glEnable(GL11.GL_DEPTH_TEST); //we do that through this method
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);//we have to clear the depth buffer every frame
		GL11.glClearColor(RED, GREEN, BLUE, 1); //will take the new color specified
	}

	/**
	 * Method that creates the projection matrix, which defines how objects are projected in the 3D world.
	 */
	private void createProjectionMatrix() {
		float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
		float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))) * aspectRatio);
		float x_scale = y_scale / aspectRatio;
		float frustum_length = FAR_PLANE - NEAR_PLANE;

		projectionMatrix = new Matrix4f();
		projectionMatrix.m00 = x_scale;
		projectionMatrix.m11 = y_scale;
		projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length);
		projectionMatrix.m33 = 0;
	}

}
