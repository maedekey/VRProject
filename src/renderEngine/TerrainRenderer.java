package renderEngine;

import java.util.List;

import models.RawModel;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import shaders.TerrainShader;
import terrains.Terrain;
import textures.TerrainTexturePack;
import toolbox.Maths;

/**
 * Class responsible for rendering the terrain.
 */
public class TerrainRenderer {

	private final TerrainShader shader;

	/**
	 * Constructor that passes the projection matrix to the shaders, and connects the texture units. The texture units
	 * are units where the textures are stored.
	 * @param shader in question
	 * @param projectionMatrix in question
	 */
	public TerrainRenderer(TerrainShader shader, Matrix4f projectionMatrix) {
		this.shader = shader;
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.connectTextureUnits(); //only need to connect the textures once, when we load the game, and the samplers will stay connected to these texture units.
		shader.stop();
	}

	/**
	 * Method that prepares each terrain and renders it. Then, we load the transformation matrix for that terrain and
	 * draw it on the screen.
	 * @param terrains list of all the terrains we have to render
	 */
	public void render(List<Terrain> terrains) {
		for (Terrain terrain : terrains) {
			prepareTerrain(terrain);
			loadTransformationMatrix(terrain);
			GL11.glDrawElements(GL11.GL_TRIANGLES, terrain.getModel().getVertexCount(),
					GL11.GL_UNSIGNED_INT, 0);
			unbindTexturedModel();
		}
	}

	/**
	 * Method that binds the model and bind the texture of the model to a VAO, and sets the specular light parameters.
	 * @param terrain to render
	 */
	private void prepareTerrain(Terrain terrain) {
		RawModel rawModel = terrain.getModel();
		GL30.glBindVertexArray(rawModel.getVaoID()); //to render or process any vao, we have to bind it first
		GL20.glEnableVertexAttribArray(0);//we also need to enable the attribute array our data is in
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		bindTextures(terrain);
		shader.loadShineVariables(1, 0); //everytime we load up an entity, we have to get those 2 shine variables. I think it determines whether the entity will shine
	}

	/**
	 * Method that binds all the textures we want to use as well as the blend map to texture units. We first retrieve
	 * the different textures for the terrain, then we bind them. Here, we have 4 different textures.
	 * @param terrain to render
	 */
	private void bindTextures(Terrain terrain){
		TerrainTexturePack texturePack = terrain.getTexturePack();
		GL13.glActiveTexture(GL13.GL_TEXTURE0); //we tell openGL which texture we want to render //sampler2D uses by default texture0
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texturePack.getBackgroundTexture().getTextureID()); //binds a 2D texture on an object
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texturePack.getrTexture().getTextureID());
		GL13.glActiveTexture(GL13.GL_TEXTURE2);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texturePack.getgTexture().getTextureID());
		GL13.glActiveTexture(GL13.GL_TEXTURE3);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texturePack.getbTexture().getTextureID());
		GL13.glActiveTexture(GL13.GL_TEXTURE4);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, terrain.getBlendMap().getTextureID());
	}

	private void unbindTexturedModel() {
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
	}

	/**
	 * Method that loads the transformation matrix to the shader.
	 * @param terrain to render
	 */
	private void loadTransformationMatrix(Terrain terrain) {
		Matrix4f transformationMatrix = Maths.createTransformationMatrix(
				new Vector3f(terrain.getX(), 0, terrain.getZ()), 0, 0, 0, 1);
		shader.loadTransformationMatrix(transformationMatrix);
	}

}
