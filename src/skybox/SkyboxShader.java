package skybox;
 
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import renderEngine.DisplayManager;
import shaders.ShaderProgram;
import toolbox.Maths;
import entities.Camera;
 
public class SkyboxShader extends ShaderProgram{
 
    private static final String VERTEX_FILE = "src/skybox/skyboxVertexShader.glsl";
    private static final String FRAGMENT_FILE = "src/skybox/skyboxFragmentShader.glsl";
    
    private static final float ROTATE_SPEED = 1f;
     
    private int location_projectionMatrix;
    private int location_viewMatrix;
    private int location_fogColour;
    private int location_cubeMap;
    private int location_cubeMap2;
    private int location_blendFactor;
    
    private float rotation = 0;
     
    public SkyboxShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }
     
    public void loadProjectionMatrix(Matrix4f matrix){
        super.loadMatrix(location_projectionMatrix, matrix);
    }
 
    public void loadViewMatrix(Camera camera){
        Matrix4f matrix = Maths.createViewMatrix(camera);
        matrix.m30 = 0; //in a view matrix, the translation is determined by the last column (4x4 matrix). So we have to set this last column to 0,0,0, so that it never moves, as the skybox is never supposed to move.
        matrix.m31 = 0;
        matrix.m32 = 0;
        rotation += ROTATE_SPEED * DisplayManager.getFrameTimeSeconds(); //as the rotation of the skybox is an animation, we multiply it by the elapsed seconds
        Matrix4f.rotate((float) Math.toRadians(rotation), new Vector3f(0,1,0), matrix, matrix); //0,1,0: we rotate around the y-axis to simulate the clouds moving
        super.loadMatrix(location_viewMatrix, matrix);
    }
    
    public void loadFogColour(float r, float g, float b){
    	super.loadVector(location_fogColour, new Vector3f(r, g, b));
    }
    
    public void connectTextureUnits(){
    	super.loadInt(location_cubeMap, 0);
    	super.loadInt(location_cubeMap2, 1);
    }
    
    public void loadBlendFactor(float blend){
    	super.loadFloat(location_blendFactor, blend);
    }
     
    @Override
    protected void getAllUniformLocations() {
        location_projectionMatrix = super.getUniformLocation("projectionMatrix");
        location_viewMatrix = super.getUniformLocation("viewMatrix");
        location_fogColour = super.getUniformLocation("fogColour");
        location_blendFactor = super.getUniformLocation("blendFactor");
        location_cubeMap = super.getUniformLocation("cubeMap");
        location_cubeMap2 = super.getUniformLocation("cubeMap2");
    }
 
    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
    }
 
}