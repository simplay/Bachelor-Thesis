#version 150
#extension GL_EXT_gpu_shader4 : enable

//substitutes
#define MAX_LIGHTS 1
#define MAX_FACTORS 31
#define MAX_WEIGHTS 311
//Uniform variables, passed in from host program via suitable 

uniform sampler2DArray TexArray;
uniform vec4 cop_w;
uniform vec3 radianceArray[MAX_LIGHTS];
uniform vec3 brdf_weights[MAX_WEIGHTS];
uniform vec4 directionArray[MAX_LIGHTS];
uniform vec4 scalingFactors[31];
uniform vec4 global_extrema[1];
uniform vec4 camPos;

uniform float LMIN;
uniform float LMAX;
uniform float approxSteps;
uniform float dimN;
uniform float dimSmall; // not used right now
uniform float dimDiff; // not used right now
uniform float repNN; // not used right now
uniform int periodCount;
uniform float maxBumpHeight;
uniform float patchSpacing;
uniform sampler2DArray lookupText;
// Variables passed in from the vertex shader
in vec2 frag_texcoord;
in vec4 light_direction[MAX_LIGHTS];
in vec4 normal_out;
in vec4 eyeVector;
//in vec4 col;
in vec3 o_pos;
in vec3 o_light;
in vec3 o_normal;
in vec3 o_tangent;

// Output variable, will be written to framebuffer automatically
out vec4 frag_shaded;

//material and math constants
const float PI = 3.14159265358979323846264;
const float CERATIN = 1.6;
const float SMOOTH = 2.5;

//wave constants
float l_min = LMIN;
float l_max = LMAX;
float lambda_min = l_min*pow(10.0, -9.0);
float lambda_max = l_max*pow(10.0, -9.0);
const float rescale = pow(10.0, 9.0);
float dx = patchSpacing;// 2.5*pow(10.0, -6.0); // -6 // distance between two patches (from center to center), make me parametric too
float s = maxBumpHeight;// 2.4623*pow(10,-7.0); // -7 // max height of a bump, make me parametric

//error constants
const float eps_pq = 1.0*pow(10.0, -5.0); 
const float eps = 1.0*pow(10.0, -4.0);
const float tolerance = 0.999999; 

//period constants
float N_1 = dimN; // number of pixels of downsized patch 
float N_2 = dimN; // number of pixels padded patch - see matlab
float t_0 = dx / N_1;
float T_1 = t_0 * N_1;
float T_2 = t_0 * N_1;
float periods = periodCount-1.0; // 26 // number of patch periods along surface
float Omega = ((N_1/N_2)*2.0*PI)/t_0; // (N_1/N_2)*2*PI/t_0, before 8.0*PI*pow(10.0,7.0);
float bias = (N_2/2.0)/(N_2-1.0); // old: 50.0/99.0;

//transformation constant
const mat3 M_Adobe_XR = mat3(
		2.0414, -0.5649, -0.3447,
		-0.9693,  1.8760,  0.0416,
		 0.0134, -0.01184,  1.0154
);	


const mat3 CIE_XYZ = mat3(
0.418465712421894,	-0.158660784803799,	-0.0828349276180955,
-0.0911689639090227,	0.252431442139465,	0.0157075217695576,
0.000920898625343664,	-0.00254981254686328,	0.178598913921520);


const float[16] fixed_lambdas = float[](
	390.0000,  410.6667,  431.3333,  452.0000,  
	472.6667,  493.3333,  514.0000,  534.6667,  
	555.3333,  576.0000,  596.6667,  617.3333,  
	638.0000,  658.6667,  679.3333,  700.0000
);

const mat3 CIE_RGB = mat3(
2.3706743, -0.9000405, -0.4706338,
-0.5138850,  1.4253036,  0.0885814,
0.0052982, -0.0146949,  1.009396);



//FUNCTIONS

//gamma correction
vec3 getGammaCorrection(vec3 rgb, float t, float f, float s, float gamma){
	float q = (1.0+f);
	return q*pow(rgb, vec3(gamma))-f;
}


