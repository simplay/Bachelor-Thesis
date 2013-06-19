package ReadObjects;

import java.util.ArrayList;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import jrtr.VertexData;

public class VertexFaceData {
	
	private ArrayList<Vertex> vertices;
	private ArrayList<Face> faces;
	
	
	protected VertexData vertexData;
	
	
	
	
	public VertexFaceData(ArrayList<Vertex> vertices, ArrayList<Face> faces){
		this.vertices = vertices;
		this.faces = faces;
		
		
		// postprocessing: tangent and bitangent avg-summation over each face.
		for(Face face : this.faces){
			Vector3f pos21 = new Vector3f();
			Vector3f pos31 = new Vector3f();
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
			
			pos21.sub(pos2, pos1);
			pos31.sub(pos3, pos1);
			
			coord21.sub(coord2, coord1);
			coord31.sub(coord3, coord1);
			
			float r = 1.0f / (coord21.x*coord31.y - coord21.y - coord31.x); // determinant
			
			
			float sdir_x = (coord31.y*pos21.x-coord21.y-pos31.x)*r;
			float sdir_y = (coord31.y*pos21.y-coord21.y-pos31.y)*r;
			float sdir_z = (coord31.y*pos21.z-coord21.y-pos31.z)*r;
			Vector3f sdir = new Vector3f(sdir_x, sdir_y, sdir_z);
			
			float tdir_x = (coord21.x*pos31.x-coord31.x-pos21.x)*r;
			float tdir_y = (coord21.x*pos31.y-coord31.x-pos21.y)*r;
			float tdir_z = (coord21.x*pos31.z-coord31.x-pos21.z)*r;
			Vector3f tdir = new Vector3f(tdir_x, tdir_y, tdir_z);
			
			v1.addVectorToTan1(sdir);
			v2.addVectorToTan1(sdir);
			v3.addVectorToTan1(sdir);
			
			v1.addVectorToTan2(tdir);
			v2.addVectorToTan2(tdir);
			v3.addVectorToTan2(tdir);
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
		
		int verticesCount = vertices.size();
		float colors[] = new float[verticesCount*3];
		
		for(Vertex vertex : vertices){
			float[] position = vertex.getPosition();
			float[] normal = vertex.getNormal();
			float[] color = {1.0f, 0.0f, 0.0f};
			float[] textureCoordinate = vertex.getTextureCoordiante();
			int[] inds = vertex.getFaceIndices();
		}
		
		
//		this.vertexData = new VertexData(verticesCount);
//		this.vertexData.addElement(colors, VertexData.Semantic.COLOR, 3);
//		this.vertexData.addElement(this.vetices, VertexData.Semantic.POSITION, 3);
//		this.vertexData.addElement(this.textureCoordinates, VertexData.Semantic.TEXCOORD, 2);
//		this.vertexData.addElement(this.normals, VertexData.Semantic.NORMAL, 3);
//		this.vertexData.addIndices(this.indices);
		
		
	}
}
