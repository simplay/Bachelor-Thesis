import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;


public class NminmaxFreqRespGen extends LookupGenerator {

	/**
	 * @param args
	 */
	
	protected static int angMin;
	protected static int angMax;
	protected static double angInc;
	protected static float dimX = 65.0f;// 2.5f; 
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		NminmaxFreqRespGen lGen = new NminmaxFreqRespGen();
		
		if (args.length > 0)
			ipPath = args[0];
		
		if (args.length > 1)
			opPath = args[1];
		
		if (args.length > 2)
			uvWid = Integer.parseInt(args[2]);
		
		uvHgh = uvWid;
		
		float lInc = (float)5e-3; // in microns

		if (args.length > 3)
			lInc = (float)(Integer.parseInt(args[3])*1e-3);

		if (args.length > 4)
			angMin = (Integer.parseInt(args[4]));
		
		if (args.length > 5)
			angMax = (Integer.parseInt(args[5]));
		
		if (args.length > 6)
			angInc = (Double.parseDouble(args[6]));
		
		if (args.length > 7)
			lGen.thetaI = (Integer.parseInt(args[7])) * Math.PI/180;
		
		if (args.length > 8)
			lGen.phiI = (Integer.parseInt(args[8])) * Math.PI/180;
			
		if (args.length > 9)
			lGen.phiR = (Integer.parseInt(args[9])) * Math.PI/180;
		
		if (args.length > 10)
			dimX = (Float.parseFloat(args[10]));
		
		else
		{
			System.out.println("Not continuing as all the parametes are not provided");
			return ;
		}
		
		
		//LMAX = 1732e-3;
		
		

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
		
		//float lInc = (float)5e-3; // in microns
		lGen.prepareColorTables(lInc);

		//lGen.thetaI = (float)(Math.PI * 75.0/180.0);
		//lGen.phiI = (float)(Math.PI * 0.0/180.0);
		//lGen.phiR = (float)(Math.PI * 0.0/180.0);
		
		//vAngInc = 0.01f;

		String fileName = opPath;
		PrintStream opFile = null;
		try {
			opFile = new PrintStream(new BufferedOutputStream(new FileOutputStream(fileName)));

			opFile.println("angMin = " + angMin );
			opFile.println("angMax = " + angMax );
			opFile.println("angInc = " + angInc );
			//opFile.println("lInc = "   + LMAX -LMIN)*1e3 );
			opFile.println("lMin = "   + LMIN*1e3 );
			opFile.println("lMax = "   + LMAX*1e3 );
			
			
			opFile.print("response= [ ");

		} catch (Exception e) {
			System.out.println("failed to write Response");
		}	
			
		lGen.numAng = (int)(Math.ceil((double)(angMax - angMin)/ angInc));
		lGen.response = new float[lGen.numAng]; 
		
		R = new float[lGen.numAng][lGen.lambdaCnt]; 
		
		
		flushResponseArray();
//		for (int lIdx = 0; lIdx < lGen.lambdaCnt; ++lIdx){
//			
//
//		}
		
		lGen.genFreqResponse();
		lGen.writeFrequencyResponse(opFile);
		
