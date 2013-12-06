package Setup.Managers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import jrtr.RenderContext;
import jrtr.Texture;

import Constants.ShaderTaskNr;
import Materials.Material;
import Setup.Constants.PatchDataPaths;

public class PreCompDataManager {
	private String extension = ".bmp";
	private Material mat;
	private Texture[] textures = new Texture[2624];
	private RenderContext renderContext;
	private PatchDataPathsManager pdpm;
	private int layerCount = 39;
	private boolean useTxtFiles = true;
	
	
	public PreCompDataManager(RenderContext rc, ShaderTaskNr tasknumber, String patchName, Material mat){
		this.renderContext = rc;
		this.mat = mat;
		pdpm = new PatchDataPathsManager();
		perfromTask(tasknumber, patchName);
	}
	
	private void perfromTask(ShaderTaskNr tasknumber, String patchName){
		PatchDataPaths patchPaths = pdpm.getPathsByIdentifiers(tasknumber, patchName);
//		mat.setKValues(loadKValues(patchPaths.getKValuesPath())); 
//		mat.setGlobals(loadglobals(patchPaths.getGlobalsPath()));
		mat.setWeights(readWeights(patchPaths.getWeightsPath()));
		loadPatches(tasknumber.getValue(), patchPaths.getSamplesPath());
		mat.setHeightfieldFactors(loadScalingConstants(patchPaths.getExtremaPath()));
		setupMaterialParamters(patchPaths.getParamtersPath());
	}
	
	private void loadPatches(int tasknumber, String samples){
		if(tasknumber == 10 
				|| tasknumber == 11 
				|| tasknumber == 12 
				|| tasknumber == 13 
				|| tasknumber == 14
				|| tasknumber == 15){
			loadCompositeTaylorPatches(samples);
		}
		else loadPatches2(samples, false, true);
	}	
		
	
	private void loadCompositeTaylorPatches(String basisPath){
		String path = basisPath;
		String ext = "";	
		for(int iter = 0; iter < layerCount; iter++){
			if(useTxtFiles){	
				ext = "AmpReIm"+Integer.toString(iter)+".txt";
				this.textures[iter] = renderContext.makeTextureFloat();
			}else{
				ext = "AmpReIm"+Integer.toString(iter)+".bmp";
				this.textures[iter] = renderContext.makeTexture();
			}
			
			mat.setTextureAt(path+ext, textures[iter], iter);
		}
	}
	
	private void setupMaterialParamters(String parameter_path){
		List<String> paramters = new LinkedList<String>();
		
		try{
			FileInputStream fstream = new FileInputStream(parameter_path);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine = "";
			
			//Read File Line By Line
			while ((strLine = br.readLine()) != null){
				paramters.add(strLine);
			}
			in.close();
		}catch (Exception e){
			System.err.println("Error: " + e.getMessage());
		}
		
		paramters.remove(paramters.size()-1);
		mat.setLambdaMin((new Float(paramters.get(0))).floatValue());
		mat.setLambdaMax((new Float(paramters.get(1))).floatValue());
		int setps = (int) (new Float(paramters.get(2)).floatValue());
		int  dimN= (int) (new Float(paramters.get(3)).floatValue());
		int  dimSmall= (int) (new Float(paramters.get(4)).floatValue());
		int  dimDiff= (int) (new Float(paramters.get(5)).floatValue());
		int rep_nn = (int) (new Float(paramters.get(6)).floatValue());
		mat.setStepCount(setps);
		mat.setDimN(dimN);
		mat.setSmall(dimSmall);
		mat.setDimDiff(dimDiff);
		mat.setRepNN(rep_nn);
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
			List<Double> scalingFactors = new LinkedList<Double>();
			
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
					double f = Double.valueOf(strLine);
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
			for(Double value : scalingFactors){
				sfactors[i] = (float)value.floatValue();
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
}
