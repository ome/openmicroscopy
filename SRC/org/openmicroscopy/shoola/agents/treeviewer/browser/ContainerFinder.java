/*
 * org.openmicroscopy.shoola.agents.treeviewer.browser.ContainerFinder
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

package org.openmicroscopy.shoola.agents.treeviewer.browser;




//Java imports
import java.util.HashSet;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import pojos.DataObject;
import pojos.DatasetData;
import pojos.TagAnnotationData;

/** 
 * Finds the {@link TreeImageSet} representing {@link TagAnnotationData} or 
 * a {@link DatasetData}.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$Date: )
 * </small>
 * @since OME2.2
 */
public class ContainerFinder
	implements TreeImageDisplayVisitor
{

    /** Set of <code>TreeImageSet</code>s */
    private Set<TreeImageSet> containerNodes;
    
    /** Set of corresponding <code>DataObject</code>s */
    private Set<DataObject>   containers;
    
    /** The type of node to track.*/
    private Class             rootType;
    
    /** 
     * Creates a new instance. 
     * 
     * @param rootType The type of nodes to track.
     */
    public ContainerFinder(Class rootType)
    {
    	this.rootType = rootType;
        containerNodes = new HashSet<TreeImageSet>();
        containers = new HashSet<DataObject>();
    }
    
    /**
     * Returns the collection of found nodes.
     * 
     * @return See above.
     */
    public Set<TreeImageSet> getContainerNodes() { return containerNodes; }
    
    /**
     * Returns the collection of found <code>DataObject</code>s.
     * 
     * @return See above.
     */
    public Set<DataObject> getContainers() { return containers; }

    /** 
     * Required by the {@link TreeImageDisplayVisitor} I/F but no-op 
     * implementation in our case.
     * @see TreeImageDisplayVisitor#visit(TreeImageNode)
     */
    public void visit(TreeImageNode node) {}

    /** 
     * Implemented as specified by the {@link TreeImageDisplayVisitor} I/F.
     * @see TreeImageDisplayVisitor#visit(TreeImageSet)
     */
    public void visit(TreeImageSet node)
    {
        Object userObject = node.getUserObject();
        if (userObject != null && userObject.getClass().equals(rootType)) {
        	if (userObject instanceof DatasetData) {
                containerNodes.add(node); 
                containers.add((DataObject) userObject);
            } else if (userObject instanceof TagAnnotationData) {
            	TagAnnotationData tag = (TagAnnotationData) userObject;
            	String ns = tag.getNameSpace();
            	if (!TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns)) {
            		containerNodes.add(node); 
                    containers.add((DataObject) userObject);
            	}
            	/*
            	if (!node.isChildrenLoaded()) {
            		containerNodes.add(node); 
                    containers.add((DataObject) userObject);
            	}
            	*/
            }
        }
    }

}
