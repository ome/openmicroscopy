/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ChainbuilderToolTipHandler
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
import java.awt.Font;

//Third-party libraries
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PPickPath;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.Constants;
import org.openmicroscopy.shoola.util.ui.piccolo.ToolTipHandler;

/** 
 *
 * An event handler for tooltips on the {@link PPaletteCanvas} and the
 * {@link ChainPaletteCanvas}
 *  
 * @author Harry Hochheiser
 * @version 2.1
 * @since OME2.1
 */
public class ChainbuilderToolTipHandler extends ToolTipHandler {
	
	protected Font font = Constants.TOOLTIP_FONT;
	protected Font descFont = Constants.SMALL_TOOLTIP_FONT;
	
	public ChainbuilderToolTipHandler(PCamera camera) {
		super(camera);
	}
	
	/**
	 * The toolTip String is either the name of the module that the mouse
	 * is on, or it is null. If the scale is too large 
	 * (exceeding {@link ToolTipHandler.SCALE_THRESHOLD),
	 * no tooltip is shown
	 * 
	 * @param event the input event that leads to the change.
	 */
	public PNode setToolTipNode(PInputEvent event) {
		PNode n = null;
		double scale = camera.getViewScale();
		if (scale < ToolTipHandler.SCALE_THRESHOLD) {
			n = getToolTipNode(event);
			if (n != null && n instanceof ToolTipNode) {
				ToolTipNode ttn = (ToolTipNode) n;
				return ttn.getToolTip();
			}
			else
				return null;
		}
		else 
			return null;
	}
	
	private PNode getToolTipNode(PInputEvent event) {
		PPickPath p = event.getInputManager().getMouseOver();
		
		PNode n = p.getPickedNode();
		System.err.println("picked...  "+n);
		System.err.println("transform scale is "+n.getTransform().getScale());
		return n;
	}
}