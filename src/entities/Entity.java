package entities;
import models.TexturedModel;
import org.lwjgl.util.vector.Vector3f;

/**
 * Class that represents a textured model and its transformation (position in the world + rotation).
 */
public class Entity {

	private TexturedModel model;
	private Vector3f position;
	private float rotationX;
	private float rotationY;
	private float rotationZ;
	private float scale;
	private int textureIndex = 0; //attribute indicating which texture in the texture atlas the entity uses. The textures are numbered from red to right,123\n456\n789 etc

	/**
	 * Constructor used on models not requiring a texture atlas, i.e. models that have only one texture available for them.
	 * @param model of the entity
	 * @param position of the entity in the world
	 * @param rotationX of the entity in the world
	 * @param rotationY of the entity in the world
	 * @param rotationZ of the entity in the world
	 * @param scale of the entity in the world
	 */
	public Entity(TexturedModel model, Vector3f position, float rotationX, float rotationY, float rotationZ, float scale) {
		this.model = model;
		this.position = position;
		this.rotationX = rotationX;
		this.rotationY = rotationY;
		this.rotationZ = rotationZ;
		this.scale = scale;
	}

	/**
	 * Constructor used on models not requiring a texture atlas, i.e. models that have only one texture available for them.
	 * @param model of the entity
	 * @param index of the texture atlas that the entity will use
	 * @param position of the entity in the world
	 * @param rotationX of the entity in the world
	 * @param rotationY of the entity in the world
	 * @param rotationZ of the entity in the world
	 * @param scale of the entity in the world
	 */
	public Entity(TexturedModel model, int index, Vector3f position, float rotationX, float rotationY, float rotationZ, float scale) {
		this.textureIndex = index;
		this.model = model;
		this.position = position;
		this.rotationX = rotationX;
		this.rotationY = rotationY;
		this.rotationZ = rotationZ;
		this.scale = scale;
	}

	public TexturedModel getModel() {
		return model;
	}

	public void setModel(TexturedModel model) {
		this.model = model;
	}

	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}

	public float getRotationX() {
		return rotationX;
	}

	public void setRotationX(float rotationX) {
		this.rotationX = rotationX;
	}

	public float getRotationY() {
		return rotationY;
	}

	public void setRotationY(float rotationY) {
		this.rotationY = rotationY;
	}

	public float getRotationZ() {
		return rotationZ;
	}

	public void setRotationZ(float rotationZ) {
		this.rotationZ = rotationZ;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	/**
	 * Method that finds the X position of the texture in the texture atlas
	 * @return the position X of the texture in the texture atlas
	 */
	public float getTextureXOffset(){
		int column = textureIndex % model.getTexture().getNumberOfRows();
		return (float)column/(float)model.getTexture().getNumberOfRows();
	}

	/**
	 * Method that finds the Y position of the texture in the texture atlas
	 * @return the position Y of the texture in the texture atlas
	 */
	public float getTextureYOffset(){
		int row = textureIndex/model.getTexture().getNumberOfRows();
		return (float)row/(float)model.getTexture().getNumberOfRows();
	}

	/**
	 * Method that allows to move the entity in the world by increasing its position.
	 * @param dx distance to add to the x-axis of the entity
	 * @param dy distance to add to the y-axis of the entity
	 * @param dz distance to add to the z-axis of the entity
	 */
	public void increasePosition(float dx, float dy, float dz) {
		this.position.x += dx;
		this.position.y += dy;
		this.position.z += dz;
	}

	/**
	 * Method that allows to rotate the entity in the world by increasing its rotation around axis.
	 * @param dx rotation to add around the x-axis
	 * @param dy rotation to add around the y-axis
	 * @param dz rotation to add around the z-axis
	 */
	public void increaseRotation(float dx, float dy, float dz) {
		this.rotationX += dx;
		this.rotationY += dy;
		this.rotationZ += dz;
	}



}
