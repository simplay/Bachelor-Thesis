// Diffraction shader using a given heightfield
// adptive (regarding wavelength) diffraction shader 
// using taylor series approximation.
// which have been precalculated.
// Michael Single

// The MIT License (MIT)
//
// Copyright (c) <2014> <Michael Single>
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



#version 150
#extension GL_EXT_gpu_shader4 : enable

// substitutes
#define MAX_LIGHTS 1
#define MAX_FACTORS 31
#define MAX_WEIGHTS 311

//Uniform variables, passed in from host program via suitable 
//variants of glUniform*
uniform mat4 projection;
uniform mat4 modelview;
uniform mat4 modelM;
uniform vec4 cop_w;
uniform vec3 radianceArray[MAX_LIGHTS];
uniform vec3 brdf_weights[MAX_WEIGHTS];
uniform vec4 directionArray[MAX_LIGHTS];
uniform vec4 scalingFactors[MAX_FACTORS];
uniform vec4 global_extrema[1];
uniform sampler2DArray TexArray;
uniform vec4 camPos;

uniform float LMIN;
uniform float LMAX;
uniform float approxSteps;
uniform float dimN;
uniform float dimSmall; // not used right now
uniform float dimDiff; // not used right now
uniform float repNN; // not used right now
uniform int periodCount;
uniform float maxBumpHeight;
uniform float patchSpacing;

in vec3 normal;
in vec4 position;
in vec2 texcoord;
in vec3 tangent;
in vec4 color;

//Output variables for fragment shader
out vec2 frag_texcoord;
out vec3 o_pos;
out vec3 o_light;
out vec3 o_normal;
out vec3 o_tangent;
out vec3 o_org_pos;

const float PI = 3.14159265358979323846264;
// rotate around 90deg
const float phi = -PI/2.0;


// rotate given vector v around given axis with angle phi ccw
// by perfroming a rodrigues rotation - see wikipedia
vec3 rotateRodrigues(vec3 v, vec3 axis, float phi){
	vec3 rotatedV = vec3(0.0, 0.0, 0.0);
	vec3 axisCrossV = cross(axis, v);
	
	float axisDotV = axis.x *v.x + axis.y * v.y + axis.z * v.z; 
	float dotScale = axisDotV * (1 - cos(phi));
	
	rotatedV.x = v.x * cos(phi) + axisCrossV.x * sin (phi) + axis.x * dotScale;
	rotatedV.y = v.y * cos(phi) + axisCrossV.y * sin (phi) + axis.y * dotScale;
	rotatedV.z = v.z * cos(phi) + axisCrossV.z * sin (phi) + axis.z * dotScale;
	
	return rotatedV;
}


void main() {
	
	// Initialize Tangentspace=span{T,B,N}
    vec3 N = normalize(modelM * vec4(normal,0.0)).xyz;
    vec3 T = normalize(modelM * vec4(tangent,0.0)).xyz;
    N = normalize(N);
    T = normalize(T);
    T = rotateRodrigues(T, N, phi);
    T = normalize(T);
    vec3 B = normalize(cross(N, T));
    
    
	// directional light source
	vec3 Pos =  ((cop_w-position)).xyz; // point in camera space
	vec4 lightDir = (directionArray[0]); // light direction in camera space
	lightDir = normalize(lightDir);

	
	// new lightDir in tangent space
	// light direction: from camera space to tangent space
	vec3 lit2 = vec3(0.0);
	float lx = dot(lightDir.xyz, T);
	float ly = dot(lightDir.xyz, B);
	float lz = dot(lightDir.xyz, N);
	lit2 = vec3(lx, ly, lz);
	
	
	// new position in tangent space
	// position: from camera space to tangent space
	vec3 Pos2 = vec3(0.0);
	float px = dot(Pos, T);
	float py = dot(Pos, B);
	float pz = dot(Pos, N);
	Pos2 = vec3(px, py, pz);
	
	
	// stream output
	o_org_pos = position.xyz;
	o_pos = Pos2;
	o_light = normalize(lit2.xyz);
	o_normal = N;
	o_tangent = T;
	frag_texcoord = texcoord;
	gl_Position = projection * modelview * position;
}
