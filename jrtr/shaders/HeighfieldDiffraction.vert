#version 150

#define MAX_LIGHTS 1


//Uniform variables, passed in from host program via suitable 
//variants of glUniform*
uniform mat4 projection;
uniform mat4 modelview;
uniform vec3 radianceArray[MAX_LIGHTS];
uniform vec4 directionArray[MAX_LIGHTS];
uniform sampler2D myTexture;

//Input vertex attributes; passed in from host program to shader
//via vertex buffer objects
in vec3 normal;
in vec4 position;
in vec2 texcoord;
in vec3 tangent;
in vec4 color;

//Output variables for fragment shader
//out float ndotL;
//out vec4 normal_out;
//out vec4 eyeVector; // k_2_hat
//out vec4 frag_color;
//out vec4 light_direction[MAX_LIGHTS];
//out vec4 normal_out;
//out vec2 frag_texcoord0;

out vec2 frag_texcoord;
out vec4 col;

vec3 blend3 (vec3 x){
	vec3 y = 1 - x * x;
	y = max(y, vec3 (0, 0, 0));
	return (y);
}

float bump(int m, int n){
	float val = 0.0;
	if(m >= 45 && m <= 55 && n >= 45 && n <= 55)
		val = 1.0;
	return val;
}


void main() {	
	float PI = 3.14159265358979323846264;
	
	vec4 hiliteColor = vec4(radianceArray[0],0);
	vec4 eyePosition = -(modelview * position);
	vec3 lightPosition = directionArray[0].xyz;
	
	vec3 _k1 = normalize(lightPosition);
	vec3 _k2 = normalize(eyePosition.xyz);
	
	vec3 V = _k1 - _k2;
	
	float u = V.x;
	float v = V.y;
	float w = V.z;
	
	// those angles are also not correct yet;
	// teta1: angle between surface normal and _k1
	// teta2: angle between surface normal and _k2
	float teta1 = 1.0;
	float teta2 = 1.0;
	
	float F = 1.0; // change this F to Fresnel of Schlick Approx
	float G = 1.0; // not correct yet, call getG
	float A = 1.0; // check that
	// 2d discret fourier transform over heightfield
	// vertexvise
	
	// iterate over spectrum:
	// introduce blend function soon

		
		float REsum = 0; // real part
		float IMsum = 0; // imaginar part
		
		// fix this bounds - iterate over whole image
		int N = 100; 
		int M = 100;

		
		float deltaM = 1/(M-1);
		float deltaN = 1/(N-1);
		
		// replace with wavenumber of current wavelength
		float k = 0.0; 
		float delta_n = 0;
		// constant factor in front of BRDF
		float fac = (k*k*F*F*G)/(4*PI*PI*A*w*w);
		
		float n_min = 0;
		float n_max = 0;
		float lambda_min = 0.4; // 400nm red
		float lambda_max = 0.7; // 700nm blue

		
		float lambda = 0;
		vec4 cdiff = vec4(0, 0, 0, 1);
		float f = 1.0;
		
		
		float d = 120.0;
		
		if (u > 0) {
			n_min = (d*u)/lambda_max;
			n_max = (d*u)/lambda_min;

			int lower = 0;
			while(lower < n_min){
				lower++;
			}
			
			int upper = 0;
			while(upper < n_max){
				upper++;
			}
			upper--;
			
			float alpha = (lambda_max - lambda_min) / (n_max - n_min);
			//alpha = (lambda_max - lambda_min) / 3;
			for (int n = 0; n <= 3; n = n + 1){
				
				delta_n = n - n_min;
				
				lambda = lambda_min + alpha*delta_n;
				k = (2*PI / lambda);
				
				REsum = 0.0;
				IMsum = 0.0;
				
				// sumsum exp(-i[w_k*n + w_l*m]*f(n,m)
				for(int m = 0; m < M; m++){
					for(int n = 0; n < N; n++){	
						float mStep = (d*2*PI*m)/M;
						float nStep = (d*2*PI*n)/N;
						
						//vec4 tex_mn = texture(myTexture, vec2(m*deltaM, n*deltaN));
						
						//float h_mn = dot(tex_mn.rgb, vec3(0.299, 0.587, 0.114));
						float h_mn = bump(m,n);
						float h = w*h_mn*d;
						REsum += cos(-k*(u*mStep + v*nStep - h));
						IMsum += sin(-k*(u*mStep + v*nStep - h));
					}
				}
				
				// abs_P_Sq equals <|P(ku,kv|^2>
				float abs_P_Sq = REsum*REsum + IMsum*IMsum; 
				
				float brdf = abs_P_Sq;
				//y = 1.0;
				// col = vec4(BRDF,BRDF,BRDF,1);
				
				
				
				float y = ((10/3)*(brdf*lambda*(d/(delta_n+n_min))) - (4/3));
				
//				float delta_n = 0;
//				delta_n = n - n_min;
//				float alpha = (lambda_max - lambda_min) / (n_max - n_min);
//				lambda = lambda_min + alpha*delta_n;
//				// y:[lamda_min, lamda_max] -> [0,1]
//				float y = ((10/3)*(lambda*(d/(delta_n+n_min))) - (4/3)); 
				cdiff.xyz += ( blend3(vec3(f * (y - 0.75), f * (y - 0.5), f * (y - 0.25))) ); 
			}

		} else if(u < 0){
			
		}
		
		
		
		
		
		
		
		
		
		

		col = cdiff + vec4(0.1, 0.1, 0.1 ,0);
		
	
	
	
	frag_texcoord = texcoord;
	gl_Position = projection * modelview * position;
}
