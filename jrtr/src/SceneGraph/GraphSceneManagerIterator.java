package SceneGraph;

import java.util.ArrayList;
import java.util.Stack;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;

import jrtr.Light;
import jrtr.RenderItem;
import jrtr.SceneManagerIterator;

public class GraphSceneManagerIterator implements SceneManagerIterator{
	private Stack<INode> nodeStack;
    private Stack<Matrix4f> transforamtionMatricesStack;
    private int dropCounter = 0;
	
    /**
     * Constructor of a graph manager iterator 
     * need a root which it can start its iteration.
     * @param root iteration starting node 
     */
    public GraphSceneManagerIterator(INode root){
    	nodeStack = new Stack<INode>();
    	nodeStack.push(root);
    	transforamtionMatricesStack = new Stack<Matrix4f>();
    	Matrix4f IdTransformation = new Matrix4f();
    	IdTransformation.setIdentity();
    	transforamtionMatricesStack.push(IdTransformation);
    	
    }
    
    /**
     * does this iterator have a next item?
     * true if our node stack still contains elements.
     */
    
	@Override
	public boolean hasNext() {
		return !this.nodeStack.isEmpty();
	}
	
	// TODO implement this overridden method
	
	@Override
	public RenderItem next() {
		RenderItem renderItem = null; 
		
		INode nodePeek = nodeStack.peek();
		
		INode currentNode = nodeStack.pop();
		Matrix4f currentTransformation = transforamtionMatricesStack.pop();
		
		Matrix4f tempMatrix = new Matrix4f();
		tempMatrix.setIdentity();
		
		//TODO do this whole functionality in a polimorph fashion
		// have for each INode implementing class a method which handels
		// how to put an inode item onto the stack
		// such a method requires the stacks as an input
		// and retruns a render item
		
		if(nodePeek instanceof TransformGroup){
			tempMatrix.mul(currentTransformation, currentNode.getTransformationMatrix());
			ArrayList<INode> children = currentNode.getChildren();
			for(INode child : children){
				transforamtionMatricesStack.push(tempMatrix);
				nodeStack.push(child);
			}
		}
		
		else if (nodePeek instanceof ShapeNode){
			tempMatrix.mul(currentTransformation, currentNode.getTransformationMatrix());
			
			ShapeNode leaf = (ShapeNode) currentNode;
			
			//TODO do some code cleanup
			if(leaf.performsCulling()){
			    if (leaf.getCenter() != null) {
				    Vector4f center = (Vector4f) leaf.getCenter().clone();
				    center.setW(1);
				    currentTransformation.transform(center);
				    Matrix4f camera = (Matrix4f) leaf.getCameraMatrix().clone();
				    camera.invert();
				    camera.transform(center);
				     // Return null if current object is outside the frustum
				   // System.out.println(leaf.getName());
				    if (!leaf.intersectsFrustumAfterCulling(center)) {
				    	System.out.println("culled shape");
				    	this.dropCounter++;
				    	return null;
				    }
			    }
			}
			
			
			renderItem = new RenderItem(currentNode.getShape(), tempMatrix);
		}
		
		else{
			// nothing to do here
		}
			
		return renderItem;
	}
	
	public int getDrops(){
		return this.dropCounter;
	}
}
