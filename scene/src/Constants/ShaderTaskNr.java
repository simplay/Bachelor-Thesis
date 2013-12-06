package Constants;

public enum ShaderTaskNr {
	STAM(4),
	TAYLORGAUSSIAN(13),
	DEBUG_ANNOTATION(14),
	DEBUG_SPECULAR(15),
	ELSE(200);
	
	private final int value;
	
	ShaderTaskNr(int value){
		this.value = value;
	}
	
	public int getValue(){
		return this.value;
	}
}
