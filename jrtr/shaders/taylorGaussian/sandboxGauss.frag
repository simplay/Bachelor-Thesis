#version 150
#extension GL_EXT_gpu_shader4 : enable

//substitutes
#define MAX_LIGHTS 1
#define MAX_TAYLORTERMS 79
//#define MAX_TAYLORTERMS 54
#define MAX_WFACTORS    79*2
//#define MAX_WEIGHTS 311
//Uniform variables, passed in from host program via suitable 

uniform sampler2DArray TexArray;
uniform sampler2D bodyTexture;
//uniform sampler2D bumpMapTexture;

uniform vec4 cop_w;
uniform vec3 radianceArray[MAX_LIGHTS];
//uniform vec3 brdf_weights[MAX_WEIGHTS];
uniform vec4 directionArray[MAX_LIGHTS];

uniform vec4 scalingFactors[MAX_WFACTORS];
//uniform vec4 global_extrema[1];
uniform vec4 camPos;

uniform int drawTexture;



uniform sampler2DArray lookupText;
// Variables passed in from the vertex shader
in vec2 frag_texcoord;
in vec4 light_direction[MAX_LIGHTS];
in vec4 normal_out;
in vec4 eyeVector;
//in vec4 col;
in vec3 o_org_pos;
in vec3 o_pos;
in vec3 o_light;
in vec3 o_normal;
in vec3 o_tangent;

// Output variable, will be written to framebuffer automatically
out vec4 frag_shaded;

const float PI = 3.14159265358979323846264;

const float powerUV = 5.0;
//const float powerUV = 1.0;

const int numPTerms = 39;
uniform float thetaI;
uniform float phiI;

//transformation constant
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

vec3 getBRDF_RGB_T_D65(mat3 T, vec3 brdf_xyz){
	//vec3 D65 = vec3(0.95047, 1.0, 1.08883);
	vec3 D65 = vec3(1.0, 1.0, 1.0);

	vec3 output = vec3(0.0);
	vec3 D65BRDF = vec3(brdf_xyz.x*D65.x, brdf_xyz.y*D65.y, brdf_xyz.z*D65.z);
	
	//output.x = D65BRDF.x * T[0][0] + D65BRDF.y * T[0][1] + D65BRDF.z * T[0][2] ;
	//output.y = D65BRDF.x * T[1][0] + D65BRDF.y * T[1][1] + D65BRDF.z * T[1][2] ;
	//output.z = D65BRDF.x * T[2][0] + D65BRDF.y * T[2][1] + D65BRDF.z * T[2][2] ;
	
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
	//float nSkin = 1.0015;
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
	//fF = fF/ ((nSkin + 1.0)* (nSkin + 1.0) + nK*nK);
	
	//return fF/R0; // This one is correct. trying other one for effects
	return fF*fF/R0/R0;
}

float getFresnelFactorAbsolute(vec3 K1, vec3 K2)
{
	float nSkin = 1.5;
	//float nSkin = 1.0015;
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
	
	//return fF; This one is correct, trying other one for effects
	return fF*fF;
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
		 return 0.0; // Shadowing Function
	 }
	 
	 gF = gF / K2.z;
	 gF = gF / ww;
	 
	 float fFac = getFresnelFactor(K1, K2); 

	 return fFac*gF;
}

const float uvSampleGap = 0.01f;

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


