/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.PaletteModuleView;
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
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
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.PNode;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainFormalInputData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainFormalOutputData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainModuleData;
import org.openmicroscopy.shoola.env.data.model.AnalysisNodeData;
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
	
	
	
	private PNode nexNode = null;
	
	private AnalysisNodeData chainNode;
	
	public PaletteModuleView() {
		super();
	}
	
	
	public PaletteModuleView(ChainModuleData module,float x,float y) {
		super(module,x,y);
		showDetails();
		labelNodes.setPickable(false);
	}
	
	 
	public PaletteModuleView(AnalysisNodeData node) {
		super((ChainModuleData) node.getModule());
		this.chainNode = node;
		showDetails();
		labelNodes.setPickable(false);
	}
	
	/**
	 * The main constructor 
	 * @param module The OME Module being represented
	 */
	public PaletteModuleView(AnalysisNodeData node,Collection nexes,int maxCount) {
		this(node);
		addNexes(nexes,maxCount);
	}
	
	private void addNexes(Collection nexes,int maxCount) {
		if (nexes != null && nexes.size() > 0) {
			NexSetView setView = new NexSetView(nexes,getBodyWidth(),maxCount);
			addChild(setView);
			setView.setPosition();
			//setView.setOffset(0,-setView.getHeight());
		}
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
	
	

	public void mouseExited(GenericEventHandler handler) {
		setAllHighlights(false);
	}
	
	public ChainBox getChainBoxParent() {
		PNode p = getParent();
		while (p != null) {
			if (p instanceof ChainBox)
				return (ChainBox) p;
			p = p.getParent();
		}
		return null;
	}

	public PaletteChainView getChainViewParent() {
		PNode p = getParent();
		while (p != null) {
			if (p instanceof PaletteChainView)
				return (PaletteChainView) p;
			p = p.getParent();
		}
		return null;
	}
	protected ModuleLinkTarget getLinkTarget() {
		ModuleLinkTarget link = super.getLinkTarget();
		link.setPickable(false);
		return link;
	}
	
	// in chain palette, always show details
	public void showOverview() {
		
	}
	
	public void mousePopup(GenericEventHandler handler,PInputEvent e) {
		ChainBox cb = getChainBoxParent();
		if (cb != null)
			cb.mousePopup(handler,e);
	}
	
	public void mouseClicked(GenericEventHandler handler,PInputEvent e) {
		ChainBox cb = getChainBoxParent();
		if (cb != null)
			cb.mouseClicked(handler,e);
	}
	
	public void mouseEntered(GenericEventHandler handler,PInputEvent e) {
		setAllHighlights(true);
		((ChainPaletteEventHandler) handler).setLastEntered(this);
	}
	
}
	