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

const mat3 M_test = mat3(
0.4185,   -0.1587,   -0.0828,
-0.0912,    0.2524,    0.0157,
0.0009,   -0.0025,    0.1786);



// gamma correction
vec3 getGammaCorrection(vec3 rgb, float t, float f, float s, float gamma){
	
	float q = (1+f);
	return vec3(q*pow(rgb.x,gamma)-f, q*pow(rgb.y,gamma)-f, q*pow(rgb.z,gamma)-f );
}

// see derivations
float get_p_factor(float w_i, float T_i, float N_i){
	//(0.5 + (cos(N*theta) - cos(theta*(N+1)))./(2*(1 - cos(theta)))) ./ (N+1);
	float eps = 0.00000001; 
	float tmp = cos(w_i*T_i*N_i)-cos(w_i*T_i*(N_i + 1));
	tmp /= (1.0 - cos(w_i*T_i));
	tmp = 0.5 + 0.5*(tmp);
	tmp = ( abs(T_i*N_i) < eps ) ? tmp : (N_i + 1);
	return tmp;
}


// is this correct
float get_q_factor(float w_i, float T_i, float N_i){
	float eps = 0.00000001; 
	float tmp = sin(w_i*T_i*(N_i+1))*sin(w_i*T_i*N_i)*sin(w_i*T_i);
	tmp /= 2.0*(1.0 - cos(w_i*T_i)); 
	tmp = ( abs(T_i*N_i) < eps ) ? tmp : (N_i + 1);
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
	return (k*k*F*F*G)/(4.0*PI*PI*w*w*A);
}

void main() {

	float a = global_extrema[0].x;
	float b = global_extrema[0].y;
	float c = global_extrema[0].z;
	float d = global_extrema[0].w;
	
	
	float brdfMax = pow((b-a),2)+pow((d-c),2);

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
	

	
	
	
	
	float omega = 8.0*PI*pow(10,7); // omega scaled for texture cordinates => corresponds to 2.5
	omega = (30.0/100.0)*8.0*PI*pow(10,7);
	
	float N_1 = 30.0;
	float N_2 = 100.0;
	
	float t_0 = (2.5*pow(10.0,-6.0)) / 30.0;
	float T_1 = t_0 * N_1;
	float T_2 = t_0 * N_2;
	
	float periods = 40.0;
	float N = periods - 1.0;
	float M = 100.0; // #samples
	 
	
	
	vec4 brdf2 = vec4(0);
	
	//omega = (8.0/1.0)*PI*pow(10,7);
	
	float wStep = 0.1;
	float hwStep = 0.05;
	int MaxIter = 16;
	int totRuns = 41;
	
	// we wont use something like that here mathematically
	// just in case we are going to try to look the same like 
	// stam's shader
	vec4 hiliteColor = vec4(radianceArray[0],1);
	
	// set up direction vectors
	
//	vec3 cameraPos = vec3(0,0,1);
//	
//	
//	vec4 eye = (modelview * position); // point in camera space
//	vec4 lightPosition = modelview*vec4(directionArray[0].xyz, 1); // light position in camera space
//	vec3 P = (modelview * position).xyz; // point p under consideration
//	vec3 _k1 = normalize(P - lightPosition.xyz); // _k1: vector from lightPos to point P
//	vec3 _k2 = normalize(cameraPos - P); // _k2: vector from point P to camera
	
//	
//	vec3 L = normalize( (eye-lightPosition ).xyz );
	
//	vec3 _k1 = normalize(  -L ); // _k1: vector from lightPos to point P
//	//vec3 _k1 = normalize(eye.xyz- lightPosition.xyz); // _k1: vector from lightPos to point P
//	vec3 _k2 = normalize(-eye.xyz); // _k2: vector from point P to camera
	
	
//	vec4 aa = (- modelview*position);
//	vec4 aa = (-modelview*camPos + modelview*position);
//	vec3 _k2 = normalize(aa.xyz);
//	
//	vec4 bb = (modelview*position - modelview*vec4(directionArray[0].xyz, 1) );
//	vec3 _k1 = normalize(bb.xyz);
//	
	// components of V are in between the range [-2,2]
	// since _k1 and _k2 are normalized vectors.
	
	
//	vec3 binormal = cross(normal, tangent);
//	vec4 eyePosition = -(modelview * position);
//	vec3 P = (modelview * position).xyz;
//	vec3 lightPosition = directionArray[0].xyz;
//	vec3 V = normalize(lightPosition - P);
//	vec3 L = normalize(eyePosition.xyz - P);
//	vec3 H = (L + V); // Halfway vector
//	vec3 N = (modelview * vec4(normal,0)).xyz;
//	vec3 T = (modelview * vec4(tangent,0)).xyz; // receiver
//	vec3 B = (modelview * vec4(binormal,0)).xyz;
//
//	
//	float u = dot(T, H);
//	float v = dot(B, H);
//	float w = dot(N, H); // component of the halfway vector in the normal direction.
//
//	
//	vec3 _k2 = normalize(-eyePosition.xyz);
//	vec3 _k1 = normalize(-eyePosition.xyz);
	
//	
//	float u = V.x;
//	float v = V.y;
//	float w = V.z;
	
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
//		k = k/ (2.0*PI);
		vec2 coords = vec2((k*modUV.x/omega) + bias, (k*modUV.y/(omega)) + bias); //2d
//		coords = vec2((k*modUV.x/omega) + bias, bias); //1d
		
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
		
		
		float w_u = 2*PI*k*u;
		float w_v = 2*PI*k*v;
		
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
					
					
					float p1_fac = get_p_factor(w_u, T_1, N_1);
					float p2_fac = get_p_factor(w_v, T_2, N_2);
					
					float q1_fac = get_q_factor(w_u, T_1, N_1);
					float q2_fac = get_q_factor(w_v, T_2, N_2);
					
					
					real_part = scales.x*p1_fac*p2_fac;
					imag_part = scales.y*q1_fac*q2_fac;
					isRunning = false;
				}

				runCount++;
				if(runCount == 41) isRunning = false; // bruteforce break;
			}
			

			
			float abs_P_Sq = pow((real_part*real_part + imag_part*imag_part),1);
			factor1 = getFactor(k, F, G, PI, w);

			//if(abs_P_Sq > 1.0) factor1 = 0.0;
			vec4 waveColor = vec4(brdf_weights[iter],1);
			
			
			float tmp = k*v*1.25*pow(10.0,-7.0);
