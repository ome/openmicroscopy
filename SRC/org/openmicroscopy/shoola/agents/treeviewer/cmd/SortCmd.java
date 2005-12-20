/*
 * org.openmicroscopy.shoola.agents.treeviewer.cmd.SortCmd
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

package org.openmicroscopy.shoola.agents.treeviewer.cmd;




//Java imports
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplayVisitor;
import pojos.ImageData;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class SortCmd
    implements ActionCmd
{

    /** Indicates to sort the nodes by name. */
    public static final int SORT = 0;
    
    /** Indicates to sort the nodes by date. */
    public static final int SORT_DATE = 1;
    
    /** Reference to the model. */
    private Browser model;
    
    /** The sorting type. One of the constants defined by this class. */
    private int     sortType;
    
    /**
     * Checks if the specified type is one of the constants defined by this 
     * class.
     * 
     * @param type The type to control.
     */
    private void checkSortType(int type)
    {
        switch (type) {
            case SORT:
            case SORT_DATE:    
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
            case SORT_DATE:
                c = new Comparator() {
                    public int compare(Object o1, Object o2)
                    {
                        ImageData i1 = (ImageData) 
                                      (((TreeImageDisplay) o1).getUserObject());
                        ImageData i2 = (ImageData) 
                                    (((TreeImageDisplay) o2).getUserObject());
                        
                        int r = i1.getInserted().compareTo(i2.getInserted());
                        int v = 0;
                        if (r < 0) v = -1;
                        else if (r > 0) v = 1;
                        if (ascending) return v;
                        return -v;
                    }
                };
                break;
            case SORT:
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
     * @param model Reference to the model. Mustn't be <code>null</code>.
     * @param sortType One of the constants defined by this class.
     */
    public SortCmd(Browser model, int sortType)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        this.model = model;
        checkSortType(sortType);
        this.sortType = sortType;
    }
    
    /** Implemented as specified by {@link ActionCmd}. */
    public void execute()
    {
        SortVisitor visitor = new SortVisitor(model);
        model.accept(visitor, TreeImageDisplayVisitor.TREEIMAGE_NODE_ONLY);
        List sortedNodes = sort(visitor.getNodes(), true);
        model.setSortedNodes(sortedNodes);
    }

}
