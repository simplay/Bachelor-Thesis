package Listeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import jrtr.RenderPanel;
import SceneGraph.GraphSceneManager;

public class ShooterMouseMotionListener implements MouseMotionListener{
	
	int width, height;
	Vector3f v1,v2;
	float theta;
	GraphSceneManager sceneManager;
	RenderPanel renderPanel;
	
	
	private float deltaX, deltaY;
	private float oldXPos, oldYPos;
	
	public ShooterMouseMotionListener(GraphSceneManager sceneManager, RenderPanel renderPanel, int width, int height){
		this.sceneManager = sceneManager;
		this.renderPanel = renderPanel;
		this.width = width;
		this.height = height;
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {

		
		
		
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		
		float newXPos = e.getXOnScreen();
		this.deltaX = newXPos-oldXPos;
		this.oldXPos = newXPos;
		
		float newYPos = e.getYOnScreen();
		this.deltaY = newYPos-oldYPos;
		this.oldYPos = newYPos;
		
		System.out.println(" shiftX " + (deltaX) + " shiftY " + (deltaY));
		

		
		
		Matrix4f camera = sceneManager.getCamera().getCameraMatrix();
		
		Matrix4f rotY = new Matrix4f();
		Matrix4f rotZ = new Matrix4f();
		rotY.rotY((deltaX)/80);
		rotZ.rotZ((deltaY)/1000);
		
		camera.mul(rotY);
		camera.mul(rotZ);
		sceneManager.getCamera().setCameraMatrix(camera);

		// Trigger redrawing of the render window
		renderPanel.getCanvas().repaint();
		
		
		
	}

}
