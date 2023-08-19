#version 140

out vec4 out_colour;

in vec2 textureCoords1;
in vec2 textureCoords2;
in float blend;


uniform sampler2D particleTexture;

void main(void){
    vec4 colour1 = texture(particleTexture, textureCoords1); //we have a colour by sampling the texture of the animation (current)
    vec4 colour2 = texture(particleTexture, textureCoords2);

	out_colour = mix(colour1, colour2, blend); //animations can fade from one texture to the other

}