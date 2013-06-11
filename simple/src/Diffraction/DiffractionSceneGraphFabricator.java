package Diffraction;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
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
import jrtr.Shader;
import jrtr.Shape;
import jrtr.Texture;
import Constants.ShaderPaths;
import Materials.Material;
import SceneGraph.GraphSceneManager;
import SceneGraph.INode;
import SceneGraph.LightNode;
import SceneGraph.ShapeNode;
import SceneGraph.TransformGroup;
import ShaderLogic.DefaultShaderTask;
import ShaderLogic.DiffractionShaderTask;
import ShaderLogic.ExpTaylorShaderTask;
import ShaderLogic.MultiTexturesTAShaderTask;
import ShaderLogic.MultiTexturesTaylorShaderTask;
import ShaderLogic.ShaderTask;
import ShaderLogic.Task3ShaderTask;

public class DiffractionSceneGraphFabricator {
	private GraphSceneManager sceneManager;
	private RenderContext renderContext;
	private INode root;
	private ShaderTask activeShaderTask;
	private Shape diffDice;
	private Shape diffPlane;
	private Matrix4f diffDiceIMat;
	private Matrix4f diffPlaneIMat;
    private Material mat;

    private Texture[] textures = new Texture[2624];
	private Light lightSource1;
	private float trackDistance = 2.5f;
	private TransformGroup rootGroup;
	
	String extension = ".bmp";
	// stam 4
	// grid 9
	// taylor 10
	// experimental: adaptive taylor series 11
	private int version = 10;

	
	
	private boolean hasVectorfield = true;
	private boolean isPlane = true;

	public DiffractionSceneGraphFabricator(GraphSceneManager sceneManager, RenderContext renderContext){
		this.sceneManager = sceneManager;
		this.renderContext = renderContext;
		setUpShaderTask();
		setUpMaterials();
		setUpShapes();
		setUpSceneGraph();
		setUpLight();
		setUpCamera(false);
	}
	
	private void setUpShaderTask(){

		if(version == 9){
			activeShaderTask = new MultiTexturesTAShaderTask();
		}else if(version == 10){
			activeShaderTask = new MultiTexturesTaylorShaderTask();
		}else if(version == 11){
			activeShaderTask = new ExpTaylorShaderTask();
		}else if(version == 4){
		    activeShaderTask = new DiffractionShaderTask();
		}
	}
	
