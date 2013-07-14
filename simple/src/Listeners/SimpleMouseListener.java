package Listeners;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.vecmath.Point2f;
import javax.vecmath.Vector3f;


public class SimpleMouseListener implements MouseListener{
	
	private Vector3f v1,v2;
	private int width, height;
	private Storage s;
	
	public SimpleMouseListener(Storage s, int height, int width){
		this.s = s;
		this.height = height;
		this.width = width;
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		//v1 = s.getV1();
		//v2 = s.getV2();
		
		Point2f xy = new Point2f(e.getX(),e.getY());
		int minWH = Math.min(width, height);
		
		// scale bounds to [0,0] - [2,2]
		float x = xy.getX() / (minWH / 2);
		float y = xy.getY() / (minWH / 2);
		
		// translate 0,0 to the center
		x = x - 1;
		
		// Flip so +Y is up instead of down
		y = 1 - y;
		
		// Parameterization of sphere surface
		// compare x^2 + y^2 = 1 unit circle
		// see differential geometry
		float z2 = 1 - x * x - y * y;
		float z = 0;
		if (z2 > 0) z = (float) Math.sqrt(z2);
		
		
		v1 = new Vector3f(x, y, z);
		v1.normalize();	
		
		s.setV1(v1);
		//s.setV2(v2);
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
