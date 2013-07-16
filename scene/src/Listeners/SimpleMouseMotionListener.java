package Listeners;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point2f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import jrtr.RenderPanel;
import SceneGraph.GraphSceneManager;



public class SimpleMouseMotionListener implements MouseMotionListener{
	private Storage s;
	private int width, height;
	private Vector3f v1,v2;
	private float theta;
	private GraphSceneManager sceneManager;
	private RenderPanel renderPanel;
	
	public SimpleMouseMotionListener(Storage s, GraphSceneManager sceneManager, RenderPanel renderPanel, int width, int height){
		this.s = s;
		this.sceneManager = sceneManager;
		this.renderPanel = renderPanel;
		this.width = width;
		this.height = height;
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		v1 = s.getV1();
		v2 = s.getV2();
		
		Point2f xy = new Point2f(e.getX(),e.getY());
		int minWH = Math.min(width, height);
		
		// scale bounds to [0,0] - [2,2]
		float x = xy.getX() / (minWH / 2);
		float y = xy.getY() / (minWH / 2);
		
		// translate 0,0 to the center
		x = x - 1.0f;
		
		// Flip so +Y is up instead of down
		y = 1.0f - y;
		
		float z2 = 1.0f - x*x - y*y;
		float z = 0.0f;
		if (z2 > 0) z = (float) Math.sqrt(z2);
		
		v2 = new Vector3f(x, y, z);
		v2.normalize();
		
		// calculate axis and angle
	    Vector3f axis = new Vector3f();
	    axis.cross(v1, v2);    		
	    
	    // Handles when mouse is outside trackball (x,y)
//	    theta = v1.angle(v2);
//	    theta = theta * -1;
	    
	    Vector3f nV1 = v1;
	    nV1.normalize();
	    
	    Vector3f nV2 = v2;
	    nV2.normalize();
	    
	    theta = (float) Math.acos(nV1.dot(nV2));
	    
	    
	    
	    // we have negated the angle theta because we are rotating the camera
	    AxisAngle4f axisaAngle = new AxisAngle4f();
	    axisaAngle.set(axis, -theta);
	    Quat4f delta = new Quat4f();
	    delta.set(axisaAngle);
	    
//	    // Get current orientation
	    Matrix4f camera = sceneManager.getCamera().getCameraMatrix();
//	    camera.invert();
	    Quat4f current = new Quat4f();
	    camera.get(current);
	    
	    // prepare rotation matrix
	    delta.negate();
	    current.conjugate(delta);
	    Matrix4f deltaMatrix = new Matrix4f();
	    deltaMatrix.set(current);

	    // do the rotation step and update sceneManager
	    camera.mul(deltaMatrix);
	    
	    
	    //sceneManager.getCamera().getCameraMatrix().mul(deltaMatrix);
	    sceneManager.getCamera().setCameraMatrix(camera);
	    
	    Vector4f pos = new Vector4f();
	    sceneManager.getCamera().getCameraMatrix().getColumn(3, pos);
	    
	    System.out.println();
	    System.out.println("Trackball inverse C_c_w: " + sceneManager.getCamera().getInvC());
	    System.out.println();
	    
	    // redraw render window (canvas)
	    renderPanel.getCanvas().repaint();
		s.setV1(v1);
		s.setV2(v2);
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
