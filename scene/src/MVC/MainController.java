package MVC;
import Listeners.SimpleKeyListener;
import Listeners.SimpleMouseListener;
import Listeners.SimpleMouseMotionListener;
import Util.Subscriber;

public class MainController implements Subscriber{
	
	private MainModel model;
	private MainView view;
	
	
	public MainController(MainModel model, MainView view){
		this.model = model;
		this.view = view;
		
		MyRenderPanel rp = view.getRenderPanel();
		SimpleKeyListener ks = new SimpleKeyListener(model.getStorage(), model.getSceneManager(), rp);
		rp.setKeys(ks);
		int dim = view.getWindowDim();
		
		rp.getCanvas().addKeyListener(ks);
		rp.getCanvas().addMouseListener(new SimpleMouseListener(model.getStorage(), dim, dim));
		rp.getCanvas().addMouseMotionListener(new SimpleMouseMotionListener(model.getStorage(), model.getSceneManager(), rp, dim, dim)); 
		
		//setup listeners
		
	}


	@Override
	public void handleEvent() {
		// TODO Auto-generated method stub
		
	}
}
