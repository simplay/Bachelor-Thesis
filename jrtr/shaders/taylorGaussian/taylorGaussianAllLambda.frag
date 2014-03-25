#version 150
#extension GL_EXT_gpu_shader4 : enable

// substitutes
#define MAX_LIGHTS 1
#define MAX_TAYLORTERMS 37
#define MAX_WFACTORS 78
#define MAX_WEIGHTS 401

//Uniform variables, passed in from host program via suitable
uniform sampler2DArray TexArray;
uniform sampler2D bodyTexture;
uniform vec4 cop_w;
uniform vec4 brdf_weights[MAX_WEIGHTS];
uniform vec4 directionArray[MAX_LIGHTS];
uniform vec4 scalingFactors[MAX_WFACTORS];
uniform vec4 camPos;
uniform vec3 radianceArray[MAX_LIGHTS];
uniform int fftHH; // height of FFT Image
uniform int fftWW; // width of FFT Image
uniform int approxSteps;
uniform int renderBrdfMap;
uniform int isCone;
uniform float LMIN;
uniform float LMAX;
uniform float delLamda;
uniform float dimX;
uniform float t0;
uniform float thetaI;
uniform float phiI;
uniform float bruteforcespacing;

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

// shader constants
const float PI = 3.14159265358979323846264;
const float phiRect = // 0.87285485835042309f; // For Elaphe650
	// 0.0f;
	2.4436511851453195f;  // this one works for Elaphe650with fingers pointing
							// downwards in the .mat file

const mat3 M_Adobe_XR = mat3(
		2.0414, -0.5649, -0.3447,
		-0.9693,  1.8760,  0.0416,
		 0.0134, -0.01184,  1.0154
);

const mat3 M_Adobe_XRNew = mat3(
		 2.3642, -0.8964, -0.4680,
		-0.5151,  1.4262,  0.0887,
		 0.0052, -0.0144,  1.0090
);

// global shader variables
float varX_InTxtUnits;
float varY_InTxtUnits;
float dH = 0.0f;
float orgU;
float orgV;

// function declarations
void mainBRDFMap();
void mainRenderGeometry();
void coneMain();
void gemMain();
vec2 getNMMfor(float, float);
vec3 sumContributionAlongDir(vec2, float, float);

void setVarXY(){
	dH = t0;
	float coherenceLength = 65.0e-6;
	float sigSpatial = coherenceLength/4.0f;

	// temporary sigma
	float sigTemp;
	sigTemp = 0.5 / PI ;
	sigTemp = sigTemp /sigSpatial;
	sigTemp = sigTemp * dH;
	
	varX_InTxtUnits = sigTemp * sigTemp * fftWW * fftWW ; 
	varY_InTxtUnits = sigTemp * sigTemp * fftHH * fftHH;
	
	// Set coordinates for the Origin
	if(fftWW % 2 == 0){
		orgU = float(fftWW) / 2.0f  ; // -2 dur to rotational lochay
	}else{
		orgU = float(fftWW - 1.0) / 2.0f  ;
	}
	
	if(fftHH % 2 == 0){
		orgV = float(fftHH) / 2.0f  ;
	}else{
		orgV = float(fftHH - 1.0)/2.0f ;
	}
}

vec3 getBRDF_RGB_T_D65(mat3 T, vec3 brdf_xyz){
	vec3 D65 = vec3(0.95047, 1.0, 1.08883);
	vec3 output = vec3(0.0);
	vec3 D65BRDF = vec3(brdf_xyz.x*D65.x, brdf_xyz.y*D65.y, brdf_xyz.z*D65.z);
	output.x = dot(D65BRDF, T[0]);
	output.y = dot(D65BRDF, T[1]);
	output.z = dot(D65BRDF, T[2]);
	return output;
}

float getFresnelFactor(vec3 K1, vec3 K2){
	float nSkin = 1.5;
	float nK = 0.0;
	vec3 hVec = -K1 + K2;	 
	hVec = normalize(hVec);	 
	float cosTheta = dot(hVec, K2);	
	float fF = (nSkin - 1.0);
	fF = fF*fF;
	float R0 = fF + nK*nK;
	if(cosTheta > 0.999){
		fF = R0;
	}else{
		fF = fF + 4.0*nSkin*pow(1.0 - cosTheta, 5.0) + nK*nK;
	}	
	return fF/R0;
}

float getFresnelFactorAbsolute(vec3 K1, vec3 K2){
	float nSkin = 1.5;
	// float nSkin = 1.0015;
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
	fF = fF/ ((nSkin + 1.0)* (nSkin + 1.0) + nK*nK);
	
	return fF;
}

