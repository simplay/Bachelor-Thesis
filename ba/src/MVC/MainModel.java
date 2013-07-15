package MVC;


import Listeners.Storage;
import Util.Observer;
import SceneGraph.GraphSceneManager;
import Util.Subscriber;

public class MainModel extends Observer{
	private GraphSceneManager sceneManager;
	private Storage s;
	public MainModel(){
		super();
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
}
