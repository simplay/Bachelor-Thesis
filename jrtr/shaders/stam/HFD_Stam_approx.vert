// Diffraction shader using a heightfield
// crude approximation, working with fourier transformations.
// which have been precalculated.
// Michael Single

// TODO check direction vectors
// TODO implement calculation of F
// TODO implement calculation of G
// TODO multiply brdf with constants
// TODO perform calulations with vectorfield, using tangent


#version 150
#extension GL_EXT_gpu_shader4 : enable

// substitutes
#define MAX_LIGHTS 1
#define MAX_FACTORS 54


//Uniform variables, passed in from host program via suitable 
//variants of glUniform*
uniform mat4 projection;
uniform mat4 modelview;
uniform vec3 radianceArray[MAX_LIGHTS];
uniform vec4 directionArray[MAX_LIGHTS];
uniform vec4 scalingFactors[MAX_FACTORS];
uniform sampler2DArray TexArray;


in vec3 normal;
in vec4 position;
in vec2 texcoord;
in vec3 tangent;
in vec4 color;


//Output variables for fragment shader
out vec2 frag_texcoord;
out vec4 col;

//float xScale = scalingFactors[0].w;

// returns correct scaling factor
// hard coded indices read - fix that for arbitrary w.
vec2 getC(float reHeight, float imHeight, int index){
	vec4 v = scalingFactors[index];
	float reMin = v.x;
	float reMax = v.y;
	float imMin = v.z;
	float imMax = v.w;
	
	float reC = reMin + reHeight*(reMax-reMin);
	float imC = imMin + imHeight*(imMax-imMin);
	
//	float reC = reHeight;
//	float imC = imHeight;
	return (vec2(reC, imC) - vec2(100,100) ) / 5000.0;
//	return vec2(reC, imC)/30.0; // if k1 k2 -P
}

float getFactor(float k, float F, float G, float PI, float w, float omega){
	float A = 0.0000005;
	A = A*A;
	//return (k*k*F*F*G)/(4*PI*PI*w*w*A);
	return 1.0;
}