	private void setUpMaterials(){
		mat = new Material();
		mat.setMaterialColor(new Vector3f(0, 0f, 0f));
		mat.setShinnyCoefficient(new Vector3f(0f, 0f, 1f));
		mat.setAmbientCoefficient(new Vector3f(0.0f, 0.0f, 1.0f));
		mat.setPhongExponent(64f);
		mat.setTrackDistance(trackDistance);
		
		
		mat.setLayerCount(108);
		if(version == 10 || version == 11) mat.setLayerCount(62);
		
		Shader shader = renderContext.makeShader();
		try {
			
			if(version==1){
	
			}else if(version == 4){
				shader.load(ShaderPaths.stamVert.toString(), ShaderPaths.stamFrag.toString());
					
			}else if(version == 9){
//				shader.load(ShaderPaths.grid_1d_Vert.toString(), ShaderPaths.grid_1d_Frag.toString());|
//				shader.load(ShaderPaths.grid_1d_Vert.toString(), ShaderPaths.grid_1d_Frag.toString());
				//shader.load(ShaderPaths.grid_2d_Vert.toString(), ShaderPaths.grid_2d_Frag.toString());
//				shader.load(ShaderPaths.grid_T_1dVert.toString(), ShaderPaths.grid_T_1dFrag.toString());
				shader.load(ShaderPaths.grid_T_2dVert.toString(), ShaderPaths.grid_T_2dFrag.toString());
			}else if(version == 10){
				
//				shader.load(ShaderPaths.taylor_1d_Vert.toString(), ShaderPaths.taylor_1d_Frag.toString());
//				shader.load(ShaderPaths.taylor_2d_Vert.toString(), ShaderPaths.taylor_2d_Frag.toString());
//				shader.load(ShaderPaths.taylor_T_1d_Vert.toString(), ShaderPaths.taylor_T_1d_Frag.toString());
				shader.load(ShaderPaths.taylor_T_2d_Vert.toString(), ShaderPaths.taylor_T_2d_Frag.toString());
				
			}else if(version == 11){
				shader.load(ShaderPaths.expTaylor_2d_Vert.toString(), ShaderPaths.expTaylor_2d_Frag.toString());
			}
			

		} catch (Exception e) {}
		
		mat.setShader(shader);
		
		String samples = null;
		String extrema = null;
		
		if(version == 9 || version == 10){
			if(version==1){

				
			// using bmp rgb	
			}else if(version == 9){
				// basis
//				samples = "../jrtr/textures/sampleX/rgb1/";;
//				extrema = "../jrtr/textures/sampleX/rgb1/extrema.txt";;
//				mat.setKValues(loadKValues("../jrtr/textures/sampleX/rgb1/kvalues.txt"));
//				mat.setGlobals(loadglobals("../jrtr/textures/sampleX/rgb1/globals.txt"));
//				mat.setWeights(readWeights("../jrtr/textures/sampleX/rgb1/weights.txt"));
//				
				
				
//				samples = "../jrtr/textures/sampleX/milestone/1dw10/";
//				extrema = "../jrtr/textures/sampleX/milestone/1dw10/extrema.txt";
//				mat.setKValues(loadKValues("../jrtr/textures/sampleX/milestone/1dw10/kvalues.txt"));
//				mat.setGlobals(loadglobals("../jrtr/textures/sampleX/milestone/1dw10/globals.txt"));
//				mat.setWeights(readWeights("../jrtr/textures/sampleX/milestone/1dw10/weights.txt"));
				
				// basis comparision
				samples = "../jrtr/textures/sampleX/milestone/1dw20/";
				extrema = "../jrtr/textures/sampleX/milestone/1dw20/extrema.txt";
				mat.setKValues(loadKValues("../jrtr/textures/sampleX/milestone/1dw20/kvalues.txt"));
				mat.setGlobals(loadglobals("../jrtr/textures/sampleX/milestone/1dw20/globals.txt"));
				mat.setWeights(readWeights("../jrtr/textures/sampleX/milestone/1dw20/weights.txt"));
//				
				
				
//				samples = "../jrtr/textures/sampleX/2dStam_1/";
//				extrema = "../jrtr/textures/sampleX/2dStam_1/extrema.txt";
//				mat.setKValues(loadKValues("../jrtr/textures/sampleX/2dStam_1/kvalues.txt"));
//				mat.setGlobals(loadglobals("../jrtr/textures/sampleX/2dStam_1/globals.txt"));
//				mat.setWeights(readWeights("../jrtr/textures/sampleX/2dStam_1/weights.txt"));
				

				
				
//				samples = "../jrtr/textures/sampleX/milestone/1dw30/";
//				extrema = "../jrtr/textures/sampleX/milestone/1dw30/extrema.txt";
//				mat.setKValues(loadKValues("../jrtr/textures/sampleX/milestone/1dw30/kvalues.txt"));
//				mat.setGlobals(loadglobals("../jrtr/textures/sampleX/milestone/1dw30/globals.txt"));
//				mat.setWeights(readWeights("../jrtr/textures/sampleX/milestone/1dw30/weights.txt"));
				
				
//				samples = "../jrtr/textures/sampleX/milestone/1dspec/";
//				extrema = "../jrtr/textures/sampleX/milestone/1dspec/extrema.txt";
//				mat.setKValues(loadKValues("../jrtr/textures/sampleX/milestone/1dspec/kvalues.txt"));
//				mat.setGlobals(loadglobals("../jrtr/textures/sampleX/milestone/1dspec/globals.txt"));
//				mat.setWeights(readWeights("../jrtr/textures/sampleX/milestone/1dspec/weights.txt"));
				
				
//				samples = "../jrtr/textures/sampleX/milestone/tri/";
//				extrema = "../jrtr/textures/sampleX/milestone/tri/extrema.txt";
//				mat.setKValues(loadKValues("../jrtr/textures/sampleX/milestone/tri/kvalues.txt"));
//				mat.setGlobals(loadglobals("../jrtr/textures/sampleX/milestone/tri/globals.txt"));
//				mat.setWeights(readWeights("../jrtr/textures/sampleX/milestone/tri/weights.txt"));
				
				
//				samples = "../jrtr/textures/sampleX/milestone/sin/";
//				extrema = "../jrtr/textures/sampleX/milestone/sin/extrema.txt";
//				mat.setKValues(loadKValues("../jrtr/textures/sampleX/milestone/sin/kvalues.txt"));
//				mat.setGlobals(loadglobals("../jrtr/textures/sampleX/milestone/sin/globals.txt"));
//				mat.setWeights(readWeights("../jrtr/textures/sampleX/milestone/sin/weights.txt"));
				
//				samples = "../jrtr/textures/sampleX/milestone/crossw20/";
//				extrema = "../jrtr/textures/sampleX/milestone/crossw20/extrema.txt";
//				mat.setKValues(loadKValues("../jrtr/textures/sampleX/milestone/crossw20/kvalues.txt"));
//				mat.setGlobals(loadglobals("../jrtr/textures/sampleX/milestone/crossw20/globals.txt"));
//				mat.setWeights(readWeights("../jrtr/textures/sampleX/milestone/crossw20/weights.txt"));
				
//				samples = "../jrtr/textures/sampleX/milestone/C/";
//				extrema = "../jrtr/textures/sampleX/milestone/C/extrema.txt";
//				mat.setKValues(loadKValues("../jrtr/textures/sampleX/milestone/C/kvalues.txt"));
//				mat.setGlobals(loadglobals("../jrtr/textures/sampleX/milestone/C/globals.txt"));
//				mat.setWeights(readWeights("../jrtr/textures/sampleX/milestone/C/weights.txt"));
				
				
//				samples = "../jrtr/textures/sampleX/milestone/cos/";
//				extrema = "../jrtr/textures/sampleX/milestone/cos/extrema.txt";
//				mat.setKValues(loadKValues("../jrtr/textures/sampleX/milestone/cos/kvalues.txt"));
//				mat.setGlobals(loadglobals("../jrtr/textures/sampleX/milestone/cos/globals.txt"));
//				mat.setWeights(readWeights("../jrtr/textures/sampleX/milestone/cos/weights.txt"));
				

				
//				samples = "../jrtr/textures/sampleX/milestone/pew/";
//				extrema = "../jrtr/textures/sampleX/milestone/pew/extrema.txt";
//				mat.setKValues(loadKValues("../jrtr/textures/sampleX/milestone/pew/kvalues.txt"));
//				mat.setGlobals(loadglobals("../jrtr/textures/sampleX/milestone/pew/globals.txt"));
//				mat.setWeights(readWeights("../jrtr/textures/sampleX/milestone/pew/weights.txt"));

				String s_name1 = "1dStam";
				String s_name2 = "2dStam";
				String s_name3 = "blaze";
				String s_name4 = "cos";
				String s_name = s_name1;
				samples = "../jrtr/textures/sampleX/padded/"+ s_name +"/";
				extrema = "../jrtr/textures/sampleX/padded/"+ s_name +"/extrema.txt";
				mat.setKValues(loadKValues("../jrtr/textures/sampleX/padded/"+ s_name +"/kvalues.txt"));
				mat.setGlobals(loadglobals("../jrtr/textures/sampleX/padded/"+ s_name +"/globals.txt"));
				mat.setWeights(readWeights("../jrtr/textures/sampleX/padded/"+ s_name +"/weights.txt"));

//				
				
//				samples = "../jrtr/textures/sampleX/milestone/blaze/";
//				extrema = "../jrtr/textures/sampleX/milestone/blaze/extrema.txt";
//				mat.setKValues(loadKValues("../jrtr/textures/sampleX/milestone/blaze/kvalues.txt"));
//				mat.setGlobals(loadglobals("../jrtr/textures/sampleX/milestone/blaze/globals.txt"));
//				mat.setWeights(readWeights("../jrtr/textures/sampleX/milestone/blaze/weights.txt"));
				
			}else if(version == 10){
//				samples = "../jrtr/textures/sampleX/taylor/w10/";
//				extrema = "../jrtr/textures/sampleX/taylor/w10/extrema.txt";
//				mat.setKValues(loadKValues("../jrtr/textures/sampleX/taylor/w10/kvalues.txt"));
//				mat.setGlobals(loadglobals("../jrtr/textures/sampleX/taylor/w10/globals.txt"));
//				mat.setWeights(readWeights("../jrtr/textures/sampleX/taylor/w10/weights.txt"));
//				
//				samples = "../jrtr/textures/sampleX/taylor/w20/";
//				extrema = "../jrtr/textures/sampleX/taylor/w20/extrema.txt";
//				mat.setKValues(loadKValues("../jrtr/textures/sampleX/taylor/w20/kvalues.txt"));
//				mat.setGlobals(loadglobals("../jrtr/textures/sampleX/taylor/w20/globals.txt"));
//				mat.setWeights(readWeights("../jrtr/textures/sampleX/taylor/w20/weights.txt"));
				
				samples = "../jrtr/textures/sampleX/taylor/pewpew/";
				extrema = "../jrtr/textures/sampleX/taylor/pewpew/extrema.txt";
				mat.setKValues(loadKValues("../jrtr/textures/sampleX/taylor/pewpew/kvalues.txt"));
				mat.setGlobals(loadglobals("../jrtr/textures/sampleX/taylor/pewpew/globals.txt"));
				mat.setWeights(readWeights("../jrtr/textures/sampleX/taylor/pewpew/weights.txt"));
				
//				samples = "../jrtr/textures/sampleX/taylor/newA/";
//				extrema = "../jrtr/textures/sampleX/taylor/newA/extrema.txt";
//				mat.setKValues(loadKValues("../jrtr/textures/sampleX/taylor/newA/kvalues.txt"));
//				mat.setGlobals(loadglobals("../jrtr/textures/sampleX/taylor/newA/globals.txt"));
//				mat.setWeights(readWeights("../jrtr/textures/sampleX/taylor/newA/weights.txt"));
//				
//				samples = "../jrtr/textures/sampleX/taylor/w30/";
//				extrema = "../jrtr/textures/sampleX/taylor/w30/extrema.txt";
//				mat.setKValues(loadKValues("../jrtr/textures/sampleX/taylor/w30/kvalues.txt"));
//				mat.setGlobals(loadglobals("../jrtr/textures/sampleX/taylor/w30/globals.txt"));
//				mat.setWeights(readWeights("../jrtr/textures/sampleX/taylor/w30/weights.txt"));
//				
//				samples = "../jrtr/textures/sampleX/taylor/cos/";
//				extrema = "../jrtr/textures/sampleX/taylor/cos/extrema.txt";
//				mat.setKValues(loadKValues("../jrtr/textures/sampleX/taylor/cos/kvalues.txt"));
//				mat.setGlobals(loadglobals("../jrtr/textures/sampleX/taylor/cos/globals.txt"));
//				mat.setWeights(readWeights("../jrtr/textures/sampleX/taylor/cos/weights.txt"));
//				
//				samples = "../jrtr/textures/sampleX/taylor/blaze/";
//				extrema = "../jrtr/textures/sampleX/taylor/blaze/extrema.txt";
//				mat.setKValues(loadKValues("../jrtr/textures/sampleX/taylor/blaze/kvalues.txt"));
//				mat.setGlobals(loadglobals("../jrtr/textures/sampleX/taylor/blaze/globals.txt"));
//				mat.setWeights(readWeights("../jrtr/textures/sampleX/taylor/blaze/weights.txt"));
				
			}else if(version == 11){
				// TODO add us
			}
			
			if(version == 10) loadTaylorPatches(samples);
			else loadPatches2(samples, false, true);
			
			mat.setHeightfieldFactors(loadScalingConstants(extrema));
		}

	}
	
