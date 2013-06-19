package ReadObjects;

import com.sun.org.apache.regexp.internal.recompile;

public class Vertex {
	private long id;
	private float[] position;
	private float[] normal;
	private float[] textureCoordiante;
	private long triangleId;
	private float[] vp;
	
	public Vertex(long id, float[] position){
		this.id = id;
		this.position = position;
	}
	
	public void setNormal(float[] normal){
		this.normal = normal;
	}
	
	public void setTextureCoordinate(float[] textureCoordinate){
		this.textureCoordiante = textureCoordinate;
	}
	
	public void setTriagnleID(long triangleID){
		this.triangleId = triangleID;
	}
	
	public void setVp(float[] vp){
		this.vp = vp;
	}
	
	public float[] getPosition(){
		return this.position;
	}
	
	public float[] getNormal(){
		return this.normal;
	}
	
	public float[] getTextureCoordiante(){
		return this.textureCoordiante;
	}
	
	public long getTriangleId(){
		return this.id;
	}
	
	public float[] getVp(){
		return this.vp;
	}
	
	public long getId(){
		return this.id;
	}
}
