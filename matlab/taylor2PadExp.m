function taylor2PadExp
	format long
	steps=30;
    
    dimSmall = 30;
    
    %dimN =  99;
    rep_nn=1;
    out = "out/";
    %patch_basis_path = "../input_patches/";
	patch_basis_path = "E:/Program Files (x86)/Octave-3.6.2/input_patches/";
	
	
	
    RectPatch1d = "100x100stam1dBump.bmp";
    RectPatch2d = "100x100stamBump.bmp";
    BlazingPatch = "BlazingBump.bmp";
	BlazingPatch99 = "BlazingBump99.bmp";
    CosinePatch = "CosineBump.bmp";
    hugePatch = "huge.png";
    Elaph100x100 = "Elaph100x100.png"
    Xeno100x100 = "Xeno100x100.png"
    SingleFingerElaphIdeal = "SingleFingerElaphIdeal.bmp"
    
    patch_file = Elaph100x100;
    whole_path = strcat(patch_basis_path,patch_file);
	inputIMG = imread(whole_path);
    inputIMG = repmat(inputIMG, rep_nn, rep_nn);
   
	d_inputIMG = double(inputIMG);
	d_inputIMG = d_inputIMG./255;
	
	if(length(size(inputIMG))==3),
		A = d_inputIMG(:,:,1);		
	else
		A = d_inputIMG;
	end
	%A = imresize(A, [dimSmall,dimSmall]);
    %A = imresize(A, [dimN,dimN]);
	%A = padarray(A,[dimDiff, dimDiff], 'both');
    
	
	dimN = size(A, 1);
	dimDiff = (dimN-dimSmall)/2;
	
	counter = 0;
	extrema = zeros(steps*4,1);
	globals = zeros(4,1);
	
	globalReMin = 1000000;
	globalReMax = -1000000;
	globalImMin = 1000000;
	globalImMax = -1000000;
	
	
	eps_teshold = 1e-10;
	
	angleA = -90;
	A = imrotate(A, angleA);
	
	for n=0:1:steps,
		B = power(1j*A, n);
		%B = A.^(n);
		C = fftshift(ifft2(B));
		C = imrotate(C, -angleA);
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
		
		
		
		outAmp(:,:,1) = reD;
		outAmp(:,:,2) = imD;
		outAmp(:,:,3) = zeros(dimN,dimN);
					
				
		% generate real and imaginary part images.		
		imwrite(outAmp, strcat(out,"AmpReIm",num2str(n),".bmp"))
		
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
    fprintf(f, whole_path);
    fclose(f);
	%dlmwrite(strcat(out,'paramters.txt'), parameters, 'delimiter', '\n')
end