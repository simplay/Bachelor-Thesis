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

// Uniform variables, passed in from host program via suitable
uniform int debugTxtIdx;

uniform sampler2DArray TexArray;
uniform sampler2D bodyTexture;
// uniform sampler2D bumpMapTexture;

uniform int fftHH; // height of FFT Image
uniform int fftWW; // width of FFT Image
uniform int approxSteps;
uniform vec4 cop_w;

uniform vec3 radianceArray[MAX_LIGHTS];
uniform vec4 brdf_weights[MAX_WEIGHTS];
uniform vec4 directionArray[MAX_LIGHTS];

uniform vec4 scalingFactors[MAX_WFACTORS];
// uniform vec4 global_extrema[1];
uniform vec4 camPos;
uniform int drawTexture;
uniform float dimX;
uniform float t0;
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

// paramters for BRDF plots
// const float thetaI = -PI/4.0f;

const float phiRect = // 0.87285485835042309f; // For Elaphe650
	// 0.0f;
	2.4436511851453195f;  // this one works for Elaphe650with fingers pointing
							// downwards in the .mat file

// const float phiI = 0.0f;

uniform float thetaI;
uniform float phiI;


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


float varX_InTxtUnits;
float varY_InTxtUnits;

// uniform const float dH = 1.0e-7;
float dH = 0.0f;
float orgU;
float orgV;
float lambda_min = LMIN*pow(10.0, -9.0);
float lambda_max = LMAX*pow(10.0, -9.0);

float GetLightNormalCos()
{
   vec3 N = normalize(o_normal);

   vec3 L =  normalize(o_light); 	
   
   return abs( - N.x * L.x - N.y * L.y - N.z * L.z);

}

vec2 compute_N_min_max(float t){
	// default case if t == 0 otherwise override it.
	float N_min = 0.0;
	float N_max = 0.0;
	
	if(t > 0.0){
		N_min = ceil((dimX*t) / lambda_max);
		N_max = floor((dimX*t) / lambda_min);
	}else if(t < 0.0){
		N_min = ceil((dimX*t) / lambda_min);
		N_max = floor((dimX*t) / lambda_max);
	}
	return vec2(N_min, N_max);
}


void setVarXY()
{
	
	
	dH = t0;//scalingFactors[0].w;
	
	// scalingFactors[0].w = 15e-6/256;
	
	float sigSpatial = 65e-6/4.0f;
	// float sigSpatial = 15e-6/4.0f;
	
	// temporary sigma
	float sigTemp;
	
	sigTemp = 0.5 / PI ;
	// sigTemp = 1.0;
	sigTemp = sigTemp /sigSpatial;
	// sigTemp = 1.0f / sigSpatial;
	
	// sigTemp = sigTemp / GetLightNormalCos();
	
	sigTemp = sigTemp * dH;
	
	varX_InTxtUnits = sigTemp * sigTemp * fftWW * fftWW ; 
	varY_InTxtUnits = sigTemp * sigTemp * fftHH * fftHH;
	
	// Set coordinates for the Origin
	if (fftWW % 2 == 0)
		orgU = float(fftWW )/ 2.0f  ; // -2 dur to rotational lochay
	else
		orgU = float(fftWW - 1.0) / 2.0f  ;
		
	if (fftHH % 2 == 0)
		orgV = float(fftHH  )/2.0f  ;
	else
		orgV = float(fftHH - 1.0)/2.0f ;
	
}


vec3 getBRDF_RGB_T_D65(mat3 T, vec3 brdf_xyz){
	// vec3 D65 = vec3(0.95047, 1.0, 1.08883);
	vec3 D65 = vec3(1.0, 1.0, 1.0);

	vec3 output = vec3(0.0);
	vec3 D65BRDF = vec3(brdf_xyz.x*D65.x, brdf_xyz.y*D65.y, brdf_xyz.z*D65.z);
	
	// output.x = D65BRDF.x * T[0][0] + D65BRDF.y * T[0][1] + D65BRDF.z *
	// T[0][2] ;
	// output.y = D65BRDF.x * T[1][0] + D65BRDF.y * T[1][1] + D65BRDF.z *
	// T[1][2] ;
	// output.z = D65BRDF.x * T[2][0] + D65BRDF.y * T[2][1] + D65BRDF.z *
	// T[2][2] ;
	
	output.x = dot(D65BRDF, T[0]);
	output.y = dot(D65BRDF, T[1]);
	output.z = dot(D65BRDF, T[2]);
	
	return output;
}

