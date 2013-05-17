function taylor(steps=30, specStep=16)
	format long
	inputIMG = imread("E:/image/milestone_samples/100x100stam1dBumpW20.bmp");
	d_inputIMG = double(inputIMG);
	d_inputIMG = d_inputIMG./255;
	
	if(length(size(inputIMG))==3),
		A = d_inputIMG(:,:,1);		
	else
		A = d_inputIMG;
	end
	
	counter = 0;
	extrema = [];
	globals = [];
	
	globalReMin = 1000000;
	globalReMax = -1000000;
	
	for n=0:1:steps,
	
		B = A^n;
		C = fftshift(fft2(B));
		D = (real(C))^2 + (imag(C))^2;
		
		% find real minimum in shifted D matrix.
		reMin = min(min(D));
				
		% shift towards zero
		reD = (D-reMin);
				
		% find real maximum in shifted D matrix.
		reMax = max(max(reD));
				
		% since we will defide by that found maximum value
		% we have to care about dividing by zero.
		reMax = max(abs(reMax),1);
				
		% scale entries of shifted D towards range [0,1]
		E = reD / reMax;
		
		% store extrema values in predefined format manor.
		extrema(2*counter+1) = reMin; 
		extrema(2*counter+2) = reMax;
	
		% set global extrema.
		if(reMin < globalReMin) globalReMin = reMin; end
		if(reMax > globalReMax) globalReMax = reMax; end
		
		outReal(:,:,1) = E;
		outReal(:,:,2) = zeros(100,100);
		outReal(:,:,3) = zeros(100,100);		
				
		% generate real and imaginary part images.		
		imwrite(outReal, strcat("AmpL",num2str(n),"BH.bmp"))
				
		% increment index counter
		counter = counter + 1;
		
	end
	
	% store global extrema for imaginary and real part
	globals(1) = globalReMin;
	globals(2) = globalReMax;

	% save extrema  and globals in a txt file.
	dlmwrite("extrema.txt", extrema, 'delimiter', '\n')
	dlmwrite("globals.txt", globals, 'delimiter', '\n')
	
	
	% spectrum
	Lmin = 390;
	Lmax = 700;
	discretSpectrum = linspace(Lmin,Lmax,specStep);
	kValues = (2.0*pi) ./ (discretSpectrum*(10^-9));
	
	% save color weights for CIE_XYZ space
	getBW3(discretSpectrum);
	
	% save wavenumbers
	dlmwrite("kvalues.txt", kValues, 'delimiter', '\n')
	
	
end