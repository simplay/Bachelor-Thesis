package Materials;

import java.io.IOException;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import jrtr.Shader;
import jrtr.Texture;


/**
 * Stores the properties of a material. You will implement this 
 * class in the "Shading and Texturing" project.
 */
public class Material {
	private int N = 2624; // w=0.1, k=32, re,im
	private Shader shader;
    
	private Texture bodyTexture;
    private Texture[] textures = new Texture[N];
    private Texture bumpMapTexture;
    
    private Vector4f cop;
    private Vector3f materialColor;
    private Vector3f shinnyCoefficient;
    private Vector3f ambientCoefficient;
	
    private float[] heightfieldFactors;
	private float[] leftHF = new float[2624];
	private float[] rightHF = new float[2624]; 
	private float[] weights;
	private float[] globals;
	private float[] kValues;
	// 1 micron
	private float bruteforceSpacing = 1.0f;
	
    private int layerCount;
    private int stepCount;
    private int dimN;
    private int dimSmall;
    private int dimDiff;
    private int repNN;
    private int periodCount;
    private int neighborhoodRadius;
    
    private float patchDimX; //in microns  
    private float patchDimY; //in microns 
    private float patchSpacing; //in meters
    private float maxBumpHeight;
    private float lambdaMin;
    private float lambdaMax;
	private float phongExponent;
	private float trackDistance;
    
	private boolean renderBrdfMap;
	
    public Material(){}
    
    public float getBruteforceSpacing(){
    	return this.bruteforceSpacing;
    }
    
    public Texture getBumpMapTexture(){
    	return this.bumpMapTexture;
    }
    
    public void setNeighborhoodRadius(int neighborhoodRadius){
    	this.neighborhoodRadius = neighborhoodRadius;
    }
    
    public int getNeighborhoodRadius(){
    	return this.neighborhoodRadius;
    }
    
    public void setPatchDimX(float patchDimX){
    	this.patchDimX = patchDimX;
    }
    
    public float getPatchDimX(){
    	return this.patchDimX;
    }
    
    public void setPatchDimY(float patchDimY){
    	this.patchDimY = patchDimY;
    }
    
    public float getPatchDimY(){
    	return this.patchDimY;
    } 
    
    public void setPatchSpacing(float patchSpacing){
    	this.patchSpacing = patchSpacing;
    }
    
    public float getPatchSpacing(){
    	return this.patchSpacing;
    }
    
    public void setMaxBumpHeight(float maxBumpHeight){
    	this.maxBumpHeight = maxBumpHeight;
    }
    
    public float getMaxBumpHeight(){
    	return this.maxBumpHeight;
    }
    
    public void setPeriodCount(int periodCount){
    	this.periodCount = periodCount;
    }
    
    public int getPeriodCount(){
    	return this.periodCount;
    }
    
    public void setCOP(Point3f cop){
    	Vector4f tmpCOP = new Vector4f(cop.x, cop.y, cop.z, 1.0f); 
    	this.cop = tmpCOP;
    }
    
    public Vector4f getCOP(){
    	return this.cop;
    }
    
    public void setLambdaMin(float lambdaMin){
    	this.lambdaMin = lambdaMin;
    }
    
    public float getLambdaMin(){
    	return this.lambdaMin;
    }
    
    public void setLambdaMax(float lambdaMax){
    	this.lambdaMax = lambdaMax;
    }
    
    public float getLambdaMax(){
    	return this.lambdaMax;
    }
    
    public void setBruteForceSpacing(float value){
    	this.bruteforceSpacing = value;
    }
    
    public void setStepCount(int stepCount){
    	this.stepCount = stepCount;
    }
    
    public int getStepCount(){
    	return this.stepCount;
    }
    
    public void setDimN(int dimN){
    	this.dimN = dimN;
    }
    
    public int getDimN(){
    	return this.dimN;
    }
    
    public void setSmall(int dimSmall){
    	this.dimSmall = dimSmall;
    }
    
    public int getDimSmall(){
    	return this.dimSmall;
    }
    
    public void setDimDiff(int dimDiff){
    	this.dimDiff = dimDiff;
    }
    
    public int getDimDiff(){
    	return this.dimDiff;
    }
    
    public void setRepNN(int repNN){
    	this.repNN = repNN;
    }
    
    public int getRepNN(){
    	return this.repNN;
    }
    
    public Point3f getDistanceToCamera(){
    	return this.getDistanceToCamera();
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
    
    public Texture getBodyTexture(){
    	return this.bodyTexture;
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
    
    public boolean getRenderBrdfMap(){
    	return this.renderBrdfMap;
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
	public void setBodyTexture(String path, Texture bodyTexture){
    	try {
    	    bodyTexture.load(path);
    	} catch (IOException e) {
    		System.err.println("could not load texture with given path: " + path);
    	}

    	this.bodyTexture = bodyTexture;
    }
	
	public void setBumpMapTexture(String path, Texture bumpMaxpTexture){
    	try {
    	    bumpMaxpTexture.load(path);
    	} catch (IOException e) {
    		System.err.println("could not load this texture with given path: " + path);
    	}

    	this.bumpMapTexture = bumpMaxpTexture;
    }
	
	public void setTextureAt(String path, Texture texture, int at){
    	try {
    	    texture.load(path);
    	} catch (IOException e) {
    		System.out.println("could not load this texture with given path: " + path);
    	}
    	this.textures[at] = texture;
    }
	
	public void setRenderBrdfMap(boolean renderBrdfMap){
		this.renderBrdfMap = renderBrdfMap;
	}
	
}
