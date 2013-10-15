package SceneGraph;

import java.util.ArrayList;
import java.util.Iterator;

import javax.vecmath.Vector4f;


import jrtr.Camera;
import jrtr.Frustum;
import jrtr.Light;
import jrtr.SceneManagerInterface;
import jrtr.SceneManagerIterator;


public class GraphSceneManager implements SceneManagerInterface{
	private INode root;
    private Camera camera;
    private Frustum frustum;
	private ArrayList<Light> lightSources;
	
	//TODO: MOVE US SOMEWHERE ELSE
	private int thetaI = 0;
	private int phiI   = 180;
	
	public void incThetaI(){
		this.thetaI = (this.thetaI+1)%360;
	}
	
	public void incPhiI(){
		this.phiI = (this.phiI+1)%360;
	}
	
	public void decThetaI(){
		this.thetaI = (this.thetaI-1)%360;
	}
	
	public void decPhiI(){
		this.phiI = (this.phiI-1)%360;
	}
	
	public int getThetaI(){
		return this.thetaI;
	}
	
	public int getPhiI(){
		return this.phiI;
	}
	
	public GraphSceneManager(){
		this.lightSources = new ArrayList<Light>();
		this.camera = new Camera();
		this.frustum = new Frustum();
	}
	
	public INode getRoot(){
		return this.root;
	}
	
	public void setRoot(INode root){
		this.root = root;
	}

	@Override
	public SceneManagerIterator iterator() {
		return new GraphSceneManagerIterator(this.root);
	}

	@Override
	public Camera getCamera() {
		return this.camera;
	}

	@Override
	public Frustum getFrustum() {
		return this.frustum;
	}
	
	public Iterator<Light> lightIterator() {
		return new LightSourceIterator(this.root);
	}

//	@Override
//	public ArrayList<Light> old_getLightSources() {
//		return this.lightSources;
//	}
	
	public ArrayList<Light> getLightSources() {
		ArrayList<Light> lightSources2 = new ArrayList<Light>();
		
		ArrayList<INode> nodes = this.getRoot().getChildren();
		for(INode node : nodes){
			if(node instanceof LightNode){
				Light tagetLightSource = ((LightNode) node).getLightSource();
				lightSources2.add(tagetLightSource);
			}
		}
		return lightSources2;
	}
	
//    public void addLight(Light lightSource) {
//    	int index = -1;
//    	for(Light light : this.lightSources){
//    		if(light.getName().equals(lightSource.getName()) )
//    			index = lightSources.indexOf(light);
//    	}
//    	
//    	if(index == -1) lightSources.add(lightSource);
//    	else lightSources.set(index, lightSource);
//    }
    
    
    public void addLight(Light lightSource) {
		ArrayList<INode> nodes = this.getRoot().getChildren();
		for(INode node : nodes){
			if(node instanceof LightNode){
				Light tagetLightSource = ((LightNode) node).getLightSource();
				Vector4f newLightDir = lightSource.getLightDirection();
				tagetLightSource.setLightDirection(newLightDir);
				break;
			}
		}
    }
    
    
    
    public void flushLightSources(){
    	lightSources = new ArrayList<Light>();
    }

	@Override
	public LightNode getRootLight() {	
		ArrayList<INode> nodes = this.root.getChildren();
		LightNode answer = null;
		for(INode node : nodes){
			if(node instanceof LightNode){
				answer = (LightNode) node;
				break;
			}
		}
		return answer;
	}
	
}
