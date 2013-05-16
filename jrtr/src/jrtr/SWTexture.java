package jrtr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Manages textures for the software renderer. Not implemented here.
 */
public class SWTexture implements Texture {
    private BufferedImage texture;
    private File file;

    @Override
    public void load(String fileName) throws IOException {
		file = new File(fileName);
		texture = ImageIO.read(file);
    }

    public BufferedImage getTexture() {
    	return texture;
    }
}
