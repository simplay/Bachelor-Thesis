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
	 * 	public SceneConfiguration(String id, ShapeTask shapeTask, String cameraConstant, 
			String bumpConstant, String lightConstant, ShaderTaskNr shaderTask, 
			int periodCount, String patchName, int neihborhoodRadius){
	 */
	
	private void defineConstant(){
		SceneConfiguration sc = null;		
		sc = new SceneConfiguration("sandbox", ShapeTask.PLANE, "plane1", 
				"xeno", "light1", ShaderTaskNr.TAYLORGAUSSIAN, -1, "xeno100", 2);
		constants.add(sc);
	
		sc = new SceneConfiguration("extreme_case", ShapeTask.SNAKE, "snake1", 
				"Stam", "light1", ShaderTaskNr.TAYLORGAUSSIAN, -1, "test", 1);
		constants.add(sc);
		
		sc = new SceneConfiguration("snake", ShapeTask.SNAKE, "snake1", 
				"Stam", "light1", ShaderTaskNr.EXPERIMENTAL_F, 26, "blaze", 1);
		constants.add(sc);
		
		
		// test case 1: plane equal long cylinder
		sc = new SceneConfiguration("xeno100", ShapeTask.SNAKE, "snake1", 
				"xeno", "light1", ShaderTaskNr.TAYLORGAUSSIAN, -1, "xeno100", 1);
		constants.add(sc);
		
		// test case 1: plane equal long cylinder
		sc = new SceneConfiguration("elaph100", ShapeTask.SNAKE, "snake1", 
				"elaph", "light1", ShaderTaskNr.TAYLORGAUSSIAN, -1, "elaph100", 1);
		constants.add(sc);
		
		// test case 1: plane equal long cylinder
		sc = new SceneConfiguration("testcase1_1", ShapeTask.CYLINDER, "plane1", 
				"Stam", "light1", ShaderTaskNr.TAYLORGAUSSIAN, 26, "blaze", 2);
		constants.add(sc);
		
		// test case 1: plane equal long cylinder
		sc = new SceneConfiguration("testcase1_2", ShapeTask.PLANE, "plane1", 
				"Stam", "light1", ShaderTaskNr.EXPERIMENTAL_F, 26, "blaze", 2);
		constants.add(sc);
		
		// test case for specular reflection.
		sc = new SceneConfiguration("spec_tc1", ShapeTask.PLANE, "plane1_o", 
				"Stam", "light1", ShaderTaskNr.TAYLORGAUSSIAN, 26, "blaze", 2);
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
