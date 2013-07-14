package MVC;

import javax.swing.JFrame;
import Listeners.SimpleKeyListener;
import Listeners.SimpleMouseListener;
import Listeners.SimpleMouseMotionListener;

public class MainView {
	private MyRenderPanel renderPanel;
	private SimpleKeyListener ks;
	private int WindowDim;
	private MainModel model;
	
	public MainView(MainModel model){
		this.model = model;
		renderPanel = new MyRenderPanel(model);
		ks = new SimpleKeyListener(model.getStorage(), model.getSceneManager(), renderPanel);
		renderPanel.setKeys(ks);
		this.WindowDim = 800;
		setUpGui();
	}
	
	private void setUpGui(){
		JFrame jframe = new JFrame("ba - diffraction shader");
		jframe.setSize(WindowDim, WindowDim);
		jframe.setLocationRelativeTo(null); // center of screen
		jframe.getContentPane().add(renderPanel.getCanvas());// put the canvas into a JFrame window
		
		renderPanel.getCanvas().addKeyListener(ks);
		renderPanel.getCanvas().addMouseListener(new SimpleMouseListener(model.getStorage(), WindowDim,WindowDim));
		renderPanel.getCanvas().addMouseMotionListener(new SimpleMouseMotionListener(model.getStorage(), model.getSceneManager(), renderPanel, WindowDim, WindowDim)); 

	    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    jframe.setVisible(true); // show window
	}
}
