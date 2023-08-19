package entities;
import models.TexturedModel;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;
import renderEngine.DisplayManager;
import terrains.Terrain;

public class Player extends Entity {

	private static final float RUN_SPEED = 40; //in units per second
	private static final float TURN_SPEED = 160; //in degrees
	public static final float GRAVITY = -50;
	private static final float JUMP_POWER = 18;

	private float speed = 0;
	private float turnSpeed = 0;
	private float ySpeed = 0;

	private boolean isInAir = false; // we need a variable that allows us to know if the player is in the air, as we don't want it to be able to keep jumping limitlessly

	/**
	 * Constructor of a player. As a player is a more complex entity (as it moves), it extends the entity class.
	 * @param model of the player
	 * @param position of the player
	 * @param rotationX of the player
	 * @param rotationY of the player
	 * @param rotationZ of the player
	 * @param scale of the player
	 */
	public Player(TexturedModel model, Vector3f position, float rotationX, float rotationY, float rotationZ,
			float scale) {
		super(model, position, rotationX, rotationY, rotationZ, scale);
	}

	/**
	 * Method that allows the player to move. First, it checks the inputs, then, moves the player accordingly by
	 * updating its position  based on the speed and direction of the character. It first does it for the horizontal
	 * position of the player (so, on the x and z axis), where, if the player wants to rotate, has to rotate around
	 * the y-axis. Then, it computes jumping, by increasing its position on the y axis overtime, so that the animation
	 * is shared between several frames. Finally, it makes sure that the player lands on the terrain by retrieving the
	 * terrain height at the player position.
	 * @param terrain on which the player jumps
	 */
	public void move(Terrain terrain) {
		checkInputs();
		super.increaseRotation(0, turnSpeed * DisplayManager.getFrameTimeSeconds(), 0);
		float distance = speed * DisplayManager.getFrameTimeSeconds(); 	//it's the turn speed per second, so we have to multiply the speed by the time that has passed. We retrieve it from the display manager
		float dx = (float) (distance * Math.sin(Math.toRadians(super.getRotationY())));
		float dz = (float) (distance * Math.cos(Math.toRadians(super.getRotationY())));
		super.increasePosition(dx, 0, dz);

		//falling
		ySpeed += GRAVITY * DisplayManager.getFrameTimeSeconds();	//the speed on the y-axis is decreased by gravity
		super.increasePosition(0, ySpeed * DisplayManager.getFrameTimeSeconds(), 0);//increase the y position of the player when he jumps

		//collision detection of terrain
		float terrainHeight = terrain.getHeightOfTerrain(getPosition().x, getPosition().z);//we get the height of the terrain at the current player's x and z position
		if (super.getPosition().y < terrainHeight) {	//if the position of the player is underneath the terrain, it collided with the terrain, and we need to stop falling
			ySpeed = 0;
			isInAir = false;
			super.getPosition().y = terrainHeight;	//we put the player back on the terrain because we don't want it to be inside the terrain
		}
	}

	/**
	 * Method that checks if the player is in the air. If not, he is allowed to jump. Otherwise, he can't.
	 */
	private void jump() {
		if (!isInAir) {
			this.ySpeed = JUMP_POWER;
			isInAir = true;
		}
	}

	/**
	 * Method that checks the user inputs and acts accordingly. If he goes forward, increases the run speed. Backwards:
	 * decreases it. Goes left: increases the turn speed. Goes right: decreases it. Also allows the player to jump.
	 */
	private void checkInputs() {
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			this.speed = RUN_SPEED;
		} else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			this.speed = -RUN_SPEED;
		} else {
			this.speed = 0;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			this.turnSpeed = -TURN_SPEED;
		} else if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			this.turnSpeed = TURN_SPEED;
		} else {
			this.turnSpeed = 0;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			jump();
		}
	}
}