//			tmp = k*v*0.01;
//			tmp = k*v*0.0001;
			tmp /= 2.0*PI;
			
			if(abs(tmp) < pow(10.0,-8.0)){
				tmp = 1.0;
			}else{
				tmp = sin(tmp)/tmp;
			}
			tmp = 1.0;
			tmp *= tmp;
			
			

			
			brdf += vec4(tmp*factor1 * abs_P_Sq * brdf_weights[iter], 1);
			brdf2 += vec4(tmp*factor1 * brdfMax * brdf_weights[iter], 1);
			
			
			
			//brdf += vec4(factor1 * abs_P_Sq * LValues[iter]);
			sum += waveColor.xyz;
			
		}
	}
	
//	brdf = vec4(brdf.x/brdf2.x, brdf.y/brdf2.y, brdf.z/brdf2.z, 1) ;
	
	float frac = 1.0 / 32.0;
	float fac2 = 1.0 / 70000.0;

	fac2 = 3.0;
	fac2 = 1.0 / 35.0;
	fac2 = 1.0 / 220.0;

	
	brdf.xyz =  M_Adobe_XR*brdf.xyz;
	brdf.xyz = getGammaCorrection(brdf.xyz, 1.0, 0, 1.0, 1.0 / 2.2);
	brdf.xyz = fac2*fac2*fac2*fac2*frac*brdf.xyz;
	
//	brdf.xyz = getGammaCorrection(brdf.xyz, 0.0031308, 0.055, 12.92, 1.0 / 2.2);
	
	// final set up

	

	

//	fac2 = 1.0 / 8000.0;
	float ambient = 0.0;
	

	// test for error - debug mode
	if(brdf.x < 0.0 || brdf.y < 0.0 || brdf.z < 0.0){
		col = vec4(0,1,0,1);
	}else{
		col = brdf+vec4(ambient,ambient,ambient,1);
	//		col = fac2*fac2*fac2*fac2*frac*vec4(G,G,G,1);
		//col = vec4(ambient,ambient,ambient,1);
	}
	
	
	float fff = 1.0 / 20;
	//col = vec4(FG*fff,FG*fff,FG*fff,1);
	
	frag_texcoord = texcoord;
	gl_Position = projection * modelview * position;
}
