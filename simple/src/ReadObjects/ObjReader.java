package ReadObjects;
import java.io.*;
import java.util.ArrayList;

public class ObjReader {
	private VertexFaceData vfData;
	
	public ObjReader(String filename) throws IOException{
		String line = null;
		boolean has_vp = false;
		boolean once = true;
		boolean once2 = true;
		String delimiter = "//";
		long vertexCount = 0;
		long faceCount = 0;
		
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		ArrayList<float[]> texCoords = new ArrayList<float[]>();
		ArrayList<float[]> normals = new ArrayList<float[]>();
		ArrayList<float[]>  parameter_space = new ArrayList<float[]>();
		
		ArrayList<Face> faces = new ArrayList<Face>();
	
		BufferedReader reader = new BufferedReader(new FileReader(filename));		
		
		while((line = reader.readLine()) != null){
			String[] s = line.split("\\s+");
			
			if(s[0].compareTo("v")==0){
				float[] v = new float[3];
				v[0] = Float.valueOf(s[1]).floatValue();
				v[1] = Float.valueOf(s[2]).floatValue();
				v[2] = Float.valueOf(s[3]).floatValue();
				
				vertices.add(new Vertex(vertexCount, v));
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
				
				faceCount++;
				
				// find seperator
				if(once){
					once = false;
					boolean var1 = s[1].contains("//");
					if(!var1) delimiter = "/";
				}
				
				String reg = "\\" + delimiter;
				String[] column_of_row1 = s[1].split(reg);
				String[] column_of_row2 = s[2].split(reg);
				String[] column_of_row3 = s[3].split(reg);

				
				
				
				int i1 = Integer.parseInt(column_of_row1[0])-1;
				int i2 = Integer.parseInt(column_of_row2[0])-1;
				int i3 = Integer.parseInt(column_of_row3[0])-1;
				
				Vertex v1 = vertices.get(i1);
				Vertex v2 = vertices.get(i2);
				Vertex v3 = vertices.get(i3);
				
				Face face = new Face(faceCount, v1, v2, v3);
				
				
				if(once2){
					once2 = true;
					int elCount = column_of_row1.length;
					
					if(elCount==2){
						
					}else if(elCount == 3){
//						i1 = Integer.parseInt(column_of_row1[1])-1;
//						i2 = Integer.parseInt(column_of_row2[1])-1;
//						i3 = Integer.parseInt(column_of_row3[1])-1;
//						face.addTriTextCoord(normals.get(i1));
//						face.addTriTextCoord(normals.get(i2));
//						face.addTriTextCoord(normals.get(i3));
//						
//						i1 = Integer.parseInt(column_of_row1[2])-1;
//						i2 = Integer.parseInt(column_of_row2[2])-1;
//						i3 = Integer.parseInt(column_of_row3[2])-1;
//						face.addTriNormal(normals.get(i1));
//						face.addTriNormal(normals.get(i2));
//						face.addTriNormal(normals.get(i3));
					}
				}
				
				faces.add(face);
				
			}	
		} // end while loop
		long counter = 0;
		for(Vertex v : vertices){
			v.setNormal(normals.get((int) counter));
			v.setTextureCoordinate(texCoords.get((int) counter));
			counter++;
		}
		
		this.vfData = new VertexFaceData(vertices, faces);
	}
	
	public VertexFaceData getVFData(){
		return this.vfData;
	}
}
