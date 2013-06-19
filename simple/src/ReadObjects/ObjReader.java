package ReadObjects;
import java.io.*;
import java.util.ArrayList;

public class ObjReader {
	String line = null;
	public ObjReader(String filename) throws IOException{
		boolean has_vp = false;
		boolean once = true;
		boolean once2 = true;
		String delimiter = "//";
		int vertexCount = 0;
		
		ArrayList<float[]> vertices = new ArrayList<float[]>();
		ArrayList<float[]> texCoords = new ArrayList<float[]>();
		ArrayList<float[]> normals = new ArrayList<float[]>();
		ArrayList<float[]>  parameter_space = new ArrayList<float[]>();
		
		ArrayList<int[][]> faces = new ArrayList<int[][]>();
	
		BufferedReader reader = new BufferedReader(new FileReader(filename));		
		
		while((line = reader.readLine()) != null){
			String[] s = line.split("\\s+");
			
			if(s[0].compareTo("v")==0){
				float[] v = new float[3];
				v[0] = Float.valueOf(s[1]).floatValue();
				v[1] = Float.valueOf(s[2]).floatValue();
				v[2] = Float.valueOf(s[3]).floatValue();
				vertices.add(v);
				vertexCount++;
				
			}else if(s[0].compareTo("vt")==0){
				float[] t = new float[2];
				t[0] = Float.valueOf(s[1]).floatValue();
				t[1] = Float.valueOf(s[2]).floatValue();
				texCoords.add(t);
				
			}else if(s[0].compareTo("vn")==0){
				float[] n = new float[3];
				n[0] = Float.valueOf(s[1]).floatValue();
				n[1] = Float.valueOf(s[2]).floatValue();
				n[2] = Float.valueOf(s[3]).floatValue();
				normals.add(n);
				
			}else if(s[0].compareTo("vp")==0){
				has_vp = true;
				float[] vp = new float[3];
				vp[0] = Float.valueOf(s[1]).floatValue();
				vp[1] = Float.valueOf(s[2]).floatValue();
				vp[2] = Float.valueOf(s[3]).floatValue();
				parameter_space.add(vp);
				
			}else if(s[0].compareTo("f")==0){
				// find seperator
				if(once){
					once = false;
					boolean var1 = s[1].contains("//");
					if(!var1) delimiter = "/";
				}
				
				for(int k=1; k < 4; k++){
					String reg = "\\" + delimiter;
					String[] column_of_row = s[k].split(reg);
					if(once2){
						once2 = false;
						int elCount = column_of_row.length;
						
						if(elCount==2){
							System.out.println("teapot model");
						}else if(elCount == 3){
							System.out.println("snake model");
						}
					}
				}

			}	
		}
		
	}
}
