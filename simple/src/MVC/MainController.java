package MVC;
import Util.Subscriber;

public class MainController implements Subscriber{
	
	private MainModel model;
	private MainView view;
	
	
	public MainController(MainModel model, MainView view){
		this.model = model;
		this.view = view;
		
		//setup listeners
		
	}


	@Override
	public void handleEvent() {
		// TODO Auto-generated method stub
		
	}
}
