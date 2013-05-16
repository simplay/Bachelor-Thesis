#version 150
// GLSL version 1.50 
// diffraction shader implementation 
// described in the paper "Diffraction Shaders" written by Jos Stam
// Michael Single

// predefined constants
#define MAX_LIGHTS 1


//Uniform variables, passed in from host program via suitable 
//variants of glUniform*
uniform mat4 projection;
uniform mat4 modelview;
uniform float d; // is equal light position
uniform vec3 radianceArray[MAX_LIGHTS];
uniform vec4 directionArray[MAX_LIGHTS];

// Input vertex attributes; passed in from host program to shader
// via vertex buffer objects
in vec3 normal;
in vec4 position;
in vec2 texcoord;
in vec3 tangent;
in vec4 color;

// Output variables for fragment shader
//out float ndotL;
//out vec4 normal_out;
out vec4 eyeVector; // k_2_hat
out vec4 frag_color;
out vec4 light_direction[MAX_LIGHTS];
out vec4 normal_out;
out vec2 frag_texcoord0;

vec3 blend3 (vec3 x){
	vec3 y = 1 - x * x;
	y = max(y, vec3 (0, 0, 0));
	return (y);
}


void main() {		
	
	float r = 1.5f; // roughness factor for: 1.5f seems to look nice
	vec4 hiliteColor = vec4(radianceArray[0],0);
	
	vec4 eyePosition = -(modelview * position);
	vec3 P = (modelview * position).xyz;
	vec4 tmplight = modelview*directionArray[0];
	vec3 lightPosition = tmplight.xyz;
	vec3 V = normalize(-lightPosition);
	vec3 L = normalize(-P);
	vec3 H = (L + V); // Halfway vector
	vec3 N = (modelview * vec4(normal,0)).xyz;
	vec3 T = (modelview * vec4(tangent,0)).xyz; // receiver
	
	
	
	

	
	
	
	// From the halfway vector between the light source and the receiver (not normalized), 
	// we compute the u value by projecting it onto the local tangent vector.
	float u =  dot(T, H) * d;
	//float u =  dot(T, L-V) * d;
	float w = dot(N, H); // component of the halfway vector in the normal direction.
	float e = r * u / w;
	float c = exp(-e * e);
	vec4 anis = hiliteColor * vec4(c, c, c, 1);
	
	// imo necessary nvidia did a mistake beam doesnt look symmetrix
	// it is rather a smoth transition from red to blue
	// notice in order to get a correct transition along the beam
	// i suppose we have to implement this diffraction shader as a fragment shader
	int shiftSpectrum = 0; 
	if (u < 0) {
		u = -u;
		shiftSpectrum = 1;
	}

	float lambda = 0;
	float n_min = 0;
	float n_max = 0;
	float lambda_min = 0.4; // 400nm red
	float lambda_max = 0.7; // 700nm blue
	
	n_min = (d*u)/lambda_max;
	n_max = (d*u)/lambda_min;

	int lower = 0;
	while(lower < n_min){
		lower++;
	}
	
	int upper = 0;
	while(upper < n_max){
		upper++;
	}
	upper--;
	
	vec4 cdiff = vec4(0, 0, 0, 1);
	
	// default lower = 1, upper = 8
	float step = 1.0;
	float f = 4;

	for (int n = lower; n <= upper; n = n + 1){
		float delta_n = 0;
		delta_n = n - n_min;
		float alpha = (lambda_max - lambda_min) / (n_max - n_min);
		lambda = lambda_min + alpha*delta_n;
		// u*d = n*lambda and y(lambda) is in [0,1]
		float y = ((10/3)*(lambda*(d/n)) - (4/3));
		cdiff.xyz += ( blend3(vec3(f * (y - 0.25), f * (y - 0.5), f * (y - 0.75))) ); 
//		cdiff.xyz += ( blend3(vec3(f * (y - 0.75), f * (y - 0.5), f * (y - 0.25))) ); 
	}
	
	for(int i = 0; i< MAX_LIGHTS; i++) {
		light_direction[i] = directionArray[i] + eyePosition;
	}
	
	normal_out = modelview * vec4(normal,0);
	frag_texcoord0 = texcoord;
	gl_Position = projection * modelview * position;

	
	// blue like bug
	vec4 bug = vec4(0,0,1,1);
	int b = 0;
	
	if(upper == 4) b = 1;
	
//	
//	if(b == 1)
//		frag_color = bug;
//	else
//		frag_color =color;
	
	frag_color = 0.5*cdiff + 0.5*anis;
	
	//frag_color = vec4(u,u,u,1);
	
	//frag_color = anis;
}
