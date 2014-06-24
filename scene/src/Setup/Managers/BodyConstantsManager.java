package Setup.Managers;

import java.util.LinkedList;
import Setup.Constants.BodyConstants;

public class BodyConstantsManager {
	private LinkedList<BodyConstants> constants;
	
	public BodyConstantsManager(){
		constants = new LinkedList<BodyConstants>();
		defineConstant();
	}
	
	private void defineConstant(){
		BodyConstants bc = null;

		bc = new BodyConstants("ElapheFront", "jpg");
		constants.add(bc);
		bc = new BodyConstants("ProcessedElapheFront", "bmp");
		constants.add(bc);
	}
	
	public BodyConstants getByIdentifyer(String id){
		BodyConstants bc = null;
		for(BodyConstants bump : constants){
			if(bump.getIdentifyerName().equals(id)){
				bc = bump;
				break;
			}
		}
		return bc;
	}
}
