/*
 * org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserTranslator 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser;



//Java imports
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageSet;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.WellImageNode;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.PermissionData;
import pojos.ProjectData;
import pojos.TagAnnotationData;
import pojos.WellData;
import pojos.WellSampleData;

/** 
 * This class contains a collection of utility static methods that transform
 * an hierarchy of {@link DataObject}s into a visualisation tree.
 * The tree is then displayed in the DataBrowser. For example,
 * A list of Projects-Datasets-Images is passed to the 
 * {@link #transformProjects(Set, long, long)} method and transforms into a set
 * of ImageSet-ImageSet-ImageNode.
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
public class DataBrowserTranslator 
{

	 /** Message for unclassified images. */
    private static final String UNCLASSIFIED = "Wild at heart and free images.";
    
    /** 
     * The left element displayed before the number of items contained in a 
     * given container. 
     */
    private static final String LEFT = "[";
    
    /** 
     * The left element displayed before the number of items contained in a 
     * given container. 
     */
    private static final String RIGHT = "]";
    
    /**
     * Formats the toolTip of the specified {@link ImageDisplay} node.
     * 
     * @param node The specified node. Mustn't be <code>null</code>.
     */
    private static void formatToolTipFor(ImageDisplay node)
    {
        if (node == null) throw new IllegalArgumentException("No node");
        String toolTip = UIUtilities.formatToolTipText(node.getTitle());  
        node.getTitleBar().setToolTipText(toolTip);
    }
    
    /** 
     * Returns the first element of the specified set. 
     * Returns <code>null</code> if the set is empty or <code>null</code>.
     * 
     * @param set The set to analyse.
     * @return See above.
     */
    private static ImageDisplay getFirstElement(Set set)
    {
        if (set == null || set.size() == 0) return null;
        ImageDisplay display = null;
        Iterator i = set.iterator();
        while (i.hasNext()) {
            display = (ImageDisplay) i.next();
            break;  
        }
        return display;
    }
    
    /** 
     * Transforms each {@link ImageData} object into a visualisation object
     * i.e. {@link ImageNode}.
     * Then adds the newly created {@link ImageNode} to the specified 
     * {@link ImageSet parent}. 
     * 
     * @param is        The {@link ImageData} to transform.
     * @param parent    The {@link ImageSet parent} of the image node.
     * @return  The new created {@link ImageNode}.
     */
    private static ImageNode linkImageTo(ImageData is, ImageSet parent)
    {
    	long id = is.getId();
    	String name = "";
    	if (id >= 0) name = is.getName();
        ThumbnailProvider provider = new ThumbnailProvider(is);
        ImageNode node = new ImageNode(name, is, provider);
        formatToolTipFor(node);  
        provider.setImageNode(node);
        if (parent != null) parent.addChildDisplay(node);
        return node;
    }
    
    /** 
     * Transforms each {@link ImageData} object contained in the specified
     * list into a visualisation object i.e. {@link ImageNode}.
     * Then adds the newly created {@link ImageNode} to the specified 
     * {@link ImageSet parent}. 
     * 
     * @param images    Collection of {@link ImageData}s.
     * @param parent    The {@link ImageSet} corresponding to the
     *                  {@link DataObject} containing the images.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                      retrieving the data.             
     */
    private static void linkImagesTo(Set images, ImageSet parent, long userID,
                                    long groupID)
    {
        if (images == null || parent == null) return;
        Iterator i = images.iterator();
        ImageData child;
        while (i.hasNext()) {
            child = (ImageData) i.next();
            if (isReadable(child, userID, groupID))
                linkImageTo(child, parent);
        }  
    }
    
    /** 
     * Links the images contained into the specified {@link DataObject} 
     * to {@link ImageSet} corresponding to the {@link DataObject}.
     * 
     * @param uo        Parent object. Either an instance of {@link DatasetData}
     *                  or {@link CategoryData}.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                      retrieving the data.        
     * @return  The corresponding {@link ImageDisplay} or <code>null</code>.
     */
    private static ImageDisplay linkImages(DataObject uo, long userID,
                                        long groupID)
    {
        ImageSet node = null;
        Set images;
        if (uo instanceof DatasetData) {
            DatasetData ds = (DatasetData) uo;
            images = ds.getImages();
            String note = "";
            if (images != null) note = LEFT+images.size()+RIGHT;
            node = new ImageSet(ds.getName(), note, ds);
            formatToolTipFor(node);
            linkImagesTo(images, node, userID, groupID);
        } else if (uo instanceof CategoryData) {
            CategoryData data = (CategoryData) uo;
            String note = "";
            images = data.getImages();
            if (images != null) note = LEFT+images.size()+RIGHT;
            node = new ImageSet(data.getName(), note, data);
            formatToolTipFor(node);
            linkImagesTo(images, node, userID, groupID);
        } 
        return node;
    }

    /**
     * Transforms the images contained in the passed container.
     * 
     * @param uo		The data object to handle. Mustn't be <code>null</code>.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                      retrieving the data.
     * @return Collection of {@link ImageNode} or <code>null</code>.
     */
    private static Set transformImagesFrom(DataObject uo, long userID,
                                        long groupID)
    {
    	if (uo == null) 
            throw new IllegalArgumentException("No Object.");
    	Set images = null;
    	Set children;
    	Iterator i, j;
    	CategoryData c;
    	DatasetData d;
    	if (uo instanceof CategoryData) {
    		c = (CategoryData) uo;
    		images = transformImages(c.getImages(), userID, groupID);
    	} else if (uo instanceof CategoryGroupData) {
    		CategoryGroupData cg = (CategoryGroupData) uo;
    		children = cg.getCategories();
    		if (children != null) {
    			i = children.iterator();
    			images = new HashSet();
    			while (i.hasNext()) {
					c = (CategoryData) i.next();
					images.addAll(transformImages(c.getImages(), 
								userID, groupID));
				}
    		}
    	} else if (uo instanceof DatasetData) {
    		d = (DatasetData) uo;
    		images = transformImages(d.getImages(), userID, groupID);
    	} else if (uo instanceof ProjectData) {
    		ProjectData pj = (ProjectData) uo;
    		children = pj.getDatasets();
    		if (children != null) {
    			i = children.iterator();
    			images = new HashSet();
    			while (i.hasNext()) {
					d = (DatasetData) i.next();
					images.addAll(transformImages(d.getImages(), 
								userID, groupID));
				}
    		}
    	} else if (uo instanceof ImageData) {
    		images = new HashSet(1); 
    		if (isReadable(uo, userID, groupID))
                images.add(linkImageTo((ImageData) uo, null));
    	} 
    	return images;
    }
    
    /**
     * Transforms a Projects/Datasets/Images hierarchy into a visualisation
     * tree. 
     * 
     * @param projects  Collection of {@link ProjectData}s to transform.
     *                  Mustn't be <code>null</code>.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                  retrieving the data.                   
     * @return Collection of corresponding {@link ImageDisplay}s.
     */
    private static Set transformProjects(Set projects, long userID,
                                        long groupID)
    {
        if (projects == null) 
            throw new IllegalArgumentException("No projects.");
        Set results = new HashSet();
        Iterator i = projects.iterator(), j;
        //DataObject
        ProjectData ps;
        DataObject child;
        //Visualisation object.
        ImageSet project;  
        Set datasets;
        String note = "";
        while (i.hasNext()) {
            ps = (ProjectData) i.next();
            if (isReadable(ps, userID, groupID)) {
                datasets = ps.getDatasets();
                if (datasets != null) note += LEFT+datasets.size()+RIGHT;
                project = new ImageSet(ps.getName(), note, ps);
                formatToolTipFor(project);
                if (datasets != null) {
                    j = datasets.iterator();
                    while (j.hasNext()) {
                        child = (DataObject) j.next();
                        if (isReadable(child, userID, groupID)) 
                            project.addChildDisplay(linkImages(child, userID, 
                                                                groupID));
                    }     
                }
                results.add(project);
            }
            
        }
        return results;
    }
 
    /**
     * Transforms the specified <code>Well</code> object into its corresponding
     * visualisation object.
     *  
     * @param data		The <code>Well</code> to transform
     * @param userID	The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                  retrieving the data.  
     * @return See above.
     */
    private static ImageDisplay transformWell(WellData data, long userID,  
    		                                long groupID)
    {
    	if (data == null) 
            throw new IllegalArgumentException("No tag.");
        if (!isReadable(data, userID, groupID)) return null;
        WellSampleData wsd;
        ImageData img;
        WellImageNode node = null;
        List<WellSampleData> samples = data.getWellSamples();
        if (samples == null || samples.size() == 0) {
        	img = new ImageData();
        	img.setId(-1);
        	node = createWellImage(img);
        	node.setWellData(data);
        } else {
        	Iterator<WellSampleData> i = samples.iterator();
        	
        	while (i.hasNext()) {
				wsd = i.next();
				img = wsd.getImage();
				if (img != null && isReadable(img, userID, groupID)) {
					node = createWellImage(img);
                	node.setWellData(data);
				}
				
			}
        }
        return node;
    }
    
    /**
     * Creates a well image node.
     * 
     * @param is The image data to host.
     * @return See above.
     */
    private static WellImageNode createWellImage(ImageData is)
    {
    	long id = is.getId();
    	String name = "";
    	if (id >= 0) name = is.getName();
        ThumbnailProvider provider = new ThumbnailProvider(is);
        WellImageNode node = new WellImageNode(name, is, provider);
        //formatToolTipFor(node);  
        provider.setImageNode(node);
        return node;
    }
    
    /**
     * Transforms a Datasets/Images hierarchy into a visualisation
     * tree. 
     * 
     * @param tag     The {@link TagAnnotationData}s to transform.
     *                Mustn't be <code>null</code>.
     * @param userID  The id of the current user.
     * @param groupID The id of the group the current user selects when 
     *                retrieving the data.                
     * @return The corresponding {@link ImageDisplay}s.
     */
    private static ImageDisplay transformTag(TagAnnotationData tag, long userID,
                                        long groupID)
    {
        if (tag == null) 
            throw new IllegalArgumentException("No tag.");
        if (!isReadable(tag, userID, groupID)) return null;
        ImageSet data = null;
        Set tags = tag.getTags();
        Set images = tag.getImages();
        String note = "";
        Iterator i;
        if (tags != null && tags.size() > 0) {
        	note += LEFT+tags.size()+RIGHT;
        	data = new ImageSet(tag.getTagValue(), note, tag);
        	i = tags.iterator();
        	TagAnnotationData child;
        	while (i.hasNext()) {
        		child = (TagAnnotationData) i.next();
				data.addChildDisplay(transformTag(child, userID, groupID));
			}
        } if (images != null && images.size() > 0) {
        	note += LEFT+images.size()+RIGHT;
        	data = new ImageSet(tag.getTagValue(), note, tag);
        	ImageData child;
        	i = images.iterator();
        	while (i.hasNext()) {
        		child = (ImageData) i.next();
        		linkImageTo(child, data);
			}
        } else data = new ImageSet(tag.getTagValue(), "", tag);
        formatToolTipFor(data);
        return data;
    }
    
    /** 
     * Transforms the specified {@link DataObject} into its corresponding
     * visualisation element.
     * 
     * @param project   The {@link DataObject} to transform. 
     *                  Must be an instance of {@link ProjectData}.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                  retrieving the data.                  
     * @return See below.
     */
    private static Set transformProject(DataObject project, long userID,
                                        long groupID)
    {
        Set set = new HashSet(1);
        set.add(project);
        return transformProjects(set, userID, groupID);
    }
    
    /**
     * Transforms a Datasets/Images hierarchy into a visualisation
     * tree. 
     * 
     * @param datasets  Collection of {@link DatasetData}s to transform.
     *                  Mustn't be <code>null</code>.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                  retrieving the data.                
     * @return Collection of corresponding {@link ImageDisplay}s.
     */
    private static Set transformDatasets(Set datasets, long userID,
                                        long groupID)
    {
        if (datasets == null) 
            throw new IllegalArgumentException("No datasets.");
        Set results = new HashSet();
        Iterator i = datasets.iterator();
        DataObject ho;
        while (i.hasNext()) {
            //create datasetNode.
            ho = (DataObject) i.next();
            if (isReadable(ho, userID, groupID))
                results.add(linkImages(ho, userID, groupID));
        }  
        return results;
    }
    
    
    /** 
     * Transforms the specified {@link DataObject} into its corresponding
     * visualisation element.
     * 
     * @param dataset   The {@link DataObject} to transform.
     *                  Must be an instance of {@link DatasetData}.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                  retrieving the data.               
     * @return See below.
     */
    private static Set transformDataset(DataObject dataset, long userID,
                                        long groupID)
    {
        Set set = new HashSet(1);
        set.add(dataset);
        return transformDatasets(set, userID, groupID);
    }
    
    /**
     * Transforms a CategoryGroup/Category/Images hierarchy into a visualisation
     * tree. 
     * 
     * @param groups    Collection of {@link CategoryGroupData}s to transform.
     *                  Mustn't be <code>null</code>.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                  retrieving the data.                
     * @return Collection of corresponding {@link ImageDisplay}s.
     */
    private static Set transformCategoryGroups(Set groups, long userID,
                                            long groupID)
    {
        if (groups == null) throw new IllegalArgumentException("No groups.");
        Set results = new HashSet();
        Iterator i = groups.iterator(), j;
        //DataObject
        CategoryGroupData  cgData;
        DataObject child;
        //Visualisation object.
        ImageSet group;  
        Set categories;
        String note = "";
        while (i.hasNext()) {
            cgData = (CategoryGroupData) i.next();
            categories = cgData.getCategories();
            if (categories != null) note = LEFT+categories.size()+RIGHT;
            group = new ImageSet(cgData.getName(), note, cgData);
            formatToolTipFor(group);
            
            if (categories != null) {
                j = categories.iterator();
                while (j.hasNext()) {
                    child = (DataObject) j.next();
                    if (isReadable(child, userID, groupID))
                        group.addChildDisplay(linkImages(child, userID, 
                                                groupID));
                }     
            }
            results.add(group); //add the group ImageSet 
        }
        return results;
    }
    
    /** 
     * Transforms the specified {@link CategoryGroupData} into its corresponding 
     * visualisation element.
     * 
     * @param data      The {@link CategoryGroupData} to transform.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                  retrieving the data. 
     * @return See below.
     */
    private static Set transformCategoryGroup(CategoryGroupData data, 
                                        long userID, long groupID)
    {
        Set set = new HashSet(1);
        set.add(data);
        return transformCategoryGroups(set, userID, groupID);
    }
    
    /**
     * Transforms a Category/Images hierarchy into a visualisation tree. 
     * 
     * @param categories    Collection of {@link CategoryData}s to transform.
     * @param userID        The id of the current user.
     * @param groupID       The id of the group the current user selects when 
     *                      retrieving the data. 
     * @return Set of corresponding {@link ImageDisplay}s.
     */
    private static Set transformCategories(Set categories, long userID, 
                                            long groupID)
    {
        if (categories == null) 
            throw new IllegalArgumentException("No categories.");
        Set results = new HashSet();
        Iterator i = categories.iterator();
        CategoryData data;
        ImageSet parent;
        Set images;
        String note = "";
        while (i.hasNext()) {
            data = (CategoryData) i.next();
            if (isReadable(data, userID, groupID)) {
                images = data.getImages();
                if (images != null) note = LEFT+images.size()+RIGHT;
                parent = new ImageSet(data.getName(), note, data);
                formatToolTipFor(parent);
                linkImagesTo(images, parent, userID, groupID);
                results.add(parent);
            } 
        }
        return results;
    }
    
    /** 
     * Transforms the specified {@link CategoryData} into its corresponding 
     * visualisation element.
     * 
     * @param data      The {@link CategoryData} to transform.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                      retrieving the data. 
     * @return See below.
     */
    private static Set transformCategory(CategoryData data, long userID, 
                                        long groupID)
    {
        Set set = new HashSet(1);
        set.add(data);
        return transformCategories(set, userID, groupID);
    }
    
    /** 
     * Transforms a set of {@link DataObject}s into their corresponding 
     * visualization objects. The elements of the set can either be
     * {@link ProjectData}, {@link DatasetData}, {@link CategoryGroupData},
     * {@link CategoryData} or {@link ImageData}.
     * The {@link ImageData}s are added to an unclassified {@link ImageSet}.
     * 
     * @param dataObjects   The {@link DataObject}s to transform.
     *                      Mustn't be <code>null</code>.
     * @param userID        The id of the current user.
     * @param groupID       The id of the group the current user selects when 
     *                      retrieving the data.                      
     * @return See above.
     */
    public static Set transformHierarchy(Set dataObjects, long userID, 
                                        long groupID)
    {
        if (dataObjects == null)
            throw new IllegalArgumentException("No objects.");
        Set results = new HashSet();
        Iterator i = dataObjects.iterator();
        DataObject ho;
        ImageSet unclassified = null;
        ImageDisplay child;
        while (i.hasNext()) {
            ho = (DataObject) i.next();
            if (ho instanceof ProjectData)
                results.add(getFirstElement(transformProject(ho, userID, 
                                        groupID)));
            else if (ho instanceof DatasetData)
                results.add(getFirstElement(transformDataset(ho, userID, 
                                            groupID)));
            else if (ho instanceof CategoryGroupData)
                results.add(getFirstElement(
                        transformCategoryGroup((CategoryGroupData) ho, userID, 
                                                groupID)));
            else if (ho instanceof CategoryData)
                results.add(getFirstElement(
                        transformCategory((CategoryData) ho, userID, 
                                            groupID)));
            else if (ho instanceof ImageData) {
                if (isReadable(ho, userID, groupID)) {
                    if (unclassified == null) {
                        unclassified = new ImageSet(UNCLASSIFIED, new Object());
                        formatToolTipFor(unclassified);
                    }
                    linkImageTo((ImageData) ho, unclassified);
                }
            } else if (ho instanceof TagAnnotationData) {
            	child = transformTag((TagAnnotationData) ho, userID, 
			            groupID);
            	if (child != null) results.add(child);
            } else if (ho instanceof WellData) {
            	child = transformWell((WellData) ho, userID, groupID);
            	if (child != null) results.add(child);
            }
        }
        if (unclassified != null) results.add(unclassified);
        return results;
    }
    
    /** 
     * Transforms a set of {@link DataObject}s into their corresponding 
     * visualization objects. The elements of the set only be {@link ImageData}.
     * The {@link ImageData}s are added to a {@link ImageSet}.
     * 
     * @param dataObjects   The {@link DataObject}s to transform.
     *                      Mustn't be <code>null</code>.
     * @param userID        The id of the current user.
     * @param groupID       The id of the group the current user selects when 
     *                      retrieving the data.                   
     * @return See above.
     */
    public static Set transformObjects(Collection dataObjects, long userID,
                                    long groupID)
    {
        if (dataObjects == null)
            throw new IllegalArgumentException("No objects.");
        Set results = new HashSet();
        DataObject ho;
        Iterator i = dataObjects.iterator();
        while (i.hasNext()) {
            ho = (DataObject) i.next();
            if (isReadable(ho, userID, groupID) && ho instanceof ImageData)
                results.add(linkImageTo((ImageData) ho, null));
        }
        return results;
    }
    
    /** 
     * Transforms a set of {@link DataObject}s into their corresponding 
     * visualization objects. The elements of the set only be {@link ImageData}.
     * The {@link ImageData}s are added to a {@link ImageSet}.
     * 
     * @param dataObjects   The {@link DataObject}s to transform.
     *                      Mustn't be <code>null</code>.
     * @param userID        The id of the current user.
     * @param groupID       The id of the group the current user selects when 
     *                      retrieving the data.                   
     * @return See above.
     */
    public static Set transformImages(Collection dataObjects, long userID,
                                    long groupID)
    {
        if (dataObjects == null)
            throw new IllegalArgumentException("No objects.");
        Set results = new HashSet();
        DataObject ho;
        Iterator i = dataObjects.iterator();
        while (i.hasNext()) {
            ho = (DataObject) i.next();
            if (isReadable(ho, userID, groupID) && ho instanceof ImageData)
                results.add(linkImageTo((ImageData) ho, null));
        }
        return results;
    }
    
    /** 
     * Transforms a {@link DataObject} into its corresponding visualization
     * object. The object can either be
     * {@link ProjectData}, {@link DatasetData}, {@link CategoryGroupData},
     * {@link CategoryData} or {@link ImageData}.
     * The {@link ImageData}s are added to an unclassified {@link ImageSet}.
     * 
     * @param ho        The {@link DataObject} to transform.
     *                  Mustn't be <code>null</code>.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                      retrieving the data.              
     * @return See above.
     */
    public static Set transform(DataObject ho, long userID, long groupID)
    {
        if (ho == null) throw new IllegalArgumentException("No objects.");
        Set s = new HashSet(1);
        s.add(ho);
        return transformHierarchy(s, userID, groupID);
    }
    
    /**
     * Returns <code>true</code> if the specified data object is readable,
     * <code>false</code> otherwise, depending on the permission.
     * 
     * @param ho        The data object to check.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                  retrieving the data.
     * @return See above.
     */
    public static boolean isReadable(DataObject ho, long userID, long groupID)
    {
        PermissionData permissions = ho.getPermissions();
        long objectOwnerID = ho.getOwner().getId();
       
        if (userID == objectOwnerID) return permissions.isUserRead();
        /*
        Set groups = ho.getOwner().getGroups();
        Iterator i = groups.iterator();
        long id = -1;
        boolean groupRead = false;
        while (i.hasNext()) {
            id = ((GroupData) i.next()).getId();
            if (groupID == id) {
                groupRead = true;
                break;
            }
        }
        if (groupRead) 
        */
        return permissions.isGroupRead();
       // return permissions.isWorldRead();
    }
    
    /**
     * Returns <code>true</code> if the specified data object is readable,
     * <code>false</code> otherwise, depending on the permission.
     * 
     * @param ho        The data object to check.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                  retrieving the data.
     * @return See above.
     */
    public static boolean isWritable(DataObject ho, long userID, long groupID)
    {
        PermissionData permissions = ho.getPermissions();
        if (userID == ho.getOwner().getId())
            return permissions.isUserWrite();
        /*
        Set groups = ho.getOwner().getGroups();
        Iterator i = groups.iterator();
        long id = -1;
        boolean groupRead = false;
        while (i.hasNext()) {
            id = ((GroupData) i.next()).getId();
            if (groupID == id) {
                groupRead = true;
                break;
            }
        }
        if (groupRead) 
        */
        return permissions.isGroupWrite();
        //return permissions.isWorldWrite();
    }
    
}
