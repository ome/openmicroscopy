/*
* org.openmicroscopy.shoola.agents.chainbuilder.piccolo.FormalParameterMouseDelegate
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
import edu.umd.cs.piccolo.PNode;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.piccolo.GenericEventHandler;
import org.openmicroscopy.shoola.util.ui.piccolo.ModuleView;


/** 
* A delegate to handle mouse events for formal parameter objects
*  Needed because we need to have different behavior for {@FormalParameter}
* objects - depending on whether we're in a palette canvas or not.
*  Since {@FormalParameter} has subclasses, it wouldn't be easy to
*  subclass it directly. thus, the delegate.
*
* @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
*
* @version 2.2
* <small>
* (<b>Internal version:</b> $Revision$ $Date$)
* </small>
*/


public class FormalParameterMouseDelegate  {

	protected FormalParameter param = null;
	
	public FormalParameterMouseDelegate() {
		
	}
	
	public void setParam(FormalParameter param) {
		this.param = param;
	}
	

	public void mouseEntered(GenericEventHandler handler,PInputEvent e) {
		if (param == null) 
			return;
		ModuleView node = param.getModuleView();
		param.setParamsHighlighted(true);
		node.setModulesHighlighted(true);
		((ModuleNodeEventHandler) handler).setSelectedForDrag(node);
	}

	public void mouseExited(GenericEventHandler handler,PInputEvent e) {
		if (param == null) 
			return;
		param.setParamsHighlighted(false);
		ModuleView node = param.getModuleView();
		node.setAllHighlights(false);
		((ModuleNodeEventHandler) handler).setSelectedForDrag(node);
	}
	
	/** must go up to parent to tell where to click on - don't
	 *  want to click on parameter directly.
	 */
	public void mouseClicked(GenericEventHandler handler,PInputEvent e) {
		ModuleView module = getModuleView();
		module.mouseClicked(handler,e);
	}
	
	
	public void mousePopup(GenericEventHandler handler,PInputEvent e) {
		if (param == null)
			return;
	}
	
	private ModuleView getModuleView() {
		PNode parent = param.getParent();
		while (parent != null) {
			if (parent instanceof ModuleView)
				return (ModuleView) parent;
			parent = parent.getParent();
		}
		return null;
	}
}