//see derivations: T_i*w_i is a multiple of 2*PI
float get_p_factor(float w_i, float T_i, float N_i){
	float tmp = 1.0;
	if (abs(1.0-cos(T_i*w_i)) < eps_pq){
		tmp = N_i;
	}else{
		tmp = cos(w_i*T_i*N_i)-cos(w_i*T_i*(N_i + 1.0));
		tmp /= (1.0 - cos(w_i*T_i));
		tmp = 0.5 + 0.5*(tmp);
	}
	return tmp/N_i;
}


//is this correct: T_i*w_i is a multiple of 2*PI
float get_q_factor(float w_i, float T_i, float N_i){
	float tmp = N_i;
	if (abs(1.0-cos(T_i*w_i)) < eps_pq){
		tmp = 0.0;
	}else{
		tmp = sin(w_i*T_i*(N_i+1.0))-sin(w_i*T_i*N_i)-sin(w_i*T_i);
		tmp /= 2.0*(1.0 - cos(w_i*T_i));
	}
	return tmp/N_i;
}


//use tangent in oder to consider the vector field.
vec2 getRotation(float u, float v, float phi){
	float uu = u*cos(phi) - v*sin(phi);
	float vv = u*sin(phi) + v*cos(phi);
	return vec2(uu, vv);
}


//returns correct scaling factor
vec2 getRescaledHeight(float reHeight, float imHeight, int index){
	vec4 v = scalingFactors[index];
	float reMin = v.x; float reMax = v.y;
	float imMin = v.z; float imMax = v.w;
	
	float reC = reMin + reHeight*(reMax);
	float imC = imMin + imHeight*(imMax);
	
	return vec2(reC, imC); 
}


//do some kind of normalization of returned value
//divide by maximal amount
float getFactor(float k, float F, float G, float w){
	
	// area of CD with d=30cm
	float d = 0.3;
	float A = 1.0;
	
	float kk_ww = (k*k)/(w*w);
	return kk_ww*(F*F*G)/(4.0*PI*PI*A);
}


float getAbsFressnelFactor(vec3 _k1, vec3 _k2){
	float n_t = SMOOTH; // material constant
	float R0 = pow( (n_t - 1.0) / (n_t + 1.0) , 2.0); 
	vec3 L = _k2; 
	vec3 V = -_k1;
	vec3 H = normalize(L + V);
	float cos_teta = dot(H,V);
	cos_teta = (cos_teta > tolerance)? tolerance : ((cos_teta < 0.0) ? 0.0 :  cos_teta);
	float ret_value = (R0 + (1.0 - R0) * pow(1.0 - cos_teta, 5.0));
	ret_value = abs(ret_value);
	if(ret_value < 1.0*pow(10.0, -18.0)) ret_value = 0.0;
	return ret_value;

	// faster than above - see GLSL specs
//	return mix(R0, 1.0, pow(1.0 - cos_teta, 5.0));
}


float computeRotationAngle(vec3 tangent){
	vec3 ntangent = normalize(tangent);
	float dTemp = dot(ntangent,vec3(1.0,0.0,0.0) );
	dTemp = (dTemp > tolerance)? tolerance : ((dTemp < -tolerance) ? -tolerance :  dTemp);
	float phi = acos(dTemp);
	vec3 tempV = cross(vec3(1.0,0,0), ntangent);
	if(tempV.z < 0.0) phi = -phi;
	return 0.0;
}


float computeGFactor(vec3 camNormal, vec3 _k1, vec3 _k2){
	float dominator = pow(1.0 - dot(_k1,_k2), 2.0);
	float leftNom = dot(-_k1, camNormal);
	leftNom = (leftNom > tolerance)? tolerance : ((leftNom < -tolerance) ? -tolerance :  leftNom);
	float rightNom = dot(_k2, camNormal);
	rightNom = (rightNom > tolerance)? tolerance : ((rightNom < -tolerance) ? -tolerance :  rightNom);
	float nominator = leftNom*rightNom;
	return (dominator / nominator);
}

//assuming we have weigths given foreach lambda in [380nm,700nm] with delta 1nm steps.
vec3 avgWeighted_XYZ_weight(float lambda){
	
	float lambda_a = floor(lambda*rescale); // lower bound current lambda
	float lambda_b = ceil(lambda*rescale); // upper bound current lambda
	
	// convex combination of a,b gives us the nearest weight for current:
	// (L_b-L)f(L_b) + (L-L_a)f(L_a)
	
	// find index by wavelength
	int index_a = int(lambda_a - l_min);
	int index_b = int(lambda_b - l_min);
	float weight_b = (lambda_b - lambda*rescale);
	float weight_a = (lambda*rescale - lambda_a);
	
	vec3 cie_XYZ_lambda_weight = weight_b*brdf_weights[index_b] + weight_a*brdf_weights[index_a];
	return cie_XYZ_lambda_weight;
}

