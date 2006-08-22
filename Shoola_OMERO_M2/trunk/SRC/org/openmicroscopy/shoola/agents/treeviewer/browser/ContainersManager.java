/*
 * org.openmicroscopy.shoola.agents.treeviewer.browser.ContainersManager
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

package org.openmicroscopy.shoola.agents.treeviewer.browser;



//Java imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageSet;
import pojos.CategoryData;
import pojos.DatasetData;

/** 
 * Manages the process of assigning the number of items to 
 * {@link TreeImageSet}s whose userObject is <code>Dataset</code> or 
 * <code>CategoryGroup</code>, in a visualization tree. 
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$Date: )
 * </small>
 * @since OME2.2
 */
public class ContainersManager
{
    
    /** How many different Images' containers we have. */
    private int     totalIDs;

    /** Ids of the containers whose number of items has already been set. */
    private Set     processedIDs;
    
    private Map		providers;
    
    /** The tree hosting the display. */
    private JTree	tree;
    
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
            if (userObject instanceof DatasetData) 
                id = new Long(((DatasetData) userObject).getId());
            else if (userObject instanceof CategoryData) 
                id = new Long(((CategoryData) userObject).getId());
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
    public void setNumberItems(long containerID, int value)
    {
        if (value < 0) throw new IllegalArgumentException("Value not valid.");
        Long id = new Long(containerID);
        Set p = (Set) providers.get(id);
        if (p != null) {
            Iterator i = p.iterator();
            TreeImageSet node;
            DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();
            while (i.hasNext()) {
                node = (TreeImageSet) i.next();
                node.setNumberItems(value);
                dtm.reload(node);
            }    
            processedIDs.add(id);
        } 
    }
    
    /**
     * Tells if every {@link TreeImageSet}, containing Images,
     * in the visualization tree has been assigned a value.
     * 
     * @return <code>true</code> for yes, <code>false</code> for no.
     */
    public boolean isDone() { return (processedIDs.size() == totalIDs); }
    
}
