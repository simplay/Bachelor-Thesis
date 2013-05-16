package SceneGraph;



import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import jrtr.Camera;
import jrtr.Frustum;

public class FrustumPoints {

	private float aspectRation;
	private float farPlane;
	private float nearPlane;
	private float vertFOV;

	private Vector3f look;
	private Vector3f cop;
	private Vector3f up;

	public FrustumPoints(Frustum frustum, Camera camera) {
		this.aspectRation = frustum.getAspectRatio();
		this.farPlane = frustum.getFarPlane();
		this.nearPlane = frustum.getNearPlane();
		this.vertFOV = frustum.getVertFOV();

		this.up = camera.getUpVector();

		initNewLook(camera);
		initNewCoop(camera);
	}

	public ArrayList<Vector3f> getPoints() {
		ArrayList<Vector3f> points = new ArrayList<Vector3f>();

		look.normalize();
		up.normalize();

		points.addAll(calculateNearPlanePoints());
		points.addAll(calculateFarPlanePoints());

		return points;
	}

	private void initNewLook(Camera camera) {
		Point3f look = camera.getLookAtPointPoint();
		this.look = new Vector3f(look.x, look.y, look.z);
	}

	private void initNewCoop(Camera camera) {
		Point3f cop = camera.getProjectionCenterPoint();
		this.cop = new Vector3f(cop.x, cop.y, cop.z);
	}

	private Vector3f nearPlaneCenter() {
		Vector3f temp = new Vector3f();
		temp.scale(nearPlane, look);
		Vector3f nc = new Vector3f(cop.x + temp.x, cop.y + temp.y, cop.z
				+ temp.z);
		return nc;
	}

	private Vector3f farPlaneCenter() {
		Vector3f temp = new Vector3f();
		temp.scale(farPlane, look);
		Vector3f fc = new Vector3f(cop.x + temp.x, cop.y + temp.y, cop.z
				+ temp.z);
		return fc;
	}

	private float hNear() {
		return (float) (nearPlane * Math.tan(vertFOV / 2));
	}

	private float hFar() {
		return (float) (farPlane * Math.tan(vertFOV));
	}

	private float wNear() {
		return hNear() * aspectRation;
	}

	private float wFar() {
		return hFar() * aspectRation;
	}

	private Vector3f right() {
		Vector3f right = new Vector3f();
		right.cross(up, look);
		right.negate();
		right.normalize();
		return right;
	}

	private Vector3f hNearUp() {
		Vector3f hNearUp = new Vector3f(up.x, up.y, up.z);
		hNearUp.scale(hNear());
		return hNearUp;
	}

	private Vector3f hFarUp() {
		Vector3f hFarUp = new Vector3f(up.x, up.y, up.z);
		hFarUp.scale(hFar());
		return hFarUp;
	}

	private Vector3f wNearRight() {
		Vector3f right = right();
		Vector3f wNearRight = new Vector3f(right.x, right.y, right.z);
		wNearRight.scale(wNear());
		return wNearRight;
	}

	private Vector3f wFarRight() {
		Vector3f right = right();
		Vector3f wFarRight = new Vector3f(right.x, right.y, right.z);
		wFarRight.scale(wFar());
		return wFarRight;
	}

	private ArrayList<Vector3f> calculateNearPlanePoints() {
		ArrayList<Vector3f> points = new ArrayList<Vector3f>();

		Vector3f point1 = new Vector3f();
		point1.sub(nearPlaneCenter(), hNearUp());
		point1.add(wNearRight());
		points.add(point1);

		Vector3f point2 = new Vector3f();
		point2.add(nearPlaneCenter(), hNearUp());
		point2.add(wNearRight());
		points.add(point2);

		Vector3f point3 = new Vector3f();
		point3.add(nearPlaneCenter());
		point3.sub(wNearRight());
		points.add(point3);

		Vector3f point4 = new Vector3f();
		point4.sub(nearPlaneCenter(), farPlaneCenter());
		point4.sub(wNearRight());
		points.add(point4);

		return points;

	}

	private ArrayList<Vector3f> calculateFarPlanePoints() {
		ArrayList<Vector3f> points = new ArrayList<Vector3f>();

		Vector3f point5 = new Vector3f();
		point5.sub(farPlaneCenter(), hFarUp());
		point5.add(wFarRight());
		points.add(point5);

		Vector3f point6 = new Vector3f();
		point6.add(farPlaneCenter(), hFarUp());
		point6.add(wFarRight());
		points.add(point6);

		Vector3f point7 = new Vector3f();
		point7.add(farPlaneCenter(), hFarUp());
		point7.sub(wFarRight());
		points.add(point7);

		Vector3f point8 = new Vector3f();
		point8.sub(farPlaneCenter(), hFarUp());
		point8.sub(wFarRight());
		points.add(point8);

		return points;
	}

}
