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
		
			}else if(shaderTask == ShaderTaskNr.TAYLORGAUSSIAN){
				shader.load(ShaderPaths.tg_Vert.toString(), ShaderPaths.tg_Frag.toString());
			}else if(shaderTask == ShaderTaskNr.DEBUG_ANNOTATION){

			}else if(shaderTask == ShaderTaskNr.DEBUG_SPECULAR){

			}
		} catch (Exception e) {}
		
		mat.setShader(shader);
	}
	
	public Shader getShader(){
		return this.shader;
	}
}
