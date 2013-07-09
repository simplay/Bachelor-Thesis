package Managers;

import java.util.LinkedList;

import Constants.ShaderTaskNr;
import Materials.Material;

public class PatchDataPathsManager {
	private LinkedList<PatchDataPaths> constants;
	
	public PatchDataPathsManager(){
		this.constants = new LinkedList<PatchDataPaths>();
		defineConstant();
	}
	
	private void defineConstant(){
		PatchDataPaths sc = null;
		sc = new PatchDataPaths(ShaderTaskNr.EXPERIMENTAL_F, "blaze");
		constants.add(sc);
		sc = new PatchDataPaths(ShaderTaskNr.EXPERIMENTAL_V, "blaze");
		constants.add(sc);
	}
	
	public PatchDataPaths getSceneConfigurationConstantByName(ShaderTaskNr task, String patchName){
		PatchDataPaths pa= null;
		for(PatchDataPaths path : constants){
			if(path.getShaderTaskNr().equals(task) && path.getPatchName().equals(patchName)){
				pa = path;
				break;
			}
		}
		return pa;
	}
}
