/*
 * org.openmicroscopy.shoola.agents.treeviewer.ExperimenterDataLoader
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer;


//Java imports
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;
import org.openmicroscopy.shoola.env.data.FSFileSystemView;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.ProjectData;
import pojos.TagAnnotationData;

/** 
 * Loads a Project/Dataset/(Image) hierarchy rooted by a given Project
 * if the {@link #containerType} is {@link #PROJECT}.
 * Loads a Dataset/(Image) hierarchy rooted by a given Dataset
 * if the {@link #containerType} is {@link #DATASET}.
 * Note that Images are retrieved if the {@link #withImages} flag is set to
 * <code>true</code>.
 * This class calls the <code>loadContainerHierarchy</code> method in the
 * <code>DataManagerView</code>.
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
public class ExperimenterDataLoader
	extends DataBrowserLoader
{

	/** Indicates that the root node is of type <code>Project</code>. */
    public static final int PROJECT = 0;
    
    /** Indicates that the root node is of type <code>Dataset</code>. */
    public static final int DATASET = 1;
    
    /** Indicates that the root node is of type <code>Image</code>. */
    public static final int IMAGE = 2;
    
    /** Indicates that the root node is of type <code>Tag</code>. */
    public static final int TAG = 3;
    
    /** Indicates that the root node is of type <code>Tag Set</code>. */
    public static final int TAG_SET = 4;
    
    /** Indicates that the root node is of type <code>File</code>. */
    public static final int FILE = 5;

    /** Indicates that the root node is of type <code>All</code>. */
    public static final int ALL = 6;
    
    /** Indicates that the root node is of type <code>File Data</code>. */
    public static final int FILE_DATA = 7;
    
    /** 
     * Flag to indicate if the images are also retrieved.
     * Value set to <code>true</code> to retrieve the images,
     * <code>false</code> otherwise.
     */
    private boolean     		withImages;
    
    /** The type of the root node. */
    private Class       		rootNodeType;
    
    /** The parent the nodes to retrieve are for. */
    private TreeImageSet		parent;
    
    /** The node hosting the experimenter the data are for. */
    private TreeImageSet		expNode;
    
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  		handle;
    
    /** One of the constants defined by this class. */
    private int					type;
    
    /**
     * Returns the class corresponding to the specified type.
     * Returns <code>null</code> if the type is not supported,
     * otherwise the corresponding class.
     * 
     * @param type  The type of the root node.
     * @return See above.
     */
    private Class getClassType(int type)
    {
    	this.type = type;
        switch (type) {
            case PROJECT: return ProjectData.class;
            case DATASET: return DatasetData.class; 
            case TAG: return TagAnnotationData.class;
            case TAG_SET: return TagAnnotationData.class;
            case FILE: return FileAnnotationData.class;
            case ALL: return null;
        }
        return null;
    }
   
    /**
     * Creates a new instance. 
     * 
     * @param viewer        The viewer this data loader is for.
     *                      Mustn't be <code>null</code>.
     * @param containerType	One of the type defined by this class.
     * @param expNode		The node hosting the experimenter the data are for.
     */
    public ExperimenterDataLoader(Browser viewer, int containerType, 
    							TreeImageSet expNode)
    {
        this(viewer, containerType, expNode, null);
    } 
    
    /**
     * Creates a new instance. 
     * 
     * @param viewer        The viewer this data loader is for.
     *                      Mustn't be <code>null</code>.
     * @param containerType	One of the type defined by this class.
     * @param expNode		The node hosting the experimenter the data are for.
     * 						Mustn't be <code>null</code>.
     * @param parent		The parent the nodes are for.
     */
    public ExperimenterDataLoader(Browser viewer, int containerType, 
    							TreeImageSet expNode, TreeImageSet parent)
    {
    	super(viewer);
        if (expNode == null ||
        		!(expNode.getUserObject() instanceof ExperimenterData))
        	throw new IllegalArgumentException("Experimenter node not valid.");
        this.parent = parent;
        this.expNode = expNode;
        rootNodeType = getClassType(containerType);
        //if (rootNodeType == null)
          //  throw new IllegalArgumentException("Type not supported");
        if (parent != null)  withImages = true;
        if (type == TAG_SET) withImages = false;
    } 
    
    /**
     * Retrieves the data.
     * @see DataBrowserLoader#load()
     */
    public void load()
    {
    	ExperimenterData exp = (ExperimenterData) expNode.getUserObject();
    	if (TagAnnotationData.class.equals(rootNodeType)) {
    		long id = -1;
    		if (parent != null) {
    			id = parent.getUserObjectId();
    		}
    		boolean top = type == TAG_SET;
    		handle = dmView.loadTags(id, withImages, top, exp.getId(), 
    				viewer.getUserGroupID(), this);
    	} else if (FileAnnotationData.class.equals(rootNodeType)) {
    		handle = mhView.loadExistingAnnotations(rootNodeType, exp.getId(), 
    				viewer.getUserGroupID(), this);
    	} else {
    		if (viewer.getBrowserType() == Browser.FILE_SYSTEM_EXPLORER) {
    			handle = dmView.loadRepositories(exp.getId(), this);
    		} else {
    			if (parent == null) {
            		handle = dmView.loadContainerHierarchy(rootNodeType, null, 
            				withImages, exp.getId(), viewer.getUserGroupID(), 
            				this);	
            	} else {
            		handle = dmView.loadContainerHierarchy(rootNodeType,
            				Arrays.asList(parent.getUserObjectId()),
            				withImages, exp.getId(), viewer.getUserGroupID(), 
            				this);
            	}
    		}
    	}
    }

    /**
     * Cancels the data loading.
     * @see DataBrowserLoader#cancel()
     */
    public void cancel() { handle.cancel(); }

    /**
     * Feeds the result back to the viewer.
     * @see DataBrowserLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == Browser.DISCARDED) return;  //Async cancel.
        if (viewer.getBrowserType() == Browser.FILE_SYSTEM_EXPLORER) {
        	viewer.setRepositories(expNode, (FSFileSystemView) result);
        	return;
        }
        if (FileAnnotationData.class.equals(rootNodeType)){
        	viewer.setExperimenterData(expNode, (Collection) result);
        	return;
        }
        if (parent == null) 
        	viewer.setExperimenterData(expNode, (Collection) result);
        else {
        	Collection nodes = (Collection) result;
    		Iterator i = nodes.iterator();
    		DataObject object;
    		Class klass = parent.getUserObject().getClass();
    		long id = parent.getUserObjectId();
    		if (TagAnnotationData.class.equals(klass)) {
    			viewer.setLeaves((Set) result, parent, expNode);
    		} else {
    			while (i.hasNext()) {
        			object = (DataObject) i.next();
    				if (object.getClass().equals(klass)
    						&& object.getId() == id) {
    					if (object instanceof DatasetData) {
    						viewer.setLeaves(((DatasetData) object).getImages(), 
    								parent, expNode);
    					} 
    				}
    			}
    		}
        }
    }
    
}
