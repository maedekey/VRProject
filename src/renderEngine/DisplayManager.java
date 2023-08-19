package renderEngine;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.PixelFormat;

/**
 * Class that manages the display: sets parameters for the game window and updates what is shown in this window
 */
public class DisplayManager {
	
	private static final int WIDTH = 1280;
	private static final int HEIGHT = 720;
	private static final int FPS_CAP = 60;
	
	private static long lastFrameTime; //time at the last frame that occured
	private static float delta; //holds the time taken to render the previous frame

	/**
	 * Method that creates a window on game launch, and sets some parameters for the rendering.
	 */
	public static void createDisplay(){		
		ContextAttribs attribs = new ContextAttribs(3,2)
		.withForwardCompatible(true)
		.withProfileCore(true);
		
		try {
			Display.setDisplayMode(new DisplayMode(WIDTH,HEIGHT));
			Display.create(new PixelFormat(), attribs);
			Display.setTitle("VR PROJECT");
			GL11.glEnable(GL13.GL_MULTISAMPLE);
		} catch (LWJGLException e) {
			e.printStackTrace();
		}

		//tell opengl where in the display it can render the game
		GL11.glViewport(0,0, WIDTH, HEIGHT); // 2 first args: bottom left, 2 last args: top right of the display
		lastFrameTime = getCurrentTime();
	}

	/**
	 * Method that updates the display every single frame, and keeps track of the time needed for every frame.
	 */
	public static void updateDisplay(){
		Display.sync(FPS_CAP); //allows the game to run smoothly
		Display.update();
		long currentFrameTime = getCurrentTime(); //time at which the frame executes
		delta = (currentFrameTime - lastFrameTime)/1000f; //we calculate how long the last frame took to render
		lastFrameTime = currentFrameTime;
	}

	/**
	 * method that returns the time needed for a frame to execute
	 * @return time needed for a frame to execute
	 */
	public static float getFrameTimeSeconds(){
		return delta;
	}

	/**
	 * Method that closes the game window on stop of the game
	 */
	public static void closeDisplay(){
		Display.destroy();
	}

	/**
	 * Method that returns the current time in milliseconds
	 * @return current time in milliseconds
	 */
	private static long getCurrentTime(){
		return Sys.getTime()*1000/Sys.getTimerResolution();
	}
}
