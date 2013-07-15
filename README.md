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
