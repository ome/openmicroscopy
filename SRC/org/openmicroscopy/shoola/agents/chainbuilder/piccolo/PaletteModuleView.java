/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.PaletteModuleView;
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
import edu.umd.cs.piccolo.PNode;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainFormalInputData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainFormalOutputData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainModuleData;
import org.openmicroscopy.shoola.util.ui.piccolo.GenericEventHandler;



/** 
 * A Piccolo widget for an OME analysis  module, when found in a chain on the chain
 * palette. Contains alternative interaction code as appropriate for use on the
 * chain palette
 *
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */



public class PaletteModuleView extends SingleModuleView {
	
	public PaletteModuleView() {
		super();
	}
	
	
	public PaletteModuleView(ChainModuleData module,float x,float y) {
		super(module,x,y);
	}
	
	 
	/**
	 * The main constructor 
	 * @param module The OME Module being represented
	 */
	public PaletteModuleView(ChainModuleData module) {
		super(module);
	}
	
	
	// pass in delegates to formal inputs and outs,
	// in order to get my special purpose handling.
	protected FormalInput getFormalInput(ChainFormalInputData paramIn) {
		PaletteFormalParameterMouseDelegate delegate =
			new PaletteFormalParameterMouseDelegate();
		return new FormalInput(this,paramIn,delegate);
	}
	
	protected FormalOutput getFormalOutput(ChainFormalOutputData paramOut) {
		PaletteFormalParameterMouseDelegate delegate =
			new PaletteFormalParameterMouseDelegate();
		return new FormalOutput(this,paramOut,delegate);
	}
	
	public void mouseEntered(GenericEventHandler handler) {
		setAllHighlights(true);
		((ChainPaletteEventHandler) handler).setLastEntered(this);
		ChainBox cb = getChainBoxParent();
		if (cb != null)
			cb.mouseEntered(handler);
	}

	public void mouseExited(GenericEventHandler handler) {
		setAllHighlights(false);
		((ChainPaletteEventHandler) handler).setLastEntered(null);
	}
	
	private ChainBox getChainBoxParent() {
		PNode p = getParent();
		while (p != null) {
			if (p instanceof ChainBox)
				return (ChainBox) p;
			p = p.getParent();
		}
		return null;
	}

	private ChainView getChainViewParent() {
		PNode p = getParent();
		while (p != null) {
			if (p instanceof ChainView)
				return (ChainView) p;
			p = p.getParent();
		}
		return null;
	}
	protected LinkTarget getLinkTarget() {
		LinkTarget link = super.getLinkTarget();
		link.setPickable(false);
		return link;
	}
	
	public void mousePopup(GenericEventHandler handler) {
		ChainView view = getChainViewParent();
		((ChainPaletteEventHandler) handler).animateToNode(view);
		((ChainPaletteEventHandler) handler).setLastEntered(view);
		((ChainPaletteEventHandler) handler).hideLastChainView();
	}
	
	public void mouseClicked(GenericEventHandler handler) {
		((ChainPaletteEventHandler) handler).animateToNode(this);
		((ChainPaletteEventHandler) handler).setLastEntered(this);
	}
	
	
}
	