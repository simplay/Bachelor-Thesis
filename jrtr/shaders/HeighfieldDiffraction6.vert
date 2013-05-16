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
	
	// TODO remove that hard coded constant and load them dynamically.
//	float a = -2.463288986185481;
//	float b = 10001.57518895576;
//	float c = -37.68516056771414;
//	float d = 72.95372615635891;
	
	
	
	
	
	
	float a = -1210.456479070569;
	float b = 10003.66987350477;
	float c = -799.9877742966562;
	float d = 1571.834696405178;
	
	
	
	
	

	
		
	reC = (reC-a)/b;
	imC = (imC-c)/d;
	
	return vec2(reC, imC); 

}

// do some kind of normalization of returned value
// divide by maximal amount
float getFactor(float k, float F, float G, float PI, float w, float omega){
	float A = 0.05;
	A = A*A;
	return (k*k*F*F*G*0.000000000000011)/(4*PI*PI*w*w*A);
}

void main() {
	
	// our constants
//	vec4 L400 = vec4(0.286, 0.0, 0.647, 1.0);
//	vec4 L450 = vec4(0.0, 0.199, 1.0, 1.0);
//	vec4 L500 = vec4(0.0, 1.0, 0.498, 1.0);
//	vec4 L550 = vec4(0.568, 1.0, 0.0, 1.0);
//	vec4 L600 = vec4(1.0, 0.690, 0.0, 1.0);
//	vec4 L650 = vec4(1.0, 0.0, 0.0, 1.0);
	
	

	
	
	vec4 LValues[16] = vec4[](
			vec4(0.19000003, 0.0, 0.99, 1.0),
			vec4(0.48155573, 0.0, 0.56333315, 1.0),
			vec4(0.08022216, 0.0, 1.0, 1.0),
			vec4(0.0, 0.07999998, 1.0, 1.0),
			vec4(0.0, 0.5066669, 1.0, 1.0),
			vec4(0.0, 0.93333375, 1.0, 1.0),
			vec4(0.0, 1.0, 0.1, 1.0),
			vec4(0.27619106, 1.0, 0.0, 1.0),
			vec4(0.58095264, 1.0, 0.0, 1.0),
			vec4(0.8857143, 1.0, 0.0, 1.0),
			vec4(1.0, 0.7777771, 0.0, 1.0),
			vec4(1.0, 0.42222187, 0.0, 1.0),
			vec4(1.0, 0.06666667, 0.0, 1.0),
			vec4(1.0, 0.0, 0.0, 1.0),
			vec4(1.0, 0.0, 0.0, 1.0),
			vec4(1.0, 0.0, 0.0, 1.0)
	);
	
//	vec4 LValues[6] = vec4[](L400,L450,L500,L550,L600,L650);
	
	float PI = 3.14159265358979323846264;
	float omega = 8.0*PI*pow(10,7);
	
	float A = omega*omega;
	float wStep = 0.1;
	float hwStep = 0.05;
	int MaxIter = 16;
	int totRuns = 41;
	float kValues[16] = float[](
								2.0*PI/0.00000038, 
								2.0*PI/0.00000040133335, 
								2.0*PI/0.00000042266665, 
								2.0*PI/0.0000004440, 
								2.0*PI/0.00000046533335,
								2.0*PI/0.0000004866667,
								2.0*PI/0.00000050800,
								2.0*PI/0.0000005293334,
								2.0*PI/0.0000005506667,
								2.0*PI/0.00000057200,
								2.0*PI/0.0000005933334,
								2.0*PI/0.0000006146667,
								2.0*PI/0.00000063600,
								2.0*PI/0.0000006573334,
								2.0*PI/0.0000006786667,
								2.0*PI/0.00000070000
	);
	
	
	// we wont use something like that here mathematically
	// just in case we are going to try to look the same like 
	// stam's shader
	vec4 hiliteColor = vec4(radianceArray[0],0);
	
	// check this sign again since + seems to produce way better results...
	// really check this...
	// also try to introduce the tangent apporach.
	vec4 eyePosition = -(modelview * position);  
	vec4 lightPosition = modelview*vec4(directionArray[0].xyz,0);
	vec3 P = (modelview * position).xyz;
	vec3 _k1 = normalize(P - lightPosition.xyz);
	vec3 _k2 = normalize(eyePosition.xyz - P);
	
	// components of V are in between the range [-2,2]
	// since _k1 and _k2 are normalized vectors.
	vec3 V = _k1 - _k2;
	
	float u = V.x;
	float v = V.y;
	float w = V.z;

	float R0 = 0.5; 
	float F = R0 + (1.0-R0) * pow((1.0 - dot(normalize(V), normalize(normal))),6);
	float div = ( dot(-_k1, normal)*dot(_k2, normal) );
	if(div == 0) div = 1.0;
	float G = (1.0-dot(_k1, _k2) )*(1.0-dot(_k1, _k2) ) / div; 

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
		k = kValues[iter];;
		vec2 coords = vec2(k*u/omega, k*v/omega);
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
		
		float abs_P_Sq = real_part*real_part + imag_part*imag_part;
		factor1 = getFactor(k, F, G, PI, w, omega);
		brdf += vec4(factor1*abs_P_Sq*LValues[iter].xyz,1);
	}
	
	
	// final set up
	float frac = 1.0/16.0;
	float fac2 = 1.0 / 23.0;
	float ambient = 0.0;
	

	col = fac2*fac2*fac2*frac*brdf+vec4(ambient,ambient,ambient,1);
	frag_texcoord = texcoord;
	gl_Position = projection * modelview * position;
}
