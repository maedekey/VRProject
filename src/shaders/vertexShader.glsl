#version 150

//light calculations are all calculated in the shader program, need position and colour of the light
//the normals calculate how bright a point on an object should be. Normals are vectors which indicate which direction the light goes to. It points to the exact direction the surface which is hit by the light faces the light.
//every point of a surface has a normal vector, and always pointing directly out of the face (perpendicular to the face)

in vec3 position;
in vec2 textureCoordinates;
in vec3 normal;

out vec2 pass_textureCoordinates;
out vec3 surfaceNormal;
out vec3 toLightVector[4];
out vec3 toCameraVector;
out float visibility;

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform vec3 lightPosition[4];

uniform float useFakeLighting;

uniform float numberOfRows;
uniform vec2 offset;

const float density = 0.0035;
const float gradient = 3.0;

uniform vec4 plane;

void main(void){
	//method that runs everytime a vertex is processed by the shader
	vec4 worldPosition = transformationMatrix * vec4(position,1.0); //position of the vertex in the world
	
	gl_ClipDistance[0] = dot(worldPosition, plane);
	
	vec4 positionRelativeToCam = viewMatrix * worldPosition; //We need the position of a vertex to the camera tod etermine how much it is affected by fog
	gl_Position = projectionMatrix * positionRelativeToCam;  //we tell openGL where to render the vertex on the screen
	pass_textureCoordinates = (textureCoordinates/numberOfRows) + offset;
	
	vec3 actualNormal = normal;
	if(useFakeLighting > 0.5){
		actualNormal = vec3(0.0,1.0,0.0);  //normal points directly up if dealing with selected objects, such as grass
	}
	
	surfaceNormal = (transformationMatrix * vec4(actualNormal,0.0)).xyz; //sometimes we rotate our model, so normal is not always = to surface normal, because its direction is rotated when the model is rotated. We transform our normal into a vec4 so that we can multiply it by the tranfo matrix
	for(int i=0;i<4;i++){
		toLightVector[i] = lightPosition[i] - worldPosition.xyz; //we need the difference between the light position and the position of the vertex on the model. but the pos will change if we used a transformation on the position, so we have to first multiply it by the transformation matrix
	}
	toCameraVector = (inverse(viewMatrix) * vec4(0.0,0.0,0.0,1.0)).xyz - worldPosition.xyz; //viewmatrix contains negative position of the camera position (reminder). we multiply by an empty vec4 vector to transform everything into a 4D vector and we obtain the camera position
	
	float distance = length(positionRelativeToCam.xyz);
	visibility = exp(-pow((distance*density),gradient)); //equation of fog
	visibility = clamp(visibility,0.0,1.0); //We want the visibility to stay between 0 and 1

	
}