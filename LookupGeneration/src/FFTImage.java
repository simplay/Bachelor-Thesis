// The MIT License (MIT)
//
// Copyright (c) <2014> <Michael Single>
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;


public class FFTImage {
	
	public int imWidth;
	public int imHeight;
	
	public float [][] real;
	public float [][] imag;
	
	public FFTImage(String fileName, double minR, double maxR, double minI, double maxI) 
	{
		boolean hasData = true;
		
		imWidth = 1;
		imHeight = 1;
		 
		try {
			DataInputStream dataFile = new DataInputStream(new BufferedInputStream(new FileInputStream(fileName)));
			
			if (hasData = dataFile.available()> 0)
				imWidth  = dataFile.readInt();
			if (hasData  =  dataFile.available()> 0)
				imHeight = dataFile.readInt();

			real= new float[imHeight][imWidth];
			imag= new float[imHeight][imWidth];

			if(hasData)
	    	{
				for (int yy = 0; yy < imHeight; yy++) {
					for(int xx = 0; xx < imWidth; xx++)
					{
						real[yy][xx] = dataFile.readFloat();
						real[yy][xx] *= maxR;
						real[yy][xx] += minR;
						
						imag[yy][xx] = dataFile.readFloat();

						imag[yy][xx] *= maxI;
						imag[yy][xx] += minI;
						
						float temp = dataFile.readFloat(); // a waste third value for compability
					}
				}
	    	}
			
			dataFile.close();
			
		} catch (Exception e) 
		{
			System.out.println("failed to load FFT Image File < "+ fileName + " >");
		}
	}

	//Call this function if interpolation is needed
	public double RE(double yy, double xx)
	{
		if (yy < 0 || yy > imHeight - 1 || xx < 0 || xx > imWidth - 1)
		{
			return 0.0f;
		}
		
		int ancY = (int)(Math.floor(yy));
		int ancX = (int)(Math.floor(xx));
		
		if (ancX == imWidth - 1)
			ancX--;
		
		if (ancY == imHeight - 1)
			ancY--;
		
		
		double alphaY = yy - ancY;
		double alphaX = xx - ancX;
		

		// bilinear interpolation
		return alphaY * (alphaX * real[ancY+1][ancX+1] + (1- alphaX) * real[ancY+1][ancX]) +
				(1 - alphaY) * (alphaX * real[ancY][ancX+1] + (1- alphaX) * real[ancY][ancX]);
	}

	//Call this function if interpolation is needed
	public double IM(double yy, double xx)
	{
		if (yy < 0 || yy > imHeight - 1 || xx < 0 || xx > imWidth - 1)
		{
			return 0.0f;
		}
		
		int ancY = (int)(Math.floor(yy));
		int ancX = (int)(Math.floor(xx));
		
		if (ancX == imWidth - 1)
			ancX--;
		
		if (ancY == imHeight - 1)
			ancY--;
		
		
		double alphaY = yy - ancY;
		double alphaX = xx - ancX;
		

		// bilinear interpolation
		return alphaY * (alphaX * imag[ancY+1][ancX+1] + (1- alphaX) * imag[ancY+1][ancX]) +
				(1 - alphaY) * (alphaX * imag[ancY][ancX+1] + (1- alphaX) * imag[ancY][ancX]);
	}
}
