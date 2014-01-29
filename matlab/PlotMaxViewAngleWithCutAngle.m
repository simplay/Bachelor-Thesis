% First Cut Angle


%
lambdaCnt = size(response,1);

if (~exist("lMax"))
  lMax = 780;
end

lInc = (lMax - lMin)./(lambdaCnt-1);

lambda = lMin + lInc*(-1+[1:lambdaCnt]);


numEle = ceil((angMax - angMin)/angInc);
angSet = angMin + ([1:numEle]-1)*angInc;
[xx yy] = meshgrid(lambda,angSet);

%surf(xx,yy,response.','linestyle','none');
colormap(summer);
view([0 90]);


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

surf(response,'linestyle','none');
thetaI = input("IcidenceAngle in degrees:\n");

xCut = input("Enter Index for cutAngle:\n");
cutAng1 = angMin + xCut*angInc;


dRep=lMin./( sin(cutAng1*pi()/180) + sin(thetaI*pi()/180));
cutAng = 180*asin(lambda/dRep -  sin(thetaI*pi()/180))/pi();

xMin = (cutAng - angMin)./angInc;
xMin = int32(xMin);

for i = 1:size(response,1)
response(i,1:xMin(i)) = 0;
end
surf(response, 'linestyle','none');
colormap(summer);
view([0 90]);
%% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
[maxC maxI] = max(response.');
viewAngForMax = angMin + angInc * (maxI-1);



hold on
plot(lambda, viewAngForMax,'r');
hold on

dPeriod = input("Enter Periodicity in Nanometers:\n");


thetaV =asin(lambda./dPeriod - sin(thetaI*pi()/180))*180/pi();
plot(lambda, thetaV,'b');

