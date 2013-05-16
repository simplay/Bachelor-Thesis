package SceneGraph;
import javax.vecmath.Matrix4f;

import jrtr.Shape;

public class TransformGroup extends Group{
	
	private String name;
	
	public TransformGroup(String name){
		super();
		this.name = name;
	}
	
	public TransformGroup(Matrix4f matrix, String name){
		super();
		this.setTransformationMatrix(matrix);
		this.name = name;
	}

	@Override
	public Shape getShape() {
		return null;
	}
	
	public String getName(){
		return this.name;
	}

}
