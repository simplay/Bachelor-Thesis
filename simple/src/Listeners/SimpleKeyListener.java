package Listeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import jrtr.RenderPanel;
import SceneGraph.GraphSceneManager;

public class SimpleKeyListener implements KeyListener{
	Storage s;
	int width, height;
	Vector3f v1,v2;
	float theta;
	GraphSceneManager sceneManager;
	RenderPanel renderPanel;
	float speed = 1.0f;
	
	public SimpleKeyListener(Storage s, GraphSceneManager sceneManager, RenderPanel renderPanel){
		this.s = s;
		this.sceneManager = sceneManager;
		this.renderPanel = renderPanel;
	}
	
	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		switch (e.getKeyChar()) {
		    case 'w':
				Matrix4f camera = sceneManager.getCamera().getCameraMatrix();
				Matrix4f rwMatrix = new Matrix4f();
				rwMatrix.setIdentity();
				Vector3f rwVector = new Vector3f(0f, 0f, speed);
				rwMatrix.setTranslation(rwVector);
				camera.mul(rwMatrix);
				sceneManager.getCamera().setCameraMatrix(camera);
				renderPanel.getCanvas().repaint();
				break;
	
		    case 's':
				Matrix4f camera2 = sceneManager.getCamera().getCameraMatrix();
				Matrix4f rwMatrix2 = new Matrix4f();
				rwMatrix2.setIdentity();
				Vector3f rwVector2 = new Vector3f(0f, 0f, -speed);
				rwMatrix2.setTranslation(rwVector2);
				camera2.mul(rwMatrix2);
				sceneManager.getCamera().setCameraMatrix(camera2);
	
				// Trigger redrawing of the render window
				renderPanel.getCanvas().repaint();
				break;
	
		    case 'a':
				Matrix4f camera1 = sceneManager.getCamera().getCameraMatrix();
				Matrix4f rwMatrix1 = new Matrix4f();
				rwMatrix1.setIdentity();
				Vector3f rwVector1 = new Vector3f(speed, 0f, 0f);
				rwMatrix1.setTranslation(rwVector1);
				camera1.mul(rwMatrix1);
				sceneManager.getCamera().setCameraMatrix(camera1);
				renderPanel.getCanvas().repaint();
				break;
	
		    case 'd':
				Matrix4f camera11 = sceneManager.getCamera().getCameraMatrix();
				Matrix4f rwMatrix11 = new Matrix4f();
				rwMatrix11.setIdentity();
				Vector3f rwVector11 = new Vector3f(-speed, 0f, 0f);
				rwMatrix11.setTranslation(rwVector11);
				camera11.mul(rwMatrix11);
				sceneManager.getCamera().setCameraMatrix(camera11);
				renderPanel.getCanvas().repaint();
				break;
				
		    case 'e':
				Matrix4f camera12 = sceneManager.getCamera().getCameraMatrix();
				Matrix4f rwMatrix12 = new Matrix4f();
				rwMatrix12.setIdentity();
				Vector3f rwVector12 = new Vector3f(0f, -speed, 0f);
				rwMatrix12.setTranslation(rwVector12);
				camera12.mul(rwMatrix12);
				sceneManager.getCamera().setCameraMatrix(camera12);
				renderPanel.getCanvas().repaint();
				break;
				
		    case 'q':
				Matrix4f camera22 = sceneManager.getCamera().getCameraMatrix();
				Matrix4f rwMatrix22 = new Matrix4f();
				rwMatrix22.setIdentity();
				Vector3f rwVector22 = new Vector3f(0f, speed, 0f);
				rwMatrix22.setTranslation(rwVector22);
				camera22.mul(rwMatrix22);
				sceneManager.getCamera().setCameraMatrix(camera22);
				renderPanel.getCanvas().repaint();
				break;
				
		    case '+':
		    	if(speed < 1.95f)this.speed += 0.05;
				break;
		    case '-':
		    	
		    	if(speed > 0.10f) this.speed -= 0.05;
				break;

		}
	}

}
