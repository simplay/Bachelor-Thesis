package jrtr;

import javax.vecmath.*;

/**
 * Stores the specification of a virtual camera. You will extend
 * this class to construct a 4x4 camera matrix, i.e., the world-to-
 * camera transform from intuitive parameters. 
 * 
 * A scene manager (see {@link SceneManagerInterface}, {@link SimpleSceneManager}) 
 * stores a camera.
 * 
 * extended by: 
 * @author Michael Single, 08-917-445
 * 
 */
public class Camera {

	private Matrix4f cameraMatrix;
	private Point3f projectionCenterPoint;
	private Point3f lookAtPoint;
	private Vector3f upVector;
	
	/**
	 * Construct a camera with a default camera matrix. The camera
	 * matrix corresponds to the world-to-camera transform. This default
	 * matrix places the camera at (0,0,10) in world space, facing towards
	 * the origin (0,0,0) of world space, i.e., towards the negative z-axis.
	 */
	public Camera(){
		this.projectionCenterPoint = new Point3f(0f,0f,0f);
		this.lookAtPoint = new Point3f(0f,0f,0f);
		this.upVector = new Vector3f(0f,0f,0f);
		
		cameraMatrix = new Matrix4f();
		float f[] = {1.f, 0.f, 0.f, 0.f,
					 0.f, 1.f, 0.f, 0.f,
					 0.f, 0.f, 1.f, -10.f,
					 0.f, 0.f, 0.f, 1.f};
		cameraMatrix.set(f);
	}
	
	/**
	 * 
	 * @param projectionCenterPoint
	 * @param lookAtPoint
	 * @param upVector
	 */
	public Camera(Point3f projectionCenterPoint, Point3f lookAtPoint, Vector3f upVector){
		this.projectionCenterPoint = projectionCenterPoint;
		this.lookAtPoint = lookAtPoint;
		this.upVector = upVector;
		updateCameraMatrix();
	}
	
	/**
	 * Return the camera matrix, i.e., the world-to-camera transform. For example, 
	 * this is used by the renderer.
	 * 
	 * @return the 4x4 world-to-camera transform matrix
	 */
	public Matrix4f getCameraMatrix(){
		return cameraMatrix;
	}
	
	/**
	 * get the corresponding center of projection point
	 * @return projectionCenterPoint
	 */
	public Point3f getProjectionCenterPoint(){
		return this.projectionCenterPoint;
	}
	
	/**
	 * get the corresponding look at point
	 * @return lookAtPointPoint
	 */
	public Point3f getLookAtPointPoint(){
		return this.lookAtPoint;
	}
	
	/**
	 * get the corresponding up vector
	 * @return upVector
	 */
	public Vector3f getUpVector(){
		return this.upVector;
	}
	
	/**
	 * set a projection center point for this camera and then update this cameraMatrix.
	 * @param newProjectionCenterPoint new value for this.projectionCenterPoint.
	 */
	public void setProjectionCenterPoint(Point3f newProjectionCenterPoint){
		this.projectionCenterPoint = newProjectionCenterPoint;
		updateCameraMatrix();
	}
	
	/**
	 * set a look at point for this camera and then update this cameraMatrix.
	 * @param newLookAtPoint new value for this.lookAtPoint.
	 */
	public void setLookAtPoint(Point3f newLookAtPoint){
		this.lookAtPoint = newLookAtPoint;
		updateCameraMatrix();
	}
	
	/**
	 * set a up vector for this camera and then update this cameraMatrix.
	 * @param newUpVector new value for this.upVector
	 */
	public void setUpVector(Vector3f newUpVector){
		this.upVector = newUpVector;
		updateCameraMatrix();
	}
	
	/**
	 * @param projectionCenterPoint 
	 * @param lookAtPoint 
	 * @param upVector 
	 */
	public void setParameter(Point3f projectionCenterPoint, Point3f lookAtPoint, Vector3f upVector){
		this.projectionCenterPoint = projectionCenterPoint;
		this.lookAtPoint = lookAtPoint;
		this.upVector = upVector;
		updateCameraMatrix();
	}
	
	/**
	 * update this cameraMatrix with the values stored in
	 * projectionCenterPoint, lookAtPoint, upVector.
	 * only perform an update in the case any of those vector,
	 * points has been modified or set for the first time.
	 * update mechanism: 
	 * e denotes = ProjectionCenterPoint
	 * d denotes = getLookAtPointPoint
	 * up denotes = getUpVector
	 * we have first to calculate those 3 vectors:
	 * zc := (e-d) / ||(e-d)||
	 * xc := (up x zc) / ||(up x zc)||
	 * yc := (zc x xc)
	 * and then the camera to world transformation-matrix
	 * Cnew := [xc	yc	zc	e]
	 * 		   [0	0	0	1]
	 */
	protected void updateCameraMatrix(){
		Point3f e = new Point3f(getProjectionCenterPoint());
		Point3f d = new Point3f(getLookAtPointPoint());
		Vector3f up = new Vector3f(getUpVector());
		Matrix4f tmpCamera = new Matrix4f();
		
		// z-axis:
		Vector3f tmpZC = new Vector3f(0.0f, 0.0f, 0.0f);
		tmpZC.sub(e, d);
		tmpZC.normalize();
		Vector4f ZC = new Vector4f(tmpZC);
		
		// x-axis:
		Vector3f tmpXC = new Vector3f(0.0f, 0.0f, 0.0f);
		tmpXC.cross(up, tmpZC);
		tmpXC.normalize();
		Vector4f XC = new Vector4f(tmpXC);
		
		// y-axis:
		Vector3f tmpYC = new Vector3f(0.0f, 0.0f, 0.0f);
		tmpYC.cross(tmpZC, tmpXC);
		Vector4f YC = new Vector4f(tmpYC);
	
		tmpCamera.setColumn(0, XC);
		tmpCamera.setColumn(1, YC);
		tmpCamera.setColumn(2, ZC);
		tmpCamera.setColumn(3, new Vector4f(e.x, e.y, e.z, 1));
		
		Matrix4f invC = new Matrix4f();
		invC.invert(tmpCamera);
		
		this.cameraMatrix = invC;
	}
	
	public void setCameraMatrix(Matrix4f ma){
		this.cameraMatrix = ma;
	}
}
