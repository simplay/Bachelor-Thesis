#version 150
#extension GL_EXT_gpu_shader4 : enable

//substitutes
#define MAX_LIGHTS 1
#define MAX_FACTORS 31
#define MAX_WEIGHTS 311
//Uniform variables, passed in from host program via suitable 

uniform sampler2DArray TexArray;
uniform sampler2D bodyTexture;
uniform sampler2D bumpMapTexture;

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
uniform int neigh_rad;
uniform float maxBumpHeight;
uniform float patchSpacing;
uniform float dimX;
uniform float dimY;

uniform sampler2DArray lookupText;


uniform float thetaI;
uniform float phiI;



// Variables passed in from the vertex shader
in vec2 frag_texcoord;
in vec4 light_direction[MAX_LIGHTS];
in vec4 normal_out;
in vec4 eyeVector;

in vec3 o_org_pos;
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
const float i_rescale = pow(10.0, -9.0);
float dx = patchSpacing;// 2.5*pow(10.0, -6.0); // -6 // distance between two patches (from center to center), make me parametric too
float s = maxBumpHeight;// 2.4623*pow(10,-7.0); // -7 // max height of a bump, make me parametric



float thetaR;


//error constants
const float eps_pq = 1.0*pow(10.0, -5.0); 
const float eps = 1.0*pow(10.0, -2.2);
const float tolerance = 0.999999; 

// flags
bool userSetPeriodFlag = (periodCount <= 0) ? true : false;
float dH = float(dimX)/float(dimN); // pixelsize how many microns does one pixel cover

//float dH = 65e-6/50.0;

//period constants
float N_1 = dimN; // number of pixels of downsized patch 
float N_2 = dimN; // number of pixels padded patch - see matlab
float t_0 = dx / N_1;
float T_1 = t_0 * N_1;
float T_2 = t_0 * N_1;
float periods = periodCount-1.0; // 26 // number of patch periods along surface
float Omega = ((N_1/N_2)*2.0*PI)/t_0; // (N_1/N_2)*2*PI/t_0, before 8.0*PI*pow(10.0,7.0);


//float bias = (N_2/2.0)/(N_2-1.0); // old: 50.0/99.0;


float getBias(){
	float tmp_bias = 0.0;
	if(int(N_2)%2 == 0){
		tmp_bias = (N_2/2.0)/(N_2-1.0);
	}else{
		tmp_bias = 0.5; // old: 50.0/99.0;
	}
	return tmp_bias;
}
float bias = getBias();

// for 99 works fine
//float bias = (1.0/(N_1-1.0))*((N_1-1.0)/2.0); // old: 50.0/99.0;
//float bias = 0.5; // old: 50.0/99.0;


float neighborRadius = (neigh_rad < 5 && neigh_rad > -1) ? float(neigh_rad) : 0.0;

//transformation constant
const mat3 M_Adobe_XR = mat3(
		2.0414, -0.5649, -0.3447,
		-0.9693,  1.8760,  0.0416,
		 0.0134, -0.01184,  1.0154
);	

const mat3 wd65 = mat3(
3.240479, -1.537150, -0.498535,
-0.969256,  1.875992,  0.041556,
0.055648, -0.204043,  1.057311
);

const mat3 CIE_XYZ = mat3(
0.418465712421894,	-0.158660784803799,	-0.0828349276180955,
-0.0911689639090227,	0.252431442139465,	0.0157075217695576,
0.000920898625343664,	-0.00254981254686328,	0.178598913921520);

const mat3 CIE_RGB = mat3(
2.3706743, -0.9000405, -0.4706338,
-0.5138850,  1.4253036,  0.0885814,
0.0052982, -0.0146949,  1.009396);

const mat3 M_Adobe_XRNew = mat3(
		 2.3642, -0.8964, -0.4680,
		-0.5151,  1.4262,  0.0887,
		 0.0052, -0.0144,  1.0090
);

//FUNCTIONS


//returns correct scaling factor
vec2 getRescaledHeight(float reHeight, float imHeight, int index){
	vec4 v = scalingFactors[index];
	float reMin = v.x; float reMax = v.y;
	float imMin = v.z; float imMax = v.w;
	
	float reC = reMin + reHeight*(reMax);
	float imC = imMin + imHeight*(imMax);
	
	return vec2(reC, imC); 
}


