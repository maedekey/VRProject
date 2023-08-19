package skybox;

import models.RawModel;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;

import renderEngine.DisplayManager;
import renderEngine.Loader;
import entities.Camera;

/**
 * Class that renders a skybox. It has its vertices, the size of the skybox,and the texture files it should use for its
 * model.
 */
public class SkyboxRenderer {
	
	private static final float SIZE = 500f;
	
	private static final float[] VERTICES = {        
	    -SIZE,  SIZE, -SIZE,
	    -SIZE, -SIZE, -SIZE,
	    SIZE, -SIZE, -SIZE,
	     SIZE, -SIZE, -SIZE,
	     SIZE,  SIZE, -SIZE,
	    -SIZE,  SIZE, -SIZE,

	    -SIZE, -SIZE,  SIZE,
	    -SIZE, -SIZE, -SIZE,
	    -SIZE,  SIZE, -SIZE,
	    -SIZE,  SIZE, -SIZE,
	    -SIZE,  SIZE,  SIZE,
	    -SIZE, -SIZE,  SIZE,

	     SIZE, -SIZE, -SIZE,
	     SIZE, -SIZE,  SIZE,
	     SIZE,  SIZE,  SIZE,
	     SIZE,  SIZE,  SIZE,
	     SIZE,  SIZE, -SIZE,
	     SIZE, -SIZE, -SIZE,

	    -SIZE, -SIZE,  SIZE,
	    -SIZE,  SIZE,  SIZE,
	     SIZE,  SIZE,  SIZE,
	     SIZE,  SIZE,  SIZE,
	     SIZE, -SIZE,  SIZE,
	    -SIZE, -SIZE,  SIZE,

	    -SIZE,  SIZE, -SIZE,
	     SIZE,  SIZE, -SIZE,
	     SIZE,  SIZE,  SIZE,
	     SIZE,  SIZE,  SIZE,
	    -SIZE,  SIZE,  SIZE,
	    -SIZE,  SIZE, -SIZE,

	    -SIZE, -SIZE, -SIZE,
	    -SIZE, -SIZE,  SIZE,
	     SIZE, -SIZE, -SIZE,
	     SIZE, -SIZE, -SIZE,
	    -SIZE, -SIZE,  SIZE,
	     SIZE, -SIZE,  SIZE
	};
	
	private static final String[] TEXTURE_FILES = {"right", "left", "top", "bottom", "back", "front"};
	
	private final RawModel cube;
	private final int texture;
	private final SkyboxShader shader;
	private float time =0;

	/**
	 * Constructor that loads to the VAO the specified vertices, loads the texture files for the skybox, connects the
	 * textures to the texture unit and loads the projection matrix to the shader.
	 * @param loader required to load objects to VAOs
	 * @param projectionMatrix loaded in the shader
	 */
	public SkyboxRenderer(Loader loader, Matrix4f projectionMatrix){
		cube = loader.loadToVAO(VERTICES, 3);
		texture = loader.loadCubeMap(TEXTURE_FILES);
		shader = new SkyboxShader();
		shader.start();
		shader.connectTextureUnits(); //we call th connectTextureUnits method to tell the shader which textures it has to be sampling
		shader.loadProjectionMatrix(projectionMatrix);
		shader.stop();
	}

	/**
	 * Method that shows the skybox on the screen.
	 * @param camera looking at the scene
	 * @param r value for the fog
	 * @param g value for the fog
	 * @param b value for the fog
	 */
	public void render(Camera camera, float r, float g, float b){
		shader.start();
		shader.loadViewMatrix(camera);
		shader.loadFogColour(r, g, b);
		GL30.glBindVertexArray(cube.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		bindTextures();
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, cube.getVertexCount());
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
		shader.stop();
	}

	/**
	 * Method that was used to simulate a day-night cycle, blend between each texture and bind them.
	 */
	private void bindTextures(){
		time += DisplayManager.getFrameTimeSeconds() * 1000;
		time %= 24000;
		int texture1;
		int texture2;
		float blendFactor;
		if(time >= 0 && time < 5000){
			texture1 = texture;
			texture2 = texture;
			blendFactor = (time - 0)/(5000);
		}else if(time >= 5000 && time < 8000){
			texture1 = texture;
			texture2 = texture;
			blendFactor = (time - 5000)/(8000 - 5000);
		}else if(time >= 8000 && time < 21000){
			texture1 = texture;
			texture2 = texture;
			blendFactor = (time - 8000)/(21000 - 8000);
		}else{
			texture1 = texture;
			texture2 = texture;
			blendFactor = (time - 21000)/(24000 - 21000);
		}

		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texture1);
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texture2);
		shader.loadBlendFactor(blendFactor);
	}


	

}
