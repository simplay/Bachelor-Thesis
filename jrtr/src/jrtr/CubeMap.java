package jrtr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GLException;

import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.awt.AWTTextureData;

/** Creates the Cube map from 6 textures to be given to shaders */
public class CubeMap {
	private static com.jogamp.opengl.util.texture.Texture cubeMapTex = null;
	private static BufferedImage img1, img2, img3, img4, img5, img6;
	private static AWTTextureData data1, data2, data3, data4, data5, data6;
	private GL gl;
	private File file1;
	private File file2;
	private File file3;
	private File file4;
	private File file5;
	private File file6;
	private int counter = 0;
	private boolean flag = true;

	public CubeMap() {
//		
//		if(flag){
//			file1 = new File("../jrtr/textures/cm2/negz.jpg");
//			file2 = new File("../jrtr/textures/cm2/posz.jpg");
//			file3 = new File("../jrtr/textures/cm2/negx.jpg");
//			file4 = new File("../jrtr/textures/cm2/posx.jpg");
//			file5 = new File("../jrtr/textures/cm2/posy.jpg");
//			file6 = new File("../jrtr/textures/cm2/negy.jpg");
//		}else{
//			file1 = new File("../jrtr/textures/cm3/02.jpg");
//			file2 = new File("../jrtr/textures/cm3/01.jpg");
//			file3 = new File("../jrtr/textures/cm3/04.jpg");
//			file4 = new File("../jrtr/textures/cm3/03.jpg");
//			file5 = new File("../jrtr/textures/cm3/05.jpg");
//			file6 = new File("../jrtr/textures/cm3/06.jpg");
//		}
//
//		
//		try {
//			img1 = ImageIO.read(file1);
//			img2 = ImageIO.read(file2);
//			img3 = ImageIO.read(file3);
//			img4 = ImageIO.read(file4);
//			img5 = ImageIO.read(file5);
//			img6 = ImageIO.read(file6);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}	
	}
	
	public void CubeSetUpGLUT(GL gl){
		this.gl = gl;
		this.init(this.gl);
	}

	/** Initializes the cube map data. */
	private void init(GL gl) {
		
		try {
			if(counter == 0){
				cubeMapTex = TextureIO.newTexture(GL.GL_TEXTURE_CUBE_MAP);
				this.counter++;
				data1 = new AWTTextureData(gl.getGLProfile(), 0, 0, false, img1);
				data2 = new AWTTextureData(gl.getGLProfile(), 0, 0, false, img2);
				data3 = new AWTTextureData(gl.getGLProfile(), 0, 0, false, img3);
				data4 = new AWTTextureData(gl.getGLProfile(), 0, 0, false, img4);
				data5 = new AWTTextureData(gl.getGLProfile(), 0, 0, false, img5);
				data6 = new AWTTextureData(gl.getGLProfile(), 0, 0, false, img6);
			}

		} catch (GLException e) {
			System.err.println("couldn not load cube map textures for env map shader");
		}
		if(flag){
			cubeMapTex.updateImage(gl, data3, GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X);
			cubeMapTex.updateImage(gl, data4, GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_X);
			cubeMapTex.updateImage(gl, data6, GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Y);
			cubeMapTex.updateImage(gl, data5, GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y);
			cubeMapTex.updateImage(gl, data1, GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Z);
			cubeMapTex.updateImage(gl, data2, GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z);
		}else{
			cubeMapTex.updateImage(gl, data1, GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X);
			cubeMapTex.updateImage(gl, data2, GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_X);
			cubeMapTex.updateImage(gl, data3, GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Y);
			cubeMapTex.updateImage(gl, data4, GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y);
			cubeMapTex.updateImage(gl, data5, GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Z);
			cubeMapTex.updateImage(gl, data6, GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z);
		}

	}

	public com.jogamp.opengl.util.texture.Texture getCubeMapTex() {
		return cubeMapTex;
	}
}