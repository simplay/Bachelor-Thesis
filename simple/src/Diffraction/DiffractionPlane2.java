package Diffraction;

import java.util.LinkedList;
import java.util.List;

import jrtr.VertexData;

public class DiffractionPlane2 extends DiffractionGeometricObject{
	private float scale;
	private int indexSize;
	private float widthFac;
	private float heightFac;
	private int N;
	private int M;
	
	public DiffractionPlane2(int size, float widthFac, float heightFac){

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
	}

	@Override
	protected float[] getVertexPositions() {
		List<Float> vertexVectors = new LinkedList<Float>();
		
		float mStep = (widthFac*1.0f) / (M - 1.0f);
		float nStep = (heightFac*1.0f) / (N - 1.0f);
		
//		for(int m = 0; m < M; m++){
//			for(int n = 0; n < N; n++){
		for(int m = -M/2; m < M/2; m++){
			for(int n = -N/2; n <N/2; n++){
				vertexVectors.add(mStep*m*scale);
				vertexVectors.add(nStep*n*scale);
				vertexVectors.add(0.0f);
			}
		}
		
		
		
		float[] vertices = new float[vertexVectors.size()];
		int i = 0;
		for(Float value : vertexVectors){
			vertices[i] = value.floatValue();
			i++;
		}
		
		return vertices;
	}

	@Override
	protected float[] getTextureCoordinates() {

		
		List<Float> uvVectors = new LinkedList<Float>();
		
		float mStep = (widthFac*1.0f) / (M - 1.0f);
		float nStep = (heightFac*1.0f) / (N - 1.0f);
		
		for(int m = 0; m < M; m++){
			for(int n = 0; n < N; n++){
				uvVectors.add(mStep*m);
				uvVectors.add(nStep*n);
			}
		}
		
		
		
		float[] uv = new float[uvVectors.size()];
		int i = 0;
		for(Float value : uvVectors){
			uv[i] = value.floatValue();
			i++;
		}
		
		return uv;
	}

	@Override
	protected float[] getNormals() {
		List<Float> normalVectors = new LinkedList<Float>();
		
		for(int m = 0; m < M; m++){
			for(int n = 0; n < N; n++){
				normalVectors.add(0.0f);
				normalVectors.add(0.0f);
				normalVectors.add(1.0f);
			}
		}
		
		
		
		float[] normals = new float[normalVectors.size()];
		int i = 0;
		for(Float value : normalVectors){
			normals[i] = value.floatValue();
			i++;
		}
		
		return normals;
	}

	@Override
	protected float[] getVertexColors() {
		List<Float> colorVectors = new LinkedList<Float>();
		
		for(int m = 0; m < M; m++){
			for(int n = 0; n < N; n++){
				colorVectors.add(0.0f);
				colorVectors.add(0.0f);
				colorVectors.add(1.0f);
			}
		}
		
		
		
		float[] colors = new float[colorVectors.size()];
		int i = 0;
		for(Float value : colorVectors){
			colors[i] = value.floatValue();
			i++;
		}
		
		return colors;
	}

	@Override
	protected int[] getTriangulationIndices() {
		List<Integer> indexVectors = new LinkedList<Integer>();
		
		for(int m = 0; m < M-1; m++){
			for(int n = 0; n < N-1; n++){		
				int a = n+(m*N);
				int b = (n+1)%N+(m*N);
				int c = n+((m+1)*N);
				
				indexVectors.add(a);
				indexVectors.add(b);
				indexVectors.add(c);
				
			}
		}
		
		for(int m = 0; m < M-1; m++){
			for(int n = 0; n < N-1; n++){		
				int a = n+((m+1)*N);
				int b = (n+1)%N+((m+1)*N);
				int c = (n+1)%N+(m*N);
				
				indexVectors.add(a);
				indexVectors.add(b);
				indexVectors.add(c);
				
			}
		}
		
		
		
		
		int[] indices = new int[indexVectors.size()];
		int i = 0;
		for(Integer value : indexVectors){
			indices[i] = value.intValue();
			i++;
		}
		
		return indices;
	}

	@Override
	protected float[] getTangentVectors() {
		List<Float> tangentVectors = new LinkedList<Float>();
		
		for(int m = 0; m < M; m++){
			for(int n = 0; n < N; n++){
				tangentVectors.add(1.0f);
				tangentVectors.add(0.0f);
				tangentVectors.add(0.0f);
			}
		}
		
		
		
		float[] tangents = new float[tangentVectors.size()];
		int i = 0;
		for(Float value : tangentVectors){
			tangents[i] = value.floatValue();
			i++;
		}
		
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
