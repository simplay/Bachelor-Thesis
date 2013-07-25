l_min = 390.0;
l_max = 700.0;
im0 = imread('../screenshots/debug.png');
im = double(im0);
im = im./255;
dim = size(im);

%corners = zeros(5,2);
p = 1;
min_x = dim(1)+1;
min_y = dim(2)+1;
max_x = -1;
max_y = -1;

for k=1:1:dim(1),
    for t=1:1:dim(2),
        x1 = im(k,t,1);
        x2 = im(k,t,2);
        x3 = im(k,t,3);
        
        if(x2 ~= 0 && x3 ~= 0)
            
            if(k < min_x) 
                min_x = k; 
            end
            if(t < min_y) 
                min_y = t; 
            end
            if(k > max_x) 
                max_x = k; 
            end
            if(t > max_y) 
                max_y = t; 
            end
            

        end
        
    end
end


delta = double(l_max-l_min);
im2 = im(1, min_y:max_y, 1);

im2 = (im2 - min(im2(1,:)));
im2 = (im2 / max(im2(1,:)));

for k=1:size(im2,2),
    im2(1,k) = delta*im2(1,k)+l_min;
end

angle_axis = linspace(-90,90,size(im2,2));
angle_wavelength = [angle_axis; im2(1,:)];

hold on

sp_1 = subplot(2,1,1);
plot(angle_wavelength(1,:), angle_wavelength(2,:));
title(sp_1,'respone shader') 
axis([-90 90 300 800])
xlabel('angle in degree')
ylabel('wavelength in nm')
clear all;

sp_2 = subplot(2,1,2);
to = -pi/2:pi/1000:pi/2;
ti = 20*pi/180
lambdaMin = 390;
lambdaMax = 700;

d = 1.0 ; % in micrometers
k = 1;
m_lower = -10;
m_upper = 10;
for m = m_lower:1:m_upper
  if m == 0
    continue;
  end
  temp = (d*1000)*(sin(ti)+sin(to))/m; % wavelenght in nanometers
  temp = temp.*(temp > lambdaMin);
  temp = temp.*(temp < lambdaMax);
  
    
  plot(to*180/pi, temp,'color',rand(3,1));
  title(sp_2,'grating equation')
  axis([-90 90 300 800])
  xlabel('angle in degree')
  ylabel('wavelength in nm')
  
  f{k} = temp;
  k = k+1;
  hold on;
end

