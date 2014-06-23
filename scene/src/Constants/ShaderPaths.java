package Constants;

public enum ShaderPaths {
	

	// taylor Gauss
	tg_Vert("../jrtr/shaders/vertexshader.vert"),
//	tg_Frag("../jrtr/shaders/approaches/flss.frag");	
//	tg_Frag("../jrtr/shaders/approaches/pq.frag"); // PQ
//	tg_Frag("../jrtr/shaders/approaches/nmm.frag"); // NMM
	tg_Frag("../jrtr/shaders/approaches/gem.frag");
	
	
	 

	
	private final String value;
		 
	private ShaderPaths(String s) { 
		this.value = s; 
	}
		 
	@Override
	public String toString() { 
		return value; 
	}
}
