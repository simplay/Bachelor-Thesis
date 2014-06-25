package jrtr;

import java.nio.IntBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.vecmath.*;

import Materials.Material;
import SceneGraph.GraphSceneManager;
import ShaderLogic.ShaderTask;

/**
 * This class implements a {@link RenderContext} (a renderer) using OpenGL version 3 (or later).
 */
public class GLRenderContext implements RenderContext {

	private SceneManagerInterface sceneManager;
	private GL3 gl;
	private GLShader activeShader;


	/**
	 * This constructor is called by {@link GLRenderPanel}.
	 * 
	 * @param drawable 	the OpenGL rendering context. All OpenGL calls are
	 * 					directed to this object.
	 */
	
	
	public GL3 getGL3(){
		return this.gl;
	}
	
	public GLRenderContext(GLAutoDrawable drawable){
		gl = drawable.getGL().getGL3();
	}

		
	/**
	 * Set the scene manager. The scene manager contains the 3D
	 * scene that will be rendered. The scene includes geometry
	 * as well as the camera and viewing frustum.
	 */
	@Override
	public void setSceneManager(SceneManagerInterface sceneManager){
		this.sceneManager = sceneManager;
	}
	
	/**
	 * This method is called by the GLRenderPanel to redraw the 3D scene.
	 * The method traverses the scene using the scene manager and passes
	 * each object to the rendering method.
	 */
	public void display(GLAutoDrawable drawable){
		gl = drawable.getGL().getGL3();
				
		beginFrame();		
		SceneManagerIterator iterator = sceneManager.iterator();	
		while(iterator.hasNext()){
			RenderItem r = iterator.next();
			if(r != null)
				if(r.getShape()!=null) draw(r);
		}
		
		Iterator<Light> lightIterator = sceneManager.lightIterator();
		GraphSceneManager manager = (GraphSceneManager) sceneManager;
		while (lightIterator.hasNext()){
			Light lightSource = lightIterator.next();
			if (lightSource != null) {
				manager.addLight(lightSource);
			}
		}
		endFrame();
		
	}
		
	/**
	 * This method is called at the beginning of each frame, i.e., before
	 * scene drawing starts.
	 */
	private void beginFrame(){
        gl.glClearColor(1.00f, 1.00f, 1.00f, 1.0f);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
	}

	/**
	 * This method is called at the end of each frame, i.e., after
	 * scene drawing is complete.
	 */
	private void endFrame(){	
		gl.glFlush();			
	}
	
	/**
	 * Convert a Matrix4f to a float array in column major ordering,
	 * as used by OpenGL.
	 */
	private float[] matrix4fToFloat16(Matrix4f m){
		float[] f = new float[16];
		for(int i=0; i<4; i++)
			for(int j=0; j<4; j++)
				f[j*4+i] = m.getElement(i,j);
		return f;
	}
	
