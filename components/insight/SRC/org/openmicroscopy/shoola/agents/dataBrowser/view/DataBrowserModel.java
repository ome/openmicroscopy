/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowserModel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;



//Java imports
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.AnnotatedFilter;
import org.openmicroscopy.shoola.agents.dataBrowser.CommentsFilter;
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.DataFilter;
import org.openmicroscopy.shoola.agents.dataBrowser.DataObjectCreator;
import org.openmicroscopy.shoola.agents.dataBrowser.DataObjectSaver;
import org.openmicroscopy.shoola.agents.dataBrowser.DatasetsLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.RateFilter;
import org.openmicroscopy.shoola.agents.dataBrowser.ReportLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.TabularDataLoader;
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
import org.openmicroscopy.shoola.agents.dataBrowser.visitor.ResetThumbnailVisitor;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.model.ApplicationData;
import org.openmicroscopy.shoola.env.data.model.TableResult;
import org.openmicroscopy.shoola.env.data.util.FilterContext;
import omero.gateway.SecurityContext;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;
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

	/** The number of loaders to use for the thumbnails.*/
	static final int MAX_LOADER = 4;
	
	/** Identifies the <code>DatasetsModel</code>. */
	static final int DATASETS = DataBrowser.DATASETS;
	
	/** Identifies the <code>ImagesModel</code>. */
	static final int IMAGES = DataBrowser.IMAGES;
	
	/** Identifies the <code>ProjectsModel</code>. */
	static final int PROJECTS = DataBrowser.PROJECTS;
	
	/** Identifies the <code>SearchModel</code>. */
	static final int SEARCH = DataBrowser.SEARCH;
	
	/** Identifies the <code>TagSetsModel</code>. */
	static final int TAGSETS = DataBrowser.TAGSETS;
	
	/** Identifies the <code>WellsModel</code>. */
	static final int WELLS = DataBrowser.WELLS;
	
	/** Identifies the <code>TagsModel</code>. */
	static final int TAGS = DataBrowser.TAGS;
	
	/** Identifies the <code>PlatesModel</code>. */
	static final int PLATES = DataBrowser.PLATES;
	
	/** Identifies the <code>GroupModel</code>. */
	static final int GROUP = DataBrowser.GROUP;
	
	/** Identifies the <code>FSFolderModel</code>. */
	static final int FS_FOLDER = DataBrowser.FS_FOLDER;
	
	/** Holds one of the state flags defined by {@link DataBrowser}. */
    protected int state;
    
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
    
    /** The collection of external applications if any. */
    private List<ApplicationData> applications;
    
    /** Sets the experimenter. */
    private ExperimenterData	  experimenter;
    
	/** Flag indicating that the thumbnails are loaded or not. */
	protected boolean			thumbnailLoaded;
	
    /** Reference to the component that embeds this model. */
    protected DataBrowser		component;
    
    /** Reference to the browser. */
    protected Browser			browser;
    
    /** Reference to the browser. */
    protected ImageTableView	tableView;
    
    /** Reference to the SearchResultView. */
    protected SearchResultView searchResultView;
    
    /** The number of images. */
    protected int				numberOfImages;
    
    /** The number of images loaded. */
    protected int				imagesLoaded;
    
	/** The parent of the nodes. Used as back pointer. */
    protected Object			parent;
	
    /** The grandparent of the node. Used as back pointer. */
    protected Object 			grandParent;
    
    /** The security context.*/
    protected SecurityContext ctx;
    
	/** The display mode.*/
    protected int displayMode;
    
    /**
	 * Invokes the value is not set. 
	 */
	private void checkDefaultDisplayMode()
	{
		Integer value = (Integer) DataBrowserAgent.getRegistry().lookup(
    			LookupNames.DATA_DISPLAY);
		if (value == null) setDisplayMode(LookupNames.EXPERIMENTER_DISPLAY);
		else setDisplayMode(value.intValue());
	}
	/**
	 * Indicates to load all annotations available if the user can annotate
	 * and is an administrator/group owner or to only load the user's
	 * annotation.
	 * 
	 * @param ho The object to handle.
	 * @return See above
	 */
	private boolean canRetrieveAll(Object ho)
	{
		if (!canAnnotate(ho)) return false;
		//check the group level
		long groupID = -1;
		if (ho instanceof DataObject) {
			DataObject data = (DataObject) ho;
			groupID = data.getGroupId();
		}
		GroupData group = getGroup(groupID);
		if (group == null) return false;
		if (GroupData.PERMISSIONS_GROUP_READ ==
			group.getPermissions().getPermissionsLevel()) {
			if (DataBrowserAgent.isAdministrator()) return true;
			Set leaders = group.getLeaders();
			Iterator i = leaders.iterator();
			long userID = getCurrentUser().getId();
			ExperimenterData exp;
			while (i.hasNext()) {
				exp = (ExperimenterData) i.next();
				if (exp.getId() == userID)
					return true;
			}
			return false;
		}
		return true;
	}
	
	/**
	 * Returns the user currently logged in.
	 * 
	 * @return See above.
	 */
	ExperimenterData getCurrentUser()
	{
		return DataBrowserAgent.getUserDetails();
	}
	
    /** 
     * Creates a new instance.
     * 
     * @param ctx The security context.
     */
    DataBrowserModel(SecurityContext ctx)
    {
    	sorter = new ViewerSorter();
    	state = DataBrowser.NEW;
    	this.ctx = ctx;
    	checkDefaultDisplayMode();
    }
    
	/**
	 * Returns <code>true</code> if the specified object can be annotated,
	 * <code>false</code> otherwise, depending on the permission.
	 * 
	 * @param ho The data object to check.
	 * @return See above.
	 */
    boolean canAnnotate(Object ho)
	{
		long id = DataBrowserAgent.getUserDetails().getId();
		boolean b = EditorUtil.isUserOwner(ho, id);
		if (b) return b; //user it the owner.
		if (!(ho instanceof DataObject)) return false;
		DataObject data = (DataObject) ho;
		return data.canAnnotate();
	}
    
    /**
     * Returns the parent of the nodes if any.
     * 
     * @return See above.
     */
    Object getParent() { return parent; }
    
    /**
     * Returns the experimenter.
     * 
     * @return See above.
     */
    Object getExperimenter() { return experimenter; }
    
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
    		Layout layout = LayoutFactory.createLayout(type, sorter, 0);
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
     * Creates or recycles the {@link SearchResultView}
     * 
     * @return See above.
     */
    SearchResultView createSearchResultView()
    {
        if (searchResultView != null)
            return searchResultView;
        searchResultView = new SearchResultView((ImageDisplay)browser.getUI(), (AdvancedResultSearchModel)this);
        
        return searchResultView;
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
    	List<DataBrowserLoader> loaders = createDataLoader(refresh, ids);
    	if (loaders == null) {
    		state = DataBrowser.READY;
    		return;
    	}
    	state = DataBrowser.LOADING;
    	Iterator<DataBrowserLoader> i = loaders.iterator();
    	while (i.hasNext()) {
			i.next().load();
		}
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
    
    /** Cancels any-going fields loading. */
    void cancelFieldsLoading()
    {
    	if (fieldsLoader != null) fieldsLoader.cancel();
    }
    
    /**
     * Sets the grand parent of the browsed nodes.
     * 
     * @param grandParent The value to set.
     */
    void setGrandParent(Object grandParent) { this.grandParent = grandParent; }
    
    /**
     * Returns the grand parent.
     * 
     * @return See above.
     */
    Object getGrandParent() { return grandParent; }
    
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
     * Returns the SearchResultView.
     * 
     * @return See above.
     */
    SearchResultView getSearchView() {
        return searchResultView;
    }
    
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
     * @param imageID The id of the image or to the object of reference
     * which the thumbnail belongs.
     * @param thumb The thumbnail pixels.
     * @param valid Pass <code>true</code> if it is a valid thumbnail,
     * <code>false</code> otherwise.
     * @param maxEntries The number of thumbnails to load.
     * @return The percentage of processed data.
     */
    int setThumbnail(Object ref, BufferedImage thumb, boolean valid,
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

        thumbsManager.setThumbnail(ref, thumb, valid);
        int perc = thumbsManager.getPercentDone();
        if (thumbsManager.isDone()) {
            state = DataBrowser.READY;
            thumbsManager = null;
        }
        return perc;
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
		RateFilter loader = new RateFilter(component, ctx, rate, nodes);
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
		TagsFilter loader = new TagsFilter(component, ctx, tags, nodes);
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
		CommentsFilter loader = new CommentsFilter(component, ctx, comments,
				nodes);
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
		DataFilter loader = new DataFilter(component, ctx, context, nodes);
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
		AnnotatedFilter loader = new AnnotatedFilter(component, ctx,
				annotationType, annotated, nodes);
		loader.load();
	}
	
	/** Starts an asynchronous call to load the existing tags. */
	void fireTagsLoading()
	{
		state = DataBrowser.LOADING;
		TagsLoader loader = new TagsLoader(component, ctx,
				canRetrieveAll(parent));
		loader.load();
	}
	
	/** Starts an asynchronous call to load the existing datasets. */
	void fireExisitingDatasetsLoading()
	{
		DatasetsLoader loader = new DatasetsLoader(component, ctx);
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
			ThumbnailLoader loader = new ThumbnailLoader(component, ctx,
					nodes, false, ThumbnailLoader.IMAGE, nodes.size());
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
		DataObjectCreator loader = new DataObjectCreator(component, ctx,
				p, data, images);
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
		DataObjectSaver loader = new DataObjectSaver(component, ctx, datasets, 
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
		ReportLoader loader = new ReportLoader(component, ctx, types, 
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
		if (tags == null) return;
		Iterator i = tags.iterator();
		List l = new ArrayList();
		TagAnnotationData tag;
		String ns;
		while (i.hasNext()) {
			tag = (TagAnnotationData) i.next();
			ns = tag.getNameSpace();
			if (!TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns))
				l.add(tag);
		}
		existingTags = sorter.sort(l); 
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
	Collection getExistingDatasets() { return existingDatasets; }
	
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
	 * Returns <code>true</code> if the parent is writable,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean canLinkParent()
	{
		if (DataBrowserAgent.isAdministrator()) return true;
		switch (getDisplayMode()) {
			case LookupNames.GROUP_DISPLAY:
				if (parent == null || !(parent instanceof DataObject))
					return false;
				return ((DataObject) parent).canLink();
			case LookupNames.EXPERIMENTER_DISPLAY:
			default:
				long userID = DataBrowserAgent.getUserDetails().getId();
				if (parent == null) {
					if (experimenter == null) return false;
					return experimenter.getId() == userID;
				}
				if (!(parent instanceof DataObject)) {
					if (experimenter == null) return false;
					return experimenter.getId() == userID;
				}
				return EditorUtil.isUserOwner(parent, userID);
		}
	}
	
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
	
	/**
	 * Sets the collection of external applications.
	 * 
	 * @param applications The value to set.
	 */
	void setApplicationData(List<ApplicationData> applications)
	{
		this.applications = applications;
	}
	
	/**
	 * Sets the experimenter.
	 * 
	 * @param experimenter The value to set.
	 */
	void setExperimenter(ExperimenterData experimenter)
	{
		this.experimenter = experimenter;
	}

	/**
	 * Returns the collections of applications.
	 * 
	 * @return See above.
	 */
	List<ApplicationData> getApplications() { return applications; }
	
	/** 
	 * Starts an asynchronous call to load the tabular data.
	 * 
	 * @param data The data to load.
	 */
	void fireTabularDataLoading(List<FileAnnotationData> data)
	{
		TabularDataLoader loader = null;
		if (data == null) {
			if (this instanceof WellsModel) {
				if (grandParent instanceof ScreenData) {
					loader = new TabularDataLoader(component, ctx,
							(DataObject) grandParent,
							canRetrieveAll(grandParent));
					loader.load();
					if (parent instanceof PlateData) {
						loader = new TabularDataLoader(component, ctx,
								(DataObject) parent, canRetrieveAll(parent));
						loader.load();
					}
				} else if (parent instanceof PlateData) {
					loader = new TabularDataLoader(component, ctx,
							(DataObject) parent, canRetrieveAll(parent));
					loader.load();
				}
			}
		} else if (data.size() > 0) {
			List<Long> ids = new ArrayList<Long>();
			Iterator<FileAnnotationData> i = data.iterator();
			while (i.hasNext()) {
				ids.add(i.next().getFileID());
			}
			loader = new TabularDataLoader(component, ctx, ids,
					canRetrieveAll(parent));
			loader.load();
		}
	}
	
	/**
	 * Sets the tabular data.
	 * 
	 * @param data The value to set.
	 */
	void setTabularData(List<TableResult> data)
	{
		if (this instanceof WellsModel) {
			((WellsModel) this).setTabularData(data);
		}
	}
	
	/**
	 * Returns the selected layout.
	 * 
	 * @return See above.
	 */
	int getLayoutIndex()
	{
		Browser b = getBrowser();
		if (b == null) return LayoutFactory.SQUARY_LAYOUT;
		Layout layout = b.getSelectedLayout();
		if (layout == null) return LayoutFactory.SQUARY_LAYOUT;
		return layout.getIndex();
	}
	
	/**
	 * Returns the security context.
	 * 
	 * @return See above.
	 */
	SecurityContext getSecurityContext() { return ctx; }
	
	/**
	 * Returns <code>true</code> if the user belongs to only one group,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isSingleGroup()
	{
		Collection l = DataBrowserAgent.getAvailableUserGroups();
		return l.size() <= 1;
	}
	
	/**
	 * Returns the group corresponding to the specified id or <code>null</code>.
	 * 
	 * @param groupId The identifier of the group.
	 * @return See above.
	 */
	GroupData getGroup(long groupId)
	{
		Collection groups = DataBrowserAgent.getAvailableUserGroups();
		if (groups == null) return null;
		Iterator i = groups.iterator();
		GroupData group;
		while (i.hasNext()) {
			group = (GroupData) i.next();
			if (group.getId() == groupId) return group;
		}
		return null;
	}
	
	/**
	 * Returns the display mode. One of the constants defined by 
	 * {@link LookupNames}.
	 * 
	 * @return See above.
	 */
	int getDisplayMode() { return displayMode; }
	
	/**
	 * Returns <code>true</code> if the thumbnails have been loaded
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasThumbnailsBeenLoaded() { return loader != null; }
	
	/**
	 * Sets the display mode.
	 * 
	 * @param value The value to set.
	 */
	void setDisplayMode(int value)
	{
		if (value < 0) {
			checkDefaultDisplayMode();
			return;
		}
		switch (value) {
			case LookupNames.EXPERIMENTER_DISPLAY:
			case LookupNames.GROUP_DISPLAY:
				displayMode = value;
				break;
			default:
				displayMode = LookupNames.EXPERIMENTER_DISPLAY;
		}
		if (existingDatasets != null) {
			existingDatasets.clear();
			setExistingDatasets(null);
		}
		if (existingTags != null) {
			existingTags.clear();
			setTags(null);
		}
	}

	/**
	 * Creates a collection of loaders for the thumbnails.
	 * 
	 * @param images The objects to load.
	 * @return See above.
	 */
	List<DataBrowserLoader> createThumbnailsLoader(List<DataObject> images)
	{
		if (images == null) return null;
		List<DataBrowserLoader> loaders = new ArrayList<DataBrowserLoader>();
		int n = images.size();
		int diff = n/MAX_LOADER;
		List<DataObject> l;
		int j;
		int step = 0;
		if (n < MAX_LOADER) diff = 1;
		for (int k = 0; k < MAX_LOADER; k++) {
			l = new ArrayList<DataObject>();
			j = step+diff;
			if (k == (MAX_LOADER-1)) j += (n-j);
			if (j <= n) {
				l = images.subList(step, j);
				step += l.size();
			}
			if (l.size() > 0) {
				loaders.add(new ThumbnailLoader(component, ctx, l, n));
			}
		}
		return loaders;
	}

    /**
     * Returns <code>true</code> if the user is a system user e.g. root
     * <code>false</code> otherwise.
     *
     * @param id The identifier of the user.
     * @return See above.
     */
    boolean isSystemUser(long id)
    {
        return DataBrowserAgent.getRegistry().getAdminService().isSystemUser(id);
    }

    /**
     * Returns <code>true</code> if the user is a system user e.g. root
     * <code>false</code> otherwise.
     *
     * @param id The identifier of the user.
     * @param key One of the constants defined by <code>GroupData</code>.
     * @return See above.
     */
    boolean isSystemUser(long id, String key)
    {
        return DataBrowserAgent.getRegistry().getAdminService().isSystemUser(
                id, key);
    }

    /**
     * Returns <code>true</code> if the group is a system group e.g. System
     * <code>false</code> otherwise.
     *
     * @param id The identifier of the group.
     * @param key One of the constants defined by <code>GroupData</code>.
     * @return See above.
     */
    boolean isSystemGroup(long id, String key)
    {
        return DataBrowserAgent.getRegistry().getAdminService().isSecuritySystemGroup(id, key);
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
    protected abstract List<DataBrowserLoader> createDataLoader(boolean refresh,
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
