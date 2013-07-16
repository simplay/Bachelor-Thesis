package MVC;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

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
	}


	@Override
	public void handleEvent() {
		model.notfyWatchman();
		view.getHistory().setText(model.getWatchmanString());
		
		String url = "foo";
//		try
//		{
			Component can = view.getCanvas();
//			BufferedImage image = new BufferedImage(can.getWidth(), can.getHeight(), BufferedImage.TYPE_INT_RGB);
//			BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(url)) ;
//			
//			can.getGraphics().getClip().
//			
//			
//			Graphics2D graphics = image.createGraphics();
//			can.paintAll(graphics);
//			ImageIO.write(image, "jpg", new File(url + ".jpg"));
// 
//			fos.close();
//			
			
        float quality = 0.9f;
		int w = can.getWidth();    
		int h = can.getHeight();    
		BufferedImage image = new BufferedImage(w,h, BufferedImage.TYPE_INT_ARGB);

		BufferedImage image2 = new BufferedImage(w,h, BufferedImage.TYPE_INT_ARGB);
		can.paint(image2.createGraphics());
		
		for (int y = 0; y < image.getHeight(); y++) {
		    for (int x = 0; x < image.getWidth(); x++) {
		          int  clr   = image2.getRGB(x, y); 
		          int  red   = (clr & 0x00ff0000) >> 16;
		          int  green = (clr & 0x0000ff00) >> 8;
		          int  blue  =  clr & 0x000000ff;
		          
		          int out_clr = red + green + blue;
		          if(out_clr != 0) System.out.println(out_clr);
		          image.setRGB(x, y, green);
		    }
		}
		
		


	    File outputfile = new File("saved.png");
	    try {
			ImageIO.write(image, "png", outputfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

		
				
		
	}
	
	private BufferedImage canvasToImage(Canvas cnvs) {
        int w = cnvs.getWidth();
        int h = cnvs.getHeight();
        int type = BufferedImage.TYPE_INT_RGB;
        BufferedImage image = new BufferedImage(w,h,type);
        Graphics2D g2 = image.createGraphics();
        cnvs.paint(g2);
        g2.dispose();
        return image;
    }
}
