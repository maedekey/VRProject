package terrains;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import models.RawModel;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import renderEngine.Loader;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import toolbox.Maths;

/**
 *  Class that represents terrains. They need different rendering requirements, such as multi texturing, tiling...
 *  Therefore, we need another shader program for rendering the terrains, and a new renderer class.
 */
public class Terrain {

	private static final float SIZE = 1600;
	private static final float MAX_HEIGHT = 40;
	private static final float MAX_PIXEL_COLOUR = 256 * 256 * 256; //there are 3 colour channels, each one has a value between 0 and 255 (256 possibilities).

	private final float x;
	private final float z;
	private final RawModel model;
	private final TerrainTexturePack texturePack;
	private final TerrainTexture blendMap;
	
	private float[][] heights; //we store the height of each vertex on the terrain.We have to detect the y coordinate of the terrain of each pixel.

	/**
	 * Constructor that generate the raw model ourselves.
	 * @param gridX
	 * @param gridZ
	 * @param loader required to load the model
	 * @param texturePack required for multi texturing the terrain
	 * @param blendMap required for multi texturing the terrain
	 * @param heightMap required to create a terrain with hills and variations
	 */
	public Terrain(int gridX, int gridZ, Loader loader, TerrainTexturePack texturePack, TerrainTexture blendMap, String heightMap) {
		this.texturePack = texturePack;
		this.blendMap = blendMap;
		this.x = gridX * SIZE;
		this.z = gridZ * SIZE;
		this.model = generateTerrain(loader, heightMap);
	}

	public float getX() {
		return x;
	}

	public float getZ() {
		return z;
	}

	public RawModel getModel() {
		return model;
	}

	public TerrainTexturePack getTexturePack() {
		return texturePack;
	}

	public TerrainTexture getBlendMap() {
		return blendMap;
	}

	/**
	 * Method that finds the height of the terrain for any x or z coordinates with the barrycentric equation used to
	 * interpolate Y coordinates for which we don't have a number for in the height map.
	 * @param worldX X coordinate
	 * @param worldZ Z coordinate
	 * @return Y coordinate
	 */
	
    public float getHeightOfTerrain(float worldX, float worldZ) {
        float terrainX = worldX - this.x; //we convert the world coord into a position relative to the terrain
        float terrainZ = worldZ - this.z;
        float gridSquareSize = SIZE / ((float) heights.length - 1); //Calculate the size of each gridsquare: (a terrain is made of multiple gridsquares) = size of terrain dividied by (amount of vertices on one côté -1). -1 because if the terrain has 4 vertices, the terrain had 3 gridsquares. |1|2|3| (there are 4 |)
        int gridX = (int) Math.floor(terrainX / gridSquareSize); //we find out which grid square the x coordinate is in
        int gridZ = (int) Math.floor(terrainZ / gridSquareSize);//we find out which grid squar	e the x coordinate is in
         
        if(gridX >= heights.length - 1 || gridZ >= heights.length - 1 || gridX < 0 || gridZ < 0) {
			//we test if the grid square is actually correct. if it's on the terrain, if it's not too big, if it's not on the edge of the terrain to the right or the bottom, to the left/above the terrain
            return 0; //if so, we exit
        }
         
        float xCoord = (terrainX % gridSquareSize)/gridSquareSize; //now that we found in which grid square the player is, we have to find out where in the grid square the player is. For this, we retrieve the distance of the player from the top left corner
        float zCoord = (terrainZ % gridSquareSize)/gridSquareSize; //divide by gridsquaresize to obtain a coordinate between 0 and 1.
        float answer; //where the player is in the square
         
        if (xCoord <= (1-zCoord)) {
            answer = Maths.barryCentric(new Vector3f(0, heights[gridX][gridZ], 0), new Vector3f(1,
                            heights[gridX + 1][gridZ], 0), new Vector3f(0,
                            heights[gridX][gridZ + 1], 1), new Vector2f(xCoord, zCoord));
        } else {
            answer = Maths.barryCentric(new Vector3f(1, heights[gridX + 1][gridZ], 0), new Vector3f(1,
                            heights[gridX + 1][gridZ + 1], 1), new Vector3f(0,
                            heights[gridX][gridZ + 1], 1), new Vector2f(xCoord, zCoord));
        }
         
        return answer;
    }

