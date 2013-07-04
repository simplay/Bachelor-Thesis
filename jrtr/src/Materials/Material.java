package Materials;

import java.io.IOException;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import jrtr.SWTexture;
import jrtr.Shader;
import jrtr.Texture;


/**
 * Stores the properties of a material. You will implement this 
 * class in the "Shading and Texturing" project.
 */
public class Material {
	private int N = 2624; // w=0.1, k=32, re,im
	private Shader shader;
    private Texture texture;
    private Texture[] textures = new Texture[N];
    private Texture glossmapTexture;
    private Vector3f materialColor;
    private Vector3f shinnyCoefficient;
    private Vector3f ambientCoefficient;
	private float[] heightfieldFactors;
	private float[] leftHF = new float[2624];
	private float[] rightHF = new float[2624]; 
	private float[] weights;
	private float[] globals;
	private float[] kValues;
	private float distanceToCamera;
	private Vector4f cop;
	private float phongExponent;
	private float trackDistance;
    private int layerCount;

    
    public Material(){

    }
    
    public void setCOP(Point3f cop){
    	Vector4f tmpCOP = new Vector4f(cop.x, cop.y, cop.z, 1.0f); 
    	this.cop = tmpCOP;
    }
    
    public Vector4f getCOP(){
    	return this.cop;
    }
    
    public Point3f getDistanceToCamera(){
    	return this.getDistanceToCamera();
    }
    
    public void setDistanceToCamera(float dist){
    	this.distanceToCamera = dist;
    }
    
    public int getLayerCount(){
    	return this.layerCount;
    }
    
    public Shader getShader(){
    	return this.shader;
    }
    
    public float getPhongExponent(){
    	return this.phongExponent;
    }
    
    public Vector3f getMaterialColor(){
    	return this.materialColor;
    }
    
    public Vector3f getShinnyCoefficient(){
    	return this.shinnyCoefficient;
    }
    
    public Vector3f getAmbientCoefficient(){
    	return this.ambientCoefficient;
    }
    
    public Texture getTexture(){
    	return this.texture;
    }
    
    public Texture getTextureAt(int at){
    	return this.textures[at];
    }

    public float[] getHeightfieldFactors(){
    	return this.heightfieldFactors;
    }
    
    public float getTrackDistance(){
    	return this.trackDistance;
    }
    
    public float[] getLeftHFFactors(){
    	return this.leftHF;
    }
    
    public float[] getRightHFFactors(){
    	return this.rightHF;
    }
    
    public float[] getGlobals(){
    	return this.globals;
    }
    
    public float[] getKValues(){
    	return this.kValues;
    }
    
    public float[] getWeights(){
    	return this.weights;
    }
    
    public void setGlobals(float[] globals){
    	this.globals = globals;
    }
    
    public void setKValues(float[] kValues){
    	this.kValues = kValues;
    }
    
    public void setWeights(float[] weights){
    	this.weights = weights;
    }
    
    public void setShader(Shader shader){
    	this.shader = shader;
    }
    
    public void setPhongExponent(float p){
    	this.phongExponent = p;
    }
    
    public void setTrackDistance(float d){
    	this.trackDistance = d;
    }
    
    public void setMaterialColor(Vector3f materialColor){
    	this.materialColor = materialColor;
    }
    
    public void setShinnyCoefficient(Vector3f shinnyCoefficient){
    	this.shinnyCoefficient = shinnyCoefficient;
    }
    
    public void setAmbientCoefficient(Vector3f ambientCoefficient){
    	this.ambientCoefficient = ambientCoefficient;
    }
    
    public void setLayerCount(int layerCount){
    	this.layerCount = layerCount;
    }
    
    public void setHeightfieldFactors(float[] heightfieldFactors){
    	int dim = heightfieldFactors.length;
    	if(dim >= 5248){
    		int counter = 0;
    		float[] left = new float[2624];
    		float[] right = new float[2624]; 
    		
    		for(Float f : heightfieldFactors){
    			if(counter <=2623){
    				left[counter] = f;
    			}else{
    				right[counter%2624] = f;
    			}
    			counter++;
    		}
    		this.leftHF = left;
    		this.rightHF = right;
    		
    	}
    	this.heightfieldFactors = heightfieldFactors;
    	
  
    }
    

    /**
     * set a texture by a path 
     * e.g. "../jrtr/textures/wood.jpg"
     * @param path path of the to be assigned texture.
     */
	public void setTexture(String path, Texture texture){
    	try {
    	    texture.load(path);
    	} catch (IOException e) {
    		System.out.println("could not load this texture with given path: " + path);
    	}
    	this.texture = texture;
    }
	
	public void setTextureAt(String path, Texture texture, int at){
    	try {
    	    texture.load(path);
    	} catch (IOException e) {
    		System.out.println("could not load this texture with given path: " + path);
    	}
    	this.textures[at] = texture;
    }
	
}