void Shapemain() 
{
	//setsF();
	 
    vec3 N = normalize(o_normal);
    vec3 T = normalize(o_tangent);

    
    //T = rotateRodrigues(T, N, phi);
    //T = normalize(vec4(T,0.0)).xyz;
        
    //vec3 B = normalize(cross(N, T));
    
	// directional light source
	vec3 Pos =  normalize(o_pos); 
	vec3 lightDir =  normalize(o_light); 
	
	float uu0 = lightDir.x - Pos.x;
	float vv0 = lightDir.y - Pos.y;
	float ww = lightDir.z - Pos.z;

	
	//uu0 = -2*Pos.x;
	//vv0 = -2*Pos.y;
	//ww =  -2*Pos.z;
	
	/* Follwoing 2 lines are to be used only if FFT was used to create the 
	 * lookup tables instead of IFFT 
	 */
	uu0 = -uu0; 
	vv0 = -vv0;
	
	float uu,vv;
	
	//vv0 = 0.0f;
	vec3 totalXYZCone = vec3(0.0f);
	vec3 totalXYZ = vec3(0.0f);
	
	for(int uuI= 0; uuI < 1 ; ++uuI)
	{
		uu = uu0 + uuI * uvSampleGap;
		for(int vvI=0; vvI < 1 ; ++vvI)
		{
			vv = vv0 + vvI*uvSampleGap;
			
			float epsiUV = 0.1f;
			
			/*
			if (abs(uu) < epsiUV)
				uu = 0.0f;

			if (abs(vv)<epsiUV)
				vv = 0.0f;
			*/
			
			vec3 texIdx = vec3(0.0);
			
			if (uu == 0.0)
				texIdx.x = 0.0;
			else if (uu < 0)
				texIdx.x = -pow(-uu/2.0, 1.0/powerUV);
			else
				texIdx.x =  pow(uu/2.0, 1.0/powerUV);
				
			//texIdx.x = 1 - 0.5 - texIdx.x/2.0;
			texIdx.x = 0.5 + texIdx.x/2.0;
			//texIdx.x = 0.5;
			
			// Texture has reverse Y direction as compared to uu vv
			if (vv == 0.0)
				texIdx.y = 0.0;
			else if (vv < 0)
				texIdx.y = - pow(-vv/2.0, 1.0/powerUV);
			else
				texIdx.y =   pow(vv/2.0, 1.0/powerUV);
				
			//texIdx.y = 0.5 + texIdx.y/2.0;
			texIdx.y = 0.5 - texIdx.y/2.0;
			//texIdx.y = 0.5 + texIdx.y/2.0;
			
			//texIdx.x = 0.5;
			//texIdx.y = 0.5;
			vec3 texRef = vec3(0.75);

			totalXYZ = vec3(0.0);
			
			if (texIdx.x >= 0.0f &&  texIdx.y >= 0.0f && texIdx.y <= 1.0f && texIdx.y <= 1.0f)
			for(int i = 0; i < MAX_TAYLORTERMS; i++)
			//for(int i = 0; i < 39; i++)
			{
				
				texIdx.z = i;

				float XX = float(texture2DArray(TexArray, texIdx).r)/1.0;
				float YY = float(texture2DArray(TexArray, texIdx).g)/1.0;
				float ZZ = float(texture2DArray(TexArray, texIdx).b)/1.0;

				//texRef.z = i;
				
				//float rXX = texture2DArray(TexArray, texRef).x;
				//float rYY = texture2DArray(TexArray, texRef).y;
				//float rZZ = texture2DArray(TexArray, texRef).z;
				
				//vec3 modXYZ = rescaleXYZ(XX - rXX, YY - rYY, ZZ - rZZ, i);
				vec3 modXYZ = rescaleXYZ(XX, YY, ZZ, i);
				
				if (i == 0)
					totalXYZ =  modXYZ;
				else if (ww > 0.0)
				{
					//break;
					//totalXYZ = totalXYZ + pow(ww, float(i))*modXYZ;
				}
				
				else if (i%2 == 0)
					totalXYZ = totalXYZ + pow(-ww, float(i))*modXYZ;
				else
					totalXYZ = totalXYZ - pow(-ww, float(i))*modXYZ;
			}
			
			totalXYZ = totalXYZ * gainF(lightDir,Pos)*1000.0;
			
			totalXYZCone = totalXYZCone + totalXYZ;
			//totalXYZ = totalXYZ * 10.0;
		}
		
	}
	
	totalXYZ = getBRDF_RGB_T_D65(M_Adobe_XRNew, totalXYZCone);
	
	
	
	//totalXYZ.x  = 0.00000000000000000000000001208;
	//totalXYZ.y  = 0.00000000000000000000000001208;
	//totalXYZ.z  = 0.00000000000000000000000001208;
	//totalXYZ.x  = float(-ww > 1.99);
	//totalXYZ.y  = float(-ww < 1.99);
	//totalXYZ.z  = float(-ww > 1.99);
	
	//totalXYZ.x  = float(vv > 0);
	//totalXYZ.y  = float(vv > 0);
	//totalXYZ.z  = float(vv > 0);
	
	
	//totalXYZ.x = abs(texIdx.x);
	//totalXYZ.y = abs(texIdx.y);
	//totalXYZ.z = abs(0.0);
	
	
	/*
	
	texIdx.z = 5.0f;
	totalXYZ.x = float(texture2DArray(TexArray, texIdx).x)/1.0;
	totalXYZ.y = float(texture2DArray(TexArray, texIdx).y)/1.0;
	totalXYZ.z = float(texture2DArray(TexArray, texIdx).z)/1.0;
	*/
	
	/*
	texIdx.x = frag_texcoord.x;
	texIdx.y = frag_texcoord.y;
	texIdx.z = 5.0;
		
	totalXYZ.x = float(texture2DArray(TexArray, texIdx).x)/1.0;
	totalXYZ.y = float(texture2DArray(TexArray, texIdx).y)/1.0;
	totalXYZ.z = float(texture2DArray(TexArray, texIdx).z)/1.0;
	*/
	
	
	float diffuseL = 0.0f;
	
	
	if (Pos.z < 0.0  || dot(-lightDir, N) < 0.0)
		diffuseL = 0.0;
	else 
		diffuseL = dot(-lightDir, N);
	
	/*
	if ( dot(Pos, N) < 0.0)
	{
		diffuseL = 0.1f;
		totalXYZ.x = 1.0f;
		totalXYZ.y = 1.0f;
		totalXYZ.z = 0.0f;
		
	}*/
	
	
		
	//float aaa = dot(-lightDir, N);
	/*
	if (abs(uu) < 0.001f) {
		totalXYZ.x  = 1.0f;
		totalXYZ.y  = 1.0f;
		totalXYZ.z  = 1.0f;
	}	else	{
		totalXYZ.x  = 0.0f;
		totalXYZ.y  = 0.0f;
		totalXYZ.z  = 0.0f;
	
	}//*/
	
	vec3 clrBF = vec3(0.0f);
	
	if (ww < 0.0f) {
		clrBF.x  = 0.5f;
		clrBF.y  = 0.0f;
		clrBF.z  = 0.0f;
	}	else	{
		clrBF.x  = 0.0f;
		clrBF.y  = 0.5f;
		clrBF.z  = 0.0f;
	}//*/
	
	
	//vec4 tex = vec4(0.2f);
	vec4 tex = vec4(0.0f);
	
	if(drawTexture > 0)
		tex = texture2D(bodyTexture, frag_texcoord);
	
		
	float alpha = getFresnelFactorAbsolute(lightDir,Pos);
	
	if (alpha > 0.0f)
		alpha = 0.0f;
	else if (alpha > 1.0f)
		alpha = 1.0f;
		
	//frag_shaded = vec4(tex*diffuseL + vec3(0.1f,0.1f,0.1f)*diffuseL, 1.0);
	//frag_shaded = tex;

	float diffW = 0.1f;
	float gamma = 2.2;
	
	
	tex.xyz = gammaCorrect(tex.xyz , 1.0f/1.2);
	
	vec3 finClr = gammaCorrect((1-diffW)*(totalXYZ + (1-alpha) * tex.xyz * diffuseL) + tex.xyz * diffW, 2.2);
	
	frag_shaded = vec4(finClr, 1.0);

	//frag_shaded = vec4(0.5*totalXYZ + 0.5*diffuseL, 1.0);
	//frag_shaded = vec4(totalXYZ + 0.75*tex.xyz*diffuseL , 1.0);
	//frag_shaded = vec4(0.75*tex.xyz*diffuseL , 1.0);

	//frag_shaded	= vec4(totalXYZ + vec3(0.1f,0.1f,0.1f), 1.0);
	//frag_shaded	= vec4(1.0*diffuseL + vec3(0.1f,0.1f,0.1f), 1.0);
	//frag_shaded	= vec4(totalXYZ+0.2*diffuseL + clrBF, 1.0);
	//frag_shaded	= vec4(totalXYZ+vec3(0.1f,0.1f,0.1f), 1.0);
	//frag_shaded	= vec4(totalXYZ + vec3(0.1f,0.1f,0.1f), 1.0);
	
	
	//frag_shaded	= vec4(totalXYZ+0.2*diffuseL + vec3(0.1f,0.1f,0.1f), 1.0);
	//frag_shaded	= vec4(vec3(diffuseL), 0.0);
	//frag_shaded	= vec4(0.1,0.1,0.1, 1.0);
	//frag_shaded	= vec4(1.0,1.0,1.0, 1.0);
}






