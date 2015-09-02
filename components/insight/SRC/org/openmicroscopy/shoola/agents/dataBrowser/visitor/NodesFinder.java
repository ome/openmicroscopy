/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser.visitor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageSet;
import omero.gateway.model.DataObject;

/** 
 * Finds for the {@link ImageDisplay}s corresponding to the selected
 * <code>DataObject</code>s.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class NodesFinder
    implements ImageDisplayVisitor
{

    /** The collection of found nodes. */
    private List<ImageDisplay> foundNodes;

    /** The collection of <code>DataObject</code>s to find. */
    private List<DataObject> nodes;

    /**
     * Checks if the passed node is contained in the list.
     *
     * @param node The node to handle.
     */
    private void findNode(ImageDisplay node)
    {
        Object o = node.getHierarchyObject();
        if (!(o instanceof DataObject)) return;
        if (nodes.isEmpty()) {
            foundNodes.add(node);
            return;
        }
        Iterator<DataObject> i = nodes.iterator();
        DataObject object;
        Class<?> k = o.getClass();
        long id = ((DataObject) o).getId();
        while (i.hasNext()) {
            object = i.next();
            if (k.equals(object.getClass()) && id == object.getId()) {
                foundNodes.add(node);
                break;
            }
        }
    }

    /** Creates a new instance.*/
    public NodesFinder()
    {
        this(new ArrayList<DataObject>());
    }

    /**
     * Creates a new instance.
     *
     * @param nodes The collection of <code>DataObject</code>s to find.
     */
    public NodesFinder(List<DataObject> nodes)
    {
        if (nodes == null)
            throw new IllegalArgumentException("No nodes to find.");
        this.nodes = nodes;
        foundNodes = new ArrayList<ImageDisplay>();
    }

    /**
     * Returns the collection of found nodes.
     *
     * @return See above.
     */
    public List<ImageDisplay> getFoundNodes() { return foundNodes; }

    /**
     * Implemented as specified by {@link ImageDisplayVisitor}.
     * @see ImageDisplayVisitor#visit(ImageNode)
     */
    public void visit(ImageNode node) { findNode(node); }

    /**
     * Implemented as specified by {@link ImageDisplayVisitor}.
     * @see ImageDisplayVisitor#visit(ImageSet)
     */
    public void visit(ImageSet node) { findNode(node); }

}
