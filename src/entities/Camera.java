package entities;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;


public class Camera {

	private float distanceFromPlayer = 50; //the player can zoom in to reduce or zoom out to increase this value
	private float angleAroundPlayer = 0; //the camera always points at the player, no matter the value. It turns around the player.
	private final Vector3f position = new Vector3f(410,10,410);
	private float pitch = 20; //rotation of the camera around x, y and z axis. = how high or low the camera is from the ground
	private float yaw = 0; //how much left/right the camera is aiming

	private final Player player;

	/**
	 * The camera follows the player around, so it needs to access information about the player.
	 * @param player being followed by the camera
	 */
	public Camera(Player player){
		this.player = player;

	}

	public Vector3f getPosition() {
		return position;
	}

	public float getPitch() {
		return pitch;
	}

	public float getYaw() {
		return yaw;
	}

	/**
	 * Method called everytime we want to move the camera around (called every frame). First it retrieves how much the
	 * player zoomed the camera and moved it around. Then, it retrieves the angle of the camera around the player, and
	 * calculates its horizontal and vertical distance from it. Then, from there, it calculates the camera position, and
	 * retrieves the yaw.
	 */
	public void move(){
		calculateZoom();
		calculatePitch();
		calculateAngleAroundPlayer();
		float horizontalDistance = calculateHorizontalDistance();
		float verticalDistance = calculateVerticalDistance();
		calculateCameraPosition(horizontalDistance, verticalDistance);
		this.yaw = 180-(player.getRotationY() + angleAroundPlayer); //By default, the camera and the player share the same angle of rotation around the y-axis. But when the player moves the camera around, this angle becomes rotation of the player around y axis + angle around player
	}

	/**
	 * Calculates the position of the camera in the 3D world from the player's position. It applies basic geometry and
	 * trigonometry to find it.
	 * @param horizontalDistance from the camera to the player
	 * @param verticalDistance from the camera to the player
	 */

	private void calculateCameraPosition(float horizontalDistance, float verticalDistance){
		float theta = player.getRotationY() + angleAroundPlayer; //The camera is at distance d of the player. This d as 2 component: an X component and a Z component. We can draw a triangle from this X and Z component, where d will be the hypothenuse. By SOHCAHTOA, we can find this X and Z components, which we call offset, that allows us to calculate the camera position.
		float offsetX = (float) (horizontalDistance * Math.sin(Math.toRadians(theta))); //SOH
		float offsetZ = (float) (horizontalDistance * Math.cos(Math.toRadians(theta))); //CAH
		position.x = player.getPosition().x - offsetX; //We do - because the X and Z offsets go from the player's back to the camera. So, it goes in the negative direction.
		position.z = player.getPosition().z - offsetZ;
		position.y = player.getPosition().y + verticalDistance;
	}

	/**
	 * Computes the horizontal distance from the player to the camera. To do so, we retrieve the distance from the
	 * player, and apply CAH from SOHCAHTOA.
	 * @return the horizontal distance from the player.
	 */
	private float calculateHorizontalDistance(){
		return (float) (distanceFromPlayer * Math.cos(Math.toRadians(pitch))); //sohcahtoa
	}

	private float calculateVerticalDistance(){
		return (float) (distanceFromPlayer * Math.sin(Math.toRadians(pitch))); //sohcahtoa
	}

	/**
	 * Method that computes the zoom level based on the wheel of the mouse and decreases it: if we scroll down, the
	 * position increases.
	 */
	private void calculateZoom(){
		float zoomLevel = Mouse.getDWheel() * 0.1f;
		distanceFromPlayer -= zoomLevel;
	}

	/**
	 * Method that computes the pitch (vertical angle of the camera) when the player presses the left click down.
	 */
	private void calculatePitch(){
		if(Mouse.isButtonDown(0)){
			float pitchChange = Mouse.getDY() * 0.1f;
			pitch -= pitchChange;
		}
	}

	/**
	 * Method that computes the horizontal angle of the camera when the player presses the left click down.
	 */
	private void calculateAngleAroundPlayer(){
		if(Mouse.isButtonDown(0)){
			float angleChange = Mouse.getDX()*0.3f;
			angleAroundPlayer -= angleChange;
		}
	}
}
