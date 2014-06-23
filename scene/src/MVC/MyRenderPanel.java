package MVC;

import java.util.Timer;

import Diffraction.DiffractionAnimationTask;
import Diffraction.DiffractionSceneGraphFabricator;
import Listeners.SimpleKeyListener;
import SceneGraph.GraphSceneManager;
import jrtr.GLRenderPanel;
import jrtr.RenderContext;
import jrtr.RenderPanel;

public class MyRenderPanel extends GLRenderPanel{
	private MainModel model;
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
	
	
	public RenderContext getRenderContext(){
		return this.renderContext;
	}
	
	@Override
	public void init(RenderContext r) {
		renderContext = r;
		renderContext.setSceneManager(sceneManager);
	    Timer timer = new Timer();

		DiffractionSceneGraphFabricator dgsf = new DiffractionSceneGraphFabricator(sceneManager, r);
		model.setDiffFab(dgsf);
		ks.setFabric(dgsf);
		sceneManager.setRoot(dgsf.getRoot());
		timer.scheduleAtFixedRate(new DiffractionAnimationTask(dgsf, renderPanel), 0, 10);
	}

}
