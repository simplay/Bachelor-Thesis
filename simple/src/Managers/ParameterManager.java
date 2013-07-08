package Managers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import Materials.Material;

public class ParameterManager {
	private Material mat;
	private String parameter_path;
	private String data_path;
	
	public ParameterManager(Material mat, String parameter_path){
		this.mat = mat;
		this.parameter_path = parameter_path;
		setupMaterialParamters();
	}
	
	private void setupMaterialParamters(){
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
		
		this.data_path = paramters.remove(paramters.size()-1);
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
	
	public String getDataPath(){
		return this.data_path;
	}
}
