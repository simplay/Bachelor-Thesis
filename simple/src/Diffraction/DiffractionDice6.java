/**
DiffractionDice4.java * nice input values for constructor:
 * DiffractionDice diffDiceObj = new DiffractionDice(45, 100, 0.1f);
 */

package Diffraction;


import java.util.LinkedList;
import java.util.List;

import jrtr.VertexData;

public class DiffractionDice6 extends DiffractionGeometricObject{

	private int trackCount;
	private float distance;
	private float basisRadius;
	private float scale = 4.0f;
	
	public DiffractionDice6(int segmentCount, int trackCount, float distance){
		this.segmentCount = segmentCount;
		this.basisRadius = 1.5f/10f;
		this.trackCount = trackCount;
		this.distance = 0.025f;
		this.setupObject();
		this.tangentVectors = this.getTangentVectors();
		this.vertexData.addElement(this.tangentVectors, VertexData.Semantic.TANGENT, 3);
	}
	

	@Override
	protected float[] getTangentVectors() {
		List<Float> tangentVectors = new LinkedList<Float>();
		
		double phi = 2*Math.PI / segmentCount;	
		int counter = 0;

		for(int track=0; track < trackCount; track++){
			for(double p = 0; p < 2*Math.PI; p += phi){
				float radius = basisRadius + track*distance;
				float x = (float) (-scale*radius*Math.sin(p)); 
				float y = (float) (scale*radius*Math.cos(p));
				float z = 0.0f;
				tangentVectors.add(x);
				tangentVectors.add(y);
				tangentVectors.add(z);
			
				counter++;
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
	
	/**
	 * parametrization of surface is dependent on a radius r and and a angle phi
	 * (r,phi) |-> (r cos(phi), r(sin(phi), height) = f(r,phi) and height is a constant  
	 * so (r,phi) in R^2 -> f(r,phi) in R^3 and f(r,phi) is our parametrization of 
	 * the surface of this dice.
	 */
	
	@Override
	protected float[] getVertexPositions() {
		List<Float> VertexPositions = new LinkedList<Float>();
		
		double phi = 2*Math.PI / segmentCount;	
		int counter = 0;
		
		for(int track=0; track < trackCount; track++){
			for(double p = 0; p < 2*Math.PI; p += phi){
				float radius = basisRadius + track*distance;
				float x = (float) (radius*Math.cos(p)); 
				float y = (float) (radius*Math.sin(p));
				float z = 0.0f;
				VertexPositions.add(x);
				VertexPositions.add(y);
				VertexPositions.add(z);
				
				counter++;
			}
		}
				
		// fill our vertices array by using an iterator for our list.
		float[] vertices = new float[VertexPositions.size()];
		int i = 0;
		for(Float value : VertexPositions){
			vertices[i] = value.floatValue();
			i++;
		}
		
//		System.out.println(vertices.length/3 + " vetices");
		return vertices;
	}
	
	/**
	 * TODO fix this is buggy...
	 * foreach track of tracks foreach point on track
	 * divide mesh into those points
	 * xValue = 1.0f / (trackCount+1);
	 * yValue = 1.0f / (segmentCount+1);
	 * in order to have equidistant texture coordinates
	 */
	@Override
	protected float[] getTextureCoordinates() {
		
		List<Float> texCoords = new LinkedList<Float>();
		
//		float xValue = 1.0f / (trackCount);
		float yValue = 1.0f / (segmentCount);
		
//		int trackRuns = trackCount;
		int segmentRuns = segmentCount;
		
		int counter = 0;

		for(int p = 0; p < segmentRuns; p++){
			float x = 1- yValue*p;
				//System.out.println(x);
			float y = 1- 0;
			texCoords.add(x);
			texCoords.add(y);
			counter++;
		}
			
		for(int p = 0; p < segmentRuns; p++){
			float x = 1- yValue*p;
			float y = 1- 1;
			texCoords.add(x);
			texCoords.add(y);
			counter++;
		}
			
		float x = yValue*segmentRuns;
		float y = 0;
		texCoords.add(x);
		texCoords.add(y);
		counter++;
			
		x = yValue*segmentRuns;
		y = 1;
		texCoords.add(x);
		texCoords.add(y);
		counter++;

		
		float[] texC = new float[texCoords.size()];
		int i = 0;
		for(Float value : texCoords){
			texC[i] = value.floatValue();
			i++;
		}
//		System.out.println(counter + " texcoords");
		return texC;
	}
	
	/**
	 * normals on surface all equal (0,0,1) point upwards, since
	 * dice is a "flat" plane.
	 * another reason that all normals for the dice are equal (0,0,1) is:
	 * n(r,phi) =  crossProd(df(r,phi)/dr, df(r,ph)/d(phi)) / ||df(r,phi)/dr, df(r,ph)/d(phi)|| 
	 * which is equal (0,0,1) for a dice, where f(r,phi) is the parameterization
	 * for the surface of a dice
	 */
	
	@Override
	protected float[] getNormals() {
		
		List<Float> normalVectors = new LinkedList<Float>();
		
		double phi = 2*Math.PI / segmentCount;	
		int counter = 0;
		
		for(int track=0; track < trackCount; track++){
			for(double p = 0; p < 2*Math.PI; p += phi){
				normalVectors.add(0.0f);
				normalVectors.add(0.0f);
				normalVectors.add(1.0f);
				counter++;
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
	
	/**
	 * default color for each vertex: red
	 */
	@Override
	protected float[] getVertexColors() {
		List<Float> colorVectors = new LinkedList<Float>();
		
		double phi = 2*Math.PI / segmentCount;	
		int counter = 0;
		
		for(int track=0; track < trackCount; track++){
			for(double p = 0; p < 2*Math.PI; p += phi){
				colorVectors.add(0.0f);
				colorVectors.add(0.0f);
				colorVectors.add(1.0f);
				counter++;
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
		List<Integer> indicesValues = new LinkedList<Integer>();
		int counter = 0;
		// triangulate two points from inner track layer with one 
		// from the next outer layer until we have triangulated circular
		// all points from the inner track layer with one in the outer layer
		// after a whole inner layer has been triangulated with a point from
		// the outer layer updates layer:
		// new inner track layer becomes previous outer track layer
		// new outer layer becomes next layer after previous outer track layer
		// care: indices of triangulation should be order counter-clockwise 
		for(int t = 0; t < trackCount-1; t++){
			for(int r = 0; r < segmentCount; r++){
				int a = r + t*segmentCount;
				int b = ((r+1) + t*segmentCount);
				int c = r + (t+1)*segmentCount;
				
				//index correction for last vertex on current track
				if(r+1 == segmentCount) b-=segmentCount; 
					
				// order point indices counter clock wise
				
				
				System.out.println(a + " " + b + " " +c);
				indicesValues.add(a);
				indicesValues.add(c);
				indicesValues.add(b);

				counter++;
			}
		}
		

		// triangulate two points from the outer track layer 
		// with one from the inner track layer.
		// proceed analogous like described above.
		// inner loop till trackCount-1: since there is no outer layer
		// after the last outer layer
		for(int t = 0; t < trackCount-1; t++){
			for(int r = 0; r < segmentCount; r++){
				int a = r + (t+1)*segmentCount;
				int b = ((r+1) + (t+1)*segmentCount);
				int c = (r+1) + t*segmentCount;
				
				//index correction for last vertex on current track
				if(r+1 == segmentCount){
					b-=segmentCount;
					c-=segmentCount;
				}
				
				
				indicesValues.add(a);
				indicesValues.add(b);
				indicesValues.add(c);
				counter++;
			}
		}
		
		int[] indices = new int[indicesValues.size()];
		int i = 0;
		for(Integer value : indicesValues){
			indices[i] = value.intValue();
			i++;
		}
//		System.out.println(counter + " triangulations" + " and it should be: " + (1)*segmentCount*2);
		return indices;
	}

	@Override
	protected int getVerticesCount() {
		return this.vetices.length/3;
	}
	
}
