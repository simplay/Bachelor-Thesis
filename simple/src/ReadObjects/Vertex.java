package ReadObjects;

import java.util.ArrayList;

public class Vertex {
	private long id;
	private float[] position;
	private float[] normal;
	private float[] textureCoordiante;
	private ArrayList<Face> faces = new ArrayList<Face>();
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
	
	public void addface(Face face){
		this.faces.add(face);
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
	
	public ArrayList<Face> getFaces(){
		return this.faces;
	}
	
	public long getId(){
		return this.id;
	}
}
