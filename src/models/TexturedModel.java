package models;
import textures.ModelTexture;

/**
 * Cass that represents a model that is textured.
 */
public class TexturedModel {
	
	private final RawModel rawModel;
	private final ModelTexture texture;

	
	public TexturedModel(RawModel model, ModelTexture texture){
		this.rawModel = model;
		this.texture = texture;
	}

	public RawModel getRawModel() {
		return rawModel;
	}

	public ModelTexture getTexture() {
		return texture;
	}

}
