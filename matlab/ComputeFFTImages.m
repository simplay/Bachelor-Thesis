% %
% Computes (inverse) Discrete Fourier Transforms for the power terms in
% Taylor series expansion for STAM's Formulation using Height-field
% Inputs:
% inName  : A string giving Filename for the height-field file. It can be a
%         .mat file with height-field stored as a 2D array named t
%         patchImage and normalized to have values between 0 - 1. 
%         Alternately, he height-field may be stored as an image file 
%         where MIN_COLOR corresponds to minimum height and MAX_COLOR 
%         corresponds to maximum height in the field.
%
% maxH    : A floating-point number specifying the value of maximum height 
%         of the height-field in MICRONS, where the minimum-height is zero. 
%         
% dH      : A floating-point number specifying the resolution (pixel-size) 
%         of the 'discrete' height-field in MICRONS. It must less than 0.1
%         MICRONS to ensure proper response for visible-range of light 
%         spectrum.
%
% termCnt : An integer specifying the number of Taylor series terms to use.
% %
function []= ComputeFFTImages(maxH, dh, termCnt)
dimSmall = 30;
rep_nn=1;
patch_basis_path = "E:/Program Files (x86)/Octave-3.6.2/input_patches/";
BlazingPatch99 = "BlazingBump99.bmp";
BlazingPatch200 = "blaze200.bmp";
E666 = "Elaph666x666.bmp";
patch_file = E666;
inName = strcat(patch_basis_path,patch_file);
%maxH = 3.75e-1;   % Elaph in microns
%maxH = 4.4345e-1; % Xeno in Microns

%dH = 15e-6/256; %

dH = dh*1E-6;
%1.0 Prepare the heightfield image
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

for i = numel(inName):-1:1
  if (inName(i)=='.')
    break
  end
end
if(strcmp(inName(i+1:end), 'mat'))
  load(inName);
else
  
  patchImg = imread(inName);
  patchImg = double(patchImg)./255;
end

if(ndims(patchImg) > 2)
  patchImg = rgb2gray(patchImg);
end
dimN = size(patchImg, 1);
dimDiff = (dimN-dimSmall)/2;
angleA = -90;

patchImg = patchImg.*maxH;
patchImg = patchImg - mean(patchImg(:));
patchImg = imrotate(patchImg, angleA);


fp = fopen("out/extrema.txt",'wt');


for t = 0 : termCnt
  patchFFT = power(1j*patchImg, t);
  
  % dont divide by numel because inverse transform is normalized with numel
  % already
  fftTerm{t+1} = fftshift(ifft2(patchFFT));
  
  imOut = [];
  imOut(:,:,1)  = real(fftTerm{t+1});
  imOut(:,:,2)  = imag(fftTerm{t+1});
  imOut(:,:,3)  = 0.5;
  
  imOut(:,:,1) = imrotate(imOut(:,:,1), -angleA);
  imOut(:,:,2) = imrotate(imOut(:,:,2), -angleA);
  imOut(:,:,3) = imrotate(imOut(:,:,3), -angleA);
  
  for p=1:2
    tempCmp = imOut(:,:,p);
    minV(p) = min(tempCmp(:));
    if (abs(minV(p)) < 1E-35)
      minV(p) = 0;
    end
    
    tempCmp = tempCmp - minV(p);
    maxV(p) = max(tempCmp(:));
    if (maxV(p) < 1E-35)
      maxV(p) = 1;
    end
    
    tempCmp = tempCmp./ maxV(p);
    imOut(:,:,p) = tempCmp;
  end
  
  minV(3) = 0.0;
  maxV(3) = 1.0;
  
  imwrite(imOut, sprintf("out/AmpReIm%d.bmp",t));
  fpData = fopen(sprintf("out/AmpReIm%d.txt",t),'wb');
  fwrite(fpData,size(imOut,2),'int',0,'b');
  fwrite(fpData,size(imOut,1),'int',0,'b');
  
  imOut(:,:,1) = imOut(end:-1:1,:,1);
  imOut(:,:,2) = imOut(end:-1:1,:,2);
  imOut(:,:,3) = imOut(end:-1:1,:,3);
  imOut2 = imOut;
  imOut2 = permute(imOut, [ 3,2,1]);
  
  %if(t==0) imshow(imOut2) end
  
  fwrite(fpData,imOut2(:),'float32',0,'b');
  fclose(fpData);
  % dH is written as the forth component
  
  fprintf(fp,'%36.35f\n',minV(1));
  fprintf(fp,'%36.35f\n',minV(2));
  fprintf(fp,'%36.35f\n',minV(3));
  
  fprintf(fp,'%36.35f\n', dH);
  fprintf(fp,'%36.35f\n',maxV(1));
  fprintf(fp,'%36.35f\n',maxV(2));
  fprintf(fp,'%36.35f\n',maxV(3));
  fprintf(fp,'%36.35f\n', dH);
end

fclose(fp);
fclose all;


% spectrum
	Lmin = 390;
	Lmax = 700;
	discretSpectrum = linspace(Lmin,Lmax,Lmax-Lmin+1);
	
	% save color weights for CIE_XYZ space
	getXYZWeights(discretSpectrum);
	
	% save important paramters which have been used for calcualtions
    %parameters = [ num2str(Lmin); num2str(Lmax); num2str(steps); num2str(dimN);num2str(n)]
    parameters = [Lmin; Lmax; termCnt; dimN;dimSmall;dimDiff;rep_nn];
    f = fopen(strcat("out/paramters.txt"), 'w');
    for t=1:length(parameters),
        fprintf(f,'%i \n', parameters(t));
    end
    fprintf(f, inName);
    fclose(f);
	%dlmwrite(strcat(out,'paramters.txt'), parameters, 'delimiter', '\n')

end

