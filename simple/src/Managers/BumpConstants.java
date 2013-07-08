package Managers;

public class BumpConstants {
	private String identifyerName;
	private float maxHeight;
	private float dimX;
	private float dimY;
	private float spacing;
	
	public BumpConstants(String identifyerName, float maxHeight, float dimX, float dimY, float spacing){
		this.identifyerName = identifyerName;
		this.maxHeight = maxHeight;
		this.dimX = dimX;
		this.dimY = dimY;
		this.spacing = spacing;
	}
	
	public String getIdentifyerName(){
		return this.identifyerName;
	}
	
	public float getMaxHeight(){
		return this.maxHeight;
	}
	
	public float getDimX(){
		return this.dimX;
	}
	
	public float getDimY(){
		return this.dimY;
	}
	
	public float getSpacing(){
		return this.spacing;
	}
}