		try {

			opFile.println("];");
			opFile.close();

		} catch (Exception e) {
			System.out.println("failed to write Response");
		}	
		

	}
	static float[][] R;
	protected static float[] response;
	protected static int numAng;
	
	protected double thetaI;
	protected double phiI;
	protected double phiR;
	
	protected void writeFrequencyResponse(PrintStream opFile){
	try{

		for(int l = 0; l < lambdaCnt; ++l){
			if( l != 0){
				opFile.println("; ...");
			}
			for(int v = 0; v < numAng; ++v){
				opFile.print(R[v][l] +", ");
			}
			System.out.println("Finished writing for " +l);	
		}
		opFile.flush();
		
	} catch (Exception e) {
		System.out.println("failed to write Response");
	}
		
	
		
	}
	
	/**
	 * set for each angular indices response array to zero.
	 */
	protected static void flushResponseArray(){
		for (int angIdx=0; angIdx < numAng; ++angIdx){
			response[angIdx] = 0.0f;
		}
	}
	

	protected static int[] compute_N_min_max(float t){
		float N_min = 0.0f;
		float N_max = 0.0f;
		
//		float f = (float) (666f*dH);

		if(t > 0.0f){
			N_min = (int) Math.ceil((dimX*t) / (LMAX));
			N_max = (int) Math.floor((dimX*t) / (LMIN));
		}else{
			N_min = (int) Math.ceil((dimX*t) / (LMIN));
			N_max = (int) Math.floor((dimX*t) / (LMAX));
		}
		
		
		int[] ret = new int[2];
		ret[0] = (int) N_min; 
		ret[1] = (int) N_max; 
		return ret;
	}

