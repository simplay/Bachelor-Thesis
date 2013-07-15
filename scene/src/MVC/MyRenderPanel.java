package MVC;

import java.util.Timer;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import Diffraction.DiffractionAnimationTask;
import Diffraction.DiffractionSceneGraphFabricator;
import Listeners.SimpleKeyListener;
import SceneGraph.GraphSceneManager;
import Simulator.SimulationFabricator;
import jrtr.GLRenderPanel;
import jrtr.RenderContext;
import jrtr.RenderPanel;
import jrtr.Shape;

public class MyRenderPanel extends GLRenderPanel{
	private MainModel model;
	private boolean simulate = false;
	private RenderPanel renderPanel;
	private RenderContext renderContext;
	private GraphSceneManager sceneManager;
	private SimpleKeyListener ks;

	public MyRenderPanel(MainModel model){
		this.model = model;
		this.sceneManager = model.getSceneManager();
	}
	
	public void setKeys(SimpleKeyListener ks){
		this.ks = ks;
	}
	
	
	@Override
	public void init(RenderContext r) {
		renderContext = r;
		renderContext.setSceneManager(sceneManager);
	    Timer timer = new Timer();

	    if(simulate){
		    SimulationFabricator simulator = new SimulationFabricator(sceneManager, r);
		    sceneManager.setRoot(simulator.getRoot());
	    }else{
		    DiffractionSceneGraphFabricator dgsf = new DiffractionSceneGraphFabricator(sceneManager, r);
		    ks.setFabric(dgsf);
			sceneManager.setRoot(dgsf.getRoot());
		    timer.scheduleAtFixedRate(new DiffractionAnimationTask(dgsf, renderPanel), 0, 10);
	    }
		
	}

}
