function patcher
	format long
	steps=30;
    
    dimSmall = 30;

    rep_nn=1;
    out = "out/";

	maxHeight = 2.4623E-7
	N = 100;
	t = linspace(0,1,N);
	t2 = [t(2:end), 0];
	A = repmat(t2, N, [1,:]);

	dimN = size(A, 1);
	dimDiff = (dimN-dimSmall)/2;
	
	counter = 0;
	extrema = zeros(steps*4,1);
	globals = zeros(4,1);
	
	globalReMin = 1000000;
	globalReMax = -1000000;
	globalImMin = 1000000;
	globalImMax = -1000000;
	A = imrotate(A, 90);
	eps_teshold = 1e-16;
	A = A.*maxHeight;
	%A = A - mean(A(:));	
	angleA = -90;
	%A = A';
	A = imrotate(A, -angleA);
	A
	for n=0:1:steps,
	outAmp = [];
		B = power(1j*A, n);
		%B = A.^(n);
		C = fftshift(ifft2(B));
		%C = imrotate(C, -angleA);
		% find real minimum in shifted D matrix.
		reMin = min(min( real(C) ));
		imMin = min(min( imag(C) ));
		
		
		% shift towards zero
		reC = (real(C)-reMin);
		imC = (imag(C)-imMin);
				
		% find real maximum in shifted D matrix.
		reMax = max(max(reC));
		imMax = max(max(imC));
				
		% since we will defide by that found maximum value
		% we have to care about dividing by zero.
		%reMax = max(abs(reMax),1);
		%imMax = max(abs(imMax),1);
        if(reMax < eps_teshold) reMax = 1; end
		if(imMax < eps_teshold) imMax = 1; end
				
		% scale entries of shifted D towards range [0,1]
		reD = reC / reMax;
		imD = imC / imMax;
		
		% store extrema values in predefined format manor.
		extrema(4*counter+1) = reMin; 
		extrema(4*counter+2) = reMax;
		extrema(4*counter+3) = imMin;
		extrema(4*counter+4) = imMax;
	
		% set global extrema.
		if(reMin < globalReMin) globalReMin = reMin; end
		if(reMax > globalReMax) globalReMax = reMax; end
		if(imMin < globalImMin) globalImMin = imMin; end
		if(imMax > globalImMax) globalImMax = imMax; end
		
		
		
		%reD = imrotate(reD, -angleA);	
		%imD = imrotate(imD, -angleA);	
		
		outAmp(:,:,1) = reD;
		outAmp(:,:,2) = imD;
		outAmp(:,:,3) = zeros(dimN,dimN);
		
				
				
		% generate real and imaginary part images.		
		imwrite(outAmp, strcat(out,"AmpReIm",num2str(n),".bmp"))


		fpData = fopen(sprintf("out/AmpReIm%d.txt",n),'wb');
		fwrite(fpData,size(outAmp,2),'int',0,'b');
		fwrite(fpData,size(outAmp,1),'int',0,'b');
  
		outAmp(:,:,1) = outAmp(end:-1:1,:,1);
		outAmp(:,:,2) = outAmp(end:-1:1,:,2);
		outAmp(:,:,3) = outAmp(end:-1:1,:,3);
	
		outAmp = permute(outAmp, [ 3,2,1]);
  
		fwrite(fpData,outAmp(:),'float32',0,'b');
		fclose(fpData);
		
		
		
		% increment index counter
		counter = counter + 1;
		
	end
	
	% store global extrema for imaginary and real part
	globals(1) = globalReMin;
	globals(2) = globalReMax;
	globals(3) = globalImMin;
	globals(4) = globalImMax;

	% save extrema  and globals in a txt file.
	dlmwrite(strcat(out,"extrema.txt"), extrema, 'delimiter', '\n')
	dlmwrite(strcat(out,"globals.txt"), globals, 'delimiter', '\n')
	
	
	% spectrum
	Lmin = 390;
	Lmax = 700;
	discretSpectrum = linspace(Lmin,Lmax,Lmax-Lmin+1);
	
	% save color weights for CIE_XYZ space
	getXYZWeights(discretSpectrum);
	
	% save important paramters which have been used for calcualtions
    %parameters = [ num2str(Lmin); num2str(Lmax); num2str(steps); num2str(dimN);num2str(n)]
    parameters = [Lmin; Lmax; steps; dimN;dimSmall;dimDiff;rep_nn]
    f = fopen(strcat(out,"paramters.txt"), 'w')
    for t=1:length(parameters),
        fprintf(f,'%i \n', parameters(t));
    end
    fprintf(f, "out/");
    fclose(f);
	%dlmwrite(strcat(out,'paramters.txt'), parameters, 'delimiter', '\n')
end