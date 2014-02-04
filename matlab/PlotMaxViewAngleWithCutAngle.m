% First Cut Angle
figure;

%
lambdaCnt = size(response,1);

if (~exist('lMax'))
  lMax = 780;
end

lInc = (lMax - lMin)./(lambdaCnt-1);

lambda = lMin + lInc*(-1+[1:lambdaCnt]);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
numEle = ceil((angMax - angMin)/angInc);
angSet = angMin + ([1:numEle]-1)*angInc;
[xx yy] = meshgrid(lambda,angSet);
surf(xx,yy,response.','linestyle','none', 'EdgeColor','none');

%% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
[maxC maxI] = max(response.');
viewAngForMax = angMin + angInc * (maxI-1);

AAA = lambda;
BBB = viewAngForMax;

hold on
plot(AAA, BBB,'r');
hold on



%surf(response,'linestyle','none');
colormap(summer);
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
surf(response,'linestyle','none', 'EdgeColor','none');
colormap(summer);


dPeriod = input("Enter Periodicity in Nanometers:\n");


thetaV =asin(lambda./dPeriod - sin(thetaI*pi()/180))*180/pi();
plot(lambda, thetaV,'b');
size(AAA);
size(BBB);
CCC = linspace(-0.05,0.05,size(BBB,2)).+0.1;
hold on
h1=plot3(AAA, BBB,CCC,'r');
hold on

hold on
h2=plot3(lambda, thetaV,CCC,'b');
hold on
set([h1 h2],'LineWidth',1)
view([0 90]);
axis([lMin,lMax,angMin,angMax])
set(gca, 'XTick', [lMin:100:lMax])
set(gca, 'YTick', [angMin:5:angMax])

grid off
replot
%print abc.png