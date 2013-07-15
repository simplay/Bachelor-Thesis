package Managers;

import java.util.LinkedList;

import Constants.ShaderTaskNr;

public class PatchDataPathsManager {
	private LinkedList<PatchDataPaths> constants;
	
	public PatchDataPathsManager(){
		this.constants = new LinkedList<PatchDataPaths>();
		defineConstant();
	}
	
	private void defineConstant(){
		PatchDataPaths sc = null;
		
		// Taylor series based experimental fragment/vertex shader patches
		sc = new PatchDataPaths(ShaderTaskNr.EXPERIMENTAL_F, "blaze");
		constants.add(sc);
		sc = new PatchDataPaths(ShaderTaskNr.EXPERIMENTAL_V, "blaze");
		constants.add(sc);
		sc = new PatchDataPaths(ShaderTaskNr.EXPERIMENTAL_F, "bump1d");
		constants.add(sc);	
		sc = new PatchDataPaths(ShaderTaskNr.EXPERIMENTAL_V, "bump1d");
		constants.add(sc);
		sc = new PatchDataPaths(ShaderTaskNr.TAYLORGAUSSIAN, "blaze");
		constants.add(sc);
		sc = new PatchDataPaths(ShaderTaskNr.TAYLORGAUSSIAN, "bump1d");
		constants.add(sc);
		
		sc = new PatchDataPaths(ShaderTaskNr.EXPERIMENTAL_V, "test");
		constants.add(sc);
		sc = new PatchDataPaths(ShaderTaskNr.EXPERIMENTAL_F, "test");
		constants.add(sc);
		sc = new PatchDataPaths(ShaderTaskNr.TAYLORGAUSSIAN, "test");
		constants.add(sc);
		
		
		// Taylor precomp patches
		sc = new PatchDataPaths(ShaderTaskNr.TAYLOR, "blaze");
		constants.add(sc);
		sc = new PatchDataPaths(ShaderTaskNr.TAYLOR, "cos");
		constants.add(sc);
		sc = new PatchDataPaths(ShaderTaskNr.TAYLOR, "newA");
		constants.add(sc);
		sc = new PatchDataPaths(ShaderTaskNr.TAYLOR, "pewpew");
		constants.add(sc);
		sc = new PatchDataPaths(ShaderTaskNr.TAYLOR, "w10");
		constants.add(sc);
		sc = new PatchDataPaths(ShaderTaskNr.TAYLOR, "w20");
		constants.add(sc);
		sc = new PatchDataPaths(ShaderTaskNr.TAYLOR, "w30");
		constants.add(sc);
		
		// grid precomp patches
		sc = new PatchDataPaths(ShaderTaskNr.GRID, "1dspec");
		constants.add(sc);
		sc = new PatchDataPaths(ShaderTaskNr.GRID, "1dw10");
		constants.add(sc);
		sc = new PatchDataPaths(ShaderTaskNr.GRID, "1dw20");
		constants.add(sc);
		sc = new PatchDataPaths(ShaderTaskNr.GRID, "1dw30");
		constants.add(sc);
		sc = new PatchDataPaths(ShaderTaskNr.GRID, "blaze");
		constants.add(sc);
		sc = new PatchDataPaths(ShaderTaskNr.GRID, "cos");
		constants.add(sc);	
	}
	
	public PatchDataPaths getPathsByIdentifiers(ShaderTaskNr task, String patchName){
		PatchDataPaths pa= null;
		for(PatchDataPaths path : constants){
			if(path.getShaderTaskNr() ==task && path.getPatchName().equals(patchName+"/")){
				pa = path;
				break;
			}
		}
		return pa;
	}
}
