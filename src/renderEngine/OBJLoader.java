package renderEngine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import models.RawModel;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

/**
 * Class used to load .obj data into our program
 */
public class OBJLoader {

	/**
	 * Method that loads an obj file data and return it as a raw model
	 * @param fileName containing the .obj object
	 * @param loader used to load to the VAO the objects in the file
	 * @return a raw model containing the obj file data
	 */
	public static RawModel loadObjModel(String fileName, Loader loader) {
		FileReader fr = null;
		try {
			fr = new FileReader(new File("res/" + fileName + ".obj"));
		} catch (FileNotFoundException e) {
			System.err.println("Couldn't load file!");
			e.printStackTrace();
		}
		BufferedReader reader = new BufferedReader(fr);
		String line;
		List<Vector3f> vertices = new ArrayList<Vector3f>();
		List<Vector2f> textures = new ArrayList<Vector2f>();
		List<Vector3f> normals = new ArrayList<Vector3f>();
		List<Integer> indices = new ArrayList<Integer>();
		float[] verticesArray; //we will need our data to be in arrays because our loader only takes data under this form
		float[] normalsArray = null;
		float[] textureArray = null;
		int[] indicesArray;
		try {

			while (true) {
				line = reader.readLine();
				String[] currentLine = line.split(" ");
				if (line.startsWith("v ")) {
					//then it's a vertex position
					Vector3f vertex = new Vector3f(Float.parseFloat(currentLine[1]),
							Float.parseFloat(currentLine[2]), Float.parseFloat(currentLine[3]));
					vertices.add(vertex);
				} else if (line.startsWith("vt ")) {
					//then it's a texture coordinate
					Vector2f texture = new Vector2f(Float.parseFloat(currentLine[1]),
							Float.parseFloat(currentLine[2]));
					textures.add(texture);
				} else if (line.startsWith("vn ")) {
					//then it's a normal vector
					Vector3f normal = new Vector3f(Float.parseFloat(currentLine[1]),
							Float.parseFloat(currentLine[2]), Float.parseFloat(currentLine[3]));
					normals.add(normal);
				} else if (line.startsWith("f ")) {
					//then it's a face: we've read in the data from the vertexes, textcoord and normals, we have all the data we need and can break out of this loop,but we just have to set up the arrays now that we know the sizes of them
					textureArray = new float[vertices.size() * 2];
					normalsArray = new float[vertices.size() * 3];
					break;
				}
			}

			while (line != null) {
				//while we haven't reached the end of the file,
				if (!line.startsWith("f ")) {
					//we first check that the line does start with an f, if it doesn't we go back to the while loop
					line = reader.readLine();
					continue;
				}
				//if we found one, we divide each line in 3, one part for each vertex
				String[] currentLine = line.split(" ");
				String[] vertex1 = currentLine[1].split("/"); //the 1st part will be an f and the next part will be the vertices of the triangle
				String[] vertex2 = currentLine[2].split("/");
				String[] vertex3 = currentLine[3].split("/");
				
				processVertex(vertex1,indices,textures,normals,textureArray,normalsArray);
				processVertex(vertex2,indices,textures,normals,textureArray,normalsArray);
				processVertex(vertex3,indices,textures,normals,textureArray,normalsArray);
				line = reader.readLine();
			}
			reader.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		verticesArray = new float[vertices.size()*3]; //we have to convert the vertex list into a vertex array into a new float array. *3 because we put them as floats not vectors, and there's 3 elements in the vector
		indicesArray = new int[indices.size()]; //same with indices

		//we copy across all the data, loop through list of vertices
		int vertexPointer = 0;
		for(Vector3f vertex:vertices){
			//for each vector in that list of vertices, we need to put each component of each vector into the vertices array
			verticesArray[vertexPointer++] = vertex.x;
			verticesArray[vertexPointer++] = vertex.y;
			verticesArray[vertexPointer++] = vertex.z;
		}
		//and then, we just have to copy accross the indices data from the indices list into the indices array
		for(int i=0;i<indices.size();i++){
			indicesArray[i] = indices.get(i);
		}
		return loader.loadToVAO(verticesArray, textureArray, normalsArray, indicesArray);

	}

	/**
	 * Method that sorts out the texture coordinates and normal vector for the current vertex and put these values into
	 * the correct position. We call this for every vertex we process of the triangle
	 * @param vertexData list of the vertexes of the object
	 * @param indices of the object describing how the vertexes should be linked
	 * @param textures coordinates of the texture of the model
	 * @param normals of the model
	 * @param textureArray array of the texture (rearranged)
	 * @param normalsArray array of the normals (rearranged)
	 */
	private static void processVertex(String[] vertexData, List<Integer> indices,
			List<Vector2f> textures, List<Vector3f> normals, float[] textureArray,
			float[] normalsArray) {
		int currentVertexPointer = Integer.parseInt(vertexData[0]) - 1; //-1 because the obj indices start at 1 and our array start at 0
		indices.add(currentVertexPointer);
		Vector2f currentTex = textures.get(Integer.parseInt(vertexData[1])-1);
		textureArray[currentVertexPointer*2] = currentTex.x;
		textureArray[currentVertexPointer*2+1] = 1 - currentTex.y; //-1 because openGL starts at the top left of a texture when blender starts from the bottom left
		Vector3f currentNorm = normals.get(Integer.parseInt(vertexData[2])-1); //we get the normal vector associated with the vertex data
		normalsArray[currentVertexPointer*3] = currentNorm.x; //and we put it to the current position
		normalsArray[currentVertexPointer*3+1] = currentNorm.y;
		normalsArray[currentVertexPointer*3+2] = currentNorm.z;	
	}

}
