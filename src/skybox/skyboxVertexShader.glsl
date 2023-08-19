#version 140

in vec3 position;
out vec3 textureCoords;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main(void){
	
	gl_Position = projectionMatrix * viewMatrix * vec4(position, 1.0);   //we want a rotation (sky needs to turn when we turn the camera) but doesn't translate (doesn't move when we move the camera)
	textureCoords = position;
	
}