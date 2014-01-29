weights = textread("weights.txt",'%f');
int = linspace(380,780,size(weights,1)/4);
plot(int,weights(1:4:end),'r', int,weights(2:4:end),'g',int,weights(3:4:end),'b')
title("Standard Observer Matching Functions")
xlabel("Wavelength [nm]")
ylabel("weights")
legend("X","Y","Z")