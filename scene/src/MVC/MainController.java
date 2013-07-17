package MVC;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.swing.JOptionPane;

import jrtr.GLRenderContext;
import jrtr.RenderContext;

import com.jogamp.opengl.util.awt.Screenshot;
import com.sun.corba.se.impl.ior.ByteBuffer;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.sun.jmx.snmp.Timestamp;

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
		File tScreenCaptureImageFile = new File("../screenshots/sn_"+ tstamp.getDateTime()+ ".png");
		try {
			ImageIO.write(tScreenshot, "png", tScreenCaptureImageFile);
		} catch (IOException e) {} 
		gl.getContext().release();
	}
	

}
