/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.treeviewer.browser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;
import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.GroupData;
import omero.gateway.model.PlateData;
import omero.gateway.model.TagAnnotationData;

/** 
 * Manages the process of assigning the number of items to 
 * {@link TreeImageSet}s whose userObject is a <code>Dataset</code>, 
 * <code>Group</code>, <code>Tag</code> or <code>Plate</code>
 * in a visualization tree. 
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public class ContainersManager
{
    
    /** How many different Images' containers we have. */
    private int		totalIDs;

    /** Ids of the containers whose number of items has already been set. */
    private Set		processedIDs;
    
    /** Keeps track the components. */
    private Map		providers;
    
    /** The tree hosting the display. */
    private JTree	tree;
    
    /**
     * Creates a new instance.
     * 
     * @param indexes The collection of indexes to handle.
     */
    public ContainersManager(Set indexes) 
    {
    	if (indexes == null)
            throw new IllegalArgumentException("No container nodes.");
    	Iterator i = indexes.iterator();
    	providers = new HashMap();
    	processedIDs = new HashSet();
    	Integer index;
    	while (i.hasNext()) {
			index = (Integer) i.next();
			providers.put(index, index);
		}
    	totalIDs = indexes.size();
    }
    
    /**
     * Creates a new instance.
     * 
     * @param tree				The component hosting the nodes.
     * @param containerNodes 	All the {@link TreeImageSet}s containing Images
     * 							in a given visualization tree. 
     * 							Mustn't be <code>null</code>.
     */
    public ContainersManager(JTree tree, Set containerNodes)
    {
        if (containerNodes == null)
            throw new IllegalArgumentException("No container nodes.");
        if (tree == null) throw new IllegalArgumentException("No tree.");
        this.tree = tree;
        totalIDs = 0;
        processedIDs = new HashSet();
        providers = new HashMap();
        Iterator i = containerNodes.iterator();
        TreeImageSet node;
        Object userObject;
        Set p;
        Long id = null;
        while (i.hasNext()) {
            node = (TreeImageSet) i.next();
            userObject = node.getUserObject();
            if (userObject instanceof DatasetData || 
            	userObject instanceof PlateData || 
            	userObject instanceof TagAnnotationData ||
            	userObject instanceof GroupData) 
            	id = Long.valueOf(((DataObject) userObject).getId());
            if (id != null) {
            	p = (Set) providers.get(id);
            	if (p == null) {
            		totalIDs++;
            		p = new HashSet();
            		providers.put(id, p);
            	}
            	p.add(node);
            }
        }
    }
    
    /**
     * Sets the number of items contained in the specified container.
     * 
     * @param containerID 	The ID of the container.
     * @param value			The number of items in the container.	
     */
    public void setNumberItems(long containerID, long value)
    {
        if (value < 0) throw new IllegalArgumentException("Value not valid.");
        Long id = Long.valueOf(containerID);
        Set p = (Set) providers.get(id);
        if (p != null) {
            Iterator i = p.iterator();
            TreeImageSet node;
            DefaultTreeModel dtm = null;
            if (tree != null)
            	dtm = (DefaultTreeModel) tree.getModel();
            while (i.hasNext()) {
                node = (TreeImageSet) i.next();
                node.setNumberItems(value);
                if (dtm != null && !node.isExpanded() &&
                		!node.isChildrenLoaded())
                		dtm.reload(node);
            }    
            processedIDs.add(id);
        } 
    }
    
    /**
     * Sets the item corresponding to the specified index.
     * 
     * @param index The index to handle.
     */
    public void setItem(int index)
    {
    	Integer i = (Integer) providers.get(index);
    	processedIDs.add(i);
    }
    
    /**
     * Tells if every {@link TreeImageSet}, containing Images,
     * in the visualization tree has been assigned a value.
     * 
     * @return <code>true</code> for yes, <code>false</code> for no.
     */
    public boolean isDone() { return (processedIDs.size() == totalIDs); }
    
}
