/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.PaletteChainView
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */




/*------------------------------------------------------------------------------
 *
 * Written by:    Harry Hochheiser <hsh@nih.gov>
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.chainbuilder.piccolo;

//Java imports
import edu.umd.cs.piccolo.PLayer;

//Third-party libraries
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.activities.PInterpolatingActivity;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PUtil;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainModuleData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutChainData;
import org.openmicroscopy.shoola.agents.events.SelectAnalysisChain;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.ui.Constants;
import org.openmicroscopy.shoola.util.ui.piccolo.GenericEventHandler;

import edu.umd.cs.piccolo.PNode;


/** 
 * A view of chains to be used on a {@link ChainPaletteCanvas}
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */
public class PaletteChainView extends ChainView {
	
	private Registry registry;
 
	private ChainCompoundModuleView compoundView;
	
	private boolean showingFull = false;
	
	public PaletteChainView(LayoutChainData chain,Registry registry) {
		super(chain);
		this.registry =registry;
		//		 how can we set the full view to scale into module view?
		fullLayer.setVisible(false);
		
		compoundView = new  ChainCompoundModuleView(chain);
		addChild(compoundView);
		scaleFullView();
		setBounds(getUnionOfChildrenBounds(null));
		fullLayer.setPickable(false);
		fullLayer.setChildrenPickable(true);
	}	
	
	
	/** 
	 * Scale the view of the full chain to occupy the space in which
	 * the compound view was drawn - the chain box.
	 * To do this,
	 * 1) find the max aspect ratio difference (width or height)
	 * 2) scale the chain by that amount.
	 * 3) recenter.
	 *
	 */
	private void scaleFullView() {
		double compoundHeight = compoundView.getHeight();
		double compoundWidth = compoundView.getWidth();
		
		double fullHeight = fullLayer.getFullBounds().getHeight();
		double fullWidth = fullLayer.getFullBounds().getWidth();
		double widthRatio = compoundWidth/fullWidth;
		double heightRatio = compoundHeight/fullHeight;
		double newScale = heightRatio;
		if (widthRatio < heightRatio)
			newScale = widthRatio;
		fullLayer.setScale(newScale);
	}

	protected PLayer getFullView() {
		return fullLayer;
	}
	protected ModuleView getModuleView(ChainModuleData mod) {
		return new PaletteModuleView(mod);
	}
	
	protected PBounds getChainDetailBounds() {
		PBounds b= fullLayer.getGlobalFullBounds(); 
		return new PBounds(b.getX()-Constants.BORDER,b.getY()-Constants.BORDER,
				b.getWidth()+2*Constants.BORDER,b.getHeight()+2*Constants.BORDER);
	}
	
	protected ParamLink getParamLink(FormalInput inputPNode,
			FormalOutput outputPNode) {
		ParamLink pLink = super.getParamLink(inputPNode,outputPNode);
		pLink.setPickable(false);
		return pLink;
	}
	
	protected ModuleLink getModuleLink(LinkLayer linkLayer,ParamLink newLinkNode) {
		ModuleLink  mLink = super.getModuleLink(linkLayer,newLinkNode);
		mLink.setPickable(false);
		return mLink;
	}
	
	public void mouseEntered(GenericEventHandler handler) {
		((ChainPaletteEventHandler) handler).setLastEntered(this);
		ChainBox cb = getParentChainBox();
		if (cb != null)
			cb.mouseEntered(handler);
	}

	// do nothing here. let the chainbox handle it.
	public void mouseExited(GenericEventHandler handler) {
		((ChainPaletteEventHandler) handler).setLastEntered(null);
	}
	
	//	 if this chain is in a chainbox - which would then be the grandparent
	// return a chain box that is the enclosing grandparent
	private ChainBox getParentChainBox() {
		PNode parent = getParent();
		if (parent == null)
			return null;
		parent = parent.getParent();
		if (parent == null)
			return null;
		if (parent instanceof ChainBox)
			return ((ChainBox) parent);
		else 
			return null;
	}
	
	public void mouseClicked(GenericEventHandler handler) {
		ChainPaletteEventHandler chainHandler = (ChainPaletteEventHandler) handler;
		if (showingFull == true)
			chainHandler.animateToNode(fullLayer);
		else
			chainHandler.animateToNode(compoundView);
		chainHandler.setLastEntered(this);
		SelectAnalysisChain event = new SelectAnalysisChain(getChain());
		registry.getEventBus().post(event);
	}
	
	public void mousePopup(GenericEventHandler handler) {
		((ModuleNodeEventHandler) handler).animateToBounds(getChainDetailBounds());
		((ModuleNodeEventHandler) handler).setLastEntered(this);
	}	
	
	/**
	 * zoom to the full view 
	 *
	 */
	public void zoomToFullView(GenericEventHandler handler) {
		if (handler instanceof ModuleNodeEventHandler) 
			((ModuleNodeEventHandler) handler).animateToNode(this);
		
		if (handler instanceof ChainPaletteEventHandler) 
			((ChainPaletteEventHandler) handler).setLastChainView(this);
	}
	
	public PActivity showFullView(boolean b) {
		showingFull = b;
		TransparencyActivity a1 = null;
		// if true, making compound visible
		if (b ==true)  {
			compoundView.setPickable(false);
			a1 = new TransparencyActivity(fullLayer,compoundView);
			
		}
		else { // make full visible
			compoundView.setPickable(true);
			a1 = new TransparencyActivity(compoundView,fullLayer);
		}
		if (a1 != null)
			getRoot().getActivityScheduler().addActivity(a1);
		return a1;
	}
	
	public void hide() {
		showFullView(false);
		compoundView.showOverview();
	}
	

	private class TransparencyActivity extends PInterpolatingActivity {
		
		private float INVISIBLE=0.0f;
		private float VISIBLE=1.0f;
		private PNode p;
		private PNode q;
		private float trans;
		private float source;
		
		TransparencyActivity(PNode p,PNode q) {
			super(Constants.TRANSPARENCY_DELAY,PUtil.DEFAULT_ACTIVITY_STEP_RATE);
			this.p = p;
			this.q = q;
			source = 0.0f;
			trans = 1.0f;
		}
		
		protected void activityStarted() {
			source = p.getTransparency();
			p.setVisible(true);
			super.activityStarted();
		}
		
		public void setRelativeTargetValue(float zeroToOne) {
			float newTrans = source + (zeroToOne * (trans - source));
			p.setTransparency(newTrans);
			q.setTransparency(1-newTrans);
		}
		
		public void activityFinished() {
			q.setVisible(false);
			super.activityFinished();
		}
	}
	
	public double getHeight() { 
		return compoundView.getHeight();
	}
	
	public double getWidth() {
		return compoundView.getWidth();
	}
}
