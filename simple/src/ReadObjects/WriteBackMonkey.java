package ReadObjects;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import Geometry.VertexDataContainer;

public class WriteBackMonkey {
	private VertexDataContainer vd;
	private String filePath = "../out_files/vertexdata.txt";
	private PrintWriter out;
	
	public WriteBackMonkey(VertexDataContainer vd){
		this.vd = vd;
        FileWriter outFile = null;
		try {
			outFile = new FileWriter(filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		out = new PrintWriter(outFile);
	}
	
	public void writeAll(){
		float[] vert = vd.getVertices();
		for(int t = 0; t < vert.length; t += 3){
			out.println("v" + " " + vert[t] + " " + vert[t+1] + " " + vert[t+2]);
		}
		
		float[] norm = vd.getNormals();
		for(int t = 0; t < norm.length; t += 3){
			out.println("n" + " " + norm[t] + " " + norm[t+1] + " " + norm[t+2]);
		}
		
		float[] tan = vd.getTangents();
		for(int t = 0; t < tan.length; t += 3){
			out.println("t" + " " + tan[t] + " " + tan[t+1] + " " + tan[t+2]);
		}
		
		int[] ind = vd.getIndices();
		for(int t = 0; t < ind.length; t += 3){
			out.println("i" + " " + ind[t] + " " + ind[t+1] + " " + ind[t+2]);
		}
		
		out.close();
	}
}
