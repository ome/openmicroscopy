/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.SingleModuleView;
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
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainModuleData;


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

public class SingleModuleView extends ModuleView {
	
	private ChainModuleData module;
	public SingleModuleView() {
		super();
	}
	
	
	public SingleModuleView(ChainModuleData module,float x,float y) {
		super(x,y);
		this.module = module;
		init();
	}
	
	/**
	 * The main constructor 
	 * @param module The OME Module being represented
	 */
	public SingleModuleView(ChainModuleData module) {
		super();
		this.module = module;
		init();
	}
	
	/**
	 * Set all of the {@link ModuleView} objects with the same OME {@link Module} 
	 * as this one to have the same highlighted state.
	 * @param v true if the modules should be highlighted, else false
	 */
	public void setModulesHighlighted(boolean v) {	
		module.setModulesHighlighted(v);
	}
	
	public void setAllHighlights(boolean v) {
		super.setAllHighlights(v);
		setModulesHighlighted(v);
	}
	
	public String getName() {
		return module.getName();
	}
	
	protected List getFormalInputs() {
		return module.getFormalInputs();
	}
	
	protected List getFormalOutputs() {
		return module.getFormalOutputs();
	}
	
	/**
	 * 
	 * @return the OME Module associated with this graphical display
	 */
	public ChainModuleData getModule() {
		return module;
	}
	
	
	/**
	 * to remove a {@link ModuleView}, remove all of its links,
	 * remove this widget from the list of widgets for the corresponding OME 
	 * Module, and remove this widget from the scenegraph
	 *
	 */
	public void remove() {
		// iterate over children of labelNodes
		Iterator iter = labelNodes.getChildrenIterator();
		
		FormalParameter p;
		while (iter.hasNext()) {
			p = (FormalParameter) iter.next();
			p.removeLinks();
		}
		module.removeModuleNode(this);
		removeFromParent();
	}
}