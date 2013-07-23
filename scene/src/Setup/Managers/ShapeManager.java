package Setup.Managers;
import java.io.IOException;

import javax.vecmath.Matrix4f;

import Constants.ShapeTask;
import Diffraction.DiffractionCylinder;
import Diffraction.DiffractionDice;
import Diffraction.DiffractionPlane2;
import ReadObjects.VertexFaceData;
import jrtr.Shape;

public class ShapeManager {
	private Shape targetShape;
	private ShapeTask task;
	private String snake_file = "../models/snake_test_piece.obj";
	private String teapot_file = "../models/Teapot_33K.obj";
	public ShapeManager(ShapeTask task){
		this.task = task;
		buildShape();
	}
	
	private void buildShape(){
		if(task == ShapeTask.PLANE){
			DiffractionPlane2 diffPlane = new DiffractionPlane2(300,2.0f,0.15f);
			this.targetShape = new Shape(diffPlane.getVertices());
			
		}else if(task == ShapeTask.PLANE2){
			DiffractionPlane2 diffPlane = new DiffractionPlane2(20,2.0f,0.15f);
			this.targetShape = new Shape(diffPlane.getVertices());
			
		}else if(task == ShapeTask.PLANE3){
			DiffractionPlane2 diffPlane = new DiffractionPlane2(20, (float) Math.PI, 10.0f);
			this.targetShape = new Shape(diffPlane.getVertices());	
			
		}else if(task == ShapeTask.DICE){
			DiffractionDice diffDice = new DiffractionDice(1.20f, 0.15f, 900, 50);
			this.targetShape = new Shape(diffDice.getVertices());
			
		}else if(task == ShapeTask.DICE2){
			DiffractionDice diffDice = new DiffractionDice(1.20f, 0.15f, 200, 2);
			this.targetShape = new Shape(diffDice.getVertices());
			
		}else if(task == ShapeTask.CYLINDER){
			DiffractionCylinder diffcylinder = new DiffractionCylinder(1.00f,0.015f, 600, 600);
			this.targetShape = new Shape(diffcylinder.getVertices());
			
		}else if(task == ShapeTask.CYLINDER2){
			
			DiffractionCylinder diffcylinder = new DiffractionCylinder(0.025f,1.0f, 600, 600);
			this.targetShape = new Shape(diffcylinder.getVertices());
			
		}else if(task == ShapeTask.CYLINDER3){
			DiffractionCylinder diffcylinder = new DiffractionCylinder(1.00f,1.0f, 600, 600);
			this.targetShape = new Shape(diffcylinder.getVertices());
			
		}else if(task == ShapeTask.TEAPOT){
			readExternalShape(teapot_file);
			
		}else if(task == ShapeTask.SNAKE){
			readExternalShape(snake_file);
		}
	}
	
	private void readExternalShape(String path){
	    VertexFaceData vd = null;
	    try {
			ReadObjects.ObjReader reader = new ReadObjects.ObjReader(path);
			vd = reader.getVFData();	
		} catch (IOException e) {
			e.printStackTrace();
		}
	    this.targetShape = new Shape(vd.getVetexData());
	}
	
	public Matrix4f getTransformation(){
		return targetShape.getTransformation();
	}
	
	public Shape getShape(){
		return this.targetShape;
	}
}
