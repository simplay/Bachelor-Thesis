package ReadObjects;

import java.util.ArrayList;

public class VertexFaceData {
	
	private ArrayList<Vertex> vertices;
	private ArrayList<Face> faces;
	
	public VertexFaceData(ArrayList<Vertex> vertices, ArrayList<Face> faces){
		this.vertices = vertices;
		this.faces = faces;
	}
}
