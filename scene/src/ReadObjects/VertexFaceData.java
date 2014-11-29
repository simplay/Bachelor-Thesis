package ReadObjects;

import java.util.ArrayList;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import Geometry.VertexDataContainer;

import jrtr.VertexData;

public class VertexFaceData {
	
	private ArrayList<Vertex> vertices;
	private ArrayList<Face> faces;
	
	private int facesCount;
	private int verticesCount;
	private float vertices_f[];
	private float normals_f[];
	private float tangents_f[];
	private float colors_f[];
	private float TextureCoordinates_f[];
	private int indices_i[];
	private float avgEdgeLength;
	private VertexData vertexData;
	
	private float getNorm(Vector3f v){
		float norm_v = v.x*v.x + v.y*v.y + v.z*v.z;
		return (float) Math.sqrt(norm_v);
	}
	
	private void computeApproxTangentsByDir(){
		// see *.obj file in meshlab for convenience
				Vector3f v1_m = new Vector3f(-0.090766f, 0.824358f, -6.04617f);
				Vector3f v2_m = new Vector3f(0.014242f, -1.86301f, -5.82007f);
				
				Vector3f vMain12 = new Vector3f();
				vMain12.sub(v1_m,v2_m);
				float norm_vMain12 = getNorm(vMain12);
				// vector field directed along this main vector
				Vector3f main_v = new Vector3f(vMain12.x/norm_vMain12, vMain12.y/norm_vMain12, vMain12.z/norm_vMain12);
				
				
				avgEdgeLength = 0.0f;
				int faceCounter = 0;
				for(Face face : this.faces){
					Vector3f pos12 = new Vector3f();
					Vector3f pos13 = new Vector3f();
					Vector3f pos23 = new Vector3f();
					Vector3f faceNormal = new Vector3f();
					
					Vertex v1 = face.getVertex1();
					Vertex v2 = face.getVertex2();
					Vertex v3 = face.getVertex3();
					
					Vector3f pos1 = new Vector3f(v1.getPosition());
					Vector3f pos2 = new Vector3f(v2.getPosition()); 
					Vector3f pos3 = new Vector3f(v3.getPosition());					

					
					pos12.sub(pos2, pos1);
					pos13.sub(pos3, pos1);
					pos23.sub(pos3, pos2);
				
					
					avgEdgeLength += getNorm(pos12)+getNorm(pos13)+getNorm(pos23);
					
					faceNormal.cross(pos12, pos23);
					float face_normal = getNorm(faceNormal);
					faceNormal.scale(1.0f/face_normal);
					
					float scale_pro = main_v.x*faceNormal.x + main_v.y*faceNormal.y + main_v.z*faceNormal.z;
					Vector3f vMainOnN = new Vector3f(faceNormal.x, faceNormal.y, faceNormal.z);
					vMainOnN.scale(scale_pro);
					
					Vector3f vMainOnTri = new Vector3f();
					vMainOnTri.sub(main_v, vMainOnN);
					float norm_vMainOnTri = getNorm(vMainOnTri);
					vMainOnTri.scale(1.0f / norm_vMainOnTri);
									
					v1.addVectorToTan1(vMainOnTri);
					v2.addVectorToTan1(vMainOnTri);
					v3.addVectorToTan1(vMainOnTri);
												
					// merge indices of faces
					int[] inds = face.getIndices();
					indices_i[3*faceCounter] = inds[0];
					indices_i[3*faceCounter+1] = inds[1];
					indices_i[3*faceCounter+2] = inds[2];
					faceCounter++;
				}
				
				// compute tangent for each vertex
				for(Vertex vertex : this.vertices){
					Vector3f t = new Vector3f(vertex.getTan1());
					float norm_t = getNorm(t);
					t.scale(norm_t);
					vertex.setTangent(t);
				}
				

				// merging process
				int vertexCounter = 0;
				for(Vertex vertex : vertices){
					float[] position = vertex.getPosition();
					float[] normal = vertex.getNormal();
					float[] color = {1.0f, 0.0f, 0.0f};
					float[] textureCoordinate = vertex.getTextureCoordiante();
					float[] tangent = vertex.getTangent();
					
					vertices_f[3*vertexCounter] = position[0];
					vertices_f[3*vertexCounter+1] = position[1];
					vertices_f[3*vertexCounter+2] = position[2];
					
					normals_f[3*vertexCounter] = normal[0];
					normals_f[3*vertexCounter+1] = normal[1];
					normals_f[3*vertexCounter+2] = normal[2];
					
					tangents_f[3*vertexCounter] = tangent[0];
					tangents_f[3*vertexCounter+1] = tangent[1];
					tangents_f[3*vertexCounter+2] = tangent[2];
					
					colors_f[3*vertexCounter] = color[0];
					colors_f[3*vertexCounter+1] = color[1];
					colors_f[3*vertexCounter+2] = color[2];
					
//					TextureCoordinates_f[2*vertexCounter] = textureCoordinate[0];
//					TextureCoordinates_f[2*vertexCounter+1] = textureCoordinate[1];
					
					vertexCounter++;
				}
	}
	
