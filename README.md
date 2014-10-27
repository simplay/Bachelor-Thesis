#Bachelor Thesis Diffraction Shaders

**Author**: Michael Single [ _simplay_ ]

**Email**: silent.simplay@gmail.com

Based on J. Stam's Paper about Diffraction Shaders

Consult the following link to his Paper:
http://www.dgp.toronto.edu/people/stam/reality/Research/pdf/diff.pdf

##Description: 
Simulate the effect of diffraction (the far-field-effect) on a given surface for a given patch of the nanostructure of the surface.
Assumption: The patch is periodic all over the surface (at least piecewise). The simulation itself is rendered in realtime by a shader written in OpenGL accessed by a Java-Driver program using Jogl.


##Installation-Guide for Windows Vista/7/8 (32/64bit):

**step 1**: install java
http://www.java.com/de/download/

**step 2**: install java jdk
http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html

**step 3**: download eclipse
http://www.eclipse.org/downloads/packages/eclipse-ide-java-ee-developers/junosr2

**step 4**:
if you have windows 7, then download the files from my dropbox link below and go to step 5:
dropbox link:
https://dl.dropboxusercontent.com/u/663533/work/BA_skeletton.rar

[suggestion, before you try that out, try the skeletton]
otherwise, download the following files for your system of preference:

jogl:
http://jogamp.org/deployment/autobuilds/master/jogl-b798-2012-08-26_03-34-17/

glugen:
http://jogamp.org/deployment/autobuilds/master/gluegen-b584-2012-08-26_03-30-46/

java3d j3d (binaries):
https://java3d.java.net/binary-builds.html

and do the following: extract them
create a filestructure like in the skeletton in the dropbox link or simply replace those files in the skeletton with your files.

**step 5**: download the project code from github:
visit this link: https://github.com/simplay/Bachelor-Thesis/tree/exp-245
or rather this in order to download the source:
https://github.com/simplay/Bachelor-Thesis/tree/exp-245

extract this zip file into your project folder from the skeletton you've downloaded in step 4.

**step 6**: start eclipse using ba/project as your path.
try to start the app

the precomputed texutures are are not in this github repository, but the necessary matlab code in order to precompute those images.
nevertheless im going to send you a link, when im at home with same precomputed images.
## Tools
+ Eclipse
  + IDE I used to write my Java and GLSL code. 
  
+ Octave
  + Matlab dialect used for writing all relevant Precomputation Scrips 

+ Textmate
  + IDE I used to write Tex 

+ Git
  + For versioning my code <3 

+ MINGW
  + windows binding in order to do fancy stuff  

+ Edraw Max
  + awesome tool I used to create most of the figures in my Thesis 

+ MeshLab
  + Obj File manipulation tool 

+ GIMP 2
  + I used this tool for postprocessing rendered images and defining in-sets. 

+ GoogleDocs
  + I used this google tool in oder to create my BA presentation 

+ Microsoft Paint :)
  + resizing/reformating various image files in an easy and simple way - just a few clicks. 

## Languages
+ Java
  + Rendering Program 

+ GLSL
  + OpenGL Shaders 

+ Matlab
  + Precomputation Scripts

+ Tex
  + Thesis Document 

## Precomputation:

## How to use the program:
###Eval setup
+ Blazed grating:

````E:\baData\fftBlazeHeight_0.25Microns\ E:\Projects\DiffractionShader\op\loadBPQ265Microns.m 1000 1 -55 -40 0.01 75 0 180````

+ Elaphe grating:

````E:\baData\fftElaphe65Microns\ E:\Projects\DiffractionShader\op\loadEPQ265Microns.m 666 1 -40 -15 0.01 75 0 180````

#License
```
The MIT License (MIT)

Copyright (c) <2014> <Michael Single (simplay)>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```
