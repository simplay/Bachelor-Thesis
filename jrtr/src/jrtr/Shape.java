package jrtr;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import jrtr.VertexData.VertexElement;

import Materials.Material;
import ShaderLogic.ShaderTask;

/**
 * Represents a 3D shape. The shape currently just consists
 * of its vertex data. It should later be extended to include
 * material properties, shaders, etc.
 * also calculate the bounding sphere radius of this shape
 * 
 * @author Michael Single
 */
public class Shape {
	private VertexData vertexData;
	private Matrix4f transformationMatrix;
	private Material material;
	private ShaderTask activeShader;
	private float boundingSphereRadius = 0.0f;
	private List<Float> positions;
    private LinkedList<VertexData.VertexElement> vertexDataList;
    private Vector4f center;
    
	/**
	 * Make a shape from {@link VertexData}.
	 * @param vertexData the vertices of the shape.
	 */
	public Shape(VertexData vertexData){
		this.vertexData = vertexData;
		transformationMatrix = new Matrix4f();
		transformationMatrix.setIdentity();
		
		this.boundingSphereRadius = 0;
		this.positions = new ArrayList<Float>();
		this.vertexDataList = this.vertexData.getElements();
		this.getPositions();
		this.calculateBoundingSphere();
	}
	
	/**
	 * calculates radius of bounding sphere
	 * the bounding sphere itself it the a sphere 
	 * which contains all points of the given shape
	 * such that r is minimal or at least almost minimal.
	 * but care: this sphere is not necessary the possible smallest
	 * sphere which contains all points of given shape.
	 * since we are going to apply a conservative test 
	 * this is no problem at all.
	 */
	private void calculateBoundingSphere(){
		boundingSphereRadius = 0;
		float average = positions.size() / 3;
		float xSum = 0;
		float ySum = 0;
		float zSum = 0;
		
		
		// Calculate the center point in object coordinates
		// which is an average value among all points of shape.
		for (int i = 0; i < positions.size(); i = i + 3) {
		    xSum = xSum + positions.get(i);
		    ySum = ySum + positions.get(i + 1);
		    zSum = zSum + positions.get(i + 2);
		}

		float xCenter = xSum / average;
		float yCenter = ySum / average;
		float zCenter = zSum / average;
		center = new Vector4f(xCenter, yCenter, zCenter, 0);
		
		// Calculate smallest possible radius from center to its points such that 
		// all points of this shape are within the bounding sphere volume with that radius.
		// e is the vector pointing to a considered point of this shape (in object coordinates)
		// r is vector pointing from center to the considered point of shape.
		// calculate r_vec3 = e_vec3 - center_vec, r_vec3 is new candidate radius.
		for (int i = 0; i < positions.size(); i = i + 3) {
		    Vector3f e = new Vector3f(positions.get(i), positions.get(i + 1), positions.get(i + 2));
		    Vector3f r = new Vector3f(e.x - center.x, e.y - center.y, e.z - center.z);
		    float candidateRadius = r.length();
		    if (candidateRadius > boundingSphereRadius) boundingSphereRadius = candidateRadius;
		}
	}
	
	/**
	 * get all positions of this shape and store them in positions
	 */
    private void getPositions() {
		for (VertexElement element : vertexDataList) {
		    String semantic = element.getSemantic().toString();
	
		    if (semantic.equals("POSITION")) {
				float[] vertices = element.getData();
				for (int i = 0; i < vertices.length; i = i + 3) {
				    positions.add(vertices[i]);
				    positions.add(vertices[i + 1]);
				    positions.add(vertices[i + 2]);
				}
		    }
		}
    }
	
    /**
     * get vertex data of this shape.
     * @return
     */
	public VertexData getVertexData(){
		return vertexData;
	}
	
	/**
	 * set new transformation matrix for this shape.
	 * @param transformation
	 */
	public void setTransformation(Matrix4f transformation){
		this.transformationMatrix = transformation;
	}
	
	/**
	 * get this shape's transformation matrix.
	 * @return returns this transformation matrix.
	 */
	public Matrix4f getTransformation(){
		return transformationMatrix;
	}
	
	/**
	 * set the material for this shape.
	 * @param material material of this shape.
	 */
	public void setMaterial(Material material){
		this.material = material;
	}
	
	/**
	 * set the material for this shape.
	 * @param material material of this shape.
	 */
	public void setCloneCMCMaterial(Material material, Texture tex){	
		Material mat = new Material();
		mat = new Material();
		mat.setMaterialColor(material.getMaterialColor());
		mat.setShinnyCoefficient(material.getShinnyCoefficient());
		mat.setAmbientCoefficient(material.getAmbientCoefficient());
		mat.setPhongExponent(material.getPhongExponent());
		mat.setShader(material.getShader());
		//mat.setTexture(tex);
		//mat.setGlossmapTexture(material.getGlossmapTexture());
		this.material = mat;
	}

	/**
	 * get the material of this shape.
	 * @return return material of this shape.
	 */
	public Material getMaterial(){
		return this.material;
	}
	
	/**
	 * set a shader task for this shape.
	 * @param shader
	 */
	public void setShaderTask(ShaderTask shader){
		this.activeShader = shader;
	}
	
	/**
	 * get the shader task assigned for this shape.
	 * @return
	 */
	public ShaderTask getShaderTask(){
		return this.activeShader;
	}
	
	/**
	 * get radius of bounding sphere of this shape.
	 * @return
	 */
	public float getBoundingSphereRadius(){
		return this.boundingSphereRadius;
	}
	
	/**
	 * get vector pointing to averages center point 
	 * of points of this shape.
	 * @return
	 */
	public Vector4f getCenter(){
		return this.center;
	}

}
