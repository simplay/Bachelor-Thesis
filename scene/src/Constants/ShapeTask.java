package Constants;

public enum ShapeTask {
	PLANE(1),
	CYLINDER(2),// fast plane
	SNAKE(3),
	TEAPOT(4),
	DICE(5), 
	CYLINDER2(6),
	CYLINDER3(7),
	PLANE2(8),
	DICE2(9),
	PLANE3(10);
	
	private final int value;
	
	ShapeTask(int value){
		this.value = value;
	}
	
	public int getValue(){
		return this.value;
	}
}
