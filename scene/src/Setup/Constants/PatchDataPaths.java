package Setup.Constants;

import Constants.ShaderTaskNr;

public class PatchDataPaths {
	private ShaderTaskNr shaderTaskNr;
	private String patchName;
	private String samplesPath; // = "../jrtr/textures/sampleX/experimental/blaze/";
	private String extremaPath; // = "../jrtr/textures/sampleX/experimental/blaze/extrema.txt";
	private String globalsPath; //mat.setGlobals(loadglobals("../jrtr/textures/sampleX/experimental/blaze/globals.txt"));
	private String weightsPath; //mat.setWeights(readWeights("../jrtr/textures/sampleX/experimental/blaze/weights.txt"));
	private String base_path = "../jrtr/patch_data/";
	private String parameter_path;
	private String kValues;
	
	public PatchDataPaths(ShaderTaskNr shaderTaskNr, String patchName){
		this.shaderTaskNr = shaderTaskNr;
		this.patchName = patchName+"/";
		assignPaths();	
	}
	
	private void assignPaths(){
		String prefix = null;
		String composite = null;

		if(shaderTaskNr == ShaderTaskNr.TAYLORGAUSSIAN){
			prefix = "experimental/";
		}else if(shaderTaskNr == ShaderTaskNr.DEBUG_ANNOTATION){
			prefix = "experimental/";
		}else if(shaderTaskNr == ShaderTaskNr.DEBUG_SPECULAR){
			prefix = "experimental/";	
		}else if(shaderTaskNr == ShaderTaskNr.STAM){
			prefix = "stam/";
		}
		composite = base_path+"/dft_images/";
		
		this.samplesPath = composite;
		this.extremaPath = composite+"extrema.txt";
		this.globalsPath = base_path+"globals.txt";
		this.weightsPath = base_path+"weights.txt";
		this.parameter_path = base_path + "paramters.txt";
	}
	

	public String getPatchName(){
		return this.patchName;
	}
	
	public ShaderTaskNr getShaderTaskNr(){
		return this.shaderTaskNr;
	}
	
	public String getSamplesPath(){
		return this.samplesPath;
	}
	
	public String getExtremaPath(){
		return this.extremaPath;
	}
	
	public String getGlobalsPath(){
		return this.globalsPath;
	}
	
	public String getWeightsPath(){
		return this.weightsPath;
	}
	
	public String getParamtersPath(){
		return this.parameter_path;
	}
	
	public String getKValuesPath(){
		return this.kValues;
	}
	
}
