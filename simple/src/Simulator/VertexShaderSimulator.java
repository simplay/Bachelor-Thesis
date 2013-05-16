
// TODO check for k values looks like they are worng scaled

package Simulator;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;



import jrtr.VertexData.Semantic;

public class VertexShaderSimulator{ 
	private boolean debugMode = false;
	private int index = 0;
	private int upper = 64;
	private Matrix4f cameraMatrix;
	private Vector4f lightDirection;
	private Point3f cameraEye;
	private float[] normals;
	private float[] tangents;
	private float[] kValues;
	private float[] wavelengths;
	private float dimension = 100;
	private int width = 20; // width of white stripe in patch
	private float omega = (float) (8.0f*Math.PI*Math.pow(10,7));

	
	private float[] newColors;

	
	
	
	public VertexShaderSimulator(Matrix4f cameraMatrix, Vector4f lightDirection, Point3f cameraEye,
			float[] positions, float[] normals, float[] tangents){
		
		this.cameraMatrix = cameraMatrix;
		this.lightDirection = lightDirection;
		this.cameraEye = cameraEye;
		this.normals = normals;
		this.tangents = tangents;
		this.newColors = new float[positions.length];
		
		this.kValues = new float[upper];
		this.wavelengths = new float[upper];
		
		float lamda_min = 390.0f;
		float lamda_max = 700.0f;
		
		float delta = (lamda_max - lamda_min) / (upper-1.0f);
		
		for(int t=0; t < upper; t++){
			kValues[t] = (float) ((lamda_min + t*delta)*Math.pow(10.0f, -9.0f));
			wavelengths[t] = (lamda_min + t*delta);
		}


		System.out.println("simulation vertex shader started...");

		int counter = 0;
		for(int k = 0; k < positions.length; k = k + 3){
			Vector4f position = new Vector4f(positions[k], positions[k+1], positions[k+2], 1.0f);
			Vector4f normal = new Vector4f(normals[k], normals[k+1], normals[k+2], 0.0f);
			Vector4f tangent = new Vector4f(tangents[k], tangents[k+1], tangents[k+2], 0.0f);
			
			
			if(debugMode){
				if(position.x == 0 && position.y == 0){
					System.out.println("debug mode for pos (0,0) in world space");
					main(position, normal, tangent);
				}
			}else{
				System.out.println("calculating data for vertex " + counter);
				main(position, normal, tangent);
			}

			
			counter++;
		}
		
		
		
		System.out.println("simulation vertex shader finished...");
	}
	
	private Vector3f getApproxRGB(float currentL){

		float r = 0.0f;
		float g = 0.0f;
		float b = 0.0f;
		
		
	
		if(380.0f <= currentL && currentL < 410.0f){
			r = 0.6f - 0.41f * ((410.0f - currentL) / 30.0f);
			g = 0.0f;
			b = 0.39f + 0.6f * ((410.0f - currentL) / 30.0f);
			
		}else if(410.0f <= currentL && currentL < 440.0f){
			r = 0.19f - 0.19f * ((440.0f - currentL) / 30.0f);
			g = 0.0f;
			b = 1.0f;
			
		}else if(440.0f <= currentL && currentL < 490.0f){
			r = 0.0f;
			g = 1.0f - ((490.0f - currentL) / 50.0f);
			b = 1.0f;
			
		}else if(490.0f <= currentL && currentL < 510.0f){
			r = 0.0f;
			g = 1.0f;
			b = (510.0f - currentL) / 20.0f;
			
		}else if(510 <= currentL && currentL < 580){
			r = 1.0f - ((580.0f - currentL) / 70.0f);
			g = 1.0f;
			b = 0.0f;
			
		}else if(580.0f <= currentL && currentL < 640.0f){
			r = 1.0f;
			g = (640.0f - currentL) / 60.0f;
			b = 0.0f;
			
		}else if(640.0f <= currentL && currentL <= 700.0f){
			r = 1.0f;
			g = 0.0f;
			b = 0.0f;
			
		}
			

		
		return new Vector3f(r,g,b);
	}
	
	private Vector4f matrix4fVector4fProduct(Matrix4f mat, Vector4f vec){
	
		float a = mat.m00*vec.x + mat.m01*vec.y + mat.m02*vec.z + mat.m03*vec.w;
		float b = mat.m10*vec.x + mat.m11*vec.y + mat.m12*vec.z + mat.m13*vec.w;
		float c = mat.m20*vec.x + mat.m21*vec.y + mat.m22*vec.z + mat.m23*vec.w;
		float d = mat.m30*vec.x + mat.m31*vec.y + mat.m32*vec.z + mat.m33*vec.w;
		
		Vector4f result = new Vector4f(a,b,c,d);
		
		return result;
	}
	
