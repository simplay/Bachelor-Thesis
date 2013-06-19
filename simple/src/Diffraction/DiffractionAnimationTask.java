package Diffraction;

import java.util.TimerTask;

import jrtr.RenderPanel;

public class DiffractionAnimationTask extends TimerTask{
	private DiffractionSceneGraphFabricator dsgf;
	private RenderPanel renderPanel;
	private float tick = (float) (Math.PI/600);
	private float phi;
	
	public DiffractionAnimationTask(DiffractionSceneGraphFabricator dsgf, RenderPanel renderPanel){
		this.dsgf = dsgf;
		this.renderPanel = renderPanel;
		this.phi = 0.0f;
	}
	
	@Override
	public void run() {
//		Matrix4f currentDiffDiceMat = new Matrix4f(dsgf.getDiffDiceIMat());
//		currentDiffDiceMat.mul(dsgf.calculateDiffDiceGroup(phi));
//		dsgf.getDiffDiceGroup().setTransformationMatrix(currentDiffDiceMat);
//		
//		phi = phi + tick;
//		renderPanel.getCanvas().repaint();
	}

}
