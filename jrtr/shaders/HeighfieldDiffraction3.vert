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
	
	return vec2(reC, imC);
}

float getFactor(float k, float F, float G, float PI, float w, float omega){
	float A = 0.000000005;
	A = A*A;
	//return (k*k*F*F*abs(G))/(4*PI*PI*w*w*A);
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
	float PI = 3.14159265358979323846264;
	float omega = 8.0*PI*pow(10,6);
	
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
	vec3 lightPosition = directionArray[0].xyz;
	vec3 P = (modelview * position).xyz;
	
	vec3 _k1 = normalize(lightPosition-P);
	vec3 _k2 = normalize(eyePosition.xyz-P);
	
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
	float C = 0.5;
	vec2 scales = vec2(0);
	
	float reHeight = 0.0;
	float imHeight = 0.0;
	float factor1 = 0.0;
	
	// wavelength 400nm
	float k = 2.0*PI/0.0000004;
	vec2 coords = vec2(k*u/omega, k*v/omega);
	if(w < -1.75){
		reHeight = dot(texture2DArray(TexArray, vec3(coords,0) ).rgb, vec3(0.299, 0.587, 0.114) );
		imHeight = dot(texture2DArray(TexArray, vec3(coords,9)).rgb, vec3(0.299, 0.587, 0.114) );
		scales = getC(reHeight,imHeight,0);
		real_part = scales.x;
		imag_part = scales.y;
		
	}else if(w >= -1.75 && w < -1.25){
		reHeight = dot(texture2DArray(TexArray, vec3(coords,1)).rgb, vec3(0.299, 0.587, 0.114) );
		imHeight = dot(texture2DArray(TexArray, vec3(coords,10)).rgb, vec3(0.299, 0.587, 0.114) );
		scales = getC(reHeight,imHeight,1);
		real_part = scales.x;
		imag_part = scales.y;	
		
	}else if(w >= -1.25 && w < -0.75){
		reHeight = dot(texture2DArray(TexArray, vec3(coords,2)).rgb, vec3(0.299, 0.587, 0.114) );
		imHeight = dot(texture2DArray(TexArray, vec3(coords,11)).rgb, vec3(0.299, 0.587, 0.114) );
		scales = getC(reHeight,imHeight,2);
		real_part = scales.x;
		imag_part = scales.y;	
		
	}else if(w >= -0.75 && w < -0.25){
		reHeight = dot(texture2DArray(TexArray, vec3(coords,3)).rgb, vec3(0.299, 0.587, 0.114) );
		imHeight = dot(texture2DArray(TexArray, vec3(coords,12)).rgb, vec3(0.299, 0.587, 0.114) );
		scales = getC(reHeight,imHeight,3);
		real_part = scales.x;
		imag_part = scales.y;	
		
	}else if(w >= -0.25 && w < 0.25){
		reHeight = dot(texture2DArray(TexArray, vec3(coords,4)).rgb, vec3(0.299, 0.587, 0.114) );
		imHeight = dot(texture2DArray(TexArray, vec3(coords,13)).rgb, vec3(0.299, 0.587, 0.114) );
		scales = getC(reHeight,imHeight,4);
		real_part = scales.x;
		imag_part = scales.y;	
		
	}else if(w >= 0.25 && w < 0.75){
		reHeight = dot(texture2DArray(TexArray, vec3(coords,5)).rgb, vec3(0.299, 0.587, 0.114) );
		imHeight = dot(texture2DArray(TexArray, vec3(coords,14)).rgb, vec3(0.299, 0.587, 0.114) );
		scales = getC(reHeight,imHeight,5);
		real_part = scales.x;
		imag_part = scales.y;

	}else if(w >= 0.75 && w < 1.25){
		reHeight = dot(texture2DArray(TexArray, vec3(coords,6)).rgb, vec3(0.299, 0.587, 0.114) );
		imHeight = dot(texture2DArray(TexArray, vec3(coords,15)).rgb, vec3(0.299, 0.587, 0.114) );
		scales = getC(reHeight,imHeight,6);
		real_part = scales.x;
		imag_part = scales.y;
		
	}else if(w >= 1.25 && w < 1.75){
		reHeight = dot(texture2DArray(TexArray, vec3(coords,7)).rgb, vec3(0.299, 0.587, 0.114) );
		imHeight = dot(texture2DArray(TexArray, vec3(coords,16)).rgb, vec3(0.299, 0.587, 0.114) );
		scales = getC(reHeight,imHeight,7);
		real_part = scales.x;
		imag_part = scales.y;
		
	}else{
		reHeight = dot(texture2DArray(TexArray, vec3(coords,8)).rgb, vec3(0.299, 0.587, 0.114) );
		imHeight = dot(texture2DArray(TexArray, vec3(coords,17)).rgb, vec3(0.299, 0.587, 0.114) );
		scales = getC(reHeight,imHeight,8);
		real_part = scales.x;
		imag_part = scales.y;
	}
	
	float abs_P_Sq = real_part*real_part + imag_part*imag_part;
	
	factor1 = getFactor(k, F, G, PI, w, omega);
	vec4 PL400 = vec4(factor1*abs_P_Sq*L400.xyz,1);
	
//	
//	// wavelength 450nm
//	k = 2.0*PI/0.00000045;
//	coords = vec2(k*u/omega,k*v/omega);
//	if(w < -1.75){
////		real_part = C*dot(texture2DArray(TexArray, vec3(coords,18)).xyz, vec3(0.299, 0.587, 0.114) );
////		imag_part = C*dot(texture2DArray(TexArray, vec3(coords,27)).xyz, vec3(0.299, 0.587, 0.114) );
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,18) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,27)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,9);
//		real_part = scales.x;
//		imag_part = scales.y;
//		
//	}else if(w >= -1.75 && w < -1.25){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,19) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,28)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,10);
//		real_part = scales.x;
//		imag_part = scales.y;	
//	}else if(w >= -1.25 && w < -0.75){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,20) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,29)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,11);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= -0.75 && w < -0.25){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,21) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,30)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,12);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= -0.25 && w < 0.25){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,22) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,31)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,13);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= 0.25 && w < 0.75){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,23) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,32)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,14);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= 0.75 && w < 1.25){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,24) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,33)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,15);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= 1.25 && w < 1.75){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,25) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,34)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,16);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else{
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,26) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,35)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,17);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}
//	abs_P_Sq = real_part*real_part + imag_part*imag_part;
//	factor1 = getFactor(k, F, G, PI, w, omega);
//	vec4 PL450 = vec4(factor1*abs_P_Sq*L450.xyz,1);
//	
//	
//	// wavelength 500nm
//	k = 2.0*PI/0.0000005;	
//	coords = vec2(k*u/omega,k*v/omega);
//	if(w < -1.75){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,36) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,45)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,18);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= -1.75 && w < -1.25){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,37) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,46)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,19);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= -1.25 && w < -0.75){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,38) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,47)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,20);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= -0.75 && w < -0.25){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,39) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,48)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,21);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= -0.25 && w < 0.25){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,40) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,49)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,22);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= 0.25 && w < 0.75){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,41) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,50)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,23);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= 0.75 && w < 1.25){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,42) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,51)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,24);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= 1.25 && w < 1.75){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,43) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,52)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,25);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else{
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,44) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,53)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,26);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}
//	abs_P_Sq = real_part*real_part + imag_part*imag_part; 
//	factor1 = getFactor(k, F, G, PI, w, omega);
//	vec4 PL500 = vec4(factor1*abs_P_Sq*L500.xyz,1);
//	
//	
//	// wavelength 550nm
//	k = 2.0*PI/0.00000055;
//	coords = vec2(k*u/omega,k*v/omega);
//	if(w < -1.75){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,54) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,63)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,27);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= -1.75 && w < -1.25){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,55) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,64)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,28);
//		real_part = scales.x;
//		imag_part = scales.y;	
//	}else if(w >= -1.25 && w < -0.75){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,56) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,65)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,29);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= -0.75 && w < -0.25){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,57) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,66)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,30);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= -0.25 && w < 0.25){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,58) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,67)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,31);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= 0.25 && w < 0.75){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,59) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,68)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,32);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= 0.75 && w < 1.25){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,60) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,69)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,33);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= 1.25 && w < 1.75){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,61) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,70)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,34);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else{
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,62) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,71)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,35);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}
//	abs_P_Sq = real_part*real_part + imag_part*imag_part;
//	factor1 = (k*k*F*F*G)/(4*PI*PI*w*w*omega);
//	vec4 PL550 = vec4(factor1*abs_P_Sq*L550.xyz,1);
//	
//	
//	// wavelength 600nm
//	k = 2.0*PI/0.0000006;
//	coords = vec2(k*u/omega,k*v/omega);
//	if(w < -1.75){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,72) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,81)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,36);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= -1.75 && w < -1.25){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,73) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,82)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,37);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= -1.25 && w < -0.75){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,74) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,83)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,38);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= -0.75 && w < -0.25){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,75) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,84)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,39);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= -0.25 && w < 0.25){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,76) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,85)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,40);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= 0.25 && w < 0.75){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,77) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,86)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,41);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= 0.75 && w < 1.25){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,78) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,87)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,42);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= 1.25 && w < 1.75){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,79) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,88)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,43);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else{
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,80) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,89)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,44);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}
//	abs_P_Sq = real_part*real_part + imag_part*imag_part; 
//	factor1 = getFactor(k, F, G, PI, w, omega);
//	vec4 PL600 = vec4(factor1*abs_P_Sq*L600.xyz,1);
//	
//	
//	// wavelength 650nm
//	k = 2.0*PI/0.00000065;
//	coords = vec2(k*u/omega,k*v/omega);
//	if(w < -1.75){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,90) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,99)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,45);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= -1.75 && w < -1.25){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,91) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,100)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,46);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= -1.25 && w < -0.75){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,92) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,101)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,47);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= -0.75 && w < -0.25){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,93) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,102)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,48);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= -0.25 && w < 0.25){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,94) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,103)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,49);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= 0.25 && w < 0.75){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,95) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,104)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,50);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= 0.75 && w < 1.25){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,96) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,105)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,51);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else if(w >= 1.25 && w < 1.75){
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,97) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,106)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,52);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}else{
//		reHeight = dot(texture2DArray(TexArray, vec3(coords,98) ).rgb, vec3(0.299, 0.587, 0.114) );
//		imHeight = dot(texture2DArray(TexArray, vec3(coords,107)).rgb, vec3(0.299, 0.587, 0.114) );
//		scales = getC(reHeight,imHeight,53);
//		real_part = scales.x;
//		imag_part = scales.y;
//	}
//	abs_P_Sq = real_part*real_part + imag_part*imag_part; 
//	factor1 = getFactor(k, F, G, PI, w, omega);
//	vec4 PL650 = vec4(factor1*abs_P_Sq*L650.xyz, 1);
//	
//	
	// final set up
	float frac = 1.0/1.0;
	float fac2 = 1.0 / 1.0;
	//float fac2 = 1.0 / 1;
	float ambient = 0.1;
	

	
	//col = fac2*fac2*fac2*frac*(PL650 + PL600 + PL550 + PL500 + PL450 + PL400)+vec4(ambient,ambient,ambient,1);
	
	col = fac2*fac2*fac2*fac2*frac*(PL400)+vec4(ambient,ambient,ambient,1);
	
//	if(G == 0.0) col = vec4(1,0,0,1);
//	else if(G < 0.0) col = vec4(0,1,0,1);
//	else col = vec4((100000.0*vec3(G,G,G)),1);
	frag_texcoord = texcoord;
	gl_Position = projection * modelview * position;
}
