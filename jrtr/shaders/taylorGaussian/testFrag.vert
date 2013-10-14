// Diffraction shader using a given heightfield
// adptive (regarding wavelength) diffraction shader 
// using taylor series approximation.
// which have been precalculated.
// Michael Single

// NB: find relative weighting s.t. no huge rescale fac2 is necessary anymore.
// NB: Write better documentation.

#version 150
#extension GL_EXT_gpu_shader4 : enable

// substitutes
#define MAX_LIGHTS 1
#define MAX_FACTORS 31
#define MAX_WEIGHTS 311

//Uniform variables, passed in from host program via suitable 
//variants of glUniform*
uniform mat4 projection;
uniform mat4 modelview;
uniform vec4 cop_w;
uniform vec3 radianceArray[MAX_LIGHTS];
uniform vec3 brdf_weights[MAX_WEIGHTS];
uniform vec4 directionArray[MAX_LIGHTS];
uniform vec4 scalingFactors[MAX_FACTORS];
uniform vec4 global_extrema[1];
uniform sampler2DArray TexArray;
uniform vec4 camPos;

uniform float LMIN;
uniform float LMAX;
uniform float approxSteps;
uniform float dimN;
uniform float dimSmall; // not used right now
uniform float dimDiff; // not used right now
uniform float repNN; // not used right now
uniform int periodCount;
uniform float maxBumpHeight;
uniform float patchSpacing;


in vec3 normal;
in vec4 position;
in vec2 texcoord;
in vec3 tangent;
in vec4 color;

//Output variables for fragment shader
out vec2 frag_texcoord;
out vec3 o_pos;
out vec3 o_light;
out vec3 o_normal;
out vec3 o_tangent;


void main() {
    vec3 N = normalize(vec4(normal,0.0)).xyz;
    vec3 T = normalize(vec4(tangent,0.0)).xyz;
    vec3 B = normalize(cross(N, T));
    
	// directional light source
	vec3 Pos =  ((cop_w-position)).xyz; // point in camera space
	vec4 lightDir = (directionArray[0]); // light direction in camera space
	lightDir = normalize(lightDir);
	
	// light direction: from camera space to tangent space
	
	float lx = dot(lightDir.xyz, T);
	float ly = dot(lightDir.xyz, B);
	float lz = dot(lightDir.xyz, N);
	
//	lightDir.x = dot(lightDir.xyz, T); 
//	lightDir.y = dot(lightDir.xyz, B);
//	lightDir.z = dot(lightDir.xyz, N);
	
	lightDir.w = 0.0;
	lightDir.xyz = vec3(lx, ly, lz);
	// position: from camera space to tangent space
	
	
	float px = dot(Pos, T);
	float py = dot(Pos, B);
	float pz = dot(Pos, N);
	
//	Pos.x = dot(Pos, T);
//	Pos.y = dot(Pos, B);
//	Pos.z = dot(Pos, N);
	
	Pos.xyz = vec3(px, py, pz);

	o_pos = Pos;
	o_light = lightDir.xyz;
	o_normal = N;
	o_tangent = T;
		
	frag_texcoord = texcoord;
	gl_Position = projection * modelview * position;
}
