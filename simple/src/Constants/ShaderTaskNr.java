package Constants;

public enum ShaderTaskNr {
	Diffuse1(1),
	DiffuseMPLS(2),
	DiffuseMPLSTex(3),
	Task1(4),
	Task2(5),
	Task3(6),
	Task3E(7),
	RainbowBrick(8),
	simpleToon(9),
	diffraction(10),
	bump(11),
	ELSE(200);
	
	private final int value;
	
	ShaderTaskNr(int value){
		this.value = value;
	}
	
	public int getValue(){
		return this.value;
	}
}
