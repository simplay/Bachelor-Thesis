package Diffraction;

import java.util.LinkedList;
import java.util.List;

import Geometry.VertexDataContainer;
import ReadObjects.WriteBackMonkey;

import jrtr.VertexData;

public class DiffractionCone extends DiffractionGeometricObject{
	private float scale;
	private int indexSize;
	private float widthFac;
	private float heightFac;
	private int N;
	private int M;
	
	public DiffractionCone(int size, float widthFac, float heightFac){

		if(size%2 ==1) indexSize = size+1;
		else indexSize = size;
		
		this.N = indexSize;
		this.M = indexSize;
		
		this.widthFac = widthFac;
		this.heightFac = heightFac;
		
		this.scale = 0.1f;
		
		this.setupObject();
		this.tangentVectors = this.getTangentVectors();
		this.vertexData.addElement(this.tangentVectors, VertexData.Semantic.TANGENT, 3);
		
		VertexDataContainer data = new VertexDataContainer(vetices, normals, tangentVectors, colors, textureCoordinates, indices);
		WriteBackMonkey monkey = new WriteBackMonkey(data);
		monkey.writeAll();
	}

	@Override
	protected float[] getVertexPositions() {
		List<Float> vertexVectors = new LinkedList<Float>();

		// Add the tip of the cone
		vertexVectors.add(0.0f);
		vertexVectors.add(0.0f);
		vertexVectors.add(0.0f);

		// Add the circle on the top
		
		for(int ii = 0; ii < indexSize; ii++)
		{
			vertexVectors.add((float)(widthFac*Math.cos(ii*2*Math.PI/indexSize)));
			vertexVectors.add((float)(widthFac*Math.sin(ii*2*Math.PI/indexSize)));
			vertexVectors.add(heightFac);
		}
	
		float[] vertices = new float[vertexVectors.size()];
		int i = 0;
		for(Float value : vertexVectors){
			vertices[i] = value.floatValue();
			i++;
		}
		System.out.println(i + " vertices");
		
		return vertices;
	}

	@Override
	protected float[] getTextureCoordinates() {

		
		List<Float> uvVectors = new LinkedList<Float>();

		uvVectors.add(0.5f);
		uvVectors.add(0.5f);

		for(int ii = 0; ii < indexSize; ii++)
		{
			uvVectors.add((float)(0.5 + 0.5*Math.cos(ii*2*Math.PI/indexSize)));
			uvVectors.add((float)(0.5 + 0.5*Math.sin(ii*2*Math.PI/indexSize)));
		}
		
		float[] uv = new float[uvVectors.size()];
		int i = 0;
		for(Float value : uvVectors){
			uv[i] = value.floatValue();
			i++;
		}
		
		System.out.println(i + " texture coordinates");

		return uv;
	}

	@Override
	protected float[] getNormals() {
		List<Float> normalVectors = new LinkedList<Float>();
		
		normalVectors.add(0.0f);
		normalVectors.add(0.0f);
		normalVectors.add(-1.0f);
		
		for(int ii = 0; ii < indexSize; ii++)
		{
			normalVectors.add((float)(Math.cos(ii*2*Math.PI/indexSize)));
			normalVectors.add((float)(Math.sin(ii*2*Math.PI/indexSize)));
			normalVectors.add(0.0f);
		}
				
		float[] normals = new float[normalVectors.size()];
		int i = 0;
		for(Float value : normalVectors){
			normals[i] = value.floatValue();
			i++;
		}
		
		System.out.println(i + " normals");
		return normals;
	}

	@Override
	protected float[] getVertexColors() {
		List<Float> colorVectors = new LinkedList<Float>();


		colorVectors.add(0.0f);
		colorVectors.add(0.0f);
		colorVectors.add(1.0f);

		for(int ii = 0; ii < indexSize; ii++)
		{
			colorVectors.add(0.0f);
			colorVectors.add(0.0f);
			colorVectors.add(1.0f);
		}
			
		
		
		float[] colors = new float[colorVectors.size()];
		int i = 0;
		for(Float value : colorVectors){
			colors[i] = value.floatValue();
			i++;
		}
		
		System.out.println(i + " colors");
		return colors;
	}

	@Override
	protected int[] getTriangulationIndices() {
		List<Integer> indexVectors = new LinkedList<Integer>();
		
		for(int ii = 1; ii < indexSize; ii++)
		{
			indexVectors.add(ii);
			indexVectors.add(0);
			indexVectors.add(ii+1);
			
		}
				
		// add the closing face
		indexVectors.add(indexSize);
		indexVectors.add(0);
		indexVectors.add(1);
		
		int[] indices = new int[indexVectors.size()];
		int i = 0;
		for(Integer value : indexVectors){
			indices[i] = value.intValue();
			i++;
		}
		
		System.out.println(i + " face-vertex indices");
		return indices;
	}

	@Override
	protected float[] getTangentVectors() {
		List<Float> tangentVectors = new LinkedList<Float>();

		tangentVectors.add(1.0f);
		tangentVectors.add(1.0f);
		tangentVectors.add(0.0f);
		
		for(int ii = 0; ii < indexSize; ii++)
		{
			tangentVectors.add((float)(-Math.sin(ii*2*Math.PI/indexSize)));
			tangentVectors.add((float)(Math.cos(ii*2*Math.PI/indexSize)));
			tangentVectors.add(0.0f);
		}
				
		float[] tangents = new float[tangentVectors.size()];
		int i = 0;
		for(Float value : tangentVectors){
			tangents[i] = value.floatValue();
			i++;
		}
		
		System.out.println(i + " tangents");
		return tangents;
	}

	@Override
	protected int getVerticesCount() {
		return this.vetices.length/3;
	}
	
	public int getDimension(){
		return this.vetices.length/3;
	}
	
	public void overwriteColor(float[] newColors){
		this.vertexData.replaceElement(newColors, VertexData.Semantic.COLOR);
	}

}
