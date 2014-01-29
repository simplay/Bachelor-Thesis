function getXYZWeights(lamdas)

	load X10deg.dat
	A = X10deg;
    out = "out/";
	% matrix A has stored data [nm,x,y,z] with index-range: 1-4401
	% i.e. A(4401,;) == [830.0	1.762465E-06	7.053860E-07	0.000000E+00]
	
	sumN = zeros(1,3);
	% find closest lamda from given lamda - precission .1
	% and then calculate weighted average for this given lamda value
	
  for k=1:length(lamdas),
		intTmp1Lamda = int32 (lamdas(k)*10);
		truncated = double(intTmp1Lamda)/10;
		index = int32((truncated-390)/0.1 +1);
		tmpRow = A(index,2:4);
		sumN = sumN + tmpRow;
    res(k,:) =tmpRow;
  end
  
  for k=1:length(lamdas),
    res(k,:) = res(k,:)./sumN;
  end
  
	dlmwrite(strcat(out,"weights.txt"), res, 'delimiter', '\n')

end