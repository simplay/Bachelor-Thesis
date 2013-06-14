// Diffraction shader using a heightfield
// crude approximation, working with fourier transformations.
// which have been precalculated.
// Michael Single


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

uniform float distToCam; // ne entity

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
	
	float q = (1.0+f);
	return vec3(q*pow(rgb.x,gamma)-f, q*pow(rgb.y,gamma)-f, q*pow(rgb.z,gamma)-f );
}

// see derivations
float get_p_factor(float w_i, float T_i, float N_i){
	float eps = 0.0001; 
	float tmp = 1.0;
	float divVal =  T_i*w_i / (2.0*PI);
	float divRound = round(divVal);

	if (abs(divVal - divRound) < eps){
	// T_i*w_i is a multiple of 2*PI
		tmp = (N_i + 1);
	}else{
	//otherwise
		tmp = cos(w_i*T_i*N_i)-cos(w_i*T_i*(N_i + 1.0));
		tmp /= (1.0 - cos(w_i*T_i));
		tmp = 0.5 + 0.5*(tmp);
	}

	return tmp;
}


// is this correct
float get_q_factor(float w_i, float T_i, float N_i){
	float eps = 0.0001; 
	float tmp = 1.0;	
	float divVal =  T_i*w_i / (2.0*PI);
	float divRound = round(divVal);

	if (abs(divVal - divRound) < eps){
	// T_i*w_i is a multiple of 2*PI
		tmp = 0.0;
	}else{
	//otherwise
		tmp = sin(w_i*T_i*(N_i+1.0))-sin(w_i*T_i*N_i)-sin(w_i*T_i);
		tmp /= 2.0*(1.0 - cos(w_i*T_i));
	}
	
//	return tmp/(N_i+1);
	return tmp;
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
float getFactor(float k, float F, float G, float PI, float w){
	
	// area of CD with d=30cm
	float d = 0.3;
	float A = pow(0.5*d, 2.0)*PI;
	return (k*k*F*F*G)/(4.0*PI*PI*w*w*A);
}

void main() {

	float a = global_extrema[0].x;
	float b = global_extrema[0].y;
	float c = global_extrema[0].z;
	float d = global_extrema[0].w;
	float brdfMax = pow((b-a),2)+pow((d-c),2);
	
	float omega = 8.0*PI*pow(10,7); // omega scaled for texture cordinates => corresponds to 2.5
	omega = (30.0/100.0)*8.0*PI*pow(10,7);
	
	float N_1 = 30.0;
	float N_2 = 100.0;
	
	float t_0 = (2.5*pow(10.0,-6.0)) / N_1;
	float T_1 = t_0 * N_1;
	float T_2 = t_0 * N_1;
	
	float periods = 1.0-1.0;
	float N = periods - 1.0;
	float M = 100.0; // #samples
	 
	vec4 brdf2 = vec4(0);
	float wStep = 0.1;
	float hwStep = 0.05;
	int MaxIter = 16;
	int totRuns = 41;
	
	
	// dir light source
	vec3 P = (modelview * position).xyz;
	vec4 lightPosition = modelview*(directionArray[0]);
//	
	vec3 _k2 = normalize(-P);
	vec3 _k1 = normalize(lightPosition.xyz);

	
	// point light source
//	vec4 eyePosition = -(modelview * position); // point in camera space
//	vec4 lightPosition = modelview*vec4(directionArray[0].xyz, 1); // light position in camera space
//	vec3 P = (modelview * position).xyz; // point p under consideration
//	vec3 _k1 = normalize(P - lightPosition.xyz); // _k1: vector from lightPos to point P
//	vec3 _k2 = normalize(eyePosition.xyz - P); // _k2: vector from point P to camera
	
	
	vec3 V = _k1 - _k2;
	
	float u = V.x;
	float v = V.y;
	float w = V.z;
	
	// normal and tangent vector in camera coordinates
	vec3 camNormal = normalize((modelview*vec4(normal,0.0)).xyz);
	vec3 camTangent = normalize((modelview*vec4(tangent,0.0)).xyz);
	
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
	float dominator = pow( 1.0 - dot(_k1,_k2) , 2.0);
	
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
	
	for(int iter = 0; iter < MaxIter; iter++){
		reHeight = 0.0;
		imHeight = 0.0;
		k = kValues[iter];
		vec2 modUV = getRotation(u,v,-phi);
		float bias = 50.0/99.0;
		vec2 coords = vec2((k*modUV.x/omega) + bias, (k*modUV.y/(omega)) + bias); //2d

		
		mayRun = true;
		// only allow values within range [0,1]
		
		float eps = 1.0 / 200.0;
		
		if(coords.x < 0.0 || coords.y < 0.0 || coords.x > 1.0  || coords.y > 1.0) {
			real_part = 0.0;
			imag_part = 0.0;
			mayRun = false;
		}


		bool isRunning = true;
		int runCount = 0;
		
		
		float w_u = k*u;
		float w_v = k*v;
		
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
					
					
					float p1 = get_p_factor(w_u, T_1, periods);
					float p2 = get_p_factor(w_v, T_2, periods);
					
					float q1 = get_q_factor(w_u, T_1, periods);
					float q2 = get_q_factor(w_v, T_2, periods);
					
					
					float res_scale = pow(p1*p1 + q1*q1 , 0.5)*pow(p2*p2 + q2*q2 , 0.5); 
					
					real_part = scales.x*res_scale;
					imag_part = scales.y*res_scale;
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
			brdf2 += vec4(factor1 * brdfMax * brdf_weights[iter], 1);
			sum += waveColor.xyz;
		}
	}
	
	brdf = vec4(brdf.x/brdf2.x, brdf.y/brdf2.y, brdf.z/brdf2.z, 1) ;
	
	float frac = 1.0 / 32.0;
	float fac2 = 1.0 / 70000.0;
	fac2 = 4.0 / 2.0; // T=40
//	fac2 = 2.0 / 1.0; // T=1
//	fac2 = 1.0 / 4.0; // T=400
//	fac2 = 1.0 / 150.0;
	
	brdf.xyz =  M_Adobe_XR*brdf.xyz;
	brdf.xyz = getGammaCorrection(brdf.xyz, 1.0, 0, 1.0, 1.0 / 2.2);
	brdf.xyz = fac2*fac2*fac2*fac2*frac*brdf.xyz;
	
	float ambient = 0.0;
	
	// test for error - debug mode
	if(brdf.x < 0.0 || brdf.y < 0.0 || brdf.z < 0.0){
		col = vec4(0,1,0,1);
	}else{
		col = brdf+vec4(ambient,ambient,ambient,1);
	}
	
	
//	float ff = sin(PI/2.0); we are in radians => opengl
	
	
	frag_texcoord = texcoord;
	gl_Position = projection * modelview * position;
}
