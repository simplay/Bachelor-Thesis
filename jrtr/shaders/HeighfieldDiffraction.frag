#version 150
// GLSL version 1.50
// Fragment shader for diffuse shading in combination with a texture map

// Uniform variables passed in from host program
#define MAX_LIGHTS 2
uniform vec3 radianceArray[MAX_LIGHTS];
uniform vec3 k_a;
uniform sampler2D myTexture;
uniform float p;

// Variables passed in from the vertex shader
in float ndotl;
in vec2 frag_texcoord;
in vec4 light_direction[MAX_LIGHTS];
in vec4 normal_out;
in vec4 eyeVector;
in vec4 col;
// Output variable, will be written to framebuffer automatically
out vec4 frag_shaded;


void main() {

vec4 tex = texture(myTexture, frag_texcoord);
//	vec4 tex = texture(myTexture, vec2(0,0));
//	vec4 tex += texture(myTexture, vec2(0,1));
//	vec4 tex += texture(myTexture, vec2(1,0));
//	vec4 tex = texture(myTexture, vec2(0.0,0.0))+texture(myTexture, vec2(0.0,1.0))+texture(myTexture, vec2(1.0,0.0))+texture(myTexture, vec2(1.0,1.0));
	
	frag_shaded	= col;
}