float varX_InTxtUnits;
float varY_InTxtUnits;
float orgU;
float orgV;
void setVarXY(){

	// scalingFactors[0].w = 15e-6/256;
	
	float sigSpatial = 65e-6/4.0f;
	// float sigSpatial = 15e-6/4.0f;
	/*
	if(debugTxtIdx != 0)
		sigSpatial = sigSpatial*debugTxtIdx;
	*/
	
	// temporary sigma
	float sigTemp;
	
	sigTemp = 0.5 / PI ;
	// sigTemp = 1.0;
	sigTemp = sigTemp /sigSpatial;
	// sigTemp = 1.0f / sigSpatial;
	
	// sigTemp = sigTemp / GetLightNormalCos();
	
	sigTemp = sigTemp * dH;
	
	varX_InTxtUnits = sigTemp * sigTemp * dimN * dimN ; 
	varY_InTxtUnits = sigTemp * sigTemp * dimN * dimN;
	
	// Set coordinates for the Origin
	if (int(dimN) % 2 == 0)
		orgU = float(dimN )/ 2.0f  ; // -2 dur to rotational lochay
	else
		orgU = float(dimN - 1.0) / 2.0f  ;
		
	if (int(dimN) % 2 == 0)
		orgV = float(dimN  )/2.0f  ;
	else
		orgV = float(dimN - 1.0)/2.0f ;
	
}



