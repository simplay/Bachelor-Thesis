lambdaCnt = size(response,1);

if (~exist("lMax"))
  lMax = 780;
end

lInc = (lMax - lMin)./(lambdaCnt-1);

lambda = lMin + lInc*(-1+[1:lambdaCnt]);

[maxC maxI] = max(response.');

viewAngForMax = angMin + angInc * (maxI-1);

hold on
plot(lambda, viewAngForMax,'r');
hold on

dPeriod = input("Enter Periodicity in Nanometers:\n");
thetaI = input("Incidence Angle in degrees:\n");

thetaV =asin(lambda./dPeriod - sin(thetaI*pi()/180))*180/pi();
plot(lambda, thetaV,'b');

