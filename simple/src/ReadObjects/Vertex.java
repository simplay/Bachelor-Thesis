package ReadObjects;

import java.util.ArrayList;

import javax.vecmath.Vector3f;

public class Vertex {
	private long id;
	private float[] position;
	private float[] normal;
	private float[] textureCoordiante;
	private ArrayList<Face> faces = new ArrayList<Face>();
	private float[] vp;
	private float[] tangent;
	
	private Vector3f tan1;
	private Vector3f tan2;
	
	public Vertex(long id, float[] position){
		this.id = id;
		this.position = position;
		this.tan1 = new Vector3f(0.0f, 0.0f, 0.0f);
		this.tan2 = new Vector3f(0.0f, 0.0f, 0.0f);
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
	
	public void addVectorToTan1(Vector3f vec){
		this.tan1.add(vec);
	}
	
	public void addVectorToTan2(Vector3f vec){
		this.tan2.add(vec);
	}
	
	public Vector3f getTan1(){
		return this.tan1;
	}
	
	public Vector3f getTan2(){
		return this.tan2;
	}
	
	public float[] getTangent(){
		return this.tangent;
	}
	
	public void setTangent(Vector3f tangent){
		float[] tmp_tan = new float[3];
		tmp_tan[0] = tangent.x;
		tmp_tan[1] = tangent.y;
		tmp_tan[2] = tangent.z;
		this.tangent = tmp_tan;
	}
	
}
