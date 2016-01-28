/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.util.browser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openmicroscopy.shoola.agents.util.EditorUtil;

import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.FileData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.TagAnnotationData;

/** 
 * Finds the nodes corresponding the specified type and the identifier.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class NodesFinder 
	implements TreeImageDisplayVisitor
{

	/** The identifier of the data object to find. */
	private List<Long> ids;

	/** The type of object to handle. */
	private Class<?> type;

    /** Set of <code>TreeImageDisplay</code>s */
    private Set<TreeImageDisplay> nodes;

    /** The collection of nodes to find. */
    private Collection<DataObject> refObjects;

    /** Flag indicating to find the node by name. */
    private boolean byName;

    /**
     * Returns the name of the node.
     * 
     * @param obj The object to handle.
     * @return See above.
     */
    private String getNodeName(Object obj)
    { 
        if (obj instanceof ProjectData) return ((ProjectData) obj).getName();
        else if (obj instanceof DatasetData) 
            return ((DatasetData) obj).getName();
        else if (obj instanceof ImageData) 
            return ((ImageData) obj).getName();
        else if (obj instanceof ExperimenterData) {
        	return EditorUtil.formatExperimenter((ExperimenterData) obj);
        } else if (obj instanceof GroupData) {
        	 return ((GroupData) obj).getName();
        } else if (obj instanceof TagAnnotationData)
        	return ((TagAnnotationData) obj).getTagValue();
        else if (obj instanceof ScreenData)
        	return ((ScreenData) obj).getName();
        else if (obj instanceof PlateData) {
        	return ((PlateData) obj).getName();
        } else if (obj instanceof FileAnnotationData)
        	return ((FileAnnotationData) obj).getFileName();
        else if (obj instanceof File)
        	return ((File) obj).getName();
        else if (obj instanceof FileData)
        	return ((FileData) obj).getName();
        else if (obj instanceof PlateAcquisitionData)
        	return ((PlateAcquisitionData) obj).getLabel();
        else if (obj instanceof String) return (String) obj;
        return "";
    }

	/**
	 * Checks if the node is of the desired type.
	 * 
	 * @param node The node to handle.
	 */
	private void findNode(TreeImageDisplay node)
	{
		Object userObject = node.getUserObject();
		if (refObjects != null && refObjects.size() > 0) {
			if (userObject != null) {
				Iterator<DataObject> i = refObjects.iterator();
				DataObject object;
				Class<?> k = userObject.getClass();
				DataObject uo;
				String n = getNodeName(userObject);
				while (i.hasNext()) {
					object = i.next();
					if (object.getClass().equals(k)) {
						uo = (DataObject) userObject;
						if (byName) {
							if (n.equals(getNodeName(object))) {
								nodes.add(node);
								break;
							}
						} else {
							if (uo.getId() == object.getId()) {
								nodes.add(node);
								break;
							}
						}
					}
				}
			}
		} else {
			if (userObject != null && userObject.getClass().equals(type)) {
				if (userObject instanceof DataObject) {
					DataObject data = (DataObject) userObject;
					if (ids == null) {
						nodes.add(node);
					} else {
						if (ids.contains(data.getId())) nodes.add(node);
					}
				}
			}
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param type The type to data object.
	 */
	public NodesFinder(Class<?> type)
	{
		this.type = type;
		this.ids = null;
		nodes = new HashSet<TreeImageDisplay>();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param type The type to data object.
	 * @param ids The identifiers of the data object.
	 */
	public NodesFinder(Class<?> type, List<Long> ids)
	{
		this.type = type;
		this.ids = ids;
		nodes = new HashSet<TreeImageDisplay>();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param type The type to data object.
	 * @param id   The identifier of the data object.
	 */
	public NodesFinder(Class<?> type, long id)
	{
		this.type = type;
		ids = new ArrayList<Long>(1);
		ids.add(id);
		nodes = new HashSet<TreeImageDisplay>();
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param refObjects The collection of objects to find.
	 */
	public NodesFinder(Collection<DataObject> refObjects)
	{
		type = null;
		this.refObjects = refObjects;
		nodes = new HashSet<TreeImageDisplay>();
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param refObject The collection of objects to find.
	 */
	public NodesFinder(DataObject refObject)
	{
		type = null;
		if (refObject == null)
			throw new IllegalArgumentException("No object to find.");
		byName = refObject.getId() <= 0;
		refObjects = new ArrayList<DataObject>();
		refObjects.add(refObject);
		nodes = new HashSet<TreeImageDisplay>();
	}

	/**
	 * Returns the collection of nodes found.
	 * 
	 * @return See above.
	 */
	public Set<TreeImageDisplay> getNodes() { return nodes; }

	/**
	 * Finds the nodes.
	 * @see TreeImageDisplayVisitor#visit(TreeImageNode)
	 */
	public void visit(TreeImageNode node) { findNode(node); }

	/**
	 * Finds the nodes.
	 * @see TreeImageDisplayVisitor#visit(TreeImageSet)
	 */
	public void visit(TreeImageSet node) { findNode(node); }

}
