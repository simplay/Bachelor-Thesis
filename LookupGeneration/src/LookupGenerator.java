import java.io.*;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;


public class LookupGenerator {

	
	protected static String ipPath = "E:\\Projects\\DiffractionShader\\Lookups\\fftElaphe65Microns\\";
	protected static String opPath = "E:\\Projects\\DiffractionShader\\op\\";
	//protected static String opTag  = "aa";
	protected static int uvWid = 101;
	protected static int uvHgh = 101;

	protected FFTImage[] fftImages;

	protected int imgCnt;
	protected double dH;

	final static protected double LMIN = (double)380e-3; // for 
	static protected double LMAX = (double)780e-3;
	//final static protected double LINC = (double)  5e-9;
	final static protected int LCNT = 81;
	
	
	protected int lambdaCnt;
	// color functions premultiplied with the spectral profile
	// And normalized to sum up to 1 to correspond to white color in D65
	// max value in the spectral profile is 1
	protected double[][] currXYZ;
	//protected double[] currY;
	//protected double[] currZ;
	protected double[] currS;
	protected double[] currLambda;
	
	// place folders to improve speed
	protected double[] tmpRE;
	protected double[] tmpIM;
	
	protected double[] facto;
	
	// 3x TermCnt array of Extremas;
	protected double[][] minTabVal;
	protected double[][] maxTabVal;
	
	protected double varX, varY;
	protected int orgU, orgV, winW;
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub
		LookupGenerator lGen = new LookupGenerator();
		
		if (args.length > 0)
			ipPath = args[0];
		
		if (args.length > 1)
			opPath = args[1];
		
		if (args.length > 2)
			uvWid = Integer.parseInt(args[2]);
		
		uvHgh = uvWid;
		
		double lInc = (double)5e-3; // in microns

		if (args.length > 3)
			lInc = (double)(Integer.parseInt(args[3])*1e-3);
		else
		{
			System.out.println("Not continuing as all the parametes are not provided");
			
			return ;
		}
		
		

		List<Double> scalingFactors = lGen.readScalingFactors(ipPath+"extrema.txt");
		lGen.setImgCnt(scalingFactors.size()/4/2);  
		double[] minR = lGen.getMinReal(scalingFactors);
		double[] maxR = lGen.getMaxReal(scalingFactors);
		double[] minI = lGen.getMinImag(scalingFactors);
		double[] maxI = lGen.getMaxImag(scalingFactors);
		lGen.setDH(scalingFactors);
		lGen.initFFT();
		lGen.loadFFTImages(minR, maxR, minI, maxI);

		lGen.setUpExpoAndFftOrigin();
		lGen.initFactos();
		