	private void computeAppproxTangentsByTex(){
				int faceCounter = 0;
				for(Face face : this.faces){
					Vector3f pos12 = new Vector3f();
					Vector3f pos13 = new Vector3f();
					Vector3f pos23 = new Vector3f();

					Vector2f coord21 = new Vector2f();
					Vector2f coord31 = new Vector2f();
					
					Vertex v1 = face.getVertex1();
					Vertex v2 = face.getVertex2();
					Vertex v3 = face.getVertex3();
					
					Vector3f pos1 = new Vector3f(v1.getPosition());
					Vector3f pos2 = new Vector3f(v2.getPosition()); 
					Vector3f pos3 = new Vector3f(v3.getPosition());
					
					Vector2f coord1 = new Vector2f(v1.getTextureCoordiante());
					Vector2f coord2 = new Vector2f(v2.getTextureCoordiante());
					Vector2f coord3 = new Vector2f(v3.getTextureCoordiante());
					
					pos12.sub(pos2, pos1);
					pos13.sub(pos3, pos1);
					pos23.sub(pos3, pos2);

					
					coord21.sub(coord2, coord1);
					coord31.sub(coord3, coord1);
					
					float r = 1.0f / (coord21.x*coord31.y - coord21.y - coord31.x); // determinant
					
					
					float sdir_x = (coord31.y*pos12.x-coord21.y-pos13.x)*r;
					float sdir_y = (coord31.y*pos12.y-coord21.y-pos13.y)*r;
					float sdir_z = (coord31.y*pos12.z-coord21.y-pos13.z)*r;
					Vector3f sdir = new Vector3f(sdir_x, sdir_y, sdir_z);
					
					float tdir_x = (coord21.x*pos13.x-coord31.x-pos12.x)*r;
					float tdir_y = (coord21.x*pos13.y-coord31.x-pos12.y)*r;
					float tdir_z = (coord21.x*pos13.z-coord31.x-pos12.z)*r;
					Vector3f tdir = new Vector3f(tdir_x, tdir_y, tdir_z);
					
					v1.addVectorToTan1(sdir);
					v2.addVectorToTan1(sdir);
					v3.addVectorToTan1(sdir);
					
					v1.addVectorToTan2(tdir);
					v2.addVectorToTan2(tdir);
					v3.addVectorToTan2(tdir);
				
					// merge indices of faces
					int[] inds = face.getIndices();
					indices_i[3*faceCounter] = inds[0];
					indices_i[3*faceCounter+1] = inds[1];
					indices_i[3*faceCounter+2] = inds[2];
					faceCounter++;
				}
				
				// compute tangent for each vertex
				for(Vertex vertex : this.vertices){
					Vector3f n = new Vector3f(vertex.getNormal());
					Vector3f t = new Vector3f(vertex.getTan1());
					
					// Gram-Schmidt orthogonalize
					Vector3f tangent = new Vector3f(0.0f, 0.0f, 0.0f);
					tangent.sub(t, n);
					float dot_tn = t.x*n.x + t.y*n.y + t.z+n.z;
					tangent.scale(dot_tn);
					tangent.normalize();
					
					//fix handedness
					Vector3f cross_nt = new Vector3f(0.0f, 0.0f, 0.0f);
					cross_nt.cross(n, t);
					float dot_cross_nt_tangent = cross_nt.x+tangent.x + cross_nt.y+tangent.y + cross_nt.z+tangent.z;
					if(dot_cross_nt_tangent < 0.0f) tangent.scale(-1.0f);
					
					vertex.setTangent(tangent);
				}
	}
	
	
	public VertexFaceData(ArrayList<Vertex> vertices, ArrayList<Face> faces, ArrayList<float[]>  parameter_space, ArrayList<float[]> normals){
		this.vertices = vertices;
		this.faces = faces;
		int facesCount = faces.size();
		
		this.verticesCount = vertices.size();
		this.vertices_f = new float[verticesCount*3];
		this.normals_f = new float[verticesCount*3];
		this.tangents_f = new float[verticesCount*3];
		this.colors_f = new float[verticesCount*3];
		this.TextureCoordinates_f = new float[verticesCount*2];
		this.indices_i = new int[facesCount*3];
		
		computeApproxTangentsByDir();
		//computeAppproxTangentsByTex();
		
		
		// merging process
		int vertexCounter = 0;
		for(Vertex vertex : vertices){
			float[] position = vertex.getPosition();
			float[] normal = vertex.getNormal();
			float[] color = {1.0f, 0.0f, 0.0f};
			float[] textureCoordinate = vertex.getTextureCoordiante();
			float[] tangent = vertex.getTangent();
			
			vertices_f[3*vertexCounter] = position[0];
			vertices_f[3*vertexCounter+1] = position[1];
			vertices_f[3*vertexCounter+2] = position[2];
			
			normals_f[3*vertexCounter] = normal[0];
			normals_f[3*vertexCounter+1] = normal[1];
			normals_f[3*vertexCounter+2] = normal[2];
			
			tangents_f[3*vertexCounter] = tangent[0];
			tangents_f[3*vertexCounter+1] = tangent[1];
			tangents_f[3*vertexCounter+2] = tangent[2];
			
			colors_f[3*vertexCounter] = color[0];
			colors_f[3*vertexCounter+1] = color[1];
			colors_f[3*vertexCounter+2] = color[2];
			
			TextureCoordinates_f[2*vertexCounter] = textureCoordinate[0];
			TextureCoordinates_f[2*vertexCounter+1] = textureCoordinate[1];
			
			vertexCounter++;
		}
		
		// compose vertex data
		this.vertexData = new VertexData(verticesCount);
		this.vertexData.addElement(colors_f, VertexData.Semantic.COLOR, 3);
		this.vertexData.addElement(vertices_f, VertexData.Semantic.POSITION, 3);
		this.vertexData.addElement(TextureCoordinates_f, VertexData.Semantic.TEXCOORD, 2);

		System.out.println(verticesCount + " vertices loaded");
		System.out.println(vertices_f.length/3 + " faces loaded");
		System.out.println(normals_f.length/3 + " normals loaded");
		
		if(parameter_space != null && parameter_space.size() != 0){
			System.out.println("mesh tangents loaded");
			float[] tanParams = new float[3*parameter_space.size()];
			int k = 0;
			for(float[] t  : parameter_space){
				tanParams[3*k] = t[0];
				tanParams[3*k+1] = t[1];
				tanParams[3*k+2] = t[2];
				k++;
				          
			}
			System.out.println(tanParams.length/3 + " tangents loaded");
			this.vertexData.addElement(tanParams, VertexData.Semantic.TANGENT, 3);
		}else{
			System.out.println("dummy tangents loaded");
			System.out.println(tangents_f.length/3 + " tangents loaded");
			this.vertexData.addElement(tangents_f, VertexData.Semantic.TANGENT, 3);
		}
		
		if(normals != null && normals.size() != 0){
			System.out.println("mesh normals loaded");
			float[] normalsMesh = new float[3*normals.size()];
			int k = 0;
			for(float[] t  : normals){
				normalsMesh[3*k] = t[0];
				normalsMesh[3*k+1] = t[1];
				normalsMesh[3*k+2] = t[2];
				k++;
				          
			}
			System.out.println(normalsMesh.length/3 + " normals loaded");
			this.vertexData.addElement(normalsMesh, VertexData.Semantic.NORMAL, 3);
			
		}else{
			System.out.println("dummy normals loaded");
			System.out.println(tangents_f.length/3 + " tangents loaded");
			this.vertexData.addElement(normals_f, VertexData.Semantic.NORMAL, 3);
		}
		
		
		this.vertexData.addIndices(indices_i);	
		
		VertexDataContainer data = new VertexDataContainer(vertices_f, normals_f, tangents_f, colors_f, TextureCoordinates_f, indices_i, avgEdgeLength);
		WriteBackMonkey monkey = new WriteBackMonkey(data);
//		monkey.writeAll();
	}
	
	public VertexData getVetexData(){
		return this.vertexData;
	}
}
