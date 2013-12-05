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
								"stam", "lightZ", ShaderTaskNr.TAYLORGAUSSIAN, -1,  "blaze", 1, "random1");
								constants.add(sc);
		
//								sc = new SceneConfiguration("sandbox", ShapeTask.DICE3, "plane1_far", 
								sc = new SceneConfiguration("sandbox", ShapeTask.DICE_BRDF, "diceBRDF", 
								//sc = new SceneConfiguration("sandbox", ShapeTask.PLANE_UV, "planeUV", 
//								sc = new SceneConfiguration("sandbox", ShapeTask.SNAKE, "snake3", 
										"stam", "lightZ", ShaderTaskNr.TAYLORGAUSSIAN, 5,  "blaze", 1, "random1");
								constants.add(sc);
										
								
								
		sc = new SceneConfiguration("sandbox11", ShapeTask.SNAKE, "snake1", 
				"stam", "light1", ShaderTaskNr.TAYLORGAUSSIAN, 10, "blaze", 1, "random1");
		constants.add(sc);
			
		sc = new SceneConfiguration("sandbox2", ShapeTask.SNAKE, "snake1", 
				"xeno", "light1", ShaderTaskNr.TAYLORGAUSSIAN, -1, "xeno100", 2, "brick");
		constants.add(sc);
	
		sc = new SceneConfiguration("extreme_case", ShapeTask.SNAKE, "snake1", 
				"stam", "light1", ShaderTaskNr.TAYLORGAUSSIAN, -1, "test", 1, "random1");
		constants.add(sc);
		
		sc = new SceneConfiguration("snake", ShapeTask.SNAKE, "snake1", 
				"stam", "light1", ShaderTaskNr.EXPERIMENTAL_F, 26, "blaze", 1, "random1");
		constants.add(sc);
			
		sc = new SceneConfiguration("grayscale", ShapeTask.PLANE3, "plane1_far", 
				"stam", "light3", ShaderTaskNr.DEBUG_ANNOTATION, -1, "blaze", 1, "random1");
		constants.add(sc);
		
		// test case 1: plane equal long cylinder
		sc = new SceneConfiguration("xeno100", ShapeTask.SNAKE, "snake1", 
				"xeno", "light1", ShaderTaskNr.TAYLORGAUSSIAN, -1, "xeno100", 1, "random1");
		constants.add(sc);
		
		// test case 1: plane equal long cylinder
		sc = new SceneConfiguration("elaph100", ShapeTask.SNAKE, "snake1", 
				"elaph", "light1", ShaderTaskNr.TAYLORGAUSSIAN, -1, "elaph100", 1, "random1");
		constants.add(sc);
		
		// test case 1: plane equal long cylinder
		sc = new SceneConfiguration("testcase1_1", ShapeTask.CYLINDER, "plane1", 
				"stam", "light1", ShaderTaskNr.TAYLORGAUSSIAN, 26, "blaze", 2, "random1");
		constants.add(sc);
		
		// test case 1: plane equal long cylinder
		sc = new SceneConfiguration("testcase1_2", ShapeTask.PLANE, "plane1", 
				"stam", "light1", ShaderTaskNr.EXPERIMENTAL_F, 26, "blaze", 2, "random1");
		constants.add(sc);
		
		// test case for specular reflection.
		sc = new SceneConfiguration("spec_tc1", ShapeTask.PLANE, "plane1_o", 
				"stam", "light1", ShaderTaskNr.TAYLORGAUSSIAN, 26, "blaze", 2, "random1");
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
