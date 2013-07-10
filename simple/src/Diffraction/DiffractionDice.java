package Diffraction;

import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Vector3f;

import Geometry.VertexDataContainer;
import ReadObjects.WriteBackMonkey;

import jrtr.VertexData;

public class DiffractionDice extends DiffractionGeometricObject{
	
	private float totalR;
	private float wholeR;
	private int pointsPerCircle;
	private int innerCircleCount;
	private float deltaR;

	public DiffractionDice(float oR, float iR,  int ppc, int icc){
		this.totalR = oR;
		this.wholeR = iR;
		this.deltaR = oR - iR;
		this.pointsPerCircle = ppc;
		this.innerCircleCount = icc;
		
		this.setupObject();
		this.tangentVectors = this.getTangentVectors();
		this.vertexData.addElement(this.tangentVectors, VertexData.Semantic.TANGENT, 3);
		
		VertexDataContainer data = new VertexDataContainer(vetices, normals, tangentVectors, colors, textureCoordinates, indices);
		WriteBackMonkey monkey = new WriteBackMonkey(data);
		monkey.writeAll();
	}
	
	@Override
	protected float[] getTangentVectors() {
		int counter = 0;
		List<Float> tangents = new LinkedList<Float>();
		float phiStep = (float) (2.0f*Math.PI/pointsPerCircle);
		float setpR = deltaR/innerCircleCount;
		for(int k = 0; k < innerCircleCount; k++){
			float currDist = setpR*k + wholeR;
			for(int t = 0; t < pointsPerCircle; t++){
				float x = (float) -(currDist*Math.sin(phiStep*t));
				float y = (float) (currDist*Math.cos(phiStep*t));
				float z = 0.0f;
						
				Vector3f a = new Vector3f(x,y,z);
				a.normalize();
				
				tangents.add(a.x);
				tangents.add(a.y);
				tangents.add(a.z);	
				counter++;							
			}
		}
		
		float[] tan = new float[tangents.size()];
		int i = 0;
		for(Float value : tangents){
			tan[i] = value.floatValue();
			i++;
		}
		
		System.out.println(counter + " tangents");
		return tan;
	}

	@Override
	protected float[] getVertexPositions() {
		int counter = 0;
		List<Float> vertices = new LinkedList<Float>();
		float phiStep = (float) (2.0f*Math.PI/pointsPerCircle);
		float setpR = deltaR/innerCircleCount;
		for(int k = 0; k < innerCircleCount; k++){
			float currDist = setpR*k + wholeR;
			for(int t = 0; t < pointsPerCircle; t++){
				float x = (float) (currDist*Math.cos(phiStep*t));
				float y = (float) (currDist*Math.sin(phiStep*t));
				float z = 0.0f;
						
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
		float phiStep = (float) (2.0f*Math.PI/pointsPerCircle);
		float setpR = deltaR/innerCircleCount;
		
		for(int t = 0; t < innerCircleCount; t++){
			for(int k = 0; k < pointsPerCircle; k++){
				float u = t*setpR;
				float v = k*phiStep;
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
		float phiStep = (float) (2.0f*Math.PI/pointsPerCircle);
		float setpR = deltaR/innerCircleCount;
		for(int k = 0; k < innerCircleCount; k++){
			float currDist = setpR*k + wholeR;
			for(int t = 0; t < pointsPerCircle; t++){
				float x_tan = (float) -(currDist*Math.sin(phiStep*t));
				float y_tan = (float) (currDist*Math.cos(phiStep*t));
				float z_tan = 0.0f;
				
				float x_bitan = (float) (Math.cos(phiStep*t));
				float y_bitan = (float) (Math.sin(phiStep*t));
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
		
		for(int t = 0; t < innerCircleCount; t++){
			for(int k = 0; k < pointsPerCircle; k++){
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
		
//		System.out.println(counter + " colors");
		return col;
	}

	@Override
	protected int[] getTriangulationIndices() {
		List<Integer> indicesValues = new LinkedList<Integer>();
		int counter = 0;
		
		for(int k = 0; k < innerCircleCount-1; k++){
			for(int t = 0; t < pointsPerCircle; t++){
				int a = t+k*pointsPerCircle;
				int b = (t+1)%pointsPerCircle+k*pointsPerCircle;
				int c = t+(k+1)*pointsPerCircle;
//				System.out.println(a + " " + b + " " + c);
				
				indicesValues.add(a);
				indicesValues.add(b);
				indicesValues.add(c);
				counter++;
			}
		}
		
		for(int k = innerCircleCount-1; k > 0; k--){
			for(int t = 0; t < pointsPerCircle; t++){
				int a = t+k*pointsPerCircle;
				int b = (t+1)%pointsPerCircle+k*pointsPerCircle;
				int c = (t+1)%pointsPerCircle+((k-1)*pointsPerCircle);
				
				
				indicesValues.add(a);
				indicesValues.add(b);
				indicesValues.add(c);
//				System.out.println(a + " " + b + " " + c);
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
