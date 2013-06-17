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

public class ExpTaylorShaderTask extends ShaderTask{

	@Override
	public void loadMaterialGLShaderUniforms(Material m) {
		GL3 gl = this.getGL();
		GLShader activeShader = this.getShader();
		
		float trackDistance = m.getTrackDistance();
		int id3 = gl.glGetUniformLocation(activeShader.programId(), "d");
		gl.glUniform1f(id3, trackDistance);
		
		// load texture array storing all our Fourier transformed patches.
		gl.glUniform1i(gl.glGetUniformLocation(activeShader.programId(), "TexArray"), 0);
		GLTexture t = (GLTexture) m.getTextureAt(0);
		int width = t.getImWidth();
		int height = t.getImHeight();
		gl.glTexImage3D(GL.GL_TEXTURE_2D_ARRAY, 0, GL.GL_RGBA, width, height, m.getLayerCount(), 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null);
		for(int iter = 0; iter < m.getLayerCount(); iter++){
			t = (GLTexture) m.getTextureAt(iter);
			gl.glTexParameteri(GL.GL_TEXTURE_2D_ARRAY, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
			gl.glTexParameteri(GL.GL_TEXTURE_2D_ARRAY, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
			gl.glTexParameteri(GL.GL_TEXTURE_2D_ARRAY, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
			gl.glTexParameteri(GL.GL_TEXTURE_2D_ARRAY, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
			gl.glTexSubImage3D(GL.GL_TEXTURE_2D_ARRAY, 0, 0, 0, iter, width, height, 1, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, t.getByteBuffer());
		}
		
		// load scaling constants
		
		
		// note that the param format is
		// foreach t, foreach w, 
		// [realMin_(nm(t),w), realMax_(nm(t),w), imagMin_(nm(t),w), imagMax_(nm(t),w)]
		
		// TODO invoke a call in material, where factors are preloaded instead.
		float[] scalingFactors = m.getHeightfieldFactors();
		int paramFactorCount = scalingFactors.length/4; // TODO setup correctly
		
		if(paramFactorCount >= 1000){
			
//			float[] scalingFactors1 = m.getLeftHFFactors();
//			float[] scalingFactors2 = m.getRightHFFactors();
//			
//			int scalingID1 = gl.glGetUniformLocation(activeShader.programId(),"scalingFactors1");
//			gl.glUniform4fv(scalingID1, paramFactorCount/2, scalingFactors1, 0);
//			
//			int scalingID2 = gl.glGetUniformLocation(activeShader.programId(),"scalingFactors2");
//			gl.glUniform4fv(scalingID2, paramFactorCount/2, scalingFactors2, 0);
		}else{
			int scalingID = gl.glGetUniformLocation(activeShader.programId(),"scalingFactors");
			gl.glUniform4fv(scalingID, paramFactorCount, scalingFactors, 0);
		}
		
		// handle weights stuff
		if(m.getWeights() != null){
			System.out.println("WEIGHT COUNT " + m.getWeights().length);
			int scalingID = gl.glGetUniformLocation(activeShader.programId(),"brdf_weights");
			gl.glUniform3fv(scalingID, m.getWeights().length/3, m.getWeights(), 0);
		}
		
		// handle globals
		
		if(m.getGlobals() != null){
			int scalingID = gl.glGetUniformLocation(activeShader.programId(),"global_extrema");
			gl.glUniform4fv(scalingID, m.getGlobals().length/4, m.getGlobals(), 0);
		}



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