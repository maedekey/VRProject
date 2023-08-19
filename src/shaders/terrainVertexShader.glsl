#version 150

//light calculations are all calculated in the shader program, need position and colour of the light
//the normals calculate how bright a point on an object should be. Normals are vectors which indicate which direction the light goes to. It points to the exact direction the surface which is hit by the light faces the light.
//every point of a surface has a normal vector, and always pointing directly out of the face (perpendicular to the face)
in vec3 position; //position that we get from the vao
in vec2 textureCoordinates; //we can use textures in the fragment shader
in vec3 normal; //in : what we retrieve from the VAO

out vec2 pass_textureCoordinates; //out : what is sent  to the fragment shader
out vec3 surfaceNormal;
out vec3 toLightVector[4];
out vec3 toCameraVector;
out float visibility;

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform vec3 lightPosition[4];

//such small values for fog allows to create some nice faze, which allows entities to fade in the distance when we can't render them anymore
const float density = 0.0035;
const float gradient = 5.0;

uniform vec4 plane;

void main(void){

	vec4 worldPosition = transformationMatrix * vec4(position,1.0);

	gl_ClipDistance[0] = dot(worldPosition, plane);

	vec4 positionRelativeToCam = viewMatrix * worldPosition;
	gl_Position = projectionMatrix * positionRelativeToCam;
	pass_textureCoordinates = textureCoordinates;

	surfaceNormal = (transformationMatrix * vec4(normal,0.0)).xyz;
	for(int i=0;i<4;i++){
		toLightVector[i] = lightPosition[i] - worldPosition.xyz;
	}
	toCameraVector = (inverse(viewMatrix) * vec4(0.0,0.0,0.0,1.0)).xyz - worldPosition.xyz;

	float distance = length(positionRelativeToCam.xyz);
	visibility = exp(-pow((distance*density),gradient));
	visibility = clamp(visibility,0.0,1.0);
}