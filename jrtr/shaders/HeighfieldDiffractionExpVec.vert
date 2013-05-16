// Diffraction shader using a heightfield
// crude approximation, working with fourier transformations.
// which have been precalculated.
// Michael Single


// TODO perform calulations with vectorfield, using tangent
// TODO look for a normalization factor
// TODO correct A
// TODO check constants
// TODO add coments and explenations
// TODO take constants out of main and define them as a constant value.
// TODO add more k values: 32 values, extend range to [400,700nm] wavelenth.

#version 150
#extension GL_EXT_gpu_shader4 : enable

// substitutes
#define MAX_LIGHTS 1
#define MAX_FACTORS 656
#define MAX_Weight 16

//Uniform variables, passed in from host program via suitable 
//variants of glUniform*
uniform mat4 projection;
uniform mat4 modelview;
uniform vec3 radianceArray[MAX_LIGHTS];
uniform vec3 brdf_weights[MAX_Weight];
uniform vec4 directionArray[MAX_LIGHTS];
uniform vec4 scalingFactors[MAX_FACTORS];
uniform vec4 global_extrema[1];
uniform sampler2DArray TexArray;
uniform float kValues[16];



in vec3 normal;
in vec4 position;
in vec2 texcoord;
in vec3 tangent;
in vec4 color;


//Output variables for fragment shader
out vec2 frag_texcoord;
out vec4 col;

// constants
const float PI = 3.14159265358979323846264;

const mat3 M_sRGB_XR = mat3(
		   3.2405, -1.5371, -0.4985, 
		   -0.9693, 1.8760, 0.0416, 
		   0.0556, -0.2040, 1.0572 
		);	

const mat3 M_sRGB_XR1 = mat3(
		2.3706743, -0.9000405, -0.4706338,
		-0.5138850,  1.4253036,  0.0885814,
		 0.0052982, -0.0146949,  1.0093968
		);

const mat3 M_Adobe_XR = mat3(
		2.0414, -0.5649, -0.3447,
		-0.9693,  1.8760,  0.0416,
		 0.0134, -0.01184,  1.0154
		);	


// gamma correction
vec3 getGammaCorrection(vec3 rgb, float t, float f, float s, float gamma){
	
	float q = (1+f);
	return vec3(q*pow(rgb.x,gamma)-f, q*pow(rgb.y,gamma)-f, q*pow(rgb.z,gamma)-f );
}

//use tangent in oder to consider the vector field.
vec2 getRotation(float u, float v, float phi){
	float uu = u*cos(phi) - v*sin(phi);
	float vv = u*sin(phi) + v*cos(phi);
	return vec2(uu, vv);
}

// returns correct scaling factor
// hard coded indices read - fix that for arbitrary w.
vec2 getC(float reHeight, float imHeight, int index){
	vec4 v = scalingFactors[index];
	float reMin = v.x;
	float reMax = v.y;
	float imMin = v.z;
	float imMax = v.w;
	
	float reC = reMin + reHeight*(reMax);
	float imC = imMin + imHeight*(imMax);
	
	float a = global_extrema[0].x;
	float b = global_extrema[0].y;
	float c = global_extrema[0].z;
	float d = global_extrema[0].w;
	
//	reC = (reC-a)/b;
//	imC = (imC-c)/d;
	
	return vec2(reC, imC); 

}

// do some kind of normalization of returned value
// divide by maximal amount
float getFactor(float k, float F, float G, float PI, float w, float omega){
	
	// area of CD with d=30cm
	float d = 0.3;
	d = 0.0000003;
	float A = pow(0.5*d, 2.0)*PI;
	return (k*k*F*F*G)/(4*PI*PI*w*w*A);
}

