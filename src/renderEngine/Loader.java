package renderEngine;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import models.RawModel;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import textures.TextureData;
import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

/**
 * class that loads 3D models into memory by storing positional data about models in a VAO.
 */
public class Loader {
	//memory management: we want to delete all vaos and vbos we created in memory. in this purpose, these lists track all the vaos and vbos we create
	private final List<Integer> vaos = new ArrayList<>();
	private final List<Integer> vbos = new ArrayList<>();
	private final List<Integer> textures = new ArrayList<>(); //list of all the texture IDs. We keep track of them to delete them later once the program stopped

	/**
	 * Method that takes the positions of the model vertices, load the data into a VAO, and return information
	 * about the VAO as a raw model object.
	 * @param positions of the vertices to load in the VAO
	 * @param textureCoords of the model to load in the VAO
	 * @param normals of the model to load in the VAO
	 * @param indices which define how to draw each triangle between each vertex
	 * @return VAO info as raw model.
	 */
	public RawModel loadToVAO(float[] positions, float[] textureCoords, float[] normals,
			int[] indices) {
		int vaoID = createVAO();
		bindIndicesBuffer(indices);
		storeDataInAttributeList(0, 3, positions);
		storeDataInAttributeList(1, 2, textureCoords);
		storeDataInAttributeList(2, 3, normals);
		unbindVAO();
		return new RawModel(vaoID, indices.length);
	}
	
	public RawModel loadToVAO(float[] positions, float[] textureCoords, float[] normals, float[] tangents,
			int[] indices) {
		int vaoID = createVAO();
		bindIndicesBuffer(indices);
		storeDataInAttributeList(0, 3, positions);
		storeDataInAttributeList(1, 2, textureCoords);
		storeDataInAttributeList(2, 3, normals);
		storeDataInAttributeList(3, 3, tangents);
		unbindVAO();
		return new RawModel(vaoID, indices.length);
	}

	public RawModel loadToVAO(float[] positions, int dimensions) {
		//constructor without indices
		int vaoID = createVAO();
		this.storeDataInAttributeList(0, dimensions, positions);
		unbindVAO();
		return new RawModel(vaoID, positions.length / dimensions); //divided by 2 because positions contain 2D data
	}

