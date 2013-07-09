package Managers;

import Constants.ShaderTaskNr;
import Constants.ShapeTask;

public class SceneConfiguration {
	
	private String id;
	private String parameter_path;
	private ShapeTask shapeTask;
	private String cameraConstant;
	private String bumpConstant;
	private String lightConstant;
	private ShaderTaskNr shaderTask;
	private String patchName;
	private int periodCount;
	
	public SceneConfiguration(String id, String parameter_path, ShapeTask shapeTask, String cameraConstant, 
			String bumpConstant, String lightConstant, ShaderTaskNr shaderTask, int periodCount, String patchName){
		this.id = id;
		this.parameter_path = parameter_path;
		this.shapeTask = shapeTask;
		this.cameraConstant = cameraConstant;
		this.bumpConstant = bumpConstant;
		this.lightConstant = lightConstant;
		this.shaderTask = shaderTask;
		this.periodCount = periodCount;
		this.patchName = patchName;
	}
	
	public String getId(){
		return this.id;
	}
	
	public String getParamter_path(){
		return this.parameter_path;
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
}
