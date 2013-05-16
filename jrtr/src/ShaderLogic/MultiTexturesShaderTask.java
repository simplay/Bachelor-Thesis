package ShaderLogic;

import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import jrtr.GLShader;
import jrtr.GLTexture;
import jrtr.Light;
import Materials.Material;

public class MultiTexturesShaderTask extends ShaderTask{

	@Override
	public void loadMaterialGLShaderUniforms(Material m) {
		GL3 gl = this.getGL();
		GLShader activeShader = this.getShader();
		
		
		float trackDistance = m.getTrackDistance();
		int id3 = gl.glGetUniformLocation(activeShader.programId(), "d");
		gl.glUniform1f(id3, trackDistance);

		for(int iter = 0; iter < 108; iter++){
			gl.glUniform1i(gl.glGetUniformLocation(activeShader.programId(), "Texture"+iter), iter);
			int idTex = ((GLTexture) m.getTextureAt(iter)).getId();
			
			gl.glActiveTexture(GL3.GL_TEXTURE0+iter);
			gl.glBindTexture(GL3.GL_TEXTURE_2D, idTex+iter);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
		}
		
		
		
		int k = gl.GL_MAX_TEXTURE_IMAGE_UNITS;
		System.out.println("foobar " + k);
//		
//		gl.glUniform1i(gl.glGetUniformLocation(activeShader.programId(), "Texture0"), 0);
//		gl.glUniform1i(gl.glGetUniformLocation(activeShader.programId(), "Texture1"), 1);
//		gl.glUniform1i(gl.glGetUniformLocation(activeShader.programId(), "Texture2"), 2);	
//		gl.glUniform1i(gl.glGetUniformLocation(activeShader.programId(), "Texture3"), 3);
//		gl.glUniform1i(gl.glGetUniformLocation(activeShader.programId(), "Texture4"), 4);
//		gl.glUniform1i(gl.glGetUniformLocation(activeShader.programId(), "Texture5"), 5);
//		gl.glUniform1i(gl.glGetUniformLocation(activeShader.programId(), "Texture6"), 6);
//		
//		int idTex0 = ((GLTexture) m.getTextureAt(0)).getId();
//		int idTex1 = ((GLTexture) m.getTextureAt(1)).getId();
//		int idTex2 = ((GLTexture) m.getTextureAt(2)).getId();
//		int idTex3 = ((GLTexture) m.getTextureAt(3)).getId();
//		int idTex4 = ((GLTexture) m.getTextureAt(4)).getId();
//		int idTex5 = ((GLTexture) m.getTextureAt(5)).getId();
//		int idTex6 = ((GLTexture) m.getTextureAt(6)).getId();
//
//		
//		gl.glActiveTexture(GL3.GL_TEXTURE0);
//		gl.glBindTexture(GL3.GL_TEXTURE_2D, idTex0);
//		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
//		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
//		
//		gl.glActiveTexture(GL3.GL_TEXTURE1);
//		gl.glBindTexture(GL3.GL_TEXTURE_2D, idTex1);
//		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
//		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);	
//		
//		gl.glActiveTexture(GL3.GL_TEXTURE2);
//		gl.glBindTexture(GL3.GL_TEXTURE_2D, idTex2);
//		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
//		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
//		
//		gl.glActiveTexture(GL3.GL_TEXTURE3);
//		gl.glBindTexture(GL3.GL_TEXTURE_2D, idTex3);
//		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
//		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
//		
//		gl.glActiveTexture(GL3.GL_TEXTURE4);
//		gl.glBindTexture(GL3.GL_TEXTURE_2D, idTex4);
//		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
//		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
//		
//		gl.glActiveTexture(GL3.GL_TEXTURE5);
//		gl.glBindTexture(GL3.GL_TEXTURE_2D, idTex5);
//		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
//		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
//		
//		gl.glActiveTexture(GL3.GL_TEXTURE6);
//		gl.glBindTexture(GL3.GL_TEXTURE_2D, idTex6);
//		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
//		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
	}

	@Override
	public void loadLightGLShaderUniforms(ArrayList<Light> lightSources) {
		GL3 gl = this.getGL();
		GLShader activeShader = this.getShader();
		
		int elementCount = lightSources.size(); 
		float[] lightDirections = new float[4*elementCount];
		float[] radiances = new float[3*elementCount];
		int offset = 0;
		
		Vector3f radiance = null;
		Vector4f lightDirection = null;
		int k = 0;
		for(Light beam : lightSources){
			radiance = beam.getRadiance();
			lightDirection = beam.getLightDirection();
			
			radiances[k*3] = radiance.x;
			radiances[k*3 +1] = radiance.y;
			radiances[k*3 +2] = radiance.z;
			
			lightDirections[k*4] = lightDirection.x;
			lightDirections[k*4 +1] = lightDirection.y;
			lightDirections[k*4 +2] = lightDirection.z;
			lightDirections[k*4 +3] = lightDirection.w;
			
			k++;
		}

		if(!lightSources.isEmpty()){
			int id = gl.glGetUniformLocation(activeShader.programId(),"directionArray");
			int id2 = gl.glGetUniformLocation(activeShader.programId(), "radianceArray");
			
			gl.glUniform4fv(id, elementCount, lightDirections, offset);
			gl.glUniform3fv(id2, elementCount, radiances, offset);
		}
		
	}

}
