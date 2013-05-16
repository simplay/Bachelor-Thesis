#version 150
// GLSL version 1.50
// Fragment shader for diffuse shading in combination with a texture map

// Uniform variables passed in from host program
#define MAX_LIGHTS 2
uniform vec3 radianceArray[MAX_LIGHTS];
uniform vec3 k_a;
uniform sampler2D Texture0;
uniform sampler2D Texture1;
uniform sampler2D Texture2;
uniform sampler2D Texture3;
uniform sampler2D Texture4;
uniform sampler2D Texture5;
uniform sampler2D Texture6;

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

	vec2 foo = frag_texcoord + vec2(2.5,2.5);
	
	vec4 tex1 = texture(Texture3, foo);
	vec4 tex2 = texture(Texture5, foo);
	frag_shaded	= col;
}
