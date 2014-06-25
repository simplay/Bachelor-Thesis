/*
 * FLSS fragment shader which renders the effect of diffraction
 * on a given grating structure.
 * This shader performs an uniform sampling over the wavelength spectrum [380nm,780nm]
 * For further information please read the chapter 'implementation' of my thesis.
 */

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
uniform int approxSteps; // number of taylor iterations
uniform int shouldRenderBrdfMap;
uniform int isCone;
uniform float LMIN; // unit [nm]
uniform float LMAX;
uniform float delLamda; // delta lambda
uniform float dimX;
uniform float t0; // corresponds to dH
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
float centerU;
float centerV;

// function declarations
void mainBRDFMap();
void mainRenderGeometry();
void coneMain();
void gemMain();
vec2 getNMMfor(float, float);
vec3 sumContributionAlongDir(vec2, float, float);

// computes the frequency variance used for the gaussian window
// for the dtft reconstruction during the windowing approach.
// furthermore, this method computes the center position of the dft images
// used for computing the image lookup coordinates.
void initialzeConstants(){
	dH = t0;
	float coherenceArea = 65.0e-6;
	float sigSpatial = coherenceArea/4.0f;
	float sigTemp = 0.5 / PI ;
	sigTemp = sigTemp /sigSpatial;
	sigTemp = sigTemp * dH;
	
	varX_InTxtUnits = sigTemp * sigTemp * fftWW * fftWW ; 
	varY_InTxtUnits = sigTemp * sigTemp * fftHH * fftHH;
	
	// Set coordinates for the Origin
	if (fftWW % 2 == 0){
		centerU = float(fftWW) / 2.0f; 
	}else {
		centerU = float(fftWW - 1.0) / 2.0f;
	}
	
	if (fftHH % 2 == 0){
		centerV = float(fftHH) / 2.0f;
	}else {
		centerV = float(fftHH - 1.0)/2.0f;
	}
}

// perform a color space transformation:
// from CIE XYZ to RGB using the illuminant D65 
// in order to define the white point.
vec3 getBRDF_RGB_T_D65(mat3 T, vec3 brdf_xyz){
	vec3 D65 = vec3(0.95047, 1.0, 1.08883);
	vec3 output = vec3(0.0);
	vec3 D65BRDF = vec3(brdf_xyz.x*D65.x, brdf_xyz.y*D65.y, brdf_xyz.z*D65.z);
	output.x = dot(D65BRDF, T[0]);
	output.y = dot(D65BRDF, T[1]);
	output.z = dot(D65BRDF, T[2]);
	return output;
}

// compute fresnel coefficient according to Schlick's approximation
// quantitative description of transmissed and reflectied fraction of light
// when a beam of light hits a plane surface.
// describe what fraction of the light is reflected and what fraction is refracted
// see: http://en.wikipedia.org/wiki/Fresnel_equations
// @param K1 incident light direction
// @param K2 viewing direction
// @return reflected amount of light
float getFresnelFactor(vec3 K1, vec3 K2){
	float nSkin = 1.5;
	float nK = 0.0;
	vec3 hVec = -K1 + K2;	 
	hVec = normalize(hVec);	 
	float cosTheta = dot(hVec, K2);	
	float fF = (nSkin - 1.0);
	fF = fF*fF;
	float R0 = fF + nK*nK;
	if (cosTheta > 0.999){
		fF = R0;
	} else {
		fF = fF + 4.0*nSkin*pow(1.0 - cosTheta, 5.0) + nK*nK;
	}	
	return fF/R0;
}

// same as for #getFresnelFactor but its output values
// are normalized into the range [0,1]
float getFresnelFactorAbsolute(vec3 K1, vec3 K2){
	float nSkin = 1.5;
	float nK = 0.0;
	float fF = (nSkin - 1.0);
	fF = fF*fF;
	float R0 = fF + nK*nK;
	float tmp = getFresnelFactor(K1, K2)*R0;
	return (tmp / ((nSkin + 1.0)* (nSkin + 1.0) + nK*nK));
}

// a filter which models the occlusion of the 
// given light source which occurs since our 
// surface is assumed to be v-caved shaped.
// thus some light rays may no reach a given point
// on a surface. also models masking effects
// similarly a viewer cannot fully see all the points 
// on a given v-caved surface.
// @param K1 incident light direction
// @param K2 viewing direciton
// @return final amount of light contribution at a given point
//		   a viewer will see due to shadowing and masking influence.
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

