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
		sc = new SceneConfiguration("sandbox", "../jrtr/textures/sampleX/experimental/blaze/paramters.txt", 
				ShapeTask.PLANE, "plane1", "Stam", "light1", ShaderTaskNr.EXPERIMENTAL, 26);
		constants.add(sc);
		
		// tast case 1: plane equal long cylinder
		sc = new SceneConfiguration("testcase1", "../jrtr/textures/sampleX/experimental/blaze/paramters.txt", 
				ShapeTask.CYLINDER, "plane1", "Stam", "light1", ShaderTaskNr.EXPERIMENTAL, 26);
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
