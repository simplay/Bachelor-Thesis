package jrtr;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.imageio.ImageIO;



import java.awt.image.*;
import java.nio.*;




/**
 * Manages OpenGL textures. This class will be used in the
 * "Texturing and Shading" project.
 */
public class GLTexture implements Texture {
	
	private GL3 gl;			// The OpenGL context that stores the texture
	private IntBuffer id;	// Stores the OpenGL texture identifier
	private int w, h;		// Width and height
	private ByteBuffer bbuffer;
	private int imWidth;
	private int imHeight;
	
	public GLTexture(GL3 gl){
		this.gl = gl;
		id = IntBuffer.allocate(1);	// Make the buffer that will store the texture identifier
	}
	
	public int getImWidth(){
		return this.imWidth;
	}
	
	public int getImHeight(){
		return this.imHeight;
	}
	
	public ByteBuffer getByteBuffer(){
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
		BufferedImage i;
		
		File f = new File(fileName);
		i = ImageIO.read(f);
		
		try {
			methodRGBA(f,fileName);
//			methodRGB(f,fileName);
		} catch (Exception e) {}
		
		gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
		gl.glGenTextures(1, id);
		gl.glBindTexture(GL.GL_TEXTURE_2D, id.get(0));

		w = i.getWidth();
		h = i.getHeight();
		IntBuffer buf = getData(i);
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, w, h, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buf);			
	}
	
	private void methodRGB(File file, String path) throws Exception, IOException, UnsupportedEncodingException {

		BufferedImage image = ImageIO.read(new File(path));
    	
    	byte[] bytes = new byte[4*image.getWidth()*image.getHeight()];
    	int index = 0;
    	
    	this.imWidth = image.getWidth();
    	this.imHeight = image.getHeight();
    	
    	for(int m=0; m < imWidth; m++ ){
    		for(int n=0; n < imHeight; n++ ){
    			int intRGB = image.getRGB(m, n);
    			byte r = (byte)((intRGB)&0xFF);
    			byte g = (byte)((intRGB>>8)&0xFF); // error of source bgr!!!
    			byte b = (byte)((intRGB>>16)&0xFF);
    			byte a = (byte)((intRGB>>24)&0xFF);
    			if(g !=0) System.out.println(g);
    			bytes[4*index+0] = b;
    			bytes[4*index+1] = 0x00;
    			bytes[4*index+2] = 0x00;
    			bytes[4*index+3] = 0x00;
    			index++;
    		}
    	}
 
        ByteBuffer imageBuffer = ByteBuffer.allocateDirect(bytes.length);   
	    imageBuffer.order(ByteOrder.nativeOrder());
	    imageBuffer.put(bytes, 0, bytes.length);
	    imageBuffer.rewind();
	    
	    this.bbuffer = imageBuffer;
    }
	
	
    private void methodRGBA(File file, String path) throws Exception, IOException, UnsupportedEncodingException {

    	BufferedImage image = ImageIO.read(new File(path));
    	
    	byte[] bytes = new byte[4*image.getWidth()*image.getHeight()];
    	int index = 0;
    	
    	this.imWidth = image.getWidth();
    	this.imHeight = image.getHeight();
    	
    	for(int m=0; m < imWidth; m++ ){
    		for(int n=0; n < imHeight; n++ ){
    			int intRGB = image.getRGB(m, n);
    			byte r = (byte)((intRGB)&0xFF);
    			byte g = (byte)((intRGB>>8)&0xFF);
    			byte b = (byte)((intRGB>>16)&0xFF);
    			byte a = (byte)((intRGB>>24)&0xFF);
    			bytes[4*index+0] = b;
    			bytes[4*index+1] = g;
    			bytes[4*index+2] = r;
    			bytes[4*index+3] = a;
    			
    			
    			index++;
    		}
    	}
 
        ByteBuffer imageBuffer = ByteBuffer.allocateDirect(bytes.length);   
	    imageBuffer.order(ByteOrder.nativeOrder());
	    imageBuffer.put(bytes, 0, bytes.length);
	    imageBuffer.rewind();
	    
	    this.bbuffer = imageBuffer;
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
