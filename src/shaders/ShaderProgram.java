package shaders;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

/**
 * abstract class that represents a generic shader program containing all the attributes and methods every shader
 * program would have.
 */
public abstract class ShaderProgram {
	
	private final int programID;
	private final int vertexShaderID;
	private final int fragmentShaderID;
	
	private static final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16); //(4x4 matrix)
	
	public ShaderProgram(String vertexFile,String fragmentFile){
		vertexShaderID = loadShader(vertexFile,GL20.GL_VERTEX_SHADER);
		fragmentShaderID = loadShader(fragmentFile,GL20.GL_FRAGMENT_SHADER);
		programID = GL20.glCreateProgram();
		GL20.glAttachShader(programID, vertexShaderID); //we will use the programID whenever we need to use the shader
		GL20.glAttachShader(programID, fragmentShaderID);
		bindAttributes();
		GL20.glLinkProgram(programID);
		GL20.glValidateProgram(programID);
		getAllUniformLocations(); //we have to get all the locations before using them
	}
	
	protected abstract void getAllUniformLocations();

	/**
	 * Method that gets the location of the uniform values in the shader code, so we can access them and modify them.
	 * @param uniformName name of the uniform to access
	 * @return the location of the uniform to access
	 */
	protected int getUniformLocation(String uniformName){
		return GL20.glGetUniformLocation(programID,uniformName);
	}
	
	public void start(){
		GL20.glUseProgram(programID); //we have to start the program when we want to use it
	}
	
	public void stop(){
		GL20.glUseProgram(0);
	}

	/**
	 * Method responsible for memory  management: deletes shaders when not needed anymore.
	 */
	public void cleanUp(){
		stop();
		GL20.glDetachShader(programID, vertexShaderID);
		GL20.glDetachShader(programID, fragmentShaderID);
		GL20.glDeleteShader(vertexShaderID);
		GL20.glDeleteShader(fragmentShaderID);
		GL20.glDeleteProgram(programID);
	}
	
	protected abstract void bindAttributes(); //will link up the inputs to the shader programs to one of the attributes of the vao we're going to pass in
	
	protected void bindAttribute(int attribute, String variableName){
		GL20.glBindAttribLocation(programID, attribute, variableName);
	}

	/**
	 * Method that loads a float into a uniform through the location of this uniform
	 * @param location of the uniform
	 * @param value to put in the uniform
	 */
	protected void loadFloat(int location, float value){
		GL20.glUniform1f(location, value);
	}
	
	protected void loadInt(int location, int value){
		GL20.glUniform1i(location, value);
	}
	
	protected void loadVector(int location, Vector3f vector){
		GL20.glUniform3f(location,vector.x,vector.y,vector.z);
	}
	
	protected void loadVector(int location, Vector4f vector){
		GL20.glUniform4f(location,vector.x,vector.y,vector.z, vector.w);
	}
	
	protected void load2DVector(int location, Vector2f vector){
		GL20.glUniform2f(location,vector.x,vector.y);
	}
	
	protected void loadBoolean(int location, boolean value){
		float toLoad = 0;
		if(value){
			toLoad = 1;
		}
		GL20.glUniform1f(location, toLoad);
	}
	
	protected void loadMatrix(int location, Matrix4f matrix){
		matrix.store(matrixBuffer); //to pass a matrix to the uniforms of the shader, we first have to put it in a float buffer
		matrixBuffer.flip();
		GL20.glUniformMatrix4(location, false, matrixBuffer);  //the second argument says whether we transpose the matrix or not
	}

	/**
	 * Method that loads up shader source code files. Type indicates if vertex or fragment shader. Opens the source
	 * files, reads lines in it, collects them into one long string, before creating a new vertex or fragment shader
	 * depending on the type we gave it, attaching the string of source code to it, compiling it, and printing errors
	 * found in the code. Returns the ID of the newly created shader.
	 * @param file to load as a shader
	 * @param type of shader (vertex or fragment)
	 * @return ID of the newly created shader
	 */
	private static int loadShader(String file, int type){
		StringBuilder shaderSource = new StringBuilder();
		try{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while((line = reader.readLine())!=null){
				shaderSource.append(line).append("//\n");
			}
			reader.close();
		}catch(IOException e){
			e.printStackTrace();
			System.exit(-1);
		}
		int shaderID = GL20.glCreateShader(type);
		GL20.glShaderSource(shaderID, shaderSource);
		GL20.glCompileShader(shaderID);
		if(GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS )== GL11.GL_FALSE){
			System.out.println(GL20.glGetShaderInfoLog(shaderID, 500));
			System.err.println("Could not compile shader!");
			System.exit(-1);
		}
		return shaderID;
	}

}
