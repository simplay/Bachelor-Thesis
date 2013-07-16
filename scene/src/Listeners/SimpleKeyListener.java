package Listeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import jrtr.Camera;
import jrtr.Light;
import jrtr.RenderPanel;
import Diffraction.DiffractionSceneGraphFabricator;
import SceneGraph.GraphSceneManager;
import SceneGraph.LightNode;

public class SimpleKeyListener implements KeyListener{
	private Storage s;
	private GraphSceneManager sceneManager;
	private RenderPanel renderPanel;
	private float speed = 0.01f;
	private DiffractionSceneGraphFabricator fabric;
	private float normDiv = 1.0f;
	private float delta_eps = (float) Math.pow(10, -7);
	
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
	
	private void moveLAP(int dir, float amount, boolean backward){
		Point3f new_lap = null;
		
		Camera c = sceneManager.getCamera();
		Point3f cop_old = new Point3f(c.getProjectionCenterPoint());
		Point3f cop_new = null;
	
		if(backward) amount = -amount;
		
		if(dir == 1){
			cop_new = new Point3f(cop_old.x+amount, cop_old.y, cop_old.z);
		}else if(dir == 2){
			cop_new = new Point3f(cop_old.x, cop_old.y+amount, cop_old.z);
		}else if(dir == 3){
			cop_new = new Point3f(cop_old.x, cop_old.y, cop_old.z+amount);
		}
		
		c.setProjectionCenterPoint(cop_new);
		renderPanel.getCanvas().repaint();
	}
	
	
	
//	Matrix4f camera22 = sceneManager.getCamera().getCameraMatrix();
//	Matrix4f rwMatrix22 = new Matrix4f();
//	rwMatrix22.setIdentity();
//	Vector3f rwVector22 = new Vector3f(0f, -speed, 0f);
//	rwMatrix22.setTranslation(rwVector22);
//	camera22.mul(rwMatrix22);
//	sceneManager.getCamera().setCameraMatrix(camera22);
//	renderPanel.getCanvas().repaint();
	
