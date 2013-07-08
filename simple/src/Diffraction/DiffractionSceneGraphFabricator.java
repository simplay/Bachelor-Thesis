package Diffraction;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import jrtr.Light;
import jrtr.RenderContext;
import jrtr.Shape;
import Constants.ShaderTaskNr;
import Managers.BumpConstants;
import Managers.ConstantsManager;
import Managers.ParameterManager;
import Managers.PreCompDataManager;
import Managers.ShaderTaskSetupManager;
import Managers.ShapeManager;
import Materials.Material;
import SceneGraph.GraphSceneManager;
import SceneGraph.INode;
import SceneGraph.LightNode;
import SceneGraph.ShapeNode;
import SceneGraph.TransformGroup;
import ShaderLogic.DiffractionShaderTask;
import ShaderLogic.ExpTaylorShaderTask;
import ShaderLogic.MultiTexturesTAShaderTask;
import ShaderLogic.MultiTexturesTaylorShaderTask;
import ShaderLogic.ShaderTask;
import Constants.ShapeTask;


public class DiffractionSceneGraphFabricator {
	private GraphSceneManager sceneManager;
	private RenderContext renderContext;
	private INode root;
	private ShaderTask activeShaderTask;

	private Shape targetShape;
	private Matrix4f targetIMat;
    private Material mat;
    private PreCompDataManager pcdm;
    private ConstantsManager cm;
    
	private Light lightSource1;
	private float trackDistance = 2.5f;
	private TransformGroup rootGroup;
	private ShapeTask shapeTask = ShapeTask.PLANE;
	private String parameter_path = "../jrtr/textures/sampleX/experimental/blaze/paramters.txt";
	

	private ShaderTaskNr shaderTask = ShaderTaskNr.EXPERIMENTAL;
	private boolean useSpecificCam = false;
	public DiffractionSceneGraphFabricator(GraphSceneManager sceneManager, RenderContext renderContext){
		this.sceneManager = sceneManager;
		this.renderContext = renderContext;
		this.cm = new ConstantsManager();		
		setUpShaderTask();
		setUpMaterials();
		setUpShapes();
		setUpSceneGraph();
		setUpLight();
		setUpCamera(false);
	}
	
	private void setUpShaderTask(){

		if(shaderTask == ShaderTaskNr.GRID){
			activeShaderTask = new MultiTexturesTAShaderTask();
		}else if(shaderTask == ShaderTaskNr.TAYLOR){
			activeShaderTask = new MultiTexturesTaylorShaderTask();
		}else if(shaderTask == ShaderTaskNr.EXPERIMENTAL){
			activeShaderTask = new ExpTaylorShaderTask();
		}else if(shaderTask == ShaderTaskNr.STAM){
		    activeShaderTask = new DiffractionShaderTask();
		}
	}
	
	private void setUpMaterials(){
		mat = new Material();
		ParameterManager pm = new ParameterManager(mat, parameter_path);
		BumpConstants bc = cm.getByIdentifyer("Stam");
		mat.setPeriodCount(26);
		mat.setMaxBumpHeight(bc.getMaxHeight());
		mat.setPatchSpacing(bc.getSpacing());
		
		mat.setMaterialColor(new Vector3f(0, 0f, 0f));
		mat.setShinnyCoefficient(new Vector3f(0f, 0f, 1f));
		mat.setAmbientCoefficient(new Vector3f(0.0f, 0.0f, 1.0f));
		mat.setPhongExponent(64f);
		mat.setTrackDistance(trackDistance);
		mat.setLayerCount(108);
		if(shaderTask == ShaderTaskNr.TAYLOR || shaderTask == ShaderTaskNr.EXPERIMENTAL) mat.setLayerCount(62);
		ShaderTaskSetupManager stm = new ShaderTaskSetupManager(renderContext, mat, shaderTask);		
		mat.setShader(stm.getShader());
		pcdm = new PreCompDataManager(renderContext, shaderTask.getValue(), mat);
	}
	
	
	private void setUpLight(){
		Vector3f radiance = new Vector3f(1,1,1); 
		Vector4f lightDirection = new Vector4f(-0.1f, 0.0f, (float) -Math.sqrt(0.99f), 0.0f);  //directional light source
//		lightDirection = new Vector4f(0, 0, 10, 1);  //directional light source
		lightSource1 = new Light(radiance, lightDirection, "source1");
		LightNode diceLightNode = new LightNode(lightSource1, sceneManager.getCamera().getCameraMatrix(), "light source1");
		rootGroup.putChild(diceLightNode);
	}
	
	private void setUpShapes(){	
		ShapeManager sm = new ShapeManager(shapeTask);
		this.targetShape = sm.getShape();
		this.targetIMat = sm.getTransformation();
		targetShape.setShaderTask(activeShaderTask);
		targetShape.setMaterial(mat);	
	}
	
