function taylor2PadExpComp
	format long
	steps=30; specStep=16;
    dimN = 100;
    n=1;
    out = 'out/';
    patch_basis_path = '../input_patches/';
    RectPatch1d = '100x100stam1dBump.bmp';
    RectPatch2d = '100x100stamBump.bmp';
    BlazingPatch = 'BlazingBump.bmp';
    CosinePatch = 'CosineBump.bmp';
    
    patch_file = RectPatch1d;
    
	inputIMG = imread(strcat(patch_basis_path,patch_file));
    inputIMG = repmat(inputIMG, n, n);
   
	d_inputIMG = double(inputIMG);
	d_inputIMG = d_inputIMG./255;
	
	if(length(size(inputIMG))==3),
		A = d_inputIMG(:,:,1);		
	else
		A = d_inputIMG;
	end
	%A = imresize(A, [30,30]);
	%A = padarray(A,[35, 35], 'both');
    
	counter = 0;
	extrema = [];
	globals = [];
	
	globalReMin = 1000000;
	globalReMax = -1000000;
	globalImMin = 1000000;
	globalImMax = -1000000;

	
	for n=0:1:steps,
	
		B = A.^n;
		C = fftshift(ifft2(B));
		
		
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
        if(reMax == 0) reMax = 1; end
		if(imMax == 0) imMax = 1; end
				
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
		
		outReal(:,:,1) = reD;
		outReal(:,:,2) = zeros(dimN,dimN);
		outReal(:,:,3) = zeros(dimN,dimN);
				
		outImag(:,:,1) = imD;
		outImag(:,:,2) = zeros(dimN,dimN);
		outImag(:,:,3) = zeros(dimN,dimN);	
				
		% generate real and imaginary part images.		
		imwrite(outReal, strcat(out,'AmpRe',num2str(n),'.bmp'))
		imwrite(outImag, strcat(out,'AmpIm',num2str(n),'.bmp'))
		
		% increment index counter
		counter = counter + 1;
		
	end
	
	% store global extrema for imaginary and real part
	globals(1) = globalReMin;
	globals(2) = globalReMax;
	globals(3) = globalImMin;
	globals(4) = globalImMax;

	% save extrema  and globals in a txt file.
	dlmwrite(strcat(out,'extrema.txt'), extrema, 'delimiter', '\n')
	dlmwrite(strcat(out,'globals.txt'), globals, 'delimiter', '\n')
	
	
	% spectrum
	Lmin = 390;
	Lmax = 700;
	discretSpectrum = linspace(Lmin,Lmax,Lmax-Lmin+1);
	
	% save color weights for CIE_XYZ space
	getXYZWeights(discretSpectrum);
	
	% save wavenumbers
	
	
end