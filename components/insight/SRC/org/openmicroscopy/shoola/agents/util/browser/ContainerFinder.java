/*
 * org.openmicroscopy.shoola.agents.util.browser.ContainerFinder
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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.util.browser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.TagAnnotationData;

/** 
 * Finds the {@link TreeImageSet} representing {@link TagAnnotationData},
 * {@link DatasetData}, {@link GroupData} or {@link ExperimenterData}.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
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
    private List<Class>             rootType;
    
    /** 
     * Creates a new instance. 
     * 
     * @param rootType The type of nodes to track.
     */
    public ContainerFinder(Class rootType)
    {
    	this.rootType = Arrays.asList(rootType);
        containerNodes = new HashSet<TreeImageSet>();
        containers = new HashSet<DataObject>();
    }
    
    /** 
     * Creates a new instance. 
     * 
     * @param rootType The type of nodes to track.
     */
    public ContainerFinder(List<Class> rootType)
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
        if (userObject != null && rootType.contains(userObject.getClass())) {
        	if (userObject instanceof DatasetData || 
        			userObject instanceof GroupData || 
        			userObject instanceof ExperimenterData) {
                containerNodes.add(node); 
                containers.add((DataObject) userObject);
            } else if (userObject instanceof TagAnnotationData) {
            	TagAnnotationData tag = (TagAnnotationData) userObject;
            	String ns = tag.getNameSpace();
            	if (!TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns)) {
            		containerNodes.add(node); 
                    containers.add((DataObject) userObject);
            	}
            }
        }
    }

}