	private void setUpSceneGraph(){
		rootGroup = new TransformGroup("root");
		this.root = rootGroup;	
		rootGroup.putChild(new ShapeNode(targetShape, "target shape"));
	}
	
	private void setUpCamera(boolean isFar){
		Point3f cop = null;
		float distance = 0.0f;
		if(shapeTask == ShapeTask.PLANE){
			distance = 1.0f;
			float aspectRatio = 1.0f;
			float near = 0.0001f;
			float far = 5500.0f;
			float verticalFieldView = 15.0f;
			Vector3f up = new Vector3f(0, 1, 0); // camera height
			Point3f look = new Point3f(0, 0, 0); // point camera looks at
			cop = new Point3f(0.1f, 0.0f, distance); // camera distance
			sceneManager.getFrustum().setParameter(aspectRatio, near, far, verticalFieldView);
			sceneManager.getCamera().setParameter(cop, look, up);
			
		}else if(shapeTask == ShapeTask.SNAKE){
//			distance = 160.0f; // teapot
			distance = 12.0f;
			float aspectRatio = 1.0f;
			float near = 0.0001f;
			float far = 5500.0f;
			float verticalFieldView = 20.0f;
			Vector3f up = new Vector3f(0, 1, 0); // camera height
			Point3f look = new Point3f(1, 0, 0); // point camera looks at
			cop = new Point3f(0.1f, 0.0f, distance); // camera distance
			sceneManager.getFrustum().setParameter(aspectRatio, near, far, verticalFieldView);
			sceneManager.getCamera().setParameter(cop, look, up);
	
		}else{
			distance = 0.1f;
			float aspectRatio = 1.0f;
			float near = 0.0001f;
			float far = 5500.0f;
			float verticalFieldView = 30f;
			Vector3f up = new Vector3f(0, 1, 0); // camera height
			Point3f look = new Point3f(0, 0, 0); // point camera looks at
			cop = new Point3f(0, 0, distance); // camera distance
			sceneManager.getFrustum().setParameter(aspectRatio, near, far, verticalFieldView);
			sceneManager.getCamera().setParameter(cop, look, up);
			
			
			Matrix4f ma = new Matrix4f();
			float[] a = {0.94874644f, 0.25298318f, -0.18942694f, 0.13349566f};
			float[] b = {-0.049736004f, 0.7114186f, 0.7010073f, 0.32658237f};
			float[] c = {0.31210417f, -0.6556542f, 0.6875345f, -10.968632f};
			float[] d = {0.0f, 0.0f, 0.0f, 1.0f};
			ma.setRow(0, a);
			ma.setRow(1, b);
			ma.setRow(2, c);
			ma.setRow(3, d);	
			mat.setDistanceToCamera(distance);	
			sceneManager.getCamera().setCameraMatrix(ma);
		}
		
		if(useSpecificCam) setSpecificCam();
		mat.setCOP(cop);
		
	}
	
	private void setSpecificCam(){
		Matrix4f ma = new Matrix4f();
		float[] a = {0.94874644f, 0.25298318f, -0.18942694f, 0.13349566f};
		float[] b = {-0.049736004f, 0.7114186f, 0.7010073f, 0.32658237f};
		float[] c = {0.31210417f, -0.6556542f, 0.6875345f, -10.968632f};
		float[] d = {0.0f, 0.0f, 0.0f, 1.0f};
		ma.setRow(0, a);
		ma.setRow(1, b);
		ma.setRow(2, c);
		ma.setRow(3, d);
		
		sceneManager.getCamera().setCameraMatrix(ma);
	}
	
	public INode getRoot(){
		return this.root;
	}
	
	public Matrix4f calculateDiffDiceGroup(float phi){
		Matrix4f answer = new Matrix4f();
		answer.setIdentity();
		Matrix4f rotX = new Matrix4f();
		rotX.rotX(phi);
		Matrix4f rotY = new Matrix4f();
		rotY.rotY(phi);
		answer.mul(rotX);
		answer.mul(rotY);
		return answer;
	}
	
	public TransformGroup getDiffDiceGroup(){
		return this.rootGroup;
	}
	
	public Matrix4f getDiffDiceIMat(){
		return this.targetIMat;
	}
	
	public Matrix4f getDiffPlane(){
		return this.targetIMat;
	}
	
	public LightNode getLight(){
		ArrayList<INode> nodes = rootGroup.getChildren();
		LightNode answer = null;
		for(INode node : nodes){
			if(node instanceof LightNode){
				answer = (LightNode) node;
				break;
			}
		}
		return answer;
	}
	
}
