package Setup.Managers;

import java.util.LinkedList;

import javax.vecmath.Vector4f;

import Setup.Constants.LightConstants;

import jrtr.Light;

public class LightConstantManager {
	private LinkedList<LightConstants> constants;
	public LightConstantManager() {
		constants = new LinkedList<LightConstants>();
		defineConstant();
	}
	
	private void defineConstant(){
		LightConstants lc = null;
		
		lc = new LightConstants("light1", new Vector4f(-0.1f, 0.0f, (float) -Math.sqrt(0.99f), 0.0f));
		constants.add(lc);
		
		lc = new LightConstants("light2", new Vector4f(-0.09284766f, 0.37139064f, -0.92382264f, 0.0f));
		constants.add(lc);
		
		lc = new LightConstants("light3", new Vector4f(0.0f, 0.0f, -1.0f, 0.0f));
		constants.add(lc);
	}
	
	public Light getLightConstantByName(String id){
		Light lc = null;
		for(LightConstants lightConst : constants){
			if(lightConst.getId().equals(id)){
				lc = lightConst.getLight();
				break;
			}
		}
		return lc;
	}
}
