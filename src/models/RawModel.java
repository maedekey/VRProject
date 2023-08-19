package models;

/**
 * class representing an untextured 3D model stored in memory. VAOs are numbered lists containing VBOs, which are
 * buffers containing data relative to the 3D models. These data are accessed when showed on the screen.
 * This class contains a VAOID, which is the list where the model is stored. It also has a vertex count, which is the
 * amount of vertexes that have to show in the screen to render the model.
 */
public class RawModel {

	private final int vaoID;
	private final int vertexCount;

	public RawModel(int vaoID, int vertexCount){
		this.vaoID = vaoID;
		this.vertexCount = vertexCount;
	}

	public int getVaoID() {
		return vaoID;
	}

	public int getVertexCount() {
		return vertexCount;
	}



}
