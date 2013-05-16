package Constants;

public enum ShaderPaths {
	defaultVert("../jrtr/shaders/default.vert"),
	defaultFrag("../jrtr/shaders/default.frag"),
	diffuseVert("../jrtr/shaders/diffuse.vert"),
	diffuseFrag("../jrtr/shaders/diffuse.frag"),
	task3Vert("../jrtr/shaders/task3.vert"),
	task3Frag("../jrtr/shaders/task3.frag"),
	task3EVert("../jrtr/shaders/task3E.vert"),
	task3EFrag("../jrtr/shaders/task3E.frag"),
	diffractionVert("../jrtr/shaders/diffractionShader.vert"),
	diffractionFrag("../jrtr/shaders/diffractionShader.frag"),
	diffraction2Vert("../jrtr/shaders/diffractionShader2.vert"),
	diffraction2Frag("../jrtr/shaders/diffractionShader2.frag"),
	diffraction3Vert("../jrtr/shaders/diffractionShader3.vert"),
	diffraction3Frag("../jrtr/shaders/diffractionShader3.frag"),
	diffraction4Vert("../jrtr/shaders/diffractionShader4.vert"),
	diffraction4Frag("../jrtr/shaders/diffractionShader4.frag"),
	diffractionHFVert("../jrtr/shaders/HeighfieldDiffraction.vert"),
	diffractionHFFrag("../jrtr/shaders/HeighfieldDiffraction.frag"),
	diffractionHF2Vert("../jrtr/shaders/HeighfieldDiffraction2.vert"),
	diffractionHF2Frag("../jrtr/shaders/HeighfieldDiffraction2.frag"),
	diffractionHF3Vert("../jrtr/shaders/HeighfieldDiffraction3.vert"),
	diffractionHF3Frag("../jrtr/shaders/HeighfieldDiffraction3.frag"),
	diffractionHF4Vert("../jrtr/shaders/HeighfieldDiffraction4.vert"),
	diffractionHF4Frag("../jrtr/shaders/HeighfieldDiffraction4.frag"),
	diffractionHF5Vert("../jrtr/shaders/HeighfieldDiffraction5.vert"),
	diffractionHF5Frag("../jrtr/shaders/HeighfieldDiffraction5.frag"),
	diffractionHF5VecVert("../jrtr/shaders/HeighfieldDiffraction5Vec.vert"),
	diffractionHF5VecFrag("../jrtr/shaders/HeighfieldDiffraction5Vec.frag"),
	diffractionHF6Vert("../jrtr/shaders/HeighfieldDiffraction6.vert"),
	diffractionHF6Frag("../jrtr/shaders/HeighfieldDiffraction6.frag"),
	diffractionHF6VecVert("../jrtr/shaders/HeighfieldDiffraction6Vec.vert"),
	diffractionHF6VecFrag("../jrtr/shaders/HeighfieldDiffraction6Vec.frag"),
	diffractionHF7Vert("../jrtr/shaders/HeighfieldDiffraction7.vert"),
	diffractionHF7Frag("../jrtr/shaders/HeighfieldDiffraction7.frag"),
	diffractionHF7VecVert("../jrtr/shaders/HeighfieldDiffraction7Vec.vert"),
	diffractionHF7VecFrag("../jrtr/shaders/HeighfieldDiffraction7Vec.frag"),
	diffractionHF8VecVert("../jrtr/shaders/HeighfieldDiffraction8Vec.vert"),
	diffractionHF8VecFrag("../jrtr/shaders/HeighfieldDiffraction8Vec.frag"),
	diffractionHFExpVecVert("../jrtr/shaders/HeighfieldDiffractionExpVec.vert"),
	diffractionHFExpVecFrag("../jrtr/shaders/HeighfieldDiffractionExpVec.frag"),
	diffractionHFExp2VecVert("../jrtr/shaders/HeighfieldDiffractionExp2Vec.vert"),
	diffractionHFExp2VecFrag("../jrtr/shaders/HeighfieldDiffractionExp2Vec.frag"),
	
	// grid
	grid1Vert("../jrtr/shaders/grid/HeighfieldDiffractionGridVec.vert"),
	grid1Frag("../jrtr/shaders/grid/HeighfieldDiffractionGridVec.frag"),
	grid2Vert("../jrtr/shaders/grid/HeighfieldDiffractionGridVec2.vert"),
	grid2Frag("../jrtr/shaders/grid/HeighfieldDiffractionGridVec2.frag"),
	grid3Vert("../jrtr/shaders/grid/HeighfieldDiffractionGridVec1kStep.vert"),
	grid3Frag("../jrtr/shaders/grid/HeighfieldDiffractionGridVec1kStep.frag"),
	
	// fft
	fft2dVert("../jrtr/shaders/fft2d/FFT2Diffraction.vert"),
	fft2dFrag("../jrtr/shaders/fft2d/FFT2Diffraction.frag"),
	
	// experimental
	expVert("../jrtr/shaders/fft2d/experimental.vert"),
	expFrag("../jrtr/shaders/fft2d/experimental.frag"),
	
	// taylor
	taylor1Vert("../jrtr/shaders/taylor/HeighfieldDiffractionTaylor1.vert"),
	taylor1Frag("../jrtr/shaders/taylor/HeighfieldDiffractionTaylor1.frag"),
	
	
	bumpMapping2Frag("../jrtr/shaders/bump2.frag");
	
	private final String value;
		 
	private ShaderPaths(String s) { 
		this.value = s; 
	}
		 
	@Override
	public String toString() { 
		return value; 
	}
}
