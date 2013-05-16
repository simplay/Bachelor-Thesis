package ShaderLogic;

import java.util.ArrayList;

import javax.media.opengl.GL3;

import Materials.Material;
import jrtr.GLShader;
import jrtr.Light;

public abstract class ShaderTask {
	private GLShader activeShader;
	private GL3 gl;
	
	public void setActiveShader(GLShader shader){
		this.activeShader = shader;
	}
	
	public void setGl(GL3 gl){
		this.gl = gl;
	}
	
	public GL3 getGL(){
		return this.gl;
	}
	
	public GLShader getShader(){
		return this.activeShader;
	}
	
	public abstract void loadMaterialGLShaderUniforms(Material m);
	public abstract void loadLightGLShaderUniforms(ArrayList<Light> lightSources);

}
