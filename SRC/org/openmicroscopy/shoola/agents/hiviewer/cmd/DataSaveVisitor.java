/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.DataSaveVisitor 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.hiviewer.cmd;


//Java imports
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageSet;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class DataSaveVisitor 
	extends HiViewerVisitor
{

	/** Helper reference. */
	private IconManager icons;
	
	/** Collection of nodes to update. */
	private List 		nodes;
	
    /**
     * Creates a new instance.
     * 
     * @param model	Reference to the model. Mustn't be <code>null</code>.
     * @param nodes	Collection of nodes to update.
     */
	public DataSaveVisitor(HiViewer model, List nodes)
	{
		super(model);
		this.nodes = nodes;
		icons = IconManager.getInstance();
	}
	
	/**
     * Sets the decoration of the node.
     * @see ImageDisplayVisitor#visit(ImageNode)
     */
    public void visit(ImageNode node)
    {
        long id = ((ImageData) node.getHierarchyObject()).getId();
        Iterator i = nodes.iterator();
        DataObject object;
        while (i.hasNext()) {
        	object = (DataObject) i.next();
        	if (object instanceof ImageData) {
        		if (object.getId() == id) {
        			node.setHierarchyObject(object);
        			node.setNodeDecoration();
        			node.repaint();
        		}
        	}
		}
    }

    /**
     * No implementation because a {@link ImageSet} is not decorated.
     * @see ImageDisplayVisitor#visit(ImageSet)
     */
    public void visit(ImageSet node)
    {
    	Object uo = node.getHierarchyObject();
    	if (!(uo instanceof DataObject)) return;
    	Iterator i = nodes.iterator();
        DataObject object;
        long id = ((DataObject) uo).getId();
        while (i.hasNext()) {
        	object = (DataObject) i.next();
        	if (object instanceof ImageData) {
        		if (object.getId() == id) {
        			node.setHierarchyObject(object);
        			if (uo instanceof DatasetData) {
        	            if (node.isAnnotated())
        	                node.setFrameIcon(
        	                	icons.getIcon(IconManager.ANNOTATED_DATASET));
        	            else 
        	                node.setFrameIcon(
        	                		icons.getIcon(IconManager.DATASET));
        			}
        		}
        	}
		}
    }
	
}
