package Managers;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;


public class CameraSceneConstant {
	private String id;
	private Point3f cop;
	private float distance;
	private float aspectRatio = 1.0f;
	private float near = 0.0001f;
	private float far = 5500.0f;
	private float verticalFieldView;
	private Vector3f up;
	private Point3f look;
	
	public CameraSceneConstant(String id, float distance, float verticalFieldView, Vector3f up, Point3f look){
		this.id = id;
		this.distance = distance;
		this.verticalFieldView = verticalFieldView;
		this.up = up;
		this.look = look;
		this.cop = new Point3f(0.108211061708319f, 0.0f, distance); // camera distance
//		this.cop = new Point3f(0.0f, 0.0f, distance); // camera distance
	}
	
	public String getId(){
		return this.id;
	}
	
	public Point3f getCOP(){
		return this.cop;
	}
	
	public float getDistance(){
		return this.distance;
	}
	
	public float getAspectRatio(){
		return this.aspectRatio;
	}
	
	public float getNear(){
		return this.near;
	}
	
	public float getFar(){
		return this.far;
	}
	
	public float getVerticalFieldView(){
		return this.verticalFieldView;
	}
	
	public Vector3f getUp(){
		return this.up;
	}
	
	public Point3f getLook(){
		return this.look;
	}

}
