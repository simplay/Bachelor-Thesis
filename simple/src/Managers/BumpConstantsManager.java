package Managers;

import java.util.LinkedList;

public class BumpConstantsManager {
	
	private LinkedList<BumpConstants> constants;
	public BumpConstantsManager() {
		constants = new LinkedList<BumpConstants>();
		defineConstant();
	}
	
	private void defineConstant(){
		BumpConstants bc = null;
		
		bc = new BumpConstants("Stam", 
				(float) (2.4623*Math.pow(10.0f, -7.0f)), 
				(float) (2.5*Math.pow(10.0f, -6.0f)), 
				(float) (2.5*Math.pow(10.0f, -6.0f)), 
				(float) (2.5*Math.pow(10.0f, -6.0f)));
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
