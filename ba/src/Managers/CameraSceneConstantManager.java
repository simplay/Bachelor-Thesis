package Managers;

import java.util.LinkedList;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

public class CameraSceneConstantManager {
	private LinkedList<CameraSceneConstant> constants;
	public CameraSceneConstantManager() {
		constants = new LinkedList<CameraSceneConstant>();
		defineConstant();
	}
	
	private void defineConstant(){
		CameraSceneConstant cs = null;		
		cs = new CameraSceneConstant("plane1", 1.0f, 15.0f, new Vector3f(0, 1, 0), new Point3f(0, 0, 0));
		constants.add(cs);
		cs = new CameraSceneConstant("cylinder1", 15.0f, 4.0f, new Vector3f(0, 1, 0), new Point3f(0, 0, 0));
		constants.add(cs);
		cs = new CameraSceneConstant("snake1", 2.0f, 20.0f, new Vector3f(0, 1, 0), new Point3f(0, 0, 0));
		constants.add(cs);
		cs = new CameraSceneConstant("teapot1", 67.0f, 20.0f, new Vector3f(0, 1, 0), new Point3f(0, 0, 0));
		constants.add(cs);
		cs = new CameraSceneConstant("dice1", 10.0f, 30.0f, new Vector3f(0, 1, 0), new Point3f(0, 0, 0));
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
