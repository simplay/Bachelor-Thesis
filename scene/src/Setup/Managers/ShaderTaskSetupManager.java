package Setup.Managers;



import jrtr.RenderContext;
import jrtr.Shader;
import Constants.ShaderPaths;
import Constants.ShaderTaskNr;
import Materials.Material;

public class ShaderTaskSetupManager {
	private Shader shader;
	public ShaderTaskSetupManager(RenderContext renderContext, Material mat, ShaderTaskNr shaderTask){
		shader = renderContext.makeShader();
		try {
			
			if(shaderTask==ShaderTaskNr.ELSE){
	
			}else if(shaderTask == ShaderTaskNr.STAM){
				shader.load(ShaderPaths.stamVert.toString(), ShaderPaths.stamFrag.toString());
			}else if(shaderTask == ShaderTaskNr.GRID){
				shader.load(ShaderPaths.grid_1d_Vert.toString(), ShaderPaths.grid_1d_Frag.toString());
			}else if(shaderTask == ShaderTaskNr.TAYLOR){

			}else if(shaderTask == ShaderTaskNr.EXPERIMENTAL_V){

			}else if(shaderTask == ShaderTaskNr.EXPERIMENTAL_F){

			}else if(shaderTask == ShaderTaskNr.TAYLORGAUSSIAN){
				shader.load(ShaderPaths.tg_Vert.toString(), ShaderPaths.tg_Frag.toString());
			}else if(shaderTask == ShaderTaskNr.DEBUG_ANNOTATION){
				shader.load(ShaderPaths.tg_da_Vert.toString(), ShaderPaths.tg_da_Frag.toString());
			}else if(shaderTask == ShaderTaskNr.DEBUG_SPECULAR){
				shader.load(ShaderPaths.specular_Vert.toString(), ShaderPaths.specular_Frag.toString());
			}

		} catch (Exception e) {}
		
		mat.setShader(shader);
	}
	
	public Shader getShader(){
		return this.shader;
	}
}
