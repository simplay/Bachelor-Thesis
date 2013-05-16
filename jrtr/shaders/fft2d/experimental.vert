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


uniform vec4 camPos;

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


// do some kind of normalization of returned value
// divide by maximal amount
float getFactor(float k, float F, float G, float PI, float w){
	
	// area of CD with d=30cm
	float d = 0.3;
	float A = pow(0.5*d, 2.0)*PI;
	return (k*k*F*F*G)/(4*PI*PI*w*w*A);
}


//all function headers
float patch1d(int, int);

const float dimension = 100;
const int width = 20; // white of white stripe in patch


float globalReal = 0.0;
float globalImag = 0.0;


//sample patch, right now defined as a function: 100x100 pixel
float patch1d(int y){
	float res = 1.0;
	if(y <= (dimension/2-1)- width/2 || y >= dimension - (dimension/2 - width/2)) res = 0.0;
	return res;
}

//alpha = k*u/omega, beta = kv/omega after having perfomed rotation via vecotrfield
vec2 dft2(float alpha, float beta, float w, float k){
	float real = 0.0f;
	float imag = 0.0f;
	float prec = 1.5*pow(10.0, -7.0); // bump height
	float d = 1.0 /(dimension);
	float mStep = d;
	float nStep = d;
	
	for(int m=0; m < dimension; m++){
		for(int n=0; n < dimension; n++){
			float f = pow(-1.0,m+n)*patch1d(n); 
			
			float termsA = (2.0*PI*(-(mStep*m*alpha) -(nStep*n*beta) ));
			float termsB = (2.0*PI*w*k*f*prec);
		
			real += cos(termsA)*cos(termsB) - sin(termsA)*sin(termsB);
			imag += sin(termsA)*cos(termsB) + cos(termsA)*sin(termsB) ;
			
			
		}
	}

	globalReal = real*d;
	globalImag = imag*d;
	
	return vec2(real*d, imag*d);
}





