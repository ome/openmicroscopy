/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ChainPaletteEventHandler
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

//Third-party libraries
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.activities.PActivity.PActivityDelegate;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;


//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.SelectAnalysisChain;

import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.ui.piccolo.BufferedObject;

/** 
 * An event handler for a canvas containing {@link ModuleView} objects in the
 * chainpalette
 *
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */

public class ChainPaletteEventHandler extends ModuleNodeEventHandler  {

	/** the registry */
	private Registry registry;
	
	/** the last highlighted box..*/
	private ChainBox lastHighlighted = null;
	
	/** the last chain view that was expanded */
	private PaletteChainView lastChainView;
	
	public ChainPaletteEventHandler(ChainPaletteCanvas canvas,Registry registry) {
		super(canvas);
		this.registry = registry;
	}	
	
	
	public void mouseEntered(PInputEvent e) {
		
		PNode n = e.getPickedNode();
		if (n == null || (shouldHideLastChainView(n) &&
				!(n instanceof PCamera))) {
			hideLastChainView();
		}
		super.mouseEntered(e);
			
	}
	
	
	protected void unhighlightModules() {
		clearHighlights();
		lastEntered = null;
	}	
	
	protected void setLastEntered(PNode node) {
		if (node != null && shouldHideLastChainView(node)) {
			hideLastChainView();
		}
		if (node instanceof ChainBox) {
			ChainBox cb = (ChainBox)node;
			if (cb.getChainView() instanceof PaletteChainView)
				lastChainView = (PaletteChainView) cb.getChainView();
		}
		super.setLastEntered(node);
	}
	
	/**
	 * should we hide the last chain view when we enter this node?
	 */
	private boolean shouldHideLastChainView(PNode node) {
		boolean res = lastChainView != null
				&& lastChainView != node 
		        && !lastChainView.isAncestorOf(node) 
		        && !lastChainView.isDescendentOf(node);
		return res;
	}
	
	public void setSelectedForDrag(PNode node) {
		if (node instanceof ChainView) {
			ChainView chain = (ChainView) node;
			setSelectedChain(chain);
		}
		else if (node instanceof ModuleView) {
			ModuleView mod = (ModuleView) node;
			BufferedObject buf = mod.getEnclosingBufferedNode();
			if (buf instanceof ChainView)
				setSelectedChain((ChainView) buf);
		}
	}
	
	private void setSelectedChain(ChainView chain) {
		if (chain != null && chain.getChain() != null) {
			((ChainPaletteCanvas) canvas).setDraggingChain(chain.getChain());;
		}
		
	}
	
	public void handleBackgroundClick() {
		hideLastChainView();
		super.handleBackgroundClick();
		SelectAnalysisChain event = new SelectAnalysisChain(null);
		registry.getEventBus().post(event);
	}
	
	// called on entering/leaving chain box.
	
	public void setLastHighlighted(ChainBox box) {
		if (lastHighlighted != null)
			lastHighlighted.setHighlighted(false);
		ChainView chain = null;
		if (box != null)
			chain = box.getChainView();
		setLastEntered(chain);
		lastHighlighted = box;
	}
	
	/**
	 * The lastChainView that I clicked on
	 * called when I click on a compound...
	 * @param lastChainView 
	 */
	public void setLastChainView(PaletteChainView lastChainView) {
		// if I get this twice, show the full view.
		if (this.lastChainView == lastChainView) {
			PActivity a = lastChainView.showFullView(true);
			a.setDelegate(new PActivityDelegate() {
				public void activityStarted(PActivity activity) {
				}
				public void activityStepped(PActivity activity) {
				}
				public void activityFinished(PActivity activity) {
					animateToLastChainView();				
				}
			});
		}
		this.lastChainView = lastChainView;
	}
	
	private void animateToLastChainView() {
		if (lastChainView != null) {
			animateToBounds(lastChainView.getChainDetailBounds());
		}
	}
	
	public void hideLastChainView() {
		if (lastChainView != null) {	
			lastChainView.hide(); 
		}
		//lastChainView = null;
	}
}


