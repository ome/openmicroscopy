/*
 * org.openmicroscopy.shoola.agents.treemng.TreeViewerTranslator
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

package org.openmicroscopy.shoola.agents.treeviewer;



//Java imports
import java.io.File;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.filechooser.FileSystemView;

//Third-party libraries

//Application-internal dependencies
import omero.model.ScreenAcquisitionI;

import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeFileSet;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageNode;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageSet;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.clsf.TreeCheckNode;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenAcquisitionData;
import pojos.ScreenData;
import pojos.TagAnnotationData;
import pojos.WellData;

/** 
 * This class contains a collection of utility static methods that transform
 * an hierarchy of {@link DataObject}s into a visualisation tree.
 * The tree is then displayed in the TreeViewer. For example,
 * A list of Projects-Datasets is passed to the 
 * {@link #transformHierarchy(Set, long, long)} method and transforms into a set 
 * of TreeImageSet-TreeImageSet.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class TreeViewerTranslator
{
   
	/** Text of the dummy TreeImageSet containing the orphaned datasets. */
	//public static final String ORPHANED_DATASETS = "Orphaned Datasets";
	
	/** Text of the dummy TreeImageSet containing the orphaned categories.*/
	public static final String ORPHANED_CATEGORIES = "Ungrouped Tags";
	
	/**
     * Returns the creation time associate to the image.
     * 
     * @param image The image to handle.
     * @return See above.
     */
    public static Timestamp getAcquisitionTime(ImageData image)
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
        	ImageData img = (ImageData) uo;
            Timestamp time = EditorUtil.getAcquisitionTime(img);
            if (time == null) title = EditorUtil.DATE_NOT_AVAILABLE;
            else title = UIUtilities.formatTime(time); 
            StringBuffer buf = new StringBuffer();
    		buf.append("<html><body>");
    		buf.append(UIUtilities.formatString(img.getName(), -1));
    		buf.append("<br>");
    		buf.append(title);
            toolTip = UIUtilities.formatToolTipText(buf.toString());
            
            node.setToolTip(toolTip); 
        } else if (uo instanceof WellData) {
        	toolTip = UIUtilities.formatToolTipText(
        		((WellData) node.getUserObject()).getExternalDescription());
        	node.setToolTip(toolTip);
        } else if (uo instanceof File) {
        	
        }
    }
    
    /**
     * Transforms a {@link DatasetData} into a visualisation object i.e.
     * a {@link TreeCheckNode}.
     * 
     * @param data  The {@link DatasetData} to transform.
     *              Mustn't be <code>null</code>.
     * @return See above.
     */
    private static TreeCheckNode transformDatasetCheckNode(DatasetData data)
    {
        if (data == null)
            throw new IllegalArgumentException("Cannot be null");
        IconManager im = IconManager.getInstance();      
        TreeCheckNode node =  new TreeCheckNode(data, 
                                    im.getIcon(IconManager.DATASET),
                                    data.getName(), true);
        return node;
    }
    
    /**
     * Transforms a {@link ImageData} into a visualisation object i.e.
     * a {@link TreeCheckNode}.
     * 
     * @param data  The {@link ImageData} to transform.
     *              Mustn't be <code>null</code>.
     * @return See above.
     */
    private static TreeCheckNode transformImageCheckNode(ImageData data)
    {
        if (data == null)
            throw new IllegalArgumentException("Cannot be null");
        IconManager im = IconManager.getInstance();      
        TreeCheckNode node =  new TreeCheckNode(data, 
                                    im.getIcon(IconManager.IMAGE),
                                    data.getName(), true);
        return node;
    }
    
    /**
     * Transforms a {@link DatasetData} into a visualisation object i.e.
     * a {@link TreeImageSet}.
     * 
     * @param data      The {@link DatasetData} to transform.
     *                  Mustn't be <code>null</code>.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                      retrieving the data.                 
     * @return See above.
     */
    private static TreeImageDisplay transformDataset(DatasetData data, 
                                            long userID, long groupID)
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
                	 if (EditorUtil.isReadable(child, userID, groupID))
                         dataset.addChildDisplay(transformImage(child));
                }
            }
        }
        
        formatToolTipFor(dataset);
        return dataset;
    }
    
    /**
     * Transforms a {@link PlateData} into a visualisation object i.e.
     * a {@link TreeImageSet}.
     * 
     * @param data      The {@link PlateData} to transform.
     *                  Mustn't be <code>null</code>.
     * @param screen 	The screen this plate is related to.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                      retrieving the data.                 
     * @return See above.
     */
    private static TreeImageDisplay transformPlate(PlateData data, ScreenData
    		screen, long userID, long groupID)
    {
        if (data == null)
            throw new IllegalArgumentException("Cannot be null");
        TreeImageSet plate =  new TreeImageSet(data);
        if (screen != null) {
        	Set<ScreenAcquisitionData> set = screen.getScreenAcquisitions();
        	if (set != null) {
        		Iterator<ScreenAcquisitionData> i = set.iterator();
        		ScreenAcquisitionData sa;
        		long id = data.getId();
        		while (i.hasNext()) {
        			sa = (ScreenAcquisitionData) i.next();
        			if (sa.getRefPlateId() == id)
        				plate.addChildDisplay(new TreeImageSet(sa));	
				}
        		plate.setChildrenLoaded(Boolean.valueOf(true));
        	}
        	//ScreenAcquisitionI sa = new ScreenAcquisitionI(1, true);
        	//plate.addChildDisplay(new TreeImageSet(new ScreenAcquisitionData(sa)));
        	//plate.setChildrenLoaded(Boolean.valueOf(true));
        }
        formatToolTipFor(plate);
        return plate;
    }
    
    /**
     * Transforms a {@link WellData} into a visualisation object i.e.
     * a {@link TreeImageSet}.
     * 
     * @param data      The {@link WellData} to transform.
     *                  Mustn't be <code>null</code>.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                      retrieving the data.                 
     * @return See above.
     */
    private static TreeImageDisplay transformWell(WellData data, long userID, 
    		long groupID)
    {
        if (data == null)
            throw new IllegalArgumentException("Cannot be null");
        TreeImageSet well =  new TreeImageSet(data);
        /*
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
                	 if (EditorUtil.isReadable(child, userID, groupID))
                         dataset.addChildDisplay(transformImage(child));
                }
            }
        }
        */
        formatToolTipFor(well);
        return well;
    }
    
    /**
     * Transforms a {@link TagAnnotationData} into a visualisation object i.e.
     * a {@link TreeImageSet}.
     * 
     * @param data      The {@link TagAnnotationData} to transform.
     *                  Mustn't be <code>null</code>.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                      retrieving the data.                 
     * @return See above.
     */
    private static TreeImageDisplay transformTag(
    					TagAnnotationData data, long userID, long groupID)
    {
        if (data == null)
            throw new IllegalArgumentException("Cannot be null");
        TreeImageSet tag =  new TreeImageSet(data);
        formatToolTipFor(tag);
        
        if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(data.getNameSpace())) {
        	Set tags = data.getTags();
            if (tags != null && tags.size() > 0) {
            	tag.setChildrenLoaded(Boolean.TRUE);
            	
                Iterator i = tags.iterator();
                TagAnnotationData tmp;
                while (i.hasNext()) {
                	tmp = (TagAnnotationData) i.next();
                	tag.addChildDisplay(transformTag(tmp, userID, groupID));
                }
                tag.setNumberItems(tags.size());
                return tag;
            } 
            tag.setChildrenLoaded(Boolean.TRUE); 
            tag.setNumberItems(0);
            return tag;
        }
        
        Set dataObjects = data.getDataObjects();
        //
        if (dataObjects == null || dataObjects.size() == 0) 
        	tag.setNumberItems(-1);
        else {
        	tag.setChildrenLoaded(Boolean.TRUE);
        	tag.setNumberItems(dataObjects.size());
            Iterator i = dataObjects.iterator();
            DataObject tmp;
            ProjectData p;
            while (i.hasNext()) {
            	tmp = (DataObject) i.next();
            	if (EditorUtil.isReadable(tmp, userID, groupID)) {
            		if (tmp instanceof ImageData)
            			tag.addChildDisplay(transformImage((ImageData) tmp));
            		else if (tmp instanceof DatasetData) 
            			tag.addChildDisplay(transformDataset((DatasetData) tmp, 
            								userID, groupID));
            		else if (tmp instanceof ProjectData) {
            			p = (ProjectData) tmp;
            			tag.addChildDisplay(transformProject(p, p.getDatasets(), 
            					userID, groupID));
            		}	
            	}
                
            }
        }

        return tag;
    }
    
    /**
     * Transforms a {@link FileAnnotationData} into a visualisation object i.e.
     * a {@link TreeImageNode}.
     * 
     * @param data      The {@link FileAnnotationData} to transform.
     *                  Mustn't be <code>null</code>.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                      retrieving the data.                 
     * @return See above.
     */
    private static TreeImageDisplay transformFile(FileAnnotationData data, 
    			long userID, long groupID)
    {
        if (data == null)
            throw new IllegalArgumentException("Cannot be null");
        TreeImageNode node = new TreeImageNode(data);
        formatToolTipFor(node);
        return node;
    }
    
    /**
     * Transforms a {@link ProjectData} into a visualisation object i.e.
     * a {@link TreeImageSet}. The {@link DatasetData datasets} are also
     * transformed and linked to the newly created {@link TreeImageSet}.
     * 
     * @param data      The {@link ProjectData} to transform.
     *                  Mustn't be <code>null</code>.
     * @param datasets  Collection of datasets to add.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                      retrieving the data.             
     * @return See above.
     */
    private static TreeImageDisplay transformProject(ProjectData data, 
                        Set datasets, long userID, long groupID)
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
                if (EditorUtil.isReadable(child, userID, groupID))
                    project.addChildDisplay(transformDataset(child, userID, 
                                                            groupID));
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
     * Transforms a {@link ScreenData} into a visualisation object i.e.
     * a {@link TreeImageSet}. The {@link PlateData plates} are also
     * transformed and linked to the newly created {@link TreeImageSet}.
     * 
     * @param data      The {@link ScreenData} to transform.
     *                  Mustn't be <code>null</code>.
     * @param plates    Collection of plates to add.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                  retrieving the data.             
     * @return See above.
     */
    private static TreeImageDisplay transformScreen(ScreenData data, 
                        Set plates, long userID, long groupID)
    {
        if (data == null)
            throw new IllegalArgumentException("Cannot be null");
        TreeImageSet screen = new TreeImageSet(data);
        if (plates != null) {
        	screen.setChildrenLoaded(Boolean.TRUE);
            Iterator i = plates.iterator();
            PlateData child;
            while (i.hasNext()) {
                child = (PlateData) i.next();
                if (EditorUtil.isReadable(child, userID, groupID))
                	screen.addChildDisplay(transformPlate(child, data, userID, 
                                                            groupID));
            }
            screen.setNumberItems(plates.size());
        } else {
            //The plates not loaded.
            screen.setChildrenLoaded(Boolean.FALSE); 
            screen.setNumberItems(0);
        }
        return screen;
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
     * Transforms a set of {@link DataObject}s into their corresponding 
     * visualization objects. The elements of the set can either be
     * {@link ProjectData}, {@link CategoryGroupData} or {@link ImageData}.
     * 
     * @param dataObjects   The collection of {@link DataObject}s to transform.
     * @param userID        The id of the current user.
     * @param groupID       The id of the group the current user selects when 
     *                      retrieving the data.    
     * @return A set of visualization objects.
     */
    public static Set transformHierarchy(Collection dataObjects, long userID, 
                                        long groupID)
    {
        if (dataObjects == null)
            throw new IllegalArgumentException("No objects.");
        Set<TreeImageDisplay> results = 
        		new HashSet<TreeImageDisplay>(dataObjects.size());
        Iterator i = dataObjects.iterator();
        DataObject ho;
        TreeImageDisplay child;
        while (i.hasNext()) {
            ho = (DataObject) i.next();
            if (EditorUtil.isReadable(ho, userID, groupID)) {
                if (ho instanceof ProjectData)
                  results.add(transformProject((ProjectData) ho, 
                            ((ProjectData) ho).getDatasets(), userID, 
                                                groupID));
                else if (ho instanceof ImageData) 
                    results.add(transformImage((ImageData) ho));	
                else if (ho instanceof DatasetData) {
                	/*
                	if (orphan == null) {
                		orphan = new TreeImageSet(ORPHANED_DATASETS);
                		orphan.setChildrenLoaded(Boolean.TRUE);
                		results.add(orphan);
                	}
                	child = transformDataset((DatasetData) ho, userID, groupID);
                	orphan.addChildDisplay(child);
                	*/
                	child = transformDataset((DatasetData) ho, userID, groupID);
                	results.add(child);
                } else if (ho instanceof TagAnnotationData) {
                	child = transformTag((TagAnnotationData) ho, userID, 
                			            groupID);
                	results.add(child);
                } else if (ho instanceof ScreenData) {
                	results.add(transformScreen((ScreenData) ho, 
                            ((ScreenData) ho).getPlates(), userID, 
                                                groupID));
                } else if (ho instanceof PlateData) {
                	results.add(transformPlate((PlateData) ho, null, userID, 
                			groupID));
                } else if (ho instanceof WellData) {
                	results.add(transformWell((WellData) ho, userID, groupID));
                } else if (ho instanceof FileAnnotationData) {
                	child = transformFile((FileAnnotationData) ho, userID, 
    			            groupID);
    				results.add(child);
                }
            }   
        }
        return results;
    }
    
    /**
     * Transforms the data objects into their corresponding 
     * visualization objects.
     * 
     * @param nodes					The nodes to transform.
     * @param expandedTopNodes     	The list of expanded top nodes IDs.
     * @param userID                The id of the current user.
     * @param groupID               The id of the group the current user 
     *                              selects when retrieving the data.    
     * @return A set of visualization objects.
     */
    public static Set refreshHierarchy(Map nodes, Map expandedTopNodes, 
                                        long userID, long groupID)
    {
        if (nodes == null)
            throw new IllegalArgumentException("No objects.");
        Set<TreeImageDisplay> results = 
        						new HashSet<TreeImageDisplay>(nodes.size());
        Set set = nodes.entrySet();
        Entry entry;
        Iterator i = set.iterator();
        DataObject ho;
        TreeImageDisplay display;
        List expanded = null;
        //TreeImageSet orphan = null;
        while (i.hasNext()) {
        	entry = (Entry) i.next();
            ho = (DataObject) entry.getKey();
            if (EditorUtil.isReadable(ho, userID, groupID)) {
                if (ho instanceof ProjectData) {
                	if (expandedTopNodes != null)
                		expanded = (List) expandedTopNodes.get(
                						ProjectData.class);
                    display = transformProject((ProjectData) ho, 
                                                (Set) entry.getValue(), 
                                                userID, groupID);
                    if (expanded != null)
	                    display.setExpanded(expanded.contains(ho.getId()));
                    results.add(display);
                } else if (ho instanceof DatasetData) {
                	if (expandedTopNodes != null)
                		expanded = 
                			(List) expandedTopNodes.get(DatasetData.class);
                	Set r = (Set) entry.getValue(); //should only have one element
                	Iterator k = r.iterator();
                	DatasetData element;
                	while (k.hasNext()) {
                		element = (DatasetData) k.next();
                		display = transformDataset(element, userID, groupID);
                		if (expanded != null) {
                			display.setExpanded(expanded.contains(ho.getId()));
                		}
                			
                		//orphan.addChildDisplay(display);
                		results.add(display); 
                	}
                } else if (ho instanceof TagAnnotationData) {
                	if (expandedTopNodes != null)
                		expanded = 
                		(List) expandedTopNodes.get(TagAnnotationData.class);
                	Set r = (Set) entry.getValue(); //should only have one element
                	Iterator k = r.iterator();
                	TagAnnotationData element;
                	while (k.hasNext()) {
                		element = (TagAnnotationData) k.next();
                		display = transformTag(element, userID, groupID);
                		if (expanded != null)
                			display.setExpanded(expanded.contains(ho.getId()));
                		//orphan.addChildDisplay(display);
                		results.add(display); 
                	}
                } else if (ho instanceof ScreenData) {
                	if (expandedTopNodes != null)
                		expanded = (List) expandedTopNodes.get(
                				ScreenData.class);
                    display = transformScreen((ScreenData) ho, 
                            ((ScreenData) ho).getPlates(), userID, groupID);
                    if (expanded != null)
	                    display.setExpanded(
	                    		expanded.contains(ho.getId()));

                    results.add(display);
                } else if (ho instanceof PlateData) {
                	display = transformPlate((PlateData) ho, null, userID, 
                			groupID);
                	if (expanded != null)
                		display.setExpanded(expanded.contains(ho.getId()));
                	results.add(display);
                }
            }
        }
        return results;
    }
    
    /**
     * Transforms the specified {@link DataObject} into a visualisation
     * representation.
     * 
     * @param object    The {@link DataObject} to transform.
     *                  Mustn't be <code>null</code>.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                      retrieving the data.                  
     * @return See above
     */
    public static TreeImageDisplay transformDataObject(DataObject object, 
                                long userID, long groupID)
    {
        if (object == null)
            throw new IllegalArgumentException("No object.");
        if (!(EditorUtil.isReadable(object, userID, groupID)))
            throw new IllegalArgumentException("Data object not readable.");
        if (object instanceof ProjectData)
            return transformProject((ProjectData) object, 
                    ((ProjectData) object).getDatasets(), userID, groupID);
        else if (object instanceof DatasetData)
            return transformDataset((DatasetData) object, userID, groupID);
        else if (object instanceof ImageData)
            return transformImage((ImageData) object);
        else if (object instanceof ScreenData)
            return transformScreen((ScreenData) object, 
            		((ScreenData) object).getPlates(), userID, groupID);
        else if (object instanceof PlateData)
            return transformPlate((PlateData) object, null, userID, groupID);
        else if (object instanceof TagAnnotationData)
            return transformTag((TagAnnotationData) object, userID, groupID);
        throw new IllegalArgumentException("Data Type not supported.");
    }
    
    
    /**
     * Transforms a set of {@link DataObject}s into their corresponding 
     * {@link TreeCheckNode} visualization objects. The elements of the set can 
     * either be {@link CategoryGroupData}, {@link CategoryData} or
     * {@link DatasetData}.
     * 
     * @param dataObjects   The collection of {@link DataObject}s to transform.
     * @param userID        The id of the current user.
     * @param groupID       The id of the group the current user selects when 
     *                      retrieving the data.
     * @return A set of visualization objects.
     */
    public static Set transformDataObjectsCheckNode(Set dataObjects,
                                        long userID, long groupID)
    {
        if (dataObjects == null)
            throw new IllegalArgumentException("No objects.");
        Set<TreeCheckNode> results = 
        		new HashSet<TreeCheckNode>(dataObjects.size());
        Iterator i = dataObjects.iterator();
        DataObject ho;
        while (i.hasNext()) {
            ho = (DataObject) i.next();
            if (EditorUtil.isWritable(ho, userID, groupID)) {
                if (ho instanceof DatasetData) 
                    results.add(transformDatasetCheckNode((DatasetData) ho));
            }  
        }
        return results;
    }
    
    /**
     * Transforms a set of {@link DataObject}s into their corresponding 
     * visualization objects. The elements of the set can either be
     * {@link DatasetData}, {@link CategoryData} or {@link ImageData}.
     * 
     * @param paths     Collection of {@link DataObject}s to transform.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                      retrieving the data. 
     * @return A set of visualization objects.
     */
    public static Set transformIntoCheckNodes(Set paths, long userID, 
                                                long groupID)
    {
        if (paths == null)
            throw new IllegalArgumentException("No objects.");
        Set<TreeCheckNode> results = new HashSet<TreeCheckNode>();
        Iterator i = paths.iterator();
        DataObject ho;
        while (i.hasNext()) {
            ho = (DataObject) i.next();
            if (EditorUtil.isWritable(ho, userID, groupID)) {
                if (ho instanceof DatasetData)
                    results.add(transformDatasetCheckNode((DatasetData) ho));
                else if (ho instanceof ImageData)
                    results.add(transformImageCheckNode((ImageData) ho));
            }
        }
        return results;
    }
    
    /**
     * Returns <code>true</code> if the specified data object is visible,
     * <code>false</code> otherwise, depending on the permission.
     * 
     * @param data			The <code>DataObject</code>. 
     * 						Mustn't be <code>null</code>.
     * @param loggedUser	The currently logged in user.
     * @return See above.
     */
    public static boolean isVisible(DataObject data, 
    								ExperimenterData loggedUser)
    {
    	if (data == null) return false;
    	if (!((data instanceof ExperimenterData) || 
    		(data instanceof GroupData)))
    		return false;
    	
    	return true;
    	/*
    	PermissionData permissions = data.getPermissions();
    	if (permissions.isWorldRead()) return true;
    	if (permissions.isGroupRead()) { //check if logged user can view it
    		Set groups = loggedUser.getGroups();
    		if (groups == null || groups.size() == 0) return false;
    		Iterator i = groups.iterator();
    		GroupData group;
    		while (i.hasNext()) {
    			group = (GroupData) i.next();
				if (group.getId() == data.getId()) return true;
			}
    		return false;
    	}
    	return false;
    	*/
    }
    
    /**
     * Transforms a set of objects into their corresponding 
     * visualization objects.
     *  
     * @param nodes		The nodes to transform.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                      retrieving the data. 
     * @return A set of visualization objects.
     */
    public static Map<Integer, Set> refreshFolderHierarchy(Map nodes, 
    								long userID, long groupID)
    {
    	if (nodes == null)
            throw new IllegalArgumentException("No objects.");
    	Map<Integer, Set> r = new HashMap<Integer, Set>(nodes.size());
        
    	Set set = nodes.entrySet();
    	Entry entry;
        Iterator i = set.iterator();
        TreeImageDisplay node;
        Set results;
        Iterator j;
        DataObject ho;
        Set<TreeImageDisplay> converted;
        while (i.hasNext()) {
        	entry = (Entry) i.next();
            node = (TreeImageDisplay) entry.getKey();
            if (node instanceof TreeImageTimeSet) {
            	results = (Set) entry.getValue();
                converted = new HashSet<TreeImageDisplay>(results.size());
                j = results.iterator();
                while (j.hasNext()) {
                	ho = (DataObject) j.next();
                	if (EditorUtil.isReadable(ho, userID, groupID))
                		converted.add(transformImage((ImageData) ho));
    			}
                r.put(((TreeImageTimeSet) node).getIndex(), converted);
            } else if (node instanceof TreeFileSet) {
            	results = (Set) entry.getValue();
                converted = new HashSet<TreeImageDisplay>(results.size());
                j = results.iterator();
                while (j.hasNext()) {
                	ho = (DataObject) j.next();
                	
                	if (EditorUtil.isReadable(ho, userID, groupID))
                		converted.add(transformFile((FileAnnotationData) ho, 
                				userID, groupID));
    			}
                r.put(((TreeFileSet) node).getType(), converted);
            }
        }
        return r;
	}
    
    /**
     * Transforms the directory.
     * 
     * @param dir The directory to transform.
     * @return See above.
     */
    private static TreeImageSet transformDirectory(FileSystemView fs, File dir)
    {
    	TreeImageSet dirSet = new TreeImageSet(dir);
    	File[] files = fs.getFiles(dir, false);
    	if (files != null && files.length > 0) {
    		File file;
    		TreeImageDisplay display;
    		for (int i = 0; i < files.length; i++) {
    			file = files[i];
    			if (file.isDirectory()) {
        			if (!file.isHidden()) {
        				//dirSet.addChildDisplay(transformDirectory(fs, file));
        				
        				dirSet.addChildDisplay(new TreeImageSet(file));
        			}
        				
        		} else {
        			/*
        			display = transformFile(file);
        			if (display != null) 
        				dirSet.addChildDisplay(display);
        				*/
        		}
			}
    	}
    	return dirSet;
    }
    
}
    
