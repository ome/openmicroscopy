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
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;


//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.SelectAnalysisChain;
import org.openmicroscopy.shoola.agents.zoombrowser.piccolo.BufferedObject;

import org.openmicroscopy.shoola.env.config.Registry;

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
	
	public ChainPaletteEventHandler(ChainPaletteCanvas canvas,Registry registry) {
		super(canvas);
		this.registry = registry;
	}	
	
	
	public void mouseExited(PInputEvent e) {
		PNode n = e.getPickedNode();
		if (n instanceof ChainBox)
			checkExit((ChainBox) n,e);
		else 
			super.mouseExited(e);
		e.setHandled(true);
	}
	
	private void checkExit(ChainBox n,PInputEvent e) { 
		PBounds b = n.getBounds();
		Point2D pickedPos = e.getPositionRelativeTo(n);
		double pickX =  pickedPos.getX();
		double pickY =  pickedPos.getY();
		
		// it's only really an exit if I've gone outside of the bounds
		// of the chainbox. Otherwise, I'm over one of the chain box's components
		//if (pickX < 0 || pickX > b.getWidth() || pickY < 0 || pickY > 
		//			b.getHeight()) {
		if (!b.contains(pickedPos)) {
			super.mouseExited(e);
		}
	}
	
	public void mouseEntered(PInputEvent e) {
		PNode n = e.getPickedNode();
		System.err.println("entering..."+n);
		super.mouseEntered(e);
	}
	
	protected void setLastEntered(PNode node) {
		super.setLastEntered(node);
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
		super.handleBackgroundClick();
		SelectAnalysisChain event = new SelectAnalysisChain(null);
		registry.getEventBus().post(event);
	}
	
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


