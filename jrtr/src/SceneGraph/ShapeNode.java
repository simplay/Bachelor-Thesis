/**
 * shape node for scene graph.
 * @author Michael Single
 */
package SceneGraph;

import java.util.ArrayList;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import jrtr.Shape;

public class ShapeNode extends Leaf{
	private boolean isUpdatable;
	private boolean performsCulling;
	private String name;
	private Matrix4f cameraMatrix;
	private ArrayList<Vector3f> frustumVectors;
	private Vector4f boundingSphereCenter;
	private float boundingSphereRadius;
	protected ArrayList<Vector3f> frustumPoints = new ArrayList<Vector3f>();
	
	/**
	 * 
	 * @param shape
	 * @param name
	 */
	public ShapeNode(Shape shape, String name){
		super(shape);
		this.name = name;
		this.isUpdatable = true;
		this.performsCulling = false;
		this.boundingSphereRadius = this.shape.getBoundingSphereRadius();
		this.boundingSphereCenter = this.shape.getCenter();
	}
	
	/**
	 * wont perform culling
	 * @param shape
	 * @param name
	 * @param flag
	 */
	public ShapeNode(Shape shape, String name, boolean flag){
		super(shape);
		this.name = name;
		this.isUpdatable = flag;
		this.performsCulling = false;
		this.boundingSphereRadius = this.shape.getBoundingSphereRadius();
		this.boundingSphereCenter = this.shape.getCenter();
	}
	
	/**
	 * will perform culling.
	 * @param shape
	 * @param name
	 * @param frustumVectors
	 * @param cameraMatrix
	 */
	public ShapeNode(Shape shape, String name, ArrayList<Vector3f> frustumVectors, Matrix4f cameraMatrix){
		super(shape);
		this.name = name;
		this.frustumVectors = frustumVectors;
		this.cameraMatrix = cameraMatrix;
		this.performsCulling = true;
		this.boundingSphereRadius = this.shape.getBoundingSphereRadius();
		this.boundingSphereCenter = this.shape.getCenter();
	}
	

	public ShapeNode(Shape shape, String name, ArrayList<Vector3f> frustumVectors, Matrix4f cameraMatrix, boolean performCulling){
		super(shape);
		this.name = name;
		this.frustumVectors = frustumVectors;
		this.cameraMatrix = cameraMatrix;
		this.performsCulling = performCulling;
		this.boundingSphereRadius = this.shape.getBoundingSphereRadius();
		this.boundingSphereCenter = this.shape.getCenter();
	}
	
	private Vector3f normalFarPlane() {
		Vector3f normalFar = new Vector3f();
		Vector3f temp1 = new Vector3f();
		temp1.sub(frustumVectors.get(5), frustumVectors.get(4));
		Vector3f temp2 = new Vector3f();
		temp2.sub(frustumVectors.get(7), frustumVectors.get(4));
		normalFar.cross(temp1, temp2);

		normalFar.normalize();
		frustumPoints.add(frustumVectors.get(7));
		return normalFar;
	}

	private Vector3f normalBottomPlane() {
		Vector3f normalBottom = new Vector3f();
		Vector3f temp1 = new Vector3f();
		temp1.sub(frustumVectors.get(3), frustumVectors.get(0));
		Vector3f temp2 = new Vector3f();
		temp2.sub(frustumVectors.get(4), frustumVectors.get(0));
		normalBottom.cross(temp1, temp2);

		normalBottom.normalize();
		frustumPoints.add(frustumVectors.get(3));
		return normalBottom;
	}

	private Vector3f normalLeftPlane() {
		Vector3f normalLeft = new Vector3f();
		Vector3f temp1 = new Vector3f();
		temp1.sub(frustumVectors.get(3), frustumVectors.get(2));
		Vector3f temp2 = new Vector3f();
		temp2.sub(frustumVectors.get(3), frustumVectors.get(7));
		normalLeft.cross(temp1, temp2);

		normalLeft.normalize();
		frustumPoints.add(frustumVectors.get(3));
		return normalLeft;
	}

