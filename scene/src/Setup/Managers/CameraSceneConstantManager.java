package Setup.Managers;

import java.util.LinkedList;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import Setup.Constants.CameraSceneConstant;

public class CameraSceneConstantManager {
	private LinkedList<CameraSceneConstant> constants;
	public CameraSceneConstantManager() {
		constants = new LinkedList<CameraSceneConstant>();
		defineConstant();
	}
	
	/*
	 * CameraSceneConstant(String id, float distance, float verticalFieldView, Vector3f up, Point3f look, boolean orthCam)
	 */
	private void defineConstant(){
		CameraSceneConstant cs = null;		
		//cs = new CameraSceneConstant("plane1", 1.0f, 60.0f, new Vector3f(0, 1, 0), new Point3f(0, 0, 0), false);
		cs = new CameraSceneConstant("plane1", 1.0f, 15.0f, new Vector3f(0,1, 0), new Point3f(0, 0, 0), false);
		constants.add(cs);
		cs = new CameraSceneConstant("plane1_far", 20.0f, 2.0f, new Vector3f(0, 1, 0), new Point3f(0, 0, 0), false);
		constants.add(cs);
		cs = new CameraSceneConstant("plane8x8_far", 1.0f, 150.0f, new Vector3f(0, 1, 0), new Point3f(0, 0, 0), false);
		constants.add(cs);
		cs = new CameraSceneConstant("planeUV", 10.0f, 30.0f, new Vector3f(0, 1, 0), new Point3f(0, 0, 0), true);
		constants.add(cs);
		cs = new CameraSceneConstant("plane1_o", 1.0f, 15.0f, new Vector3f(0, 1, 0), new Point3f(0, 0, 0), true);
		constants.add(cs);
		cs = new CameraSceneConstant("plane1_w", 20.0f, 2.5f, new Vector3f(0, 1, 0), new Point3f(0, 0, 0), true);
		constants.add(cs);
		cs = new CameraSceneConstant("cylinder1", 15.0f, 4.0f, new Vector3f(0, 1, 0), new Point3f(0, 0, 0), false);
		constants.add(cs);
		cs = new CameraSceneConstant("snake1", 2.0f, 20.0f, new Vector3f(0, 1, 0), new Point3f(0, 0, 0), false);
		constants.add(cs);
		cs = new CameraSceneConstant("snake2", 2.0f, 1.0f, new Vector3f(0, 1, 0), new Point3f(0, 0, 0), false);
		constants.add(cs);
		cs = new CameraSceneConstant("snake3", 2.0f, 15.0f, new Vector3f(0, 1, 0), new Point3f(0, 0, 0), false);
		constants.add(cs);
		cs = new CameraSceneConstant("snake2far", 200.0f, 0.01f, new Vector3f(0, 1, 0), new Point3f(0, 0, 0), false);
		constants.add(cs);
		cs = new CameraSceneConstant("teapot1", 67.0f, 20.0f, new Vector3f(0, 1, 0), new Point3f(0, 0, 0), false);
		constants.add(cs);
		cs = new CameraSceneConstant("dice1", 10.0f, 30.0f, new Vector3f(0, 1, 0), new Point3f(0, 0, 0), false);
		constants.add(cs);
		cs = new CameraSceneConstant("dice2", 1.0f, 75.0f, new Vector3f(0, 1, 0), new Point3f(0, 0, 0), false);
		constants.add(cs);
		cs = new CameraSceneConstant("diceBRDF", 10.0f, 12.0f, new Vector3f(0, 1, 0), new Point3f(0, 0, 0), true);
		constants.add(cs);
	}
	
	public CameraSceneConstant getCameraSceneConstantByName(String id){
		CameraSceneConstant cs = null;
		for(CameraSceneConstant lightConst : constants){
			if(lightConst.getId().equals(id)){
				cs = lightConst;
				break;
			}
		}
		return cs;
	}
}