void main() {


	vec4 LValues[16] = vec4[](
			vec4(0.32666668, 0.0, 0.79, 1.0),
			vec4(0.0042221546, 0.0, 1.0, 1.0),
			vec4(0.13511118, 0.0, 1.0, 1.0),
			vec4(0.0, 0.24000001, 1.0, 1.0),
			vec4(0.0, 0.6533331, 1.0, 1.0),
			vec4(0.0, 1.0, 0.8333343, 1.0),
			vec4(0.057142854, 1.0, 0.0, 1.0),
			vec4(0.3523804, 1.0, 0.0, 1.0),
			vec4(0.6476188, 1.0, 0.0, 1.0),
			vec4(0.94285715, 1.0, 0.0, 1.0),
			vec4(1.0, 0.7222229, 0.0, 1.0),
			vec4(1.0, 0.3777781, 0.0, 1.0),
			vec4(1.0, 0.033333335, 0.0, 1.0),
			vec4(1.0, 0.0, 0.0, 1.0),
			vec4(1.0, 0.0, 0.0, 1.0),
			vec4(1.0, 0.0, 0.0, 1.0)
	);
	

	
	
	
	//float omega = 8.0*PI*pow(10,7);
	float omega = 8.0*PI*pow(10,7);
	
	float wStep = 0.1;
	float hwStep = 0.05;
	int MaxIter = 16;
	int totRuns = 41;
	
	// we wont use something like that here mathematically
	// just in case we are going to try to look the same like 
	// stam's shader
	vec4 hiliteColor = vec4(radianceArray[0],1);
	
	// set up direction vectors
	
	// set up direction vectors
	vec4 eyePosition = -(modelview * position); // point in camera space
	vec4 lightPosition = modelview*vec4(directionArray[0].xyz, 0); // light position in camera space
	vec3 _k1 = normalize(lightPosition.xyz); // _k1: vector from lightPos to point P
	vec3 _k2 = normalize(eyePosition.xyz); // _k2: vector from point P to camera
	
	// components of V are in between the range [-2,2]
	// since _k1 and _k2 are normalized vectors.
	vec3 V = _k1 - _k2;
	
	float u = V.x;
	float v = V.y;
	float w = V.z;
	
	// normal and tangent vector in camera coordinates
	vec3 camNormal = normalize((modelview*vec4(normal,0)).xyz);
	vec3 camTangent = normalize((modelview*vec4(tangent,0)).xyz);
	
	float tol = 0.9999999;
	
	// handle vector-field defined by tangent
	vec3 ntangent = normalize(tangent);
	float dTemp = dot(ntangent,vec3(1.0,0.0,0.0) );
	
 	dTemp = (dTemp > tol)? tol : ((dTemp < -tol) ? -tol :  dTemp);
	float phi = acos(dTemp);
	
	vec3 tempV = cross(vec3(1.0,0,0), ntangent);
	if(tempV.z < 0.0) phi = -phi;
	
	
	// handle Fresnel factor
	float n_t = 1.0;
	float R0 = pow( (n_t - 1.0) / (n_t + 1.0) , 2); 
	float tmp =  (2.0*PI*(dot(-_k1, camNormal)/360.0));
	float alpha = acos(tmp);
	float teta = PI - alpha;
	float F = (R0 + (1.0 - R0) * pow(1.0 - cos(alpha), 5));

	
	// handle G factor
	float dominator = pow( 1.0 - dot(_k1,_k2) , 2);
	float leftNom = dot(-_k1, camNormal.xyz);
	leftNom = (leftNom > tol)? tol : ((leftNom < -tol) ? -tol :  leftNom);
	float rightNom = dot(_k2, camNormal);
	rightNom = (rightNom > tol)? tol : ((rightNom < -tol) ? -tol :  rightNom);
	float nominator = leftNom*rightNom;
	float G = dominator / nominator;
	

	// some inits for variables
	float real_part = 0.0;
	float imag_part = 0.0;
	vec4 brdf = vec4(0.0,0.0,0.0,1.0);
	vec2 scales = vec2(0);
	
	float reHeight = 0.0;
	float imHeight = 0.0;
	float factor1 = 0.0;
	float k = 0.0;
	bool mayRun = true;
	
	
	vec3 sum = vec3(0);
	for(int iter = 0; iter < 16; iter++){
		reHeight = 0.0;
		imHeight = 0.0;
		k = kValues[iter];
		vec2 modUV = getRotation(u,v,-phi);
		vec2 coords = vec2((k*modUV.x/omega), (k*modUV.y/omega));
		
		float alpha = (k*u/omega) - 0.5;
		float beta = (k*v/omega) - 0.5;
		
		vec2 res = vec2(0);
		if(abs(k*u) < omega/2.0 && abs(k*v) < omega/2.0)
			res = dft2(alpha, beta, w, k);

		float abs_P_Sq = pow((res.x*res.x + res.y*res.y),1.0);
		factor1 = getFactor(k, F, G, PI, w);
		factor1 = 1;
		vec4 waveColor = LValues[iter];
		
		brdf += vec4(factor1 * abs_P_Sq * LValues[iter]);
			
		
		sum += waveColor.xyz;	
		
	}
	
	
	//vec3 getGammaCorrection(vec3 rgb, float t, float f, float s, float gamma){
//	brdf.xyz =  M_sRGB_XR*brdf.xyz;
	
//	brdf.xyz = getGammaCorrection(brdf.xyz, 1, 0, 1, 1.0 / 2.2);
//	brdf.xyz = getGammaCorrection(brdf.xyz, 0.0031308, 0.055, 12.92, 1.0 / 2.4);
	
	// final set up
	
//	brdf.x /= sum.x;
//	brdf.y /= sum.y;
//	brdf.z /= sum.z;
	
	float scale = pow(10, 4);
	brdf *= 1;
	

	
//	brdf.w = 1;

	float ambient = 0.1;
	
	
	// test for error - debug mode
	if(brdf.x < 0.0 || brdf.y < 0.0 || brdf.z < 0.0){
		col = vec4(0,1,0,0);
	}else{
		col = brdf+vec4(ambient,ambient,ambient,1);
//		col = vec4(0.1,0.1,0.1,1);
	}
	
	
	frag_texcoord = texcoord;
	gl_Position = projection * modelview * position;
}
