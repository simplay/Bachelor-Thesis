package Listeners;
import javax.vecmath.Vector3f;


public class Storage {
	Vector3f v1 = new Vector3f(0,0,0), v2 = new Vector3f(0,0,0);
	
	public Vector3f getV1(){
		return this.v1;
	}
	
	public Vector3f getV2(){
		return this.v2;
	}
	
	public void setV1(Vector3f v){
		this.v1 = v;
	}
	
	public void setV2(Vector3f v){
		this.v2 = v;
	}
}
