package Setup.Constants;

public class BodyConstants {
	private String identifyerName;
	private String bodyTexturePath;
	private String bumpMapTexturePath;
	private String base_path = "../jrtr/textures/shape_textures/";
	
	public BodyConstants(String identifyerName, String extenstion){
		this.identifyerName = identifyerName;
		this.bodyTexturePath = base_path+"/body/"+identifyerName+"."+extenstion;
		this.bumpMapTexturePath = base_path+"/bumpmap/"+"bm_"+identifyerName+"."+extenstion;
	}
	
	public String getIdentifyerName(){
		return this.identifyerName;
	}
	
	public String getBodyTexturePath(){
		return this.bodyTexturePath;
	}
	
	public String getBumpMapTexturePath(){
		return this.bumpMapTexturePath;
	}
}
