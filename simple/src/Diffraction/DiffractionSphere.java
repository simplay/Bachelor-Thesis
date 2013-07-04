package Diffraction;

import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Vector3f;

import jrtr.VertexData;

public class DiffractionSphere extends DiffractionGeometricObject{
	
	
	private float radius;
	private int circlePointCount;
	private int circleCount;
	private float scale;
	
	
	public DiffractionSphere(float r, int cpc){
		this.radius = r;

		this.circlePointCount = cpc;
		this.circleCount = 1;
		this.scale = 0.1f;
		
		this.setupObject();
		this.tangentVectors = this.getTangentVectors();
		this.vertexData.addElement(this.tangentVectors, VertexData.Semantic.TANGENT, 3);
	}
	
	@Override
	protected float[] getTangentVectors() {
		int counter = 0;
		List<Float> tangents = new LinkedList<Float>();
		float thetaStep = (float) (2.0f*Math.PI/circlePointCount);
		float rohStep = (float) (1.0f*Math.PI/circlePointCount);
		
		for(int u = 0; u < circlePointCount; u++){
			for(int v = 0; v < circlePointCount; v++){
				float x = (float) (radius*Math.cos(rohStep*v)*Math.cos(thetaStep*u));
				float y = (float) (radius*Math.cos(rohStep*v)*Math.sin(thetaStep*u));
				float z = (float) (-radius*Math.sin(v*rohStep));
				
				Vector3f a = new Vector3f(x,y,z);
				a.normalize();
				
				tangents.add(a.x);
				tangents.add(a.y);
				tangents.add(a.z);	
				counter++;
			}
		}
		

		float[] tang = new float[tangents.size()];
		int i = 0;
		for(Float value : tangents){
			tang[i] = value.floatValue();
			i++;
		}
		
		System.out.println(counter + " tangents");
		return tang;
	}

	@Override
	protected float[] getVertexPositions() {
		int counter = 0;
		List<Float> vertices = new LinkedList<Float>();
		float thetaStep = (float) (2.0f*Math.PI/circlePointCount);
		float rohStep = (float) (1.0f*Math.PI/circlePointCount);
		
		for(int u = 0; u < circlePointCount; u++){
			for(int v = 0; v < circlePointCount; v++){
				float x = (float) (radius*Math.sin(rohStep*v)*Math.cos(thetaStep*u));
				float y = (float) (radius*Math.sin(rohStep*v)*Math.sin(thetaStep*u));
				float z = (float) (radius*Math.cos(v*rohStep));
				
				vertices.add(x);
				vertices.add(y);
				vertices.add(z);	
				counter++;
			}
		}
		

		float[] vert = new float[vertices.size()];
		int i = 0;
		for(Float value : vertices){
			vert[i] = value.floatValue();
			i++;
		}
		
		System.out.println(counter + " vertices");
		return vert;
	}

	@Override
	protected float[] getTextureCoordinates() {
		int counter = 0;
		List<Float> textureCoordiantes = new LinkedList<Float>();
		float thetaStep = (float) (2.0f*Math.PI/circlePointCount);
		float rohStep = (float) (1.0f*Math.PI/circlePointCount);

		
		for(int t = 0; t < circlePointCount; t++){
			for(int k = 0; k < circlePointCount; k++){
				float u = t*thetaStep;
				float v = k*rohStep;
				textureCoordiantes.add(u);
				textureCoordiantes.add(v);
				counter++;
			}
		}
		
		
		float[] text = new float[textureCoordiantes.size()];
		int i = 0;
		for(Float value : textureCoordiantes){
			text[i] = value.floatValue();
			i++;
		}
		
		System.out.println(counter + " texture coordinates");
		return text;
	}

	@Override
	protected float[] getNormals() {
		int counter = 0;
		List<Float> normals = new LinkedList<Float>();
		float thetaStep = (float) (2.0f*Math.PI/circlePointCount);
		float rohStep = (float) (1.0f*Math.PI/circlePointCount);
	
		for(int u = 0; u < circlePointCount; u++){
			for(int v = 0; v < circlePointCount; v++){
				float x_tan = (float) (radius*Math.cos(rohStep*v)*Math.cos(thetaStep*u));
				float y_tan = (float) (radius*Math.cos(rohStep*v)*Math.sin(thetaStep*u));
				float z_tan = (float) (-radius*Math.sin(v*rohStep));
				
				float x_bitan = (float) (-radius*Math.sin(rohStep*v)*Math.sin(thetaStep*u));
				float y_bitan = (float) (radius*Math.sin(rohStep*v)*Math.cos(thetaStep*u));
				float z_bitan = 0.0f;
				
				
				Vector3f a = new Vector3f(x_tan,y_tan,z_tan);
				Vector3f b = new Vector3f(x_bitan,y_bitan,z_bitan);
				Vector3f c = new Vector3f();
				
				c.cross(a, b);
				c.normalize();
				
				normals.add(c.x);
				normals.add(c.y);
				normals.add(c.z);	
				counter++;
			}
		}
		

		float[] norm = new float[normals.size()];
		int i = 0;
		for(Float value : normals){
			norm[i] = value.floatValue();
			i++;
		}
		
		System.out.println(counter + " normals");
		return norm;
	}

	@Override
	protected float[] getVertexColors() {
		int counter = 0;
		List<Float> colors = new LinkedList<Float>();
		
		for(int t = 0; t < circlePointCount; t++){
			for(int k = 0; k < circlePointCount; k++){
				float x = 1.0f;
				float y = 0.0f;
				float z = 0.0f;
				
				colors.add(x);
				colors.add(y);
				colors.add(z);	
				counter++;
			}
		}
		
		float[] col = new float[colors.size()];
		int i = 0;
		for(Float value : colors){
			col[i] = value.floatValue();
			i++;
		}
		
		System.out.println(counter + " colors");
		return col;
	}

	@Override
	protected int[] getTriangulationIndices() {
		List<Integer> indicesValues = new LinkedList<Integer>();
		int counter = 0;
		
		for(int t = 0; t < circlePointCount-1; t++){
			for(int k = 0; k < circlePointCount-1; k++){
				
				int a = k+t*circlePointCount;
				int b = (k+1)%circlePointCount+t*circlePointCount;
				int c = (k)%circlePointCount+((t+1)*circlePointCount);
				
				
				indicesValues.add(a);
				indicesValues.add(b);
				indicesValues.add(c);
				System.out.println(a + " " + b + " " + c);
				counter++;
			}
		}
		
		for(int t = circlePointCount-1; t > 0; t--){
			for(int k = 0; k < circlePointCount-1; k++){
				
				int a = k+t*circlePointCount;
				int b = (k+1)%circlePointCount+t*circlePointCount;
				int c = (k+1)%circlePointCount+((t-1)*circlePointCount);
				
				
				indicesValues.add(a);
				indicesValues.add(b);
				indicesValues.add(c);
				
				System.out.println(a + " " + b + " " + c);
				counter++;
			}
		}
		
		int[] indices = new int[indicesValues.size()];
		int i = 0;
		for(Integer value : indicesValues){
			indices[i] = value.intValue();
			i++;
		}
		
		System.out.println(counter + " indices");
		return indices;
	}

	@Override
	protected int getVerticesCount() {
		return this.vetices.length/3;
	}
	

}
