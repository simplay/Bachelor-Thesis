package Diffraction;

import jrtr.VertexData;

public class DiffractionPlane extends DiffractionGeometricObject{
	private int scale;
	public DiffractionPlane(){
		this.scale = 2;
		this.setupObject();
		this.tangentVectors = this.getTangentVectors();
		this.vertexData.addElement(this.tangentVectors, VertexData.Semantic.TANGENT, 3);
	}

	@Override
	protected float[] getVertexPositions() {
		float vertices[] = {
				-scale,-scale,scale, 
				scale,-scale,scale, 
				scale,scale,scale, 
				-scale,scale,scale
		};
		return vertices;
	}

	@Override
	protected float[] getTextureCoordinates() {
		float uv[] = {
				0,0, 1,0, 1,1, 0,1
		};
		return uv;
	}

	@Override
	protected float[] getNormals() {
		float normals[] = {
				0,0,1, 0,0,1, 0,0,1, 0,0,1
		};  
		return normals;
	}

	@Override
	protected float[] getVertexColors() {
		float colors[] = {
				 1,0,0, 1,0,0, 1,0,0, 1,0,0
		};
		return colors;
	}

	@Override
	protected int[] getTriangulationIndices() {
		int indices[] = {
				0,2,3, 0,1,2
		};
		return indices;
	}

	@Override
	protected float[] getTangentVectors() {
		float tangents[] = {
				-1,0,0, -1,0,0, -1,0,0, -1,0,0
		}; 
		return tangents;
	}

	@Override
	protected int getVerticesCount() {
		return this.vetices.length/3;
	}

}
