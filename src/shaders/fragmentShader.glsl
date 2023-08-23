#version 140

in vec2 pass_textureCoordinates;
in vec3 surfaceNormal;
in vec3 toLightVector[4];
in vec3 toCameraVector;
in float visibility;

out vec4 out_Color;

uniform sampler2D modelTexture;  //represents the texture we use
uniform vec3 lightColour[4];
uniform vec3 attenuation[4];  //we need an attenuation vector for each of the light sources
uniform float shineDamper;
uniform float reflectivity;
uniform vec3 skyColour;

void main(void){
	//convert the input colour into an output colour

	vec3 unitNormal = normalize(surfaceNormal); //we have to normalize the vectors to make sure that their size is 1, to make only the direction matter and the size is irrelevant
	vec3 unitVectorToCamera = normalize(toCameraVector);
	
	vec3 totalDiffuse = vec3(0.0);
	vec3 totalSpecular = vec3(0.0);
	
	for(int i=0;i<4;i++){
		float distance = length(toLightVector[i]); //the tolight vector is a vector from the object to the light. Its length = distance from the light
		float attFactor = attenuation[i].x + (attenuation[i].y * distance) + (attenuation[i].z * distance * distance);
		vec3 unitLightVector = normalize(toLightVector[i]);	
		float nDotl = dot(unitNormal,unitLightVector); //float representative of how bright the pixel should be
		float brightness = max(nDotl,0.0); // EQUATION OF DIFFUSE LIGHT. should always be between 0 and 1. we don't care about negatives. that 0.2 makes sure that the brightness never goes below 0.2 --> ambient light. never black
		vec3 lightDirection = -unitLightVector; //need to create the vector that points in the direction that the light is coming from, which is the opposite of the vector pointing towards the light that we had in the diffuse lighting
		//now, we only need the reflected light vector
		vec3 reflectedLightDirection = reflect(lightDirection,unitNormal);
		//and finally, we can do the dot product between the camera vector and the reflectance vector:
		float specularFactor = dot(reflectedLightDirection , unitVectorToCamera);  //indicates how bright, without any dampening, the pixel should be
		specularFactor = max(specularFactor,0.0); //has to stay positive
		float dampedFactor = pow(specularFactor,shineDamper); //we apply the damping of the material. EQUATION FOR SPECULAR LIGHT
		totalDiffuse = totalDiffuse + (brightness * lightColour[i])/attFactor;

		//now, we can multiply the damped factor factor by the light colour, so that the specular light is the colour of the light source
		totalSpecular = totalSpecular + (dampedFactor * reflectivity * lightColour[i])/attFactor;
	}
	totalDiffuse = max(totalDiffuse, 0.2);
	
	vec4 textureColour = texture(modelTexture,pass_textureCoordinates);
	if(textureColour.a<0.5){ //the transparency is given by the alpha component of the colour. If the opacity is below 0.5, then we don't render.
		discard;
	}
	//to make the pixel colour bright, we multiply the brightness of the texture by the colour of the texture at one pixel
	out_Color =  vec4(totalDiffuse,1.0) * textureColour + vec4(totalSpecular,1.0); //returns the color of the pixel on the texture at the coords we give it. samples the texture that we give it in the sampler2D, sample it at defined texture coordinates(pass_textureCoords) --> get the colour of the pixel that it finds at those texture coords and return it. We output the returned colour to the pixel that is currently being processed.
	out_Color = mix(vec4(skyColour,1.0),out_Color, visibility); //compute the colour of the pixel by mixing it with the colour of the sky and the colour of the object to simulate fog. Mix allows us to mix 2 values(sky colour and out colour) and determine how(visibility factor).
}