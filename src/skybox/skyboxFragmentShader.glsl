#version 140

in vec3 textureCoords;
out vec4 out_Color;

uniform samplerCube cubeMap;
uniform samplerCube cubeMap2;
uniform float blendFactor;
uniform vec3 fogColour;

const float lowerLimit = 0.0; //center of the skybox
const float upperLimit = 30.0; //slightly above the horizon


void main(void){
    vec4 texture1 = texture(cubeMap, textureCoords);
    vec4 texture2 = texture(cubeMap2, textureCoords);
    vec4 finalColour = mix(texture1, texture2, blendFactor);

    float factor = (textureCoords.y - lowerLimit)/(upperLimit - lowerLimit); //Represents the visibility of each pixel of the sky. 0: below the lower limit, should be the fog colour. 1: above the upper limit, just uses the skybox texture colour. texturecoords.y : height of the pixel
    factor = clamp(factor, 0.0, 1.0);
    out_Color = mix(vec4(fogColour, 1.0), finalColour, factor);
}