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
	
	public Watchman getWatchman(){
		return this.watchman;
	}
	
	private Camera getCamera(){
		return this.sceneManager.getCamera();
	}

	@Override
	public void handleEvent() {
		counter++;
		camString = getCamera().getCameraMatrix();
		System.out.println("outcamstring AAAA " +camString);
		System.out.println("handle watchmen event " + counter);
		
	}
	
	private String printedMatrix(Matrix4f mat, String id){
		String out = null;
		
		String line1 = mat.m00 + " " + mat.m01 + " " + mat.m02 + " " + mat.m03;
		String line2 = mat.m10 + " " + mat.m11 + " " + mat.m12 + " " + mat.m13;
		String line3 = mat.m20 + " " + mat.m21 + " " + mat.m22 + " " + mat.m23;
		String line4 = mat.m30 + " " + mat.m31 + " " + mat.m32 + " " + mat.m33;
		
		out = id + "\n";
		out += line1 + "\n";
		out += line2 + "\n";
		out += line3 + "\n";
		out += line4;
		return out;
	}
	
	private Matrix4f camString;
	
	public String getCam(){
		return printedMatrix(camString, "camera Matrix");
	}
}