	@Override
	public void keyTyped(KeyEvent e) {
		
    	LightNode light = null;;
    	Vector3f radiance = null;;   	
    	Vector4f oldLightDir = null;;
    	Vector4f newLightDir = null;;
    	Vector3f tmpLightDir = null;;
		LightNode newlight = null;
		Light newlightSource = null;
		Camera c = null;
		Point3f o_lap = null;
		Point3f new_lap = null;
		float eps = 0.0f;
		
		switch (e.getKeyChar()) {
		    case 'w':		
		    	moveLAP(2, speed, false);
				break;
	
		    case 's':
		    	moveLAP(2, speed, true);
				break;
	
		    case 'a':
		    	moveLAP(1, speed, false);
				break;
	
		    case 'd':
		    	moveLAP(1, speed, true);
				break;
				
		    case 'e':
		    	moveLAP(3, speed, false);
				break;
				
		    case 'q':
		    	moveLAP(3, speed, true);
				break;
				
				
		    case 'u':
		    	eps = 0.2f;
		    	light = fabric.getLight();
		    	radiance = new Vector3f(1,1,1);   	
		    	oldLightDir = light.getLightSource().getLightDirection();
		    	
		    	newLightDir = new Vector4f(normDiv*oldLightDir.x , normDiv*oldLightDir.y, normDiv*oldLightDir.z, oldLightDir.w);
		    	tmpLightDir = new Vector3f(newLightDir.x + eps, newLightDir.y, newLightDir.z);
		    	
		    	
		    	
		    	if(Math.abs(Math.abs(tmpLightDir.x)-1.0f) < delta_eps ) tmpLightDir.x = 1.0f*Math.signum(tmpLightDir.x);
		    	if(Math.abs(Math.abs(tmpLightDir.y)-1.0f) < delta_eps ) tmpLightDir.y = 1.0f*Math.signum(tmpLightDir.y);
		    	if(Math.abs(Math.abs(tmpLightDir.z)-1.0f) < delta_eps ) tmpLightDir.z = 1.0f*Math.signum(tmpLightDir.z);
		    	
		    	if(Math.abs(tmpLightDir.x) < delta_eps ) tmpLightDir.x = 0.0f;
		    	if(Math.abs(tmpLightDir.y) < delta_eps ) tmpLightDir.y = 0.0f;
		    	if(Math.abs(tmpLightDir.z) < delta_eps ) tmpLightDir.z = 0.0f;
		    	tmpLightDir.normalize();
		    	
		    	newLightDir = new Vector4f(tmpLightDir.x, tmpLightDir.y, tmpLightDir.z, oldLightDir.w);
		    	normDiv =  (float) Math.sqrt( (oldLightDir.x - eps)*(oldLightDir.x - eps)+oldLightDir.y*oldLightDir.y+oldLightDir.z*oldLightDir.z );
		    	
				newlightSource  = new Light(radiance, newLightDir, "source1");
				newlight = new LightNode(newlightSource, sceneManager.getCamera().getCameraMatrix(), "light source1");		
		    	sceneManager.addLight(newlightSource);
				renderPanel.getCanvas().repaint();
				break;
				
		    case 'j':
		    	eps = -0.2f;
		    	light = fabric.getLight();
		    	radiance = new Vector3f(1,1,1);   	
		    	oldLightDir = light.getLightSource().getLightDirection();
		    	
		    	newLightDir = new Vector4f(normDiv*oldLightDir.x , normDiv*oldLightDir.y, normDiv*oldLightDir.z, oldLightDir.w);
		    	tmpLightDir = new Vector3f(newLightDir.x + eps, newLightDir.y, newLightDir.z);
		    	
		    	
		    	
		    	if(Math.abs(Math.abs(tmpLightDir.x)-1.0f) < delta_eps ) tmpLightDir.x = 1.0f*Math.signum(tmpLightDir.x);
		    	if(Math.abs(Math.abs(tmpLightDir.y)-1.0f) < delta_eps ) tmpLightDir.y = 1.0f*Math.signum(tmpLightDir.y);
		    	if(Math.abs(Math.abs(tmpLightDir.z)-1.0f) < delta_eps ) tmpLightDir.z = 1.0f*Math.signum(tmpLightDir.z);
		    	
		    	if(Math.abs(tmpLightDir.x) < delta_eps ) tmpLightDir.x = 0.0f;
		    	if(Math.abs(tmpLightDir.y) < delta_eps ) tmpLightDir.y = 0.0f;
		    	if(Math.abs(tmpLightDir.z) < delta_eps ) tmpLightDir.z = 0.0f;
		    	tmpLightDir.normalize();
		    	
		    	newLightDir = new Vector4f(tmpLightDir.x, tmpLightDir.y, tmpLightDir.z, oldLightDir.w);
		    	normDiv =  (float) Math.sqrt( (oldLightDir.x - eps)*(oldLightDir.x - eps)+oldLightDir.y*oldLightDir.y+oldLightDir.z*oldLightDir.z );
		    	
				newlightSource  = new Light(radiance, newLightDir, "source1");
				newlight = new LightNode(newlightSource, sceneManager.getCamera().getCameraMatrix(), "light source1");		
		    	sceneManager.addLight(newlightSource);
				renderPanel.getCanvas().repaint();
				break;
				
		    case 'i':
		    	eps = 0.2f;
		    	light = fabric.getLight();
		    	radiance = new Vector3f(1,1,1);   	
		    	oldLightDir = light.getLightSource().getLightDirection();
		    	
		    	newLightDir = new Vector4f(normDiv*oldLightDir.x , normDiv*oldLightDir.y, normDiv*oldLightDir.z, oldLightDir.w);
		    	tmpLightDir = new Vector3f(newLightDir.x, newLightDir.y+eps, newLightDir.z);
		    	tmpLightDir.normalize();
		    	
		    	if(Math.abs(Math.abs(tmpLightDir.x)-1.0f) < delta_eps ) tmpLightDir.x = 1.0f*Math.signum(tmpLightDir.x);
		    	if(Math.abs(Math.abs(tmpLightDir.y)-1.0f) < delta_eps ) tmpLightDir.y = 1.0f*Math.signum(tmpLightDir.y);
		    	if(Math.abs(Math.abs(tmpLightDir.z)-1.0f) < delta_eps ) tmpLightDir.z = 1.0f*Math.signum(tmpLightDir.z);
		    	
		    	if(Math.abs(tmpLightDir.x) < delta_eps ) tmpLightDir.x = 0.0f;
		    	if(Math.abs(tmpLightDir.y) < delta_eps ) tmpLightDir.y = 0.0f;
		    	if(Math.abs(tmpLightDir.z) < delta_eps ) tmpLightDir.z = 0.0f;
		    	
		    	newLightDir = new Vector4f(tmpLightDir.x, tmpLightDir.y, tmpLightDir.z, oldLightDir.w);
		    	normDiv =  (float) Math.sqrt( (oldLightDir.x)*(oldLightDir.x)+(oldLightDir.y+eps)*(oldLightDir.y+eps)+oldLightDir.z*oldLightDir.z );
		    	
				newlightSource  = new Light(radiance, newLightDir, "source1");
				newlight = new LightNode(newlightSource, sceneManager.getCamera().getCameraMatrix(), "light source1");		
		    	sceneManager.addLight(newlightSource);
				renderPanel.getCanvas().repaint();
				break;
				
		    case 'k':
		    	eps = -0.2f;
		    	light = fabric.getLight();
		    	radiance = new Vector3f(1,1,1);   	
		    	oldLightDir = light.getLightSource().getLightDirection();
		    	
		    	newLightDir = new Vector4f(normDiv*oldLightDir.x , normDiv*oldLightDir.y, normDiv*oldLightDir.z, oldLightDir.w);
		    	tmpLightDir = new Vector3f(newLightDir.x, newLightDir.y+eps, newLightDir.z);
		    	tmpLightDir.normalize();
		    	
		    	if(Math.abs(Math.abs(tmpLightDir.x)-1.0f) < delta_eps ) tmpLightDir.x = 1.0f*Math.signum(tmpLightDir.x);
		    	if(Math.abs(Math.abs(tmpLightDir.y)-1.0f) < delta_eps ) tmpLightDir.y = 1.0f*Math.signum(tmpLightDir.y);
		    	if(Math.abs(Math.abs(tmpLightDir.z)-1.0f) < delta_eps ) tmpLightDir.z = 1.0f*Math.signum(tmpLightDir.z);
		    	
		    	if(Math.abs(tmpLightDir.x) < delta_eps ) tmpLightDir.x = 0.0f;
		    	if(Math.abs(tmpLightDir.y) < delta_eps ) tmpLightDir.y = 0.0f;
		    	if(Math.abs(tmpLightDir.z) < delta_eps ) tmpLightDir.z = 0.0f;
		    	
		    	newLightDir = new Vector4f(tmpLightDir.x, tmpLightDir.y, tmpLightDir.z, oldLightDir.w);
		    	normDiv =  (float) Math.sqrt( (oldLightDir.x)*(oldLightDir.x)+(oldLightDir.y+eps)*(oldLightDir.y+eps)+oldLightDir.z*oldLightDir.z );
		    	
				newlightSource  = new Light(radiance, newLightDir, "source1");
				newlight = new LightNode(newlightSource, sceneManager.getCamera().getCameraMatrix(), "light source1");		
		    	sceneManager.addLight(newlightSource);
				renderPanel.getCanvas().repaint();
				break;
				
		    case 'o':
		    	eps = 0.2f;
		    	light = fabric.getLight();
		    	radiance = new Vector3f(1,1,1);   	
		    	oldLightDir = light.getLightSource().getLightDirection();
		    	
		    	newLightDir = new Vector4f(normDiv*oldLightDir.x , normDiv*oldLightDir.y, normDiv*oldLightDir.z, oldLightDir.w);
		    	tmpLightDir = new Vector3f(newLightDir.x, newLightDir.y, newLightDir.z+eps);
		    	tmpLightDir.normalize();
		    	
		    	if(Math.abs(Math.abs(tmpLightDir.x)-1.0f) < delta_eps ) tmpLightDir.x = 1.0f*Math.signum(tmpLightDir.x);
		    	if(Math.abs(Math.abs(tmpLightDir.y)-1.0f) < delta_eps ) tmpLightDir.y = 1.0f*Math.signum(tmpLightDir.y);
		    	if(Math.abs(Math.abs(tmpLightDir.z)-1.0f) < delta_eps ) tmpLightDir.z = 1.0f*Math.signum(tmpLightDir.z);
		    	
		    	if(Math.abs(tmpLightDir.x) < delta_eps ) tmpLightDir.x = 0.0f;
		    	if(Math.abs(tmpLightDir.y) < delta_eps ) tmpLightDir.y = 0.0f;
		    	if(Math.abs(tmpLightDir.z) < delta_eps ) tmpLightDir.z = 0.0f;
		    	
		    	newLightDir = new Vector4f(tmpLightDir.x, tmpLightDir.y, tmpLightDir.z, oldLightDir.w);
		    	normDiv =  (float) Math.sqrt( (oldLightDir.x)*(oldLightDir.x)+(oldLightDir.y)*(oldLightDir.y)+(oldLightDir.z+eps)*(oldLightDir.z+eps) );
		    	
				newlightSource  = new Light(radiance, newLightDir, "source1");
				newlight = new LightNode(newlightSource, sceneManager.getCamera().getCameraMatrix(), "light source1");		
		    	sceneManager.addLight(newlightSource);
				renderPanel.getCanvas().repaint();
				break;
				
		    case 'l':
		    	eps = -0.2f;
		    	light = fabric.getLight();
		    	radiance = new Vector3f(1,1,1);   	
		    	oldLightDir = light.getLightSource().getLightDirection();
		    	
		    	newLightDir = new Vector4f(normDiv*oldLightDir.x , normDiv*oldLightDir.y, normDiv*oldLightDir.z, oldLightDir.w);
		    	tmpLightDir = new Vector3f(newLightDir.x, newLightDir.y, newLightDir.z+eps);
		    	tmpLightDir.normalize();
		    	
		    	if(Math.abs(Math.abs(tmpLightDir.x)-1.0f) < delta_eps ) tmpLightDir.x = 1.0f*Math.signum(tmpLightDir.x);
		    	if(Math.abs(Math.abs(tmpLightDir.y)-1.0f) < delta_eps ) tmpLightDir.y = 1.0f*Math.signum(tmpLightDir.y);
		    	if(Math.abs(Math.abs(tmpLightDir.z)-1.0f) < delta_eps ) tmpLightDir.z = 1.0f*Math.signum(tmpLightDir.z);
		    	
		    	if(Math.abs(tmpLightDir.x) < delta_eps ) tmpLightDir.x = 0.0f;
		    	if(Math.abs(tmpLightDir.y) < delta_eps ) tmpLightDir.y = 0.0f;
		    	if(Math.abs(tmpLightDir.z) < delta_eps ) tmpLightDir.z = 0.0f;
		    	
		    	newLightDir = new Vector4f(tmpLightDir.x, tmpLightDir.y, tmpLightDir.z, oldLightDir.w);
		    	normDiv =  (float) Math.sqrt( (oldLightDir.x)*(oldLightDir.x)+(oldLightDir.y)*(oldLightDir.y)+(oldLightDir.z+eps)*(oldLightDir.z+eps) );
		    	
				newlightSource  = new Light(radiance, newLightDir, "source1");
				newlight = new LightNode(newlightSource, sceneManager.getCamera().getCameraMatrix(), "light source1");		
		    	sceneManager.addLight(newlightSource);
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
	
	public void setFabric(DiffractionSceneGraphFabricator fabric){
		this.fabric = fabric;
	}

}
