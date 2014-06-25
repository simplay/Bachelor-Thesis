package ShaderLogic;

import java.util.ArrayList;
import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import jrtr.GLShader;
import jrtr.GLTextureFloat;
import jrtr.GLTexture;
import jrtr.Light;
import Materials.Material;

public class TaylorGaussianShaderTask extends ShaderTask{

	@Override
	public void loadMaterialGLShaderUniforms(Material m) {
		GL3 gl = this.getGL();
		GLShader activeShader = this.getShader();
		
		float trackDistance = m.getTrackDistance();
		int id3 = gl.glGetUniformLocation(activeShader.programId(), "d");
		gl.glUniform1f(id3, trackDistance);
		
		// load texture array storing all our Fourier transformed patches.
		gl.glUniform1i(gl.glGetUniformLocation(activeShader.programId(), "TexArray"), 0);
		int width = 0;
		int height = 0;
		
		if(m.getTextureAt(0) instanceof GLTextureFloat){
			GLTextureFloat t = (GLTextureFloat) m.getTextureAt(0);
			width = t.getImWidth();
			height = t.getImHeight();
			
			System.out.println("Loading txt textures with eidth = " +width + " and height "+ height);
			gl.glTexImage3D(GL.GL_TEXTURE_2D_ARRAY, 0, GL.GL_RGB32F, width, height, m.getLayerCount(), 0, GL.GL_RGB, GL.GL_FLOAT, null);
			for(int iter = 0; iter < m.getLayerCount(); iter++){
				t = (GLTextureFloat) m.getTextureAt(iter);
				gl.glTexParameteri(GL.GL_TEXTURE_2D_ARRAY, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
				gl.glTexParameteri(GL.GL_TEXTURE_2D_ARRAY, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
				gl.glTexParameteri(GL.GL_TEXTURE_2D_ARRAY, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
				gl.glTexParameteri(GL.GL_TEXTURE_2D_ARRAY, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
				gl.glTexSubImage3D(GL.GL_TEXTURE_2D_ARRAY, 0, 0, 0, iter, width, height, 1, GL.GL_RGB, GL.GL_FLOAT, t.getByteBuffer());
			}
		}else{
			GLTexture t = (GLTexture) m.getTextureAt(0);
			width = t.getImWidth();
			height = t.getImHeight();
			
			System.out.println("Loading bmp textures with eidth = " +width + " and height "+ height);
			gl.glTexImage3D(GL.GL_TEXTURE_2D_ARRAY, 0, GL.GL_RGBA, width, height, m.getLayerCount(), 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null);
			for(int iter = 0; iter < m.getLayerCount(); iter++){
				t = (GLTexture) m.getTextureAt(iter);
				gl.glTexParameteri(GL.GL_TEXTURE_2D_ARRAY, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
				gl.glTexParameteri(GL.GL_TEXTURE_2D_ARRAY, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
				gl.glTexParameteri(GL.GL_TEXTURE_2D_ARRAY, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
				gl.glTexParameteri(GL.GL_TEXTURE_2D_ARRAY, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
				gl.glTexSubImage3D(GL.GL_TEXTURE_2D_ARRAY, 0, 0, 0, iter, width, height, 1, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, t.getByteBuffer());
			}
		}

		
		int dimIDx = gl.glGetUniformLocation(activeShader.programId(),"fftHH");
		gl.glUniform1i(dimIDx, height);
		
		dimIDx = gl.glGetUniformLocation(activeShader.programId(),"fftWW");
		gl.glUniform1i(dimIDx, width);
		
		// use texture channel >=1 (not 0) since the texure array is already using channel 0.
        gl.glUniform1i(gl.glGetUniformLocation(activeShader.programId(), "bodyTexture"), 1);
        int id_body = ((GLTexture) m.getBodyTexture()).getId();
        gl.glActiveTexture(GL3.GL_TEXTURE1);
        gl.glBindTexture(GL3.GL_TEXTURE_2D, id_body);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
		
        
		// load scaling constants
		// note that the param format is
		// foreach t, foreach w, 
		// [realMin_(nm(t),w), realMax_(nm(t),w), imagMin_(nm(t),w), imagMax_(nm(t),w)]
		
		// TODO invoke a call in material, where factors are preloaded instead.
		float[] scalingFactors = m.getHeightfieldFactors();
		int paramFactorCount = scalingFactors.length/4; // TODO setup correctly
		int scalingID = gl.glGetUniformLocation(activeShader.programId(),"scalingFactors");
		gl.glUniform4fv(scalingID, paramFactorCount, scalingFactors, 0);
	
		//for (int iii=0; iii < scalingFactors.length; iii = iii+4)
			//System.out.println("Scaling Factor for " + iii/4 + " : " + scalingFactors[iii] + ", " +  scalingFactors[iii+1] + ", " +  scalingFactors[iii+2] +"\n");
		
		// handle weights stuff
		if(m.getWeights() != null){
			System.out.println("WEIGHT COUNT " + m.getWeights().length);
			scalingID = gl.glGetUniformLocation(activeShader.programId(),"brdf_weights");
			gl.glUniform4fv(scalingID, m.getWeights().length/4, m.getWeights(), 0);
		}
		
		
		System.out.println("WEIGHT COUNT " + m.getWeights().length);
		scalingID = gl.glGetUniformLocation(activeShader.programId(),"brdf_weights");
		gl.glUniform3fv(scalingID, m.getWeights().length/4, m.getWeights(), 0);
		
		System.out.println("WEIGHT COUNT " + m.getWeights().length);
		scalingID = gl.glGetUniformLocation(activeShader.programId(),"brdf_weights_Dal");
		gl.glUniform4fv(scalingID, m.getWeights().length/3, m.getWeights(), 0);
		
		
		float lambdaSteps = m.getWeights().length/4 - 1;
		gl.glUniform1f(gl.glGetUniformLocation(activeShader.programId(),"delLamda"),  (m.getLambdaMax() - m.getLambdaMin())/lambdaSteps);
		
		
		// handle globals
		
//		if(m.getGlobals() != null){
//			scalingID = gl.glGetUniformLocation(activeShader.programId(),"global_extrema");
//			gl.glUniform4fv(scalingID, m.getGlobals().length/4, m.getGlobals(), 0);
//		}
		
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
		System.out.println("LMIN " + lmin + " LMAX " + lmax);
		
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
		
		
		
//		float dimX = m.getPatchDimX();
		// see images and estimate
		float dimX = m.getPatchDimX();
		System.out.println("dim x " + dimX);
		id3 = gl.glGetUniformLocation(activeShader.programId(), "dimX");
		gl.glUniform1f(id3, width);
		
		id3 = gl.glGetUniformLocation(activeShader.programId(), "dimY");
		gl.glUniform1f(id3, height);
		
		int neighr = m.getNeighborhoodRadius();
		id3 = gl.glGetUniformLocation(activeShader.programId(), "neigh_rad");
		gl.glUniform1i(id3, neighr);
		
		
		int isCone = 0;
		id3 = gl.glGetUniformLocation(activeShader.programId(), "isCone");
		gl.glUniform1i(id3, isCone);
		
		// TODO make this user-setable
		// should we user a higher sampling rate close to the zero frequency region.
		int useOptSampling = 1;
		id3 = gl.glGetUniformLocation(activeShader.programId(), "useOptSampling");
		gl.glUniform1i(id3, useOptSampling);
		
		int renderBrdfMap = (m.getRenderBrdfMap()) ? 1 : 0;
		id3 = gl.glGetUniformLocation(activeShader.programId(), "shouldRenderBrdfMap");
		gl.glUniform1i(id3, renderBrdfMap);
		
//		float to = (dimX/width);
		float dh = scalingFactors[3];
//		to = (float)dh;
		id3 = gl.glGetUniformLocation(activeShader.programId(), "t0");
		gl.glUniform1f(id3, dh);
		
		// spacing between two patches
//		float dx = 35;
		float dx = (float)((double)dh*width*1000000.0d);//2.5f;
//		if(dx < 60.0) dx = 60;
		System.out.println("patch spacing in microns: " + dx);
		id3 = gl.glGetUniformLocation(activeShader.programId(), "dx");
		gl.glUniform1f(id3, dx);
		
		float minSpacer = (float) (dx / 0.38f);
		float maxSpacer = (float) (dx / 0.78f);
		System.out.println("max spacer: " + maxSpacer);
		System.out.println("min spacer: " + minSpacer);
		
		id3 = gl.glGetUniformLocation(activeShader.programId(), "minspacer");
		gl.glUniform1f(id3, minSpacer);
		
		id3 = gl.glGetUniformLocation(activeShader.programId(), "maxspacer");
		gl.glUniform1f(id3, maxSpacer);
		
		id3 = gl.glGetUniformLocation(activeShader.programId(), "dx");
		gl.glUniform1f(id3, dx);
		
		float patchResolution = 65.0f;
		System.out.println("patch total resolution in microns: " + patchResolution);
		id3 = gl.glGetUniformLocation(activeShader.programId(), "patchReso");
		gl.glUniform1f(id3, patchResolution);
		
//		float dh = scalingFactors[3];
		System.out.println("resolution: microns per pixel: " + dh);		
		
		float bruteforceSpacing = m.getBruteforceSpacing();
		System.out.println("bruteforce spacing: " + bruteforceSpacing);
		id3 = gl.glGetUniformLocation(activeShader.programId(), "bruteforcespacing");
		gl.glUniform1f(id3, bruteforceSpacing);
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
			//
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
