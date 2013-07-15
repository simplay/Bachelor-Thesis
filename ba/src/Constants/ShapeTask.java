package Constants;

public enum ShapeTask {
	PLANE(1),
	CYLINDER(2),
	SNAKE(3),
	TEAPOT(4),
	DICE(5), 
	CYLINDER2(6);
	
	private final int value;
	
	ShapeTask(int value){
		this.value = value;
	}
	
	public int getValue(){
		return this.value;
	}
}
