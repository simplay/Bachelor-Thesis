package Diffraction;

import jrtr.VertexData;

public abstract class GeometricObject2 {
	protected VertexData vertexData;
	protected int segmentCount;
	protected float vetices[];
	protected float colors[];
	protected float normals[];
	protected int indices[];
	protected float[] textureCoordinates;
	
	protected abstract float[] getVertexPositions();
	protected abstract float[] getTextureCoordinates();
	protected abstract float[] getNormals();
	protected abstract float[] getVertexColors();
	protected abstract int[] getTriangulationIndices();
	
//	public GeometricObject(int segmentCount){
//		this.segmentCount = segmentCount;
//	}
	
	/**
	 * returns the vertex data
	 * @return vertex data
	 */
	public VertexData getVertices(){
		return this.vertexData;
	}
	
	/**
	 * set up this geometric object by assembling all its geometric properties.
	 */
	protected void setupObject(){
		this.vertexData = this.getVertices();
		this.vetices = this.getVertexPositions();
		this.colors = this.getVertexColors();
		this.textureCoordinates = this.getTextureCoordinates();
		this.indices = this.getTriangulationIndices();
		this.normals = this.getNormals();
		
		int verticesCount = getVerticesCount();
		this.vertexData = new VertexData(verticesCount);
		this.vertexData.addElement(this.colors, VertexData.Semantic.COLOR, 3);
		this.vertexData.addElement(this.vetices, VertexData.Semantic.POSITION, 3);
		this.vertexData.addElement(this.textureCoordinates, VertexData.Semantic.TEXCOORD, 2);
		this.vertexData.addElement(this.normals, VertexData.Semantic.NORMAL, 3);
		this.vertexData.addIndices(this.indices);
	}
	
	protected abstract int getVerticesCount();
}
