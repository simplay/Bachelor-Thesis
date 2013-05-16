package Materials;

import java.io.IOException;

import javax.vecmath.Vector3f;

import jrtr.SWTexture;
import jrtr.Shader;
import jrtr.Texture;

public class CubeMapMaterial extends Material{
	private Shader shader;
    private Texture glossmapTexture;
    private Texture front, back, left, right, top, bottom;
    
    private Vector3f materialColor;
    private Vector3f shinnyCoefficient;
    private Vector3f ambientCoefficient;
	private float phongExponent;
	private float trackDistance;
    
    public CubeMapMaterial(){
    	front = new SWTexture();
    	back = new SWTexture();
    	left = new SWTexture();
    	right = new SWTexture();
    	top = new SWTexture();
    	bottom = new SWTexture();
    	glossmapTexture = new SWTexture();
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
    
    public Texture getFrontTexture(){
    	return this.front;
    }
    
    public Texture getBackTexture(){
    	return this.back;
    }
    
    public Texture getLeftTexture(){
    	return this.left;
    }
    
    public Texture getRightTexture(){
    	return this.right;
    }
    
    public Texture getTopTexture(){
    	return this.top;
    }
    
    public Texture getBottomTexture(){
    	return this.bottom;
    }
    
    public Texture getGlossmapTexture(){
    	return this.glossmapTexture;
    }
    
    public float getTrackDistance(){
    	return this.trackDistance;
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
    
    
    public void setGlossmapTexture(Texture glossmapTexture){
    	this.glossmapTexture = glossmapTexture;
    }
    
    /**
     * set a texture by a path 
     * e.g. "../jrtr/textures/wood.jpg"
     * @param path path of the to be assigned texture.
     */
	public void setTexture(String front, String back, String left, String right, String top, String bottom, Texture[] textures){
    	try {
    	    textures[0].load(front);
    	    textures[1].load(back);
    	    textures[2].load(left);
    	    textures[3].load(right);
    	    textures[4].load(top);
    	    textures[5].load(bottom);
    	} catch (IOException e) {
    		System.out.println("error loading textures for cubemap cube");
    	}
    	this.front = textures[0];
    	this.back = textures[1];
    	this.left = textures[2];
    	this.right = textures[3];
    	this.top = textures[4];
    	this.bottom = textures[5];
    }
	
	
	public void setGlossmapTexture(String path, Texture gmtexture){
    	try {
    	    gmtexture.load(path);
    	} catch (IOException e) {
    		System.out.println("could not load this texture with given path: " + path);
    	}
    	this.glossmapTexture = gmtexture;
    }
}
