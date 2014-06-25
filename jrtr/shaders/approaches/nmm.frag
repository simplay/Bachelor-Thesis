/*
 * NMM fragment shader which renders the effect of diffraction
 * on a given grating structure.
 * This shader performs an non-uniform sampling over the wavelength spectrum [380nm,780nm]
 * For further information please read the chapter 'implementation' of my thesis.
 */

#version 150
#extension GL_EXT_gpu_shader4 : enable

// substitutes
#define MAX_LIGHTS 1
#define MAX_TAYLORTERMS 31
#define MAX_WFACTORS    78
#define MAX_WEIGHTS 401

uniform float LMIN;
uniform float LMAX;
uniform float delLamda;

uniform float minspacer;
uniform float maxspacer;
uniform float correction;
// Uniform variables, passed in from host program via suitable
uniform int debugTxtIdx;
uniform int useOptSampling;
uniform sampler2DArray TexArray;
uniform sampler2D bodyTexture;
// uniform sampler2D bumpMapTexture;

uniform int fftHH; // height of FFT Image
uniform int fftWW; // width of FFT Image
uniform int approxSteps;
uniform int shouldRenderBrdfMap;
uniform vec4 cop_w;

uniform vec3 radianceArray[MAX_LIGHTS];
uniform vec4 brdf_weights[MAX_WEIGHTS];
uniform vec4 directionArray[MAX_LIGHTS];

uniform vec4 scalingFactors[MAX_WFACTORS];
// uniform vec4 global_extrema[1];
uniform vec4 camPos;
uniform int drawTexture;
uniform float dimX;
uniform float dx;
uniform float t0;
uniform float dHPix;
uniform float thetaI;
uniform float phiI;
uniform float brightness;
uniform sampler2DArray lookupText;
// Variables passed in from the vertex shader
in vec2 frag_texcoord;
in vec4 light_direction[MAX_LIGHTS];
in vec4 normal_out;
in vec4 eyeVector;
// in vec4 col;
in vec3 o_org_pos;
in vec3 o_pos;
in vec3 o_light;
in vec3 o_normal;
in vec3 o_tangent;
// Output variable, will be written to framebuffer automatically
out vec4 frag_shaded;

const float PI = 3.14159265358979323846264;
const float phiRect = // 0.87285485835042309f; // For Elaphe650
	// 0.0f;
	2.4436511851453195f;  // this one works for Elaphe650with fingers pointing
							// downwards in the .mat file



// transformation constant
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

void mainBRDFMap();
void mainRenderMesh();

float varX_InTxtUnits;
float varY_InTxtUnits;

// uniform const float dH = 1.0e-7;
float dH = 0.0f;
float centerU;
float centerV;
float lambda_min = LMIN*pow(10.0, -9.0);
float lambda_max = LMAX*pow(10.0, -9.0);

// increased accuracy of sampling
float getMask() {
	if(minspacer-maxspacer < 50) {
		return 10;
	} else {
		return 1;
	}
}

// compute N_min and N_max used used as integration range
// for performing our NMM sampling along a given direction.
// this method does take into account negaive values
// we are kind of normalizing these N values
// such taht we only have to deal with positve values
// otherwise we would have to invert our integration loop
// min gets max and vcse versa.
// @t is either equals u or v of (u,v,w)
// @return [N_min, N_max] along direction of t.
vec2 compute_N_min_max(float t){
	// default case if t == 0 otherwise override it.
	float N_min = 0.0;
	float N_max = 0.0;
	float mask = getMask();
	if(t > 0.0){
		N_min = ceil(mask*maxspacer*t)/mask;
		N_max = floor(mask*minspacer*t)/mask;
	}else if(t < 0.0){
		N_min = ceil(mask*minspacer*t)/mask;
		N_max = floor(mask*maxspacer*t)/mask;
	}
	
	return vec2(N_min, N_max);
//	return vec2(dx/N_min, dx/N_max);
}

