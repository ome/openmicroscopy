/*
 * org.openmicroscopy.shoola.agents.zoombrowser.piccolo.ToolTipHandler
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
 
package org.openmicroscopy.shoola.util.ui.piccolo;

//Java imports
import java.awt.geom.Point2D;

import org.openmicroscopy.shoola.util.ui.Constants;


//Third-party libraries 
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;

/** 
 *
 * An event handler for tooltips. Borrows heavily from the code
 * in the Piccolo example files. 
 *  
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */

public abstract class ToolTipHandler extends PBasicInputEventHandler {

	/**
	 * if the scale exceeds SCALE_THRESHOLD, the tooltip might be distracting,
	 *  as the object is already large. Therefore, we don't show it.
	 * 
	 */
	protected static double 	SCALE_THRESHOLD=1.0;
	protected PCamera camera;
	
	
	
	
	
	protected PPath tooltip;
	protected PNode tip;
	
	protected boolean displayed = false;

	/**
	 * Initializes the tool tip handler
	 * @param camera the camera that will display the tooltip
	 */
	public ToolTipHandler(PCamera camera) {
		this.camera = camera;
		tooltip = new PPath();
		tooltip.setPaint(Constants.TOOLTIP_FILL_COLOR);
		tooltip.setStrokePaint(Constants.TOOLTIP_BORDER_COLOR);
		tooltip.setPickable(false);
	}
	
	
	/**
	 * update the tooltip when the mouse is moved
	 * 
	 
	 */
	public void mouseMoved(PInputEvent event) {
		updateToolTip(event);
	}
	
	/**
	 * also update when the mouse is dragged
	 *
	 * * @param event the mouse event 
	 */
	public void mouseDragged(PInputEvent event) {
		updateToolTip(event);
	}
		
	/**
	 * Update the tooltip text and position it, with an
	 * offset appropraite for the display.
	 * 
	 * @param event the input event leading to the update. 
	 */
	public void updateToolTip(PInputEvent event) {
		PNode n  = setToolTipNode(event);

		setToolTip(n);
			
		Point2D p = event.getCanvasPosition();
		
			//eseentially, this converts p to
			//camera coordinates.
			// layers can be in the way, so we
				//can't go localToGlobal.
		event.getPath().canvasToLocal(p, camera);
		tooltip.setOffset(p.getX() + 8, p.getY() - 8);
	}
	
	/**
	 * Called when the tool tip must be updated, this procedure generally
	 * chooses the appropriate text and then calls {@link setToolTipString}
	 * 
	 * @param event the input event leading to the change
	 */
	public abstract PNode  setToolTipNode(PInputEvent event);
	
	/**
	 * Set the tool tip text - if the new string is null,
	 * remove the node from the camera's scenegraph. Otherwise,
	 * set the text and adjust the position
	 * @param s
	 */
	public void setToolTip(PNode n) {
	
		if (n == tip)
			return;
			
		if (tip != null) {
			tooltip.removeChild(tip);
		}
		if (n == null) {
			if (displayed == true) {
				camera.removeChild(tooltip);
				tip = null;
			}
			displayed = false;   
		}
		else {
			tip = n;
			if (displayed == false)
				camera.addChild(tooltip);
			tooltip.addChild(n);
			n.setPickable(false);
			n.moveToFront();
			displayed = true;
			PBounds newBounds = new PBounds(0,0,n.getWidth()*n.getScale(),
				n.getHeight()*n.getScale());
			tooltip.setPathTo(newBounds);
		}	
	}
	
}

