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
	
	return vec2(u, v);
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
	
	reC = (reC-a)/b;
	imC = (imC-c)/d;
	
	return vec2(reC, imC); 

}

// do some kind of normalization of returned value
// divide by maximal amount
float getFactor(float k, float F, float G, float PI, float w){
	
	// area of CD with d=30cm
	float d = 0.3;
	float A = pow(0.5*d, 2.0)*PI;
	return (k*k*F*F*G)/(4*PI*PI*w*w*A);
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
	

	
	
	
	
	float omega = 8.0*PI*pow(10,7);
	
	float wStep = 0.1;
	float hwStep = 0.05;
	int MaxIter = 16;
	int totRuns = 41;
	
	
	// dir light source
	vec3 P = (modelview * position).xyz;
	vec4 lightPosition = modelview*(directionArray[0]);
	
	vec3 _k2 = normalize(-P);
	vec3 _k1 = normalize(lightPosition.xyz);

	
	// point light source
//	vec4 eyePosition = -(modelview * position); // point in camera space
//	vec4 lightPosition = modelview*vec4(directionArray[0].xyz, 1); // light position in camera space
//	vec3 P = (modelview * position).xyz; // point p under consideration
//	vec3 _k1 = normalize(P - lightPosition.xyz); // _k1: vector from lightPos to point P
//	vec3 _k2 = normalize(eyePosition.xyz - P); // _k2: vector from point P to camera
	
	
//	Vector4f P = matrix4fVector4fProduct(cameraMatrix, position);
//	Vector4f lookAT = new Vector4f(-P.x, -P.y, -P.z, -P.w);
//	Vector4f lightDir = matrix4fVector4fProduct(cameraMatrix, lightDirection);
//	k1.sub(P, lightDir); // light source is a point light
//	k2 = lookAT;
	
	
	
	
	vec3 V = _k1 - _k2;
	
	float u = V.x;
	float v = V.y;
	float w = V.z;
	
	// normal and tangent vector in camera coordinates
	vec3 camNormal = normalize((modelview*vec4(normal,0)).xyz);
	vec3 camTangent = normalize((modelview*vec4(tangent,0)).xyz);
	
	float tol = 0.999999999;
	
	// handle vector-field defined by tangent
	vec3 ntangent = normalize(tangent);
	float dTemp = dot(ntangent,vec3(1.0,0.0,0.0) );
	
 	dTemp = (dTemp > tol)? tol : ((dTemp < -tol) ? -tol :  dTemp);
	float phi = acos(dTemp);
	
	vec3 tempV = cross(vec3(1.0,0,0), ntangent);
	if(tempV.z < 0.0) phi = -phi;
	
	
	// handle Fresnel factor
	float n_t = 2.5;
	float R0 = pow( (n_t - 1.0) / (n_t + 1.0) , 2); 
	float tmp =  ((dot(-_k1, camNormal)));
	float alpha = acos(tmp);
	float teta = PI - alpha;
	float F = (R0 + (1.0 - R0) * pow(1.0 - cos(teta), 5));

	

	// handle G factor
	float dominator = pow( 1.0 - dot(_k1,_k2) , 2);
	
	float leftNom = dot(-_k1, camNormal.xyz);
	leftNom = (leftNom > tol)? tol : ((leftNom < -tol) ? -tol :  leftNom);
	
	float rightNom = dot(_k2, camNormal);
	rightNom = (rightNom > tol)? tol : ((rightNom < -tol) ? -tol :  rightNom);
	
	float nominator = leftNom*rightNom;
	float G = dominator / nominator;
	
	
	//G = abs(G);
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
	
	
	
	float smallest_k = 2.0 * PI / (390*pow(10,-9));
	float limitBound = (0.5*omega)/(100.0*abs(u));
	
	
	

	for(int iter = 0; iter < MaxIter; iter++){
		reHeight = 0.0;
		imHeight = 0.0;
		k = kValues[iter]; 
		vec2 modUV = getRotation(u,v,-phi);
		vec2 coords = vec2((k*modUV.x/omega) + 0.5, (k*modUV.y/omega) + 0.5);
		//coords = vec2((k*modUV.x/omega) + 0.5, 0.5); // NICE
		
		mayRun = true;
		// only allow values within range [0,1]
		
		float eps = 1.0 / 200.0;
		
//		if(coords.x < 0.0 || coords.y < 0.0 || coords.x > 1.0  || coords.y > 1.0) {
//			real_part = 0.0;
//			imag_part = 0.0;
//			mayRun = false;
//		}

		
		bool isRunning = true;
		int runCount = 0;
		
		
	
		mayRun = false;
		brdf = vec4(0,0,0,1);
		if(abs(u) > 0 && limitBound < smallest_k) brdf = vec4(1,1,1,1);
		
//		if(k >= limitBound) mayRun = false;
		
		// only contribute iff coords have components in range [0,1]
		if(mayRun){
			while(isRunning){
				float lowerWBound = -2.0 + wStep*runCount - hwStep;
				float upperWBound = -2.0 + wStep*runCount + hwStep;
				
				if( (w >= lowerWBound ) && (w < upperWBound ) ){
					int index1 = totRuns*2*iter + runCount;
					int index2 = totRuns*iter + runCount + 41;
					int index3 = totRuns*iter + runCount;

					reHeight = texture2DArray(TexArray, vec3(coords, index1) ).x;
					imHeight = texture2DArray(TexArray, vec3(coords, index2) ).x;
					
					scales = getC(reHeight, imHeight, index3);
					real_part = scales.x;
					imag_part = scales.y;
					isRunning = false;
				}

				runCount++;
				if(runCount == 41) isRunning = false; // bruteforce break;
			}
			
			float abs_P_Sq = pow((real_part*real_part + imag_part*imag_part),1);
			factor1 = getFactor(k, F, G, PI, w);

			//if(abs_P_Sq > 1.0) factor1 = 0.0;
			vec4 waveColor = vec4(brdf_weights[iter],1);
			brdf += vec4(factor1 * abs_P_Sq * brdf_weights[iter], 1);
//			brdf += vec4(factor1 * abs_P_Sq * LValues[iter]);
			sum += waveColor.xyz;
			
		}
		
		
		
	}
	
//	float amount = 100000000;
//	amount *= amount;
//	brdf += vec4(amount,amount,amount,0);
	//vec3 getGammaCorrection(vec3 rgb, float t, float f, float s, float gamma){
//	brdf.xyz =  M_Adobe_XR*brdf.xyz;
	sum = M_Adobe_XR*sum;
//	brdf.xyz = getGammaCorrection(brdf.xyz, 1, 0, 1, 1.0 / 2.2);
//	brdf.xyz = getGammaCorrection(brdf.xyz, 0.0031308, 0.055, 12.92, 1.0 / 2.4);
	
	// final set up
	float frac = 1.0 / 16.0;
	

	
	float fac2 = 1.0 / 70000.0;
	fac2 = 1.0 / 7500.0;
	fac2 = 1.0 / 3000000.0;
	fac2 = 1.0 / 15.0;
	fac2 = 1.0 / 2500.0;


	fac2 = 1.0 / 15.0;
	fac2 = 1.0 / 600.0;
	fac2 = 1.0 / 11500.0;
//	fac2 = 1.0 / 30.0;
	fac2 = 1.0 / 24.0;
	fac2 = 1.0 / 1.0;

	float ambient = 0.1;
	
	
	// test for error - debug mode
	if(brdf.x < 0.0 || brdf.y < 0.0 || brdf.z < 0.0){
		col = vec4(1,0,0,0);
	}else{
//		col = fac2*fac2*fac2*fac2*frac*brdf+vec4(ambient,ambient,ambient,1);
		col = brdf+vec4(ambient,ambient,ambient,1);
		//col = vec4(0.1,0.1,0.1,1);
	}
	
	
	float fff = 1.0 / 20;
	//col = vec4(FG*fff,FG*fff,FG*fff,1);
	
	frag_texcoord = texcoord;
	gl_Position = projection * modelview * position;
}
