/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ModuleNodeEventHandler
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
import java.awt.geom.Point2D;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainModuleData;
import org.openmicroscopy.shoola.util.ui.piccolo.BufferedCanvas;
import org.openmicroscopy.shoola.util.ui.piccolo.GenericZoomEventHandler;
import org.openmicroscopy.shoola.util.ui.piccolo.ModuleView;



/** 
 * An event handler for a canvas containing {@link ModuleView} objects 
 *
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */

public abstract class ModuleNodeEventHandler extends GenericZoomEventHandler  {
	
	protected ModuleView lastEntered;
	
	public ModuleNodeEventHandler(BufferedCanvas canvas) {
		super(canvas);
	}	
	
	public void mouseEntered(PInputEvent e) {
		
		PNode n = e.getPickedNode();
		if (lastEntered != null && !lastEntered.isAncestorOf(n))
			unhighlightModules();
		n = n.getParent();
		
		super.mouseEntered(e);
	}
	
	/**
	 * check to see if I've actually gone "outside" of the box (in
	 * screen terms), as opposed to simply going inside of another node that is
	 * physically surrounded by the chainbox.
	 */
	public void mouseExited(PInputEvent e) {
		PNode n = e.getPickedNode();
		if (!checkEventInNodeInterior(n,e)) {
			super.mouseExited(e);
		}
	}
	
	private boolean checkEventInNodeInterior(PNode node,PInputEvent e) {
		PBounds b = node.getBounds();
		// must do something in case node is not on picked path.
		try {
			Point2D pickedPos = e.getPositionRelativeTo(node);
			if (b.contains(pickedPos)) {
				return true;
			}
			else {
				return false;
			}
				
		} catch(Exception exc) {
			// if this pick did not contain the node,
			// then then event can't be in the node's interior
			return false;
		}
	}
	


	protected void clearHighlights() {
		
		if (lastEntered != null) {
			lastEntered.setParamsHighlighted(false);
			ChainModuleData mod = lastEntered.getModule();
			if (mod != null)
				mod.setModulesHighlighted(false);
		}
	}
	
	protected void unhighlightModules() {
		clearHighlights();
		if (lastEntered != null)
			lastEntered.showOverview();
		lastEntered = null;
	}	
	
	public void setLastEntered(PNode node) {
		if (node instanceof ModuleView)
			this.lastEntered = (ModuleView) node;
		else if (node == null)
			this.lastEntered = null;
		setSelectedForDrag(node);
	}
	
	public abstract void setSelectedForDrag(PNode node);
	
	protected void highlightModules(ChainModuleData mod) {		
		unhighlightModules();
		mod.setModulesHighlighted(true);
	}
	
	public void unhighlightModules(ChainModuleData mod) {
		mod.setModulesHighlighted(false);
	}
}
