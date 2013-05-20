#version 150
// Default fragment shader

#define MAX_LIGHTS 1

uniform vec3 radianceArray[MAX_LIGHTS];
uniform vec3 k_a;
uniform float p;
uniform vec3 k_d;
uniform vec3 k_s;
uniform sampler2D Texture0;

// Input variable, passed from vertex to fragment shader
// and interpolated automatically to each fragment
in vec4 frag_color;
in vec4 light_direction[MAX_LIGHTS];
in vec4 normal_out;
in vec4 eyeVector;
in vec2 frag_texcoord0;

// Output variable, will be written to framebuffer automatically
out vec4 out_color;

void main(){	
	
	vec4 N = normalize(normal_out);
	vec4 color = vec4(0);
	vec4 tex = texture(Texture0, frag_texcoord0);
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

	
	out_color = frag_color+0.3*color;
	//out_color = frag_color;
	//out_color = color;	
}
