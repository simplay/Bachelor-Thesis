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
//				shader.load(ShaderPaths.grid_1d_Vert.toString(), ShaderPaths.grid_1d_Frag.toString());
//				shader.load(ShaderPaths.grid_2d_Vert.toString(), ShaderPaths.grid_2d_Frag.toString());
//				shader.load(ShaderPaths.grid_T_1dVert.toString(), ShaderPaths.grid_T_1dFrag.toString());
//				shader.load(ShaderPaths.grid_T_2dVert.toString(), ShaderPaths.grid_T_2dFrag.toString());
			}else if(shaderTask == ShaderTaskNr.TAYLOR){
				shader.load(ShaderPaths.taylor_T_2d_Vert.toString(), ShaderPaths.taylor_T_2d_Frag.toString());
			}else if(shaderTask == ShaderTaskNr.EXPERIMENTAL_V){
				shader.load(ShaderPaths.expTaylor_2d_Vert.toString(), ShaderPaths.expTaylor_2d_Frag.toString());
			}else if(shaderTask == ShaderTaskNr.EXPERIMENTAL_F){
				shader.load(ShaderPaths.test_Vert.toString(), ShaderPaths.test_Frag.toString());
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
