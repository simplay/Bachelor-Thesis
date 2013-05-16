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

// Output variable, will be written to framebuffer automatically
out vec4 frag_shaded;


void main() {
	// our constants 		
	vec4 N = normalize(normal_out);
	vec4 color = vec4(0);
	vec4 tex = texture(myTexture, frag_texcoord);
	vec3 gm_k_d = vec3(tex.x, tex.y, tex.z);
	float glossmap_val = tex.x+tex.y+tex.z;
	vec3 gm_k_s = vec3(glossmap_val,glossmap_val,glossmap_val);
	
	// loop over each light source
	for(int i = 0; i< MAX_LIGHTS; i++) {
		vec4 L = normalize(light_direction[i]);
		float ndotl = max(dot(N,L),0);
		color += vec4(radianceArray[i],0)*ndotl*vec4(gm_k_d,0);
		vec4 e = normalize(eyeVector);
		vec4 R = normalize(reflect(-L,N));
		// specular: in phong model color is equal irradiance times BRDF
		color += vec4(radianceArray[i],0)*vec4(gm_k_s,0)*pow(max(0,dot(R,e)),p);
		// ambient
		color += vec4(k_a,1)*tex;
	}

	frag_shaded	= color;
}
