package Constants;

public enum ShaderPaths {
	
	// a default shader
	defaultVert("../jrtr/shaders/default.vert"),
	defaultFrag("../jrtr/shaders/default.frag"),

	// stam's approach based on nvidia code
	stamVert("../jrtr/shaders/stam/HFD_Stam_approx.vert"),
	stamFrag("../jrtr/shaders/stam/HFD_Stam_approx.frag"),
	
	// grid
	grid_1d_Vert("../jrtr/shaders/grid/HFD_Grid_1d.vert"),
	grid_1d_Frag("../jrtr/shaders/grid/HFD_Grid.frag"),
	
	grid_2d_Vert("../jrtr/shaders/grid/HFD_Grid_2d.vert"),
	grid_2d_Frag("../jrtr/shaders/grid/HFD_Grid.frag"),
	
	grid_T_1dVert("../jrtr/shaders/grid/HFD_Grid_T_1d.vert"),
	grid_T_1dFrag("../jrtr/shaders/grid/HFD_Grid.frag"),
	
	grid_T_2dVert("../jrtr/shaders/grid/HFD_Grid_T_2d.vert"),
	grid_T_2dFrag("../jrtr/shaders/grid/HFD_Grid.frag"),
	
	
	// fft
	fft2dVert("../jrtr/shaders/fft2d/FFT2Diffraction.vert"),
	fft2dFrag("../jrtr/shaders/fft2d/FFT2Diffraction.frag"),
	
	// experimental
	expVert("../jrtr/shaders/fft2d/experimental.vert"),
	expFrag("../jrtr/shaders/fft2d/experimental.frag"),
	
	// taylor
	test_Vert("../jrtr/shaders/taylor/testFrag.vert"),
	test_Frag("../jrtr/shaders/taylor/testFrag.frag"),
	
	taylor1Vert("../jrtr/shaders/taylor/HeighfieldDiffractionTaylor1.vert"),
	taylor1Frag("../jrtr/shaders/taylor/HeighfieldDiffractionTaylor1.frag"),
	
	taylor_1d_Vert("../jrtr/shaders/taylor/HFD_Taylor_1d.vert"),
	taylor_1d_Frag("../jrtr/shaders/taylor/HFD_Taylor.frag"),
	
	taylor_2d_Vert("../jrtr/shaders/taylor/HFD_Taylor_2d.vert"),
	taylor_2d_Frag("../jrtr/shaders/taylor/HFD_Taylor.frag"),
	
	taylor_T_1d_Vert("../jrtr/shaders/taylor/HFD_Taylor_T_1d.vert"),
	taylor_T_1d_Frag("../jrtr/shaders/taylor/HFD_Taylor.frag"),
	
	taylor_T_2d_Vert("../jrtr/shaders/taylor/HFD_Taylor_T_2d.vert"),
	taylor_T_2d_Frag("../jrtr/shaders/taylor/HFD_Taylor.frag"),
	
	expTaylor_2d_Vert("../jrtr/shaders/taylor/HFD_Taylor_T_2d_experimental.vert"),
	expTaylor_2d_Frag("../jrtr/shaders/taylor/HFD_Taylor.frag"),
	
	//taylor Gauss
	tg_Vert("../jrtr/shaders/taylorGaussian/testFrag.vert"),
//	tg_Frag("../jrtr/shaders/taylorGaussian/testFrag.frag"),
//	tg_Frag("../jrtr/shaders/taylorGaussian/evaluate.frag"),
//	tg_Frag("../jrtr/shaders/taylorGaussian/sampleLambda.frag"),
	
//	tg_Frag("../jrtr/shaders/taylorGaussian/testGauss.frag"),
	tg_Frag("../jrtr/shaders/taylorGaussian/sampleLambdaGauss.frag"),
//	tg_Frag("../jrtr/shaders/taylorGaussian/GT.frag"),
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
