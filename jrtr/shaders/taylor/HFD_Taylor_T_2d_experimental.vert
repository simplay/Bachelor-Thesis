// Diffraction shader using a heightfield
// crude approximation, working with fourier transformations.
// which have been precalculated.
// Michael Single


//NB compute Omega dynamically!!!

#version 150
#extension GL_EXT_gpu_shader4 : enable

// substitutes
#define MAX_LIGHTS 1
#define MAX_FACTORS 656
#define MAX_Weight 16
#define MAX_Weight2 321

//Uniform variables, passed in from host program via suitable 
//variants of glUniform*
uniform mat4 projection;
uniform mat4 modelview;
uniform vec3 radianceArray[MAX_LIGHTS];
uniform vec3 brdf_weights[MAX_Weight2];
uniform vec4 directionArray[MAX_LIGHTS];
uniform vec4 scalingFactors[MAX_FACTORS];
uniform vec4 global_extrema[1];
uniform sampler2DArray TexArray;
uniform vec4 camPos;


in vec3 normal;
in vec4 position;
in vec2 texcoord;
in vec3 tangent;
in vec4 color;


//Output variables for fragment shader
out vec2 frag_texcoord;
out vec4 col;

// material and math constants
const float PI = 3.14159265358979323846264;
const float CERATIN = 1.6;
const float SMOOTH = 2.5;

// wave constants
const float lambda_min = 380.0*pow(10.0, -9.0);
const float lambda_max = 700.0*pow(10.0, -9.0);
const float dx = 2.5*pow(10.0, -6.0);

// error constants
const float eps_pq = 0.0001; 
const float eps = 0.0000001;
const float tolerance = 0.999999999;

// period constants
const float N_1 = 30.0; // input dyn
const float N_2 = 100.0; // input dyn
const float t_0 = (2.5*pow(10.0,-6.0)) / N_1;
const float T_1 = t_0 * N_1;
const float T_2 = t_0 * N_1;
const float periods = 260.0-1.0;
const float M = 100.0; // #samples //not used?

// transformation constant
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


//see derivations: T_i*w_i is a multiple of 2*PI
float get_p_factor(float w_i, float T_i, float N_i){
	float tmp = 1.0;
	if (abs(1.0-cos(T_i*w_i)) < eps_pq){
		tmp = (N_i + 1);
	}else{
		tmp = cos(w_i*T_i*N_i)-cos(w_i*T_i*(N_i + 1.0));
		tmp /= (1.0 - cos(w_i*T_i));
		tmp = 0.5 + 0.5*(tmp);
	}
	return tmp;
}


