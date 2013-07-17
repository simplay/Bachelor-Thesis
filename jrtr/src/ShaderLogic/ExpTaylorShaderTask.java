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
		
		
		// load body texture into shader
		gl.glActiveTexture(GL.GL_TEXTURE0);	// Work with texture unit 0
		gl.glBindTexture(GL.GL_TEXTURE_2D, ((GLTexture) m.getBodyTexture()).getId());
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
		int id_body = gl.glGetUniformLocation(activeShader.programId(), "bodyTexture");
		gl.glUniform1i(id_body, 0);
		
		
		
		// load scaling constants
		
		
		// note that the param format is
		// foreach t, foreach w, 
		// [realMin_(nm(t),w), realMax_(nm(t),w), imagMin_(nm(t),w), imagMax_(nm(t),w)]
		
		// TODO invoke a call in material, where factors are preloaded instead.
		float[] scalingFactors = m.getHeightfieldFactors();
		int paramFactorCount = scalingFactors.length/4; // TODO setup correctly
		int scalingID = gl.glGetUniformLocation(activeShader.programId(),"scalingFactors");
		gl.glUniform4fv(scalingID, paramFactorCount, scalingFactors, 0);
	
		
		// handle weights stuff
		if(m.getWeights() != null){
			System.out.println("WEIGHT COUNT " + m.getWeights().length);
			scalingID = gl.glGetUniformLocation(activeShader.programId(),"brdf_weights");
			gl.glUniform3fv(scalingID, m.getWeights().length/3, m.getWeights(), 0);
		}
		
		// handle globals
		
		if(m.getGlobals() != null){
			scalingID = gl.glGetUniformLocation(activeShader.programId(),"global_extrema");
			gl.glUniform4fv(scalingID, m.getGlobals().length/4, m.getGlobals(), 0);
		}
		
		Vector4f t_cop = m.getCOP();
		float[] cop_f = {t_cop.x, t_cop.y, t_cop.z, t_cop.w};
		scalingID = gl.glGetUniformLocation(activeShader.programId(),"cop_w");
		gl.glUniform4fv(scalingID, 1, cop_f, 0);
		
		System.out.println("assigned cop value: " + t_cop);
		
		float lmin = m.getLambdaMin();
		id3 = gl.glGetUniformLocation(activeShader.programId(), "LMIN");
		gl.glUniform1f(id3, lmin);
		
		float lmax = m.getLambdaMax();
		id3 = gl.glGetUniformLocation(activeShader.programId(), "LMAX");
		gl.glUniform1f(id3, lmax);
		
		
		float dimN = m.getDimN();
		id3 = gl.glGetUniformLocation(activeShader.programId(), "dimN");
		gl.glUniform1f(id3, dimN);
		
		float dimSmall = m.getDimSmall();
		id3 = gl.glGetUniformLocation(activeShader.programId(), "dimSmall");
		gl.glUniform1f(id3, dimSmall);
		
		float dimDiff = m.getDimDiff();
		id3 = gl.glGetUniformLocation(activeShader.programId(), "dimDiff");
		gl.glUniform1f(id3, dimDiff);
		
		float steps = m.getStepCount();
		id3 = gl.glGetUniformLocation(activeShader.programId(), "approxSteps");
		gl.glUniform1f(id3, steps);
		
		float rep_nn = m.getRepNN();
		id3 = gl.glGetUniformLocation(activeShader.programId(), "repNN");
		gl.glUniform1f(id3, rep_nn);
		
		int periodCount = m.getPeriodCount();
		id3 = gl.glGetUniformLocation(activeShader.programId(), "periodCount");
		gl.glUniform1i(id3, periodCount);
		
		float maxHeight = m.getMaxBumpHeight();
		id3 = gl.glGetUniformLocation(activeShader.programId(), "maxBumpHeight");
		gl.glUniform1f(id3, maxHeight);
		
		float patchSpacing = m.getPatchSpacing();
		id3 = gl.glGetUniformLocation(activeShader.programId(), "patchSpacing");
		gl.glUniform1f(id3, patchSpacing);
		
		float dimX = m.getPatchDimX();
		id3 = gl.glGetUniformLocation(activeShader.programId(), "dimX");
		gl.glUniform1f(id3, dimX);
		
		float dimY = m.getPatchDimY();
		id3 = gl.glGetUniformLocation(activeShader.programId(), "dimY");
		gl.glUniform1f(id3, dimY);
		
		int neighr = m.getNeighborhoodRadius();
		id3 = gl.glGetUniformLocation(activeShader.programId(), "neigh_rad");
		gl.glUniform1i(id3, neighr);

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
