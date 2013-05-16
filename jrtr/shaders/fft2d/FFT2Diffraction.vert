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



in vec3 normal;
in vec4 position;
in vec2 texcoord;
in vec3 tangent;
in vec4 color;


//Output variables for fragment shader
out vec2 frag_texcoord;
out vec4 col;



// all function headers
float patch1d(int, int);


// constants
const float PI = 3.14159265358979323846264;

const mat3 M_Adobe_XR = mat3(
		2.0414, -0.5649, -0.3447,
		-0.9693,  1.8760,  0.0416,
		 0.0134, -0.01184,  1.0154
);	



const int dimension = 100;
const int width = 20; // white of white stripe in patch

float[dimension] w1;


float globalReal = 0.0;
float globalImag = 0.0;

// sample patch, right now defined as a function: 100x100 pixel
float patch1d(int y){
	float res = 1.0;
	if(y <= (dimension/2-1)- width/2 || y >= dimension - (dimension/2 - width/2)) res = 0.0;
	return res;
}

// alpha = k*u/omega, beta = kv/omega after having perfomed rotation via vecotrfield
void dft2(float alpha, float beta){
	float real = 0.0f;
	float imag = 0.0f;
	
	float prec = 1.5*pow(10.0, -7.0); // bump height
	float d = 1.0 /(dimension);
	float mStep = d;
	float nStep = d;
	
	for(int m=0; m < dimension; m++){
		for(int n=0; n < dimension; n++){

			float f = pow(-1.0,m+n)*patch1d(n); 
	
			float termsA = (2.0*PI*(-(mStep*m*alpha) -(nStep*n*beta) ));
			float termsB = (2.0*PI*w*k*f*prec);
		
			real += cos(termsA)*cos(termsB) - sin(termsA)*sin(termsB);
			imag += sin(termsA)*cos(termsB) + cos(termsA)*sin(termsB) ;
			
			
		}
	}
	
	globalReal = real*d;
	globalImag = imag*d;
}




void main() {
	dft2(0.5, 0.5);
	
	w1[1] = 0.1;
	col = vec4(w1[1],w1[1],w1[1], 1);
	
	frag_texcoord = texcoord;
	gl_Position = projection * modelview * position;
}