vec3 rotateRodrigues(vec3 vecV, vec3 axisV, float phi)
{
	vec3 vecR = vec3(0.0);
	vec3 crossV = cross(axisV, vecV);
	
	float dotScale = axisV.x *vecV.x + axisV.y * vecV.y + axisV.z * vecV.z; 
	
	dotScale = dotScale * (1 - cos(phi));
	
	vecR.x = vecV.x * cos(phi) + crossV.x * sin (phi) + axisV.x * dotScale;
	vecR.y = vecV.y * cos(phi) + crossV.y * sin (phi) + axisV.y * dotScale;
	vecR.z = vecV.z * cos(phi) + crossV.z * sin (phi) + axisV.z * dotScale;
	
	return vecR;
}

float getFresnelFactor(vec3 K1, vec3 K2)
{
	float nSkin = 1.5;
	float nK = 0.0;
	
	vec3 hVec = -K1 + K2;
		 
	hVec = normalize(hVec);
	 
	float cosTheta = dot(hVec,K2);	
	
	float fF = (nSkin - 1.0);
	
	fF = fF * fF;
	
	float R0 = fF + nK*nK;
	if (cosTheta > 0.999)
		fF = R0;
	else
		fF = fF + 4*nSkin*pow(1- cosTheta,5.0) + nK*nK;
	
	// do this division if its not on relative scale
	// fF = fF/ ((nSkin + 1.0)* (nSkin + 1.0) + nK*nK);
	
	return fF/R0;
}

