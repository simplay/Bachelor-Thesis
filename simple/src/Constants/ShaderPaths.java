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
	
	grid_T_2dVert("../jrtr/shaders/grid/HFD_Grid_T_2d.vert"),
	grid_T_2dFrag("../jrtr/shaders/grid/HFD_Grid.frag"),
	
	
	// fft
	fft2dVert("../jrtr/shaders/fft2d/FFT2Diffraction.vert"),
	fft2dFrag("../jrtr/shaders/fft2d/FFT2Diffraction.frag"),
	
	// experimental
	expVert("../jrtr/shaders/fft2d/experimental.vert"),
	expFrag("../jrtr/shaders/fft2d/experimental.frag"),
	
	// taylor
	taylor1Vert("../jrtr/shaders/taylor/HeighfieldDiffractionTaylor1.vert"),
	taylor1Frag("../jrtr/shaders/taylor/HeighfieldDiffractionTaylor1.frag"),
	
	taylor_1d_Vert("../jrtr/shaders/taylor/HFD_Taylor_1d.vert"),
	taylor_1d_Frag("../jrtr/shaders/taylor/HFD_Taylor.frag"),
	
	taylor_2d_Vert("../jrtr/shaders/taylor/HFD_Taylor_2d.vert"),
	taylor_2d_Frag("../jrtr/shaders/taylor/HFD_Taylor.frag");
	
	private final String value;
		 
	private ShaderPaths(String s) { 
		this.value = s; 
	}
		 
	@Override
	public String toString() { 
		return value; 
	}
}
