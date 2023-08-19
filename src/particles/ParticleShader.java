package particles;

import org.lwjgl.util.vector.Matrix4f;

import org.lwjgl.util.vector.Vector2f;
import shaders.ShaderProgram;

/**
 * Class that allows to send information to the shaders, that they will process.
 */
public class ParticleShader extends ShaderProgram {

	private static final String VERTEX_FILE = "src/particles/particleVShader.glsl";
	private static final String FRAGMENT_FILE = "src/particles/particleFShader.glsl";

	private int location_modelViewMatrix;
	private int location_projectionMatrix;
	private int location_texOffset1;
	private int location_texOffset2;
	private int location_texCoordInfo;

	public ParticleShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}

	/**
	 * Method that allows to retrieve the physical location of uniforms set in the shader. This allows to put data in
	 * them later.
	 */
	@Override
	protected void getAllUniformLocations() {
		location_modelViewMatrix = super.getUniformLocation("modelViewMatrix");
		location_projectionMatrix = super.getUniformLocation("projectionMatrix");
		location_texOffset1 = super.getUniformLocation("texOffset1");
		location_texOffset2 = super.getUniformLocation("texOffset2");
		location_texCoordInfo = super.getUniformLocation("texCoordInfo");
	}

	/**
	 * Method that allows linking up the inputs to the shader programs to one of the attributes of the vao.
	 */
	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
	}

	/**
	 * Method that loads information to the shaders through uniforms.
	 * @param offset1 current texture in the texture atlas for the animation
	 * @param offset2 next texture in the texture atlas for the animation
	 * @param numRows amount of rows in the texture atlas
	 * @param blend: blend factor allowing to execute the animation smoothly
	 */
	protected void loadTextureCoordInfo(Vector2f offset1, Vector2f offset2, float numRows, float blend){
		super.load2DVector(location_texOffset1, offset1);
		super.load2DVector(location_texOffset2, offset2);
		super.load2DVector(location_texCoordInfo, new Vector2f(numRows, blend));
	}

	/**
	 * Method that sends the model view matrix to the shader
	 * @param modelViewMatrix matrix to send to the shader
	 */
	protected void loadModelViewMatrix(Matrix4f modelViewMatrix) {
		super.loadMatrix(location_modelViewMatrix, modelViewMatrix);
	}

	/**
	 * Method that sends the projection matrix to the shader
	 * @param projectionMatrix matrix to send to the shader
	 */
	protected void loadProjectionMatrix(Matrix4f projectionMatrix) {
		super.loadMatrix(location_projectionMatrix, projectionMatrix);
	}

}
