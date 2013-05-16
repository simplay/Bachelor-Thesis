#version 150
// GLSL version 1.50 
// Vertex shader for diffuse shading in combination with a texture map

// Uniform variables, passed in from host program via suitable 
// variants of glUniform*
uniform mat4 projection;
uniform mat4 modelview;
uniform vec4 lightDirection;
#define MAX_LIGHTS 2
uniform vec4 directionArray[MAX_LIGHTS];

// Input vertex attributes; passed in from host program to shader
// via vertex buffer objects
in vec3 normal;
in vec4 position;
in vec2 texcoord;

// Output variables for fragment shader
out float ndotl;
out vec2 frag_texcoord;

// Output variables for fragment shader
out vec4 normal_out;
out vec4 eyeVector;
out vec4 light_direction[MAX_LIGHTS];

void main() {		
	// Compute dot product of normal and light direction
	// and pass color to fragment shader
	// Note: here we assume "lightDirection" is specified in camera coordinates,
	// so we transform the normal to camera coordinates, and we don't transform
	// the light direction, i.e., it stays in camera coordinates
	ndotl = max(dot(modelview * vec4(normal,0), lightDirection),0);

	// Transform position, including projection matrix
	// Note: gl_Position is a default output variable containing
	// the transformed vertex position
	// gl_Position = projection * modelview * position;
	
	vec4 vVertex = modelview * position;
	eyeVector = - vVertex;

	for(int i = 0; i< MAX_LIGHTS; i++) {
		light_direction[i] = directionArray[i] - vVertex;
	}

	// Pass texture coordiantes to fragment shader, OpenGL automatically
	// interpolates them to each pixel  (in a perspectively correct manner) 
	frag_texcoord = texcoord;
	
	normal_out = modelview * vec4(normal,0);
	
	gl_Position = projection * modelview * position;
	
	
	
}
