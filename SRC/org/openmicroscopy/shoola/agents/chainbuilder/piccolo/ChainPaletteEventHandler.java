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
import java.awt.geom.Point2D;

//Third-party libraries
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.activities.PActivity.PActivityDelegate;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;


//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.SelectAnalysisChain;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.ui.Constants;

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
	
	/** how far zoomed in are we? */
	private int zoomInSteps = 0;
	
	
	public ChainPaletteEventHandler(ChainPaletteCanvas canvas,Registry registry) {
		super(canvas);
		this.registry = registry;
	}	
	
	
	public void zoomIn(PInputEvent e) {
		zoom(e,Constants.LARGE_SCALE_FACTOR);
		zoomInSteps++;
	}
	
	public void zoomOut(PInputEvent e) {
		if (zoomInSteps > 0) {		
			zoom(e,1/Constants.LARGE_SCALE_FACTOR);
			zoomInSteps--;
		}
			
	}
	
	public boolean isZoomedIntoChain() {
		return (zoomInSteps > 0);
	}
	
	public PActivity animateToNode(PNode node) {
		zoomInSteps = 0;
		PActivity act =  super.animateToNode(node);
		act.setDelegate(getActivityDelegate());
		return act;
	}
	
	private void zoom(PInputEvent e,double scale) {
		PCamera camera=canvas.getCamera();
		Point2D pos = e.getPosition();
		camera.scaleViewAboutPoint(scale,pos.getX(),pos.getY());
		lastBounds = camera.getViewBounds();
		((ChainPaletteCanvas) canvas).updateOverview();
	}
	
	private PActivityDelegate getActivityDelegate() {
		return new PActivityDelegate() {
			public void activityStarted(PActivity activity) {
			}
			public void activityStepped(PActivity activity) {
			}
			public void activityFinished(PActivity activity) {
				((ChainPaletteCanvas) canvas).hideOverview();
			}
		};
	}
	
	protected void unhighlightModules() {
		clearHighlights();
		lastEntered = null;
	}	
		
	public void setSelectedForDrag(PNode node) {
		if (node instanceof ChainView) {
			ChainView chain = (ChainView) node;
			setSelectedChain(chain);
		}
		else if (node instanceof PaletteModuleView) {
			PaletteModuleView mod = (PaletteModuleView) node;
			PaletteChainView chainView = mod.getChainViewParent();
			if (chainView != null);
				setSelectedChain(chainView);
		}
	}
	
	private void setSelectedChain(ChainView chain) {
		if (chain != null && chain.getChain() != null) {
			((ChainPaletteCanvas) canvas).setDraggingChain(chain.getChain());;
		}
		
	}
	
	public PActivity handleBackgroundClick() {
		PActivity act = super.handleBackgroundClick();
		act.setDelegate(getActivityDelegate());
		SelectAnalysisChain event = new SelectAnalysisChain(null);
		registry.getEventBus().post(event);
		return act;
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
}


