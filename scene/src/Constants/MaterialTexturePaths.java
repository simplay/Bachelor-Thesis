package Constants;

public enum MaterialTexturePaths {
	Wood("../jrtr/textures/wood.jpg"),
	Grass("../jrtr/textures/grass.jpg"),
	XXX("../jrtr/textures/xxx.jpg"),
	XGM("../jrtr/textures/xgm.jpg"),
	Rainbow("../jrtr/textures/rainbowgrad.jpg"),
	Bricks("../jrtr/textures/bricks.jpg"),
	BricksN("../jrtr/textures/bricks2N.jpg"),
	Bricks2("../jrtr/textures/bricks2.jpg"),
	Bricks2N("../jrtr/textures/bricksN.jpg"),
	
	front1("../jrtr/textures/cm/negz.jpg"),
	back1("../jrtr/textures/cm/posz.jpg"),
	left1("../jrtr/textures/cm/negx.jpg"),
	right1("../jrtr/textures/cm/posx.jpg"),
	top1("../jrtr/textures/cm/posy.jpg"),
	bottom1("../jrtr/textures/cm/negy.jpg"),
	
	front2("../jrtr/textures/cm2/negz.jpg"),
	back2("../jrtr/textures/cm2/posz.jpg"),
	left2("../jrtr/textures/cm2/negx.jpg"),
	right2("../jrtr/textures/cm2/posx.jpg"),
	top2("../jrtr/textures/cm2/posy.jpg"),
	bottom2("../jrtr/textures/cm2/negy.jpg"),
	
	Ground("../jrtr/textures/ground.jpg"),
	GroundN("../jrtr/textures/groundN.jpg"),
	MCD("../jrtr/textures/mcd.jpg"),
	heightfield("../jrtr/textures/heightfieldSample.jpg"),
	heightfield2("../jrtr/textures/Heightmap.png"),
	bumpH("../jrtr/textures/bumpH.png"),
	street("../jrtr/textures/street.jpg"),
	streetN("../jrtr/textures/streetNormal.jpg"),
	gray("../jrtr/textures/grayBG.bmp"),
	RCD("../jrtr/textures/RCD.png"),
	
	
	red("../jrtr/textures/samples/red.png"),
	green("../jrtr/textures/samples/green.png"),
	yellow("../jrtr/textures/samples/yellow.png"),
	orange("../jrtr/textures/samples/orange.png"),
	white("../jrtr/textures/samples/white.png"),
	pruple("../jrtr/textures/samples/purple.png"),
	blue("../jrtr/textures/samples/blue.png");
	
	
	
	private final String value;
		 
	private MaterialTexturePaths(String s) { 
		this.value = s; 
	}
		 
	@Override
	public String toString() { 
		return value; 
	}
}