float getShadowMaskFactor(vec3 K1, vec3 K2){
	vec3 N = normalize(o_normal);
	vec3 hVec = -K1 + K2;
	hVec = normalize(hVec);	
	float eDotH = dot(hVec,K2);
	float eDotN = dot(K2,N); // normal is (0,0,1);
	float hDotN = dot(hVec,N);// normal is (0,0,1);
	float lDotN = dot(-K1, N); // normal is (0,0,1);
	float f1 = 2.0 * hDotN * eDotN / eDotH;
	float f2 = 2.0 * hDotN * lDotN / eDotH;
	f1 = min(f1, f2);
	return min(1.0f, f1);
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

float gainF(vec3 K1, vec3 K2){
	float gF = 1.0 - dot(K1,K2);
	gF = gF*gF; 
	float ww = K1.z - K2.z;
	 
	 
	if(K1.z > 0.0 || K2.z < 0.0){
		return 0.0; // This side is not visible
	}
	 
	ww = ww*ww;

	gF = gF / K2.z;
	gF = gF / ww;
	 
	float fFac = getFresnelFactor(K1, K2); 
	return fFac*gF;
}

vec3 gammaCorrect(vec3 inRGB, float gamma){
	float clLim = 0.0031308;
	float clScale = 1.055;
	
	if(inRGB.r < 0.0){ 
		inRGB.r = 0.0;
	}else if(inRGB.r < clLim){
		inRGB.r = inRGB.r * 12.92;
	}else{
		inRGB.r = clScale * pow(inRGB.r , (1.0/gamma)) - clScale + 1.0;
	}
	
	if (inRGB.g < 0.0){ 
		inRGB.g = 0.0;
	}else if(inRGB.g < clLim){
		inRGB.g = inRGB.g * 12.92;
	}else{
		inRGB.g = clScale * pow(inRGB.g , (1.0/gamma)) - clScale + 1.0;
	}
	
	if(inRGB.b < 0.0){ 
		inRGB.b = 0.0;
	}else if(inRGB.b < clLim){
		inRGB.b = inRGB.b * 12.92;
	}else{
		inRGB.b = clScale * pow(inRGB.b , (1.0/gamma)) - clScale + 1.0;
	}
	
	if(isnan(inRGB.r *inRGB.g *inRGB.b)){
		inRGB.r  = 1.0;
		inRGB.g  = 0.0;
		inRGB.b  = 0.0;
	}
	
	if(inRGB.r > 1.0){
		inRGB.r = 1.0;
	}
	
	if(inRGB.g > 1.0){
		inRGB.g = 1.0;
	}
	
	if(inRGB.b > 1.0){
		inRGB.b = 1.0;
	}
	return inRGB;		
}

/*
 * return 2D coordinates for given uu vv in 0.0.. 1.0, 0.0.. 1.0 scale i.e in
 * texture Coordniiates..
 */
vec2 getLookupCoord(float uu, float vv, float lambda){
	float dH = t0;
	vec2 coord = vec2(0.0, 0.0);
	coord.x = uu * 1e9 *dH / lambda ;
	coord.y = vv * 1e9 *dH / lambda ;
	return coord;
}

float getGaussWeightAtDistance(float distU, float distV){
	// note that distU and distV are in textureCoordinateUnits
	distU = distU * distU / varX_InTxtUnits;
	distV = distV * distV / varY_InTxtUnits;
	return exp((-distU - distV)/2.0f);
}

vec2 getFFTAt(vec2 lookupCoord, int tIdx){
	const int winW = 1;

	// These are frequency increments
	int anchorX = int(floor(orgU + lookupCoord.x * (fftWW - 0)));
	int anchorY = int(floor(orgV + lookupCoord.y * (fftHH - 0)));
	
	vec3 fftMag = vec3(0.0f);
	
	// the following is a work around to have fixed number of operations
	// for each pixel
	if(anchorX < winW){
		anchorX = winW;
	}
	
	if(anchorY < winW){
		anchorY = winW;
	}
	
	if(anchorX + winW + 1 >  fftWW - 1){
		anchorX = fftWW - 1 - winW - 1;
	}
	
	if(anchorY + winW + 1 >  fftHH - 1){
		anchorY = fftHH - 1 - winW - 1;
	}
	
	for(float i = (anchorX-winW); i <= (anchorX + winW + 1 ); ++i){
		for(float j = (anchorY - winW); j <= (anchorY + winW + 1); ++j){
			vec3 texIdx = vec3(0.0f);
			float distU = float(i) - orgU - lookupCoord.x*float(fftWW - 0);
			float distV = float(j) - orgV - lookupCoord.y*float(fftHH - 0);
		
			texIdx.x = float(i)/ float(fftWW - 1);
			texIdx.y = float(j)/float(fftHH - 1);
			texIdx.z = float(tIdx);
			
			vec3 fftVal = texture2DArray(TexArray, texIdx).xyz;			
			fftVal = rescaleXYZ(fftVal.x, fftVal.y, 0.0, tIdx);
			fftVal = fftVal * getGaussWeightAtDistance(distU, distV);
			fftMag = fftMag + fftVal;
		}
	}
	
	// onlt real and imaginary part are required. Third term is a waste
	return (fftMag.xy); 	
}

vec4 getClrMatchingFnWeights(float lVal){
	if(lVal < LMIN){
		lVal = LMIN;
	}
	
	if(lVal >= LMAX){
		// just to ensure that the flooring
		// latches to the lower value
		lVal = LMAX - delLamda/100000.0f; 
	}								
	
	float alpha  = (lVal - LMIN)/delLamda;
	int lIdx = int(floor(alpha));
	alpha = alpha - lIdx;
	return brdf_weights[lIdx] * (1-alpha) + brdf_weights[lIdx+1] * alpha; 
}

vec3 getRawXYZFromTaylorSeries(float uu,float vv,float ww){
	vec3 opVal = vec3(0.0f);	
	float scale = 1.0f;
	float fftMag = 0.0f;
	float xNorm = 0.0f;
	float yNorm = 0.0f;
	float zNorm = 0.0f;
	float specSum = 0.0f;
	float lambdaStep = 5.0;
	
	float k_max = (2.0 * PI * pow(10.0f, 3.0f)) / (LMIN);
	float k_min = (2.0 * PI * pow(10.0f, 3.0f)) / (LMAX);
	float k_step = (2.0 * PI)/lambdaStep;
	
	for(float lambda = LMIN; lambda <= LMAX; lambda = lambda + lambdaStep){
//	for(float k = k_min; k <= k_max; k = k + k_step){
//		float lambda = (2.0 * PI * pow(10.0f, 3.0f)) / k;
		
		vec4 xyzColorWeights = getClrMatchingFnWeights(lambda);
		float specV = xyzColorWeights.w;
		xNorm = xNorm + specV*xyzColorWeights.x;
		yNorm = yNorm + specV*xyzColorWeights.y;
		zNorm = zNorm + specV*xyzColorWeights.z;
		
		vec2 lookupCoord = getLookupCoord(uu, vv, lambda);	
		vec2 tempFFTScale = vec2(0.0f);

		for(int tIdx = 0; tIdx < MAX_TAYLORTERMS; tIdx++){
			if(0 == tIdx){
				scale = 1.0f;
			}else{
				float currenScale = (ww * 2.0 * PI * pow(10.0f, 3.0f)) / (lambda * tIdx);
				scale = scale * currenScale;
			}	
			vec2 fftCoef = getFFTAt(lookupCoord, tIdx);
			tempFFTScale = tempFFTScale + scale * fftCoef;
		}
		
		float fftMagSqr = tempFFTScale.x * tempFFTScale.x + tempFFTScale.y * tempFFTScale.y;
		opVal.x = opVal.x + fftMagSqr * specV * xyzColorWeights.x;
		opVal.y = opVal.y + fftMagSqr * specV * xyzColorWeights.y;
		opVal.z = opVal.z + fftMagSqr * specV * xyzColorWeights.z;
	}
	
	opVal.x = opVal.x / xNorm ;
	opVal.y = opVal.y / yNorm ;
	opVal.z = opVal.z / zNorm ;

	return opVal;
}




void mainRenderGeometry(){
	setVarXY();
	float thetaR = asin(sqrt(o_org_pos.x*o_org_pos.x + o_org_pos.y*o_org_pos.y ));
	float phiR = atan(o_org_pos.y, o_org_pos.x);

    vec3 N = normalize(o_normal);
    vec3 T = normalize(o_tangent);

  
	// directional light source
	vec3 Pos =  normalize(o_pos); 
	vec3 lightDir =  normalize(o_light);
	
	
	float shadowF = getShadowMaskFactor(lightDir, Pos);
	float uu = lightDir.x - Pos.x;
	float vv = lightDir.y - Pos.y;
	float ww = lightDir.z - Pos.z;

	vec3 totalXYZ  = getRawXYZFromTaylorSeries(uu, vv, ww);
	
//	vec3 totalXYZ  = vec3(1, 0, 0);
	
	totalXYZ = totalXYZ*gainF(lightDir, Pos)*1000.0*shadowF;
	totalXYZ = getBRDF_RGB_T_D65(M_Adobe_XRNew, totalXYZ);
	if(isnan(totalXYZ.x*totalXYZ.y*totalXYZ.z)){
		totalXYZ.x = 1.0;
		totalXYZ.y = 1.0;
		totalXYZ.z = 0.0;
	}
	
	
	
	float diffuseL = 0.0f;
	
	if(Pos.z < 0.0  || dot(-lightDir, N) < 0.0){
		diffuseL = 0.0;
	}else{
		diffuseL = dot(-lightDir, N);
	}
	
	vec4 tex = vec4(0.0f);


	tex = texture2D(bodyTexture, frag_texcoord);
	

	float alpha = getFresnelFactorAbsolute(lightDir,Pos);
	 
	if (alpha > 0.0f){
		alpha = 0.0f; 
		
	}else if(alpha > 1.0f){ 
		alpha = 1.0f;
	}
	
	float diffW = 0.1f; 
	float gamma = 2.5; 
	tex.xyz = gammaCorrect(tex.xyz ,1.0f/1.0);
	vec3 finClr = gammaCorrect((1-diffW)*(totalXYZ + (1-alpha) * tex.xyz *diffuseL) + tex.xyz * diffW, gamma);
	frag_shaded = vec4(gammaCorrect(totalXYZ, gamma), 1.0);
//	frag_shaded = vec4(finClr, 1.0);
//	frag_shaded = vec4(tex.xyz, 1.0);
	
}

void coneMain(){
	frag_shaded = vec4(vec3(0,1,0), 1.0);
}

void mainBRDFMap(){
	setVarXY();
	float thetaR = asin(sqrt(o_org_pos.x*o_org_pos.x + o_org_pos.y*o_org_pos.y ));
	float phiR = atan(o_org_pos.y, o_org_pos.x);
	vec3 k1 = vec3(0.0f);
	vec3 k2 = vec3(0.0f);
	
	k1.x = -sin(thetaI)*cos(phiI);
	k1.y = -sin(thetaI)*sin(phiI);
	k1.z = -cos(thetaI);
	
	k2.x = sin(thetaR)*cos(phiR);
	k2.y = sin(thetaR)*sin(phiR);
	k2.z = cos(thetaR);
	
	float shadowF = getShadowMaskFactor(k1, k2);
	float uu = k1.x - k2.x;
	float vv = k1.y - k2.y;
	float ww = k1.z - k2.z;

	vec3 totalXYZ = getRawXYZFromTaylorSeries(uu, vv, ww);

	
	totalXYZ = totalXYZ*gainF(k1, k2)*150.0*shadowF;
	totalXYZ = getBRDF_RGB_T_D65(M_Adobe_XRNew, totalXYZ);
	if(isnan(totalXYZ.x*totalXYZ.y*totalXYZ.z)){
		totalXYZ.x = 1.0;
		totalXYZ.y = 1.0;
		totalXYZ.z = 0.0;
	}
	frag_shaded = vec4(gammaCorrect(totalXYZ, 2.5), 1.0);
}


vec3 blend3 (vec3 x){
	vec3 y = 1.0 - x * x;
	y = max(y, vec3 (0, 0, 0));
	return (y);
}
//
void gemMain(){

	float thetaR = asin(sqrt(o_org_pos.x*o_org_pos.x + o_org_pos.y*o_org_pos.y ));
	float phiR = atan(o_org_pos.y, o_org_pos.x);
	vec3 k1 = vec3(0.0f);
	vec3 k2 = vec3(0.0f);
	
	k1.x = -sin(thetaI)*cos(phiI);
	k1.y = -sin(thetaI)*sin(phiI);
	k1.z = -cos(thetaI);
	
	k2.x = sin(thetaR)*cos(phiR);
	k2.y = sin(thetaR)*sin(phiR);
	k2.z = cos(thetaR);
	
	float u = k1.x - k2.x;
	float v = k1.y - k2.y;
	float w = k1.z - k2.z;
	

	
//	float thetaR = asin(sqrt(o_org_pos.x*o_org_pos.x + o_org_pos.y*o_org_pos.y ));
//	float phiR = atan(o_org_pos.y, o_org_pos.x);

    vec3 N = normalize(o_normal);
    vec3 T = normalize(o_tangent);

  
	// directional light source
	vec3 Pos =  normalize(o_pos); 
	vec3 lightDir =  normalize(o_light);
	
	
	float shadowF = getShadowMaskFactor(lightDir, Pos);
//	float u = lightDir.x - Pos.x;
//	float v = lightDir.y - Pos.y;
//	float w = lightDir.z - Pos.z;
	
	
//	float r = 10.50; // snake
	float r = 1.50; // roughness factor for: 1.5f seems to look nice
//	r = 2.50; // roughness factor for: 1.5f seems to look nice
	float e = r * u / w;
	float c = exp(-e * e);
	
	float e2 = 3.0 * u / w;
	float c2 = exp(-e * e);
	
	vec4 anis = vec4(1,1,1,1) * vec4(c, c, c, 1);
	vec4 anis2 = vec4(1,1,1,1) * vec4(c2, c2, c2, 1);
	
//	float shadowF = getShadowMaskFactor(k1, k2);
	float lambda_min = 0.38; // 400nm red
	float lambda_max = 0.78; // 700nm blue
	
	vec4 cdiff = vec4(0, 0, 0, 1);
	
	// 2.5microns
	float d = 2.5;
//	float d = 1.55227;
//	float d = 2.5;
	// bruteforce spacing
//	d = bruteforcespacing*1;
	
	
	float eps = 0.0;
	
	float uuu = abs(u);
	float vvv = abs(v);
	
	vec2 uNMM = getNMMfor(uuu, d);
	vec2 vNMM = getNMMfor(vvv, d);
	
	float dist2Zero = sqrt(uuu*uuu + vvv*vvv);
	
	if(vvv < 0.01){
		cdiff.xyz += sumContributionAlongDir(uNMM, uuu, d);
		if(dist2Zero < 0.01) {
//		if(uuu < 0.002) {
			eps = 1.0;
		}
	}
	
//	if(uuu < 0.0){
//		cdiff.xyz += sumContributionAlongDir(vNMM, vvv, d);
//		if(vvv < 0.01) eps = 0.0;
//		if(vvv < 0.005) {
//
//			eps = 1.0;
//		}
//	}
	
		
	vec3 totalXYZ = cdiff.xyz/10.0 + eps;
	vec3 pepepe = totalXYZ;
	
	//totalXYZ = totalXYZ*gainF(lightDir, Pos)*1.0*shadowF;
	//totalXYZ = getBRDF_RGB_T_D65(M_Adobe_XRNew, totalXYZ);
	
	
	float value = (getShadowMaskFactor(lightDir, Pos)*gainF(lightDir, Pos));
	value = (value < 0.0) ? 0.0 : value;
//	value = getShadowMaskFactor(lightDir, Pos);
	vec3 pewpew = 1.0*totalXYZ*value;
	
	// frag_shaded = vec4(pewpew,1.0);
	frag_shaded = anis*vec4(gammaCorrect(pewpew, 2.6), 1.0);
//	float mask = getShadowMaskFactor(lightDir, Pos);
	
//	frag_shaded = anis*vec4(gammaCorrect(mask*totalXYZ, 2.6), 1.0);

	
//	frag_shaded = anis;
//	frag_shaded = vec4(pepepe,1);
//	frag_shaded = vec4(gammaCorrect(totalXYZ, 2.2), 1.0);
}

vec3 sumContributionAlongDir(vec2 boundaries, float dir, float spacing) {
	float f = 4;
	float lambda_min = 0.38; // 400nm red
	float lambda_max = 0.78; // 700nm blue
	vec3 spectrumContribution = vec3(0.0, 0.0, 0.0);
	
	for (float n = boundaries.x; n <= boundaries.y; n = n + 1) {
		// values between 0.38 and 0.78 microns
		float lval = (dir*spacing/n);
			
		// rescale them to [0,1]
		float y = (lval-lambda_min)/(lambda_max-lambda_min);
		spectrumContribution += ( blend3(vec3(f * (y - 0.75), f * (y - 0.5), f * (y - 0.25))) ); 
	}
	return spectrumContribution;
}

// compute n_min n_max from given direction
// spacing in microns
// t is absolute valued compontent of (u,v,w)
vec2 getNMMfor(float t, float spacing) {
	float lambda_min = 0.38; // 400nm red
	float lambda_max = 0.78; // 700nm blue
	
	float n_min = (spacing*t)/lambda_max;
	float n_max = (spacing*t)/lambda_min;
	
	float lower = ceil(n_min);
	float upper = floor(n_max);
	return vec2(lower, upper);
}

void main(){
	gemMain();
//	if(isCone==1){
//		coneMain();
//	}else{
//		if(renderBrdfMap == 1){
//			mainBRDFMap();
//		}else{
//			mainRenderGeometry();
//		}
//	}
	

}