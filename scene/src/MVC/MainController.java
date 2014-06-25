package MVC;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.media.opengl.GL3;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jrtr.GLRenderContext;
import jrtr.RenderPanel;

import com.jogamp.opengl.util.awt.Screenshot;
import com.sun.jmx.snmp.Timestamp;
import Constants.ShaderTaskNr;
import Listeners.SimpleKeyListener;
import Listeners.SimpleMouseListener;
import Listeners.SimpleMouseMotionListener;
import Util.Subscriber;

public class MainController implements Subscriber, ChangeListener{
	
	private MainModel model;
	private MainView view;
	public MainController(MainModel model, MainView view){
		this.model = model;
		this.view = view;
		
		MyRenderPanel rp = view.getRenderPanel();
		SimpleKeyListener ks = new SimpleKeyListener(model.getStorage(), model.getSceneManager(), rp, this);
		rp.setKeys(ks);
		int dim = view.getWindowDim();
		
		rp.getCanvas().addKeyListener(ks);
		rp.getCanvas().addMouseListener(new SimpleMouseListener(model.getStorage(), dim, dim, this));
		rp.getCanvas().addMouseMotionListener(new SimpleMouseMotionListener(model.getStorage(), model.getSceneManager(), rp, dim, dim, this)); 
		
		view.getSnapshotButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveSnapshot();
				System.out.println("snapshot taken...");
			}
		});
		
//		.addComponentListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				saveSnapshot();
//				System.out.println("snapshot taken...");
//			}
//		});
//		
		view.getSlider().addChangeListener((ChangeListener) this);

	}
	
	@Override
	public void handleEvent() {
		model.notfyWatchman();
		view.getHistory().setText(model.getWatchmanString());	
	}
	
	private void saveSnapshot(){
		GL3 gl = ((GLRenderContext)view.getRenderPanel().getRenderContext()).getGL3();
		gl.getContext().makeCurrent();
		Timestamp tstamp = new Timestamp(System.currentTimeMillis()); 
		BufferedImage tScreenshot = Screenshot.readToBufferedImage(0,0, 800, 800, false);
		
		
		String img_title = "../screenshots/sn_"+ tstamp.getDateTime();
		if(model.getDiffFact().getShaderTask() == ShaderTaskNr.DEBUG_ANNOTATION){
			img_title = "../screenshots/debug";
		}
		
		File tScreenCaptureImageFile = new File(img_title + ".png");
		try {
			ImageIO.write(tScreenshot, "png", tScreenCaptureImageFile);
		} catch (IOException e) {} 
		gl.getContext().release();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if (!source.getValueIsAdjusting()) {
            float lum = (int)source.getValue();
            model.getDiffFact().getMat().setBrightness(lum);
            view.getRenderPanel().getCanvas().repaint();
        }
	}
}
