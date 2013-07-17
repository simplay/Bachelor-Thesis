package Setup.Constants;

public class BodyConstants {
	private String identifyerName;
	private String textturePath;
	private String base_path = "../jrtr/textures/shape_textures/";
	
	public BodyConstants(String identifyerName, String extenstion){
		this.identifyerName = identifyerName;
		this.textturePath = base_path+"/"+identifyerName+"."+extenstion;
	}
	
	public String getIdentifyerName(){
		return this.identifyerName;
	}
	
	public String getTexturePath(){
		return this.textturePath;
	}
}
