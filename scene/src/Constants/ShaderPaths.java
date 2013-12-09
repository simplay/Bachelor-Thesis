package Constants;

public enum ShaderPaths {
	
	// a default shader
	defaultVert("../jrtr/shaders/default.vert"),
	defaultFrag("../jrtr/shaders/default.frag"),

	// stam's approach based on nvidia code
	stamVert("../jrtr/shaders/stam/HFD_Stam_approx.vert"),
	stamFrag("../jrtr/shaders/stam/HFD_Stam_approx.frag"),
	
	// taylor Gauss
	tg_Vert("../jrtr/shaders/taylorGaussian/diffractionVPrecomp.vert"),
//	tg_Frag("../jrtr/shaders/taylorGaussian/taylorGaussianAllLambda.frag"),
//	tg_Frag("../jrtr/shaders/taylorGaussian/taylorPQAllLambda.frag"),
	tg_Frag("../jrtr/shaders/taylorGaussian/taylorGaussReqLambda.frag"),
	
	// debug
	specular_Vert("../jrtr/shaders/debug/full_specular.vert"),
	specular_Frag("../jrtr/shaders/debug/full_specular.frag"),
	
	tg_da_Vert("../jrtr/shaders/debug/TG_F_a.vert"),
	tg_da_Frag("../jrtr/shaders/debug/TG_F_a.frag");
	
	private final String value;
		 
	private ShaderPaths(String s) { 
		this.value = s; 
	}
		 
	@Override
	public String toString() { 
		return value; 
	}
}
