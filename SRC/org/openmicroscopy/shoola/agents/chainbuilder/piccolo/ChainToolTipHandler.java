/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ChainToolTipHandler
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

//Third-party libraries
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;


/** 
 *
 * An event handler for tooltips on the {@link PChainCanvas} and
 * {@link PResultCanvas}.
 *  
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */
public class ChainToolTipHandler extends ModulePaletteToolTipHandler {
	
	
	public ChainToolTipHandler(PCamera camera) {
		super(camera);
	}
	
	/**
	 * The text of the tooltip is either the name of a module,
	 * the name of a parameter, or (if the mouse goes over a link) the name
	 * of the parameters at the ends of a link.
	 * 
	 * @param event the event that caused the update of the tool tip 
	 */
	public PNode setToolTipNode(PInputEvent event) {
		
		double scale = camera.getViewScale();
		if (scale >= SCALE_THRESHOLD)
			return null;
		
		PNode n = event.getInputManager().getMouseOver().getPickedNode();
			
		if (n instanceof ParamLink) {
			FormalInput in = ((ParamLink) n).getInput();
			FormalOutput out = ((ParamLink) n).getOutput();
			String s = in.getName()+"-"+out.getName();
			if (s.compareTo("") == 0)
				return null;
			else {
				PText p = new PText(s);
				p.setFont(font);
				return p;
			}
		}
		else
			return super.setToolTipNode(event);
	}
}
