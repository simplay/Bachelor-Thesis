package MVC;

import java.util.LinkedList;

import javax.vecmath.Matrix4f;

import jrtr.Camera;

import SceneGraph.GraphSceneManager;
import Util.Observer;
import Util.Subscriber;

public class Watchman extends Observer{
	private String cameraString;
	
	public Watchman(){
	
	}
	
	private String printedMatrix(Matrix4f mat, String id){
		String out = null;
		
		String line1 = mat.m00 + " " + mat.m01 + " " + mat.m02 + " " + mat.m03;
		String line2 = mat.m10 + " " + mat.m11 + " " + mat.m12 + " " + mat.m13;
		String line3 = mat.m20 + " " + mat.m21 + " " + mat.m22 + " " + mat.m23;
		String line4 = mat.m30 + " " + mat.m31 + " " + mat.m32 + " " + mat.m33;
		
		out = id + "\n";
		out += line1 + "\n";
		out += line2 + "\n";
		out += line3 + "\n";
		out += line4;
		return out;
	}
	
	public void computeData(GraphSceneManager sceneManager){
		Camera cam = sceneManager.getCamera();
		cameraString = printedMatrix(cam.getCameraMatrix(), "camera Matrix");
	}
	
	public String getCam(){
		return this.cameraString;
	}

}
