package Constants;

public enum ShaderPaths {

	tg_Vert("../jrtr/shaders/vertexshader.vert"),
	tg_flss("../jrtr/shaders/approaches/flss.frag"),
	tg_pq("../jrtr/shaders/approaches/pq.frag"),
	tg_nmm("../jrtr/shaders/approaches/nmm.frag"),
	tg_gem("../jrtr/shaders/approaches/gem.frag");

	private final String value;
		 
	private ShaderPaths(String s) { 
		this.value = s; 
	}
		 
	@Override
	public String toString() { 
		return value; 
	}
}