	// TODO again check this method - for santy's sake!!!
	private void loadTaylorPatches(String basisPath){
		String path = basisPath;
		String ext = ".bmp";
		int counter = 0;
		
		for(int iter = 0; iter < 31; iter++){
			ext = "AmpRe"+Integer.toString(iter)+extension;
			this.textures[iter] = renderContext.makeTexture();
			mat.setTextureAt(path+ext, textures[iter], iter);
			counter++;
		}
		
		for(int iter = 0; iter < 31; iter++){
			ext = "AmpIm"+Integer.toString(iter)+extension;
			this.textures[counter] = renderContext.makeTexture();
			mat.setTextureAt(path+ext, textures[counter], counter);
			counter++;
		}
	}
	
	
	private float[] loadKValues(String kValues_path){
		List<Float> scalingFactors = new LinkedList<Float>();
		
		try {
			// Open the file that is the first 
			// command line parameter
			FileInputStream fstream = new FileInputStream(kValues_path);
			
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine = "";
			
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
				// Print the content on the console
				float f = Float.valueOf(strLine);
				scalingFactors.add(f);
				//System.out.println(f);
			}
			
			//Close the input stream
			in.close();
		} catch (Exception e){
			//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		
		float[] kValues = new float[scalingFactors.size()];
		int i = 0;
		for(Float value : scalingFactors){
			kValues[i] = value.floatValue();
			i++;
		}
		
		return kValues;
		
	}
	
	
	private float[] loadglobals(String globals_path){
		List<Float> scalingFactors = new LinkedList<Float>();
		
		try {
			// Open the file that is the first 
			// command line parameter
			FileInputStream fstream = new FileInputStream(globals_path);
			
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine = "";
			
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
				// Print the content on the console
				float f = Float.valueOf(strLine);
				scalingFactors.add(f);
				//System.out.println(f);
			}
			
			//Close the input stream
			in.close();
		} catch (Exception e){
			//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		
		float[] globals = new float[scalingFactors.size()];
		int i = 0;
		for(Float value : scalingFactors){
			globals[i] = value.floatValue();
			i++;
		}
		
		return globals;
		
	}
	
	
	private float[] loadScalingConstants(String filepath){
		List<Float> scalingFactors = new LinkedList<Float>();
		
		try {
			// Open the file that is the first 
			// command line parameter
			FileInputStream fstream = new FileInputStream(filepath);
			
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine = "";
			
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
				// Print the content on the console
				float f = Float.valueOf(strLine);
				scalingFactors.add(f);
				//System.out.println(f);
			}
			
			//Close the input stream
			in.close();
		} catch (Exception e){
			//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		
		float[] sfactors = new float[scalingFactors.size()];
		int i = 0;
		for(Float value : scalingFactors){
			sfactors[i] = value.floatValue();
			i++;
		}
		
		return sfactors;
		
	}
	
