package Managers;

import java.util.LinkedList;


import Constants.ShaderTaskNr;
import Constants.ShapeTask;


public class SceneConfigurationManager {
	
	private LinkedList<SceneConfiguration> constants;
	public SceneConfigurationManager(){
		constants = new LinkedList<SceneConfiguration>();
		defineConstant();
	}
	
	private void defineConstant(){
		SceneConfiguration sc = null;		
		sc = new SceneConfiguration("sandbox", ShapeTask.PLANE, "plane1", 
				"Stam", "light1", ShaderTaskNr.TAYLORGAUSSIAN, 26, "test");
		constants.add(sc);
		
		sc = new SceneConfiguration("extreme_case", ShapeTask.SNAKE, "snake1", 
				"Stam", "light1", ShaderTaskNr.TAYLORGAUSSIAN, 26, "test");
		constants.add(sc);
		
		sc = new SceneConfiguration("snake", ShapeTask.SNAKE, "snake1", 
				"Stam", "light1", ShaderTaskNr.EXPERIMENTAL_F, 26, "blaze");
		constants.add(sc);
		
		
		// tast case 1: plane equal long cylinder
		sc = new SceneConfiguration("testcase1_1", ShapeTask.CYLINDER, "plane1", 
				"Stam", "light1", ShaderTaskNr.TAYLORGAUSSIAN, 26, "blaze");
		constants.add(sc);
		
		// tast case 1: plane equal long cylinder
		sc = new SceneConfiguration("testcase1_2", ShapeTask.PLANE, "plane1", 
				"Stam", "light1", ShaderTaskNr.EXPERIMENTAL_F, 26, "blaze");
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
