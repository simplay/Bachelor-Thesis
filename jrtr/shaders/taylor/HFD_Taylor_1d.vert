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
	vec4 brdf2 = vec4(0);
//	omega = 4.0*PI*pow(10,7);
	
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
	
	float s = 1.5*pow(10,-7);
	
	
	for(int iter = 0; iter < MaxIter; iter++){
		reHeight = 0.0;
		imHeight = 0.0;
		k = kValues[iter];
		vec2 modUV = getRotation(u,v,-phi);
		
		float bias = 50.0/99.0; // (100/2) / (100-1) = (sizeIMG/2) / (upper-lower)
		vec2 coords = vec2((k*modUV.x/omega) + bias, (k*modUV.y/(omega*1.0)) + bias); //2d
		coords = vec2((k*modUV.x/omega) + bias, bias); //1d
			
		mayRun = true;
		// only allow values within range [0,1]
		
		if(coords.x < 0.0 || coords.y < 0.0 || coords.x > 1.0  || coords.y > 1.0) {
			real_part = 0.0;
			imag_part = 0.0;
			mayRun = false;
		}

		float fourier_fact = 1.0;
		// only contribute iff coords have components in range [0,1]
		if(mayRun){
			
			
			
			
			// approximation till iteration 30 of fourier coefficient
			// TODO paramet. upper bound asap (but remember to limit) 
			for(int n = 0; n < 28; n++){
				
				
				float scaler = 3.2699430*pow(10,11); // see derivation
				scaler = 1.0; // see derivation
//				scaler = 1.0;
//				scaler = (1.0/scaler);
				
				int index_re = n;
				int index_im = (n + 31);
				
				reHeight = texture2DArray(TexArray, vec3(coords, index_re) ).x;
				imHeight = texture2DArray(TexArray, vec3(coords, index_im) ).x;
				int extremaIndex = n;
				
				scales = getC(reHeight, imHeight, extremaIndex);
				
//				k = k / (2*PI); // important
				
				
				// develope factorial and pow like this since 
				// otherwise we could get numerical rounding errors.
				// PRODUCT_n=0^N { pow(k*w*s,n)/n! }
				
				if(n == 0) fourier_fact = 1.0;
				else fourier_fact *= ((k*w*s)/n);
				
				float fourier_re = fourier_fact*scaler*scales.x;
				float fourier_im = fourier_fact*scaler*scales.y;
				
				// see derivations
				if(n % 4 == 0){
					real_part += fourier_re;
					imag_part += fourier_im;
					
				}else if(n % 4 == 1){
					real_part -= fourier_im;
					imag_part += fourier_re;
					
				}else if(n % 4 == 2){
					real_part -= fourier_re;
					imag_part -= fourier_im;
					
				}else{
					real_part += fourier_im;
					imag_part -= fourier_re;
				}
			}
			
//			k = k * 2*PI; // important
			
			float abs_P_Sq = pow((real_part*real_part + imag_part*imag_part),1);
			factor1 = getFactor(k, F, G, PI, w);
			
			vec4 waveColor = vec4(brdf_weights[iter],1);

			float tmp = k*v*1.25*pow(10.0,-7.0);
			tmp /= 2.0*PI;
			
			if(abs(tmp) < pow(10.0,-8.0)){
				tmp = 1.0;
			}else{
				tmp = sin(tmp)/tmp;
			}
//			tmp = 1.0;
			tmp *= tmp;

			
			brdf += vec4(tmp*factor1 * abs_P_Sq * LValues[iter]);
			brdf2 += vec4(tmp*factor1 * brdfMax * brdf_weights[iter], 1);
		}
	}
	//brdf = vec4(brdf.x/brdf2.x, brdf.y/brdf2.y, brdf.z/brdf2.z, 1) ; //  A
	float frac = 1.0 / 32.0;
	float fac2 = 100.0 / 70000.0;
	
	
	fac2 = 10.0 / 7.5; // wenn A und ohne global minmax
	fac2 = 1.0 / 600.0; // wenn nicht A und ohne gloabl minmax
//	fac2 = 7.0 / 1.0;

	
	
	brdf.xyz =  M_Adobe_XR*brdf.xyz;
	brdf.xyz = getGammaCorrection(brdf.xyz, 1.0, 0, 1.0, 1.0 / 2.2);
	brdf.xyz = fac2*fac2*fac2*fac2*frac*brdf.xyz;
	
//	brdf.xyz = getGammaCorrection(brdf.xyz, 0.0031308, 0.055, 12.92, 1.0 / 2.2);
	
	float ambient = 0.0;
	

	// test for error - debug mode
	if(brdf.x < 0.0 || brdf.y < 0.0 || brdf.z < 0.0){
		col = vec4(1,1,1,1);
	}else{
		col = brdf+vec4(ambient,ambient,ambient,1);
	//		col = fac2*fac2*fac2*fac2*frac*vec4(G,G,G,1);
		//col = vec4(ambient,ambient,ambient,1);
	}
	

	frag_texcoord = texcoord;
	gl_Position = projection * modelview * position;
}
