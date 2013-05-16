package jrtr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * A simple scene manager that stores objects in a linked list.
 */
public class SimpleSceneManager implements ISceneManagerExtended {

	private LinkedList<Shape> shapes;
	private ArrayList<Light> lightSources;
	private Camera camera;
	private Frustum frustum;
	
	public SimpleSceneManager(){
		this.shapes = new LinkedList<Shape>();
		this.lightSources = new ArrayList<Light>();
		this.camera = new Camera();
		this.frustum = new Frustum();
	}
	
	@Override
	public Camera getCamera(){
		return camera;
	}
	
	@Override
	public Frustum getFrustum(){
		return frustum;
	}
	
	@Override
	public ArrayList<Light> getLightSources(){
		return this.lightSources;
	}
	
	public void addShape(Shape shape){
		shapes.add(shape);
	}
	
	public void addLightSource(Light lightSource){
		lightSources.add(lightSource);
	}
	
	@Override
	public SceneManagerIterator iterator(){
		return new SimpleSceneManagerItr(this);
	}
	
	@Override
	public LinkedList<Shape> getShapes() {
		return this.shapes;
	}

	private class SimpleSceneManagerItr implements SceneManagerIterator {
		ListIterator<Shape> itr;
		
		public SimpleSceneManagerItr(SimpleSceneManager sceneManager){
			itr = sceneManager.shapes.listIterator(0);
		}
		
		@Override
		public boolean hasNext(){
			return itr.hasNext();
		}
		
		@Override
		public RenderItem next(){
			Shape shape = itr.next();
			// Here the transformation in the RenderItem is simply the 
			// transformation matrix of the shape. More sophisticated 
			// scene managers will set the transformation for the 
			// RenderItem differently.
			return new RenderItem(shape, shape.getTransformation());
		}
	}

	@Override
	public Iterator<Light> lightIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void flushLightSources() {
		this.lightSources = new ArrayList<Light>();
		
	}


}
