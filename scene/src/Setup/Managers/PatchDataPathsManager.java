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
		
		sc = new PatchDataPaths(ShaderTaskNr.NMM, "nmm");
		constants.add(sc);
		
		sc = new PatchDataPaths(ShaderTaskNr.PQ, "pq");
		constants.add(sc);
		
		
	}
	
	public PatchDataPaths getPathsByIdentifiers(ShaderTaskNr task, String patchName){
		return constants.get(0);
	}
}
