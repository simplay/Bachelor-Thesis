u = 0:0.5:2;
% in microns
dx = 65.0;
lmin = 0.38;
lmax = 0.78;
lambdas = linspace(0.38, 0.78, size(u,2));
lmins = (u.*dx)./lmax;
lmins = (u.*dx)./lmax;

%k1.x = - sin(thetaI)*cos(phiI);
%k1.y = - sin(thetaI)*sin(phiI);
%k1.z = - cos(thetaI);
%k2.x = sin(thetaR)*cos(phiR);
%k2.y = sin(thetaR)*sin(phiR);
%k2.z = cos(thetaR);

for t=1:1:size(u,2),
Nmin = (u(t)*dx)/lmax;
Nmax = (u(t)*dx)/lmin;
delta = Nmax-Nmin
for k=Nmin:1:Nmax,
if(k ~= 0)
	l = (u(t)*dx)/k;
	plot(k,l*1000, '*')
	hold on;
	end
end
end
text = strcat("wavelengths between Nmin,Nmax for u-axis ",num2str(dx)," microns spacing");
title(text)
ylabel("Wavelength [nm]")
xlabel("N")


