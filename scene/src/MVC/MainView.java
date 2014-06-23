package MVC;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;

public class MainView {
	private MyRenderPanel renderPanel;
	private int WindowDim;
	@SuppressWarnings("unused")
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
