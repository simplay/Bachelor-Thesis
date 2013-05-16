package jrtr;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

/**
 * Stores the properties of a light source. To be implemented for 
 * the "Texturing and Shading" project.
 * Light has: 
 * a color(radiance) c_l 
 * and unit direction L
 */
public class Light {
    private Vector3f radiance;
    private Vector4f lightDirection;
    private String name;
    
    /**
     * Constructor of a light beam.
     * @param radiance "color" of beam.
     * @param lightDirection beam's direction.
     */
    public Light(Vector3f radiance, Vector4f lightDirection, String name){
    	this.radiance = radiance;
    	this.lightDirection = lightDirection;
    	this.name = name;
    }
    
    /**
     * get this light beam's radiance.
     * @return radiance of this light beam.
     */
    public Vector3f getRadiance(){
    	return this.radiance;
    }
    
    /**
     * get this light beam's direction.
     * @return light direction of this light beam.
     */
    public Vector4f getLightDirection(){
    	return this.lightDirection;
    }
    
    /**
     * set new radiance for this light source
     * @param radiance
     */
    public void setRadiance(Vector3f radiance){
    	this.radiance = radiance;
    }
    
    /**
     * set new light direction for this light source.
     * @param lightDirection
     */
    public void setLightDirection(Vector4f lightDirection){
    	this.lightDirection = lightDirection;
    }
    
    /**
     * get name of this light source
     * @return
     */
    public String getName(){
    	return this.name;
    }
}