// rescales given normalized CIE XYZ colors back to heir original scale
// using their extreme values.
// @param X 1st CIE XYZ colorvalue (normalized)
// @param Y 2nd CIE XYZ colorvalue (normalized)
// @param Z 3rd CIE XYZ colorvalue (normalized)
// @param index which dft terms should be used
//        in oder to appropriately lookup their
//        extreme values for rescaling them.
// @return rescaled CIE XYZ colors
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

// gain factor as described in the thesis
// consisting of C = F*G/w^2
// where F is the fesnel number
//       G is the geometric term
//       w is the 3rd component of (u,v,w)
// for further details I refer to my thesis,
// in the chapter 'derivations'.
// @param incident light direction
// @param viewing direction
// @return F*G/w^2
float gainFactor(vec3 k1, vec3 k2){
	// This side is not visible
	if (k1.z > 0.0 || k2.z < 0.0){
		return 0.0;
	}
	float ww2 = k1.z - k2.z;
	ww2 = ww2*ww2;
	
	float geometricalTerm = 1.0 - dot(k1,k2);
	geometricalTerm *= (geometricalTerm/(k2.z*ww2)); 
	 
	float fresnelTerm = getFresnelFactor(k1, k2); 
	return fresnelTerm*geometricalTerm;
}

// performs gamma correction applied on a vector containing rgb colors
// see http://en.wikipedia.org/wiki/Gamma_correction
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

// computes lookup coordinates used during sampling the spectrum.
// dh:float denotes the length of one particular pixel
// for further information please refere to the thesis, 
// section lookup coorinates of chapter 'implementation'
// @param uu 1st component of (u,v,w)
// @param vv 2nd component of (u,v,w)
// @returns texture coordinates [0,1]^2 :vec2 for given uu vv
vec2 getLookupCoord(float uu, float vv, float lambda){
	float dH = t0;
	vec2 coord = vec2(0.0, 0.0);
	coord.x = uu * 1e9 *dH / lambda ;
	coord.y = vv * 1e9 *dH / lambda ;
	return coord;
}

// windowing function used for reconstructing the DTFT
// a Gaussian window defined by sigma_f 
// (see sections 'coherence' and 'reconstruct dtft from dft' in thesis)
// distance to pixel (i,j) is used as mean value
// contribution weight of pixel (i,j) having with 
// distance vector (dU,dV) = (baseU, BaseV) - (i,j)
// @param distU 1st component of distance vec
// @param distV 2nd component of distance vec
// @return gaussian window weight for given distance determined by 
// 		   pixel location (i,j) distance to its base.
float getGaussWeightAtDistance(float distU, float distV){
	// note that distU and distV are in textureCoordinateUnits
	distU = distU * distU / varX_InTxtUnits;
	distV = distV * distV / varY_InTxtUnits;
	return exp((-distU - distV)/2.0f);
}