	/**
	 * The main rendering method.
	 * 
	 * @param renderItem	the object that needs to be drawn
	 */
	private void draw(RenderItem renderItem){
		VertexData vertexData = renderItem.getShape().getVertexData();
		LinkedList<VertexData.VertexElement> vertexElements = vertexData.getElements();
		int indices[] = vertexData.getIndices();

		// Don't draw if there are no indices
		if(indices == null) return;
		

		// Set the material
		
		Material mat = renderItem.getShape().getMaterial();
		
		setMaterial(mat);
		mat.setCOP(sceneManager.getCamera().getCOPbyInv());
//		mat.setCOP(sceneManager.getCamera().getProjectionCenterPoint());
		// Compute the modelview matrix by multiplying the camera matrix and the 
		// transformation matrix of the object
		Matrix4f t = new Matrix4f();
		t.set(sceneManager.getCamera().getCameraMatrix());
		t.mul(renderItem.getT());
		
		Vector4f cameraPosition = new Vector4f();
		sceneManager.getCamera().getCameraMatrix().getColumn(3, cameraPosition);

		gl.glUniform1f(gl.glGetUniformLocation(activeShader.programId(), "thetaI"), (float)( ((GraphSceneManager)sceneManager).getThetaI()*Math.PI/180.0) );
		gl.glUniform1f(gl.glGetUniformLocation(activeShader.programId(), "phiI"), (float)( ((GraphSceneManager)sceneManager).getPhiI()*Math.PI/180.0) );

		// Set modelview and projection matrices in shader
		gl.glUniformMatrix4fv(gl.glGetUniformLocation(activeShader.programId(), "modelM"), 1, false, matrix4fToFloat16(renderItem.getT()), 0);
		gl.glUniformMatrix4fv(gl.glGetUniformLocation(activeShader.programId(), "modelview"), 1, false, matrix4fToFloat16(t), 0);
		gl.glUniformMatrix4fv(gl.glGetUniformLocation(activeShader.programId(), "projection"), 1, false, matrix4fToFloat16(sceneManager.getFrustum().getProjectionMatrix()), 0);
	     		
		
		// TODO: REMEMBER US
		gl.glUniform1f(gl.glGetUniformLocation(activeShader.programId(), "thetaI"), (float)( ((GraphSceneManager)sceneManager).getThetaI()*Math.PI/180.0) );
		gl.glUniform1f(gl.glGetUniformLocation(activeShader.programId(), "phiI"), (float)( ((GraphSceneManager)sceneManager).getPhiI()*Math.PI/180.0));		
		gl.glUniformMatrix4fv(gl.glGetUniformLocation(activeShader.programId(), "modelM"), 1, false, matrix4fToFloat16(renderItem.getT()), 0);
		// Steps to pass vertex data to OpenGL:
		// 1. For all vertex attributes (position, normal, etc.)
			// Copy vertex data into float buffers that can be passed to OpenGL
			// Make/bind vertex buffer objects
			// Tell OpenGL which "in" variable in the shader corresponds to the current attribute
		// 2. Render vertex buffer objects
		// 3. Clean up
		// Note: Of course it would be more efficient to store the vertex buffer objects (VBOs) in a
		// vertex array object (VAO), and simply bind ("load") the VAO each time to render. But this
		// requires a bit more logic in the rendering engine, so we render every time "from scratch".
		        
        // 1. For all vertex attributes, make vertex buffer objects
        IntBuffer vboBuffer = IntBuffer.allocate(vertexElements.size());
        gl.glGenBuffers(vertexElements.size(), vboBuffer);
		ListIterator<VertexData.VertexElement> itr = vertexElements.listIterator(0);
		while(itr.hasNext())
		{
			// Copy vertex data into float buffer
			VertexData.VertexElement e = itr.next();
			int dim = e.getNumberOfComponents();

	        FloatBuffer varr = FloatBuffer.allocate(indices.length * dim);
	        for (int i = 0; i < indices.length; i++) {
	            for (int j = 0; j < dim; j++) {
	                varr.put(e.getData()[dim * indices[i] + j]);
	            }
	        }
	        	        
	        // Make vertex buffer object 
	        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboBuffer.get());
	        // Upload vertex data
	        gl.glBufferData(GL.GL_ARRAY_BUFFER, varr.array().length * 4, FloatBuffer.wrap(varr.array()), GL.GL_DYNAMIC_DRAW);	        
	        
	        // Tell OpenGL which "in" variable in the vertex shader corresponds to the current vertex buffer object
	        // We use our own convention to name the variables, i.e., "position", "normal", "color", "texcoord", or others if necessary
	        
	        //TODO export that functionality.
	        /**
	         * load IN for vert shading
	         */
	        
	        int attribIndex = -1;
	        if(e.getSemantic() == VertexData.Semantic.POSITION) {
	        	attribIndex = gl.glGetAttribLocation(activeShader.programId(), "position");
	        }	        	       
	        else if(e.getSemantic() == VertexData.Semantic.NORMAL) {
	        	attribIndex = gl.glGetAttribLocation(activeShader.programId(), "normal");
	        }
	        else if(e.getSemantic() == VertexData.Semantic.TANGENT) {
	        	attribIndex = gl.glGetAttribLocation(activeShader.programId(), "tangent");
	        }	
	        else if(e.getSemantic() == VertexData.Semantic.COLOR) {
	        	attribIndex  = gl.glGetAttribLocation(activeShader.programId(), "color");
	        }
	        else if(e.getSemantic() == VertexData.Semantic.TEXCOORD) {
	        	attribIndex = gl.glGetAttribLocation(activeShader.programId(), "texcoord");
	        }
        	gl.glVertexAttribPointer(attribIndex, dim, GL.GL_FLOAT, false, 0, 0);
            gl.glEnableVertexAttribArray(attribIndex);        
		}
		
		// 3. Render the vertex buffer objects
		gl.glDrawArrays(GL.GL_TRIANGLES, 0, indices.length);
		
		// 4. Clean up
		gl.glDeleteBuffers(vboBuffer.array().length, vboBuffer.array(), 0);
        
        cleanMaterial(renderItem.getShape().getMaterial());
	}

	/**
	 * Pass the material properties to OpenGL, including textures and shaders.
	 * 
	 * To be implemented in the "Textures and Shading" project.
	 */
	private void setMaterial(Material m){
		m.getShader().use();
		this.activeShader = (GLShader) m.getShader();
		this.setGLTexture(m);
	}
	
	/**
	 * load for each shape its own individual shader task
	 * and invoke the corresponding glut commands necessary
	 * for its shader here.
	 * 
	 * @param m material of this shape
	 */
	private void setGLTexture(Material m){		
		SceneManagerIterator iterator = sceneManager.iterator();
		while(iterator.hasNext()){
			RenderItem r = iterator.next();
			 if (r != null) {
				Shape shape = r.getShape();
				ShaderTask shaderTask = shape.getShaderTask();
				shaderTask.setActiveShader(this.activeShader);
				shaderTask.setGl(this.gl);
				shaderTask.loadMaterialGLShaderUniforms(m);
				ArrayList<Light> lightSources = sceneManager.getLightSources();
				shaderTask.loadLightGLShaderUniforms(lightSources);
			 }
		}
	}
	


	/**
	 * Disable a material.
	 * 
	 * To be implemented in the "Textures and Shading" project.
	 */
	private void cleanMaterial(Material m){
	}

	@Override
	public Shader makeShader(){
		return new GLShader(gl);
	}
	
	@Override
	public Texture makeTexture(){
		return new GLTexture(gl);
	}
	public Texture makeTextureFloat(){
		return new GLTextureFloat(gl);
	}
}
	