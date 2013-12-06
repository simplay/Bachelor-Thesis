package jrtr;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

/**
 * Stores the specification of a viewing frustum, or a viewing
 * volume. The viewing frustum is represented by a 4x4 projection
 * matrix. You will extend this class to construct the projection 
 * matrix from intuitive parameters.
 * <p>
 * A scene manager (see {@link SceneManagerInterface}, {@link SimpleSceneManager}) 
 * stores a frustum.
 * @author Michael Single, 08-917-445
 */
public class Frustum {
	// near / far = near plane / far plane
	private Matrix4f projectionMatrix;
	private float aspectRatio;
	private float near;
	private float far;
	private float verticalFieldView;
	private Vector3f[] planeNormals = new Vector3f[6];
	/**
	 * Construct a default viewing frustum. The frustum is given by a 
	 * default 4x4 projection matrix.
	 */
	public Frustum(){
		projectionMatrix = new Matrix4f();
		float f[] = {2.f, 0.f, 0.f, 0.f, 
					 0.f, 2.f, 0.f, 0.f,
				     0.f, 0.f, -1.02f, -2.02f,
				     0.f, 0.f, -1.f, 0.f};
		projectionMatrix.set(f);
	}
	
	/**
	 * Return the 4x4 projection matrix, which is used for example by 
	 * the renderer.
	 * 
	 * @return the 4x4 projection matrix
	 */
	public Matrix4f getProjectionMatrix(){
		return projectionMatrix;
	}
	
	/**
	 * set the near and far plane, aspect ratio and the vertical field of view
	 * and then constructs by those parameters a new projection matrix for our frustum.
	 * @param aspectRatio
	 * @param near
	 * @param far
	 * @param verticalFieldView
	 */
	public void setParameter(float aspectRatio, float near, float far, float verticalFieldView){
		float ratio = (verticalFieldView/180.0f);
		float fov = (float) (Math.PI * ratio);
		this.aspectRatio = aspectRatio;
		this.near = near;
		this.far = far;
		this.verticalFieldView = fov;
		updateProjectionMatrix();
	}
	
	public Vector3f[] calculateFrustumPoints(Vector3f up, Point3f look, Point3f cop){
		@SuppressWarnings("unused")
		float a,b,c,d;
		
		// near plane normal
		a = projectionMatrix.m03 + projectionMatrix.m02;
		b = projectionMatrix.m13 + projectionMatrix.m12;
		c = projectionMatrix.m23 + projectionMatrix.m22;
		d = projectionMatrix.m33 + projectionMatrix.m32;
		
		//Vector4f v4 = new Vector4f(a,b,c,d);
		Vector3f v3 = new Vector3f(a,b,c);
		v3.normalize();
		planeNormals[0] = v3;
		
		// far plane normal
		a = projectionMatrix.m03 - projectionMatrix.m02;
		b = projectionMatrix.m13 - projectionMatrix.m12;
		c = projectionMatrix.m23 - projectionMatrix.m22;
		d = projectionMatrix.m33 - projectionMatrix.m32;
		
		v3 = new Vector3f(a,b,c);
		v3.normalize();
		planeNormals[1] = v3;
		
		// left plane normal
		a = projectionMatrix.m03 + projectionMatrix.m00;
		b = projectionMatrix.m13 + projectionMatrix.m10;
		c = projectionMatrix.m23 + projectionMatrix.m20;
		d = projectionMatrix.m33 + projectionMatrix.m30;
		
		v3 = new Vector3f(a,b,c);
		v3.normalize();
		planeNormals[2] = v3;
		
		// right plane normal
		a = projectionMatrix.m03 - projectionMatrix.m00;
		b = projectionMatrix.m13 - projectionMatrix.m10;
		c = projectionMatrix.m23 - projectionMatrix.m20;
		d = projectionMatrix.m33 - projectionMatrix.m30;
		
		v3 = new Vector3f(a,b,c);
		v3.normalize();
		planeNormals[3] = v3;
		
		// top plane normal
		a = projectionMatrix.m03 - projectionMatrix.m01;
		b = projectionMatrix.m13 - projectionMatrix.m11;
		c = projectionMatrix.m23 - projectionMatrix.m21;
		d = projectionMatrix.m33 - projectionMatrix.m31;
		
		v3 = new Vector3f(a,b,c);
		v3.normalize();
		planeNormals[4] = v3;
		
		// bottom plane normal
		a = projectionMatrix.m03 + projectionMatrix.m01;
		b = projectionMatrix.m13 + projectionMatrix.m11;
		c = projectionMatrix.m23 + projectionMatrix.m21;
		d = projectionMatrix.m33 + projectionMatrix.m31;
		
		v3 = new Vector3f(a,b,c);
		v3.normalize();
		planeNormals[5] = v3;
		
		return planeNormals;
	}
	
	/**
	 * update this projectionMatrix with the values stored in
	 * aspectRatio, near, far, verticalFieldView.
	 * only perform an update in the case any of those vector,
	 * points has been modified or set for the first time.
	 * update mechanism: just calculate a new perspective projection
	 * matrix as described in the slides, lecture 3.
	 */
	private void updateProjectionMatrix(){
		float tanFOV = (float) Math.tan(this.verticalFieldView/2);
		float p11 = (1.0f / tanFOV);
		float p00 = (1.0f / (tanFOV * this.aspectRatio));
		float deltaNF = (this.near - this.far);
		float p22 =  (this.near + this.far) / deltaNF;
		float p23 = (2.0f * this.near * this.far) / deltaNF;
		
		Vector4f column0 = new Vector4f(p00, 0, 0, 0);
		Vector4f column1 = new Vector4f(0, p11, 0, 0);
		Vector4f column2 = new Vector4f(0, 0, p22, -1.0f);
		Vector4f column3 = new Vector4f(0, 0, p23, 0);
		
		Matrix4f newProjectionMatrix = new Matrix4f();
		newProjectionMatrix.setColumn(0, column0);
		newProjectionMatrix.setColumn(1, column1);
		newProjectionMatrix.setColumn(2, column2);
		newProjectionMatrix.setColumn(3, column3);
		
		this.projectionMatrix = newProjectionMatrix;
	}
	
	public float getAspectRatio(){
		return this.aspectRatio;
	}
	
	public float getFarPlane(){
		return this.far;
	}
	
	public float getNearPlane(){
		return this.near;
	}
	public float getVertFOV(){
		return this.verticalFieldView;
	}
	
}
