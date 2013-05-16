package SceneGraph;

import java.util.ArrayList;

import javax.vecmath.Matrix4f;

import jrtr.Shape;

public abstract class Leaf implements INode{
	protected Shape shape;
	
	/**
	 * assign a shape for every leaf
	 * @param shape
	 */
	public Leaf(Shape shape){
		this.shape = shape;
	}
	
	/**
	 * get corresponding shape back.
	 */
	@Override
	public Shape getShape() {
		return this.shape;
	}
	
	/**
	 * get the transformation matrix of this leaf's shape.
	 */
	@Override
	public Matrix4f getTransformationMatrix() {
		return shape.getTransformation();
	}
	
	/**
	 * a leaf never has any children and therefore return null.
	 */
	@Override
	public ArrayList<INode> getChildren() {
		return null;
	}
	
	
}
