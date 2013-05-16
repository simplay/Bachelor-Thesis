/**
 * note interface
 * whenever a getter wants to return a node object 
 * that does not exist then return null
 * 
 * @author Michael Single
 */

package SceneGraph;

import java.util.ArrayList;
import javax.vecmath.Matrix4f;
import jrtr.Shape;

public interface INode {
	public Matrix4f getTransformationMatrix();
	public Shape getShape();
	public ArrayList<INode> getChildren();
}
