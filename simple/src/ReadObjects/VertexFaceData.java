package ReadObjects;

import java.util.ArrayList;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import jrtr.VertexData;

public class VertexFaceData {
	
	private ArrayList<Vertex> vertices;
	private ArrayList<Face> faces;
	
	
	private VertexData vertexData;
	
	private float getNorm(Vector3f v){
		float norm_v = v.x*v.x + v.y*v.y + v.z*v.z;
		return (float) Math.sqrt(norm_v);
	}
	
	
	public VertexFaceData(ArrayList<Vertex> vertices, ArrayList<Face> faces){
		this.vertices = vertices;
		this.faces = faces;
		int facesCount = faces.size();
		
		int verticesCount = vertices.size();
		float vertices_f[] = new float[verticesCount*3];
		float normals_f[] = new float[verticesCount*3];
		float tangents_f[] = new float[verticesCount*3];
		float colors_f[] = new float[verticesCount*3];
		float TextureCoordinates_f[] = new float[verticesCount*2];
		int indices_i[] = new int[facesCount*3];
		
//		float colors
		// postprocessing: tangent and bitangent avg-summation over each face.
		
		
		// see *.obj file in meshlab for convenience
		Vector3f v1_m = new Vector3f(-0.090766f, 0.824358f, -6.04617f);
		Vector3f v2_m = new Vector3f(0.014242f, -1.86301f, -5.82007f);
		
		Vector3f vMain12 = new Vector3f();
		vMain12.sub(v1_m,v2_m);
		float norm_vMain12 = getNorm(vMain12);
		// vector field directed along this main vector
		Vector3f main_v = new Vector3f(vMain12.x/norm_vMain12, vMain12.y/norm_vMain12, vMain12.z/norm_vMain12);
		
		
		float avgEdgeLength = 0.0f;
		int faceCounter = 0;
		for(Face face : this.faces){
			Vector3f pos12 = new Vector3f();
			Vector3f pos13 = new Vector3f();
			Vector3f pos23 = new Vector3f();
			Vector3f faceNormal = new Vector3f();
//			Vector2f coord21 = new Vector2f();
//			Vector2f coord31 = new Vector2f();
			
			Vertex v1 = face.getVertex1();
			Vertex v2 = face.getVertex2();
			Vertex v3 = face.getVertex3();
			
			Vector3f pos1 = new Vector3f(v1.getPosition());
			Vector3f pos2 = new Vector3f(v2.getPosition()); 
			Vector3f pos3 = new Vector3f(v3.getPosition());
			
//			Vector2f coord1 = new Vector2f(v1.getTextureCoordiante());
//			Vector2f coord2 = new Vector2f(v2.getTextureCoordiante());
//			Vector2f coord3 = new Vector2f(v3.getTextureCoordiante());
			
			pos12.sub(pos2, pos1);
			pos13.sub(pos3, pos1);
			pos23.sub(pos3, pos2);
			
			
			avgEdgeLength += getNorm(pos12)+getNorm(pos13)+getNorm(pos23);
			
			faceNormal.cross(pos12, pos23);
			float face_normal = getNorm(faceNormal);
			faceNormal.scale(1.0f/face_normal);
			
			float scale_pro = vMain12.x*faceNormal.x + vMain12.y*faceNormal.y + vMain12.z*faceNormal.z;
			Vector3f vMainOnN = new Vector3f(faceNormal.x, faceNormal.y, faceNormal.z);
			vMainOnN.scale(scale_pro);
			
			Vector3f vMainOnTri = new Vector3f();
			vMainOnTri.sub(vMain12, vMainOnN);
			float norm_vMainOnTri = getNorm(vMainOnTri);
			vMainOnTri.scale(1.0f / norm_vMainOnTri);
			
			
			v1.addVectorToTan1(vMainOnTri);
			v2.addVectorToTan1(vMainOnTri);
			v3.addVectorToTan1(vMainOnTri);
			
			
//			coord21.sub(coord2, coord1);
//			coord31.sub(coord3, coord1);
//			
//			float r = 1.0f / (coord21.x*coord31.y - coord21.y - coord31.x); // determinant
			
			
//			float sdir_x = (coord31.y*pos21.x-coord21.y-pos31.x)*r;
//			float sdir_y = (coord31.y*pos21.y-coord21.y-pos31.y)*r;
//			float sdir_z = (coord31.y*pos21.z-coord21.y-pos31.z)*r;
//			Vector3f sdir = new Vector3f(sdir_x, sdir_y, sdir_z);
//			
//			float tdir_x = (coord21.x*pos31.x-coord31.x-pos21.x)*r;
//			float tdir_y = (coord21.x*pos31.y-coord31.x-pos21.y)*r;
//			float tdir_z = (coord21.x*pos31.z-coord31.x-pos21.z)*r;
//			Vector3f tdir = new Vector3f(tdir_x, tdir_y, tdir_z);
			
//			v1.addVectorToTan1(sdir);
//			v2.addVectorToTan1(sdir);
//			v3.addVectorToTan1(sdir);
//			
//			v1.addVectorToTan2(tdir);
//			v2.addVectorToTan2(tdir);
//			v3.addVectorToTan2(tdir);
//			
//			// merge indices of faces
//			int[] inds = face.getIndices();
//			indices_i[3*faceCounter] = inds[0];
//			indices_i[3*faceCounter+1] = inds[1];
//			indices_i[3*faceCounter+2] = inds[2];
//			faceCounter++;
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
		this.vertexData.addElement(normals_f, VertexData.Semantic.NORMAL, 3);
		this.vertexData.addElement(tangents_f, VertexData.Semantic.TANGENT, 3);
		this.vertexData.addIndices(indices_i);	
	}
	
	public VertexData getVetexData(){
		return this.vertexData;
	}
}
