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
import java.util.Iterator;

//Third-party libraries
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.PNode;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainFormalInputData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainFormalOutputData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainModuleData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainNodeExecutionData;
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
	
	private static final int MEX_GAP=1;
	
	private PNode nexNode = null;
	public PaletteModuleView() {
		super();
	}
	
	
	public PaletteModuleView(ChainModuleData module,float x,float y) {
		super(module,x,y);
		showDetails();
		labelNodes.setPickable(false);
	}
	
	 
	/**
	 * The main constructor 
	 * @param module The OME Module being represented
	 */
	public PaletteModuleView(AnalysisNodeData node,Collection nexes,int maxCount) {
		super((ChainModuleData) node.getModule());
		showDetails();
		labelNodes.setPickable(false);
		if (nexes != null && nexes.size() > 0)
			addNexes(nexes,maxCount);
	}
	
	/**
	 * Add the widgets for the individual mexes.
	 * @param mexes
	 */
	private void addNexes(Collection nexes,int maxCount) {
		
		// set up the mex node
		double x = 0;
		double y = 0;
		// get the width of the node thus far.
		float width = getBodyWidth();
		nexNode = new PNode();
		addChild(nexNode);
		
		// add the mexes.
		ChainNodeExecutionData nex;
		Iterator iter = nexes.iterator();
		NexView view;
		
		while (iter.hasNext()) {
			nex = (ChainNodeExecutionData) iter.next();
			view = new NexView(nex);
			nexNode.addChild(view);
			if (x + view.getWidth() > width) {
				// move to next row
				x =0;
				y += view.getHeight()+MEX_GAP;
			}
			view.setOffset(x,y);
			x += view.getWidth()+MEX_GAP;
		}
		// place the nexNode . remember, main overview/detail @ 0,0
		nexNode.setBounds(nexNode.getUnionOfChildrenBounds(null));
		nexNode.setOffset(0,-nexNode.getHeight());
		
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
		//((ChainPaletteEventHandler) handler).setLastEntered(null);
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
	