float getFresnelFactorAbsolute(vec3 K1, vec3 K2)
{
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

float gainF(vec3 K1, vec3 K2)
{
	float gF = 1 - dot(K1,K2);
	
	 gF = gF*gF;
	 
	 float ww = K1.z - K2.z;
	 
	 
	 if (K1.z > 0.0 || K2.z < 0.0)
	 {
		 return 0.0; // This side is not visible
	 }
	 
	 ww = ww * ww;
	 
	 if (ww < pow(10.0,-4.0))
	 {
		 // return 0.0; // Shadowing Function
	 }
	 
	 gF = gF / K2.z;
	 gF = gF / ww;
	 
	 float fFac = getFresnelFactor(K1, K2); 

	 return fFac*gF;
}


vec3 gammaCorrect(vec3 inRGB, float gamma)
{
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

/*
 * return 2D coordinates for given uu vv in 0.0.. 1.0, 0.0.. 1.0 scale i.e in
 * texture Coordniiates..
 */
vec2 getLookupCoord(float uu, float vv, float lambda)
{
	// every 4th element in scaling Factors in DH
	float dH = t0; //scalingFactors[0].w;
	vec2 coord = vec2(0.0f);
	
	/*
	 * float orgU; float orgV;
	 *  /* For an even N size Matlab fftshift has origin at 1 + N/2 in MATLAB
	 * coords i,e, at N/2 in C coords For an odd N size Matlab fftshift has
	 * origin at (N+1)/2 in MATLAB coords (not sure but need to verify) i,e, at
	 * (N-1)/2 in C coords /
	 * 
	 * 
	 * if (fftWW % 2 == 0) orgU = float(fftWW )/ 2.0f / float(fftWW - 1); // -2
	 * dur to rotational lochay else orgU = float(fftWW - 1.0) / 2.0f /
	 * float(fftWW - 1);
	 * 
	 * if (fftHH % 2 == 0) orgV = float(fftHH )/2.0f / float(fftHH - 1); else
	 * orgV = float(fftHH - 1.0)/2.0f / float(fftHH - 1);
	 * 
	 */
	coord.x = uu * 1e9 *dH / lambda ;
	coord.y = vv * 1e9 *dH / lambda ;
	
	return coord;
}

float getGaussWeightAtDistance(float distU, float distV)
{
	// note that distU and distV are in textureCoordinateUnits
	
	distU = distU * distU / varX_InTxtUnits;
	distV = distV * distV / varY_InTxtUnits;
	
	return exp((-distU - distV)/2.0f);
	//return 1.0f;
}



vec2 getFFTAt(vec2 lookupCoord, int tIdx)
{
	const int winW = 1;

	// These are frequency increments
	int anchorX = int(floor(orgU + lookupCoord.x * (fftWW - 0)));
	int anchorY = int(floor(orgV + lookupCoord.y * (fftHH - 0)));
	
	vec3 fftMag = vec3(0.0f);
	
	// the following is a work around to have fixed number of operations
	// for each pixel
	if (anchorX < winW)
		anchorX = winW;
	
	if (anchorY < winW)
		anchorY = winW;
	
	if (anchorX + winW + 1 >  fftWW - 1)
		anchorX = fftWW - 1 - winW - 1;
	
	if (anchorY + winW + 1 >  fftHH - 1)
		anchorY = fftHH - 1 - winW - 1;
	
	
	for (float i = (anchorX-winW); i <= (anchorX + winW + 1 ); ++i) {
		for (float j = (anchorY - winW); j <= (anchorY + winW + 1); ++j)
		{
			/*
			 * if (i < 0 || i >= fftWW || j < 0 || j >= fftHH) { continue; }
			 */
			// we have a valid lookup integet index into fft Images.
			
			vec3 texIdx = vec3(0.0f);
			
			// These distances are in pixel units in range 0 - 1
			// float distU = float(i) / float(fftWW - 1) - lookupCoord.x;
			// float distV = float(j) / float(fftHH - 1) - lookupCoord.y;
		
			float distU = float(i) - orgU - lookupCoord.x *float(fftWW - 0);
			float distV = float(j) - orgV - lookupCoord.y *float(fftHH - 0);
		
		
			texIdx.x = float(i)/ float(fftWW - 1);
			texIdx.y = float(j)/float(fftHH - 1);
			texIdx.z = float(tIdx);
			/*
			 * texIdx.x = float(i)/(fftWW - 1); texIdx.y = float(j)/(fftHH - 1);
			 * texIdx.z = (tIdx); no diffrence though
			 */
			
			vec3 fftVal = texture2DArray(TexArray, texIdx).xyz;
			
			fftVal = rescaleXYZ(fftVal.x, fftVal.y, 0.0, tIdx);
			fftVal = fftVal * getGaussWeightAtDistance(distU, distV);
			fftMag = fftMag + fftVal;
		}
	}
	
	// onlt real and imaginary part are required. Third term is a waste
	return (fftMag.xy); 
	// return 0.25f;
	
}

vec4 getClrMatchingFnWeights(float lVal)
{
	if (lVal < LMIN)
		lVal = LMIN;
	
	if (lVal >= LMAX)
		lVal = LMAX - delLamda/100000.0f; // just to ensure that the flooring
											// latches to the lower value
	
	float alpha  = (lVal - LMIN)/delLamda;
	
	int lIdx = int(floor(alpha));
	
	alpha = alpha - lIdx;

	return brdf_weights[lIdx] * (1-alpha) + brdf_weights[lIdx+1] * alpha; 
}


vec3 getRawXYZFromTaylorSeries(float uu,float vv,float ww)
{
	vec3 opVal = vec3(0.0f);
	
	float preScale = 1.0f;
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
	
	float lambda_lower_u = ((dimX*uu)/upper_u)*pow(10.0, 9.0);
	float lambda_upper_u = ((dimX*uu)/lower_u)*pow(10.0, 9.0);
	if(upper_u < 0.0){
		lambda_lower_u = ((dimX*uu)/lower_u)*pow(10.0, 9.0);
		lambda_upper_u = ((dimX*uu)/upper_u)*pow(10.0, 9.0);	
	}
	
	float lambda_lower_v = ((dimX*vv)/upper_v)*pow(10.0, 9.0);
	float lambda_upper_v = ((dimX*vv)/lower_v)*pow(10.0, 9.0);
	if(upper_v < 0.0){
		lambda_lower_v = ((dimX*vv)/lower_v)*pow(10.0, 9.0);
		lambda_upper_v = ((dimX*vv)/upper_v)*pow(10.0, 9.0);	
	}

	

//	float exodus = lambda_lower_a-lambda_upper_a;
	

	
	
	for(float lVal = lambda_lower_v; lVal <= lambda_upper_v; lVal = lVal+lambdaStep)
	{
		
		vec4 clrFn = getClrMatchingFnWeights(lVal);
		
		float specV = clrFn.w;
		// specSum = specSum + specV;
		
		
		xNorm = xNorm + specV*clrFn.x;
		yNorm = yNorm + specV*clrFn.y;
		zNorm = zNorm + specV*clrFn.z;
		
		vec2 lookupCoord = getLookupCoord(uu, vv, lVal);
		
		vec2 tempFFTScale = vec2(0.0f);
		
		for(int tIdx = 0; tIdx < MAX_TAYLORTERMS; ++tIdx)
		{
			if(0 == tIdx) {
				preScale = 1.0f;
			} else {
				float currS = ww * 2 * PI * pow(10.0f, 3.0f) / lVal / tIdx;
				preScale = preScale * currS;
			}
		
			vec2 fftCoef = getFFTAt(lookupCoord, tIdx);
			tempFFTScale = tempFFTScale + preScale * fftCoef;
		}
		
		float fftMagSqr = tempFFTScale.x * tempFFTScale.x + tempFFTScale.y * tempFFTScale.y;
		opVal.x = opVal.x + fftMagSqr * specV * clrFn.x;
		opVal.y = opVal.y + fftMagSqr * specV * clrFn.y;
		opVal.z = opVal.z + fftMagSqr * specV * clrFn.z;
	}
	
	opVal.x = opVal.x / xNorm ;
	opVal.y = opVal.y / yNorm ;
	opVal.z = opVal.z / zNorm ;

	return opVal;
}


void mainMain() 
{
	// setsF();
	setVarXY();
	 
    vec3 N = normalize(o_normal);
    vec3 T = normalize(o_tangent);

    
	// directional light source
	vec3 Pos =  normalize(o_pos); 
	vec3 lightDir =  normalize(o_light); 
	
	float uu = lightDir.x - Pos.x;
	float vv = lightDir.y - Pos.y;
	float ww = lightDir.z - Pos.z;

	// uu = uu/10;
	// vv = vv/10;
	
	
	vec3 totalXYZ = getRawXYZFromTaylorSeries( uu, vv, ww);
	
	
	totalXYZ = totalXYZ * gainF(lightDir,Pos)*100;
	// totalXYZ = totalXYZ * 10.0;
	
	totalXYZ = getBRDF_RGB_T_D65(M_Adobe_XRNew, totalXYZ);
	
	
	if (isnan(totalXYZ.x *totalXYZ.y *totalXYZ.z))
	{
		totalXYZ.x  = 1.0;
		totalXYZ.y  = 1.0;
		totalXYZ.z  = 0.0;
	}


	/*
	 * vec3 texIdx = vec3(0.0); texIdx.x = frag_texcoord.x; texIdx.y =
	 * frag_texcoord.y; texIdx.z = debugTxtIdx;
	 * 
	 * totalXYZ.x = float(texture2DArray(TexArray, texIdx).x)/1.0; totalXYZ.y =
	 * float(texture2DArray(TexArray, texIdx).y)/1.0; totalXYZ.z =
	 * float(texture2DArray(TexArray, texIdx).z)/1.0;
	 * 
	 */
	
	float diffuseL = 0.0f;
	
	if (Pos.z < 0.0  || dot(-lightDir, N) < 0.0)
		diffuseL = 0.0;
	else 
		diffuseL = dot(-lightDir, N);
	


	/*
	 * totalXYZ.x = uu*1; totalXYZ.y = vv*0; totalXYZ.z = 0.0;
	 */
	frag_shaded = vec4(gammaCorrect(totalXYZ,2.2)+0.05, 1.0);
	// frag_shaded = vec4(gammaCorrect(totalXYZ,1.0), 1.0);

	// frag_shaded = vec4(totalXYZ + vec3(0.1f,0.1f,0.1f), 1.0);
	
	// frag_shaded = vec4(totalXYZ+0.2*diffuseL + vec3(0.1f,0.1f,0.1f), 1.0);
	// frag_shaded = vec4(vec3(diffuseL), 0.0);
	// frag_shaded = vec4(0.1,0.1,0.1, 1.0);
	// frag_shaded = vec4(1.0,1.0,1.0, 1.0);
}


float rotU(float uu, float vv, float ang)
{
	return uu*cos(ang) - vv*sin(ang);
}

float rotV(float uu, float vv, float ang)
{
	return uu*sin(ang) + vv*cos(ang);
}


void main() 
{
	// setsF();
	setVarXY();
	 

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

	
	float uu = k1.x - k2.x;
	float vv = k1.y - k2.y;
	float ww = k1.z - k2.z;


	// uu = o_org_pos.x;
	// vv = o_org_pos.y;
	// ww = - abs(thetaI *2* 2)/PI;
	
	
	// vec3 totalXYZ = getRawXYZFromTaylorSeries( rotU(uu, vv, phiRect),
	// rotU(uu, vv, phiRect), ww);
	
	vec3 totalXYZ  = getRawXYZFromTaylorSeries( uu, vv, ww);
//	vec3 totalXYZ  = vec3(0);
	// vec3 totalXYZ2 = getRawXYZFromTaylorSeries( uu, vv, -2.0f);
	
	
	 totalXYZ = totalXYZ * gainF(k1, k2)*100;
	//totalXYZ = totalXYZ *100;
	
	totalXYZ = getBRDF_RGB_T_D65(M_Adobe_XRNew, totalXYZ);
	/*
	 * totalXYZ2 = getBRDF_RGB_T_D65(M_Adobe_XRNew, totalXYZ2);
	 * 
	 * totalXYZ.x = totalXYZ.x / totalXYZ2.x/10; totalXYZ.y = totalXYZ.y /
	 * totalXYZ2.y/10; totalXYZ.z = totalXYZ.z / totalXYZ2.z/10;
	 *  /* totalXYZ.x = (totalXYZ.x - totalXYZ2.x)*10000; totalXYZ.y =
	 * (totalXYZ.y - totalXYZ2.y)*10000; totalXYZ.z = (totalXYZ.z -
	 * totalXYZ2.z)*10000;
	 */
	
	if (isnan(totalXYZ.x *totalXYZ.y *totalXYZ.z))
	{
		totalXYZ.x  = 1.0;
		totalXYZ.y  = 1.0;
		totalXYZ.z  = 0.0;
	}
	
	// test-case
//	vec2 N_u = compute_N_min_max(uu);
//	vec2 N_v = compute_N_min_max(vv);
//	float lower_u = N_u.x;
//	float upper_u = N_u.y;
//	float lower_v = N_v.x;
//	float upper_v = N_v.y;
//	float lambda_lower_u = ((dimX*uu)/upper_u)*pow(10.0, 9.0);
//	float lambda_upper_u = ((dimX*uu)/lower_u)*pow(10.0, 9.0);
//	float lambda_lower_v = ((dimX*vv)/upper_v)*pow(10.0, 9.0);
//	float lambda_upper_v = ((dimX*vv)/lower_v)*pow(10.0, 9.0);
//	
//	float exodus_u = lambda_upper_u-lambda_lower_u;
//	float exodus_v = lambda_upper_v-lambda_lower_v;
//	
//	float col = 0.1;
//	
//	vec3 xyz = vec3(col,col, col);
//	
//	if(abs(exodus_v) < 780.0){
//		xyz = vec3(1.0,0.0, 0.0);
//	}
	

	frag_shaded = vec4(gammaCorrect(totalXYZ,2.2), 1.0);
//	 frag_shaded = vec4(xyz , 1.0);
}