// mainBRDF
// For BRDF Plot
void main() 
{
	//setsF();
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

	
	float uu0 = k1.x - k2.x;
	float vv0 = k1.y - k2.y;
	float ww = k1.z - k2.z;	
	/* Follwoing 2 lines are to be used only if FFT was used to create the 
	 * lookup tables instead of IFFT 
	 */
	// check for the signs.. for the new lookups neg sign must not be there to make it work
	
	uu0 = uu0; 
	vv0 = vv0;
	
	float uu,vv;
	
	//vv0 = 0.0f;
	vec3 totalXYZCone = vec3(0.0f);
	vec3 totalXYZ = vec3(0.0f);
	
	for(int uuI= 0; uuI < 1 ; ++uuI)
	{
		uu = uu0 + uuI * uvSampleGap;
		for(int vvI=0; vvI < 1 ; ++vvI)
		{
			vv = vv0 + vvI*uvSampleGap;
			
			float epsiUV = 0.1f;
			
			/*
			if (abs(uu) < epsiUV)
				uu = 0.0f;

			if (abs(vv)<epsiUV)
				vv = 0.0f;
			*/
			
			vec3 texIdx = vec3(0.0);
			
			if (uu == 0.0)
				texIdx.x = 0.0;
			else if (uu < 0)
				texIdx.x = -pow(-uu/2.0, 1.0/powerUV);
			else
				texIdx.x =  pow(uu/2.0, 1.0/powerUV);
				
			//texIdx.x = 1 - 0.5 - texIdx.x/2.0;
			texIdx.x = 0.5 + texIdx.x/2.0;
			//texIdx.x = 0.5;
			
			// Texture has reverse Y direction as compared to uu vv
			if (vv == 0.0)
				texIdx.y = 0.0;
			else if (vv < 0)
				texIdx.y = - pow(-vv/2.0, 1.0/powerUV);
			else
				texIdx.y =   pow(vv/2.0, 1.0/powerUV);
				
			//texIdx.y = 0.5 + texIdx.y/2.0;
			texIdx.y = 0.5 - texIdx.y/2.0;
			//texIdx.y = 0.5 + texIdx.y/2.0;
			

			totalXYZ = vec3(0.0);
			
			if (texIdx.x >= 0.0f &&  texIdx.y >= 0.0f && texIdx.y <= 1.0f && texIdx.y <= 1.0f)
			for(int i = 0; i < MAX_TAYLORTERMS; i++)
			//for(int i = 0; i < 39; i++)
			{
				
				texIdx.z = i;

				float XX = float(texture2DArray(TexArray, texIdx).r)/1.0;
				float YY = float(texture2DArray(TexArray, texIdx).g)/1.0;
				float ZZ = float(texture2DArray(TexArray, texIdx).b)/1.0;

				vec3 modXYZ = rescaleXYZ(XX, YY, ZZ, i);
				
				if (i == 0)
					totalXYZ =  modXYZ;
				else if (ww > 0.0)
				{
					//break;
					//totalXYZ = totalXYZ + pow(ww, float(i))*modXYZ;
				}
				
				else if (i%2 == 0)
					totalXYZ = totalXYZ + pow(-ww, float(i))*modXYZ;
				else
					totalXYZ = totalXYZ - pow(-ww, float(i))*modXYZ;
			}
			
			totalXYZ = totalXYZ * gainF(k1, k2) * 100.0;
			
			totalXYZCone = totalXYZCone + totalXYZ;
			//totalXYZ = totalXYZ * 10.0;
		}
	}
	
	totalXYZ = getBRDF_RGB_T_D65(M_Adobe_XRNew, totalXYZCone);
	frag_shaded = vec4(gammaCorrect(totalXYZ,2.2), 1.0);
}
