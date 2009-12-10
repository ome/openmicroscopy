/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowserModel 
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;



//Java imports
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.AnnotatedFilter;
import org.openmicroscopy.shoola.agents.dataBrowser.CommentsFilter;
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.DataFilter;
import org.openmicroscopy.shoola.agents.dataBrowser.DataObjectCreator;
import org.openmicroscopy.shoola.agents.dataBrowser.DataObjectSaver;
import org.openmicroscopy.shoola.agents.dataBrowser.DatasetsLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.RateFilter;
import org.openmicroscopy.shoola.agents.dataBrowser.ReportLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.TagsFilter;
import org.openmicroscopy.shoola.agents.dataBrowser.TagsLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.ThumbnailLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.ThumbnailsManager;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Browser;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.layout.Layout;
import org.openmicroscopy.shoola.agents.dataBrowser.layout.LayoutFactory;
import org.openmicroscopy.shoola.agents.dataBrowser.layout.LayoutUtils;
import org.openmicroscopy.shoola.agents.dataBrowser.visitor.ResetThumbnailVisitor;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.env.data.util.FilterContext;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;
import pojos.TagAnnotationData;

/** 
 * The Model component in the <code>DataBrowser</code> MVC triad.
 * This class tracks the <code>DataBrowser</code>'s state and knows how to
 * initiate data retrievals. It also knows how to store and manipulate
 * the results. However, this class doesn't know the actual hierarchy
 * the <code>DataBrowser</code> is for. Subclasses fill this gap and provide  
 * a suitable data loader. The {@link DataBrowserComponent} intercepts the 
 * results of data loadings, feeds them back to this class and fires state
 * transitions as appropriate.
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
abstract class DataBrowserModel
{

	/** Identifies the <code>DatasetsModel</code>. */
	static final int	DATASETS = 0;
	
	/** Identifies the <code>ImagesModel</code>. */
	static final int	IMAGES = 1;
	
	/** Identifies the <code>ProjectsModel</code>. */
	static final int	PROJECTS = 2;
	
	/** Identifies the <code>SearchModel</code>. */
	static final int	SEARCH = 3;
	
	/** Identifies the <code>TagSetsModel</code>. */
	static final int	TAGSETS = 4;
	
	/** Identifies the <code>WellsModel</code>. */
	static final int	WELLS = 5;
	
	/** Identifies the <code>TagsModel</code>. */
	static final int	TAGS = 4;
	
	/** Identifies the <code>PlatesModel</code>. */
	static final int	PLATES = 5;
	
	/** Holds one of the state flags defined by {@link DataBrowser}. */
    private int					state;
    
    /** Maps an image id to the list of thumbnail providers for that image. */
    private ThumbnailsManager   thumbsManager;
    
    /** Maps an image id to the list of thumbnail providers for that image. */
    private ThumbnailsManager   fullSizeThumbsManager;
    
    /** Used to sort the nodes by date or alphabetically. */
    protected ViewerSorter      sorter;
    
    /** The current fields loader. */
    private	DataBrowserLoader 	fieldsLoader;
    
    /** The current data loader. */
    private DataBrowserLoader	loader;
    
    /** The collection of existing tags. */
    private Collection			existingTags;
    
    /** The collection of existing datasets. */
    private Collection			existingDatasets;
    
	/** Flag indicating that the thumbnails are loaded or not. */
	protected boolean			thumbnailLoaded;
	
    /** Reference to the component that embeds this model. */
    protected DataBrowser		component;
    
    /** Reference to the browser. */
    protected Browser			browser;
    
    /** Reference to the browser. */
    protected ImageTableView	tableView;
    
    /** The number of images. */
    protected int				numberOfImages;
    
    /** The number of images loaded. */
    protected int				imagesLoaded;
    
	/** The parent of the nodes. Used as back pointer. */
    protected Object			parent;
	
    /** The grandparent of the node. Used as back pointer. */
    protected Object 			grandParent;
    
    /** Creates a new instance. */
    DataBrowserModel()
    {
    	sorter = new ViewerSorter();
    	state = DataBrowser.NEW; 
    }
    
    /**
     * Returns the parent of the nodes if any.
     * 
     * @return See above.
     */
    Object getParent() { return parent; }
    
     /**
     * Returns the number of images.
     * 
     * @return See above.
     */
    int getNumberOfImages() { return numberOfImages; }
    
    /**
     * Returns the {@link ViewerSorter}.
     * 
     * @return See above.
     */
    ViewerSorter getSorter() { return sorter; }
    
    /** Creates a default layout and lays out the nodes. */
    void layoutBrowser()
    {
    	layoutBrowser(LayoutFactory.SQUARY_LAYOUT);
    }
    
    /** 
     * Lays out the browser. 
     * 
     * @param type The type of layout to create.
     */
    void layoutBrowser(int type)
    {
    	if (browser == null) return;
		//Do initial layout and set the icons.
    	if (browser.getSelectedLayout() == null) {
    		Layout layout = LayoutFactory.createLayout(type, sorter, 
    				LayoutUtils.DEFAULT_PER_ROW);
            browser.setSelectedLayout(layout);
    	}
        //browser.accept(browser.getSelectedLayout(), 
        //				ImageDisplayVisitor.IMAGE_SET_ONLY);
        browser.accept(browser.getSelectedLayout());
    }
    
    /**
     * Creates or recycles the table view.
     * 
     * @return See above.
     */
    ImageTableView createImageTableView()
    {
    	if (tableView != null) return tableView;
    	tableView = new ImageTableView(this, (ImageDisplay) browser.getUI());
    	return tableView;
    }
    
    /**
     * Returns the current state.
     * 
     * @return See above.
     */
    int getState() { return state; }
    
    /**
     * Loads the data.
     * 
     * @param refresh 	Pass <code>false</code> if we retrieve the data for
     * 					the first time, <code>true</code> otherwise.
     * @param ids		The collection of image's ids or <code>null</code>.
     */
    void loadData(boolean refresh, Collection ids)
    {
    	if (refresh) 
    		browser.accept(new ResetThumbnailVisitor(ids), 
    				ImageDisplayVisitor.IMAGE_NODE_ONLY);
    	loader = createDataLoader(refresh, ids);
    	if (loader == null) {
    		state = DataBrowser.READY;
    		return;
    	}
    	state = DataBrowser.LOADING;
    	loader.load();
    }
    
    /**
     * Sets the grand parent of the browsed nodes.
     * 
     * @param grandParent The value to set.
     */
    void setGrandParent(Object grandParent) { this.grandParent = grandParent; }
    
    /**
     * Returns the browser.
     * 
     * @return See above.
     */
    Browser getBrowser() { return browser; }
    
    /**
     * Returns the browser.
     * 
     * @return See above.
     */
    ImageTableView getTableView() { return tableView; }
    
    /**
     * Called by the <code>DataBrowser</code> after creation to allow this
     * object to store a back reference to the embedding component.
     * 
     * @param component The embedding component.
     */
    void initialize(DataBrowser component)
    {
    	this.component = component;
    }

    /**
     * Sets the specified thumbnail for all image nodes in the display that
     * map to the same image hierarchy object.
     * When every image object has a thumbnail, this method sets the state
     * to {@link HiViewer#READY}.
     * 
     * @param imageID    The id of the hierarchy object to which the thumbnail 
     *                   belongs.
     * @param thumb      The thumbnail pixels.
     * @param valid		 Pass <code>true</code> if it is a valid thumbnail,
     * 					 <code>false</code> otherwise.
     * @param maxEntries The number of thumbnails to load.
     */
    void setThumbnail(long imageID, BufferedImage thumb, boolean valid, 
    		int maxEntries)
    {
        if (thumbsManager == null) {
        	if (getType() == WELLS) {
        		thumbsManager = new ThumbnailsManager(getNodes(), maxEntries);
        	} else {
        		thumbsManager = new ThumbnailsManager(
        		          browser.getVisibleImageNodes(), maxEntries);
        	}
        }

        thumbsManager.setThumbnail(imageID, thumb, valid);
        if (thumbsManager.isDone()) {
            state = DataBrowser.READY;
            thumbsManager = null;
        }
    }

    /**
     * Returns <code>true</code> if the loading is done, 
     * <code>false</code> otherwise.
     * 
     * @param imageID 	The id of the image the passed object is for.
     * @param thumb		The Buffered image.
     * @return See above.
     */
    boolean setSlideViewImage(long imageID, BufferedImage thumb)
    {
		if (fullSizeThumbsManager != null) {
			fullSizeThumbsManager.setFullSizeImage(imageID, thumb);
			if (fullSizeThumbsManager.isDone()) {
	            state = DataBrowser.READY;
	            fullSizeThumbsManager = null;
	            return true;
	        }
		}
		return false;
	}

    
    /** Discards any on-going data loading. */
	void discard()
	{
		
	}
	
	void cancelFiltering()
	{
		
	}
	
	void cancelSlideShow()
	{
		
	}
	
	/** 
	 * Sets the current state.
	 * 
	 * @param state The value to set.
	 */
	void setState(int state) { this.state = state; }
	
	/**
	 * Filters the passed <code>DataObject</code>s by rate.
	 * 
	 * @param rate	The selected rate. One of the constants defined 
	 * 				by {@link RateFilter}.
	 * @param nodes	The collection of <code>DataObject</code>s to filter.
	 */
	void fireFilteringByRate(int rate, Set nodes)
	{
		state = DataBrowser.FILTERING;
		RateFilter loader = new RateFilter(component, rate, nodes);
		loader.load();
	}
	
	/**
	 * Filters the passed <code>DataObject</code>s by tags.
	 * 
	 * @param tags	The selected tags.
	 * @param nodes	The collection of <code>DataObject</code>s to filter.
	 */
	void fireFilteringByTags(List<String> tags, Set<DataObject> nodes)
	{
		state = DataBrowser.FILTERING;
		TagsFilter loader = new TagsFilter(component, tags, nodes);
		loader.load();
	}
	
	/**
	 * Filters the passed <code>DataObject</code>s by comments.
	 * 
	 * @param comments	The selected comments.
	 * @param nodes	The collection of <code>DataObject</code>s to filter.
	 */
	void fireFilteringByComments(List<String> comments, Set<DataObject> nodes)
	{
		state = DataBrowser.FILTERING;
		CommentsFilter loader = new CommentsFilter(component, comments, nodes);
		loader.load();
	}

	/**
	 * Filters the passed <code>DataObject</code>s by context.
	 * 
	 * @param context 	The filtering context.
	 * @param nodes		The collection of <code>DataObject</code>s to filter.
	 */
	void fireFilteringByContext(FilterContext context, Set<DataObject> nodes)
	{
		state = DataBrowser.FILTERING;
		DataFilter loader = new DataFilter(component, context, nodes);
		loader.load();
	}
	
	/**
	 * Filters the passed <code>DataObject</code>s depending on the 
	 * type of annotations.
	 * 
	 * @param annotationType The type of annotation to filter by.
	 * @param annotated      Pass <code>true</code> to determine the annotated 
	 *                       nodes, <code>false</code> to determine the 
	 *                       not annotated nodes.
	 * @param nodes	The collection of <code>DataObject</code>s to filter.
	 */
	void fireFilteringByAnnotated(Class annotationType, boolean annotated, 
			                   Set<DataObject> nodes)
	{
		state = DataBrowser.FILTERING;
		AnnotatedFilter loader = new AnnotatedFilter(component, annotationType,
				                                   annotated, nodes);
		loader.load();
	}
	
	/** Starts an asynchronous call to load the existing tags. */
	void fireTagsLoading()
	{
		state = DataBrowser.LOADING;
		TagsLoader loader = new TagsLoader(component);
		loader.load();
	}
	
	/** Starts an asynchronous call to load the existing datasets. */
	void fireExisitingDatasetsLoading()
	{
		DatasetsLoader loader = new DatasetsLoader(component);
		loader.load();
	}
	
	/** 
	 * Starts an asynchronous retrieval all the full size image.
	 * 
	 * @param images The value to handle.
	 */
	void fireFullSizeLoading(Collection<ImageNode> images)
	{
		Set nodes = new HashSet<ImageData>();
		Set<ImageNode> toKeep = new HashSet<ImageNode>();
		Iterator i = images.iterator();
		ImageNode node;
		while (i.hasNext()) {
			node = (ImageNode) i.next();
			if (node.getThumbnail().getFullSizeImage() == null) {
				nodes.add(node.getHierarchyObject());
				toKeep.add(node);
			}
		}
		if (nodes.size() > 0) {
			fullSizeThumbsManager = new ThumbnailsManager(toKeep, 
					                                    toKeep.size());
			ThumbnailLoader loader = new ThumbnailLoader(component, nodes, 
														false);
			loader.load();
			state = DataBrowser.LOADING_SLIDE_VIEW;
		}
	}
	
	/**
	 * Starts an asynchronous retrieval 
	 * 
	 * @param data 		The <code>DataObject</code> to create.
	 * @param images	The images to add to the <code>DataObject</code>.
	 */
	void fireDataSaving(DataObject data, Collection images)
	{
		DataObject p = null;
		if (parent instanceof DataObject) p = (DataObject) parent;
		if (grandParent != null && grandParent instanceof DataObject)
			p = (DataObject) grandParent;
		
		//Review that code.
		if (data instanceof DatasetData) {
			if (!(p instanceof ProjectData)) p = null;
		} else if (data instanceof TagAnnotationData) {
			if (p instanceof TagAnnotationData) {
				TagAnnotationData tag = (TagAnnotationData) p;
				if (!TagAnnotationData.INSIGHT_TAGSET_NS.equals(
						tag.getNameSpace()))
					p = null;
			}
		}
		DataObjectCreator loader = new DataObjectCreator(component, p, data, 
														images);
		loader.load();
	}
	
	/**
	 * Starts an asynchronous retrieval 
	 * 
	 * @param data 		The <code>DataObject</code> to create.
	 * @param images	The images to add to the <code>DataObject</code>.
	 */
	void fireDataSaving(Collection datasets, Collection images)
	{
		DataObjectSaver loader = new DataObjectSaver(component, datasets, 
														images);
		loader.load();
	}
	
	/**
	 * Starts an asynchronous data retrieval for writing the report.
	 * 
	 * @param images 	The images to handle.
	 * @param types	 	The types of data to report.
	 * @param name 		The name of the report.
	 */
	void fireReportLoading(Collection images, List<Class> types,
			String name)
	{
		ReportLoader loader = new ReportLoader(component, types, 
				sorter.sort(images), name);
		loader.load();
	}
	
	/**
	 * Sets the collection of existing tags.
	 * 
	 * @param tags The value to set.
	 */
	void setTags(Collection tags)
	{ 
		existingTags = tags; 
	}
	
	/**
	 * Sets the collection of existing datasets.
	 * 
	 * @param datasets The value to set.
	 */
	void setExistingDatasets(Collection datasets)
	{
		existingDatasets = datasets; 
	}
	
	/**
	 * Returns the collection of existing tags.
	 * 
	 * @return See above.
	 */
	Collection getExisitingDatasets() { return existingDatasets; }
	
	/**
	 * Returns <code>true</code> if the model is of type
	 * <code>Dataset</code>, <code>Images</code>, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isImagesModel()
	{
		switch (getType()) {
			case DATASETS:
			case IMAGES:
			case TAGS:
				return true;
			default:
				return false;
		}
	}
	
	/**
	 * Returns the collection of existing tags.
	 * 
	 * @return See above.
	 */
	Collection getExistingTags() { return existingTags; }
	
	/**
	 * Returns <code>true</code> is a magnified thumbnail is displayed when
	 * the user mouses over a node, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isRollOver() { return browser.isRollOver(); }
	
	/**
	 * Returns the sorted collection.
	 * 
	 * @param collection The collection to sort.
	 * @return See above.
	 */
	List sortCollection(Collection collection)
	{
		return sorter.sort(collection);
	}
	
    /** Cancels any-going fields loading. */
    void cancelFieldsLoading()
    {
    	if (fieldsLoader != null) fieldsLoader.cancel();
    }
    
    /**
     * Loads the fields for the specified well. Returns <code>true</code>
     * if a loader was created, <code>false</code> otherwise.
     * 
     * @param row 	 The row identifying the well.
     * @param column The column identifying the well.
     * @return See above.
     */
    boolean loadFields(int row, int column)
    {
    	if (!(this instanceof WellsModel)) return false;
    	fieldsLoader = ((WellsModel) this).createFieldsLoader(row, column);
    	if (fieldsLoader == null) return false;
    	fieldsLoader.load();
    	return true;
    }
    
    /**
     * Creates a data loader that can retrieve the hierarchy objects needed
     * by this model.
     * 
     * @param refresh 	Pass <code>false</code> if we retrieve the data for
     * 					the first time, <code>true</code> otherwise.
     * @param ids       The collection of images' ids to reload.
     * @return A suitable data loader.
     */
    protected abstract DataBrowserLoader createDataLoader(boolean refresh, 
    		                                             Collection ids);

    /** 
     * Returns the type of the model. 
     * 
     * @return See above.
     */
    protected abstract int getType(); 
    
    /**
     * Returns the collection of {@link ImageDisplay}.
     * 
     * @return See above.
     */
    protected abstract List<ImageDisplay> getNodes();
    
}
