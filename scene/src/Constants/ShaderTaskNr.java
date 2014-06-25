package Constants;

public enum ShaderTaskNr {
	STAM(4),
	TAYLORGAUSSIAN(13),
	DEBUG_ANNOTATION(14),
	DEBUG_SPECULAR(15),
	FLSS(16),
	NMM(17),
	PQ(18),
	GEM(19),
	ELSE(200);
	
	private final int value;
	
	ShaderTaskNr(int value){
		this.value = value;
	}
	
	public int getValue(){
		return this.value;
	}
}