void main() {
	// our constants
	vec4 L400 = vec4(0.286, 0.0, 0.647, 1.0);
	vec4 L450 = vec4(0.0, 0.199, 1.0, 1.0);
	vec4 L500 = vec4(0.0, 1.0, 0.498, 1.0);
	vec4 L550 = vec4(0.568, 1.0, 0.0, 1.0);
	vec4 L600 = vec4(1.0, 0.690, 0.0, 1.0);
	vec4 L650 = vec4(1.0, 0.0, 0.0, 1.0);
	
	vec4 LValues[6] = vec4[](L400,L450,L500,L550,L600,L650);
	
	float PI = 3.14159265358979323846264;
	float omega = 8.0*PI*pow(10,6);
	float A = omega*omega;
	float kValues[6] = float[](
								2.0*PI/0.0000004, 
								2.0*PI/0.00000045, 
								2.0*PI/0.00000050, 
								2.0*PI/0.00000055, 
								2.0*PI/0.0000006,
								2.0*PI/0.00000065
								);
	
	// index T maps to (k,w)
	// scalingFactors[T].x 
	// scalingFactors[T].y
	// scalingFactors[T].y
	// scalingFactors[T].w
	
	// we wont use something like that here mathematically
	// just in case we are going to try to look the same like 
	// stam's shader
	vec4 hiliteColor = vec4(radianceArray[0],0);
	
	// check this sign again since + seems to produce way better results...
	// really check this...
	// also try to introduce the tangent apporach.
	vec4 eyePosition = -(modelview * position);  
	vec3 lightPosition = -directionArray[0].xyz;
	vec3 P = (modelview * position).xyz;
	
//	vec3 _k1 = normalize(lightPosition-P);
//	vec3 _k2 = normalize(eyePosition.xyz-P);
	
	vec3 _k1 = normalize(lightPosition);
	vec3 _k2 = normalize(eyePosition.xyz);
	
	// components of V are in between the range [-2,2]
	// since _k1 and _k2 are normalized vectors.
	vec3 V = _k1 - _k2;
	
	float u = V.x;
	float v = V.y;
	float w = V.z;
	
	
	
	float R0 = 0.8; 
	float F = R0 + (1.0-R0) * pow((1.0 - dot(normalize(V), normalize(normal))),5);
	
	// change this F to Fresnel of Schlick Approx
	// G = (1-_k1*_k2) / (cos(tet1)*cos(teta2))
	
	float div = ( dot(_k1, normal)*dot(_k2, normal) );
	if(div == 0) div = 1.0;
	float G = (1.0-dot(_k1, _k2) )*(1.0-dot(_k1, _k2) ) / div; 
	//float G = 0.1; 
	
	// some inits for variables
	float real_part = 0.0;
	float imag_part = 0.0;
	vec4 brdf = vec4(0.0,0.0,0.0,1.0);
	vec2 scales = vec2(0);
	
	float reHeight = 0.0;
	float imHeight = 0.0;
	float factor1 = 0.0;
	float k = 0.0;
	
	int MaxIter = 6;
	for(int iter = 0; iter < MaxIter; iter++){
		k = kValues[iter];
		vec2 coords = vec2(k*u/omega, k*v/omega);
		if(w < -1.75){
			reHeight = dot(texture2DArray(TexArray, vec3(coords,MaxIter*iter+0) ).rgb, vec3(0.299, 0.587, 0.114) );
			imHeight = dot(texture2DArray(TexArray, vec3(coords,MaxIter*iter+0+9)).rgb, vec3(0.299, 0.587, 0.114) );
			scales = getC(reHeight,imHeight,MaxIter*iter+0);
			real_part = scales.x;
			imag_part = scales.y;
			
		}else if(w >= -1.75 && w < -1.25){
			reHeight = dot(texture2DArray(TexArray, vec3(coords,MaxIter*iter+1)).rgb, vec3(0.299, 0.587, 0.114) );
			imHeight = dot(texture2DArray(TexArray, vec3(coords,MaxIter*iter+1+9)).rgb, vec3(0.299, 0.587, 0.114) );
			scales = getC(reHeight,imHeight,MaxIter*iter+1);
			real_part = scales.x;
			imag_part = scales.y;	
			
		}else if(w >= -1.25 && w < -0.75){
			reHeight = dot(texture2DArray(TexArray, vec3(coords,MaxIter*iter+2)).rgb, vec3(0.299, 0.587, 0.114) );
			imHeight = dot(texture2DArray(TexArray, vec3(coords,MaxIter*iter+2+9)).rgb, vec3(0.299, 0.587, 0.114) );
			scales = getC(reHeight,imHeight,MaxIter*iter+2);
			real_part = scales.x;
			imag_part = scales.y;	
			
		}else if(w >= -0.75 && w < -0.25){
			reHeight = dot(texture2DArray(TexArray, vec3(coords,MaxIter*iter+3)).rgb, vec3(0.299, 0.587, 0.114) );
			imHeight = dot(texture2DArray(TexArray, vec3(coords,MaxIter*iter+3+9)).rgb, vec3(0.299, 0.587, 0.114) );
			scales = getC(reHeight,imHeight,MaxIter*iter+3);
			real_part = scales.x;
			imag_part = scales.y;	
			
		}else if(w >= -0.25 && w < 0.25){
			reHeight = dot(texture2DArray(TexArray, vec3(coords,MaxIter*iter+4)).rgb, vec3(0.299, 0.587, 0.114) );
			imHeight = dot(texture2DArray(TexArray, vec3(coords,MaxIter*iter+4+9)).rgb, vec3(0.299, 0.587, 0.114) );
			scales = getC(reHeight,imHeight,MaxIter*iter+4);
			real_part = scales.x;
			imag_part = scales.y;	
			
		}else if(w >= 0.25 && w < 0.75){
			reHeight = dot(texture2DArray(TexArray, vec3(coords,MaxIter*iter+5)).rgb, vec3(0.299, 0.587, 0.114) );
			imHeight = dot(texture2DArray(TexArray, vec3(coords,MaxIter*iter+5+9)).rgb, vec3(0.299, 0.587, 0.114) );
			scales = getC(reHeight,imHeight,MaxIter*iter+5);
			real_part = scales.x;
			imag_part = scales.y;

		}else if(w >= 0.75 && w < 1.25){
			reHeight = dot(texture2DArray(TexArray, vec3(coords,MaxIter*iter+6)).rgb, vec3(0.299, 0.587, 0.114) );
			imHeight = dot(texture2DArray(TexArray, vec3(coords,MaxIter*iter+6+9)).rgb, vec3(0.299, 0.587, 0.114) );
			scales = getC(reHeight,imHeight,MaxIter*iter+6);
			real_part = scales.x;
			imag_part = scales.y;
			
		}else if(w >= 1.25 && w < 1.75){
			reHeight = dot(texture2DArray(TexArray, vec3(coords,MaxIter*iter+7)).rgb, vec3(0.299, 0.587, 0.114) );
			imHeight = dot(texture2DArray(TexArray, vec3(coords,MaxIter*iter+7+9)).rgb, vec3(0.299, 0.587, 0.114) );
			scales = getC(reHeight,imHeight,MaxIter*iter+7);
			real_part = scales.x;
			imag_part = scales.y;
			
		}else{
			reHeight = dot(texture2DArray(TexArray, vec3(coords,MaxIter*iter+8)).rgb, vec3(0.299, 0.587, 0.114) );
			imHeight = dot(texture2DArray(TexArray, vec3(coords,MaxIter*iter+8+9)).rgb, vec3(0.299, 0.587, 0.114) );
			scales = getC(reHeight,imHeight,MaxIter*iter+8);
			real_part = scales.x;
			imag_part = scales.y;
		}
		
		float abs_P_Sq = real_part*real_part + imag_part*imag_part;
		
		factor1 = getFactor(k, F, G, PI, w, omega);
		brdf += vec4(factor1*abs_P_Sq*LValues[iter].xyz,1);
	}
	
	
	
	// final set up
	float frac = 1.0/6.0;
	float fac2 = 1.0 / 1.0;
	float ambient = 0.0;
	

	
	col = fac2*frac*brdf+vec4(ambient,ambient,ambient,1);

	frag_texcoord = texcoord;
	gl_Position = projection * modelview * position;
}
