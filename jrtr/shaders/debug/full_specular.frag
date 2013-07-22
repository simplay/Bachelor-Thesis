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
	
	vec3 _k2 = normalize(o_pos); //vector from point P to camera
	vec3 _k1 = normalize(o_light); // light direction, same for every point		
	vec3 V = _k1 - _k2;
	
	float attenuation = 1.0; 
    vec3 specularReflection;
    if (dot(o_normal, -_k1) < 0.0){
    	specularReflection = vec3(1.0, 0.0, 0.0); // no specular reflection
    }else{
    	// reflect(I,N) == I - 2.0 * dot(N, I) * N.
    	vec3 ref = reflect(-_k1, o_normal);
    	float dotRL = -dot(ref,  _k2);
        specularReflection = attenuation * pow(max(0.0, dotRL), 5.0) * vec3(1.0);
    }
    
	
	
	frag_shaded	= vec4(specularReflection, 1.0);
}
