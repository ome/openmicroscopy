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

import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.ui.piccolo.BufferedObject;
import org.openmicroscopy.shoola.util.ui.piccolo.MouseableNode;

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
	
		if (!b.contains(pickedPos)) {
			super.mouseExited(e);
		}
	}
	
	public void mouseEntered(PInputEvent e) {
		PNode n = e.getPickedNode();
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
	
	/** 
	 * In the chain palette, we don't want to zoom into the chain itself,
	 * only in the box. So, if I've clicked on a chain view, pass the click 
	 * along to the parent.
	 */
	public void doMouseClicked(PInputEvent e) {
		PNode n = e.getPickedNode();
		if (!(n instanceof ChainView))
			super.doMouseClicked(e);
		else {
			//System.err.println("clicked on chain in chain palette..");
			
			
			// In ChainPaletteCanvas,
			// chain view has chain box as a grandparent. that's what we 
			// want to zoom to
			PNode parent = n.getParent();
			if (parent == null) 
				return;
			parent = parent.getParent();
			if (parent != null && parent instanceof MouseableNode) {
				((MouseableNode) parent).mouseClicked(this);		
				ChainPaletteCanvas c = (ChainPaletteCanvas) canvas;
				//System.err.println("after zoom, scale is "+c.getCamera().getViewScale());
			}
		}
		e.setHandled(true);
	}
	
	public void handlePopup(PInputEvent e) {
		PNode n = e.getPickedNode();	
		
		if (n instanceof ModuleView) {
			//	if we're in a module view, we go to the great-grandparent
			// -- the chain box.
			n = n.getParent();
			if (n != null) {
				n = n.getParent();
				if (n != null) { 
					n = n.getParent();
					if (n != null) {
						animateToNode(n);
					}
				}
			}
		}
		else if (n != null && n instanceof ChainView) {
			// if we're in a chain view, we want to zoom to canvas bounds
			animateToCanvasBounds();
		}
		else
			super.handlePopup(e);
		
		e.setHandled(true);
	}
	
}