float getAbsFressnelFactor(vec3 _k1, vec3 _k2){
	float n_t = SMOOTH; // material constant
	float R0 = pow( (n_t - 1.0) / (n_t + 1.0) , 2.0); 
	vec3 L = _k2; 
	vec3 V = -_k1;
	vec3 H = normalize(L + V);
	float cos_teta = dot(H,V);
	cos_teta = (cos_teta > tolerance)? tolerance : ((cos_teta < 0.0) ? 0.0 :  cos_teta);
//	float ret_value = (R0 + (1.0 - R0) * pow(1.0 - cos_teta, 5.0));
	// faster than above - see GLSL specs
	float ret_value = mix(R0, 1.0, pow(1.0 - cos_teta, 5.0));
	ret_value = abs(ret_value);
	if(ret_value < 1.0*pow(10.0, -12.0)) ret_value = 0.0;
	return ret_value;

	
//	return mix(R0, 1.0, pow(1.0 - cos_teta, 5.0));
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


//perform taylor approximation
vec2 taylorApproximation(vec2 coords, float k, float w){
	vec2 precomputedFourier = vec2(0.0, 0.0);
	int lower = 0; int upper = int(approxSteps)+1;
	float reHeight = 0.0; float imHeight = 0.0;
	float real_part = 0.0; float imag_part = 0.0;
	float fourier_coefficients = 1.0;
	vec2 sum = vec2(0);
	
	// approximation till iteration 30 of fourier coefficient
	for(int n = lower; n <= upper; n++){
		reHeight = texture2DArray(TexArray, vec3(coords, n) ).x;
		imHeight = texture2DArray(TexArray, vec3(coords, n) ).y;
		int extremaIndex = n;
		
		precomputedFourier = getRescaledHeight(reHeight, imHeight, extremaIndex);

		// develope factorial and pow like this since 
		// otherwise we could get numerical rounding errors.
		// PRODUCT_n=0^N { pow(k*w*s,n)/n! }
		
		if(n == 0) fourier_coefficients = 1.0;
		else fourier_coefficients *= ((k*w*s)/float(n));
		
		sum = sum + fourier_coefficients*precomputedFourier;
	}

	return vec2(sum.x, sum.y);
}



vec3 getBRDF_RGB_T_D65(mat3 T, vec3 brdf_xyz){
//	vec3 D65 = vec3(0.95047, 1.0, 1.08883);
	vec3 D65 = vec3(1.0, 1.0, 1.0);
	vec3 output = vec3(0.0);
	vec3 D65BRDF = vec3(brdf_xyz.x*D65.x, brdf_xyz.y*D65.y, brdf_xyz.z*D65.z);
	
	output.x = dot(D65BRDF, T[0]);
	output.y = dot(D65BRDF, T[1]);
	output.z = dot(D65BRDF, T[2]);
	
	return output;
}


float getShadowMaskFactor(vec3 K1, vec3 K2){
	vec3 N = normalize(o_normal);
	vec3 hVec = -K1 + K2;
	hVec = normalize(hVec);
	
	float eDotH =  dot(hVec,K2);
	float eDotN =  dot(K2,N); // normal is (0,0,1);
	float hDotN =  dot(hVec,N);// normal is (0,0,1);
	float lDotN =  dot(-K1, N); // normal is (0,0,1);
	
	float f1 = 2 * hDotN * eDotN / eDotH;
	float f2 = 2 * hDotN * lDotN / eDotH;

	f1 = min(f1, f2);
	return min(1.0f, f1);
}


float getFresnelFactorAbsoluteRelative(vec3 K1, vec3 K2){
	float nSkin = 1.5;
	float nK = 0.0;
	
	vec3 hVec = -K1 + K2;
		 
	hVec = normalize(hVec);
	 
	float cosTheta = dot(hVec,K2);	
	
	float fF = (nSkin - 1.0);
	
	fF = fF * fF;
	
	float R0 = fF + nK*nK;
	if (cosTheta > 0.999999)
		fF = R0;
	else
		fF = fF + 4*nSkin*pow(1- cosTheta,5.0) + nK*nK;
	
	// do this division if its not on relative scale
	fF = fF / ((nSkin + 1.0)* (nSkin + 1.0) + nK*nK);
	
	return fF/R0;
}

vec3 rescaleXYZ(float X, float Y, float Z, int index){
	vec4 vMin = scalingFactors[index*2];
	vec4 vMax = scalingFactors[index*2 + 1];

	X = X * vMax.x;
	X = X + vMin.x;

	Y = Y * vMax.y;
	Y = Y + vMin.y;

	Z = Z * vMax.z;
	Z = Z + vMin.z;

	return vec3(X, Y, Z); 
}

float fFByR0;
float gainF(vec3 K1, vec3 K2){
	
	// This side is not visible
	if (K1.z > 0.0 || K2.z < 0.0){
		 return 0.0; 
	}
	
	// relative Fresnel Factor
	float F = fFByR0;
	F = F*F;
//	float cosNumNumSamples = cos(thetaR)*dimN*dimN;
	float cosNumNumSamples = 1.0;
	// compute G part
	float G = pow(1 - dot(K1,K2), 2.0); 
	
	// since (0,0) for spec
	float ww = K1.z - K2.z;
	ww = ww * ww;
	 
	if (ww < pow(10.0,-4.0)){
		return 0.0; // Shadowing Function
	}
	 
	return (F*G)/(ww*cosNumNumSamples);
}

vec3 gammaCorrect(vec3 inRGB, float gamma){
	float clLim = 0.0031308;
	float clScale = 1.055;
	
	if (inRGB.r < 0.0) 
		inRGB.r = 0.0;
	else if (inRGB.r < clLim)
		inRGB.r = inRGB.r * 12.92;
	else
		inRGB.r = clScale * pow(inRGB.r , (1.0/gamma)) - clScale + 1.0;
	
	if (inRGB.g < 0.0) 
		inRGB.g = 0.0;
	else if (inRGB.g < clLim)
		inRGB.g = inRGB.g * 12.92;
	else
		inRGB.g = clScale * pow(inRGB.g , (1.0/gamma)) - clScale + 1.0;
	
	if (inRGB.b < 0.0) 
		inRGB.b = 0.0;
	else if (inRGB.b < clLim)
		inRGB.b = inRGB.b * 12.92;
	else
		inRGB.b = clScale * pow(inRGB.b , (1.0/gamma)) - clScale + 1.0;
	
	
	if (isnan(inRGB.r *inRGB.g *inRGB.b))
	{
		inRGB.r  = 1.0;
		inRGB.g  = 0.0;
		inRGB.b  = 0.0;
	}
	
	
	if (inRGB.r > 1.0)
		inRGB.r = 1.0;
	
	if (inRGB.g > 1.0)
		inRGB.g = 1.0;
	
	if (inRGB.b > 1.0)
		inRGB.b = 1.0;
	
	return inRGB;
		
}
// get weight of gaussian window
float getGaussianWeight(float dist2, float sigma_f_pix){
	// sigma_f_pix = 2*sigma^2
	float norm_fact = sigma_f_pix;
	float exponent = -dist2/(sigma_f_pix);
	float w_ij = exp(exponent);
		
	if(abs(exponent) < 1.0*pow(10.0, -18.0)){
		return w_ij = 1.0;
	}
	w_ij /= norm_fact;
	return w_ij;
}


// get looup coordinates
vec2 getLookupCoord(float uu, float vv, float lambda){
	vec2 coord = vec2(0.0f);
	coord.x = uu * dH / lambda ;
	coord.y = vv * dH / lambda ;
	return coord;
}

float getGaussWeightAtDistance(float distU, float distV){
	// note that distU and distV are in textureCoordinateUnits
	
	distU = distU * distU / varX_InTxtUnits;
	distV = distV * distV / varY_InTxtUnits;
	
	return exp((-distU - distV)/2.0f);
}

vec2 getFFTAt(vec2 lookupCoord, int tIdx){
	const int winW = 2;
	//const int normF = (2*winW + 2)*(2*winW + 2);
	const float normF = 1.0f;

	// These are frequency increments
	int anchorX = int(floor(orgU + lookupCoord.x * (dimN - 0)));
	int anchorY = int(floor(orgV + lookupCoord.y * (dimN - 0)));
	
	vec2 fftMag = vec2(0.0f);
	
	// the following is a work around to have fixed number of operations
	// for each pixel
	if (anchorX < winW)
		anchorX = winW;
	
	if (anchorY < winW)
		anchorY = winW;
	
	if (anchorX + winW + 1 >  dimN - 1)
		anchorX = int(dimN) - 1 - int(winW) - 1;
	
	if (anchorY + winW + 1 >  dimN - 1)
		anchorY = int(dimN) - 1 - winW - 1;
	
	
	for (float i = (anchorX-winW); i <= (anchorX + winW + 1.0 ); ++i) {
		for (float j = (anchorY - winW); j <= (anchorY + winW + 1.0); ++j){

			vec3 texIdx = vec3(0.0f);
			

			float distU = float(i) - orgU - lookupCoord.x *dimN;
			float distV = float(j) - orgV - lookupCoord.y *dimN;
		
		
			texIdx.x = float(i)/ float(dimN - 1.0);
			texIdx.y = float(j)/float(dimN - 1.0);
			texIdx.z = float(tIdx);

			
			vec3 tmp = texture2DArray(TexArray, texIdx).xyz;
			
//			vec2 fftVal = getRescaledHeight(tmp.x, tmp.y, tIdx);
			vec2 fftVal = vec2(tmp.x, tmp.y);
			
			
			fftVal = fftVal * getGaussWeightAtDistance(distU, distV);
//			fftVal;
			
			
			fftMag = fftMag + fftVal;
		}
	}
	
	// onlt real and imaginary part are required. Third term is a waste
	return (fftMag.xy/normF); 
	// return 0.25f;
	
}





void runEvaluation(){
	
	float abs_P_Sq = 0.0;
	float real_part = 0.0;
	float imag_part = 0.0;
	float reHeight = 0.0;
	float imHeight = 0.0;
	float factor1 = 0.0;
	float k = 0.0;
	float fourier_coefficients = 1.0;
	float lambda_iter = 0.0;
	float t1 = 0.0;
	float t2 = 0.0;
	float phi = -PI/2.0;
	phi = 0.0;
	
	vec4 o_col = vec4(0.0, 0.0, 0.0, 0.0);
	vec4 brdf = vec4(0.0, 0.0, 0.0, 1.0);
	vec4 maxBRDF = vec4(0.0, 0.0, 0.0, 1.0);
	vec2 P = vec2(0.0, 0.0);
	vec2 coords = vec2(0.0);
	
	setVarXY();
	// okay from here on
	thetaR = asin(sqrt(o_org_pos.x * o_org_pos.x + o_org_pos.y * o_org_pos.y ));
	float phiR = atan(o_org_pos.y, o_org_pos.x);

	vec3 _k1 = vec3(0.0f);
	vec3 _k2 = vec3(0.0f);
	
	
	_k1.x = - sin(thetaI)*cos(phiI);
	_k1.y = - sin(thetaI)*sin(phiI);
	_k1.z = - cos(thetaI);
	
	_k2.x = sin(thetaR)*cos(phiR);
	_k2.y = sin(thetaR)*sin(phiR);
	_k2.z = cos(thetaR);

	float uu0 = _k1.x - _k2.x;
	float vv0 = _k1.y - _k2.y;
	float ww = _k1.z - _k2.z;	
	
	fFByR0 = getFresnelFactorAbsoluteRelative(_k1, _k2);
	float shadowF = getShadowMaskFactor(_k1, _k2);

	vec3 V = vec3(uu0, vv0, ww);
	float u = V.x; float v = V.y; float w = V.z;
	

	float iterMax = 700.0;
	float lambdaStep = (lambda_max - lambda_min)/(iterMax-1.0);
	float F2 = fFByR0*fFByR0;
	
	
	float stepSize = 20.0;
	float sigma_f_pix = ((2.0*dx) / (PI*dimX));
	float comp_sigma = sigma_f_pix;
	sigma_f_pix *= sigma_f_pix;
	sigma_f_pix *= 2.0;

	for(float iter = 0; iter < iterMax; iter = iter + stepSize){
		
		float lambda_iter = iter*lambdaStep + lambda_min;
		k = (2.0*PI) / lambda_iter;
		float kk = (1.0) / lambda_iter;

		

		// xyz value of color for current wavelength (regarding current wavenumber k).
		vec3 waveColor = avgWeighted_XYZ_weight(lambda_iter);
		vec2 lookupCoord = getLookupCoord(u, v, lambda_iter);
		
		
		vec2 precomputedFourier = vec2(0.0, 0.0);
		int lower = 0; int upper = int(approxSteps)+1;
		float reHeight = 0.0; float imHeight = 0.0;
		float real_part = 0.0; float imag_part = 0.0;
		float fourier_coefficients = 1.0;
		vec2 sum = vec2(0.0, 0.0);
		
		
		// approximation till iteration 30 of fourier coefficient
		for(int n = lower; n <= upper; n++){
			reHeight = texture2DArray(TexArray, vec3(lookupCoord, n) ).x;
			imHeight = texture2DArray(TexArray, vec3(lookupCoord, n) ).y;
			
			vec2 fftCoef = getFFTAt(lookupCoord, n);
			fftCoef = getRescaledHeight(fftCoef.x, fftCoef.y, n);


			// develope factorial and pow like this since 
			// otherwise we could get numerical rounding errors.
			// PRODUCT_n=0^N { pow(k*w*s,n)/n! }
			
			if(n == 0) fourier_coefficients = 1.0;
			else fourier_coefficients *= ((k*w*s)/float(n));
			
//			sum = sum + fourier_coefficients*vec2(1.0, 1.0);
			sum = sum + fourier_coefficients*fftCoef;
		}
		float abs_P_Sq = sum.x * sum.x + sum.y * sum.y;
		brdf += vec4(abs_P_Sq * waveColor, 0.0);	
	
	}

	float ambient = 0.1;	
	// remove negative values
	if(brdf.x < 0.0 ) brdf.x = 0.0;
	if(brdf.y < 0.0 ) brdf.y = 0.0;
	if(brdf.z < 0.0 ) brdf.z = 0.0;
	brdf.w = 1.0;
	
	if(brdf.x < 1e-5) brdf.x = 0.0;
	if(brdf.y < 1e-5) brdf.y = 0.0;
	if(brdf.z < 1e-5) brdf.z = 0.0;
	
	brdf =  brdf*0.01*gainF(_k1, _k2)*shadowF;
	brdf.xyz = getBRDF_RGB_T_D65(M_Adobe_XRNew, brdf.xyz);
	

	if(isnan(brdf.x) ||isnan(brdf.y) ||isnan(brdf.z)) o_col = vec4(1.0, 0.0, 0.0, 1.0);
	else if(isinf(brdf.x) ||isinf(brdf.y) ||isinf(brdf.z)) o_col = vec4(0.0, 1.0, 0.0, 1.0);
	else o_col = brdf+vec4(ambient,ambient,ambient,0.0);

	o_col = vec4(gammaCorrect(o_col.xyz, 1.3), 1.0f);
	frag_shaded	= o_col;

}

void main(){
	runEvaluation();
}
