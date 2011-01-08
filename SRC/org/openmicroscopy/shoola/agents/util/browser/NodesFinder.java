/*
 * org.openmicroscopy.shoola.agents.util.browser.NodesFinder 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.util.browser;


//Java imports
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import pojos.DataObject;

/** 
 * Finds the nodes corresponding the specified type and the identifier.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class NodesFinder 
	implements TreeImageDisplayVisitor
{

	/** The identifier of the data object to find. */
	private List<Long> ids;
	
	/** The type of object to handle. */
	private Class type;
	
    /** Set of <code>TreeImageDisplay</code>s */
    private Set<TreeImageDisplay> nodes;
    
    /** The collection of nodes to find. */
    private List<DataObject> refObjects;
    
	/**
	 * Checks if the node is of the desired type.
	 * 
	 * @param node The node to handle.
	 */
	private void findNode(TreeImageDisplay node)
	{
		Object userObject = node.getUserObject();
		if (refObjects != null && refObjects.size() > 0) {
			if (userObject != null) {
				Iterator<DataObject> i = refObjects.iterator();
				DataObject object;
				Class k = userObject.getClass();
				DataObject uo;
				while (i.hasNext()) {
					object = i.next();
					if (object.getClass().equals(k)) {
						uo = (DataObject) userObject;
						if (uo.getId() == object.getId()) {
							nodes.add(node);
							break;
						}
					}
				}
			}
		} else {
			if (userObject != null && userObject.getClass().equals(type)) {
				if (userObject instanceof DataObject) {
					DataObject data = (DataObject) userObject;
					if (ids.contains(data.getId())) nodes.add(node);
				}
			} 	
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param type The type to data object.
	 * @param ids   The identifiers of the data object.
	 */
	public NodesFinder(Class type, List<Long> id)
	{
		this.type = type;
		this.ids = id;
		nodes = new HashSet<TreeImageDisplay>();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param type The type to data object.
	 * @param id   The identifier of the data object.
	 */
	public NodesFinder(Class type, long id)
	{
		this.type = type;
		ids = new ArrayList<Long>(1);
		ids.add(id);
		nodes = new HashSet<TreeImageDisplay>();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param refObjects The collection of objects to find.
	 */
	public NodesFinder(List<DataObject> refObjects)
	{
		type = null;
		this.refObjects = refObjects;
		nodes = new HashSet<TreeImageDisplay>();
	}
	
	/**
	 * Returns the collection of nodes found.
	 * 
	 * @return See above.
	 */
	public Set<TreeImageDisplay> getNodes() { return nodes; }
	
	/**
	 * Finds the nodes.
	 * @see TreeImageDisplayVisitor#visit(TreeImageNode)
	 */
	public void visit(TreeImageNode node) { findNode(node); }

	/**
	 * Finds the nodes.
	 * @see TreeImageDisplayVisitor#visit(TreeImageSet)
	 */
	public void visit(TreeImageSet node) { findNode(node); }

}
