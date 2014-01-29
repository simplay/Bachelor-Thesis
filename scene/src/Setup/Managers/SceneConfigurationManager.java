package Setup.Managers;

import java.util.LinkedList;

import Setup.Constants.SceneConfiguration;


import Constants.ShaderTaskNr;
import Constants.ShapeTask;

public class SceneConfigurationManager {
	
	private LinkedList<SceneConfiguration> constants;
	public SceneConfigurationManager(){
		constants = new LinkedList<SceneConfiguration>();
		defineConstant();
	}
	
	
	/*
	public SceneConfiguration(String id, ShapeTask shapeTask, String cameraConstant, 
			String bumpConstant, String lightConstant, ShaderTaskNr shaderTask, 
			int periodCount, String patchName, int neighborhoodRadius, String textureId){
	 */
	
	private void defineConstant(){
		SceneConfiguration sc = null;		
		
//		sc = new SceneConfiguration("evaluation", ShapeTask.DICE_BRDF, "diceBRDF", 
//				"stam", "lightZ", ShaderTaskNr.TAYLORGAUSSIAN, 5,  "blaze", 1, "random1");
//				constants.add(sc);
				
//				sc = new SceneConfiguration("evaluation", ShapeTask.DICE_BRDF, "diceBRDF", 
//						"stam", "lightZ", ShaderTaskNr.TAYLORGAUSSIAN, -1,  "blaze", 1, "random1");
//						constants.add(sc);	
//						
						
						sc = new SceneConfiguration("evaluation", ShapeTask.DICE_BRDF, "diceBRDF", 
								"stam", "lightZ", ShaderTaskNr.TAYLORGAUSSIAN, -1,  "blaze", 1, "random1",false);
								constants.add(sc);
		

								sc = new SceneConfiguration("sandbox", ShapeTask.DICE_BRDF, "diceBRDF", 
//								sc = new SceneConfiguration("sandbox", ShapeTask.SNAKE, "snake3", 
										"stam", "lightDebug", ShaderTaskNr.TAYLORGAUSSIAN, 5,  "blaze", 1, "ProcessedElapheFront", false);
								constants.add(sc);
										
			
		
	}
	
	public SceneConfiguration getSceneConfigurationConstantByName(String id){
		SceneConfiguration conf= null;
		for(SceneConfiguration config : constants){
			if(config.getId().equals(id)){
				conf = config;
				break;
			}
		}
		return conf;
	}
}
