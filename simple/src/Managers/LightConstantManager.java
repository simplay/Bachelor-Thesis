package Managers;

import java.util.LinkedList;

import javax.vecmath.Vector4f;

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
