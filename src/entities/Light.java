package entities;
import org.lwjgl.util.vector.Vector3f;

public class Light {
	
	private Vector3f position;
	private Vector3f colour;
	private Vector3f attenuation = new Vector3f(1, 0, 0);//not really a vector, but easier if we store the coefficients in one. Default (1,0,0) is no attenuation at all

	/**
	 * Constructor that takes as parameters the position and the colour of the light.
	 * @param position of the light
	 * @param colour of the light
	 */
	public Light(Vector3f position, Vector3f colour) {
		this.position = position;
		this.colour = colour;
	}

	/**
	 * Constructor that takes as parameters the position, the colour of the light and the attenuation of the light.
	 * @param position of the light
	 * @param colour of the light
	 * @param attenuation of the light
	 */
	public Light(Vector3f position, Vector3f colour, Vector3f attenuation) {
		this.position = position;
		this.colour = colour;
		this.attenuation = attenuation;
	}
	
	public Vector3f getAttenuation(){
		return attenuation;
	}

	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}

	public Vector3f getColour() {
		return colour;
	}

	public void setColour(Vector3f colour) {
		this.colour = colour;
	}
	

}
