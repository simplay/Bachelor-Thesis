// The MIT License (MIT)
//
// Copyright (c) <2014-2015> <Michael Single>
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.


/*
 * GEM fragment shader which renders the effect of diffraction
 * on a given grating structure. Implemention of Stam's approach provided by Nvidia Gem.
 * This shader performs an uniform sampling over the wavelength spectrum [380nm,780nm]
 * For further information please visit 
 * http://http.developer.nvidia.com/GPUGems/gpugems_ch08.html
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
uniform int approxSteps;;
uniform int isCone;
uniform int shouldRenderBrdfMap;
uniform float LMIN;
uniform float LMAX;
uniform float delLamda;
uniform float dimX;
uniform float t0;
uniform float thetaI;
uniform float phiI;
uniform float bruteforcespacing;
uniform float brightness;

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

// function declarations
void mainBRDFMap();
void mainRenderGeometry();
void coneMain();
void gemMain();
vec2 getNMMfor(float, float);
vec3 sumContributionAlongDir(vec2, float, float);

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

// heuristic to assign a certain vec3 a rgb color
// assumption: colormatching functions are piecewise quadratic functions
// see: http://http.developer.nvidia.com/GPUGems/gpugems_ch08.html
// @param x some vec3 computed by some by a certain heuristic
// @return rgb color
vec3 blend3 (vec3 x){
	vec3 y = 1.0 - x * x;
	y = max(y, vec3 (0, 0, 0));
	return (y);
}

//Models the effect of diffraction using our snake mesh
void mainRenderGeometry(){
    vec3 N = normalize(o_normal);
    vec3 T = normalize(o_tangent);

	// directional light source
	vec3 Pos =  normalize(o_pos); 
	vec3 lightDir =  normalize(o_light);
	
	float shadowF = getShadowMaskFactor(lightDir, Pos);
	float u = lightDir.x - Pos.x;
	float v = lightDir.y - Pos.y;
	float w = lightDir.z - Pos.z;
	
	float r = 10.50; // snake
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
//	float d = 2.5;
	float d = 1.55227;
	
	float eps = 0.0;
	float uuu = abs(u);
	float vvv = abs(v);
	
	vec2 uNMM = getNMMfor(uuu, d);
	vec2 vNMM = getNMMfor(vvv, d);
	
	float dist2Zero = sqrt(uuu*uuu + vvv*vvv);
	
	cdiff.xyz += sumContributionAlongDir(uNMM, uuu, d);
	if(dist2Zero < 0.01) {
		eps = 1.0;
	}

	cdiff.xyz += sumContributionAlongDir(vNMM, vvv, d);
	if(vvv < 0.01) eps = 0.0;
	if(vvv < 0.005) {
		eps = 1.0;
	}
		
	vec3 color = 0.1*cdiff.xyz + eps;
	color = getBRDF_RGB_T_D65(M_Adobe_XRNew, color);
	float bri = (brightness/8000)*2.0;
	float fac = (getShadowMaskFactor(lightDir, Pos)*gainFactor(lightDir, Pos));
	fac = (fac < 0.0) ? 0.0 : fac*bri;
	frag_shaded = anis*vec4(gammaCorrect(fac*color, 2.3), 1.0);
}

//Models the effect of diffraction using BRDF maps.
void mainBRDFMap(){
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

    vec3 N = normalize(o_normal);
    vec3 T = normalize(o_tangent);

	float shadowF = getShadowMaskFactor(k1, k1);
	float r = 1.50; // roughness factor for: 1.5f seems to look nice
	float e = r * u / w;
	float c = exp(-e * e);
	float e2 = 3.0 * u / w;
	float c2 = exp(-e * e);
	
	vec4 anis = vec4(1,1,1,1) * vec4(c, c, c, 1);
	vec4 anis2 = vec4(1,1,1,1) * vec4(c2, c2, c2, 1);
	
	float lambda_min = 0.38; // 400nm red
	float lambda_max = 0.78; // 700nm blue
	
	vec4 cdiff = vec4(0, 0, 0, 1);
	
	// 2.5microns
	float d = 2.5;

	
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
		
	vec3 color = cdiff.xyz/10.0 + eps;
	float value = (getShadowMaskFactor(k1, k2)*gainFactor(k1, k2));
	value = (value < 0.0) ? 0.0 : value;
	float bri = (brightness/8000)*2.0;
	vec3 scaledColor = bri*color*value;
	frag_shaded = anis*vec4(gammaCorrect(scaledColor, 2.6), 1.0);
}

// non-uniform wavelength spectrum sampling along a certain direction
// @param bounderies integration range [N_min, N_max]
// @param dir direction along we perform the sampling (either u or v)
// @param spacing periodicity constant
// @returns XIE XYZ color contribution along a certain direction.
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
// @param t direction along we perform the sampling (either u or v)
// @param spacing periodicity constant
// @return [N_min, N_max] wavenumber integration range.
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
	if(shouldRenderBrdfMap == 1){
		mainBRDFMap();
	}else{
		mainRenderGeometry();
	}
}
