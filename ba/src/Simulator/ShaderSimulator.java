package Simulator;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector4f;

import jrtr.Light;
import jrtr.Shape;
import jrtr.VertexData.Semantic;
import SceneGraph.GraphSceneManager;

// this class prepares a vertex shader input
public class ShaderSimulator {
	private GraphSceneManager sceneManager;
	private Shape diffPlane;
	private Light lightSource;
	private Point3f look;
	private VertexShaderSimulator sim;
	// add later textures
	// globals
	// and so on...
	public ShaderSimulator(GraphSceneManager sceneManager, Shape diffPlane, Light lightSource, Point3f look){
		this.sceneManager = sceneManager;
		this.diffPlane = diffPlane;
		this.lightSource = lightSource;
		this.look = look;
		
		Vector4f lightDirection = lightSource.getLightDirection(); // in world space
		Matrix4f cameraMatrix = sceneManager.getCamera().getCameraMatrix();
		Point3f cameraEye = look; // TODO perhaps multiply by -1.
		float[] positions = diffPlane.getVertexData().getDataBySchematic(Semantic.POSITION);
		float[] normals = diffPlane.getVertexData().getDataBySchematic(Semantic.NORMAL);
		float[] tangents = diffPlane.getVertexData().getDataBySchematic(Semantic.TANGENT);
		
		sim = new VertexShaderSimulator(cameraMatrix, lightDirection, cameraEye, positions, normals, tangents);
	}
	
	public float[] getColors(){
		return this.sim.getColors();
	}

}
