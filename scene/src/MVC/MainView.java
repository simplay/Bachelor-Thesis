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
	private Component canvas;
	private JButton snapshotButton;
	
	public MainView(MainModel model){
		this.model = model;
		renderPanel = new MyRenderPanel(model);
		this.WindowDim = 800;
		setUpGui();
	}
	
	public void setUpGui(){
		JFrame window = new JFrame("ba - diffraction shader");
		BorderLayout experimentLayout = new BorderLayout();
		
		Container container = window.getContentPane();
		container.setLayout(experimentLayout);

		canvas = renderPanel.getCanvas();
		history = new JTextArea("Some Swing Component");
		history.setText("camera matrix");
		history.setEditable(false);
		Dimension dim1 = new Dimension(300, 100);
		history.setPreferredSize(dim1);
		Dimension dim = new Dimension(WindowDim, WindowDim);
		canvas.setPreferredSize(dim);
		snapshotButton = new JButton("snapshot");
		
		
		container.add(canvas, BorderLayout.PAGE_START);
		container.add(history, BorderLayout.LINE_START);
		container.add(snapshotButton, BorderLayout.AFTER_LAST_LINE);
		
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
	
	public Component getCanvas(){
		return renderPanel.getCanvas();
	}
	
	public JButton getSnapshotButton(){
		return this.snapshotButton;
	}
}