	/**
	 * Method that loads texture from a png file. It sets some parameters to increase performance, gets the texture
	 * ID that identifies the texture and adds it in our list keeping track of all the textures.
	 * @param fileName containing the texture
	 * @return texture ID
	 */
	public int loadTexture(String fileName) {
		Texture texture = null;
		try {
			texture = TextureLoader.getTexture("PNG", new FileInputStream("res/" + fileName
					+ ".png"));
			GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D); //generate lower res versions of the textures, and put in the type of the texture
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);//Tell openGL to use those lower res images. We put the texture type and define its behavior when the surface is rendered onto a surface with smaller dimensions than the texture. The linear option tells openGL to transition smoothly between different texture res.
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, -0.4f); //Level Of Detail bias. If we put a negative number as the 3rd parameter, it gives us a slightly higher resolution all over. But if too negative: lose the increase in performances.
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Tried to load texture " + fileName + ".png , didn't work");
			System.exit(-1);
		}
		textures.add(texture.getTextureID());
		return texture.getTextureID();
	}

	/**
	 * method that deletes all vaos and vbos from memory when closing the game.
	 */
	public void cleanUp() {
		for (int vao : vaos) {
			GL30.glDeleteVertexArrays(vao);
		}
		for (int vbo : vbos) {
			GL15.glDeleteBuffers(vbo);
		}
		for (int texture : textures) {
			GL11.glDeleteTextures(texture);
		}
	}

	/**
	 * Method that loads up a cube map to opengl, returns the id of the cube map texture so that we can bind it when we
	 * render the skybox.
	 * @param textureFiles containing the files of the cube map
	 * @return id of the texture
	 */
	public int loadCubeMap(String[] textureFiles) {
		int textureId = GL11.glGenTextures();
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, textureId);

		for (int i = 0; i < textureFiles.length; i++) {
			TextureData data = decodeTextureFile("res/" + textureFiles[i] + ".png");
			GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL11.GL_RGBA, data.getWidth(), data.getHeight(), 0,
					GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data.getBuffer());
		}
		
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		textures.add(textureId);
		return textureId;
	}

	/**
	 * Method that decodes the texture file for the cube map. Gets its wigth, height, retrieves the colours of the
	 * texture, put them in a buffer, and creates a texturedata object with such data.
	 * @param fileName containing the texture
	 * @return texture data object
	 */
	private TextureData decodeTextureFile(String fileName) {
		int width = 0;
		int height = 0;
		ByteBuffer buffer = null;
		try {
			FileInputStream in = new FileInputStream(fileName);
			PNGDecoder decoder = new PNGDecoder(in);
			width = decoder.getWidth();
			height = decoder.getHeight();
			buffer = ByteBuffer.allocateDirect(4 * width * height);
			decoder.decode(buffer, width * 4, Format.RGBA);
			buffer.flip();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Tried to load texture " + fileName + ", didn't work");
			System.exit(-1);
		}
		return new TextureData(buffer, width, height);
	}

	/**
	 * method that creates an empty VAO, returns the id of the VAO we create.
	 * @return the id of the newly created VAO.
	 */
	private int createVAO() {
		int vaoID = GL30.glGenVertexArrays();
		vaos.add(vaoID); //memory management: track the vaos by adding them to a list to delete them later
		GL30.glBindVertexArray(vaoID); //activate the vao by binding it. Stays bound until unbound
		return vaoID;
	}

	/**
	 * Method that loads data in VBOs and loads VBOs into a VAO.
	 * @param attributeNumber attribute list number in which we want to store the data
	 * @param coordinateSize length of vertex. It can be 3 for 3D vector positions, or 2 for textures.
	 * @param data to store in the attribute list
	 */
	private void storeDataInAttributeList(int attributeNumber, int coordinateSize, float[] data) {
		int vboID = GL15.glGenBuffers(); //creates empty vbo
		vbos.add(vboID); //We keep track of vbos to delete them later
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID); //vbo needs to be bound to store data into it
		FloatBuffer buffer = storeDataInFloatBuffer(data);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW); //once we get the float buffer with the data in it, we can store it in the vbo. Needs to specify the type of data, the data and what the data will be used for: either static data or editable data
		GL20.glVertexAttribPointer(attributeNumber, coordinateSize, GL11.GL_FLOAT, false, 0, 0);  //stores vbo into vao. 3rd argument: type of data, 4rth: is the data normalized, 5th:distance between each vertex. is any data between them? 6th: offset. Should it start at the beginning of the data?
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}

	/**
	 * Method bindinding a VAO once we finish using it.
	 */
	private void unbindVAO() {
		GL30.glBindVertexArray(0);
	}

	/**
	 * Method that binds an indicesBuffer to a VAO we want to render, creates an array buffer of indices,
	 * puts the indices in an int buffer, puts the indices into a vbo
	 * @param indices indices buffer in question
	 */
	private void bindIndicesBuffer(int[] indices) {
		int vboID = GL15.glGenBuffers();
		vbos.add(vboID);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);
		IntBuffer buffer = storeDataInIntBuffer(indices);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW); //stores the indices buffer in the vbo
	}

	/**
	 * Method that stores indices of the model array into an int buffer (same concept as float buffer for vbos). We can
	 * only store int buffers into VBOs, so we have to convert them.
	 * @param data array containing the data to put in a VBO
	 * @return the buffer filled
	 */
	private IntBuffer storeDataInIntBuffer(int[] data) {
		IntBuffer buffer = BufferUtils.createIntBuffer(data.length);//empty int buffer, size of the data to put in it
		buffer.put(data);
		buffer.flip();//makes the buffer ready to get read from
		return buffer;
	}

	/**
	 * Method that stores data in a VBO as a float buffer : we need to convert the floatArray obtained by GL into a
	 * float buffer, as we can only store float buffers in VBOs.
	 * @param data to store in the VBO.
	 * @return the buffer filled
	 */
	private FloatBuffer storeDataInFloatBuffer(float[] data) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}

}
