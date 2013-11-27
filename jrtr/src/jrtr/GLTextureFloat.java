package jrtr;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.DataInputStream;
import java.io.BufferedInputStream;

import javax.imageio.ImageIO;
import java.util.Scanner;


import java.awt.image.*;
import java.nio.*;


/**
 * Manages OpenGL textures. This class will be used in the
 * "Texturing and Shading" project.
 */
public class GLTextureFloat implements Texture {
	
	private GL3 gl;			// The OpenGL context that stores the texture
	private IntBuffer id;	// Stores the OpenGL texture identifier
	private int w, h;		// Width and height
	private FloatBuffer bbuffer;
	private int imWidth;
	private int imHeight;
	
	public GLTextureFloat(GL3 gl){
		this.gl = gl;
		id = IntBuffer.allocate(1);	// Make the buffer that will store the texture identifier
	}
	
	public int getImWidth(){
		return this.imWidth;
	}
	
	public int getImHeight(){
		return this.imHeight;
	}
	
	public FloatBuffer getByteBuffer(){
		return this.bbuffer;
	}
	
	public IntBuffer getImageBuffer(){
		return this.id;
	}

	/**
	 * Load the texture from an image file.
	 */
	@Override
	public void load(String fileName) throws IOException
	{
		readDataInBinarySinglePrecision(fileName);
		
		gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
		gl.glGenTextures(1, id);
		gl.glBindTexture(GL.GL_TEXTURE_2D, id.get(0));

		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB32F, imWidth , imHeight, 0, GL.GL_RGB, GL.GL_FLOAT, bbuffer);			
	}
	
	private void readDataInBinarySinglePrecision(String fileName) 
	{
		boolean hasData = true;
		
		imWidth = 1;
		imHeight = 1;
		
		try {

			int idx = 0;
			DataInputStream dataFile = new DataInputStream(new BufferedInputStream(new FileInputStream(fileName)));
			
			if (hasData = dataFile.available()> 0)
				imWidth  = dataFile.readInt();
			if (hasData  =  dataFile.available()> 0)
				imHeight = dataFile.readInt();

    		float[] data = new float[3*imWidth*imHeight];

			if(hasData)
	    	{
				for (int i = 0; i < imWidth; i++) {
					for(int j = 0; j < imHeight; j++)
					{
						for(int k=0; k <3; ++k)
						//if(hasData = dataFile.readInt();)
						{
							data[idx] =  dataFile.readFloat();
							idx++;
						} 
						//else {
						//	break;
						//}
						//if(!hasData)
							//break;
					}
					//if(!hasData)
						//break;
				}
	    	}
	        FloatBuffer imageBuffer = FloatBuffer.allocate(data.length);   
		    //imageBuffer.order(ByteOrder.nativeOrder());
		    imageBuffer.put(data, 0, 3*imWidth*imHeight);
		    imageBuffer.rewind();
		    
		    this.bbuffer = imageBuffer;
	    	
	//		methodRGB(f,fileName);
		} catch (Exception e) {
		}
	}
	private void readDataInASCII(String fileName) 
	{
		boolean hasData = true;
		
		imWidth = 1;
		imHeight = 1;
		
		try {

			int idx = 0;
			
			Scanner scanFile = new Scanner(new File(fileName));
			
			if (hasData  = scanFile.hasNextInt())
				imWidth  = scanFile.nextInt();
			if (hasData  = scanFile.hasNextInt())
				imHeight = scanFile.nextInt();

    		float[] data = new float[3*imWidth*imHeight];

			if(hasData)
	    	{
				for (int i = 0; i < imWidth; i++) {
					for(int j = 0; j < imHeight; j++)
					{
						for(int k=0; k <3; ++k)
						if(hasData = scanFile.hasNextFloat())
						{
							data[idx] =  scanFile.nextFloat();
							idx++;
						} else {
							break;
						}
						if(!hasData)
							break;
					}
					if(!hasData)
						break;
				}
	    	}
	        FloatBuffer imageBuffer = FloatBuffer.allocate(data.length);   
		    //imageBuffer.order(ByteOrder.nativeOrder());
		    imageBuffer.put(data, 0, idx);
		    imageBuffer.rewind();
		    
		    this.bbuffer = imageBuffer;
	    	
	//		methodRGB(f,fileName);
		} catch (Exception e) {
		}
	}


	
	public int getId()
	{
		return id.get(0);
	}
	
	/**
	 * Copy the image data into a buffer that can be passed to OpenGL.
	 */
	public IntBuffer getData(BufferedImage img)
	{
		IntBuffer buf = IntBuffer.allocate(img.getWidth()*img.getHeight());
		//ByteBuffer b = ByteBuffer.allocate(buf.capacity()*4);

		for(int i=0; i<img.getHeight(); i++)
		{
			for(int j=0; j<img.getWidth(); j++)
			{
				// We need to shuffle the RGB values to pass them correctly to OpenGL. 
				int in = img.getRGB(j,i);
				int out = ((in & 0x000000FF) << 16) | (in & 0x0000FF00) | ((in & 0x00FF0000) >> 16);
				buf.put((img.getHeight()-i-1)*img.getWidth()+j, out);
			}
		}
		//b.asIntBuffer().put(buf);
		return buf;
	}
}
