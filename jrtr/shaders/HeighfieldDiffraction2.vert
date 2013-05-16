#version 150

#define MAX_LIGHTS 1


//Uniform variables, passed in from host program via suitable 
//variants of glUniform*
uniform mat4 projection;
uniform mat4 modelview;
uniform vec3 radianceArray[MAX_LIGHTS];
uniform vec4 directionArray[MAX_LIGHTS];

uniform sampler2D Texture0;
uniform sampler2D Texture1;
uniform sampler2D Texture2;
uniform sampler2D Texture3;
uniform sampler2D Texture4;
uniform sampler2D Texture5;
uniform sampler2D Texture6;
uniform sampler2D Texture7;
uniform sampler2D Texture8;
uniform sampler2D Texture9;
uniform sampler2D Texture10;
uniform sampler2D Texture11;
uniform sampler2D Texture12;
uniform sampler2D Texture13;
uniform sampler2D Texture14;
uniform sampler2D Texture15;
uniform sampler2D Texture16;
uniform sampler2D Texture17;

uniform sampler2D Texture18;
uniform sampler2D Texture19;
uniform sampler2D Texture20;
uniform sampler2D Texture21;
uniform sampler2D Texture22;
uniform sampler2D Texture23;
uniform sampler2D Texture24;
uniform sampler2D Texture25;
uniform sampler2D Texture26;
uniform sampler2D Texture27;
uniform sampler2D Texture28;
uniform sampler2D Texture29;
uniform sampler2D Texture30;
uniform sampler2D Texture31;
uniform sampler2D Texture32;
uniform sampler2D Texture33;
uniform sampler2D Texture34;
uniform sampler2D Texture35;

//Input vertex attributes; passed in from host program to shader
//via vertex buffer objects
in vec3 normal;
in vec4 position;
in vec2 texcoord;
in vec3 tangent;
in vec4 color;

//Output variables for fragment shader
out vec2 frag_texcoord;
out vec4 col;