	private float[] readWeights(String weightPath){
		List<Float> scalingFactors = new LinkedList<Float>();
		
		try {
			// Open the file that is the first 
			// command line parameter
			FileInputStream fstream = new FileInputStream(weightPath);
			
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine = "";
			
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
				// Print the content on the console
				float f = Float.valueOf(strLine);
				scalingFactors.add(f);
				//System.out.println(f);
			}
			
			//Close the input stream
			in.close();
		} catch (Exception e){
			//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		
		float[] weights = new float[scalingFactors.size()];
		int i = 0;
		for(Float value : scalingFactors){
			weights[i] = value.floatValue();
			i++;
		}
		
		return weights;
	}
	

	//String extension = ".bmp";
	private void loadPatches2(String basisPath, boolean Lfolders, boolean bigSample){
		int L = 350;
		int step = 50;
		if(bigSample){
			L = 0;
			step = 1;
		}


		String path = basisPath;

		for(int iter = 0; iter < 492; iter++){
			String ext = "";
			if(iter%82 == 0) L+=step;
			if(Lfolders) ext+=Integer.toString(L)+"/";
			if(iter%82 < 41) ext+="imL"+L;
			else ext+="reL"+L;
			
			int p = iter%41;
			float t = -2.0f + p*0.1f;
			t = (float)Math.round(t * 100000) / 100000;
			if(t==-2.0f || t==-1.0f || t == 0.0f || t == 1.0f || t==2.0f){
				if(t==-2.0f) ext += "w"+"-2"+"BH"+extension;
				else if(t==-1.0f) ext += "w"+"-1"+"BH"+extension;
				else if( t == 0.0f) ext += "w"+"0"+"BH"+extension;
				else if(t == 1.0f)ext += "w"+"1"+"BH"+extension;
				else ext += "w"+"2"+"BH"+extension;
					
			}else ext += "w"+t+"BH"+extension;
			
			this.textures[iter] = renderContext.makeTexture();
			mat.setTextureAt(path+ext, textures[iter], iter);
		}
	}
	
	
	private void setUpLight(){
		Vector3f radiance = new Vector3f(1,1,1); 
		Vector4f lightDirection = new Vector4f(0.0f, 0.0f, -1.0f, 0);  //directional light source
//		lightDirection = new Vector4f(0, 0, 10, 1);  //directional light source
		lightSource1 = new Light(radiance, lightDirection, "source1");
		LightNode diceLightNode = new LightNode(lightSource1, sceneManager.getCamera().getCameraMatrix(), "light source1");
		rootGroup.putChild(diceLightNode);
	}
	
