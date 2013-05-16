package SceneGraph;

import java.util.ArrayList;
import java.util.Iterator;


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

	@Override
	public ArrayList<Light> getLightSources() {
		return this.lightSources;
	}
	
    public void addLight(Light lightSource) {
    	int index = -1;
    	for(Light light : this.lightSources){
    		if(light.getName().equals(lightSource.getName()) )
    			index = lightSources.indexOf(light);
    	}
    	
    	if(index == -1) lightSources.add(lightSource);
    	else lightSources.set(index, lightSource);
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
