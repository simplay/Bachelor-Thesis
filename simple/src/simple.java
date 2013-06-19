import jrtr.*;

import javax.swing.*;
import javax.vecmath.*;
import Constants.ShaderTaskNr;
import Diffraction.DiffractionAnimationTask;
import Diffraction.DiffractionSceneGraphFabricator;
import Listeners.SimpleKeyListener;
import Listeners.SimpleMouseListener;
import Listeners.SimpleMouseMotionListener;
import Listeners.Storage;
import SceneGraph.GraphSceneManager;
import Simulator.SimulationFabricator;

import java.io.IOException;
import java.util.Timer;


public class simple{	
	static boolean simulate = false;
	static int shaderFlag = ShaderTaskNr.simpleToon.getValue();
	static RenderPanel renderPanel;
	static RenderPanel renderPanel2;
	static RenderContext renderContext;
	static GraphSceneManager sceneManager;
	static Shape shape;
	static Shape shape2;
	static float angle;
	static Matrix4f originalShape2Matrix;
	static SimpleKeyListener ks;
	static Vector3f v1 = new Vector3f(0,0,0), v2 = new Vector3f(0,0,0);
	
	/**
	 * An extension of {@link GLRenderPanel} or {@link SWRenderPanel} to 
	 * provide a call-back function for initialization. 
	 */ 
	public final static class SimpleRenderPanel extends GLRenderPanel{
		
		/**
		 * Initialization call-back. We initialize our renderer here.
		 * 
		 * @param r	the render context that is associated with this render panel
		 */
		@Override
		public void init(RenderContext r){
			renderContext = r;
			renderContext.setSceneManager(sceneManager);

		    Timer timer = new Timer();
		    angle = 0.01f;
		    
		    
//		    double a = Math.cos(2.0d*Math.PI);
//		    double b = Math.sin(2.0d*Math.PI);
//		    System.out.println("cos 2pi " +a);
//		    System.out.println("sin 2pi " +b);
		    
		    try {
//				ReadObjects.ObjReader reader = new ReadObjects.ObjReader("../models/teapot.obj");
				ReadObjects.ObjReader reader = new ReadObjects.ObjReader("../models/snake_test_piece.obj");
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
		    
		    if(simulate){
			    SimulationFabricator simulator = new SimulationFabricator(sceneManager, r);
			    sceneManager.setRoot(simulator.getRoot());
		    }else{
			    DiffractionSceneGraphFabricator dgsf = new DiffractionSceneGraphFabricator(sceneManager, r);
			    ks.setFabric(dgsf);
				sceneManager.setRoot(dgsf.getRoot());
			    timer.scheduleAtFixedRate(new DiffractionAnimationTask(dgsf, renderPanel), 0, 10);
		    }

		    
		    // diffraction fabricator

		}
	}

	
	/**
	 * The main function opens a 3D rendering window, constructs a simple 3D
	 * scene, and starts a timer task to generate an animation.
	 */
	public static void main(String[] args){	

		// Make a render panel. The init function of the renderPanel
		// (see above) will be called back for initialization.
		renderPanel = new SimpleRenderPanel();
		sceneManager = new GraphSceneManager();
		int dim = 800;
		
		//sceneManager1 = new SimpleSceneManager();

		// Make the main window of this application and add the renderer to it
		JFrame jframe = new JFrame("ba - diffraction shader");
		jframe.setSize(dim, dim);
		jframe.setLocationRelativeTo(null); // center of screen
		jframe.getContentPane().add(renderPanel.getCanvas());// put the canvas into a JFrame window


		//add(playFrame, BorderLayout.CENTER);
		// Add a mouse listener
		Storage s = new Storage();
		ks = new SimpleKeyListener(s, sceneManager, renderPanel);
		renderPanel.getCanvas().addKeyListener(ks);
		renderPanel.getCanvas().addMouseListener(new SimpleMouseListener(s,dim,dim));
		renderPanel.getCanvas().addMouseMotionListener(new SimpleMouseMotionListener(s,sceneManager, renderPanel, dim, dim)); 

		
	    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    jframe.setVisible(true); // show window
	}
}
