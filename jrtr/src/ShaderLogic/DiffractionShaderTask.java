package ShaderLogic;

import java.util.ArrayList;
import javax.media.opengl.GL3;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import Materials.Material;
import jrtr.GLShader;
import jrtr.Light;

public class DiffractionShaderTask extends ShaderTask{
	
	@Override
	public void loadMaterialGLShaderUniforms(Material m) {
		GL3 gl = this.getGL();
		GLShader activeShader = this.getShader();
		
		Vector3f kd = m.getMaterialColor();
		int id = gl.glGetUniformLocation(activeShader.programId(), "k_d");
		gl.glUniform3f(id, kd.x, kd.y, kd.z);
		
		Vector3f ks = m.getShinnyCoefficient();
		id = gl.glGetUniformLocation(activeShader.programId(), "k_s");
		gl.glUniform3f(id, ks.x, ks.y, ks.z);
		
		Vector3f ka = m.getAmbientCoefficient();
		id = gl.glGetUniformLocation(activeShader.programId(), "k_a");
		gl.glUniform3f(id, ka.x, ka.y, ka.z);
		
		float phong = m.getPhongExponent();
		int id2 = gl.glGetUniformLocation(activeShader.programId(), "p");
		gl.glUniform1f(id2, phong);
		
		float trackDistance = m.getTrackDistance();
		int id3 = gl.glGetUniformLocation(activeShader.programId(), "d");
		gl.glUniform1f(id3, trackDistance);
		
		id = gl.glGetUniformLocation(activeShader.programId(), "lightDirection");
		gl.glUniform4f(id, 0, 0, 1, 0);		// Set light direction
		
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