	private void setUpShapes(){
		// care some numbers for segment count are buggy due to rounding error
		// segment count = 45 seems to look enough smooth in order to represent
		// a compact disc shape (i.e. dice)
		// 45, 120, 240, 480
		DiffractionDice4 diffDiceObj = new DiffractionDice4(960, 25, trackDistance);
		
		
//		DiffractionDice6 diffDiceObj = new DiffractionDice6(480, 100, trackDistance);
		
		
		DiffractionPlane2 diffPlaneObj = new DiffractionPlane2(400,10f,1f);
		
		diffDice = new Shape(diffDiceObj.getVertices());
		diffPlane = new Shape(diffPlaneObj.getVertices());
		
		diffDice.setShaderTask(activeShaderTask);
		diffDice.setMaterial(mat);
		
		diffPlane.setShaderTask(activeShaderTask);
		diffPlane.setMaterial(mat);
		
		this.diffDiceIMat = diffDice.getTransformation();
		
		this.diffPlaneIMat = diffPlane.getTransformation();
	}
	
	private void setUpSceneGraph(){
		rootGroup = new TransformGroup("root");
		this.root = rootGroup;
		
		if(isPlane) rootGroup.putChild(new ShapeNode(diffPlane, "plane shape"));
		else rootGroup.putChild(new ShapeNode(diffDice, "dice shape"));
	
	}
	
