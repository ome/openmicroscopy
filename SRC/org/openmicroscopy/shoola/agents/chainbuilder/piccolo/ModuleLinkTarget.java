/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ModuleLinkTarget
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
import java.util.Collection;
 
//Third-party libraries
import edu.umd.cs.piccolo.PNode;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.Constants;

/** 
 * A Piccolo widget for a target for direct links between modules.
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */

public class ModuleLinkTarget extends LinkTarget {
 	
 	public ModuleLinkTarget() {
 		super();
 	}	
 	
 	public float getSize() {
 		return Constants.MODULE_LINK_TARGET_SIZE;
 	}
 	
 	public ModuleView getModuleView() {
 		PNode p  = getParent();
 		while (p !=null) {
 			if (p instanceof ModuleView)
 				return (ModuleView) p;
 			p = p.getParent();
 		}
 		return null;
 	}
 	public boolean isInputLinkTarget() {
 		ModuleView mod = getModuleView();
 		if (this == mod.getInputLinkTarget())
 			return true;
 		else
 			return false;
 	}
 	
 	public boolean isOutputLinkTarget() {
 		return !isInputLinkTarget();
 	}
 	
 	public Collection getParameters() {
 		ModuleView mod = getModuleView();
 		if (isInputLinkTarget())
 			return mod.getUnlinkedInputParameters();
 		else
 			return mod.getOutputParameters();
 	}
 	
 	public void setParametersHighlighted(boolean v) {
 		ModuleView mod = getModuleView();
 		mod.setLinkTargetHighlighted(this,v);
 	}
}


	