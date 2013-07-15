package Geometry;



public class Cube extends GeometricObject{
	public Cube(){
		this.setupObject();
		
	}
	
	@Override
	protected float[] getVertexPositions() {
		float vertices[] = {
				-1,-1,1, 1,-1,1, 1,1,1, -1,1,1,			// front face
		        -1,-1,-1, -1,-1,1, -1,1,1, -1,1,-1,		// left face
			  	1,-1,-1, -1,-1,-1, -1,1,-1, 1,1,-1,		// back face
				1,-1,1, 1,-1,-1, 1,1,-1, 1,1,1,			// right face
				1,1,1, 1,1,-1, -1,1,-1, -1,1,1,			// top face
				-1,-1,1, -1,-1,-1, 1,-1,-1, 1,-1,1};	// bottom face
		return vertices;
	}

	@Override
	protected float[] getTextureCoordinates() {
		float uv[] = {
				0,0, 1,0, 1,1, 0,1,
				0,0, 1,0, 1,1, 0,1,
				0,0, 1,0, 1,1, 0,1,
				0,0, 1,0, 1,1, 0,1,
				0,0, 1,0, 1,1, 0,1,
				0,0, 1,0, 1,1, 0,1};
		return uv;
	}

	@Override
	protected float[] getNormals() {
		float normals[] = {
				0,0,1, 0,0,1, 0,0,1, 0,0,1,
	         	-1,0,0, -1,0,0, -1,0,0, -1,0,0,
		  	    0,0,-1, 0,0,-1, 0,0,-1, 0,0,-1, 
			    1,0,0, 1,0,0, 1,0,0, 1,0,0,
			    0,1,0, 0,1,0, 0,1,0, 0,1,0, 
			    0,-1,0, 0,-1,0, 0,-1,0,  0,-1,0};  
		return normals;
	}

	@Override
	protected float[] getVertexColors() {
		float colors[] = {
				 1,0,0, 1,0,0, 1,0,0, 1,0,0,
			     0,1,0, 0,1,0, 0,1,0, 0,1,0,
				 1,1,0, 1,1,0, 1,1,0, 1,1,0,//back
				 0,1,1, 0,1,1, 0,1,1, 0,1,1,
				 0,0,1, 0,0,1, 0,0,1, 0,0,1,
				 1,0,1, 1,0,1, 1,0,1, 1,0,1};
		return colors;
	}

	@Override
	protected int[] getTriangulationIndices() {
		int indices[] = {
				0,2,3, 0,1,2,			// front face
				4,6,7, 4,5,6,			// left face
				8,10,11, 8,9,10,		// back face
				12,14,15, 12,13,14,		// right face
				16,18,19, 16,17,18,		// top face
				20,22,23, 20,21,22};	// bottom face
		return indices;
	}
}