// similar as #compute_N_min_max but used when we refined our
// sampling range. one of our adaptive methods in order to 
// increase the sampling accuracy.
// @t is either equals u or v of (u,v,w)
// @own_mask determining refinement scale. 
//           values in e-k, where k is a positive nat. number
// @return [N_min, N_max] refined along direction of t.
vec2 compute_N_min_max_own_mask(float t, float own_mask){
	// default case if t == 0 otherwise override it.
	float N_min = 0.0;
	float N_max = 0.0;

	if(t > 0.0){
		N_min = ceil(own_mask*maxspacer*t)/own_mask;
		N_max = floor(own_mask*minspacer*t)/own_mask;
	}else if(t < 0.0){
		N_min = ceil(own_mask*minspacer*t)/own_mask;
		N_max = floor(own_mask*maxspacer*t)/own_mask;
	}
	
	return vec2(N_min, N_max);
}

//computes the frequency variance used for the gaussian window
//for the dtft reconstruction during the windowing approach.
//furthermore, this method computes the center position of the dft images
//used for computing the image lookup coordinates.
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

//perform a color space transformation:
//from CIE XYZ to RGB using the illuminant D65 
//in order to define the white point.
vec3 getBRDF_RGB_T_D65(mat3 T, vec3 brdf_xyz){
	vec3 D65 = vec3(0.95047, 1.0, 1.08883);
	vec3 output = vec3(0.0);
	vec3 D65BRDF = vec3(brdf_xyz.x*D65.x, brdf_xyz.y*D65.y, brdf_xyz.z*D65.z);
	output.x = dot(D65BRDF, T[0]);
	output.y = dot(D65BRDF, T[1]);
	output.z = dot(D65BRDF, T[2]);
	return output;
}

//a filter which models the occlusion of the 
//given light source which occurs since our 
//surface is assumed to be v-caved shaped.
//thus some light rays may no reach a given point
//on a surface. also models masking effects
//similarly a viewer cannot fully see all the points 
//on a given v-caved surface.
//@param K1 incident light direction
//@param K2 viewing direciton
//@return final amount of light contribution at a given point
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

//compute fresnel coefficient according to Schlick's approximation
//quantitative description of transmissed and reflectied fraction of light
//when a beam of light hits a plane surface.
//describe what fraction of the light is reflected and what fraction is refracted
//see: http://en.wikipedia.org/wiki/Fresnel_equations
//@param K1 incident light direction
//@param K2 viewing direction
//@return reflected amount of light
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

//rescales given normalized CIE XYZ colors back to heir original scale
//using their extreme values.
//@param X 1st CIE XYZ colorvalue (normalized)
//@param Y 2nd CIE XYZ colorvalue (normalized)
//@param Z 3rd CIE XYZ colorvalue (normalized)
//@param index which dft terms should be used
//     in oder to appropriately lookup their
//     extreme values for rescaling them.
//@return rescaled CIE XYZ colors
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

//gain factor as described in the thesis
//consisting of C = F*G/w^2
//where F is the fesnel number
//    G is the geometric term
//    w is the 3rd component of (u,v,w)
//for further details I refer to my thesis,
//in the chapter 'derivations'.
//@param incident light direction
//@param viewing direction
//@return F*G/w^2
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

//performs gamma correction applied on a vector containing rgb colors
//see http://en.wikipedia.org/wiki/Gamma_correction
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

//computes lookup coordinates used during sampling the spectrum.
//dh:float denotes the length of one particular pixel
//for further information please refere to the thesis, 
//section lookup coorinates of chapter 'implementation'
//@param uu 1st component of (u,v,w)
//@param vv 2nd component of (u,v,w)
//@returns texture coordinates [0,1]^2 :vec2 for given uu vv
vec2 getLookupCoord(float uu, float vv, float lambda){
	float dH = t0;
	vec2 coord = vec2(0.0, 0.0);
	coord.x = uu * 1e9 *dH / lambda ;
	coord.y = vv * 1e9 *dH / lambda ;
	return coord;
}

//windowing function used for reconstructing the DTFT
//a Gaussian window defined by sigma_f 
//(see sections 'coherence' and 'reconstruct dtft from dft' in thesis)
//distance to pixel (i,j) is used as mean value
//contribution weight of pixel (i,j) having with 
//distance vector (dU,dV) = (baseU, BaseV) - (i,j)
//@param distU 1st component of distance vec
//@param distV 2nd component of distance vec
//@return gaussian window weight for given distance determined by 
//		   pixel location (i,j) distance to its base.
float getGaussWeightAtDistance(float distU, float distV){
	// note that distU and distV are in textureCoordinateUnits
	distU = distU * distU / varX_InTxtUnits;
	distV = distV * distV / varY_InTxtUnits;
	return exp((-distU - distV)/2.0f);
}

