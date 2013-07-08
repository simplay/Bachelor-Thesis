package Managers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import jrtr.RenderContext;
import jrtr.Texture;

import Materials.Material;

public class PreCompDataManager {
	private String extension = ".bmp";
	private Material mat;
	private Texture[] textures = new Texture[2624];
	private RenderContext renderContext;
	
	
	public PreCompDataManager(RenderContext rc, int tasknumber, Material mat){
		this.renderContext = rc;
		this.mat = mat;
		perfromTask(tasknumber);
	}
	
	private void perfromTask(int tasknumber){
		String samples = null;
		String extrema = null;
		
		if(tasknumber == 9 || tasknumber == 10 || tasknumber == 11){
			if(tasknumber==1){

				
			// using bmp rgb	
			}else if(tasknumber == 9){
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
				
			}else if(tasknumber == 10){
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
				
			}else if(tasknumber == 11){
				samples = "../jrtr/textures/sampleX/expTaylor/blaze/";
				extrema = "../jrtr/textures/sampleX/expTaylor/blaze/extrema.txt";
				mat.setGlobals(loadglobals("../jrtr/textures/sampleX/expTaylor/blaze/globals.txt"));
				mat.setWeights(readWeights("../jrtr/textures/sampleX/expTaylor/blaze/weights.txt"));
			}
			
			if(tasknumber == 10 || tasknumber == 11) loadTaylorPatches(samples);
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
}
