package Setup.Managers;

import java.util.LinkedList;
import Setup.Constants.BumpConstants;

public class BumpConstantsManager {
	
	private LinkedList<BumpConstants> constants;
	public BumpConstantsManager() {
		constants = new LinkedList<BumpConstants>();
		defineConstant();
	}
	
	/*
	 * BumpConstants(String identifyerName, float maxHeight, float dimX, float dimY, float spacing){
	 */
	private void defineConstant(){
		BumpConstants bc = null;
		
		bc = new BumpConstants("Stam", 
				(float) (2.4623f*Math.pow(10.0f, -7.0f)), 
				(float) (65.0f*Math.pow(10.0f, -6.0f)), 
				(float) (65.0f*Math.pow(10.0f, -6.0f)), 
				(float) (2.5f*Math.pow(10.0f, -6.0f)));
		constants.add(bc);
		
		
		bc = new BumpConstants("elaph", 
				(float) (3.75f*Math.pow(10.0f, -7.0f)), 
				(float) (9.2f*Math.pow(10.0f, -6.0f)), 
				(float) (9.2f*Math.pow(10.0f, -6.0f)), 
				(float) (2.5f*Math.pow(10.0f, -6.0f)));
		constants.add(bc);
		
		bc = new BumpConstants("xeno", 
				(float) (4.4345f*Math.pow(10.0f, -7.0f)), 
				(float) (8.5f*Math.pow(10.0f, -6.0f)), 
				(float) (8.5f*Math.pow(10.0f, -6.0f)), 
				(float) (2.5f*Math.pow(10.0f, -6.0f)));
		constants.add(bc);
		
		bc = new BumpConstants("SingleFingerElaphIdeal", 
				(float) (3.75f*Math.pow(10.0f, -7.0f)), 
				(float) (((30.0f*72.0f)/512.0f)*Math.pow(10.0f, -6.0f)), 
				(float) (((30.0f*28.0f)/512.0f)*Math.pow(10.0f, -6.0f)), 
				(float) (2.5f*Math.pow(10.0f, -6.0f)));
		constants.add(bc);
	}
	
	public BumpConstants getByIdentifyer(String id){
		BumpConstants bc = null;
		for(BumpConstants bump : constants){
			if(bump.getIdentifyerName().equals(id)){
				bc = bump;
				break;
			}
		}
		return bc;
	}
}