		//double lInc = (double)5e-3; // in microns
		lGen.prepareColorTables(lInc);
		lGen.genLookups();
	}
	
	protected double[][] prepareUU(int H, int W, double pVal)
	{
		double[][] uu = new double[H][W];
		double wInc = 4.0f/(W-1);
		int W0 = (W-1)/2; // minus because of zero based indexing
		
		
		for (int wIdx = 0; wIdx < W; ++wIdx)
		{
			for(int hIdx = 0; hIdx < H;++hIdx)
			{
				uu[hIdx][wIdx] = 2.0f * (double)Math.pow( wInc*(wIdx-W0)/2.0, pVal); 
			}
		}
		
		return uu;
	}
	
	
	protected double[][] prepareVV(int H, int W, double pVal)
	{
		double[][] vv = new double[H][W];
		double hInc = 4.0f/(W-1);
		int H0 = (H-1)/2; // minus because of zero based indexing
		
		
		for (int hIdx = 0; hIdx < H; ++hIdx)
		{
			for(int wIdx = 0; wIdx < W;++wIdx)
			{
				vv[hIdx][wIdx] = 2.0f * (double)Math.pow( hInc*(hIdx-H0)/2.0, pVal); 
			}
		}
		
		return vv;
	}
	
	protected void initLupToZeros(double[][] lup, int H, int W)
	{
		for (int hIdx = 0; hIdx < H; ++hIdx)
		{
			for(int wIdx = 0; wIdx < W;++wIdx)
			{
				lup[hIdx][wIdx] = 0.0f; 
			}
		}
	}
	
	protected void genLookups()
	{
		double pow = 5.0f;
		
		double[][] uu = prepareUU(uvHgh, uvWid, pow);
		double[][] vv = prepareVV(uvHgh, uvWid, pow);
		
		// lookup for 3 channels
		double[][][] lup = new double[3][uvHgh][uvWid];
		//double[][] lupY = new double[uvHgh][uvWid];
		//double[][] lupZ = new double[uvHgh][uvWid];
		PrintStream opFile = null;
		try {
			 opFile = new PrintStream(new BufferedOutputStream(new FileOutputStream(opPath+"extrema.txt")));
			System.out.println("Opened Extrema File successfully");
		} catch (Exception e) 
		{
			System.out.println("failed to Open Extrema File");
		}		
		
		double dHLoc = this.dH * 1e-6;
		// process each pth Component
		for (int p=0; p <= 2*(this.imgCnt-1);++p)
		{ 
			initLupToZeros(lup[0], uvHgh, uvWid);
			initLupToZeros(lup[1], uvHgh, uvWid);
			initLupToZeros(lup[2], uvHgh, uvWid);
			// process each color component
			for (int c=0; c < 3; ++c)
			{
				double[][] currLup = lup[c];
				
				for(int vIdx = 0; vIdx <  uvHgh; ++vIdx)
					for(int uIdx = 0; uIdx <  uvWid; ++uIdx)
						currLup[vIdx][uIdx] = getIpValueAt(vv[vIdx][uIdx], uu[vIdx][uIdx], p, c);
			}
		
			rescaleLup(lup, p, uvHgh, uvWid);
			writeLup(lup, opPath+"AmpReIm"+Integer.toString(p)+".txt", uvHgh, uvWid);
			
			try {
				for(int c = 0;c < 3; ++c)
					opFile.println(minTabVal[c][p]);
				
				opFile.println(dHLoc);
				
				for(int c = 0;c < 3; ++c)
					opFile.println(maxTabVal[c][p]);
				
				opFile.println(dHLoc);
				
				
				opFile.flush();
			} catch (Exception e) 
			{
				System.out.println("failed to write into  Extrema File");
			}	
		}
		opFile.close();
		
		writeExtrema(opPath+"extrema2.txt");
	}
	
	protected void writeExtrema(String fileName)
	{
		double dHLoc = this.dH * 1e-6;
		
		try {
			PrintStream opFile = new PrintStream(new BufferedOutputStream(new FileOutputStream(fileName)));
			
			for(int i=0; i < 2 * imgCnt - 1; ++i)
			{
				for(int c = 0;c < 3; ++c)
					opFile.println(minTabVal[c][i]);
				
				opFile.println(dHLoc);
				
				for(int c = 0;c < 3; ++c)
					opFile.println(maxTabVal[c][i]);
				
				opFile.println(dHLoc);
			}
			
			opFile.close();
		} catch (Exception e) 
		{
			System.out.println("failed to write Extrema < "+ fileName + " >");
		}		
		System.out.println("Finished writing Extrema");		
		
	}
	
	protected void rescaleLup(double[][][] lup, int p, int HH, int WW)
	{
		double minV, maxV;
		
		for(int c = 0; c < 3;c++)
		{
			minV = lup[c][0][0];
			maxV = lup[c][0][0];
			
			for (int vIdx = 0; vIdx < HH; ++vIdx)
			{
				for (int uIdx = 0; uIdx < WW; ++uIdx)
				{
					if (lup[c][vIdx][uIdx] < minV)
						minV = lup[c][vIdx][uIdx];
					
					if (lup[c][vIdx][uIdx] > maxV)
						maxV = lup[c][vIdx][uIdx];
				}
			}
			
			if (Math.abs(minV) < 1E-300)
				minV = 0.0f;
			
			if (Math.abs(maxV) < 1E-300)
				maxV = 1.0f;
			
			minTabVal[c][p] = minV;
			maxTabVal[c][p] = maxV - minV;
		}
		
		for(int c = 0; c < 3;c++)
		{
			minV = minTabVal[c][p];
			maxV = maxTabVal[c][p];
			
			for (int vIdx = 0; vIdx < HH; ++vIdx)
			{
				for (int uIdx = 0; uIdx < WW; ++uIdx)
				{
					lup[c][vIdx][uIdx] -= minV;
					lup[c][vIdx][uIdx] /= maxV;
				}
			}
		}
	}
	//lup is a 3xNxM lookup table
	protected void writeLup(double[][][] lup, String fileName, int HH, int WW)
	{
		System.out.println("Writing Output lookup <" +fileName + "> ");
		try {
			DataOutputStream dataFile = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
			
			// write width first and then the height and then
			// all three values in XYZ order
			// going from left to right, bottom to top in uu, vv space.
			// 
			dataFile.writeInt(WW);
			dataFile.writeInt(HH);
			
			// note that writing is in reverse order just to be compatible with previous lookups
			for (int yy = HH-1; yy >= 0; yy--) {
				for(int xx = 0; xx < WW; xx++)
				{
					for (int c=0; c < 3; c++)
					{
						dataFile.writeFloat((float)lup[c][yy][xx]);
					}
				}
			}
			
			dataFile.close();
		} catch (Exception e) 
		{
			System.out.println("failed to write lookup < "+ fileName + " >");
		}		
		System.out.println("Finished writing");
	}
	
	protected double getIpValueAt(double vv0, double uu0, int p, int c)
	{
		double sumOverLambda = 0.0f;
		//double totalIM = 0.0f;
		int N = this.imgCnt - 1;
		for (int lIdx = 0; lIdx < this.lambdaCnt; ++lIdx)
		{
			// for each lambda do this

			// we are abusing encapsulation and modularity here by keeping FFT data public
			// and using if for calculating indices 
			// wait, lets try passing indices between -0.5 and 0.5 instead
			double uu = (uu0 * dH)/ currLambda[lIdx];
			double vv = (vv0 * dH)/ currLambda[lIdx];
			
			// following call loads FFT values at given indices in temp buffers ie tmpRE, tmpIM
			// get indices in pixel units
			loadFFTValuesFor(vv, uu, Math.min( p+1, imgCnt));
			
			double sumOverP = 0.0f;
			for (int n = Math.max(0, p - N); n <= Math.min(p, N); ++n)
			{
				int m = p - n;
				
				if (m > N)
					continue; // no proper m
				
				sumOverP += (tmpRE[n]*tmpRE[m] + tmpIM[n]*tmpIM[m])/ (facto[n]*facto[m]);
			}
			
			
			sumOverLambda += (double) (Math.pow(2.0 * Math.PI,p) * (sumOverP * currXYZ[c][lIdx] / Math.pow( currLambda[lIdx], p)));
		}
		
		return sumOverLambda;
		
	}
	
	protected void setUpExpoAndFftOrigin()
	{
		// assuming FFT images are already read
		
		int fftWW = this.fftImages[0].imWidth;
		int fftHH = this.fftImages[0].imHeight;
		
		double sigSpatial = 65/4.0f; // in microns again!
		//double sigSpatial = 15e-6/4.0f;
		
		// temporary sigma
		double sigTemp;
		
		sigTemp = (double) (0.5 / Math.PI);
		//sigTemp = 1.0;
		
		sigTemp = sigTemp /sigSpatial;
		//sigTemp = 1.0f / sigSpatial;
		
		//sigTemp = sigTemp / GetLightNormalCos();
		
		sigTemp = sigTemp * dH;
		
		varX = sigTemp * sigTemp * fftWW * fftWW; 
		varY = sigTemp * sigTemp * fftHH * fftHH;
		
		if (fftWW % 2 == 0)
			orgU = fftWW / 2; // -2 dur to rotational lochay
		else
			orgU = (fftWW - 1) / 2;
			
		if (fftHH % 2 == 0)
			orgV = (fftHH)/2;
		else
			orgV = (fftHH - 1)/2;
				
	
		winW = (int)Math.ceil(sigTemp*Math.max(fftWW, fftHH)* 4.0);

	}
	
	
	// vv and uu range from -0.5 to 0.5 and trmCnt <= imgCnt
	protected void loadFFTValuesFor(double vv, double  uu, int trmCnt)
	{
		for(int i=0; i < trmCnt; ++i)
		{
			tmpRE[i] = 0.0f;
			tmpIM[i] = 0.0f;
		}

		if (vv < -0.5 || uu < -0.5 || vv > 0.5 || uu > 0.5)
		{ // do nothing
			
		} else 
		{
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
			
			
			for (int xx = (anchorX-winW); xx <= (anchorX + winW + 1 ); ++xx) 
			{
				for (int yy = (anchorY - winW); yy <= (anchorY + winW + 1); ++yy)
				{
					double distU = (double)(xx) - uuInTxt;
					double distV = (double)(yy) - vvInTxt;

					distU = distU * distU / varX;
					distV = distV * distV / varY;

					double gaussW = (double)Math.exp((-distU - distV)/2.0f);
					
					for(int i=0; i < trmCnt; ++i)
					{
						tmpRE[i] += (gaussW * this.fftImages[i].real[yy][xx]);
						tmpIM[i] += (gaussW * this.fftImages[i].imag[yy][xx]);
					}
				}
			}
			
		}
		
	}
	
	
	
	protected void prepareColorTables(double lInc)
	{
		lambdaCnt = (int)Math.ceil((LMAX - LMIN)/lInc) + 1;
		
		// update lInc for exact division
		lInc = (LMAX - LMIN)/(lambdaCnt - 1);
		
		currXYZ = new double[3][lambdaCnt];
		//currY = new double[lambdaCnt];
		//currZ = new double[lambdaCnt];
		currS = new double[lambdaCnt];
		currLambda = new double[lambdaCnt];
	
		double xNorm = 0;
		double yNorm = 0;
		double zNorm = 0;
		
		for(int i=0; i< lambdaCnt-1; ++i)
		{
			double lVal = lInc*i / ((LMAX - LMIN));
			currS[i] = interpol(specI, lVal);

			currXYZ[0][i] = interpol(clrX, lVal)*currS[i];
			currXYZ[1][i] = interpol(clrY, lVal)*currS[i];
			currXYZ[2][i] = interpol(clrZ, lVal)*currS[i];
			currLambda[i] = LMIN + lInc*i;
			
			xNorm += currXYZ[0][i];
			yNorm += currXYZ[1][i];
			zNorm += currXYZ[2][i];
		}
		currS[lambdaCnt-1] = interpol(specI,1.0f);
		// special setting for the last element to be exactly at the LMAX
		currXYZ[0][lambdaCnt-1] = interpol(clrX, 1.0f)*currS[lambdaCnt-1];
		currXYZ[1][lambdaCnt-1] = interpol(clrY, 1.0f)*currS[lambdaCnt-1];
		currXYZ[2][lambdaCnt-1] = interpol(clrZ, 1.0f)*currS[lambdaCnt-1];

		currLambda[lambdaCnt-1] = LMAX;

		xNorm += currXYZ[0][lambdaCnt-1];
		yNorm += currXYZ[1][lambdaCnt-1];
		zNorm += currXYZ[2][lambdaCnt-1];
		
		// note we are normalizing all the elements now
		for(int i=0; i < lambdaCnt; ++i)
		{
			currXYZ[0][i] /= xNorm;
			currXYZ[1][i] /= yNorm;
			currXYZ[2][i] /= zNorm;
		}
	}
	
	// index is between 0 and 1
	protected double interpol(double[] fn, double idx)
	{
		if (idx < 0.0f || idx > 1.0f)
		{
			System.out.println("ERROR in preparing Lookup Tables for the color function");
			return 0.0f;
		}
		
		int i = (int)Math.floor(idx*(LCNT-1));
		if (i == LCNT - 1)
			i = LCNT - 2; // to ensure anchoring at the lower end
		
		double alpha = idx*(LCNT-1) - i;
		
		return (double)(fn[i+1]*alpha + (1-alpha)*fn[i]);
	}

	
	protected void loadFFTImages(double[] minR, double[] maxR, double[] minI, double[] maxI)
	{
		String ext = "";	
		for(int i = 0; i < imgCnt; i++)
		{
			ext = "AmpReIm"+Integer.toString(i)+".txt";
			
			fftImages[i] = new FFTImage(ipPath + ext, minR[i], maxR[i], minI[i], maxI[i]);
		}		
	}
	
	
	protected void initFFT()
	{
		fftImages = new FFTImage[imgCnt];
		
		tmpRE = new double[imgCnt];
		tmpIM = new double[imgCnt];
		
		minTabVal = new double[3][imgCnt*2 -1];
		maxTabVal = new double[3][imgCnt*2 -1];
		
	}
	
	protected void initFactos()
	{
		facto = new double[imgCnt];
		
		facto[0] = 1;
		
		for (int i=1; i < imgCnt; ++i)
		{
			facto[i] = facto[i-1]*i;
		}
	}
	
	protected void setImgCnt(int i)
	{
		this.imgCnt = i;
	}
	
	//From Michael in parts
	// Returns Nx4 array of limits
	protected List<Double>  readScalingFactors(String fileName)
	{
		List<Double> scalingFactors = new LinkedList<Double>();
		
		try {
			// Open the file that is the first 
			// command line parameter
			FileInputStream fstream = new FileInputStream(fileName);
			
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine = "";
			
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
				// Print the content on the console
				double f = Float.valueOf(strLine);
				scalingFactors.add(f);
			}
			
			in.close();
		} catch (Exception e){
			//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		
		return scalingFactors;	
	}

	protected double[] getMinReal(List<Double>  scalingFactors)
	{
		double[] minReal = new double[scalingFactors.size()/4/2];
		int i   = 0;
		int j   = 0;
		int idx = 0;
		
		for(Double value : scalingFactors)
		{
			if(i%2 == 0 && j == 0) 
			{
				minReal[idx] = value.doubleValue();
				idx++;
			}
			
			j++;
			if (j == 4)
			{
				j = 0;
				i++;
			}
		}
		
		
		return minReal;
	}

	protected double[] getMaxReal(List<Double>  scalingFactors)
	{
		double[] maxReal = new double[scalingFactors.size()/4/2];
		int i   = 0;
		int j   = 0;
		int idx = 0;
		
		for(Double value : scalingFactors)
		{
			if(i%2 == 1 && j == 0) 
			{
				maxReal[idx] = value.doubleValue();
				idx++;
			}
			
			j++;
			if (j == 4)
			{
				j = 0;
				i++;
			}
		}
		return maxReal;
	}

	protected double[] getMinImag(List<Double>  scalingFactors)
	{
		double[] minImag = new double[scalingFactors.size()/4/2];
		int i   = 0;
		int j   = 0;
		int idx = 0;
		
		for(Double value : scalingFactors)
		{
			if(i%2 == 0 && j == 1) 
			{
				minImag[idx] = value.doubleValue();
				idx++;
			}
			
			j++;
			if (j == 4)
			{
				j = 0;
				i++;
			}
		}
		return minImag;
	}

	protected double[] getMaxImag(List<Double>  scalingFactors)
	{
		double[] maxImag = new double[scalingFactors.size()/4/2];
		int i   = 0;
		int j   = 0;
		int idx = 0;
		
		for(Double value : scalingFactors)
		{
			if(i%2 == 1 && j == 1) 
			{
				maxImag[idx] = value.doubleValue();
				idx++;
			}
			
			j++;
			if (j == 4)
			{
				j = 0;
				i++;
			}
		}
		return maxImag;
	}	

	protected void setDH(List<Double>  scalingFactors)
	{
		int j   = 0;
		
		dH = 0.0f;

		for(Double value : scalingFactors)
		{
			if(j == 3) 
			{
				dH = value.doubleValue() *(double)1e06; 
				// normalizing as all other measures are in microns
				break;
			}
			j++;
		}
	}
	
	protected double[] specI = 
		new double[]{4.241970e-001,
				4.440278e-001,
				4.638594e-001,
				5.831452e-001,
				7.024318e-001,
				7.394866e-001,
				7.765423e-001,
				7.848004e-001,
				7.930584e-001,
				7.644128e-001,
				7.357680e-001,
				8.129359e-001,
				8.901046e-001,
				9.416358e-001,
				9.931756e-001,
				9.965878e-001,
				1.000000e+000,
				9.874716e-001,
				9.749516e-001,
				9.794588e-001,
				9.839660e-001,
				9.537823e-001,
				9.235986e-001,
				9.258989e-001,
				9.282077e-001,
				9.216209e-001,
				9.150341e-001,
				9.022510e-001,
				8.894680e-001,
				9.017672e-001,
				9.140750e-001,
				9.001375e-001,
				8.862000e-001,
				8.846722e-001,
				8.831528e-001,
				8.659814e-001,
				8.488100e-001,
				8.332521e-001,
				8.176943e-001,
				8.153762e-001,
				8.130581e-001,
				7.829152e-001,
				7.527722e-001,
				7.583769e-001,
				7.639816e-001,
				7.622534e-001,
				7.605261e-001,
				7.524607e-001,
				7.443953e-001,
				7.256782e-001,
				7.069619e-001,
				7.087045e-001,
				7.104472e-001,
				6.948613e-001,
				6.792755e-001,
				6.800725e-001,
				6.808695e-001,
				6.896258e-001,
				6.983822e-001,
				6.814331e-001,
				6.644841e-001,
				6.281423e-001,
				5.918013e-001,
				5.998133e-001,
				6.078252e-001,
				6.194530e-001,
				6.310817e-001,
				5.769913e-001,
				5.229009e-001,
				5.580484e-001,
				5.931959e-001,
				6.152709e-001,
				6.373459e-001,
				5.885631e-001,
				5.397812e-001,
				4.668913e-001,
				3.940023e-001,
				4.805266e-001,
				5.670509e-001,
				5.525252e-001,
				5.379995e-001
	};
	
	protected double[] clrX = 
		new double[]{
			1.368000e-003,
			2.236000e-003,
			4.243000e-003,
			7.650000e-003,
			1.431000e-002,
			2.319000e-002,
			4.351000e-002,
			7.763000e-002,
			1.343800e-001,
			2.147700e-001,
			2.839000e-001,
			3.285000e-001,
			3.482800e-001,
			3.480600e-001,
			3.362000e-001,
			3.187000e-001,
			2.908000e-001,
			2.511000e-001,
			1.953600e-001,
			1.421000e-001,
			9.564000e-002,
			5.795000e-002,
			3.201000e-002,
			1.470000e-002,
			4.900000e-003,
			2.400000e-003,
			9.300000e-003,
			2.910000e-002,
			6.327000e-002,
			1.096000e-001,
			1.655000e-001,
			2.257500e-001,
			2.904000e-001,
			3.597000e-001,
			4.334500e-001,
			5.120500e-001,
			5.945000e-001,
			6.784000e-001,
			7.621000e-001,
			8.425000e-001,
			9.163000e-001,
			9.786000e-001,
			1.026300e+000,
			1.056700e+000,
			1.062200e+000,
			1.045600e+000,
			1.002600e+000,
			9.384000e-001,
			8.544500e-001,
			7.514000e-001,
			6.424000e-001,
			5.419000e-001,
			4.479000e-001,
			3.608000e-001,
			2.835000e-001,
			2.187000e-001,
			1.649000e-001,
			1.212000e-001,
			8.740000e-002,
			6.360000e-002,
			4.677000e-002,
			3.290000e-002,
			2.270000e-002,
			1.584000e-002,
			1.135900e-002,
			8.111000e-003,
			5.790000e-003,
			4.109000e-003,
			2.899000e-003,
			2.049000e-003,
			1.440000e-003,
			1.000000e-003,
			6.900000e-004,
			4.760000e-004,
			3.320000e-004,
			2.350000e-004,
			1.660000e-004,
			1.170000e-004,
			8.300000e-005,
			5.900000e-005,
			4.200000e-005
	};

	protected double[] clrY = 
		new double[]{
			3.900000e-005,
			6.400000e-005,
			1.200000e-004,
			2.170000e-004,
			3.960000e-004,
			6.400000e-004,
			1.210000e-003,
			2.180000e-003,
			4.000000e-003,
			7.300000e-003,
			1.160000e-002,
			1.684000e-002,
			2.300000e-002,
			2.980000e-002,
			3.800000e-002,
			4.800000e-002,
			6.000000e-002,
			7.390000e-002,
			9.098000e-002,
			1.126000e-001,
			1.390200e-001,
			1.693000e-001,
			2.080200e-001,
			2.586000e-001,
			3.230000e-001,
			4.073000e-001,
			5.030000e-001,
			6.082000e-001,
			7.100000e-001,
			7.932000e-001,
			8.620000e-001,
			9.148500e-001,
			9.540000e-001,
			9.803000e-001,
			9.949500e-001,
			1.000000e+000,
			9.950000e-001,
			9.786000e-001,
			9.520000e-001,
			9.154000e-001,
			8.700000e-001,
			8.163000e-001,
			7.570000e-001,
			6.949000e-001,
			6.310000e-001,
			5.668000e-001,
			5.030000e-001,
			4.412000e-001,
			3.810000e-001,
			3.210000e-001,
			2.650000e-001,
			2.170000e-001,
			1.750000e-001,
			1.382000e-001,
			1.070000e-001,
			8.160000e-002,
			6.100000e-002,
			4.458000e-002,
			3.200000e-002,
			2.320000e-002,
			1.700000e-002,
			1.192000e-002,
			8.210000e-003,
			5.723000e-003,
			4.102000e-003,
			2.929000e-003,
			2.091000e-003,
			1.484000e-003,
			1.047000e-003,
			7.400000e-004,
			5.200000e-004,
			3.610000e-004,
			2.490000e-004,
			1.720000e-004,
			1.200000e-004,
			8.500000e-005,
			6.000000e-005,
			4.200000e-005,
			3.000000e-005,
			2.100000e-005,
			1.500000e-005
	};
	
	protected double[] clrZ = 
		new double[] {
			6.450000e-003,
			1.055000e-002,
			2.005000e-002,
			3.621000e-002,
			6.785000e-002,
			1.102000e-001,
			2.074000e-001,
			3.713000e-001,
			6.456000e-001,
			1.039050e+000,
			1.385600e+000,
			1.622960e+000,
			1.747060e+000,
			1.782600e+000,
			1.772110e+000,
			1.744100e+000,
			1.669200e+000,
			1.528100e+000,
			1.287640e+000,
			1.041900e+000,
			8.129500e-001,
			6.162000e-001,
			4.651800e-001,
			3.533000e-001,
			2.720000e-001,
			2.123000e-001,
			1.582000e-001,
			1.117000e-001,
			7.825000e-002,
			5.725000e-002,
			4.216000e-002,
			2.984000e-002,
			2.030000e-002,
			1.340000e-002,
			8.750000e-003,
			5.750000e-003,
			3.900000e-003,
			2.750000e-003,
			2.100000e-003,
			1.800000e-003,
			1.650000e-003,
			1.400000e-003,
			1.100000e-003,
			1.000000e-003,
			8.000000e-004,
			6.000000e-004,
			3.400000e-004,
			2.400000e-004,
			1.900000e-004,
			1.000000e-004,
			5.000000e-005,
			3.000000e-005,
			2.000000e-005,
			1.000000e-005,
			0.000000e+000,
			0.000000e+000,
			0.000000e+000,
			0.000000e+000,
			0.000000e+000,
			0.000000e+000,
			0.000000e+000,
			0.000000e+000,
			0.000000e+000,
			0.000000e+000,
			0.000000e+000,
			0.000000e+000,
			0.000000e+000,
			0.000000e+000,
			0.000000e+000,
			0.000000e+000,
			0.000000e+000,
			0.000000e+000,
			0.000000e+000,
			0.000000e+000,
			0.000000e+000,
			0.000000e+000,
			0.000000e+000,
			0.000000e+000,
			0.000000e+000,
			0.000000e+000,
			0.000000e+000
	};

}
