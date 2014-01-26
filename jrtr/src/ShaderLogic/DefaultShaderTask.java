package ShaderLogic;

import java.util.ArrayList;

import javax.media.opengl.GL3;

import jrtr.GLShader;
import jrtr.Light;
import Materials.Material;

public class DefaultShaderTask extends ShaderTask{

	@Override
	public void loadMaterialGLShaderUniforms(Material m) {
		GL3 gl = this.getGL();
		GLShader activeShader = this.getShader();
		
		int isCone = 1;
		int id3 = gl.glGetUniformLocation(activeShader.programId(), "isCone");
		gl.glUniform1i(id3, isCone);
		
	}

	@Override
	public void loadLightGLShaderUniforms(ArrayList<Light> lightSources) {
		// TODO Auto-generated method stub
		
	}

}
