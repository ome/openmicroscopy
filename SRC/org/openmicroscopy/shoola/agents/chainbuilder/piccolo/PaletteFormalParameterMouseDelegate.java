/*
* org.openmicroscopy.shoola.agents.chainbuilder.piccolo.
* 	PaletteFormalParameterMouseDelegate
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

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.piccolo.GenericEventHandler;

import edu.umd.cs.piccolo.PNode;

/** 
* A delegate to handle mouse events for formal parameter objects
* on {@ ModuleView} widgets on a {@ ChainPaletteCanvas}
*
* @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
*
* @version 2.2
* <small>
* (<b>Internal version:</b> $Revision$ $Date$)
* </small>
*/


public class PaletteFormalParameterMouseDelegate extends  
	FormalParameterMouseDelegate  {
	
	public PaletteFormalParameterMouseDelegate() {
		super();
	}
	
	public void mouseEntered(GenericEventHandler handler) {
		super.mouseEntered(handler);
		ChainBox cb = getChainBoxParent();
		if (cb != null)
			cb.mouseEntered(handler);
	}

	public void mouseExited(GenericEventHandler handler) {
		super.mouseExited(handler);
		ChainBox cb = getChainBoxParent();
		if (cb != null)
			cb.mouseExited(handler);
	}

	
	private ChainBox getChainBoxParent() {
		PNode p = param.getParent();
		while (p != null) {
			if (p instanceof ChainBox)
				return (ChainBox) p;
			p = p.getParent();
		}
		return null;
	}
}