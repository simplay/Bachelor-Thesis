package SceneGraph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import jrtr.Light;
import jrtr.RenderItem;

public class LightSourceIterator implements Iterator<Light>{
	private Stack<INode> nodeStack;
    private Stack<Matrix4f> transforamtionMatricesStack;
    private Light lightSource_L;
    
	public LightSourceIterator(INode root){
    	nodeStack = new Stack<INode>();
    	nodeStack.push(root);
    	this.lightSource_L = new Light(null, null,null);
    	transforamtionMatricesStack = new Stack<Matrix4f>();
    	Matrix4f IdTransformation = new Matrix4f();
    	IdTransformation.setIdentity();
    	transforamtionMatricesStack.push(IdTransformation);
	}
	
	@Override
	public boolean hasNext() {
		return !this.nodeStack.isEmpty();
	}

	@Override
	public Light next() {
		Light answer = null; 
		
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
		
		if(nodePeek != null){

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
				//renderItem = new RenderItem(currentNode.getShape(), tempMatrix);
				// currently nothing to do here
			}
			
			else{
				LightNode currentLightNode = (LightNode) currentNode;
			    Light lightSource =  currentLightNode.getLightSource();
			    Vector4f currentDir = (Vector4f) lightSource.getLightDirection().clone();
			    Matrix4f camera = (Matrix4f) currentLightNode.getCamera();
			    camera.invert();
			    
			    
			    currentTransformation.transform(currentDir);
			    camera.transform(currentDir);
			    
			    this.lightSource_L = new Light(lightSource.getRadiance(), currentDir, lightSource.getName());
			    answer = this.lightSource_L;
			}
		}
		
			
		return answer;
	}

	@Override
	public void remove() {
		System.err.println("has not implemented yet");
	}

}
