/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.PaletteChainView
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
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainModuleData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutChainData;
import org.openmicroscopy.shoola.util.ui.piccolo.GenericEventHandler;

import edu.umd.cs.piccolo.PNode;


/** 
 * A view of chains to be used on a {@link ChainPaletteCanvas}
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */
public class PaletteChainView extends ChainView {
	
	public PaletteChainView(LayoutChainData chain) {
		super(chain);
	}	
	
	protected ModuleView getModuleView(ChainModuleData mod) {
		return new PaletteModuleView(mod);
	}
	
	protected ParamLink getParamLink(FormalInput inputPNode,
			FormalOutput outputPNode) {
		ParamLink pLink = super.getParamLink(inputPNode,outputPNode);
		pLink.setPickable(false);
		return pLink;
	}
	
	protected ModuleLink getModuleLink(LinkLayer linkLayer,ParamLink newLinkNode) {
		ModuleLink  mLink = super.getModuleLink(linkLayer,newLinkNode);
		mLink.setPickable(false);
		return mLink;
	}
	
	public void mouseEntered(GenericEventHandler handler) {
		super.mouseExited(handler);
		//((ModuleNodeEventHandler) handler).setLastEntered(this);
		ChainBox cb = getParentChainBox();
		if (cb != null)
			cb.mouseEntered(handler);
	}

	// let the grandparent handle the event. otherwise, clear last enetered.
	public void mouseExited(GenericEventHandler handler) {
		super.mouseExited(handler);
		//((ModuleNodeEventHandler) handler).setLastEntered(null);
		ChainBox cb = getParentChainBox();
		if (cb != null)
			cb.mouseExited(handler);
	
	}
	
	//	 if this chain is in a chainbox - which would then be the grandparent
	// return a chain box that is the enclosing grandparent
	private ChainBox getParentChainBox() {
		PNode parent = getParent();
		if (parent == null)
			return null;
		parent = parent.getParent();
		if (parent == null)
			return null;
		if (parent instanceof ChainBox)
			return ((ChainBox) parent);
		else 
			return null;
	}
}
