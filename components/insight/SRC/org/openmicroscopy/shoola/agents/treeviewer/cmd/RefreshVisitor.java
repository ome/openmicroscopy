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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.util.browser.TreeFileSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageTimeSet;
import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.GroupData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.TagAnnotationData;


/** 
 * Retrieves the nodes containing images and whose children are loaded.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME2.2
 */
public class RefreshVisitor
    extends BrowserVisitor
{

    /** 
     * Collection of expanded {@link TreeImageSet}s corresponding
     * to a container whose children are images.
     */
    private List<Object>		foundNodes;
    
    /** Contains the expanded top container nodes ID. */
    private Map<Class<?>, List<Long>>  expandedTopNodes;
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the {@link Browser}.
     *              Mustn't be <code>null</code>.
     */
    public RefreshVisitor(Browser model)
    {
        super(model);
        foundNodes = new ArrayList<Object>();
        expandedTopNodes = new HashMap<Class<?>, List<Long>>();
    }

    /**
     * Returns the list of nodes found.
     * 
     * @return See above.
     */
    public List<Object> getFoundNodes() { return foundNodes; }

    /**
     * Returns the list of expanded top nodes IDs.
     * 
     * @return See above.
     */
    public Map<Class<?>, List<Long>> getExpandedTopNodes()
    {
    	return expandedTopNodes;
    }
    
    /**
     * Retrieves the expanded nodes. Only the nodes containing images
     * are taken into account.
     * @see BrowserVisitor#visit(TreeImageSet)
     */
    public void visit(TreeImageSet node)
    {
        Object userObject = node.getUserObject();
        TreeImageDisplay parent;
        node.setToRefresh(false);
        if (userObject instanceof DatasetData && node.isChildrenLoaded() 
        	&& node.isExpanded()) {
        	parent = node.getParentDisplay();
    		if (parent.isExpanded()) 
    			foundNodes.add(userObject);
    		if (!(parent.getUserObject() instanceof ProjectData)) {
    			long id = ((DataObject) userObject).getId();
                List<Long> l = expandedTopNodes.get(DatasetData.class);
                if (l == null) {
                	l = new ArrayList<Long>();
                	expandedTopNodes.put(DatasetData.class, l);
                }
                l.add(Long.valueOf(id));
    		}
        } else if ((userObject instanceof TagAnnotationData) 
        		&& node.isChildrenLoaded() && node.isExpanded()) {
        	parent = node.getParentDisplay();
        	TagAnnotationData tag = (TagAnnotationData) userObject;
        	String ns = tag.getNameSpace();
    		if (parent.isExpanded() &&
    			!TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns)) 
    			foundNodes.add(userObject);
    		if (!(parent.getUserObject() instanceof TagAnnotationData)) {
    			long id = ((DataObject) userObject).getId();
                List<Long> l = expandedTopNodes.get(TagAnnotationData.class);
                if (l == null) {
                	l = new ArrayList<Long>();
                	expandedTopNodes.put(TagAnnotationData.class, l);
                }
                l.add(Long.valueOf(id));
    		}
        } else if ((userObject instanceof ProjectData) 
        		&& node.isExpanded()) {
        	long id = ((DataObject) userObject).getId();
            List<Long> l = expandedTopNodes.get(ProjectData.class);
            if (l == null) {
            	l = new ArrayList<Long>();
            	expandedTopNodes.put(ProjectData.class, l);
            }
            l.add(Long.valueOf(id)); 
        } else if ((userObject instanceof ScreenData) 
        		&& node.isExpanded()) {
        	long id = ((DataObject) userObject).getId();
        	List<Long> l = expandedTopNodes.get(ScreenData.class);
        	if (l == null) {
        		l = new ArrayList<Long>();
        		expandedTopNodes.put(ScreenData.class, l);
        	}
        	l.add(Long.valueOf(id));
        } else if (userObject instanceof PlateData) {
        	if (node.hasChildrenDisplay() && node.isExpanded()) {
        		long id = ((DataObject) userObject).getId();
                List<Long> l = expandedTopNodes.get(PlateData.class);
                if (l == null) {
                	l = new ArrayList<Long>();
                	expandedTopNodes.put(PlateData.class, l);
                }
                l.add(id);
        	}
        } else if (userObject instanceof GroupData) {
        	if (node.isExpanded()) {
        		long id = ((DataObject) userObject).getId();
            	List<Long> l = expandedTopNodes.get(GroupData.class);
            	if (l == null) {
            		l = new ArrayList<Long>();
            		expandedTopNodes.put(GroupData.class, l);
            	}
            	l.add(Long.valueOf(id));
        	}
        } else if (node instanceof TreeImageTimeSet && node.isChildrenLoaded() 
        		&& node.isExpanded() && node.containsImages()) {
        	foundNodes.add(node);
    	} else if (node instanceof TreeFileSet && node.isChildrenLoaded() 
        		&& node.isExpanded()) {
    		foundNodes.add(node);
    	}
    }

}
