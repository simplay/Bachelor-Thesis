package Diffraction;

import java.util.ArrayList;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import jrtr.Light;
import jrtr.RenderContext;
import jrtr.Shape;
import Constants.ShaderTaskNr;
import Managers.BumpConstants;
import Managers.BumpConstantsManager;
import Managers.CameraSceneConstant;
import Managers.CameraSceneConstantManager;
import Managers.LightConstantManager;
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
    private BumpConstantsManager bcm;
    private LightConstantManager lcm;
    private CameraSceneConstantManager cscm;
    
	private float trackDistance = 2.5f;
	private TransformGroup rootGroup;
	
	
	private ShapeTask shapeTask = ShapeTask.CYLINDER;
	private String parameter_path = "../jrtr/textures/sampleX/experimental/blaze/paramters.txt";
	private String cameraConstant = "plane1";
	private ShaderTaskNr shaderTask = ShaderTaskNr.EXPERIMENTAL;
	private boolean useSpecificCam = false;
	private int periodCount = 26;
	
	
	public DiffractionSceneGraphFabricator(GraphSceneManager sceneManager, RenderContext renderContext){
		this.sceneManager = sceneManager;
		this.renderContext = renderContext;
		this.bcm = new BumpConstantsManager();	
		this.lcm = new LightConstantManager();
		this.cscm = new CameraSceneConstantManager();
		
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
		new ParameterManager(mat, parameter_path);
		BumpConstants bc = bcm.getByIdentifyer("Stam");
		mat.setPeriodCount(periodCount);
		mat.setMaxBumpHeight(bc.getMaxHeight());
		mat.setPatchSpacing(bc.getSpacing());
		
		mat.setMaterialColor(new Vector3f(0, 0f, 0f));
		mat.setShinnyCoefficient(new Vector3f(0f, 0f, 1f));
		mat.setAmbientCoefficient(new Vector3f(0.0f, 0.0f, 1.0f));
		mat.setPhongExponent(64f);
		mat.setTrackDistance(trackDistance);
		mat.setLayerCount(108);
		if(shaderTask == ShaderTaskNr.TAYLOR || shaderTask == ShaderTaskNr.EXPERIMENTAL) mat.setLayerCount(31);
		ShaderTaskSetupManager stm = new ShaderTaskSetupManager(renderContext, mat, shaderTask);		
		mat.setShader(stm.getShader());
		new PreCompDataManager(renderContext, shaderTask.getValue(), mat); // TODO extend me, i want also the shape task, the shader task and further stuff
	}
	
	private void setUpLight(){
		Light light = lcm.getLightConstantByName("light1");
		LightNode diceLightNode = new LightNode(light, sceneManager.getCamera().getCameraMatrix(), light.getName());
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
		CameraSceneConstant csc = cscm.getCameraSceneConstantByName(cameraConstant);
		Point3f cop = csc.getCOP();
		if(useSpecificCam) setSpecificCam();
		sceneManager.getFrustum().setParameter(csc.getAspectRatio(), csc.getNear(), csc.getFar(), csc.getVerticalFieldView());
		sceneManager.getCamera().setParameter(csc.getCOP(), csc.getLook(), csc.getUp());
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