	/**
	 * Method that allows reconstructing normal, texture coodinates and vertices lists of the terrain and pass it to the
	 * VAO.
	 * @param loader required to pass objects to the VAO
	 * @param file path of the heightmap
	 * @return raw model of the terrain after it got loaded to the VAO.
	 */
	private RawModel generateTerrain(Loader loader, String file) {
		
		BufferedImage heightmap = null;
		try {
			heightmap = ImageIO.read(new File("res/" + file + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		assert heightmap != null;
		int VERTEX_COUNT = heightmap.getHeight();

		int count = VERTEX_COUNT * VERTEX_COUNT;
		heights = new float[VERTEX_COUNT][VERTEX_COUNT];
		float[] vertices = new float[count * 3];
		float[] normals = new float[count * 3];
		float[] textureCoords = new float[count * 2];
		int[] indices = new int[6 * (VERTEX_COUNT - 1) * (VERTEX_COUNT)];
		int vertexPointer = 0;
		for (int i = 0; i < VERTEX_COUNT; i++) {
			for (int j = 0; j < VERTEX_COUNT; j++) {
				vertexPointer = updateNormalsAndTextCoords(heightmap, (float) VERTEX_COUNT, vertices, normals, textureCoords, vertexPointer, i, j);
			}
		}
		int pointer = 0;
		for (int gz = 0; gz < VERTEX_COUNT - 1; gz++) {
			for (int gx = 0; gx < VERTEX_COUNT - 1; gx++) {
				pointer = updateIndices(VERTEX_COUNT, indices, pointer, gz, gx);
			}
		}
		return loader.loadToVAO(vertices, textureCoords, normals, indices);
	}

	/**
	 * Method that allows to update indices for the currently processed terrain.
	 * @param VERTEX_COUNT amount of vertexes in the object
	 * @param indices list of indices
	 * @param pointer where the appending should continue in the list
	 * @param gz z coordinate
	 * @param gx x coordinate
	 * @return where the appending should continue in the list
	 */
	private static int updateIndices(int VERTEX_COUNT, int[] indices, int pointer, int gz, int gx) {
		int topLeft = (gz * VERTEX_COUNT) + gx;
		int topRight = topLeft + 1;
		int bottomLeft = ((gz + 1) * VERTEX_COUNT) + gx;
		int bottomRight = bottomLeft + 1;
		indices[pointer++] = topLeft;
		indices[pointer++] = bottomLeft;
		indices[pointer++] = topRight;

		indices[pointer++] = topRight;
		indices[pointer++] = bottomLeft;
		indices[pointer++] = bottomRight;
		return pointer;
	}

	/**
	 * Method that allows to reconstruct vertices, normals and texture coordinates lists in the right order, upon
	 * loading a terrain.
	 * @param heightmap taken into consideration to know the y position of vertices and calculate the normals
	 * @param VERTEX_COUNT amount of vertices in the object
	 * @param vertices list of all the vertices in the object
	 * @param normals list of normals affecting the object
	 * @param textureCoords of the object
	 * @param vertexPointer pointer used to know where to add vertexes in the lists
	 * @param i number of the vertex we are currently processing
	 * @param j number of the vertex we are currently processing
	 * @return the position where the appending in the lists should continue
	 */
	private int updateNormalsAndTextCoords(BufferedImage heightmap, float VERTEX_COUNT, float[] vertices, float[] normals, float[] textureCoords, int vertexPointer, int i, int j) {
		vertices[vertexPointer * 3] = (float) j / (VERTEX_COUNT - 1) * SIZE;
		float height = getHeight(j, i, heightmap);
		vertices[vertexPointer * 3 + 1] = height;
		heights[j][i] = height;
		vertices[vertexPointer * 3 + 2] = (float) i / (VERTEX_COUNT - 1) * SIZE;
		Vector3f normal = calculateNormal(j, i, heightmap);
		normals[vertexPointer * 3] = normal.x;
		normals[vertexPointer * 3 + 1] = normal.y;
		normals[vertexPointer * 3 + 2] = normal.z;
		textureCoords[vertexPointer * 2] = (float) j / (VERTEX_COUNT - 1);
		textureCoords[vertexPointer * 2 + 1] = (float) i / (VERTEX_COUNT - 1);
		vertexPointer++;
		return vertexPointer;
	}

	/**
	 * Method that computes the normal vector for a specified vertex.
	 * @param x coordinate of the vertex we want to calculate the normal for
	 * @param z coordinate of the vertex we want to calculate the normal for
	 * @param heightmap map containing all the heights across the terrain
	 * @return normal vector for the specified vertex
	 */
	private Vector3f calculateNormal(int x, int z, BufferedImage heightmap){
		float heightL = getHeight(x-1, z, heightmap); //to calculate the normal, we have to calculate the height of all the neighbour vertices.
		float heightR = getHeight(x+1, z, heightmap);
		float heightD = getHeight(x, z-1, heightmap);
		float heightU = getHeight(x, z+1, heightmap);
		Vector3f normal = new Vector3f(heightL-heightR, 2f, heightD - heightU);
		normal.normalise();
		return normal;
	}

	/**
	 *
	 * @param x coordinate of the pixel on the terrain
	 * @param z coordinate of the pixel on the terrain
	 * @param heightmap containing all the heights across the terrain
	 * @return height of a certain pixel on the mam
	 */
	private float getHeight(int x, int z, BufferedImage heightmap){
		if(x<0 || x>=heightmap.getHeight() || z<0 || z>=heightmap.getHeight()){
			//check if the coord is in range and lies on the height map. If so, we're out of bounds
			return 0;
		}
		float height = heightmap.getRGB(x, z); //if we're in range, we get the pixel colour of the height map, and we retrieve a value representing the colour of the pixel, which we will have to convert into a height value
		height += MAX_PIXEL_COLOUR/2f; //we divide by 2 to make sure the height is reasonable
		height /= MAX_PIXEL_COLOUR/2f;  //We do this to obtain a value between 1 and -1
		height *= MAX_HEIGHT;  //we convert the height to a value between maxheight and -maxheight
		return height;
	}

	
}
