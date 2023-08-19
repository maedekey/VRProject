package particles;

import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import models.RawModel;
import renderEngine.Loader;
import toolbox.Maths;

/**
 * Class responsible for showing particles on the screen. Every particle have the same vertices. Moreover, we set a quad
 * because that's the kind of models our particles share.
 */

public class ParticleRenderer {
	
	private static final float[] VERTICES = {-0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, -0.5f};
	
	private final RawModel quad;
	private final ParticleShader shader;
	
	protected ParticleRenderer(Loader loader, Matrix4f projectionMatrix){
		quad = loader.loadToVAO(VERTICES, 2);
		shader = new ParticleShader();
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix); //the particles are projected on the screen
		shader.stop();

	}

	/**
	 * Method that renders individually the animated particles by looping through all the different textures they take
	 * along the animation. Then, updates the viewmatrix to update the positions of the particles. Finally, draws the
	 * particles on the screen.
	 * @param particles to render
	 * @param camera: the particles have to face the camera
	 */
	protected void render(Map<ParticleTexture, List<Particle>> particles, Camera camera){
		Matrix4f viewMatrix = Maths.createViewMatrix(camera);
		prepare();
		for(ParticleTexture texture : particles.keySet()){
			//loops through all the textures. for each texture, binds the texture and renders all the particles
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getTextureId());
			for(Particle particle : particles.get(texture)){
				//render the particles individually
				updateModelViewmatrix(particle.getPosition(), particle.getRotation(), particle.getScale(), viewMatrix);
				shader.loadTextureCoordInfo(particle.getTextOffset1(), particle.getTexOffset2(), texture.getNumberOfRows(), particle.getBlend());
				GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, quad.getVertexCount()); //triangle strip allows to render 2D objects
			}
			finishRendering();
		}

	}


	protected void cleanUp(){
		shader.cleanUp();
	}

	/**
	 * Method that is called every time a particle is created, as we create a model view matrix for each particle.
	 * As the particles have to face the screen, we "hardcode" certain positions in the matrix.
	 * @param position of the particle
	 * @param rotation of the particle
	 * @param scale of the particle
	 * @param viewMatrix used to view the particles
	 */
	private void updateModelViewmatrix(Vector3f position, float rotation, float scale, Matrix4f viewMatrix){
		Matrix4f modelMatrix = new Matrix4f();
		Matrix4f.translate(position, modelMatrix, modelMatrix);
		modelMatrix.m00 = viewMatrix.m00;
		modelMatrix.m01 = viewMatrix.m10;
		modelMatrix.m02 = viewMatrix.m20;
		modelMatrix.m10 = viewMatrix.m01;
		modelMatrix.m11 = viewMatrix.m11;
		modelMatrix.m12 = viewMatrix.m21;
		modelMatrix.m20 = viewMatrix.m02;
		modelMatrix.m21 = viewMatrix.m12;
		modelMatrix.m22 = viewMatrix.m22;
		Matrix4f.rotate((float) Math.toRadians(rotation), new Vector3f(0,0,1), modelMatrix, modelMatrix);//we rotate around the z axis so that they can be rotated but still face the camera
		Matrix4f.scale(new Vector3f(scale, scale,scale), modelMatrix, modelMatrix); //we want to scale the model matrix to scale the particles
		Matrix4f modelViewMatrix = Matrix4f.mul(viewMatrix, modelMatrix, null);
		shader.loadModelViewMatrix(modelViewMatrix);
	}

	/**
	 * Method that prepares the rendering by starting the shaders, loading models in VAOs and setting some graphical
	 * parameters.
	 */
	private void prepare(){
		shader.start();
		GL30.glBindVertexArray(quad.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDepthMask(false);

	}

	/**
	 * Method that is called when the rendering is finished. It unsets parameters and unbinds used VAOs, as well as
	 * stopping the shaders.
	 */
	private void finishRendering(){
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_BLEND);
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
		shader.stop();
	}

}
