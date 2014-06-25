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
			if(shaderTask == ShaderTaskNr.FLSS){
				shader.load(ShaderPaths.tg_Vert.toString(), ShaderPaths.tg_flss.toString());
			}else if(shaderTask == ShaderTaskNr.GEM){
				shader.load(ShaderPaths.tg_Vert.toString(), ShaderPaths.tg_gem.toString());
			}else if(shaderTask == ShaderTaskNr.NMM){
				shader.load(ShaderPaths.tg_Vert.toString(), ShaderPaths.tg_nmm.toString());
			}else if(shaderTask == ShaderTaskNr.PQ){
				shader.load(ShaderPaths.tg_Vert.toString(), ShaderPaths.tg_pq.toString());
			}
		} catch (Exception e) {}
		
		mat.setShader(shader);
	}
	
	public Shader getShader(){
		return this.shader;
	}
}
