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
package org.openmicroscopy.shoola.agents.treeviewer.cmd;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplayVisitor;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import omero.gateway.model.ImageData;

/** 
 * Command to sort items in tree.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public class SortCmd
    implements ActionCmd
{
    
    /** Reference to the model. */
    private Browser             model;
    
    /** The sorting type. One of the constants defined by this class. */
    private int                 sortType;
    
    /** The list of sorted nodes. */
    private List                sortedNodes;
    
    /** The node whose children needed to be ordered. */
    private TreeImageDisplay    node;
    
    /**
     * Checks if the specified type is one of the constants defined by this 
     * class.
     * 
     * @param type The type to control.
     */
    private void checkSortType(int type)
    {
        switch (type) {
            case Browser.SORT_NODES_BY_DATE:
            case Browser.SORT_NODES_BY_NAME:    
                return;
            default:
                throw new IllegalArgumentException("Sort type not supported");
        }
    }
    
    /**
     * Sorts the specified collection in the specified order.
     * 
     * @param nodes The collection to sort.
     * @param ascending The order.
     * @return The sorted collection.
     */
    private List sort(List nodes, final boolean ascending)
    {
        Comparator c;
        switch (sortType) {
            case Browser.SORT_NODES_BY_DATE:
                c = new Comparator() {
                    public int compare(Object o1, Object o2)
                    {
                        ImageData i1 = (ImageData) 
                                      (((TreeImageDisplay) o1).getUserObject());
                        ImageData i2 = (ImageData) 
                                    (((TreeImageDisplay) o2).getUserObject());
                        Timestamp t1, t2;
                        try {
                            t1 = i1.getInserted();
                        } catch (Exception e) { 
                            t1 = null;
                        }
                        try {
                            t2 = i2.getInserted();
                        } catch (Exception e) {
                            t2 = null;
                        }
                        if (t1 == null)
                            t1 = UIUtilities.getDefaultTimestamp();
                        if (t2 == null)
                            t2 = UIUtilities.getDefaultTimestamp();
                        int r = t1.compareTo(t2);
                        int v = 0;
                        if (r < 0) v = -1;
                        else if (r > 0) v = 1;
                        if (ascending) return v;
                        return -v;
                    }
                };
                break;
            case Browser.SORT_NODES_BY_NAME:
            default:
                c = new Comparator() {
                    public int compare(Object o1, Object o2)
                    {
                        String s1 = o1.toString().toLowerCase();
                        String s2 = o2.toString().toLowerCase();
                        int result = s1.compareTo(s2);
                        int v = 0;
                        if (result < 0) v = -1;
                        else if (result > 0) v = 1;
                        if (ascending) return v;
                        return -v;
                    }
                };
        }
        Collections.sort(nodes, c);
        return nodes;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model     Reference to the model. Mustn't be <code>null</code>.
     * @param sortType  One of the constants defined by this class.
     * @param node      The node to sort. If <code>null</code>, 
     *                  we visit the tree.
     */
    public SortCmd(Browser model, int sortType, TreeImageDisplay node)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        this.model = model;
        checkSortType(sortType);
        this.sortType = sortType;
        this.node = node;
    }
    
    /** 
     * Returns the list of sorted nodes or <code>null</code> if the command
     * has been executed yet.
     * 
     * @return See above.
     */
    public List getSortedNodes() { return sortedNodes; }
    
    /** Implemented as specified by {@link ActionCmd}. */
    public void execute()
    {
        SortVisitor visitor = new SortVisitor(model);
        if (node == null)
            model.accept(visitor, TreeImageDisplayVisitor.TREEIMAGE_NODE_ONLY);
        else node.accept(visitor, TreeImageDisplayVisitor.TREEIMAGE_NODE_ONLY);
        sortedNodes = sort(visitor.getNodes(), true);
    }

}
