package MVC;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import Listeners.SimpleKeyListener;
import Listeners.SimpleMouseListener;
import Listeners.SimpleMouseMotionListener;

public class MainView {
	private MyRenderPanel renderPanel;
//	private SimpleKeyListener ks;
	private int WindowDim;
	private MainModel model;
	private JTextArea history;
	
	
	public MainView(MainModel model){
		this.model = model;
		renderPanel = new MyRenderPanel(model);
		
		
		this.WindowDim = 800;
		setUpGui();
	}
	
	private void setUpGuiO(){
		JFrame jframe = new JFrame("ba - diffraction shader");
		jframe.setSize(WindowDim, WindowDim);
		jframe.setLocationRelativeTo(null); // center of screen
		jframe.getContentPane().add(renderPanel.getCanvas());// put the canvas into a JFrame window

		

	    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    jframe.setVisible(true); // show window
	}
	
	private void setUpGui2(){
		JFrame jframe = new JFrame("ba - diffraction shader");
		jframe.setSize(WindowDim, WindowDim);
		jframe.setLocationRelativeTo(null); // center of screen
		
		
		JLabel label = new JLabel("Hello World", JLabel.CENTER);
		
		
		Container container = jframe.getContentPane();
		
		Component canvas = renderPanel.getCanvas();
//		canvas.setVisible(true);
		
		container.add(label);
		container.add(canvas);// put the canvas into a JFrame window
		
		
		
	    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    jframe.setVisible(true); // show window
	}
	
	public void setUpGui(){
		JFrame window = new JFrame("ba - diffraction shader");
		BorderLayout experimentLayout = new BorderLayout();
		
		Container container = window.getContentPane();
		container.setLayout(experimentLayout);

		Component canvas = renderPanel.getCanvas();
		history = new JTextArea("Some Swing Component");
		history.setText("camera matrix");
		history.setEditable(false);
		Dimension dim1 = new Dimension(300, 100);
		history.setPreferredSize(dim1);
		
		Dimension dim = new Dimension(WindowDim, WindowDim);
		canvas.setPreferredSize(dim);
		
		container.add(canvas, BorderLayout.PAGE_START);
		container.add(history, BorderLayout.LINE_START);

		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    window.pack();
	    window.setVisible(true);
	}
	
	public MyRenderPanel getRenderPanel(){
		return this.renderPanel;
	}
	
	public int getWindowDim(){
		return this.WindowDim;
	}
	
	public JTextArea getHistory(){
		return this.history;
	}
}
