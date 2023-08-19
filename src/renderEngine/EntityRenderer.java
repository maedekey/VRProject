package renderEngine;

import java.util.List;
import java.util.Map;

import models.RawModel;
import models.TexturedModel;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;

import shaders.StaticShader;
import textures.ModelTexture;
import toolbox.Maths;
import entities.Entity;

/**
 * Class that renders a models from vaos.
 */
public class EntityRenderer {

	private final StaticShader shader;

	/**
	 * Constructor that allows loading the projection matrix straight up to the shader
	 * @param shader in question
	 * @param projectionMatrix in question
	 */
	public EntityRenderer(StaticShader shader,Matrix4f projectionMatrix) {
		this.shader = shader;
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.stop();
	}

	/**
	 * Method that renders models and takes in parameter shader so that it can apply the transformation to the entity to
	 * render. First, we prepare texture models (that can be shared between entities), then we prepare such entities,
	 * and finally, we draw them on the screen.
	 * @param entities to be shown on the screen
	 */
	public void render(Map<TexturedModel, List<Entity>> entities) {
		for (TexturedModel model : entities.keySet()) {
			prepareTexturedModel(model);
			List<Entity> batch = entities.get(model);
			for (Entity entity : batch) {
				prepareInstance(entity);
				GL11.glDrawElements(GL11.GL_TRIANGLES, model.getRawModel().getVertexCount(),
						GL11.GL_UNSIGNED_INT, 0);
			}
			unbindTexturedModel();
		}
	}

	/**
	 * Method that retrieves the raw model out of a textured model, to bind its vertexes to a VAO. Then, we load
	 * textures (they can be texture atlases). If the texture needs transparency, we disable culling. If fake lighting
	 * is required, we enable it. Finally, we tell openGL which textures we want to render.
	 * @param model to render
	 */
	private void prepareTexturedModel(TexturedModel model) {
		RawModel rawModel = model.getRawModel();
		GL30.glBindVertexArray(rawModel.getVaoID()); //to render or process any vao, we have to bind it first
		GL20.glEnableVertexAttribArray(0); //we also need to enable the attribute array our data is in
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		ModelTexture texture = model.getTexture();
		shader.loadNumberOfRows(texture.getNumberOfRows());
		if(texture.isHasTransparency()){
			//we disable culling whenever dealing with textures with transparency
			MasterRenderer.disableCulling();
		}
		shader.loadFakeLightingVariable(texture.isUseFakeLighting());
		shader.loadShineVariables(texture.getShineDamper(), texture.getReflectivity());
		GL13.glActiveTexture(GL13.GL_TEXTURE0); //we tell openGL which texture we want to render //sampler2D uses by default texture0
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTexture().getID()); //binds a 2D texture on an object
	}

	/**
	 * Method that unbinds texture model, so that it doesn't duplicate to infinite and saves some processing.
	 */
	private void unbindTexturedModel() {
		MasterRenderer.enableCulling();//enable culling again so that it's available for the next model
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
	}

	/**
	 * Method that is called after preparing the texture models, to prepare all the entities. For each entity, we
	 * load theur transformation matrix to the shader so that it shows them correctly.
	 * @param entity to be rendered
	 */
	private void prepareInstance(Entity entity) {

		Matrix4f transformationMatrix = Maths.createTransformationMatrix(entity.getPosition(), entity.getRotationX(), entity.getRotationY(), entity.getRotationZ(), entity.getScale());//send the transformation matrix to the shader so that it transforms the entity in the view and moves where the model is rendered on the screen
		shader.loadTransformationMatrix(transformationMatrix);
		shader.loadOffset(entity.getTextureXOffset(), entity.getTextureYOffset());//we have to do this per entity because it could be different for each entity (not every entity uses the same texture on the texture atlas)
	}

}