//Reproduces the dtft by convolving the dft with a gaussian window
//retrieve the approximated dft value of our height field
//from tIdx-th precomputed dft image at position (u,v)
//and convolve it by a gaussian window with window size
//according to the provided spatial variance sigma_s
//@param lookupCoord: perform a lookup in dft images at this position.
//@param tIdx:int which dft image should be used.
//@return reconstructed dtft:vec2f from tIdx-th taylor term.
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

//get the CIE XYZ color matching-function weights for a given wavelength
//by performing a lookup in a color table storing these XYZ values plus the reflectance.
//@param lambda:int wavelength in nanometer units
//@return [CIE_XYZ, reflectance]:vec4f for given wavelength
vec4 getColorMatchingFunctionWeights(float lambda){
	float alpha  = (lambda - LMIN)/delLamda;
	int lIdx = int(floor(alpha));
	alpha = alpha - lIdx;
	return brdf_weights[lIdx] * (1-alpha) + brdf_weights[lIdx+1] * alpha; 
}

// compute current CIE XYZ color contribution for a given (u,v,w)
// which is the difference between the incident light and viewing direction
// i.e. the phase difference used as the position of the emitted 2ndary wavelet.
// By performing an non-uniform sampling over wavelength space Lambda,
// (i.e. performing a uniform sampling over the wavenumber space)
// for further information please have a look in my thesis,
// in chapter 'implementation', section 'technical details'
// using a predefined wavelength step size
// relying on our Taylor series approximation in order to 
// approximate the DTFT terms.
// note that all derivations are according to the DTFT
// @param uu:float 1st component of (u,v,w)
// @param vv:float 2nd component of (u,v,w)
// @param ww:float 3rd component of (u,v,w)
// @return CIE XYZ color contribution:vec4f of (uu,vv,ww) with alpha
vec3 getXYZContributionForPosition(float uu,float vv,float ww){
	vec3 xyzContributionAtUVW = vec3(0.0f);
	float taylorCoeff = 1.0f;
	float fftMag = 0.0f;	
	float xNorm = 0.0f;
	float yNorm = 0.0f;
	float zNorm = 0.0f;
	float specSum = 0.0f;	
	
	float lambdaStep = 5.0;	
	vec2 N_u = compute_N_min_max(uu);
	vec2 N_v = compute_N_min_max(vv);
	float lower_u = N_u.x;
	float upper_u = N_u.y;
	float lower_v = N_v.x;
	float upper_v = N_v.y;
	
	float dist2Zero = sqrt(uu*uu + vv*vv);
	float epsSQT = 0.015; // blaze
	if(minspacer-maxspacer < 50) epsSQT = 0.022;
	// heuristics
	if(dist2Zero <= epsSQT){
		xyzContributionAtUVW.x = 1.0;
		xyzContributionAtUVW.y = 1.0;
		xyzContributionAtUVW.z = 1.0;
	}else{
		float maskStep = (1.0/getMask());
		
		// close to center regions apply an adaptive sampling strategy
		if(useOptSampling == 1) {
			float boundary = epsSQT+0.07;
			if(minspacer-maxspacer > 50) boundary = epsSQT+0.02;
			if(dist2Zero <= boundary){ // for elaphe
				float newMask = getMask()*10.0;
				float factor = 1;
				if ( minspacer-maxspacer > 50 ) factor = 5;
				maskStep = (1.0/newMask)*factor; // *5 for elaphe
				N_u = compute_N_min_max_own_mask(uu, newMask);
				N_v = compute_N_min_max_own_mask(vv, newMask);
				lower_u = N_u.x;
				upper_u = N_u.y;
				lower_v = N_v.x;
				upper_v = N_v.y;
			}
		} else {
			maskStep = 0.1;
		}

		// integration over wavenumber range: NMM non-uniform wavelength sampling
		// sampling along u direction
		for(float nu = lower_u; nu < upper_u; nu = nu+maskStep){
			
			// recomputed wavelength from given wavenumber k_u
			float lambda = ((uu*(correction*dx))/(nu))*1000.0;

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
				if(0 == taylorTermIdx) {
					taylorCoeff = 1.0f;
				} else {
					float currenTaylorCoeff = (ww*2.0*PI*pow(10.0f, 3.0f))/(lambda*taylorTermIdx);
					// ensures numerical stability for computing n!
					// using a horner schema approach
					taylorCoeff *= currenTaylorCoeff;
				}
			
				vec2 dtftCoeff = getNthDTFTtermAt(lookupCoord, taylorTermIdx);
				approxFT += taylorCoeff * dtftCoeff;
			}
			
			// squared magnitue (intensity) of dtft of n-th Taylor iteration
			float emittedIntensity = approxFT.x * approxFT.x + approxFT.y * approxFT.y;
			
			// corresponding CIE XYZ contribution along u direction
			xyzContributionAtUVW.x += emittedIntensity * xyzColorWeights.x;
			xyzContributionAtUVW.y += emittedIntensity * xyzColorWeights.y;
			xyzContributionAtUVW.z += emittedIntensity * xyzColorWeights.z;
		}
		
		// integration over wavenumber range: NMM non-uniform wavelength sampling
		// sampling along v direction
		for(float nv = lower_v; nv < upper_v; nv = nv+maskStep){
			
			// recomputed wavelength from given wavenumber wavenumber k_v
			float lambda = ((vv*(correction*dx))/(nv))*1000.0;
			
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
				if(0 == taylorTermIdx){
					taylorCoeff = 1.0f;
				}else {
					float currenTaylorCoeff = (ww*2.0*PI*pow(10.0f, 3.0f))/(lambda*taylorTermIdx);
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
		xyzContributionAtUVW.x /= xNorm ;
		xyzContributionAtUVW.y /= yNorm ;
		xyzContributionAtUVW.z /= zNorm ;
	}

	return xyzContributionAtUVW;
}

//Models the effect of diffraction using our snake mesh
void mainRenderGeometry(){
	initialzeConstants();
	 
    vec3 N = normalize(o_normal);
    vec3 T = normalize(o_tangent);

	// directional light source
	vec3 Pos = normalize(o_pos); 
	vec3 lightDir = normalize(o_light); 
	
	// shadowing facto: beam of light is blocked by V cave
	float shadowF = getShadowMaskFactor(lightDir, Pos);
	
	float uu = lightDir.x - Pos.x;
	float vv = lightDir.y - Pos.y;
	float ww = lightDir.z - Pos.z;
	
	vec3 color = getXYZContributionForPosition( uu, vv, ww);	
	color = color * gainFactor(lightDir,Pos)*brightness*shadowF;
	color = getBRDF_RGB_T_D65(M_Adobe_XRNew, color);

	frag_shaded = vec4(gammaCorrect(color, 2.5), 1.0);
}

//Models the effect of diffraction using BRDF maps.
void mainBRDFMap(){
	initialzeConstants();
	 
	float thetaR = asin(sqrt(o_org_pos.x * o_org_pos.x + o_org_pos.y * o_org_pos.y ));
	float phiR = atan(o_org_pos.y, o_org_pos.x);

	vec3 k1 = vec3(0.0f);
	vec3 k2 = vec3(0.0f);	
	
	k1.x = - sin(thetaI)*cos(phiI);
	k1.y = - sin(thetaI)*sin(phiI);
	k1.z = - cos(thetaI);
	
	k2.x = sin(thetaR)*cos(phiR);
	k2.y = sin(thetaR)*sin(phiR);
	k2.z = cos(thetaR);
	
	float shadowF = getShadowMaskFactor(k1, k2);
	float uu = k1.x - k2.x;
	float vv = k1.y - k2.y;
	float ww = k1.z - k2.z;

	vec3 color = getXYZContributionForPosition( uu, vv, ww);
	color = color * gainFactor(k1, k2)*brightness*shadowF;
	color = getBRDF_RGB_T_D65(M_Adobe_XRNew, color);
	
	frag_shaded = vec4(gammaCorrect(color ,2.5), 1.0);
}

void main(){
	if(shouldRenderBrdfMap == 1){
		mainBRDFMap();
	}else{
		mainRenderGeometry();
	}
}