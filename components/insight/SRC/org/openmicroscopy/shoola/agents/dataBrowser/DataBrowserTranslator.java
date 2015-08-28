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


import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageSet;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.WellImageSet;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.WellSampleNode;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.MultiImageData;
import pojos.PermissionData;
import pojos.ProjectData;
import pojos.TagAnnotationData;
import pojos.WellData;
import pojos.WellSampleData;

/** 
 * This class contains a collection of utility static methods that transform
 * an hierarchy of {@link DataObject}s into a visualization tree.
 * The tree is then displayed in the DataBrowser. For example,
 * A list of Projects-Datasets-Images is passed to the 
 * {@link #transformProjects(Set)} method and transforms into a set
 * of ImageSet-ImageSet-ImageNode.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class DataBrowserTranslator 
{
	
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
     * @param set The set to analyze.
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
     * Transforms each {@link ImageData} object into a visualization object
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
        //formatToolTipFor(node);  
        provider.setImageNode(node);
        if (parent != null) parent.addChildDisplay(node);
        return node;
    }
    
    /** 
     * Transforms each {@link ExperimenterData} object into a visualization 
     * object i.e. {@link ImageNode}.
     * Then adds the newly created {@link ImageNode} to the specified 
     * {@link ImageSet parent}. 
     * 
     * @param is        The {@link ExperimenterData} to transform.
     * @param parent    The {@link ImageSet parent} of the image node.
     * @return  The new created {@link ImageNode}.
     */
    private static ImageNode linkExperimenterTo(ExperimenterData is, 
    		ImageSet parent)
    {
    	long id = is.getId();
    	String name = "";
    	if (id >= 0) name = is.getFirstName()+" "+is.getLastName();
        ThumbnailProvider provider = new ThumbnailProvider(is);
        ImageNode node = new ImageNode(name, is, provider);
        //formatToolTipFor(node);  
        provider.setImageNode(node);
        if (parent != null) parent.addChildDisplay(node);
        return node;
    }
    
    /** 
     * Transforms each {@link FileData} object into a visualization 
     * object i.e. {@link ImageNode}.
     * Then adds the newly created {@link ImageNode} to the specified 
     * {@link ImageSet parent}. 
     * 
     * @param is        The {@link FileData} to transform.
     * @param parent    The {@link ImageSet parent} of the image node.
     * @return  The new created {@link ImageNode}.
     */
    private static ImageNode linkFileTo(FileData is, ImageSet parent)
    {
    	long id = is.getId();
    	String name = "";
    	if (id >= 0) name = is.getName();
        ThumbnailProvider provider = new ThumbnailProvider(is);
        ImageNode node = new ImageNode(name, is, provider);
        //formatToolTipFor(node);  
        provider.setImageNode(node);
        if (parent != null) parent.addChildDisplay(node);
        return node;
    }
    
    /** 
     * Transforms each {@link ImageData} object contained in the specified
     * list into a visualization object i.e. {@link ImageNode}.
     * Then adds the newly created {@link ImageNode} to the specified 
     * {@link ImageSet parent}. 
     * 
     * @param images    Collection of {@link ImageData}s.
     * @param parent    The {@link ImageSet} corresponding to the
     *                  {@link DataObject} containing the images.
     */
    private static void linkImagesTo(Set images, ImageSet parent)
    {
        if (images == null || parent == null) return;
        Iterator i = images.iterator();
        ImageData child;
        while (i.hasNext()) {
            child = (ImageData) i.next();
            linkImageTo(child, parent);
        }  
    }
    
    /** 
     * Links the images contained into the specified {@link DataObject} 
     * to {@link ImageSet} corresponding to the {@link DataObject}.
     * 
     * @param uo        Parent object. Either an instance of 
     * 					{@link DatasetData}.
     * @return  The corresponding {@link ImageDisplay} or <code>null</code>.
     */
    private static ImageDisplay linkImages(DataObject uo)
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
            linkImagesTo(images, node);
        }
        return node;
    }
    
    /**
     * Transforms a Projects/Datasets/Images hierarchy into a visualization
     * tree. 
     * 
     * @param projects  Collection of {@link ProjectData}s to transform.
     *                  Mustn't be <code>null</code>.
     * @return Collection of corresponding {@link ImageDisplay}s.
     */
    private static Set transformProjects(Set projects)
    {
        if (projects == null) 
            throw new IllegalArgumentException("No projects.");
        Set results = new HashSet();
        Iterator i = projects.iterator(), j;
        //DataObject
        ProjectData ps;
        DataObject child;
        //visualization object.
        ImageSet project;  
        Set datasets;
        //String note = "";
        StringBuffer buffer;
        while (i.hasNext()) {
            ps = (ProjectData) i.next();
            buffer = new StringBuffer();
            datasets = ps.getDatasets();
            if (datasets != null) {
            	buffer.append(LEFT);
            	buffer.append(datasets.size());
            	buffer.append(RIGHT);
            }
            project = new ImageSet(ps.getName(), buffer.toString(), ps);
            formatToolTipFor(project);
            if (datasets != null) {
                j = datasets.iterator();
                while (j.hasNext()) {
                    child = (DataObject) j.next();
                    project.addChildDisplay(linkImages(child));
                }     
            }
            results.add(project);
        }
        return results;
    }
 
    /**
     * Transforms the specified <code>Well</code> object into its corresponding
     * visualization object.
     *  
     * @param data The <code>Well</code> to transform
     * @return See above.
     */
    private static ImageDisplay transformWell(WellData data)
    {
    	if (data == null) 
            throw new IllegalArgumentException("No well.");
        WellSampleData wsd;
        ImageData img;
        WellImageSet node = new WellImageSet(data);
        List<WellSampleData> samples = data.getWellSamples();
        WellSampleNode child;
        int index = 0;
        if (samples == null || samples.size() == 0) {
        	img = new ImageData();
        	img.setId(-1);
        	wsd = new WellSampleData();
        	wsd.setId(-1);
        	wsd.setImage(img);
        	child = createWellImage(wsd, index, node);
        	node.addWellSample(child);
        } else {
        	Iterator<WellSampleData> i = samples.iterator();
        	while (i.hasNext()) {
				wsd = i.next();
				img = wsd.getImage();
				child = createWellImage(wsd, index, node);
				node.addWellSample(child);
				index++;
			}
        }
        return node;
    }
    
    /**
     * Creates node hosting the well sample.
     * 
     * @param wsd 	The image data to host.
     * @param index The field.
     * @param parent The parent of the node.
     * @return See above.
     */
    private static WellSampleNode createWellImage(WellSampleData wsd, int index, 
    				WellImageSet parent)
    {
    	String name = "";
    	ImageData is = wsd.getImage();
    	if (is != null && is.getId() >= 0) name = is.getName();
        ThumbnailProvider provider = new ThumbnailProvider(is);
        WellSampleNode node = new WellSampleNode(name, wsd, provider, index, 
        									parent);
        provider.setImageNode(node);
        return node;
    }
    
    /**
     * Transforms a Datasets/Images hierarchy into a visualization
     * tree. 
     * 
     * @param tag     The {@link TagAnnotationData}s to transform.
     *                Mustn't be <code>null</code>.
     * @return The corresponding {@link ImageDisplay}s.
     */
    private static ImageDisplay transformTag(TagAnnotationData tag)
    {
        if (tag == null) 
            throw new IllegalArgumentException("No tag.");
        ImageSet data = null;
        Set tags = tag.getTags();
        Set dataObjects = tag.getDataObjects();
        String note = "";
        Iterator i;
        if (tags != null && tags.size() > 0) {
        	note += LEFT+tags.size()+RIGHT;
        	data = new ImageSet(tag.getTagValue(), note, tag);
        	i = tags.iterator();
        	TagAnnotationData child;
        	while (i.hasNext()) {
        		child = (TagAnnotationData) i.next();
				data.addChildDisplay(transformTag(child));
			}
        } if (dataObjects != null && dataObjects.size() > 0) {
        	note += LEFT+dataObjects.size()+RIGHT;
        	data = new ImageSet(tag.getTagValue(), note, tag);
        	DataObject child, dataset;
        	i = dataObjects.iterator();
        	ProjectData p;
        	Iterator k;
        	Set datasets;
        	while (i.hasNext()) {
        		child = (DataObject) i.next();
        		if (child instanceof ImageData)
        			linkImageTo((ImageData) child, data);
        		else if (child instanceof DatasetData) {
        			 data.addChildDisplay(linkImages(child));
        		} else if (child instanceof ProjectData) {
        			p = (ProjectData) child;
        			datasets = p.getDatasets();
        			if (datasets != null) {
        				k = datasets.iterator();
        				while (k.hasNext()) {
							dataset = (DataObject) k.next();
							data.addChildDisplay(linkImages(dataset));
						}
        			}
        		}
        			
			}
        } else data = new ImageSet(tag.getTagValue(), "", tag);
        formatToolTipFor(data);
        return data;
    }
    
    /** 
     * Transforms the specified {@link DataObject} into its corresponding
     * visualization element.
     * 
     * @param project   The {@link DataObject} to transform. 
     *                  Must be an instance of {@link ProjectData}.
     * @return See below.
     */
    private static Set transformProject(DataObject project)
    {
        Set set = new HashSet(1);
        set.add(project);
        return transformProjects(set);
    }
    
    /**
     * Transforms a Datasets/Images hierarchy into a visualization
     * tree. 
     * 
     * @param datasets  Collection of {@link DatasetData}s to transform.
     *                  Mustn't be <code>null</code>.
     * @return Collection of corresponding {@link ImageDisplay}s.
     */
    private static Set transformDatasets(Set datasets)
    {
        if (datasets == null) 
            throw new IllegalArgumentException("No datasets.");
        Set results = new HashSet();
        Iterator i = datasets.iterator();
        DataObject ho;
        while (i.hasNext()) {
            //create datasetNode.
            ho = (DataObject) i.next();
             results.add(linkImages(ho));
        }  
        return results;
    }
    
    
    /** 
     * Transforms the specified {@link DataObject} into its corresponding
     * visualization element.
     * 
     * @param dataset   The {@link DataObject} to transform.
     *                  Must be an instance of {@link DatasetData}.
     * @return See below.
     */
    private static Set transformDataset(DataObject dataset)
    {
        Set set = new HashSet(1);
        set.add(dataset);
        return transformDatasets(set);
    }
    
    /**
     * Transforms the passed multi-image e.g. <code>.lei</code>  
     * into its corresponding visualization object.
     * 
     * @param img The object to handle
     * @return See above.
     */
    private static ImageDisplay transformMultiImage(MultiImageData img)
    {
    	ImageSet node = new ImageSet(img.getName(), img);
    	formatToolTipFor(node);
    	List<ImageData> images = img.getComponents();
    	Iterator i = images.iterator();
        ImageData child;
        while (i.hasNext()) {
            child = (ImageData) i.next();
            linkImageTo(child, node);
        }  
    	return node;
    }
    
    /** 
     * Transforms a set of {@link DataObject}s into their corresponding 
     * visualization objects. The elements of the set can either be
     * {@link ProjectData}, {@link DatasetData} or {@link ImageData}.
     * The {@link ImageData}s are added to an unclassified {@link ImageSet}.
     * 
     * @param dataObjects   The {@link DataObject}s to transform.
     *                      Mustn't be <code>null</code>.
     * @return See above.
     */
    public static Set transformHierarchy(Collection dataObjects)
    {
        if (dataObjects == null)
            throw new IllegalArgumentException("No objects.");
        Set results = new HashSet();
        Iterator i = dataObjects.iterator();
        DataObject ho;
        ImageDisplay child;
        while (i.hasNext()) {
            ho = (DataObject) i.next();
            if (ho instanceof ProjectData)
                results.add(getFirstElement(transformProject(ho)));
            else if (ho instanceof DatasetData)
                results.add(getFirstElement(transformDataset(ho)));
            else if (ho instanceof ImageData) {
            	results.add(linkImageTo((ImageData) ho, null));
            } else if (ho instanceof TagAnnotationData) {
            	child = transformTag((TagAnnotationData) ho);
            	if (child != null) results.add(child);
            } else if (ho instanceof WellData) {
            	child = transformWell((WellData) ho);
            	if (child != null) results.add(child);
            }
        }
        return results;
    }
    
    /** 
     * Transforms a set of {@link DataObject}s into their corresponding 
     * visualization objects. The elements of the set only be {@link ImageData}.
     * The {@link ImageData}s are added to a {@link ImageSet}.
     * 
     * @param dataObjects The {@link DataObject}s to transform.
     *                    Mustn't be <code>null</code>.
     * @param group The the group the current user selects when 
     * retrieving the data.
     * @return See above.
     */
    public static ImageSet transformObjects(Collection dataObjects,
                                    GroupData group)
    {
        if (dataObjects == null)
            throw new IllegalArgumentException("No objects.");
        Set results = new HashSet();
        DataObject ho;
        Iterator i = dataObjects.iterator();
        ImageSet groupNode = new ImageSet(group.getName(), group);
        while (i.hasNext()) {
            ho = (DataObject) i.next();
            if (ho instanceof ImageData)
                linkImageTo((ImageData) ho, groupNode);
        }
        return groupNode;
    }
    
    /** 
     * Transforms a set of {@link DataObject}s into their corresponding 
     * visualization objects. The elements of the set only be {@link ImageData}.
     * The {@link ImageData}s are added to a {@link ImageSet}.
     * 
     * @param dataObjects   The {@link DataObject}s to transform.
     *                      Mustn't be <code>null</code>.
     * @return See above.
     */
    public static Set<ImageDisplay> transformObjects(Collection dataObjects)
    {
        if (dataObjects == null)
            throw new IllegalArgumentException("No objects.");
        Set<ImageDisplay> results = new HashSet<ImageDisplay>();
        DataObject ho;
        Iterator i = dataObjects.iterator();
        while (i.hasNext()) {
            ho = (DataObject) i.next();
            if (ho instanceof ImageData)
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
     * @return See above.
     */
    public static Set transformImages(Collection dataObjects)
    {
        if (dataObjects == null)
            throw new IllegalArgumentException("No objects.");
        Set results = new HashSet();
        DataObject ho;
        Iterator i = dataObjects.iterator();
        while (i.hasNext()) {
            ho = (DataObject) i.next();
            if (ho instanceof ImageData)
                results.add(linkImageTo((ImageData) ho, null));
        }
        return results;
    }
    
    /** 
     * Transforms a set of {@link DataObject}s into their corresponding 
     * visualization objects. The elements of the set only be 
     * {@link ExperimenterData}.
     * The {@link ExperimenterData}s are added to a {@link ImageSet}.
     * 
     * @param dataObjects   The {@link DataObject}s to transform.
     *                      Mustn't be <code>null</code>.        
     * @return See above.
     */
    public static Set transformExperimenters(Collection dataObjects)
    {
        if (dataObjects == null)
            throw new IllegalArgumentException("No objects.");
        Set results = new HashSet();
        DataObject ho;
        Iterator i = dataObjects.iterator();
        while (i.hasNext()) {
            ho = (DataObject) i.next();
            if (ho instanceof ExperimenterData)
                results.add(linkExperimenterTo((ExperimenterData) ho, null));
        }
        return results;
    }
    
    /** 
     * Transforms a set of {@link DataObject}s into their corresponding 
     * visualization objects. The elements of the set only be {@link FileData}.
     * The {@link FileData}s are added to a {@link ImageSet}.
     * 
     * @param dataObjects   The {@link DataObject}s to transform.
     *                      Mustn't be <code>null</code>.
     * @return See above.
     */
    public static Set transformFSFolder(Collection dataObjects)
    {
        if (dataObjects == null)
            throw new IllegalArgumentException("No objects.");
        Set results = new HashSet();
        DataObject ho;
        FileData f;
        MultiImageData img;
        Iterator i = dataObjects.iterator();
        while (i.hasNext()) {
            ho = (DataObject) i.next();
            if (ho instanceof MultiImageData) {
            	results.add(transformMultiImage((MultiImageData) ho));
            } else if (ho instanceof ImageData) {
            	 results.add(linkImageTo((ImageData) ho, null));
            }
        }
        return results;
    }
    
    /** 
     * Transforms a {@link DataObject} into its corresponding visualization
     * object. The object can either be
     * {@link ProjectData}, {@link DatasetData} or {@link ImageData}.
     * The {@link ImageData}s are added to an unclassified {@link ImageSet}.
     * 
     * @param ho        The {@link DataObject} to transform.
     *                  Mustn't be <code>null</code>.
     * @return See above.
     */
    public static Set transform(DataObject ho)
    {
        if (ho == null) throw new IllegalArgumentException("No objects.");
        Set s = new HashSet(1);
        s.add(ho);
        return transformHierarchy(s);
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
