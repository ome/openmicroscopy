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

//Third-party libraries
import edu.umd.cs.piccolo.PNode;


//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.SelectAnalysisChain;

import org.openmicroscopy.shoola.env.config.Registry;

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
	
	
	protected void unhighlightModules() {
		clearHighlights();
		lastEntered = null;
	}	
		
	public void setSelectedForDrag(PNode node) {
		if (node instanceof ChainView) {
			ChainView chain = (ChainView) node;
			setSelectedChain(chain);
		}
		else if (node instanceof PaletteModuleView) {
			PaletteModuleView mod = (PaletteModuleView) node;
			PaletteChainView chainView = mod.getChainViewParent();
			if (chainView != null);
				setSelectedChain(chainView);
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
	
	// called on entering/leaving chain box.
	
	public void setLastHighlighted(ChainBox box) {
		if (lastHighlighted != null)
			lastHighlighted.setHighlighted(false);
		ChainView chain = null;
		if (box != null)
			chain = box.getChainView();
		setLastEntered(chain);
		lastHighlighted = box;
	}
}


