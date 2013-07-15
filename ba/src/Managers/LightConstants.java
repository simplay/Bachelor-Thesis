package Managers;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import jrtr.Light;

public class LightConstants {
	private String id;
	private Vector3f radiance = new Vector3f(1,1,1); 
	private Light light;
	
	public LightConstants(String id, Vector4f direction) {
		this.id = id;
		this.light = new Light(radiance, direction, id);
	}
	
	public String getId(){
		return this.id;
	}
	
	public Light getLight(){
		return this.light;
	}
}