// Reproduces the dtft by convolving the dft with a gaussian window
// retrieve the approximated dft value of our height field
// from tIdx-th precomputed dft image at position (u,v)
// and convolve it by a gaussian window with window size
// according to the provided spatial variance sigma_s
// @param lookupCoord: perform a lookup in dft images at this position.
// @param tIdx:int which dft image should be used.
// @return reconstructed dtft:vec2f from tIdx-th taylor term.
vec2 getNthDTFTtermAt(vec2 lookupCoord, int tIdx){
	const int winW = 1;
	
	// These are frequency increments
	int baseImgCoordX = int(floor(centerU + lookupCoord.x*fftWW));
	int baseImgCoordY = int(floor(centerV + lookupCoord.y*fftHH));
	
	vec3 dftf = vec3(0.0f);
	
	// the following is a work around to have fixed number of operations
	// for each pixel
	if(baseImgCoordX < winW){
		baseImgCoordX = winW;
	}
	
	if(baseImgCoordY < winW){
		baseImgCoordY = winW;
	}
	
	if(baseImgCoordX + winW + 1 >  fftWW - 1){
		baseImgCoordX = fftWW - 1 - winW - 1;
	}
	
	if(baseImgCoordY + winW + 1 >  fftHH - 1){
		baseImgCoordY = fftHH - 1 - winW - 1;
	}
	
	// iterate over an winW neighborhood of (baseImgCoordX,baseImgCoordY)
	// for performing the convolution with the gaussian window
	for(float i = (baseImgCoordX-winW); i <= (baseImgCoordX + winW); i++){
		for(float j = (baseImgCoordY - winW); j <= (baseImgCoordY + winW); j++){
			vec3 texIdx = vec3(0.0f);
			float distU = float(i) - centerU - lookupCoord.x*float(fftWW);
			float distV = float(j) - centerV - lookupCoord.y*float(fftHH);
			
			// since image coordinates are in [0,1]
			// we have to map the current pixel location (i,j) 
			// into appropriate lookup coordinates being within this range.
			texIdx.x = float(i)/float(fftWW - 1);
			texIdx.y = float(j)/float(fftHH - 1);
			texIdx.z = float(tIdx);
			
			// lookup dft terms and then rescale with 
			// their corresponding extrema them (since they are normalized)
			vec3 dftVal = texture2DArray(TexArray, texIdx).xyz;			
			dftVal = rescaleXYZ(dftVal.x, dftVal.y, 0.0, tIdx);
			dftVal *= getGaussWeightAtDistance(distU, distV);
			
			// reconstruct dtft
			dftf += dftVal;
		}
	}
	
	// onlt real and imaginary part are required. Third term is a waste
	return (dftf.xy); 	
}

// get the CIE XYZ color matching-function weights for a given wavelength
// by performing a lookup in a color table storing these XYZ values plus the reflectance.
// @param lambda:int wavelength in nanometer units
// @return [CIE_XYZ, reflectance]:vec4f for given wavelength
vec4 getColorMatchingFunctionWeights(float lambda){
	float alpha  = (lambda - LMIN)/delLamda;
	int lIdx = int(floor(alpha));
	alpha = alpha - lIdx;
	return brdf_weights[lIdx] * (1-alpha) + brdf_weights[lIdx+1] * alpha; 
}

// compute current CIE XYZ color contribution for a given (u,v,w)
// which is the difference between the incident light and viewing direction
// i.e. the phase difference used as the position of the emitted 2ndary wavelet.
// By performing an uniform sampling over wavelength space Lambda 
// using a predefined wavelength step size
// relying on our Taylor series approximation in order to 
// approximate the DTFT terms.
// note that all derivations are according to the DTFT
// @param uu:float 1st component of (u,v,w)
// @param vv:float 2nd component of (u,v,w)
// @param ww:float 3rd component of (u,v,w)
// @return CIE XYZ color contribution:vec4f of (uu,vv,ww) with alpha
vec3 getXYZContributionForPosition(float uu, float vv, float ww){
	vec3 xyzContributionAtUVW = vec3(0.0f);	
	float taylorCoeff = 1.0f;
	float xNorm = 0.0f; 
	float yNorm = 0.0f; 
	float zNorm = 0.0f;
	float lambdaStep = 5.0;
	
	// integration over wavelength spectrum: flss uniform sampling
	for(float lambda = LMIN; lambda <= LMAX; lambda = lambda + lambdaStep){	
		
		// lookup color weight
		vec4 xyzColorWeights = getColorMatchingFunctionWeights(lambda);
		
		// compute normalization coefficients
		float specV = xyzColorWeights.w;
		xNorm += specV*xyzColorWeights.x;
		yNorm += specV*xyzColorWeights.y;
		zNorm += specV*xyzColorWeights.z;
		
		// base lookup coordinates: according to technical details section.
		vec2 lookupCoord = getLookupCoord(uu, vv, lambda);	
		vec2 approxFT = vec2(0.0f);
		
		// taylor approximation of DFT term: 
		// DFT = sum_n=0^N (k*w)^n / n! DFT_n 
		// use MAX_TAYLORTERMS precomputed dft images, 
		// i.e. use image 0:MAX_TAYLORTERMS-1
		for(int taylorTermIdx = 0; taylorTermIdx < MAX_TAYLORTERMS; taylorTermIdx++){
			if (0 == taylorTermIdx){
				taylorCoeff = 1.0f;
			} else {
				float currenTaylorCoeff = (ww * 2.0 * PI * pow(10.0f, 3.0f)) / (lambda * taylorTermIdx);
				// ensures numerical stability for computing n!
				// using a horner schema approach
				taylorCoeff *= currenTaylorCoeff;
			}	
			vec2 dtftCoeff = getNthDTFTtermAt(lookupCoord, taylorTermIdx);
			approxFT += taylorCoeff * dtftCoeff;
		}
		// squared magnitue (intensity) of dtft of n-th Taylor iteration
		float emittedIntensity = approxFT.x * approxFT.x + approxFT.y * approxFT.y;
		
		// corresponding CIE XYZ contribution
		xyzContributionAtUVW.x += emittedIntensity * xyzColorWeights.x;
		xyzContributionAtUVW.y += emittedIntensity * xyzColorWeights.y;
		xyzContributionAtUVW.z += emittedIntensity * xyzColorWeights.z;
	}
	
	// relative reflectance according to chapter derivation
	xyzContributionAtUVW.x /= xNorm;
	xyzContributionAtUVW.y /= yNorm;
	xyzContributionAtUVW.z /= zNorm;

	return xyzContributionAtUVW;
}

