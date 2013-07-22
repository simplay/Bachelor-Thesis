#version 150
#extension GL_EXT_gpu_shader4 : enable


out vec2 frag_texcoord;
in vec4 o_color;
in vec3 o_pos;
in vec3 o_light;
in vec3 o_normal;
in vec3 o_tangent;

out vec4 frag_shaded;

void main() {
	
	
	
	float attenuation = 1.0; 
    vec3 specularReflection;
    if (dot(o_normal, -o_light) < 0.0){
    	specularReflection = vec3(1.0, 0.0, 0.0); // no specular reflection
    }else{
    	// reflect(I,N) == I - 2.0 * dot(N, I) * N.
    	vec3 ref = reflect(o_light, o_normal);
    	float dotRL = dot(ref,  o_pos);
        specularReflection = attenuation * pow(max(0.0, dotRL), 5.0) * vec3(1.0);
    }
    
	
	
	frag_shaded	= vec4(specularReflection, 1.0);
}