void main() {
	
	vec4 L400 = vec4(0.286, 0.0, 0.647, 1.0);
	vec4 L450 = vec4(0.0, 0.199, 1.0, 1.0);
	vec4 L500 = vec4(0.0, 1.0, 0.498, 1.0);
	vec4 L550 = vec4(0.568, 1.0, 0.0, 1.0);
	vec4 L600 = vec4(1.0, 0.690, 0.0, 1.0);
	vec4 L650 = vec4(1.0, 0.0, 0.0, 1.0);
	float PI = 3.14159265358979323846264;
	
	vec4 hiliteColor = vec4(radianceArray[0],0);
	vec4 eyePosition = -(modelview * position);
	vec3 lightPosition = directionArray[0].xyz;
	
	vec3 _k1 = normalize(lightPosition);
	vec3 _k2 = normalize(eyePosition.xyz);
	
	// components of V are in between the range [-2,2]
	// since _k1 and _k2 are normalized vectors.
	vec3 V = _k1 - _k2;
	
	float u = V.x;
	float v = V.y;
	float w = V.z;
	
	
	float real_part = 0.0;
	float imag_part = 0.0;
	vec4 brdf = vec4(0.0,0.0,0.0,1.0);
	
	float k = 2.0*PI/0.4;
	vec2 coords = vec2(k*u,k*v);
	float C = 1.0;
	if(w < -1.75){
		real_part = C*dot(texture(Texture0, coords).xyz, vec3(0.299, 0.587, 0.114) );
		imag_part = C*dot(texture(Texture9, coords).xyz, vec3(0.299, 0.587, 0.114) );
	}else if(w >= -1.75 && w < -1.25){
		real_part = C*dot(texture(Texture1, coords).xyz, vec3(0.299, 0.587, 0.114) );
		imag_part = C*dot(texture(Texture10, coords).xyz, vec3(0.299, 0.587, 0.114) );	
	}else if(w >= -1.25 && w < -0.75){
		real_part = C*dot(texture(Texture2, coords).xyz, vec3(0.299, 0.587, 0.114) );
		imag_part = C*dot(texture(Texture11, coords).xyz, vec3(0.299, 0.587, 0.114) );
	}else if(w >= -0.75 && w < -0.25){
		real_part = C*dot(texture(Texture3, coords).xyz, vec3(0.299, 0.587, 0.114) );
		imag_part = C*dot(texture(Texture12, coords).xyz, vec3(0.299, 0.587, 0.114) );
	}else if(w >= -0.25 && w < 0.25){
		real_part = C*dot(texture(Texture4, coords).xyz, vec3(0.299, 0.587, 0.114) );
		imag_part = C*dot(texture(Texture13, coords).xyz, vec3(0.299, 0.587, 0.114) );
	}else if(w >= 0.25 && w < 0.75){
		real_part = C*dot(texture(Texture5, coords).xyz, vec3(0.299, 0.587, 0.114) );
		imag_part = C*dot(texture(Texture14, coords).xyz, vec3(0.299, 0.587, 0.114) );
	}else if(w >= 0.75 && w < 1.25){
		real_part = C*dot(texture(Texture6, coords).xyz, vec3(0.299, 0.587, 0.114) );
		imag_part = C*dot(texture(Texture15, coords).xyz, vec3(0.299, 0.587, 0.114) );
	}else if(w >= 1.25 && w < 1.75){
		real_part = C*dot(texture(Texture7, coords).xyz, vec3(0.299, 0.587, 0.114) );
		imag_part = C*dot(texture(Texture16, coords).xyz, vec3(0.299, 0.587, 0.114) );
	}else{
		real_part = C*dot(texture(Texture8, coords).xyz, vec3(0.299, 0.587, 0.114) );
		imag_part = C*dot(texture(Texture17, coords).xyz, vec3(0.299, 0.587, 0.114) );
	}
	
	float abs_P_Sq = real_part*real_part + imag_part*imag_part; 
	brdf.xyz += abs_P_Sq*L400.xyz;
	
	k = 2.0*PI/0.45;
	coords = vec2(k*u,k*v);

//	if(w < -1.75){
//		real_part = C*dot(texture(Texture18, coords).xyz, vec3(0.299, 0.587, 0.114) );
//		imag_part = C*dot(texture(Texture27, coords).xyz, vec3(0.299, 0.587, 0.114) );
//	}else if(w >= -1.75 && w < -1.25){
//		real_part = C*dot(texture(Texture19, coords).xyz, vec3(0.299, 0.587, 0.114) );
//		imag_part = C*dot(texture(Texture28, coords).xyz, vec3(0.299, 0.587, 0.114) );	
//	}else if(w >= -1.25 && w < -0.75){
//		real_part = C*dot(texture(Texture20, coords).xyz, vec3(0.299, 0.587, 0.114) );
//		imag_part = C*dot(texture(Texture29, coords).xyz, vec3(0.299, 0.587, 0.114) );
//	}else if(w >= -0.75 && w < -0.25){
//		real_part = C*dot(texture(Texture21, coords).xyz, vec3(0.299, 0.587, 0.114) );
//		imag_part = C*dot(texture(Texture30, coords).xyz, vec3(0.299, 0.587, 0.114) );
//	}else if(w >= -0.25 && w < 0.25){
//		real_part = C*dot(texture(Texture22, coords).xyz, vec3(0.299, 0.587, 0.114) );
//		imag_part = C*dot(texture(Texture31, coords).xyz, vec3(0.299, 0.587, 0.114) );
//	}else if(w >= 0.25 && w < 0.75){
//		real_part = C*dot(texture(Texture23, coords).xyz, vec3(0.299, 0.587, 0.114) );
//		imag_part = C*dot(texture(Texture32, coords).xyz, vec3(0.299, 0.587, 0.114) );
//	}else if(w >= 0.75 && w < 1.25){
////		real_part = C*dot(texture(Texture24, coords).xyz, vec3(0.299, 0.587, 0.114) );
////		imag_part = C*dot(texture(Texture33, coords).xyz, vec3(0.299, 0.587, 0.114) );
//	}else if(w >= 1.25 && w < 1.75){
////		real_part = C*dot(texture(Texture25, coords).xyz, vec3(0.299, 0.587, 0.114) );
////		imag_part = C*dot(texture(Texture34, coords).xyz, vec3(0.299, 0.587, 0.114) );
//	}else{
//		real_part = C*dot(texture(Texture26, coords).xyz, vec3(0.299, 0.587, 0.114) );
//		imag_part = C*dot(texture(Texture35, coords).xyz, vec3(0.299, 0.587, 0.114) );
//	}
//	abs_P_Sq = real_part*real_part + imag_part*imag_part; 
//	brdf.xyz += abs_P_Sq*L450.xyz;
	
	
	
	
	
//	real_part = (texture(Texture2, frag_texcoord).xyz, vec3(0.299, 0.587, 0.114) );
//	imag_part = (texture(Texture3, frag_texcoord).xyz, vec3(0.299, 0.587, 0.114) );
//	abs_P_Sq = real_part*real_part + imag_part*imag_part; 
//	brdf.xyz += abs_P_Sq*L550.xyz;
//	
//	
//	real_part = (texture(Texture4, frag_texcoord).xyz, vec3(0.299, 0.587, 0.114) );
//	imag_part = (texture(Texture5, frag_texcoord).xyz, vec3(0.299, 0.587, 0.114) );
//	abs_P_Sq = real_part*real_part + imag_part*imag_part; 
//	brdf.xyz += abs_P_Sq*L650.xyz;
	
	
	col = brdf;
	frag_texcoord = texcoord;
	gl_Position = projection * modelview * position;
}
