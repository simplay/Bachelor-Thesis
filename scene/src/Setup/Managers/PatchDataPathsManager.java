package Setup.Managers;

import java.util.LinkedList;

import Setup.Constants.PatchDataPaths;

import Constants.ShaderTaskNr;

public class PatchDataPathsManager {
	private LinkedList<PatchDataPaths> constants;
	
	public PatchDataPathsManager(){
		this.constants = new LinkedList<PatchDataPaths>();
		defineConstant();
	}
	
	private void defineConstant(){
		PatchDataPaths sc = null;
		
		sc = new PatchDataPaths(ShaderTaskNr.FLSS, "flss");
		constants.add(sc);
		
		sc = new PatchDataPaths(ShaderTaskNr.GEM, "gem");
		constants.add(sc);
		
		
		// Taylor series based experimental fragment/vertex shader patches
		sc = new PatchDataPaths(ShaderTaskNr.TAYLORGAUSSIAN, "blaze");
		constants.add(sc);
		sc = new PatchDataPaths(ShaderTaskNr.TAYLORGAUSSIAN, "bump1d");
		constants.add(sc);	
		sc = new PatchDataPaths(ShaderTaskNr.TAYLORGAUSSIAN, "test");
		constants.add(sc);
		sc = new PatchDataPaths(ShaderTaskNr.TAYLORGAUSSIAN, "lookupBlaze");
		constants.add(sc);		
		sc = new PatchDataPaths(ShaderTaskNr.TAYLORGAUSSIAN, "xeno100");
		constants.add(sc);	
		sc = new PatchDataPaths(ShaderTaskNr.TAYLORGAUSSIAN, "elaph100");
		constants.add(sc);	

		
		// debug
		sc = new PatchDataPaths(ShaderTaskNr.DEBUG_ANNOTATION, "blaze");
		constants.add(sc);
		sc = new PatchDataPaths(ShaderTaskNr.DEBUG_SPECULAR, "blaze");
		constants.add(sc);
	}
	
	public PatchDataPaths getPathsByIdentifiers(ShaderTaskNr task, String patchName){
		return constants.get(0);
	}
}
