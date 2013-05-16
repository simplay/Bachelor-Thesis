/**
 * set up simulations scene: 
 * light, camera, shape
 * pass entities the simulations shader over
 * ask for computed values
 * assign gpu shader task
 * AK rdy in oder to perform simulation.
 */
package Simulator;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import jrtr.GLRenderContext;
import jrtr.Light;
import jrtr.RenderContext;
import jrtr.Shader;
import jrtr.Shape;
import Constants.ShaderPaths;
import Diffraction.DiffractionDice4;
import Diffraction.DiffractionPlane2;
import Materials.Material;
import SceneGraph.GraphSceneManager;
import SceneGraph.INode;
import SceneGraph.LightNode;
import SceneGraph.ShapeNode;
import SceneGraph.TransformGroup;
import ShaderLogic.DefaultShaderTask;
import ShaderLogic.DiffractionShaderTask;
import ShaderLogic.MultiTexturesTAShaderTask;
import ShaderLogic.ShaderTask;

public class SimulationFabricator {
	
	
	private GraphSceneManager sceneManager;
	private RenderContext renderContext;
	private INode root;
	private Shape diffPlane;
	private ShaderTask activeShaderTask;
	private TransformGroup rootGroup;
	private Light lightSource1;
	private Point3f look;
	private DiffractionPlane2 diffPlaneObj;
	private Material mat;
	
	public SimulationFabricator(GraphSceneManager sceneManager, RenderContext renderContext){
		this.sceneManager = sceneManager;
		this.renderContext = renderContext;
		
		rootGroup = new TransformGroup("root");
		this.root = rootGroup;
		
		setUpShaderTask();
		setUpMaterials();
		setUpShapes();
		setUpLight();
		setUpCamera();
		perfromSimulation();
		setUpSceneGraph();
	}
	
	// TODO: start shading simulation.
	private void perfromSimulation() {
		ShaderSimulator simulator = new ShaderSimulator(sceneManager, diffPlane, lightSource1, look);
		this.diffPlaneObj.overwriteColor(simulator.getColors());
		diffPlane = new Shape(diffPlaneObj.getVertices());
		diffPlane.setShaderTask(activeShaderTask);
		diffPlane.setMaterial(mat);
	}
	
	// TODO: write a shader Task for this
	private void setUpShaderTask(){
	    activeShaderTask = new DefaultShaderTask();
	}
	
	// TODO: define a material
	private void setUpMaterials(){
		mat = new Material();
		mat.setMaterialColor(new Vector3f(0, 0f, 0f));
		mat.setShinnyCoefficient(new Vector3f(0f, 0f, 1f));
		mat.setAmbientCoefficient(new Vector3f(0.0f, 0.0f, 1.0f));
		mat.setPhongExponent(64f);
		
		
		mat.setLayerCount(108);
		
		
		Shader shader = renderContext.makeShader();
		try {
			shader.load(ShaderPaths.defaultVert.toString(), ShaderPaths.defaultFrag.toString());
		} catch (Exception e) {}
		mat.setShader(shader);

	}
	
	private void setUpLight(){
		Vector3f radiance = new Vector3f(1,1,1);
		float coord = (float) (1.0f / Math.sqrt(3));
//		Vector4f lightDirection = new Vector4f(-0.5f, 0, - (float) (Math.sqrt(3)/2.0f), 0);  //directional light source
//		Vector4f lightDirection = new Vector4f(0.0f, 0, 10, 0);  //directional light source
		Vector4f lightDirection = new Vector4f(0.0f, 0, -1, 0);  //directional light source
		lightSource1 = new Light(radiance, lightDirection, "source1");
		LightNode diceLightNode = new LightNode(lightSource1, sceneManager.getCamera().getCameraMatrix(), "light source1");
		rootGroup.putChild(diceLightNode);
	}
	
	private void setUpShapes(){
		diffPlaneObj = new DiffractionPlane2(3,1f,1f);
		diffPlane = new Shape(diffPlaneObj.getVertices());
		diffPlane.setShaderTask(activeShaderTask);
		diffPlane.setMaterial(mat);
	}
	
	// TODO: assign root
	private void setUpSceneGraph(){

		rootGroup.putChild(new ShapeNode(diffPlane, "plane shape"));
	}
	
	private void setUpCamera(){
		float aspectRatio = 1.0f;
		float near = 1.0f;
		float far = 5500.0f;
		float verticalFieldView = 30; //viewing angle
		Vector3f up = new Vector3f(0, 1, 0);
//		look = new Point3f(-1f, -1f, 0); // -1,-1 nice for dir light source
		look = new Point3f(-1, -1, 0); // -1,-1 nice for dir light source
		Point3f cop = new Point3f(0f, 0f, 10); //camera distance
		sceneManager.getFrustum().setParameter(aspectRatio, near, far, verticalFieldView);
		sceneManager.getCamera().setParameter(cop, look, up);
	}
	
	public INode getRoot(){
		return this.root;
	}
	
	
}
