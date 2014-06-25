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
			int periodCount, String patchName, int neighborhoodRadius, String textureId, boolean renderBrdfMap){
	 */
	private void defineConstant(){
		SceneConfiguration sc = null;	
				sc = new SceneConfiguration("flss_map", ShapeTask.DICE_BRDF, "diceBRDF", 
						"stam", "lightDebug", ShaderTaskNr.FLSS, 5,  "blaze", 1, "ProcessedElapheFront", true);
				constants.add(sc);
				
				sc = new SceneConfiguration("nmm_map", ShapeTask.DICE_BRDF, "diceBRDF", 
						"stam", "lightDebug", ShaderTaskNr.NMM, 5,  "blaze", 1, "ProcessedElapheFront", true);
				constants.add(sc);
				
				sc = new SceneConfiguration("pq_map", ShapeTask.DICE_BRDF, "diceBRDF", 
						"stam", "lightDebug", ShaderTaskNr.PQ, 5,  "blaze", 1, "ProcessedElapheFront", true);
				constants.add(sc);
				
				sc = new SceneConfiguration("gem_map", ShapeTask.DICE_BRDF, "diceBRDF", 
						"stam", "lightDebug", ShaderTaskNr.GEM, 5,  "blaze", 1, "ProcessedElapheFront", true);
				constants.add(sc);
				
				sc = new SceneConfiguration("flss_snake", ShapeTask.SNAKE, "snake3",
						"stam", "lightDebug", ShaderTaskNr.FLSS, 5,  "blaze", 1, "ProcessedElapheFront", false);
				constants.add(sc);
				
				sc = new SceneConfiguration("nmm_snake", ShapeTask.SNAKE, "snake3",
						"stam", "lightDebug", ShaderTaskNr.NMM, 5,  "blaze", 1, "ProcessedElapheFront", false);
				constants.add(sc);
				
				sc = new SceneConfiguration("pq_snake", ShapeTask.SNAKE, "snake3",
						"stam", "lightDebug", ShaderTaskNr.PQ, 5,  "blaze", 1, "ProcessedElapheFront", false);
				constants.add(sc);
				
				sc = new SceneConfiguration("gem_snake", ShapeTask.SNAKE, "snake3",
						"stam", "lightDebug", ShaderTaskNr.GEM, 5,  "blaze", 1, "ProcessedElapheFront", false);
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
