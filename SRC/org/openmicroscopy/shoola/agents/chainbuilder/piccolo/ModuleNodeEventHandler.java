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
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainModuleData;
import org.openmicroscopy.shoola.util.ui.piccolo.BufferedObject;
import org.openmicroscopy.shoola.util.ui.piccolo.GenericZoomEventHandler;



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

public class ModuleNodeEventHandler extends GenericZoomEventHandler  {
	
	private ModuleView lastEntered;
	
	public ModuleNodeEventHandler(BufferedObject canvas) {
		super(canvas);
	}	
	
	public void mouseEntered(PInputEvent e) {
		unhighlightModules();
		super.mouseEntered(e);
	}

	protected void unhighlightModules() {
		if (lastEntered != null) { 
			lastEntered.setParamsHighlighted(false);
			ChainModuleData mod = lastEntered.getModule();
			mod.setModulesHighlighted(false);
		}
		lastEntered = null;
	}	
	
	protected void setLastEntered(PNode node) {
		if (node instanceof ModuleView)
			this.lastEntered = (ModuleView) node;
		setSelectedForDrag(node);
	}
	
	public void setSelectedForDrag(PNode node) {
	}
	
	protected void highlightModules(ChainModuleData mod) {		
		unhighlightModules();
		mod.setModulesHighlighted(true);
	}
	
	public void unhighlightModules(ChainModuleData mod) {
		mod.setModulesHighlighted(false);
	}
}
