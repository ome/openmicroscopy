/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ChainCompoundModuleView;
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
import java.util.List;

//Third-party libraries
import edu.umd.cs.piccolo.PNode;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutChainData;
import org.openmicroscopy.shoola.util.ui.piccolo.GenericEventHandler;

/** 
 * A Piccolo widget for  a single, atomic OME analysis  module - as opposed to
 * compound modules or modules that summarize chains as black boxes.
 *
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */

public class ChainCompoundModuleView extends ModuleView {
	
	private LayoutChainData chain;
	
	private static final Integer MAX_IO_CARDINALITY = new Integer(10);
		
	public ChainCompoundModuleView() {
		super();
	}
	
	
	public ChainCompoundModuleView(LayoutChainData chain,float x,float y) {
		super(x,y);
		this.chain = chain;
		init();
	}
	
	/**
	 * The main constructor 
	 * @param module The OME Module being represented
	 */
	public ChainCompoundModuleView(LayoutChainData chain) {
		super();
		this.chain = chain;
		init();
	}
	
	public void setAllHighlights(boolean v) {
		super.setAllHighlights(v);
		setModulesHighlighted(v);
	}
	
	public String getName() {
		return chain.getName();
	}
	
	protected List getFormalInputs() {
		return chain.getUnboundInputs();
	}
	
	protected List getFormalOutputs() {
		return chain.getUnboundOutputs();
	}
	
	private PaletteChainView getChainView() {
		PNode parent=this;
		do {
			parent = parent.getParent();
			if (parent instanceof PaletteChainView) {
				return (PaletteChainView) parent;
			}
		} while (parent != null);
		return null;
	}
	
	private ChainBox getChainBox() {
		PNode parent=this;
		do {
			parent = parent.getParent();
			if (parent instanceof ChainBox) {
				return (ChainBox) parent;
			}
		} while (parent != null);
		return null;
	}
	
	public void mouseClicked(GenericEventHandler handler) {
		PaletteChainView chainView = getChainView();
		chainView.zoomToFullView(handler);
	}
	
	public void mousePopup(GenericEventHandler handler) {
		PNode p = getChainBox();
		if (p != null) {
			((ModuleNodeEventHandler) handler).animateToNode(p);
		}
	}
	
	public void mouseEntered(GenericEventHandler handler) {
		ChainPaletteEventHandler modHandler = (ChainPaletteEventHandler) handler;
		showDetails();
		moveUp();
		super.mouseEntered(handler);
	}

	public void mouseExited(GenericEventHandler handler) {
		showOverview();
		super.mouseExited(handler);
	}
	
	public Integer getMaxIOCardinality() {
		return MAX_IO_CARDINALITY;
	}
}