	private Vector3f normalNearPlane() {
		Vector3f normalNear = new Vector3f();
		Vector3f temp1 = new Vector3f();
		temp1.sub(frustumVectors.get(1), frustumVectors.get(0));
		Vector3f temp2 = new Vector3f();
		temp2.sub(frustumVectors.get(3), frustumVectors.get(0));
		normalNear.cross(temp1, temp2);

		normalNear.normalize();
		frustumPoints.add(frustumVectors.get(1));
		return normalNear;
	}

	private Vector3f normalTopPlane() {
		Vector3f normalTop = new Vector3f();
		Vector3f temp1 = new Vector3f();
		temp1.sub(frustumVectors.get(2), frustumVectors.get(1));
		Vector3f temp2 = new Vector3f();
		temp2.sub(frustumVectors.get(5), frustumVectors.get(1));
		normalTop.cross(temp1, temp2);

		normalTop.negate();
		normalTop.normalize();
		frustumPoints.add(frustumVectors.get(2));
		return normalTop;
	}

	private Vector3f normalRightPlane() {
		Vector3f normalRight = new Vector3f();
		Vector3f temp1 = new Vector3f();
		temp1.sub(frustumVectors.get(1), frustumVectors.get(0));
		Vector3f temp2 = new Vector3f();
		temp2.sub(frustumVectors.get(4), frustumVectors.get(0));
		normalRight.cross(temp1, temp2);

		normalRight.negate();
		normalRight.normalize();
		frustumPoints.add(frustumVectors.get(1));
		return normalRight;
	}
	
	/**
	 * get all 6 frusum plane normals
	 * @return
	 */
	private ArrayList<Vector3f> getFrustumPlaneNormals(){
		ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
		
		/**
		 * defining planes of frustum.
		 */
		normals.add(normalRightPlane());
		normals.add(normalTopPlane());
		normals.add(normalNearPlane());
		normals.add(normalLeftPlane());
		normals.add(normalBottomPlane());
		normals.add(normalFarPlane());
		
		return normals;
	}
	
	/**
	 * tells us bounding sphere of shape intersects at least
	 * one of the 6 frustum planes.
	 * @return
	 */
	public boolean intersectsFrustumAfterCulling(Vector4f inputCenter){
		boolean answer = true;
		Vector3f x = new Vector3f(inputCenter.x, inputCenter.y, inputCenter.z);
		Vector3f p = new Vector3f(0, 0, 0); // TODO seek for the why.
	    Vector3f XP = new Vector3f();
		ArrayList<Vector3f> normals = getFrustumPlaneNormals();
		//System.out.println(x);
	    
		/**
		 * conservative test - the culling algorithm
		 */
		int i = 0;
		for(Vector3f normal : normals){
			float distance = 0;
			p =  frustumPoints.get(i); // TODO check that
			XP.sub(x, p);  
		    distance = XP.dot(normal);
		   // System.out.println("Radius" + boundingSphereRadius + " Distance " + distance);
		    if (distance > boundingSphereRadius) {
		    	System.out.println("dropped");
		    	return false;
		    }
		    i++;
		}
		
		return answer;
	}
	
	/**
	 * define if shape node is updatable.
	 * @param flag
	 */
	public void setIsUpdatabel(boolean flag){
		this.isUpdatable = flag;
	}
	
	/**
	 * is this shape node updatable?
	 * @return
	 */
	public boolean getIsUpdatable(){
		return this.isUpdatable;
	}
	
	/**
	 * get id name of this shape node
	 * @return
	 */
	public String getName(){
		return this.name;
	}
	
	/**
	 * set camera matrix for this shape node.
	 * @param cameraMatrix
	 */
	public void setCameraMatrix(Matrix4f cameraMatrix){
		this.cameraMatrix = cameraMatrix;
	}
	
	/**
	 * set frustum vectors for this shape node.
	 * @param frustumVectors
	 */
	public void setFrustumVectors(ArrayList<Vector3f> frustumVectors){
		this.frustumVectors = frustumVectors;
	}
	
	public Vector4f getCenter(){
		return this.boundingSphereCenter;
	}
	
    public Matrix4f getCameraMatrix() {
        return this.cameraMatrix;
    }
    
    /**
     * does this node allow performing the culling algorithm?
     * @return
     */
    public boolean performsCulling(){
    	return this.performsCulling;
    }
	
}
