package MVC;


import java.awt.image.renderable.RenderContext;

import javax.vecmath.Matrix4f;

import jrtr.Camera;

import Diffraction.DiffractionSceneGraphFabricator;
import Listeners.Storage;
import Util.Observer;
import SceneGraph.GraphSceneManager;
import Simulator.SimulationFabricator;
import Util.Subscriber;

public class MainModel extends Observer implements Subscriber{
	private GraphSceneManager sceneManager;
	private Storage s;
	private Watchman watchman;
	private SimulationFabricator simulator;
	private DiffractionSceneGraphFabricator dgsf;
	private int counter = 0;
	
	public void setSimFab(SimulationFabricator fact){
		this.simulator = fact;
	}
	
	public SimulationFabricator getSimFact(){
		return this.simulator;
	}
	
	public void setDiffFab(DiffractionSceneGraphFabricator fact){
		this.dgsf = fact;
	}
	
	public DiffractionSceneGraphFabricator getDiffFact(){
		return this.dgsf;
	}
	
	public MainModel(){
		super();
		this.watchman = new Watchman();
		watchman.subscribe(this);
		this.s = new Storage();
		this.sceneManager = new GraphSceneManager();
	}
	
	public void perfromUpdate(){
		
		this.notifyObservers();
	}
	
	public GraphSceneManager getSceneManager(){
		return this.sceneManager;
	}
	
	public Storage getStorage(){
		return this.s;
	}
	
	public void notfyWatchman(){
		this.watchman.notifyObservers();
	}
	
	public String getWatchmanString(){
		return this.watchman.getCam();
	}

	@Override
	public void handleEvent() {
		watchman.computeData(sceneManager);
	}
}