protected int roundNM(float lambda){
	return (int)(lambda+0.5f);
}
protected void genFreqResponse(){
		for (int angIdx=0; angIdx < numAng; ++angIdx){
		//float thetaR = (float)(Math.PI * (angIdx-midAngIdx)*vAngInc/180.0f );
		float thetaR = (float)(Math.PI * (angMin + angInc*angIdx)/180.0f );
				
		// here we made it positive so that angles are measured similarly for viewing and incidence
		// note , viewing is done at 180 degrees apart in PHI
		float k1X =  (float)( Math.sin(thetaI)*Math.cos(phiI) );
		float k1Y =  (float)( Math.sin(thetaI)*Math.sin(phiI) );
		float k1Z = - (float)( Math.cos(thetaI));
				
		float k2X = (float)(Math.sin(thetaR)*Math.cos(phiR) );
		float k2Y = (float)( Math.sin(thetaR)*Math.sin(phiR));
		float k2Z = (float)( Math.cos(thetaR) );

				
		float uu = k1X - k2X;
		float vv = k1Y - k2Y;
		float ww = k1Z - k2Z;
				
		int[] N_u = compute_N_min_max(uu);
		int[] N_v = compute_N_min_max(vv);
				

		float lower_u = N_u[0];
		float upper_u = N_u[1];
		float lower_v = N_v[0];
		float upper_v = N_v[1];
				
		int lambda_lower_u = roundNM((float) (((dimX*uu)/upper_u)*Math.pow(10.0, 3.0)));
		int lambda_upper_u = roundNM((float) (((dimX*uu)/lower_u)*Math.pow(10.0, 3.0)));
		if(upper_u < 0.0){
			lambda_lower_u = roundNM((float) (((dimX*uu)/lower_u)*Math.pow(10.0, 3.0)));
			lambda_upper_u = roundNM((float) (((dimX*uu)/upper_u)*Math.pow(10.0, 3.0)));	
		}
				
		int lambda_lower_v = roundNM((float) (((dimX*vv)/upper_v)*Math.pow(10.0, 9.0)));
		int lambda_upper_v = roundNM((float) (((dimX*vv)/lower_v)*Math.pow(10.0, 9.0)));
		if(upper_v < 0.0){
			lambda_lower_v = roundNM((float) (((dimX*vv)/lower_v)*Math.pow(10.0, 9.0)));
			lambda_upper_v = roundNM((float) (((dimX*vv)/upper_v)*Math.pow(10.0, 9.0)));	
		}
		
		for(int lambda_u = lambda_lower_u; lambda_u <=lambda_upper_u; lambda_u++ ){
			int lambdaIndex = lambda_u-(int)(LMIN*1000);
			double fftMagSqr = getFFTMagSqr_At(uu,vv,ww, lambdaIndex);
			if (angMin + angInc*angIdx > 90.0 || angMin + angInc*angIdx < -90.0) fftMagSqr = 0.0;
			R[angIdx][lambdaIndex] += (float)(fftMagSqr * gainAt(k1X, k1Y, k1Z, k2X, k2Y, k2Z) );
		}
		
		for(int lambda_v = lambda_lower_v; lambda_v <=lambda_upper_v; lambda_v++ ){
			int lambdaIndex = lambda_v-(int)(LMIN*1000);
			double fftMagSqr = getFFTMagSqr_At(uu,vv,ww, lambdaIndex);
			if (angMin + angInc*angIdx > 90.0 || angMin + angInc*angIdx < -90.0) fftMagSqr = 0.0;
			R[angIdx][lambdaIndex] += (float)(fftMagSqr * gainAt(k1X, k1Y, k1Z, k2X, k2Y, k2Z) );
		}
	}
		
}
	
	protected double gainAt(float k1X, float k1Y,float  k1Z,float k2X, float k2Y, float  k2Z){
	
		float gF = 1 - (k1X*k2X + k1Y*k2Y + k1Z*k2Z );
		
		 gF = gF*gF;
		 
		 float ww = k1Z - k2Z;
		 
		 
		 if (k1Z > 0.0 || k2Z < 0.0)
		 {
			 return 0.0; // This side is not visible
		 }
		 
		 ww = ww * ww;
		 
		 if (ww < Math.pow(10.0,-4.0))
		 {
			 // return 0.0; // Shadowing Function
		 }
		 
		 gF = gF / k2Z;
		 gF = gF / ww;
		 
		 double fFac = getFresnelFactor(k1X, k1Y, k1Z, k2X, k2Y, k2Z); 

		 //return fFac*gF;
		 
		 return 1.0;
	}
	
	protected double getFresnelFactor(float k1X, float k1Y,float  k1Z,float k2X, float k2Y, float  k2Z){
		double nSkin = 1.5;
		double nK = 0.0;
		
		double hVecX  = - k1X + k2X;
		double hVecY  = - k1Y + k2Y;
		double hVecZ  = - k1Z + k2Z;
			
		double normH;
		
		normH = Math.sqrt(hVecX*hVecX + hVecY*hVecY + hVecZ*hVecZ);
		
		hVecX /= normH;
		hVecY /= normH;
		hVecZ /= normH;
		
		 
		double cosTheta = hVecX*k2X + hVecY*k2Y + hVecZ*k2Z;	
		
		double fF = (nSkin - 1.0);
		
		fF = fF * fF;
		
		double R0 = fF + nK*nK;
		if (cosTheta > 0.999)
			fF = R0;
		else
			fF = fF + 4*nSkin*Math.pow(1- cosTheta,5.0) + nK*nK;
		
		// do this division if its not on relative scale
		// fF = fF/ ((nSkin + 1.0)* (nSkin + 1.0) + nK*nK);
		
		return fF/R0;		
	}
	
	
	protected double getFFTMagSqr_At(float uu0, float vv0, float ww0, int lIdx){
//		System.out.println("dH " + this.dH);

//		System.out.println("lIdx " + currLambda[lIdx] + " dh " + dH + " lmax " + this.LMAX);


		double uu = (uu0 * dH)/ currLambda[lIdx];
		double vv = (vv0 * dH)/ currLambda[lIdx];
		
		// following call loads FFT values at given indices in temp buffers ie tmpRE, tmpIM
		// get indices in pixel units
		loadFFTValuesFor(vv, uu, imgCnt);
		double sumRealOverP = 0.0f;
		double sumImagOverP = 0.0f;
		
		for (int n = 0; n <  imgCnt; ++n)
		{
			sumRealOverP += (Math.pow(2.0 * Math.PI, n) * (tmpRE[n] / Math.pow( currLambda[lIdx], n)) / facto[n]);
			sumImagOverP += (Math.pow(2.0 * Math.PI, n) * (tmpIM[n] / Math.pow( currLambda[lIdx], n)) / facto[n]);
		}
		

		return (sumRealOverP*sumRealOverP + sumImagOverP*sumImagOverP );
		
	}

}
