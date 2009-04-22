/*
 * ome.ij.dm.TreeViewerTranslator 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package ome.ij.dm;


//Java imports
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;



//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import ome.ij.dm.browser.TreeImageDisplay;
import ome.ij.dm.browser.TreeImageNode;
import ome.ij.dm.browser.TreeImageSet;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * This class contains a collection of utility static methods that transform
 * an hierarchy of {@link DataObject}s into a visualisation tree.
 * The tree is then displayed in the Browser.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class TreeViewerTranslator
{
	
	/** Default text displayed in the acquisition date is not available. */
	private static final String DATE_NOT_AVAILABLE = "Not available";
	
	/**
     * Returns the creation time associate to the image.
     * 
     * @param image The image to handle.
     * @return See above.
     */
    private static Timestamp getAcquisitionTime(ImageData image)
    {
    	if (image == null) return null;
    	Timestamp date = null;
        try {
        	date = image.getAcquisitionDate();
		} catch (Exception e) {}
		return date;
    }
    
	/**
     * Formats the toolTip of the specified {@link TreeImageDisplay} node.
     * 
     * @param node The specified node. Mustn't be <code>null</code>.
     */
    private static void formatToolTipFor(TreeImageDisplay node)
    {
        if (node == null) throw new IllegalArgumentException("No node");
        String toolTip = "";
        String title = null;
        Object uo = node.getUserObject();
        if (uo instanceof ImageData) {
            Timestamp time = getAcquisitionTime((ImageData) uo);
            if (time == null) title = DATE_NOT_AVAILABLE;
            else title = UIUtilities.formatTime(time); 
            toolTip = UIUtilities.formatToolTipText(title);
            node.setToolTip(toolTip); 
        } 
    }
    
    /**
     * Transforms a {@link ImageData} into a visualisation object i.e.
     * a {@link TreeImageNode}.
     * 
     * @param data  The {@link ImageData} to transform.
     *              Mustn't be <code>null</code>.
     * @return See above.
     */
    private static TreeImageDisplay transformImage(ImageData data)
    {
        if (data == null)
            throw new IllegalArgumentException("Cannot be null");
        TreeImageNode node = new TreeImageNode(data);
        formatToolTipFor(node);
        return node;
    }
    
	/**
     * Transforms a {@link DatasetData} into a visualisation object i.e.
     * a {@link TreeImageSet}.
     * 
     * @param data      The {@link DatasetData} to transform.
     *                  Mustn't be <code>null</code>.         
     * @return See above.
     */
    private static TreeImageDisplay transformDataset(DatasetData data)
    {
        if (data == null)
            throw new IllegalArgumentException("Cannot be null");
        TreeImageSet dataset =  new TreeImageSet(data);
        Set images = data.getImages();
        if (images == null) dataset.setNumberItems(-1);
        else {
            dataset.setChildrenLoaded(Boolean.TRUE);
            dataset.setNumberItems(images.size());
            Iterator i = images.iterator();
            DataObject tmp;
            ImageData child;
            while (i.hasNext()) {
            	tmp = (DataObject) i.next();
                if (tmp instanceof ImageData) {
                	 child = (ImageData) tmp;
                	 dataset.addChildDisplay(transformImage(child));    
                }
            }
        }
        
        formatToolTipFor(dataset);
        return dataset;
    }
    
    /**
     * Transforms a {@link ProjectData} into a visualisation object i.e.
     * a {@link TreeImageSet}. The {@link DatasetData datasets} are also
     * transformed and linked to the newly created {@link TreeImageSet}.
     * 
     * @param data      The {@link ProjectData} to transform.
     *                  Mustn't be <code>null</code>.
     * @param datasets  Collection of datasets to add.      
     * @return See above.
     */
    private static TreeImageDisplay transformProject(ProjectData data, 
                        Set datasets)
    {
        if (data == null)
            throw new IllegalArgumentException("Cannot be null");
        TreeImageSet project = new TreeImageSet(data);
        if (datasets != null) {
            project.setChildrenLoaded(Boolean.TRUE);
            Iterator i = datasets.iterator();
            DatasetData child;
            while (i.hasNext()) {
                child = (DatasetData) i.next();
                project.addChildDisplay(transformDataset(child)); 
            }
            project.setNumberItems(datasets.size());
        } else {
            //the datasets were not loaded
            project.setChildrenLoaded(Boolean.FALSE); 
            project.setNumberItems(0);
        }
        return project;
    }
    
	/**
	 * Transforms the passed nodes.
	 * 
	 * @param nodes The collection to transform
	 * @return See above.
	 */
	public static Set transformHierarchy(Collection nodes)
	{
		if (nodes == null)
            throw new IllegalArgumentException("No nodes.");
        Set<TreeImageDisplay> results = 
        		new HashSet<TreeImageDisplay>(nodes.size());
        Iterator i = nodes.iterator();
        DataObject ho;
        while (i.hasNext()) {
            ho = (DataObject) i.next();
            if (ho instanceof ProjectData)
            	results.add(transformProject((ProjectData) ho, 
            			((ProjectData) ho).getDatasets()));
            else if (ho instanceof ImageData) 
            	results.add(transformImage((ImageData) ho));	
            else if (ho instanceof DatasetData)
            	results.add(transformDataset((DatasetData) ho));
        }
        return results;
	}

}
