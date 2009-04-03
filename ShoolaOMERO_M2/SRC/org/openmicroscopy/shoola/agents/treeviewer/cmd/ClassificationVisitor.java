/*
 * org.openmicroscopy.shoola.agents.treeviewer.cmd.ClassificationVisitor
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageNode;
import pojos.CategoryData;
import pojos.ImageData;


/** 
 * Retrieves the nodes containing the specified {@link #image}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ClassificationVisitor
    extends BrowserVisitor
{

    /** The classified or declassified image. */
    private ImageData       image;
    
    /** 
     * Collection of {@link CategoryData}s the image was added to or removed
     * from.
     */
    private List            dataObjects;
    
    /** 
     * The parent hosting the {@link #image} if the parent is a 
     * {@link CategoryData} otherwise a node hosting the data object of the
     * same type and with the same id than the one hosting by the 
     * {@link #image}.
     */
    private List            foundNodes;
        
    /**
     * Creates a new instance.
     * 
     * @param model         Reference to the {@link Browser}.
     *                      Mustn't be <code>null</code>.
     * @param image         The classified or declassified image.
     *                      Mustn't be <code>null</code>.
     * @param categories    The categories the image was added to or 
     *                      removed from. Mustn't be <code>null</code>.
     */
    public ClassificationVisitor(Browser model, ImageData image, Set categories)
    {
        super(model);
        if (image == null) 
            throw new IllegalArgumentException("Image cannot be null");
        if (categories == null) 
            throw new IllegalArgumentException("Categories cannot be null");
        this.image = image;
        Iterator i = categories.iterator();
        dataObjects = new ArrayList(categories.size());
        CategoryData element;
        while (i.hasNext()) {
            element = (CategoryData) i.next();
            dataObjects.add(new Long(element.getId()));
        }
        foundNodes = new ArrayList();
    }
    
    /**
     * Returns the collection of found {@link TreeImageDisplay nodes}.
     * 
     * @return See above.
     */
    public List getFoundNodes() { return foundNodes; }
    
    /**
     * Retrieves the nodes hosting the {@link #image}
     * @see BrowserVisitor#visit(TreeImageNode)
     */
    public void visit(TreeImageNode node)
    { 
        ImageData o = (ImageData) node.getUserObject();
        if (o.getId() == image.getId()) {
            TreeImageDisplay display = node.getParentDisplay();
            Object p = display.getUserObject();
            if (p instanceof CategoryData) {
                long id = ((CategoryData) p).getId();
                if (dataObjects.contains(new Long(id)))
                    foundNodes.add(node);
            } else foundNodes.add(node);
        }
    }

}
