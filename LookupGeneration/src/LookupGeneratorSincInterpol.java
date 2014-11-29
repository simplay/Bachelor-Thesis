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

import java.io.*;
import java.util.LinkedList;
import java.util.List;


public class LookupGeneratorSincInterpol extends LookupGenerator{
	
	protected void loadFFTValuesFor(double vv, double  uu, int trmCnt){
		for(int i=0; i < trmCnt; ++i){
			tmpRE[i] = 0.0f;
			tmpIM[i] = 0.0f;
		}

		if (vv < -0.5 || uu < -0.5 || vv > 0.5 || uu > 0.5){ // do nothing
			
		} else {
			//here we abuse the encapsulation and modularity to about 
			// recomputation of gaussian weights and indices
			
			int fftWW = this.fftImages[0].imWidth;
			int fftHH = this.fftImages[0].imHeight;

			double uuInTxt = orgU + uu*fftWW;
			double vvInTxt = orgV + vv*fftHH;
			
			int anchorX = (int)(Math.floor(uuInTxt));
			int anchorY = (int)(Math.floor(vvInTxt));
			
			if (anchorX < winW)
				anchorX = winW;
			
			if (anchorY < winW)
				anchorY = winW;
			
			if (anchorX + winW + 1 >  fftWW - 1)
				anchorX = fftWW - 1 - winW - 1;
			
			if (anchorY + winW + 1 >  fftHH - 1)
				anchorY = fftHH - 1 - winW - 1;
			
			
			for (int xx = (anchorX-winW); xx <= (anchorX + winW + 1 ); ++xx) {
				for (int yy = (anchorY - winW); yy <= (anchorY + winW + 1); ++yy){
					double distU = (double)(xx) - uuInTxt;
					double distV = (double)(yy) - vvInTxt;

					distU = distU * distU / varX;
					distV = distV * distV / varY;
					double dist = (float) Math.pow(distU+distV, 0.5);
					double eps = (float) 1e-12;
					double angV = (float) (dist*Math.PI + eps);
					double sincW = (Math.sin(angV)/angV);
	
					for(int i=0; i < trmCnt; ++i){
						tmpRE[i] += (sincW * this.fftImages[i].real[yy][xx]);
						tmpIM[i] += (sincW * this.fftImages[i].imag[yy][xx]);
					}
				}
			}
		}
	}
}
