package Diffraction;

import java.util.ArrayList;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import jrtr.Light;
import jrtr.RenderContext;
import jrtr.Shape;
import jrtr.Texture;
import Constants.ShaderTaskNr;
import Materials.Material;
import SceneGraph.GraphSceneManager;
import SceneGraph.INode;
import SceneGraph.LightNode;
import SceneGraph.ShapeNode;
import SceneGraph.TransformGroup;
import Setup.Constants.BodyConstants;
import Setup.Constants.BumpConstants;
import Setup.Constants.CameraSceneConstant;
import Setup.Constants.SceneConfiguration;
import Setup.Managers.BodyConstantsManager;
import Setup.Managers.BumpConstantsManager;
import Setup.Managers.CameraSceneConstantManager;
import Setup.Managers.LightConstantManager;
import Setup.Managers.PreCompDataManager;
import Setup.Managers.SceneConfigurationManager;
import Setup.Managers.ShaderTaskSetupManager;
import Setup.Managers.ShapeManager;
import ShaderLogic.DiffractionShaderTask;
import ShaderLogic.TaylorGaussianShaderTask;
import ShaderLogic.ShaderTask;

public class DiffractionSceneGraphFabricator {
	private GraphSceneManager sceneManager;
	private RenderContext renderContext;
	private INode root;
	private ShaderTask activeShaderTask;
	private int layerCount = 39;
	private Shape targetShape;
	private Matrix4f targetIMat;
    private Material mat;
    private SceneConfigurationManager scm;
    private BumpConstantsManager bcm;
    private LightConstantManager lcm;
    private CameraSceneConstantManager cscm;
    private BodyConstantsManager bocm;
    
	private float trackDistance = 2.5f;
	private TransformGroup rootGroup;
	private SceneConfiguration sceneConfig;
	private String configName = "sandbox";
	private boolean useSpecificCam = false;
	
	public DiffractionSceneGraphFabricator(GraphSceneManager sceneManager, RenderContext renderContext){
		this.sceneManager = sceneManager;
		this.renderContext = renderContext;
		this.scm = new SceneConfigurationManager();
		this.sceneConfig = scm.getSceneConfigurationConstantByName(configName);
		this.bcm = new BumpConstantsManager();	
		this.lcm = new LightConstantManager();
		this.cscm = new CameraSceneConstantManager();
		this.bocm = new BodyConstantsManager();
		setUpShaderTask();
		mat = setUpMaterials();
		setUpShapes();
		setUpSceneGraph();
		setUpLight();
		setUpCamera(false);
	}
	
	private void setUpShaderTask(){
		if(sceneConfig.getShaderTask() == ShaderTaskNr.STAM){
		    activeShaderTask = new DiffractionShaderTask();
		}else if(sceneConfig.getShaderTask() == ShaderTaskNr.TAYLORGAUSSIAN){
			activeShaderTask = new TaylorGaussianShaderTask();
		}else if(sceneConfig.getShaderTask() == ShaderTaskNr.DEBUG_ANNOTATION){
			activeShaderTask = new TaylorGaussianShaderTask();
		}else if(sceneConfig.getShaderTask() == ShaderTaskNr.DEBUG_SPECULAR){
			activeShaderTask = new TaylorGaussianShaderTask();
		}
	}
	
	private Material setUpMaterials(){
		Material mat = new Material();
		BumpConstants bc = bcm.getByIdentifyer(sceneConfig.getBumpConstant());
		BodyConstants bodyC = bocm.getByIdentifyer(sceneConfig.getTextureId());
		Texture text = renderContext.makeTexture();
		mat.setBodyTexture(bodyC.getBodyTexturePath(), text);
		text = renderContext.makeTexture();
		mat.setBumpMapTexture(bodyC.getBumpMapTexturePath(), text);
		
		mat.setPeriodCount(sceneConfig.getPeriodCount());
		mat.setNeighborhoodRadius(sceneConfig.getNeighborhoodRadius());
		mat.setMaxBumpHeight(bc.getMaxHeight());
		mat.setPatchSpacing(bc.getSpacing());
		mat.setPatchDimX(bc.getDimX());
		mat.setPatchDimY(bc.getDimY());	
		mat.setMaterialColor(new Vector3f(0, 0f, 0f));
		mat.setShinnyCoefficient(new Vector3f(0f, 0f, 1f));
		mat.setAmbientCoefficient(new Vector3f(0.0f, 0.0f, 1.0f));
		mat.setPhongExponent(64f);
		mat.setTrackDistance(trackDistance);
		mat.setLayerCount(108);
		if( 
				sceneConfig.getShaderTask() == ShaderTaskNr.TAYLORGAUSSIAN ||
				sceneConfig.getShaderTask() == ShaderTaskNr.DEBUG_ANNOTATION ||
				sceneConfig.getShaderTask() == ShaderTaskNr.DEBUG_SPECULAR){
			mat.setLayerCount(layerCount);
		}
		ShaderTaskSetupManager stm = new ShaderTaskSetupManager(renderContext, mat, sceneConfig.getShaderTask());		
		mat.setShader(stm.getShader());
		new PreCompDataManager(renderContext, sceneConfig.getShaderTask(), sceneConfig.getPatchName(), mat); // TODO extend me, i want also the shape task, the shader task and further stuff

		return mat;
	}
	
	private void setUpLight(){
		Light light = lcm.getLightConstantByName(sceneConfig.getLightConstant());
		LightNode diceLightNode = new LightNode(light, sceneManager.getCamera().getCameraMatrix(), light.getName());
		rootGroup.putChild(diceLightNode);
	}
	
	private void setUpShapes(){	
		ShapeManager sm = new ShapeManager(sceneConfig.getShapeTask());
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
		CameraSceneConstant csc = cscm.getCameraSceneConstantByName(sceneConfig.getCameraConstant());
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
	
	public ShaderTaskNr getShaderTask(){
		return this.sceneConfig.getShaderTask();
	}
}
