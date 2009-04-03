/*
 * org.openmicroscopy.shoola.agents.chainbuilder.ui.ModuleTreeNode
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



 
package org.openmicroscopy.shoola.agents.chainbuilder.ui;

//Java imports
import javax.swing.tree.DefaultMutableTreeNode;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.OMEDataObject;
/** 
 * a {@link JTree} node for a tree of modules and categories.
 *
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */ 

public class ModuleTreeNode extends DefaultMutableTreeNode {
	
	public static final String UNCAT_NAME="Uncategorized";
	/** the name of the node */
	private String name;

	


	/**
	 * The OME Object for which we are building this.
	 *
	 */

	private OMEDataObject object = null;

	public ModuleTreeNode() {
		super();
	}

	public ModuleTreeNode(String s,int id) {
		super();
		name = s;
	}

	public ModuleTreeNode(String s) {
		super();
		name = s;
	}

	public ModuleTreeNode(OMEDataObject object) {
		this.object = object;
	}

	public int getID() {
		if (object != null)
			return object.getID();
		else
			return -1;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		if (object != null) 
			return object.getName();
		else
			return name;
	}

	public OMEDataObject getObject() {
		return object;
	}
}	
	