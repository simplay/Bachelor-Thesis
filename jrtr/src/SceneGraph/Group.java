/**
 * transformation group
 * 
 * @author Michael Single
 */
package SceneGraph;

import java.util.ArrayList;

import javax.vecmath.Matrix4f;

public abstract class Group implements INode{
	protected ArrayList<INode> children;
	protected Matrix4f tranformationMatrix;
	
	/**
	 * a new Group has a list of children and a corresponding 
	 * transformation matrix which belongs to itself and each of its children.
	 */
	public Group(){
		this.children = new ArrayList<INode>();
		this.tranformationMatrix = new Matrix4f();
		this.tranformationMatrix.setIdentity();
	}
	
	/**
	 * put a new child into our children list
	 * @param child new child
	 */
	public void putChild(INode child){
		this.children.add(child);
	}
	
	/**
	 * remove a given child from our children list
	 * @param child
	 */
	public void removeChild(INode child){
		this.children.remove(child);
	}
	
	/**
	 * return children list of this group
	 * if there is no children list, return null
	 */
	@Override
	public ArrayList<INode> getChildren(){
		return this.children;
		
	}
	
	/**
	 * get child in children list by given index
	 * @param index index of child
	 * @return returns child_index in children list
	 */
	public INode getChild(int index){
		return this.children.get(index);
	}
	
	/**
	 * get the transformation matrix of this group.
	 */
	@Override
	public Matrix4f getTransformationMatrix(){
		return this.tranformationMatrix;	
	}
	
	/**
	 * set transformation matrix for this group
	 * @param transformationMatrix the transformation matrix for this group
	 */
	public void setTransformationMatrix(Matrix4f transformationMatrix){
		this.tranformationMatrix = transformationMatrix;
	}
}