// is this correct: T_i*w_i is a multiple of 2*PI
float get_q_factor(float w_i, float T_i, float N_i){
	float tmp = 1.0;	
	if (abs(1.0-cos(T_i*w_i)) < eps_pq){
		tmp = 0.0;
	}else{
		tmp = sin(w_i*T_i*(N_i+1.0))-sin(w_i*T_i*N_i)-sin(w_i*T_i);
		tmp /= 2.0*(1.0 - cos(w_i*T_i));
	}
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
vec2 getRescaledHeight(float reHeight, float imHeight, int index){
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

float getFressnelFactor(vec3 _k1, vec3 camNormal){
	float n_t = SMOOTH; // material constant
	float R0 = pow( (n_t - 1.0) / (n_t + 1.0) , 2.0); 
	float angleCamNorm_k1 =  ((dot(-_k1, camNormal)));
	float alpha = acos(angleCamNorm_k1);
	float teta = PI - alpha;
	return (R0 + (1.0 - R0) * pow(1.0 - cos(teta), 5.0));
}

float computeRotationAngle(vec3 tangent){
	vec3 ntangent = normalize(tangent);
	float dTemp = dot(ntangent,vec3(1.0,0.0,0.0) );
 	dTemp = (dTemp > tolerance)? tolerance : ((dTemp < -tolerance) ? -tolerance :  dTemp);
	float phi = acos(dTemp);
	vec3 tempV = cross(vec3(1.0,0,0), ntangent);
	if(tempV.z < 0.0) phi = -phi;
	return phi;
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

// assuming we have weigths given foreach lambda in [380nm,700nm] with delta 1nm steps.
vec3 avgWeighted_XYZ_weight(float lambda){
	
	float lambda_a = floor(lambda); // lower bound current lambda
	float lambda_b = ceil(lambda); // upper bound current lambda
	
	// convex combination of a,b gives us the nearest weight for current:
	// (L_b-L)f(L_b) + (L-L_a)f(L_a)
	
	// find index by wavelength
	int index_a = int(lambda_a - lambda_min);
	int index_b = int(lambda_b - lambda_min);
	
	vec3 cie_XYZ_lambda_weight = (lambda_b - lambda)*brdf_weights[index_b] + (lambda - lambda_a)*brdf_weights[index_a];
	return cie_XYZ_lambda_weight;
}


// perform taylor approximation
vec2 taylorApproximation(vec2 coords, float k, float w, float s, float w_u, float w_v){
	vec2 precomputedFourier = vec2(0);
	int lower = 0;
	int upper = 32;
	float reHeight = 0.0;
	float imHeight = 0.0;
	float real_part = 0.0;
	float imag_part = 0.0;
	float fourier_coefficients = 1.0;
	
	
	
	// approximation till iteration 30 of fourier coefficient
	// TODO paramet. upper bound asap (but remember to limit) 
	for(int n = lower; n < upper; n++){
		
		int index_re = n;
		int index_im = (n + 31);
		
		reHeight = texture2DArray(TexArray, vec3(coords, index_re) ).x;
		imHeight = texture2DArray(TexArray, vec3(coords, index_im) ).x;
		int extremaIndex = n;
		
		precomputedFourier = getRescaledHeight(reHeight, imHeight, extremaIndex);

		float p1 = get_p_factor(w_u, T_1, periods);
		float p2 = get_p_factor(w_v, T_2, periods);
		
		float q1 = get_q_factor(w_u, T_1, periods);
		float q2 = get_q_factor(w_v, T_2, periods);
		
		
		float pq_scale_factor = pow(p1*p1 + q1*q1 , 0.5)*pow(p2*p2 + q2*q2 , 0.5); 

		// develope factorial and pow like this since 
		// otherwise we could get numerical rounding errors.
		// PRODUCT_n=0^N { pow(k*w*s,n)/n! }
		
		if(n == 0) fourier_coefficients = 1.0;
		else fourier_coefficients *= ((k*w*s)/n);
		
		float fourier_re = fourier_coefficients*precomputedFourier.x*pq_scale_factor;
		float fourier_im = fourier_coefficients*precomputedFourier.y*pq_scale_factor;
		
		
		real_part += fourier_re;
		imag_part += fourier_im;
	}
	
	
	return vec2(real_part, imag_part);
}


// first component N_min, second compontent N_max
vec2 compute_N_min_max(float t){
	// default case if t == 0 otherwise override it.
	float N_min = 0.0;
	float N_max = 0.0;
	
	if(t > 0.0){
		N_min = ceil((dx*t) / lambda_max);
		N_max = floor((dx*t) / lambda_min);
	}else if(t < 0.0){
		N_min = -ceil((dx*t) / lambda_min);
		N_max = -floor((dx*t) / lambda_max);
	}
 
	return vec2(N_min, N_max);
}

void main() {
	// INITIALIZATION
	bool mayRun = true;
	vec4 brdf = vec4(0.0,0.0,0.0,1.0);
	vec4 maxBRDF = vec4(0);
	vec2 P = vec2(0.0, 0.0);
	
	float abs_P_Sq = 0.0;
	float real_part = 0.0;
	float imag_part = 0.0;
	float reHeight = 0.0;
	float imHeight = 0.0;
	float factor1 = 0.0;
	float k = 0.0;
	float s = 2.4623*pow(10,-7);
	float fourier_coefficients = 1.0;
	float a = global_extrema[0].x;
	float b = global_extrema[0].y;
	float c = global_extrema[0].z;
	float d = global_extrema[0].w;
	float brdfMax = pow((b-a),2.0)+pow((d-c),2.0);
	
	
	//NB compute dynamically!!!
	float omega = 8.0*PI*pow(10.0, 7.0);
	omega = (30.0/100.0)*8.0*PI*pow(10.0, 7.0);
	
	
	// directional light source
	vec3 Pos = (modelview * position).xyz;
	vec4 lightPosition = modelview*(directionArray[0]);
	vec3 _k2 = normalize(-Pos);
	vec3 _k1 = normalize(lightPosition.xyz);
	vec3 V = _k1 - _k2;
	float u = V.x; float v = V.y; float w = V.z;
	
	
	// normal and tangent vector in camera coordinates
	vec3 camNormal = normalize((modelview*vec4(normal,0.0)).xyz);
	vec3 camTangent = normalize((modelview*vec4(tangent,0.0)).xyz);

	// compute vector-field rotation
	float phi = computeRotationAngle(tangent);
			
	// compute Fresnel and Gemometric Factor
	float F = getFressnelFactor(_k1, camNormal);
	float G = computeGFactor(camNormal, _k1, _k2);
	
	// get iteration bounds for given (u,v)
	vec2 N_u = compute_N_min_max(u);
	vec2 N_v = compute_N_min_max(v);
	
	
	vec2 N_uv[2] = vec2[2](N_u, N_v);
	
	// only specular contribution within epsilon range
	if(abs(u) < eps && abs(v) < eps){

		// TODO
		
	}else{
		float bias = 50.0/99.0;
		vec2 modUV = getRotation(u,v,-phi);
		
		// iterate twice: once for N_u and once for N_v lower,upper
		for(int variant = 0; variant < 2; variant++){
			
			int lower = int(N_uv[variant].x);
			int upper = int(N_uv[variant].y);
			
			float t = u;
			if(variant == 1) t = v;
			
			for(int iter = lower; iter <= upper; iter++){
				float lambda_iter = (dx*abs(t))/float(iter); 
				k = 2.0*PI / lambda_iter;
				
				vec2 coords = vec2((k*modUV.x/omega) + bias, (k*modUV.y/omega) + bias); //2d
				float w_u = k*u;
				float w_v = k*v;
				
				P = taylorApproximation(coords, k, w, s, w_u, w_v);
				float abs_P_Sq = P.x*P.x + P.y*P.y;
				
				float diffractionCoeff = getFactor(k, F, G, PI, w);			
				vec3 waveColor = avgWeighted_XYZ_weight(lambda_iter); // fix me pls QQ => brdf weights has to be increased and exported to a function- find nearest color
				brdf += vec4(diffractionCoeff * abs_P_Sq * waveColor, 1.0);
				maxBRDF += vec4(diffractionCoeff * brdfMax * waveColor, 1.0);
			}
		}
	}

	
	brdf = vec4(brdf.x/maxBRDF.x, brdf.y/maxBRDF.y, brdf.z/maxBRDF.z, 1) ; //  relative scaling
	
	float frac = 1.0 / 32.0;
	float fac2 = 100.0 / 70000.0;
	
	
	fac2 = 10.0 / 7.5; // wenn A und ohne global minmax
	fac2 = 1.0 / 3.0; // wenn nicht A und ohne gloabl minmax, // T=40
//	fac2 = 1.4 / 1.0; // wenn nicht A und ohne gloabl minmax, // T=1
//	fac2 = 1.0 / 3.0; // wenn nicht A und ohne gloabl minmax, // T=400
//	fac2 = 1.0 / 10.5; // wenn nicht A und ohne gloabl minmax, // T=4000
	fac2 = 1.0 / 1.0; // wenn nicht A und ohne gloabl minmax, // T=4
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
	}
	

	frag_texcoord = texcoord;
	gl_Position = projection * modelview * position;
}
