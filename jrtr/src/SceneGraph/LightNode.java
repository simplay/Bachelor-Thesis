package SceneGraph;

import java.util.ArrayList;

import javax.vecmath.Matrix4f;

import jrtr.Light;
import jrtr.Shape;

public class LightNode extends Leaf{
	private Light lightSource;
	private String name;
	private Matrix4f camera;
	
	public LightNode(Light lightSource, Matrix4f camera, String name){
		super(null);
		this.lightSource = lightSource;
		this.camera = camera;
		this.name = name;
	}
	
	public Light getLightSource(){
		return this.lightSource;
	}
	
	public String getName(){
		return this.name;
	}
	
	public Matrix4f getCamera(){
		return this.camera;
	}
	
	@Override
	public Matrix4f getTransformationMatrix() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Shape getShape() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<INode> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

}
