package Diffraction;

public abstract class DiffractionGeometricObject extends GeometricObject2{
	protected float[] tangentVectors;	
	protected abstract float[] getTangentVectors();
}