float compute_pq_scale_factor(float w_u, float w_v){
	float p1 = get_p_factor(w_u, T_1, periods);
	float p2 = get_p_factor(w_v, T_2, periods);
	
	float q1 = get_q_factor(w_u, T_1, periods);
	float q2 = get_q_factor(w_v, T_2, periods);

	return pow(p1*p1 + q1*q1 , 0.5)*pow(p2*p2 + q2*q2 , 0.5);
}


//perform taylor approximation
vec2 taylorApproximation(vec2 coords, float k, float w){
	vec2 precomputedFourier = vec2(0.0, 0.0);
	int lower = 0; int upper = int(approxSteps)+1;
	float reHeight = 0.0; float imHeight = 0.0;
	float real_part = 0.0; float imag_part = 0.0;
	float fourier_coefficients = 1.0;
	
	
	// approximation till iteration 30 of fourier coefficient
	for(int n = lower; n <= upper; n++){
		
//		int index_re = n;
//		int index_im = (n + upper);
//		vec4 tex = texture2DArray(TexArray, vec3(frag_texcoord, 10));
		
		
		reHeight = texture2DArray(TexArray, vec3(coords, n) ).x;
		imHeight = texture2DArray(TexArray, vec3(coords, n) ).y;
		int extremaIndex = n;
		
		precomputedFourier = getRescaledHeight(reHeight, imHeight, extremaIndex);

		// develope factorial and pow like this since 
		// otherwise we could get numerical rounding errors.
		// PRODUCT_n=0^N { pow(k*w*s,n)/n! }
		
		if(n == 0) fourier_coefficients = 1.0;
		else fourier_coefficients *= ((k*w*s)/n);
		
		float fourier_re = fourier_coefficients*precomputedFourier.x;
		float fourier_im = fourier_coefficients*precomputedFourier.y;
		
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
	
	return vec2(real_part, imag_part);
}


//first component N_min, second compontent N_max
vec2 compute_N_min_max(float t){
	// default case if t == 0 otherwise override it.
	float N_min = 0.0;
	float N_max = 0.0;
	
	if(t > 0.0){
		N_min = ceil((dx*t) / lambda_max);
		N_max = floor((dx*t) / lambda_min);
	}else if(t < 0.0){
		N_min = ceil((dx*t) / lambda_min);
		N_max = floor((dx*t) / lambda_max);
	}
	return vec2(N_min, N_max);
}

void main() {
	
	vec4 brdf = vec4(0.0, 0.0, 0.0, 1.0);
	vec4 maxBRDF = vec4(0.0, 0.0, 0.0, 1.0);
	vec2 P = vec2(0.0, 0.0);
	
	float neighborRadius = 2.0;
	float abs_P_Sq = 0.0;
	float real_part = 0.0;
	float imag_part = 0.0;
	float reHeight = 0.0;
	float imHeight = 0.0;
	float factor1 = 0.0;
	float k = 0.0;
	float fourier_coefficients = 1.0;
	float a = global_extrema[0].x; float b = global_extrema[0].y;
	float c = global_extrema[0].z; float d = global_extrema[0].w;
	float brdfMax = pow((b-a),2.0)+pow((d-c),2.0);
	float lambda_iter = 0.0;
	float t1 = 0.0;
	float t2 = 0.0;
	float phi = -PI/2.0;
	phi = 0.0;
	vec4 o_col = vec4(0.0, 0.0, 0.0, 0.0);
	
	vec3 _k2 = normalize(o_pos); //vector from point P to camera
	vec3 _k1 = normalize(o_light); // light direction, same for every point		
	vec3 V = _k1 - _k2;
	float u = V.x; float v = V.y; float w = V.z;
	float F = getAbsFressnelFactor(_k1, _k2); // issue G may cause nan
	float G = computeGFactor(o_normal, _k1, _k2); // 

	// get iteration bounds for given (u,v)
	vec2 N_u = compute_N_min_max(u);
	vec2 N_v = compute_N_min_max(v);
	vec2 N_uv[2] = vec2[2](N_u, N_v);
	vec2 modUV = getRotation(u,v,phi);
	
	
	// only specular contribution within epsilon range: i.e. fixed number of lambdas
		if(abs(u) < eps && abs(v) < eps){
			for(int iter = 0; iter < 0; iter++){	
				brdf += vec4(1.0, 1.0, 1.0, 0.0);
			}
		}else{
			// iterate twice: once for N_u and once for N_v lower,upper
			for(int variant = 0; variant < 2; variant++){
				
				int lower = int(N_uv[variant].x);
				int upper = int(N_uv[variant].y);
				
				if(variant == 0){
					t1 = u;
					t2 = v;
				}
				else{
					t1 = v;
					t2 = u;
				}
				
				for(float iter = lower; iter <= upper; iter++){
					if(iter == 0.0) continue;
					lambda_iter = (dx*t1)/iter;
					k = 2.0*PI / lambda_iter;
					
					float uv_N_n_hat = (k*dx*t2)/(2.0*PI);
					float uv_N_n = floor(uv_N_n_hat);
					float uu_N_n = (k*dx*t1)/(2.0*PI);
					
					float uu_N_base = uu_N_n - neighborRadius;
					float uv_N_base = uv_N_n - neighborRadius;
					
					for(float ind1 = uu_N_base; ind1 <= uu_N_base + 2.0*neighborRadius; ind1 = ind1 + 1.0){
						for(float ind2 = uv_N_base; ind2 <= uv_N_base + 2.0*neighborRadius; ind2 = ind2 + 1.0){
							float dist = pow(ind1-uu_N_base, 2.0) + pow(ind2-uv_N_base, 2.0);
							vec2 coords = vec2( (k*ind1/Omega) + bias, (k*ind2/Omega) + bias); //2d

							if(coords.x < 0.0 || coords.x > 1.0 || coords.y < 0.0 || coords.y > 1.0) continue;
							
							float w_u = k*ind1;
							float w_v = k*ind2;
					
							P = taylorApproximation(coords, k, w);
							float w_ij = exp(0.0);
//							float pq_scale = compute_pq_scale_factor(w_u,w_v);
//							P *= pq_scale;
							
							P *= w_ij;
							
							float abs_P_Sq = P.x*P.x + P.y*P.y;
							
							float diffractionCoeff = getFactor(k, F, G, w);
							vec3 waveColor = avgWeighted_XYZ_weight(lambda_iter);
							brdf += vec4(diffractionCoeff * abs_P_Sq * waveColor, 0.0);
							
							maxBRDF += vec4(diffractionCoeff * 1.0 * waveColor, 0.0);
							
							
						}
					}
					
					

				}
			}
		}

//		
//		if(maxBRDF.x <= 0.0) maxBRDF.x = 1.0;
//		brdf = vec4(brdf.x/maxBRDF.y, brdf.y/maxBRDF.y, brdf.z/maxBRDF.y, 1.0) ; //  relative scaling
		float fac2 = 1.0 / 42.0;
		fac2 = 1.0 / 6000.0;
		brdf.xyz = M_Adobe_XR*brdf.xyz;
		
		brdf.xyz = fac2*fac2*fac2*fac2*brdf.xyz;
		
		float ambient = 0.1;
		
		// remove negative values
		if(brdf.x < 0.0 ) brdf.x = 0.0;
		if(brdf.y < 0.0 ) brdf.y = 0.0;
		if(brdf.z < 0.0 ) brdf.z = 0.0;
		brdf.w = 1.0;
		
//		if(brdf.x != 0.0 && brdf.y != 0.0 && brdf.z != 0.0)
		brdf.xyz = getGammaCorrection(brdf.xyz, 1.0, 0.0, 1.0, 1.0 / 2.2);
		
		if(isnan(brdf.x) ||isnan(brdf.y) ||isnan(brdf.z)) o_col = vec4(1.0, 0.0, 0.0, 1.0);
		else if(isinf(brdf.x) ||isinf(brdf.y) ||isinf(brdf.z)) o_col = vec4(0.0, 1.0, 0.0, 1.0);
		else o_col = brdf+vec4(ambient,ambient,ambient,0.0);
//		else o_col = vec4(ambient,ambient,ambient,0.0);
		frag_shaded	= o_col;
}