void main() {
	
	//float omega = 8.0*PI*pow(10,7);
	
	float omega = 8.0*PI*pow(10,7);
	
	float wStep = 0.1;
	float hwStep = 0.05;
	int MaxIter = 16;
	int totRuns = 41;
	
	// we wont use something like that here mathematically
	// just in case we are going to try to look the same like 
	// stam's shader
	vec4 hiliteColor = vec4(radianceArray[0],0);
	
	// set up direction vectors
	vec4 eyePosition = -(modelview * position); // point in camera space
	vec4 lightPosition = modelview*vec4(directionArray[0].xyz, 0); // light position in camera space
	vec3 P = (modelview * position).xyz; // point p under consideration
	vec3 _k1 = normalize(P - lightPosition.xyz); // _k1: vector from lightPos to point P
	vec3 _k2 = normalize(eyePosition.xyz - P); // _k2: vector from point P to camera
	
	// components of V are in between the range [-2,2]
	// since _k1 and _k2 are normalized vectors.
	vec3 V = _k1 - _k2;
	
	float u = V.x;
	float v = V.y;
	float w = V.z;
	
	// handle vector-field defined by tangent
	vec3 ntangent = normalize(tangent);
	float dTemp = dot(ntangent,vec3(1.0,0.0,0.0) );
	
 	dTemp = (dTemp > 0.999999)? 0.999999 : ((dTemp < -0.999999) ? -0.999999 :  dTemp);
	float phi = acos(dTemp);
	
	vec3 tempV = cross(vec3(1.0,0,0), ntangent);
	if(tempV.z < 0.0) phi = -phi;
	
	// handle Fresnel factor
	float n_t = 0.50;
	float R0 = pow( (n_t - 1) / (n_t + 1) , 2); 
	float alpha = acos( dot(normalize(-_k1), normalize(normal)) );
	float teta = (PI/2.0 - alpha);
	float F = R0 + (1.0-R0) * pow(1.0 - cos(teta), 5);
	//F = 1;
	
	// handle G factor
	float div = ( dot(-_k1, normal)*dot(_k2, normal) );
	//if(div == 0) div = 1.0;
	float G = pow(1.0-dot(_k1, _k2), 2.0 ) / div; 
	
	
	// some inits for variables
	float real_part = 0.0;
	float imag_part = 0.0;
	vec4 brdf = vec4(0.0,0.0,0.0,1.0);
	vec2 scales = vec2(0);
	
	float reHeight = 0.0;
	float imHeight = 0.0;
	float factor1 = 0.0;
	float k = 0.0;
	
	for(int iter = 0; iter < MaxIter; iter++){
		k = kValues[iter];
//		vec2 modUV = getRotation(u,v,-phi);
//		vec2 coords = vec2(k*modUV.x/omega, k*modUV.y/omega);
		
		vec2 modUV = getRotation(k*u/omega,k*v/omega,-phi);
		vec2 coords = vec2(modUV.x, modUV.y);
		
		
		bool isRunning = true;
		
		int runCount = 0;
		while(isRunning){
			float lowerWBound = -2.0 + wStep*runCount - hwStep;
			float upperWBound = -2.0 + wStep*runCount + hwStep;
			
			if( (w >= lowerWBound ) && (w < upperWBound ) ){
				int index1 = totRuns*2*iter + runCount;
				int index2 = totRuns*iter + runCount + 41;
				int index3 = totRuns*iter + runCount;

				reHeight = dot(texture2DArray(TexArray, vec3(coords, index1) ).rgb, vec3(0.299, 0.587, 0.114) );
				imHeight = dot(texture2DArray(TexArray, vec3(coords, index2) ).rgb, vec3(0.299, 0.587, 0.114) );
				scales = getC(reHeight, imHeight, index3);
				real_part = scales.x;
				imag_part = scales.y;
				isRunning = false;
			}

			runCount++;
			if(runCount == 41) isRunning = false; // bruteforce break;
		}
		
		float abs_P_Sq = sqrt(real_part*real_part + imag_part*imag_part);
		factor1 = getFactor(k, F, G, PI, w, omega);
//		brdf += vec4(factor1*abs_P_Sq*LValues[iter].xyz,1);
		brdf += vec4(factor1 * abs_P_Sq * (1*brdf_weights[iter] + 1*vec3(-0.0000, -0.0000, -0.0000) ),1);
	}
	
	
	//vec3 getGammaCorrection(vec3 rgb, float t, float f, float s, float gamma){
	vec3 preBRDF = brdf.xyz;
	brdf.xyz =  M_Adobe_XR*brdf.xyz;
	
	brdf.xyz = getGammaCorrection(brdf.xyz, 1, 0, 1, 1.0 / 2.2);
	
	// final set up
	float frac = 1.0 / 16.0;
	float fac2 = 1.0 / 70000.0;
	fac2 = 1.0 / 7500.0;
	fac2 = 1.0 / 3000000.0;
	fac2 = 1.0 / 15.0;
	fac2 = 1.0 / 9000.0;
	float ambient = 0.0;
	
	
	//brdf.xyz = preBRDF;
	
	// test for error - debug mode
	if(brdf.x < 0.0 || brdf.y < 0.0 || brdf.z < 0.0){
		col = vec4(0,1,0,1);
	}else{
		col = fac2*fac2*fac2*frac*brdf+vec4(ambient,ambient,ambient,1);
		//col = vec4(0.1,0.1,0.1,1);
	}
		

	
	frag_texcoord = texcoord;
	gl_Position = projection * modelview * position;
}