	//sample patch, right now defined as a function: 100x100 pixel
	private float patch1d(int y){
		float res = 1.0f;
		if(y <= (dimension/2-1)- width/2 || y >= dimension - (dimension/2 - width/2)) res = 0.0f;
		return res;
	}
	
	private Vector2f getRotation(float u, float v, float phi){
		
		float uu = (float) (u*Math.cos(phi) - v*Math.sin(phi));
		float vv = (float) (u*Math.sin(phi) + v*Math.cos(phi));
		
		return new Vector2f(uu, vv);
	}
	
	//alpha = k*u/omega, beta = kv/omega after having perfomed rotation via vecotrfield
	private Vector2f dft2(float alpha, float beta, float w, float k){
		float real = 0.0f;
		float imag = 0.0f;
			
		float prec = (float) (1.5f*Math.pow(10.0f,-7.0f)); // bump height
		float d = 1.0f /(dimension);
		float mStep = d;
		float nStep = d;
		
		for(int m=0; m < dimension; m++){
			for(int n=0; n < dimension; n++){
				float f = (float) (Math.pow(-1.0f,m+n)*patch1d(n)); 
	
				float termsA = (float) (2.0f*Math.PI*(-(mStep*m*alpha) -(nStep*n*beta) ));
				float termsB = (float) (2.0f*Math.PI*w*k*f*prec);
		
				real += Math.cos(termsA)*Math.cos(termsB) - Math.sin(termsA)*Math.sin(termsB);
				imag += Math.sin(termsA)*Math.cos(termsB) + Math.cos(termsA)*Math.sin(termsB) ;
				
			}
		}
		
		return new Vector2f(d*real, d*imag);
	}
	
	
	private float getFactor(float k, float F, float G, float w){
		if(w==0) return 1;
		// area of CD with d=30cm
		float d = 0.3f;
		float A = (float) (Math.pow(0.5*d, 2.0)*Math.PI);
		return (float) ((k*k*F*F*G)/(4*Math.PI*Math.PI*w*w*A));
	}
	
	
	// like a real vertex shader: vertexwise
	private void main(Vector4f position, Vector4f normal, Vector4f tangent){
//		vec4 eye = (modelview * position); // point in camera space
//		vec4 lightPosition = modelview*vec4(directionArray[0].xyz, 1); // light position in camera space
//		vec3 P = (modelview * position).xyz; // point p under consideration
//		vec3 _k1 = normalize(P - lightPosition.xyz); // _k1: vector from lightPos to point P
//		vec3 _k2 = normalize(cameraPos - P); // _k2: vector from point P to camera
		

		
		
		Vector4f k1 = new Vector4f();
		Vector4f k2 = new Vector4f();
		Vector3f _k1 = new Vector3f();
		Vector3f _k2 = new Vector3f();
		Vector3f V = new Vector3f();
		
//		k1.sub(lightDirection, position);
//		_k1 = new Vector3f(k1.x, k1.y, k1.z);
//		_k1.normalize();
//		
//		Vector3f xyzPosition = new Vector3f(position.x, position.y, position.z);
//		_k2.sub(cameraEye, xyzPosition);
//		_k2.normalize();
//		
		
		
		/**
		 * point light source version
		 * NB: light looks like l = (*,*,*,1)
		 */
//		Vector4f P = matrix4fVector4fProduct(cameraMatrix, position);
//		Vector4f lookAT = new Vector4f(-P.x, -P.y, -P.z, -P.w);
//		Vector4f lightDir = matrix4fVector4fProduct(cameraMatrix, lightDirection);
//		k1.sub(P, lightDir); // light source is a point light
//		k2 = lookAT;
		
		/**
		 * directed light source version
		 */
		Vector4f P = matrix4fVector4fProduct(cameraMatrix, position);
		Vector4f lookAT = new Vector4f(-P.x, -P.y, -P.z, -P.w);
		Vector4f lightDir = matrix4fVector4fProduct(cameraMatrix, lightDirection);
		k1 = lightDir; // light source is a point light
		k2 = lookAT;
		
		
		_k1 = new Vector3f(k1.x, k1.y, k1.z);
		_k1.normalize();
		_k2 = new Vector3f(k2.x, k2.y, k2.z);
		_k2.normalize();
		
		V.sub(_k1, _k2);
		

		
//		float div = ( dot(-_k1, camNormal)*dot(_k2, camNormal) );
//		float G = pow(1.0-dot(_k1, _k2), 2.0 ) / div; 
		
		Vector4f cNormal = matrix4fVector4fProduct(cameraMatrix, normal);
		
		Vector3f xyzNormal = new Vector3f(cNormal.x, cNormal.y, cNormal.z);
		Vector3f neg_k1 = new Vector3f(-_k1.x, -_k1.y, -_k1.z);
		
		float dominator = (float) Math.pow( 1.0f - _k1.dot(_k2) , 2);
		
		
		float tol = 0.999999999f;
		float leftNom = neg_k1.dot(xyzNormal);
		leftNom = (leftNom > tol)? tol : ((leftNom < -tol) ? -tol :  leftNom);
		float rightNom = _k2.dot(xyzNormal);
		rightNom = (rightNom > tol)? tol : ((rightNom < -tol) ? -tol :  rightNom);
		float nominator = leftNom*rightNom;
		
		//float nominator = neg_k1.dot(xyzNormal)*_k2.dot(xyzNormal);
		
		float G = dominator / nominator;

		
		float n_t = 2.5f;
		float R0 = (float) Math.pow( (n_t - 1.0f) / (n_t + 1.0f) , 2); 
		
		
		float tmp =  (float) (2.0f*Math.PI*(neg_k1.dot(xyzNormal)/360.0f));
		float alpha = (float) Math.acos(tmp);
		float teta = (float) (Math.PI - alpha);
		float F = (float) (R0 + (1.0f - R0) * Math.pow(1.0f - Math.cos(alpha), 5));
		
		float u = V.x;
		float v = V.y;
		float w = V.z;
		
		
		
		Vector3f ntangent = new Vector3f(tangent.x,tangent.y,tangent.z);
		ntangent.normalize();
		
		Vector3f x_one = new Vector3f(1.0f,0.0f,0.0f);
		
		float dTemp = ntangent.dot(x_one);
	 	dTemp = (dTemp > tol)? tol : ((dTemp < -tol) ? -tol :  dTemp);
		float phi = (float) Math.acos(dTemp);
		
		Vector3f tempV = new Vector3f();
		tempV.cross(x_one, ntangent);
		
		
		
		if(tempV.z < 0.0) phi = -phi;
		
		
		Vector3f sum = new Vector3f(0,0,0);
		Vector4f brdf = new Vector4f(0.0f,0.0f,0.0f,0.0f);
		int iter = 0;
		// foreach k-value
		for(float m : this.kValues){
			
			float k = (float) ((2.0f * Math.PI)/m); //ok

			Vector2f modUV = getRotation(u,v,-phi); // right now no rot
			Vector2f coords = new Vector2f((k*modUV.x)/omega, (k*modUV.y)/omega); // no rot

			
			Vector2f res = new Vector2f(0f,0f);
			if(Math.abs(k*u) < omega/2.0f &&  Math.abs(k*v) < omega/2.0f)
//				res = dft2( (k*u*dimension/omega - dimension/2), (k*v*dimension/omega - dimension/2), w, k);
				res = dft2( (k*u*dimension/omega - dimension/2), dimension/2, w, k);
//				res = dft2( (coords.x*dimension/omega - dimension/2), (coords.y*dimension/omega - dimension/2), w, k);

			
			float abs_P_Sq = (float) Math.pow((res.x*res.x + res.y*res.y), 1.0f);
			float factor1 = getFactor(k, F, G, w);
			
//			System.out.println(abs_P_Sq);
			
			
			Vector3f waveColor = getApproxRGB(this.wavelengths[iter]);
			sum.add(waveColor);
			
			
			Vector4f tmpBRDF = new Vector4f(waveColor);
			tmpBRDF.scale(factor1 * abs_P_Sq);
			brdf.add(tmpBRDF);
			
			
			iter++;
		}

//		brdf.scale((float) 1.0f/upper);
		
		brdf.x /= sum.x;
		brdf.y /= sum.y;
		brdf.z /= sum.z;
		
//		brdf.scale(1.0f*(float) Math.pow(10, -15)); // pount light source k = 16
		brdf.scale(1.0f*(float) Math.pow(10, -15));
		Vector4f color = new Vector4f(brdf.x, brdf.y, brdf.z, 1.0f);
		// afterscaling
		
		
		/***
		 * prints
		 */	
		
//		if(position.x == 0.0 && position.y == 0.0){
		
			float upperBoundary = (0.5f*omega)/(1f*Math.abs(V.x));
			float upperNum = (float) (2f*Math.PI / (upperBoundary));
			upperNum = (float) (Math.pow(10, 9)*upperNum);
			
			System.out.println("pos in worldspace " + position);
			System.out.println("pos in cameraspace " + P);
			System.out.println("light in worldspace " + lightDirection);
			System.out.println("light in cameraspace " + lightDir);
			
			System.out.println("_k1 " + _k1);
			System.out.println("_k2 " + _k2);
			System.out.println("V " + V);
			
			System.out.println("k* " + upperNum);
			
			System.out.println("G " + G);
			System.out.println("F " + F);
			System.out.println("phi " + phi);
			System.out.println("BRDF " + brdf);
			System.out.println("color " + color);
			System.out.println();
//		}
		

		
		newColors[index] = color.x;
		newColors[index+1] = color.y;
		newColors[index+2] = color.z;
		index += 3;
		
	}
	
	public float[] getColors(){
		return this.newColors;
	}
}
