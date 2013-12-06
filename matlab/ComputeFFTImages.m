%ComputeFFTImages('./blazedBump111.png', 2.5e-1, 2.5e-6/100, 39)


function []= ComputeFFTImages(maxH, dH, termCnt)
%clear variables;
%maxH = 3.75e-7;   %Elaph
%maxH = 4.4345e-007; % Xeno

%dH = 15e-6/256;

%1.0 Prepare the heightfield image
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%imH = imread('.\DataFromJeremie\RotatedBicubicElaph256x256.png');
	patch_basis_path = '../input_patches/';
    blaze100 = 'BlazingBump.bmp';
    patch_file = blaze100;
    inName = strcat(patch_basis_path, patch_file);    
    
for i = numel(inName):-1:1
  if (inName(i)=='.')
    break
  end
end
if(strcmp(inName(i+1:end), 'mat'))
  load(inName);
  
  patchImg = imresize(patchImg,[666 666]);
  %patchImg = patchImg(1:666,1:666);
  %patchImg = patchImg * 1e-3;
  
  %patchImg = imrotate(patchImg, 180);
else

  patchImg = imread(inName);
  patchImg = double(patchImg)./255;

end

if(ndims(patchImg) > 2)
  patchImg = rgb2gray(patchImg);
end


angleA = -90;
%angleA = 0;

%patchImg = imresize(patchImg,[650 650]);

patchImg = patchImg.*maxH;
patchImg = patchImg - mean(patchImg(:));
patchImg = imrotate(patchImg, angleA);
%patchImg = repmat(patchImg,4,4);
%patchImg = imresize(patchImg,[100 100]);

fp = fopen('../out/extrema.txt','wt');

%termCnt = 40;

for i = 1 : termCnt
  patchFFT = power(1j*patchImg, i-1);
  %fftTerm{i} = fftshift(fft2(patchFFT)./numel(patchImg));
  % dont divide by numel because inverse transform is normalized with numel
  % already
  fftTerm{i} = fftshift(ifft2(patchFFT));
 
    imOut = [];
    imOut(:,:,1)  = real(fftTerm{i});
    imOut(:,:,2)  = imag(fftTerm{i});
    imOut(:,:,3)  = 0.5;

    imOut = imrotate(imOut, -angleA);

    %imOut(:,:,1) = reshape(1:numel(patchImg), size(patchImg))/numel(patchImg);
    %imOut(:,:,2) = reshape(1:numel(patchImg), size(patchImg))'/numel(patchImg);
    %imOut(:,:,3) = 0.5;
    
    %imOut = imresize(imOut,[256 256],'bilinear');
    
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
    
    imwrite(imOut, sprintf('../out/AmpReIm%d.bmp',i-1));
    fpData = fopen(sprintf('../out/AmpReIm%d.txt',i-1),'wb');
    fwrite(fpData,size(imOut,2),'int',0,'b');
    fwrite(fpData,size(imOut,1),'int',0,'b');

    imOut(:,:,1) = imOut(end:-1:1,:,1);
    imOut(:,:,2) = imOut(end:-1:1,:,2);
    imOut(:,:,3) = imOut(end:-1:1,:,3);
    
    imOut2 = permute(imOut, [ 3,2,1]);
    
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
end

