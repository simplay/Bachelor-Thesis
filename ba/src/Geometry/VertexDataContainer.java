package Geometry;

public class VertexDataContainer {
	private float vertices[];
	private float normals[];
	private float tangents[];
	private float colors[];
	private float textureCoordinates[];
	private int indices[];
	private float avgEdgeLenght = 1.0f;
	
	public VertexDataContainer(float[] vertices, float[] normals, 
			float[] tangents, float[] colors, 
			float[] textureCoordinates, int[] indices){
		
		this.vertices = vertices;
		this.normals = normals;
		this.tangents = tangents;
		this.colors = colors;
		this.textureCoordinates = textureCoordinates;
		this.indices = indices;
	}
	
	public VertexDataContainer(float[] vertices, float[] normals, 
			float[] tangents, float[] colors, 
			float[] textureCoordinates, int[] indices, float avgEdgeLenght){
		
		this.vertices = vertices;
		this.normals = normals;
		this.tangents = tangents;
		this.colors = colors;
		this.textureCoordinates = textureCoordinates;
		this.indices = indices;
		this.avgEdgeLenght = avgEdgeLenght;
	}
	
	public float[] getVertices(){
		return this.vertices;
	}
	
	public float[] getNormals(){
		return this.normals;
	}
	
	public float[] getTangents(){
		return this.tangents;
	}
	
	public float[] getColors(){
		return this.colors;
	}
	
	public float[] getTextureCoordinates(){
		return this.textureCoordinates;
	}
	
	public int[] getIndices(){
		return this.indices;
	}
	
	public float getAvgEdgeLength(){
		return this.avgEdgeLenght;
	}
}
