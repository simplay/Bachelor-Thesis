% TODO: make bump image path variable
% format of stored extrema:
% foreach t, foreach w, 
% [realMin_(nm(t),w), realMax_(nm(t),w), imagMin_(nm(t),w), imagMax_(nm(t),w)]

function compGenBMP()
	format long
    wStep=0.1;
    specStep=16;
    dimN = 100;
    n = 1;
    
    out = 'out/';
    patch_basis_path = '../input_patches/';
    RectPatch1d = '100x100stam1dBump.bmp';
    RectPatch2d = '100x100stamBump.bmp';
    BlazingPatch = 'BlazingBump.bmp';
    CosinePatch = 'CosineBump.bmp';
    
    patch_file = RectPatch1d;
    
	inputIMG = imread(strcat(patch_basis_path,patch_file));
    inputIMG = repmat(inputIMG, n, n);
    %inputIMG = rgb2gray(inputIMG);
	d_inputIMG = double(inputIMG);
	d_inputIMG = d_inputIMG./255;
	
	if(length(size(inputIMG))==3),
		A = d_inputIMG(:,:,1);	
	else
		A = d_inputIMG;
    end
    
	A = imresize(A, [100,100]);
	
	% height of bump, if equals 1 (in meters);
	f = 1.5*(10)^-7;
	%f = 1.49E-07;
	% other default constants
	counter = 0;
	p = [];
	extrema = [];
	globals = [];
	
	globalReMin = 1000000;
	globalReMax = -1000000;
	globalImMin = 1000000;
	globalImMax = -1000000;
	
	% spectrum
	Lmin = 390;
	Lmax = 700;
	
	discretSpectrum = linspace(Lmin,Lmax,specStep);
	kValues = (2.0*pi) ./ (discretSpectrum*(10^-9));
	
	% for each wavelength of our spectrum do
	for t=1:1:length(discretSpectrum),
	
		% for each discrete value in [-2,2] of w do
		for w=-2:wStep:2,

				% current wavenumber
				k = kValues(t) / (2.0*pi);
				
				% 2dim fourier transform of transformed 
				% by exp(...) of input patch. 
				B = exp(i*k*w*f*A);
				C = fftshift(fft2(B));
				% find real and imaginary minimum in matrix C.
				reMin = min(min( real(C) ));
				imMin = min(min( imag(C) ));
				
				% shift towards zero
				reC = (real(C)-reMin);
				imC = (imag(C)-imMin);
				
				% find real and imaginary maximum in shifted C matrix.
				reMax = max(max(reC));
				imMax = max(max(imC));
				
				% since we will defide by that found maximum value
				% we have to care about dividing by zero.
				%reMax = max(abs(reMax),1);
				%imMax = max(abs(imMax),1);
                if(reMax == 0) reMax = 1; end
				if(imMax == 0) imMax = 1; end
                
				% scale entries of shifted C towards range [0,1]
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
				%outReal(:,:,1) = reD*0 + 0.5;
				outReal(:,:,2) = zeros(dimN,dimN);
				outReal(:,:,3) = zeros(dimN,dimN);
				
				outImag(:,:,1) = imD;
				%outImag(:,:,1) = imD*0 + 0.5;
				outImag(:,:,2) = zeros(dimN,dimN);
				outImag(:,:,3) = zeros(dimN,dimN);


				% generate real and imaginary part images.
				
				imwrite(outReal, strcat(out,'reL',num2str(t),'w',num2str(w),'BH.bmp'))
				imwrite(outImag, strcat(out,'imL',num2str(t),'w',num2str(w),'BH.bmp'))
				
				% increment index counter

			counter = counter + 1;
		end
	end
	
	% store global extrema for imaginary and real part
	globals(1) = globalReMin;
	globals(2) = globalReMax;
	globals(3) = globalImMin;
	globals(4) = globalImMax;
	
	% save extrema  and globals in a txt file.
	dlmwrite(strcat(out,'extrema.txt'), extrema, 'delimiter', '\n')
	dlmwrite(strcat(out,'globals.txt'), globals, 'delimiter', '\n')
	
	% save color weights for CIE_XYZ space
	getXYZWeights(discretSpectrum);
	
	% save wavenumbers
	dlmwrite(strcat(out,'kvalues.txt'), kValues, 'delimiter', '\n')
end