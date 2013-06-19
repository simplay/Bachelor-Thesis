package ReadObjects;

public class Face {
	private long id;
	private Vertex i_1;
	private Vertex i_2;
	private Vertex i_3;
	
	public Face(long id, Vertex i_1, Vertex i_2, Vertex i_3){
		this.id = id;
		this.i_1 = i_1;
		this.i_2 = i_2;
		this.i_3 = i_3;
	}
}
