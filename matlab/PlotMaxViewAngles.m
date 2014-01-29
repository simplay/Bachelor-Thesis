fig3_h = figure
lambdaCnt = size(response,1);

if (~exist("lMax"))
  lMax = 780;
end

lInc = (lMax - lMin)./(lambdaCnt-1);

lambda = lMin + lInc*(-1+[1:lambdaCnt]);

[maxC maxI] = max(response.');

viewAngForMax = angMin + angInc * (maxI-1);
baseAngle = 75
eps = 2

hold on
plot(lambda, viewAngForMax,' .r');
plot(lambda, viewAngForMax,'-r');
hold on

dPeriod = input("Enter Periodicity in Nanometers:\n");
%thetaI = input("Incidence Angle in degrees:\n");

for thetaI=baseAngle-eps:0.5:baseAngle+eps,
	hold on
	thetaV =asin(lambda./dPeriod - sin(thetaI*pi()/180))*180/pi();
	if(thetaI==75)
		plot(lambda, thetaV,'+ b');
		plot(lambda, thetaV,'-b');
	else
		plot(lambda, thetaV,'. g');
	end
	hold on
end
figure(1);
print(fig3_h,"figure3.png", "-dpng"); 
hold off;