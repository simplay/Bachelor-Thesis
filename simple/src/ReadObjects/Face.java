package ReadObjects;

import java.util.ArrayList;

public class Face {
	private long id;
	private Vertex i_1;
	private Vertex i_2;
	private Vertex i_3;
	
	private ArrayList<float[]> tri_textCoord = new ArrayList<float[]>();
	private ArrayList<float[]> tri_normal = new ArrayList<float[]>();
	
	public Face(long id, Vertex i_1, Vertex i_2, Vertex i_3){
		this.id = id;
		this.i_1 = i_1;
		this.i_2 = i_2;
		this.i_3 = i_3;
		
		this.i_1.addface(this);
		this.i_2.addface(this);
		this.i_3.addface(this);
	}
	
	public void addTriTextCoord(float[] triTextCoord){
		this.tri_textCoord.add(triTextCoord);
	}
	
	public void addTriNormal(float[] triNormal){
		this.tri_normal.add(triNormal);
	}
	
	public Vertex getVertex1(){
		return this.i_1;
	}
	
	public Vertex getVertex2(){
		return this.i_2;
	}
	
	public Vertex getVertex3(){
		return this.i_3;
	}
	
	public float[] getTriTextCoord(){
		return null;
	}
	
	public float[] getTriNormal(){
		return null;
	}
	
	public long getId(){
		return this.id;
	}
	
	public int[] getIndices(){
		int index1 = (int)i_1.getId();
		int index2 = (int)i_2.getId();
		int index3 = (int)i_3.getId();
		int[] indices = {index1, index2, index3};
		return indices;
	}
}