// Models the effect of diffraction using our snake mesh
void mainRenderGeometry(){
	initialzeConstants();
	float thetaR = asin(sqrt(o_org_pos.x*o_org_pos.x + o_org_pos.y*o_org_pos.y ));
	float phiR = atan(o_org_pos.y, o_org_pos.x);

    vec3 N = normalize(o_normal);
    vec3 T = normalize(o_tangent);
  
	// directional light source
	vec3 Pos =  normalize(o_pos); 
	vec3 lightDir =  normalize(o_light);
	
	// shadowing facto: beam of light is blocked by V cave
	float shadowF = getShadowMaskFactor(lightDir, Pos);
	
	// from vertex shader
	float uu = lightDir.x - Pos.x;
	float vv = lightDir.y - Pos.y;
	float ww = lightDir.z - Pos.z;

	vec3 color = getXYZContributionForPosition(uu, vv, ww);
	color = color*gainFactor(lightDir, Pos)*1000.0*shadowF;
	color = getBRDF_RGB_T_D65(M_Adobe_XRNew, color);
	
	// debug
	if(isnan(color.x*color.y*color.z)){
		color.x = 1.0;
		color.y = 0.0;
		color.z = 0.0;
	}
	
	float diffuseL = 0.0f;	
	if(Pos.z < 0.0  || dot(-lightDir, N) < 0.0){
		diffuseL = 0.0;
	}else{
		diffuseL = dot(-lightDir, N);
	}
	
	vec4 tex = vec4(0.0f);
	tex = texture2D(bodyTexture, frag_texcoord);

	float alpha = getFresnelFactorAbsolute(lightDir, Pos);	
	alpha = (alpha > 1.0f)? 1.0 : alpha;

	
	float diffW = 0.1f; 
	float gamma = 2.5; 
	tex.xyz = gammaCorrect(tex.xyz ,1.0f/1.0);
	vec3 texturedColor = gammaCorrect((1-diffW)*(color + (1-alpha) * tex.xyz *diffuseL) + tex.xyz * diffW, gamma);
	frag_shaded = vec4(gammaCorrect(color, gamma), 1.0);
//	frag_shaded = vec4(texturedColor, 1.0); // when we want to blend the diffraction color with textures
}

//Models the effect of diffraction using BRDF maps.
void mainBRDFMap(){
	initialzeConstants();
	float thetaR = asin(sqrt(o_org_pos.x*o_org_pos.x + o_org_pos.y*o_org_pos.y ));
	float phiR = atan(o_org_pos.y, o_org_pos.x);
	vec3 k1 = vec3(0.0f);
	vec3 k2 = vec3(0.0f);
	
	// using spherical coordinates
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

	vec3 color = getXYZContributionForPosition(uu, vv, ww);
	color = color*gainFactor(k1, k2)*150.0*shadowF;
	color = getBRDF_RGB_T_D65(M_Adobe_XRNew, color);
	
	if(isnan(color.x*color.y*color.z)){
		color.x = 1.0;
		color.y = 0.0;
		color.z = 0.0;
	}
	
	frag_shaded = vec4(gammaCorrect(color, 2.5), 1.0);
}

void main(){
	if(shouldRenderBrdfMap == 1){
		mainBRDFMap();
	}else{
		mainRenderGeometry();
	}
}