	private void setUpCamera(boolean isFar){
		float distance = 0.0f;
		if(isPlane){

			distance = 1.1f;

			float aspectRatio = 1.0f;
			float near = 0.0001f;
			float far = 5500.0f;
			float verticalFieldView = 5.0f;
//			verticalFieldView = 120; // viewing angle
			Vector3f up = new Vector3f(0, 1, 0); // camera height
			Point3f look = new Point3f(0, 0, 0); // point camera looks at
			Point3f cop = new Point3f(0, 0, distance); // camera distance
//			cop = new Point3f(0, 0, 1.00f); // camera distance
			sceneManager.getFrustum().setParameter(aspectRatio, near, far, verticalFieldView);
			sceneManager.getCamera().setParameter(cop, look, up);
		}else{
			distance = 1.0f;
			float aspectRatio = 1.0f;
			float near = 0.0001f;
			float far = 5500.0f;
			float verticalFieldView = 30f;
//			verticalFieldView = 120; // viewing angle
			Vector3f up = new Vector3f(0, 1, 0); // camera height
			Point3f look = new Point3f(0, 0, 0); // point camera looks at
			Point3f cop = new Point3f(0, 0, distance); // camera distance
//			cop = new Point3f(0, 0, 1.00f); // camera distance
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
		

		
		
//		Matrix4f ma = new Matrix4f();
//		float[] a = {0.94874644f, 0.25298318f, -0.18942694f, 0.13349566f};
//		float[] b = {-0.049736004f, 0.7114186f, 0.7010073f, 0.32658237f};
//		float[] c = {0.31210417f, -0.6556542f, 0.6875345f, -10.968632f};
//		float[] d = {0.0f, 0.0f, 0.0f, 1.0f};
//		ma.setRow(0, a);
//		ma.setRow(1, b);
//		ma.setRow(2, c);
//		ma.setRow(3, d);
//		
//		sceneManager.getCamera().setCameraMatrix(ma);
		
		
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
		return this.diffDiceIMat;
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
//	
//	public void updateRootLight(LightNode light){
//		ArrayList<INode> nodes = rootGroup.getChildren();
//		for(INode node : nodes){
//			if(node instanceof LightNode){
//				Light tagetLightSource = ((LightNode) node).getLightSource();
//				Vector4f newLightDir = light.getLightSource().getLightDirection();
//				tagetLightSource.setLightDirection(newLightDir);
//				break;
//			}
//		}
//	}
	
	
	
}
