package Setup.Constants;

import Constants.ShaderTaskNr;
import Constants.ShapeTask;

public class SceneConfiguration {
	
	private String id;
	private ShapeTask shapeTask;
	private String cameraConstant;
	private String bumpConstant;
	private String lightConstant;
	private ShaderTaskNr shaderTask;
	private String patchName;
	private int periodCount;
	private int neighborhoodRadius;
	private String textureId;
	
	public SceneConfiguration(String id, ShapeTask shapeTask, String cameraConstant, 
			String bumpConstant, String lightConstant, ShaderTaskNr shaderTask, 
			int periodCount, String patchName, int neighborhoodRadius, String textureId){
		this.id = id;
		this.shapeTask = shapeTask;
		this.cameraConstant = cameraConstant;
		this.bumpConstant = bumpConstant;
		this.lightConstant = lightConstant;
		this.shaderTask = shaderTask;
		this.periodCount = periodCount;
		this.patchName = patchName;
		this.neighborhoodRadius = neighborhoodRadius;
		this.textureId = textureId;
	}
	
	public String getId(){
		return this.id;
	}
	
	public ShapeTask getShapeTask(){
		return this.shapeTask;
	}
	
	public String getCameraConstant(){
		return this.cameraConstant;
	}
	
	public String getBumpConstant(){
		return this.bumpConstant;
	}
	
	public String getLightConstant(){
		return this.lightConstant;
	}
	
	public ShaderTaskNr getShaderTask(){
		return this.shaderTask;
	}
	
	public int getPeriodCount(){
		return this.periodCount;
	}
	
	public String getPatchName(){
		return this.patchName;
	}
	
	public int getNeighborhoodRadius(){
		return this.neighborhoodRadius;
	}
	
	public String getTextureId(){
		return this.textureId;
	}
}
