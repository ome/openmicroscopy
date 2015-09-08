/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.apache.commons.collections.CollectionUtils;

import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.agents.dataBrowser.IconManager;
import org.openmicroscopy.shoola.agents.dataBrowser.ThumbnailProvider;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Browser;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.CellDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageFinder;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Thumbnail;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.WellImageSet;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.WellSampleNode;
import org.openmicroscopy.shoola.agents.dataBrowser.visitor.FlushVisitor;
import org.openmicroscopy.shoola.agents.dataBrowser.visitor.NodesFinder;
import org.openmicroscopy.shoola.agents.dataBrowser.visitor.RegexFinder;
import org.openmicroscopy.shoola.agents.dataBrowser.visitor.ResetNodesVisitor;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImageObject;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.SelectionWizard;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.events.ViewInPluginEvent;
import org.openmicroscopy.shoola.env.data.model.ApplicationData;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;
import org.openmicroscopy.shoola.env.data.util.FilterContext;

import omero.gateway.SecurityContext;
import omero.gateway.model.TableResult;

import org.openmicroscopy.shoola.env.data.util.StructuredDataResults;
import org.openmicroscopy.shoola.env.event.EventBus;

import omero.log.LogMessage;
import omero.log.Logger;

import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.file.ExcelWriter;
import org.openmicroscopy.shoola.util.ui.RegExFactory;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;

import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageData;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.model.TextualAnnotationData;
import omero.gateway.model.WellSampleData;

/** 
 * Implements the {@link DataBrowser} interface to provide the functionality
 * required of the hierarchy viewer component.
 * This class is the component hub and embeds the component's MVC triad.
 * It manages the component's state machine and fires state change 
 * notifications as appropriate, but delegates actual functionality to the
 * MVC sub-components.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
class DataBrowserComponent 
	extends AbstractComponent
	implements DataBrowser
{

	/** The maximum number of entries before switching to the table view. */
	private static final String MAX_ENTRIES = "/views/MAX_ENTRIES";

	/* The filtering message to display in modal dialogs. */
	private static final String FILTERING_MSG = "Currently filtering data. Please wait.";
	
	/** The Model sub-component. */
	private DataBrowserModel     model;

	/** The Controller sub-component. */
	private DataBrowserControl   controller;

	/** The View sub-component. */
	private DataBrowserUI       view;
	
	/** Displays the existing datasets. */
	private void showExistingDatasets()
	{
		Collection datasets = model.getExistingDatasets();
		if (datasets == null || datasets.size() == 0) {
			UserNotifier un = DataBrowserAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Existing datasets", "No Datasets already created.");
			return;
		}
		IconManager icons = IconManager.getInstance();
		String title = "Datasets Selection";
		String text = "Select the Datasets to add the images to";
		Icon icon = icons.getIcon(IconManager.DATASET_48);
		SelectionWizard wizard = new SelectionWizard(
				DataBrowserAgent.getRegistry().getTaskBar().getFrame(),
				datasets, DatasetData.class, DataBrowserAgent.getUserDetails());
		wizard.setTitle(title, text, icon);
		wizard.addPropertyChangeListener(controller);
		UIUtilities.centerAndShow(wizard);
	}

	/**
	 * Creates a new instance.
	 * The {@link #initialize() initialize} method should be called straight 
	 * after to complete the MVC set up.
	 * 
	 * @param model The Model sub-component.
	 */
	DataBrowserComponent(DataBrowserModel model)
	{
		if (model == null) throw new NullPointerException("No model."); 
		this.model = model;
	}
	
	/** Links the components. */
	void initialize()
	{
		controller = new DataBrowserControl();
		view = new DataBrowserUI();
		controller.initialize(this, view);
		view.initialize(model, controller);
	}
	
	/**
	 * Notifies the model that the user has annotated data.
	 * 
	 * @param containers The objects to handle.
	 * @param count A positive value if annotations are added, a negative value
	 * if annotations are removed.
	 */
	void onAnnotated(List<DataObject> containers, int count)
	{
		if (containers == null || containers.size() == 0) return;
		NodesFinder visitor = new NodesFinder(containers);
		model.getBrowser().accept(visitor);
		List<ImageDisplay> nodes = visitor.getFoundNodes();
		if (nodes == null || nodes.size() == 0) return;
		Iterator<ImageDisplay> i = nodes.iterator();
		while (i.hasNext()) {
			i.next().setAnnotationCount(count);
		}
	}
	
	/** 
	 * Fires a property indicating that some rendering settings can be copied.
	 */
	void notifyRndSettingsToCopy()
	{
		if (model.getState() == DISCARDED) return;
		firePropertyChange(RND_SETTINGS_TO_COPY_PROPERTY, Boolean.FALSE, 
						Boolean.TRUE);
	}
	
	/** 
	 * Fires a property indicating that some data can be copied.
	 */
	void notifyDataToCopy()
	{
		if (model.getState() == DISCARDED) return;
		firePropertyChange(ITEMS_TO_COPY_PROPERTY, Boolean.FALSE, 
						Boolean.TRUE);
	}
	
	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#activate()
	 */
	public void activate()
	{
		//Determine the view depending on the number of image.
		Integer max = (Integer) DataBrowserAgent.getRegistry().lookup(
				MAX_ENTRIES);
		if (model.getType() == DataBrowserModel.SEARCH) {
		    view.setSelectedView(SEARCH);
		    model.loadData(false, null); 
		}
		else if (model.getNumberOfImages() <= max.intValue() ||
				model.getType() == DataBrowserModel.WELLS) {
			model.loadData(false, null); 
			if (model.getType() == DataBrowserModel.WELLS) {
				model.fireTabularDataLoading(null);
			}
			view.setSelectedView(DataBrowserUI.THUMB_VIEW);
		} else {
			view.setSelectedView(DataBrowserUI.COLUMNS_VIEW);
		}
		Browser browser = model.getBrowser();
		if (browser != null) {
	    	ResetNodesVisitor visitor = new ResetNodesVisitor(null, false);
	    	browser.accept(visitor, ImageDisplayVisitor.IMAGE_SET_ONLY);
	    	browser.addPropertyChangeListener(controller);
		}
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#discard()
	 */
	public void discard()
	{
		Browser browser = model.getBrowser();
		if (browser != null) {
			browser.accept(new FlushVisitor(),
				ImageDisplayVisitor.IMAGE_NODE_ONLY);
		}
		model.discard();
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#getHierarchyType()
	 */
	public int getHierarchyType() { return 0; }

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#getState()
	 */
	public int getState() { return model.getState(); }

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#setStatus(String, int)
	 */
	public void setStatus(String description, int perc)
	{
		int state = model.getState();
        view.setStatus(description, state != LOADING, perc);
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#setThumbnail(Object, BufferedImage, boolean, int)
	 */
	public void setThumbnail(Object ref, BufferedImage thumb, boolean valid,
			int maxEntries)
	{
		int previousState = model.getState();
		int perc = model.setThumbnail(ref, thumb, valid, maxEntries);
		view.setStatus((perc == 100) ? "Done" : "", perc == 100, perc);
		if (previousState != model.getState()) fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#setSelectedDisplays(List)
	 */
	public void setSelectedDisplays(List<ImageDisplay> nodes)
	{
		if (CollectionUtils.isEmpty(nodes)) {
			if (model instanceof WellsModel) {
				((WellsModel) model).setSelectedWells(null);
				view.onSelectedWell();
			}
			return;
		}
		if (nodes.size() == 1) {
			setSelectedDisplay(nodes.get(0));
			return;
		}
		
		final List<ImageNode> visibleNodes =
				model.getBrowser().getVisibleImageNodes();
		final List<Long> visibleObjectIds =
				new ArrayList<Long>(visibleNodes.size());
		for (final ImageNode visibleNode : visibleNodes) {
			final Object hierarchyObject = visibleNode.getHierarchyObject();
			if (hierarchyObject instanceof ImageData)
				visibleObjectIds.add(((ImageData) hierarchyObject).getId());
		}
		
		List<Object> others = new ArrayList<Object>();
		List<Object> objects = new ArrayList<Object>();
		objects.add(others);
		
		for (final ImageDisplay node : nodes) {
			final Object hierarchyObject = node.getHierarchyObject();
			if (!(hierarchyObject instanceof ImageData) ||
				visibleObjectIds.contains(
						((ImageData) hierarchyObject).getId()))
				others.add(hierarchyObject);
		}
		
		ImageDisplay node = nodes.get(0);
		Object object = node.getHierarchyObject();
		
		if (object instanceof DataObject) {
			Object parent = null;
			if (object instanceof WellSampleData) {
				WellSampleNode wsn = (WellSampleNode) node;
				parent = wsn.getParentObject();
				List<WellImageSet> wells = new ArrayList<WellImageSet>();
				wells.add(wsn.getParentWell());
				Iterator<ImageDisplay> i = nodes.iterator();
				ImageDisplay n;
				while (i.hasNext()) {
					n = i.next();
					if (n instanceof WellSampleNode) {
						wsn = (WellSampleNode) n;
						wells.add(wsn.getParentWell());
					}
				}
				((WellsModel) model).setSelectedWells(wells);
				view.onSelectedWell();
			} else {
				ImageDisplay p = node.getParentDisplay();
				if (p != null) {
					parent = p.getHierarchyObject();
					if (!(parent instanceof DataObject))
						parent = model.getParent();
				}
			}
			if (parent != null)
				objects.add(parent);
		}
		firePropertyChange(SELECTED_DATA_BROWSER_NODES_DISPLAY_PROPERTY, null, 
				objects);
	}
	
	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#setSelectedDisplay(ImageDisplay)
	 */
	public void setSelectedDisplay(ImageDisplay node)
	{
		if (node == null) {
			if (model instanceof WellsModel) {
				((WellsModel) model).setSelectedWells(null);
				view.onSelectedWell();
			}
			return;
		}
		model.getBrowser().scrollToNode(node);
		Object object = node.getHierarchyObject();
		List<Object> objects = new ArrayList<Object>();
		List<Object> others = new ArrayList<Object>(); 
		
		Collection<ImageDisplay>
		selected = model.getBrowser().getSelectedDisplays();
		Iterator<ImageDisplay> i = selected.iterator();
		ImageDisplay n;
		while (i.hasNext()) {
			n = i.next();
			if (n != node) others.add(n.getHierarchyObject());
		}
		objects.add(others);
		//Root node
		if (node.equals(model.getBrowser().getUI())) {
			objects.add(model.parent);
		} else objects.add(object);
		if (object instanceof DataObject) {
			Object parent = null;
			if (object instanceof WellSampleData) {
				WellSampleNode wsn = (WellSampleNode) node;
				parent = wsn.getParentObject();
				if (others.size() > 0) parent = null;
				List<WellImageSet> wells = new ArrayList<WellImageSet>();
				boolean in = false;
				WellImageSet well;
				i = selected.iterator();
				while (i.hasNext()) {
					n = i.next();
					if (n instanceof WellSampleNode) {
						wsn = (WellSampleNode) n;
						well = wsn.getParentWell();
						if (well.equals(wsn.getParentWell())) in = true;
						wells.add(well);
					}
				}
				if (!in) wells.add(wsn.getParentWell());
				((WellsModel) model).setSelectedWells(wells);
				view.onSelectedWell();
			} else {
				ImageDisplay p = node.getParentDisplay();
				if (p != null) {
					parent = p.getHierarchyObject();
					if (!(parent instanceof DataObject))
						parent = model.getParent();
				}
			}
			if (parent != null)
				objects.add(parent);
		}
		firePropertyChange(SELECTED_NODE_DISPLAY_PROPERTY, null, objects);
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#setApplications(List)
	 */
	public void setApplications(List<ApplicationData> applications)
	{
		model.setApplicationData(applications);
	}
	
	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#setSelectedNodes(List, List)
	 */
	public void setSelectedNodes(List<DataObject> objects, 
			List<ApplicationData> applications)
	{
		ImageTableView tbView = model.getTableView();
		if (tbView != null) tbView.setSelectedNodes(objects);
		model.getBrowser().setSelectedNodes(objects);
		model.setApplicationData(applications);
		firePropertyChange(SELECTION_UPDATED_PROPERTY, 
				Boolean.valueOf(false), Boolean.valueOf(true));
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#markUnmodifiedNodes(Class, List)
	 */
	public void markUnmodifiedNodes(Class type, Collection<Long> ids)
	{
		ImageTableView tbView = model.getTableView();
		if (tbView != null && ids != null) 
			tbView.markUnmodifiedNodes(type, ids);
		model.getBrowser().markUnmodifiedNodes(type, ids);
	}
	
	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#getUI(boolean)
	 */
	public JComponent getUI(boolean full)
	{ 
		//view.buildGUI(full);
		return view;
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#filterByRate(int)
	 */
	public void filterByRate(int rate)
	{
		if (model.getState() == FILTERING) {
			UserNotifier un = DataBrowserAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Filtering", FILTERING_MSG);
			return;
		}
		Browser browser = model.getBrowser();
		ImageFinder finder = new ImageFinder();
		browser.accept(finder, ImageDisplayVisitor.IMAGE_NODE_ONLY);
		Set nodes = finder.getImages();
		if (nodes != null && nodes.size() > 0) {
			model.fireFilteringByRate(rate, nodes);
			fireStateChange();
		}
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#showAll()
	 */
	public void showAll()
	{
		model.cancelFiltering();
		model.getBrowser().showAll();
		//model.layoutBrowser();
		view.layoutUI();
		view.setNumberOfImages(model.getNumberOfImages());
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#filterByComments(List)
	 */
	public void filterByComments(List<String> comments)
	{
		if (model.getState() == FILTERING) {
			UserNotifier un = DataBrowserAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Filtering", FILTERING_MSG);
			return;
		}
		Browser browser = model.getBrowser();
		Set<DataObject> nodes = browser.getOriginal();
		if (nodes != null && nodes.size() > 0) {
			model.fireFilteringByComments(comments, nodes);
			fireStateChange();
		}
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#filterByFullText(List)
	 */
	public void filterByFullText(List<String> terms)
	{
		if (terms == null || terms.size() == 0) {
			//showAll();
			return;
		}
		
		Browser browser = model.getBrowser();
		Pattern pattern;
		try {
			pattern = RegExFactory.createPattern(
					RegExFactory.formatSearchTextAsString(terms));
		} catch (PatternSyntaxException pse) {
            UserNotifier un = DataBrowserAgent.getRegistry().getUserNotifier();
            un.notifyInfo("Find", "Some characters are not recognised.");
            return;
        }
		//view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		RegexFinder finder = new RegexFinder(pattern);
		browser.accept(finder);
		List<ImageDisplay> nodes = finder.getFoundNodes();
		browser.setFilterNodes(nodes);
		view.layoutUI();
		view.setNumberOfImages(nodes.size());
		//view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		view.setFilterStatus(false);
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#filterByTags(List)
	 */
	public void filterByTags(List<String> tags)
	{
		if (model.getState() == FILTERING) {
			UserNotifier un = DataBrowserAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Filtering", FILTERING_MSG);
			return;
		}		
		Browser browser = model.getBrowser();
		Set<DataObject> nodes = browser.getOriginal();
		if (nodes != null && nodes.size() > 0) {
			model.fireFilteringByTags(tags, nodes);
			fireStateChange();
		}
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#setFilteredNodes(List, List)
	 */
	public void setFilteredNodes(List<DataObject> objects, List<String> names)
	{
		if (objects == null) return;
		Browser browser = model.getBrowser();
		List<ImageDisplay> nodes;
		if (objects.size() > 0) {
			NodesFinder finder = new NodesFinder(objects);
			browser.accept(finder);
			nodes = finder.getFoundNodes();
		} else {
			nodes = new ArrayList<ImageDisplay>();
		}
		
		if (names != null && names.size() > 0) {
			Pattern pattern;
			try {
				pattern = RegExFactory.createPattern(
						RegExFactory.formatSearchTextAsString(names));
			} catch (PatternSyntaxException pse) {
	            UserNotifier un = 
	            	
	            	DataBrowserAgent.getRegistry().getUserNotifier();
	            un.notifyInfo("Find", "Some characters are not recognised.");
	            return;
	        }
			RegexFinder finder = new RegexFinder(pattern);
			finder.analyse(nodes);
			nodes = finder.getFoundNodes();
		}
		browser.setFilterNodes(nodes);
		view.layoutUI();
		view.setNumberOfImages(nodes.size());
		model.setState(READY);
		//view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		view.setFilterStatus(false);
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#filterByContext(FilterContext)
	 */
	public void filterByContext(FilterContext context)
	{
		if (context == null) {
			UserNotifier un = DataBrowserAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Filtering", "No filtering context.");
			return;
		}
		if (model.getState() == FILTERING) {
			UserNotifier un = DataBrowserAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Filtering", FILTERING_MSG);
			return;
		}
		if (context.isNameOnly()) {
			view.filterByContext(context);
			filterByFullText(context.getNames()); 
		} else if (context.isTagsOnly()) {
			view.filterByContext(context);
			filterByTags(context.getAnnotation(TagAnnotationData.class)); 
		} else {
			Browser browser = model.getBrowser();
			Set<DataObject> nodes = browser.getOriginal();
			if (nodes != null && nodes.size() > 0) {
				view.filterByContext(context);
				model.fireFilteringByContext(context, nodes);
				fireStateChange();
			}
		}
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#loadExistingTags()
	 */
	public void loadExistingTags()
	{
		//Do not cache the tags
		//if (model.getExistingTags() == null)
			model.fireTagsLoading();
		//else view.setFilterStatus(false);
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#setExistingTags(Collection)
	 */
	public void setExistingTags(Collection tags)
	{
		model.setTags(tags);
		view.setTags(model.getExistingTags());
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#setSlideViewImage(long, BufferedImage)
	 */
	public void setSlideViewImage(long imageID, BufferedImage thumb)
	{
		boolean done = model.setSlideViewImage(imageID, thumb);
		if (done) view.setSlideViewStatus(true, -1);
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#setSlideViewStatus(String, int)
	 */
	public void setSlideViewStatus(String description, int perc)
	{
		int state = model.getState();
		if (state == LOADING_SLIDE_VIEW)
			view.setSlideViewStatus(false, perc);
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#createDataObject(DataObject)
	 */
	public void createDataObject(DataObject data)
	{
		if (data == null) return;
		//TODO: check state.
		if (!(data instanceof DatasetData)) return;

		Browser browser = model.getBrowser();
		Collection images;
		Collection set = browser.getSelectedDisplays();
		if (set != null && set.size() > 0) {
			images = new HashSet();
			Iterator i = set.iterator();
			ImageDisplay display;
			Object ho;
			while (i.hasNext()) {
				display = (ImageDisplay) i.next();
				ho = display.getHierarchyObject();
				if (ho instanceof ImageData) {
					images.add(ho);
				}
			}
		} else {
			images = browser.getVisibleImages();
		}
		if (images == null || images.size() == 0) {
			UserNotifier un = DataBrowserAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Dataset Creation", "No images selected");
			return;
		}
		//Check if we can use the image
		if (model.getParent() == null && model.getExperimenter() != null) {
			Iterator i = images.iterator();
			ImageData img;
			Collection list = new HashSet();
			while (i.hasNext()) {
				img = (ImageData) i.next();
				if (canLink(img)) list.add(img);
			}
			if (list.size() == 0) {
				UserNotifier un = 
					DataBrowserAgent.getRegistry().getUserNotifier();
				un.notifyInfo("Dataset Creation", "The images " +
						"cannot be added to the dataset. \n ");
				return;
			}
			images = list;
		}
		model.fireDataSaving(data, images);
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#setDataObjectCreated(DataObject, DataObject)
	 */
	public void setDataObjectCreated(DataObject object, DataObject parent)
	{
		Map<Object, Object> m = new HashMap<Object, Object>(1);
		m.put(object, parent);
		firePropertyChange(DATA_OBJECT_CREATED_PROPERTY, null, m);
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#setTableNodesSelected(List)
	 */
	public void setTableNodesSelected(List<ImageDisplay> nodes)
	{
		model.getBrowser().setNodesSelection(nodes);
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#setUnselectedDisplay(ImageDisplay)
	 */
	public void setUnselectedDisplay(ImageDisplay node)
	{
		if (node == null) return;
		Object object = node.getHierarchyObject();
		if (object instanceof WellSampleData) {
			setSelectedDisplays(
					(List<ImageDisplay>) model.getBrowser().getSelectedDisplays());
			return;
		}
		List<Object> objects = new ArrayList<Object>();
		objects.add(model.getBrowser().isMultiSelection());
		objects.add(object);
		
		if (object instanceof DataObject) {
			ImageDisplay p = node.getParentDisplay();
			Object parent = p.getHierarchyObject();
			if (!(parent instanceof DataObject))
				parent = model.getParent();
			if (parent != null) objects.add(parent);
		}
		firePropertyChange(UNSELECTED_NODE_DISPLAY_PROPERTY, null, objects);
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#getBrowser()
	 */
	public Browser getBrowser()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalArgumentException("This method cannot be " +
					"invoked in the DISCARDED state.");
		return model.getBrowser();
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#pasteRndSettings()
	 */
	public void pasteRndSettings()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalArgumentException("This method cannot be " +
					"invoked in the DISCARDED state.");
		if (model.getType() == DataBrowserModel.SEARCH) {
			firePropertyChange(PASTE_RND_SETTINGS_PROPERTY, null, 
					getBrowser().getSelectedDataObjects());
		} else {
			ImageDisplay d = getBrowser().getLastSelectedDisplay();
			if (d instanceof WellSampleNode) 
				firePropertyChange(PASTE_RND_SETTINGS_PROPERTY, null, 
						getBrowser().getSelectedDataObjects());
			else 
				firePropertyChange(PASTE_RND_SETTINGS_PROPERTY, 
					Boolean.FALSE, Boolean.TRUE);
		}
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#resetRndSettings()
	 */
	public void resetRndSettings()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalArgumentException("This method cannot be " +
					"invoked in the DISCARDED state.");
		if (model.getType() == DataBrowserModel.SEARCH) {
			firePropertyChange(RESET_RND_SETTINGS_PROPERTY, null, 
					getBrowser().getSelectedDataObjects());
		} else {
			ImageDisplay d = getBrowser().getLastSelectedDisplay();
			if (d instanceof WellSampleNode) 
				firePropertyChange(RESET_RND_SETTINGS_PROPERTY, null, 
						getBrowser().getSelectedDataObjects());
			else 
				firePropertyChange(RESET_RND_SETTINGS_PROPERTY, 
					Boolean.FALSE, Boolean.TRUE);
		}
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#copyRndSettings()
	 */
	public void copyRndSettings()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalArgumentException("This method cannot be " +
					"invoked in the DISCARDED state.");
		if (model.getType() == DataBrowserModel.SEARCH) {
			ImageDisplay display = getBrowser().getLastSelectedDisplay();
			Object o = display.getHierarchyObject();
			if (o instanceof WellSampleData) {
				WellSampleData wsd = (WellSampleData) o;
				o = wsd.getImage();
			}
			if (!(o instanceof ImageData)) return;
			ImageData img = (ImageData) o;
			firePropertyChange(COPY_RND_SETTINGS_PROPERTY, null, img);
		} else {
			ImageDisplay d = getBrowser().getLastSelectedDisplay();
			if (d instanceof WellSampleNode) {
				WellSampleData wsd = (WellSampleData) d.getHierarchyObject();
				firePropertyChange(COPY_RND_SETTINGS_PROPERTY, null, 
						wsd.getImage());
			} else
				firePropertyChange(COPY_RND_SETTINGS_PROPERTY, Boolean.FALSE, 
						null);
		}
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#copy()
	 */
	public void copy()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalArgumentException("This method cannot be " +
					"invoked in the DISCARDED state.");
		firePropertyChange(COPY_ITEMS_PROPERTY, Boolean.FALSE, Boolean.TRUE);
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#cut()
	 */
	public void cut()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalArgumentException("This method cannot be " +
					"invoked in the DISCARDED state.");
		firePropertyChange(CUT_ITEMS_PROPERTY, Boolean.FALSE, Boolean.TRUE);
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#paste()
	 */
	public void paste()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalArgumentException("This method cannot be " +
					"invoked in the DISCARDED state.");
		firePropertyChange(PASTE_ITEMS_PROPERTY, Boolean.FALSE, Boolean.TRUE);
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#remove()
	 */
	public void remove()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalArgumentException("This method cannot be " +
					"invoked in the DISCARDED state.");
		firePropertyChange(REMOVE_ITEMS_PROPERTY, Boolean.FALSE, Boolean.TRUE);
	}
	
	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#isWritable(Object)
	 */
	public boolean isWritable(Object ho)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		//Check if current user can write in object
		ExperimenterData exp = DataBrowserAgent.getUserDetails();
		long id = exp.getId();
		boolean b = EditorUtil.isUserOwner(ho, id);
		if (b) return b; //user it the owner.
		switch (exp.getPermissions().getPermissionsLevel()) {
			case GroupData.PERMISSIONS_GROUP_READ_LINK:
			case GroupData.PERMISSIONS_PUBLIC_READ_WRITE:
				return true;
		}
		return false;
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#canDelete(Object)
	 */
	public boolean canDelete(Object ho)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		long id = DataBrowserAgent.getUserDetails().getId();
		if (EditorUtil.isUserOwner(ho, id)) return true; //user it the owner.
		if (!(ho instanceof DataObject)) return false;
		DataObject data = (DataObject) ho;
		return data.canDelete();
	}
	
	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#canEdit(Object)
	 */
	public boolean canEdit(Object ho)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		//Check if current user can write in object
		long id = DataBrowserAgent.getUserDetails().getId();
		if (EditorUtil.isUserOwner(ho, id)) return true; //user it the owner.
		if (!(ho instanceof DataObject)) return false;
		DataObject data = (DataObject) ho;
		return data.canEdit();
	}
	
	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#canChgrp(Object)
	 */
	public boolean canChgrp(Object ho)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		//Check if current user can write in object
		if (DataBrowserAgent.isAdministrator()) return true;
		long id = DataBrowserAgent.getUserDetails().getId();
		return EditorUtil.isUserOwner(ho, id);
	}
	
	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#canLink(Object)
	 */
	public boolean canLink(Object ho)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		//Check if current user can write in object
		long id = DataBrowserAgent.getUserDetails().getId();
		return (EditorUtil.isUserOwner(ho, id)); //user it the owner.
		/*
		if (!(ho instanceof DataObject)) return false;
		DataObject data = (DataObject) ho;
		return data.canLink();
		*/
	}
	
	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#canAnnotate(Object)
	 */
	public boolean canAnnotate(Object ho)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		return model.canAnnotate(ho);
	}
	
	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#reloadThumbnails(Collection)
	 */
	public void reloadThumbnails(Collection ids)
	{
		switch (model.getState()) {
			case DISCARDED:
				throw new IllegalStateException("This method cannot be" +
						"invoked in the DISCARDED state.");
			case NEW:
				return;
		}
		model.loadData(true, ids);
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#setOriginalSettings()
	 */
	public void setOriginalSettings()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalArgumentException("This method cannot be " +
					"invoked in the DISCARDED state.");
		if (model.getType() == DataBrowserModel.SEARCH) {
			firePropertyChange(SET__ORIGINAL_RND_SETTINGS_PROPERTY, null, 
					getBrowser().getSelectedDataObjects());
		} else {
			ImageDisplay d = getBrowser().getLastSelectedDisplay();
			if (d instanceof WellSampleNode) 
				firePropertyChange(SET__ORIGINAL_RND_SETTINGS_PROPERTY, null, 
						getBrowser().getSelectedDataObjects());
			else 
				firePropertyChange(SET__ORIGINAL_RND_SETTINGS_PROPERTY, 
					Boolean.valueOf(false), Boolean.valueOf(true));
		}
	}
	
	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#setOwnerSettings()
	 */
	public void setOwnerSettings()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalArgumentException("This method cannot be " +
					"invoked in the DISCARDED state.");
		/*
		if (model.getType() == DataBrowserModel.SEARCH) {
			firePropertyChange(SET__OWNER_RND_SETTINGS_PROPERTY, null, 
					getBrowser().getSelectedDataObjects());
		} else {
			ImageDisplay d = getBrowser().getLastSelectedDisplay();
			if (d instanceof WellSampleNode) 
				firePropertyChange(SET__OWNER_RND_SETTINGS_PROPERTY, null, 
						getBrowser().getSelectedDataObjects());
			else 
				firePropertyChange(SET__OWNER_RND_SETTINGS_PROPERTY, 
						Boolean.valueOf(false), Boolean.valueOf(true));
		}
		*/
		ImageDisplay d = getBrowser().getLastSelectedDisplay();
		if (d instanceof WellSampleNode) 
			firePropertyChange(SET__OWNER_RND_SETTINGS_PROPERTY, null, 
					getBrowser().getSelectedDataObjects());
		else 
			firePropertyChange(SET__OWNER_RND_SETTINGS_PROPERTY,
				Boolean.valueOf(false), Boolean.valueOf(true));
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#filterByTagged(boolean)
	 */
	public void filterByTagged(boolean tagged)
	{
		if (model.getState() == FILTERING) {
			UserNotifier un = DataBrowserAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Filtering", FILTERING_MSG);
			return;
		}
		Browser browser = model.getBrowser();
		Set<DataObject> nodes = browser.getOriginal();
		if (nodes != null && nodes.size() > 0) {
			model.fireFilteringByAnnotated(TagAnnotationData.class, tagged, 
	                nodes);
			fireStateChange();
		}
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#filterByCommented(boolean)
	 */
	public void filterByCommented(boolean commented)
	{
		if (model.getState() == FILTERING) {
			UserNotifier un = DataBrowserAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Filtering", FILTERING_MSG);
			return;
		}
		Browser browser = model.getBrowser();
		Set<DataObject> nodes = browser.getOriginal();
		if (nodes != null && nodes.size() > 0) {
			model.fireFilteringByAnnotated(TextualAnnotationData.class, 
					commented, nodes);
			fireStateChange();
		}
	}
	
	/**
         * Implemented as specified by the {@link DataBrowser} interface.
         * @see DataBrowser#filterByROIs(boolean)
         */
        public void filterByROIs(boolean hasROIs)
        {
                if (model.getState() == FILTERING) {
                        UserNotifier un = DataBrowserAgent.getRegistry().getUserNotifier();
                        un.notifyInfo("Filtering", FILTERING_MSG);
                        return;
                }
                Browser browser = model.getBrowser();
                Set<DataObject> nodes = browser.getOriginal();
                if (!CollectionUtils.isEmpty(nodes)) {
                    FilterContext context = new FilterContext();
                    if(hasROIs) {
                        context.setRois(FilterContext.GREATER_EQUAL, 1);
                    }
                    else {
                        context.setRois(FilterContext.EQUAL, 0);
                    }
                    model.fireFilteringByContext(context, nodes);
                    fireStateChange();
                }
        }
        
	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#setComponentTitle(String)
	 */
	public void setComponentTitle(String title)
	{
		Browser browser = model.getBrowser();
		if (browser != null) browser.setComponentTitle(title);
	}
	
	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#viewField(int)
	 */
	public void viewField(int selectedIndex)
	{
		if (!(model instanceof WellsModel)) return;
		//quietly save the field back to the server.
		((WellsModel) model).viewField(selectedIndex);
		view.viewField();
		view.repaint();
		model.loadData(false, null);
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#setSelectedCell(CellDisplay)
	 */
	public void setSelectedCell(CellDisplay cell)
	{
		if (cell == null) return;
		if (!(model instanceof WellsModel)) return;
		//quietly save the description of the well and the color.
		((WellsModel) model).setSelectedCell(cell);
		view.repaint();
	}
	
	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#saveThumbnails(File)
	 */
	public void saveThumbnails(File file)
	{
		if (!isImagesModel() || file == null) return;
		Browser browser = model.getBrowser();
		List<ImageNode> l = browser.getVisibleImageNodes();
		
		UserNotifier un = DataBrowserAgent.getRegistry().getUserNotifier();
		if (l == null || l.size() == 0) {
			un.notifyInfo("Save Thumbnails", "No images to save");
			return;
		}
		List<ImageNode> nodes = model.sortCollection(l);
		Iterator<ImageNode> i = nodes.iterator();
		ImageNode node;
		try {
			ExcelWriter writer = new ExcelWriter(file.getAbsolutePath());
			writer.openFile();
			writer.createSheet("Thumbnails");
//			ready to build report
			BufferedImage thumbnail;
			int n = browser.getSelectedLayout().getImagesPerRow();
			int row = 0;
			int col = 0;
			int w = ThumbnailProvider.THUMB_MAX_WIDTH;
			int h = ThumbnailProvider.THUMB_MAX_HEIGHT;
			int count = 0;
			String imageName;
			while (i.hasNext()) {
				node = i.next();
				imageName = node.toString();
				thumbnail = node.getThumbnail().getFullScaleThumb();
				writer.addImageToWorkbook(imageName, thumbnail); 
				writer.writeImage(row, col, w, h, imageName);
				writer.writeElement(row+6, col, imageName);
				if (count < n) {
					col = col+4;
				} else {
					col = 0;
					count = -1;
					row = row+7;
				}
				count++;
			}
			
			writer.createSheet("Legend");
			i = nodes.iterator();
			row = 0;
			col = 0;
			writer.setCellStyle(row, col, row, col+1, ExcelWriter.BOLD_DEFAULT);
			writer.writeElement(row, col, "id");
			writer.writeElement(row, col+1, "name");
			row++;
			while (i.hasNext()) {
				node = i.next();
				imageName = node.toString();
				writer.writeElement(row, col, (
						(DataObject) node.getHierarchyObject()).getId());
				writer.writeElement(row, col+1, imageName);
				row++;
			}
			writer.close();
		} catch (Exception e) {
			Logger logger = DataBrowserAgent.getRegistry().getLogger();
			LogMessage msg = new LogMessage();
	        msg.print("Error while saving.");
	        msg.print(e);
	        logger.error(this, msg);
	        un.notifyInfo("Save Thumbnails", 
	        		"An error occurs while saving the file.");
	        return;
		}
		un.notifyInfo("Save Thumbnails", 
				"The thumbnails have been save to:\n"+file.getAbsolutePath());
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#isImagesModel()
	 */
	public boolean isImagesModel()
	{
		if (model.getState() == DISCARDED) return false;
		return model.isImagesModel();
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#setReportData(Collection, List, String)
	 */
	public void setReportData(Map<ImageNode, StructuredDataResults> data, 
			List<Class> types, String name)
	{
		if (data == null || data.size() == 0) return;
		UserNotifier un = DataBrowserAgent.getRegistry().getUserNotifier();
		//tags for now
		List sorted = model.sortCollection(data.keySet());
		Iterator<ImageNode> i = sorted.iterator();
		ImageNode node;
		Map<Long, List> tagImageMap = new HashMap<Long, List>();
		Map<Long, TagAnnotationData> 
			tagMap = new HashMap<Long, TagAnnotationData>();
		StructuredDataResults r;
		Collection l;
		Iterator k;
		TagAnnotationData tag;
		long id;
		List nodes;
		Map<Object, Integer> rowImage = new HashMap<Object, Integer>();
		try {
			ExcelWriter writer = new ExcelWriter(name);
			writer.openFile();
			writer.createSheet("Tags");
			
			//write tags
			String imageName;
			int col = 0;
			int row = 0;
			
			writer.setCellStyle(row, col, row, col+1, ExcelWriter.BOLD_DEFAULT);
			writer.writeElement(row, col, "id");
			writer.writeElement(row, col+1, "name");
			row++;
			while (i.hasNext()) {
				node = i.next();
				imageName = node.toString();
				rowImage.put(node.getHierarchyObject(), row);
				writer.writeElement(row, col, (
						(DataObject) node.getHierarchyObject()).getId());
				writer.writeElement(row, col+1, imageName);
				row++;
				r = data.get(node);
				l = r.getTags();
				if (l != null) {
					k = l.iterator();
					while (k.hasNext()) {
						tag = (TagAnnotationData) k.next();
						id = tag.getId();
						nodes = tagImageMap.get(id);
						if (!tagMap.containsKey(id))
							tagMap.put(id, tag);
						if (nodes == null) {
							nodes = new ArrayList();
							tagImageMap.put(id, nodes);
						}
						nodes.add(node.getHierarchyObject());
					}
				}
			}
			
			col = 2;
			row = 0;
			List sortedTags = model.sortCollection(tagMap.keySet());
			k = sortedTags.iterator();
			Object object;
			int value;
			int numberOfImages = data.size();
			int count;
			while (k.hasNext()) {
				row = 0;
				count = 0;
				id = (Long) k.next();
				tag = tagMap.get(id);
				writer.setCellStyle(row, col, ExcelWriter.BOLD_DEFAULT);
				writer.writeElement(row, col, tag.getTagValue());
				nodes = tagImageMap.get(id);
				i = data.keySet().iterator();
				while (i.hasNext()) {
					count++;
					node = i.next();
					object = node.getHierarchyObject();
					row = rowImage.get(object);
					value = 0;
					if (nodes.contains(object)) value = 1;
					writer.setCellStyle(row, col, ExcelWriter.INTEGER);
					writer.writeElement(row, col, value);
				}
				writer.setCellStyle(count+1, col, 
							ExcelWriter.CELLBORDER_TOPLINE);
				writer.setCellStyle(row, col, ExcelWriter.INTEGER);
				writer.writeElement(count+1, col, nodes.size());
				writer.setCellStyle(count+2, col, 
						ExcelWriter.TWODECIMALPOINTS);
				double v = (double) nodes.size()/numberOfImages;
				
				writer.writeElement(count+2, col, 
						UIUtilities.formatToDecimal(v*100)+"%");
				col++;
			}
			writer.sizeAllColumnsToFit();
			//second sheet
			writer.createSheet("Thumbnails");
			i = sorted.iterator();
			//ready to build report
			BufferedImage thumbnail;
			int n = model.getBrowser().getSelectedLayout().getImagesPerRow();
			row = 0;
			col = 0;
			int w = ThumbnailProvider.THUMB_MAX_WIDTH/2;
			int h = ThumbnailProvider.THUMB_MAX_HEIGHT/2;
			count = 0;
			while (i.hasNext()) {
				node = i.next();
				id = ((DataObject) node.getHierarchyObject()).getId();
				imageName = node.toString();
				thumbnail = node.getThumbnail().getFullScaleThumb();
				writer.addImageToWorkbook(imageName, thumbnail); 
				writer.writeImage(row, col, w, h, imageName);
				writer.writeElement(row+3, col, id);
				if (count < n) {
					col++;
				} else {
					col = 0;
					row = row+4;
				}
				count++;
			}
			writer.close();
			un.notifyInfo("Report", 
					"The report has been successfully created.");
			
		} catch (Exception e) {
			Logger logger = DataBrowserAgent.getRegistry().getLogger();
			LogMessage msg = new LogMessage();
	        msg.print("Error while writing report.");
	        msg.print(e);
	        logger.error(this, msg);
	        un.notifyInfo("Report", 
	        		"An error occurs while creating the report.");
		}
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#createReport(String)
	 */
	public void createReport(String name)
	{
		Browser browser = model.getBrowser();
		List<ImageNode> nodes = browser.getVisibleImageNodes();
		if (nodes == null || nodes.size() == 0) {
			UserNotifier un = DataBrowserAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Create Report", "No images displayed");
			return;
		}
		List<Class> types = new ArrayList<Class>();
		model.fireReportLoading(nodes, types, name);
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#hasRndSettings()
	 */
	public boolean hasRndSettings()
	{
		return DataBrowserFactory.hasRndSettingsToCopy();
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#areSettingsCompatible(long)
	 */
	public boolean areSettingsCompatible(long groupID)
	{
		return DataBrowserFactory.areSettingsCompatible(groupID);
	}
	
	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#hasDataToCopy()
	 */
	public Class hasDataToCopy()
	{
		return DataBrowserFactory.hasDataToCopy();
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#showTagWizard()
	 */
	public void showTagWizard()
	{
		firePropertyChange(TAG_WIZARD_PROPERTY, Boolean.FALSE, Boolean.TRUE);
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#loadExistingDatasets()
	 */
	public void loadExistingDatasets()
	{
		if (model.getExistingDatasets() == null) {
			model.fireExisitingDatasetsLoading();
		} else showExistingDatasets();
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#setExistingDatasets(Collection)
	 */
	public void setExistingDatasets(Collection result)
	{
		model.setExistingDatasets(result);
		showExistingDatasets();
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#addToDatasets(Collection)
	 */
	public void addToDatasets(Collection selected)
	{
		if (selected == null || selected.size() == 0) return;
		Browser browser = model.getBrowser();
		Collection images;
		Collection set = browser.getSelectedDisplays();
		if (set != null && set.size() > 0) {
			images = new HashSet();
			Iterator i = set.iterator();
			ImageDisplay display;
			Object ho;
			while (i.hasNext()) {
				display = (ImageDisplay) i.next();
				ho = display.getHierarchyObject();
				if (ho instanceof ImageData) {
					images.add(ho);
				}
			}
		} else {
			images = browser.getVisibleImages();
		}
		if (images == null || images.size() == 0) {
			UserNotifier un = DataBrowserAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Dataset Creation", "No images selected");
			return;
		}
		model.fireDataSaving(selected, images);
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#refresh()
	 */
	public void refresh()
	{
		firePropertyChange(ADDED_TO_DATA_OBJECT_PROPERTY, 
				Boolean.valueOf(false), Boolean.valueOf(true));
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#displayFieldsView()
	 */
	public void displayFieldsView()
	{
		if (!(model instanceof WellsModel)) return;
		int index = view.getSelectedView();
		
		if (index == DataBrowserUI.FIELDS_VIEW) {
			view.setSelectedView(DataBrowserUI.THUMB_VIEW);
			view.setFieldsStatus(false); 
			model.cancelFieldsLoading();
		} else if (index == DataBrowserUI.THUMB_VIEW) {
			view.setSelectedView(DataBrowserUI.FIELDS_VIEW);
			WellsModel wm = (WellsModel) model;
			WellImageSet node = wm.getSelectedWell();
			if (node != null) 
				viewFieldsFor(node.getRow(), node.getColumn(), false);
		}
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#viewFieldsFor(int, int, boolean)
	 */
	public void viewFieldsFor(int row, int column, boolean multiSelection)
	{
		if (!(model instanceof WellsModel)) return;
		//depends on the view.
		WellsModel wm = (WellsModel) model;
		
		int index = view.getSelectedView();
		
		if (index == DataBrowserUI.FIELDS_VIEW) {
			if (!model.loadFields(row, column)) {
				view.displayFields(wm.getSelectedWell().getWellSamples());
			} else {
				view.setFieldsStatus(true);
				fireStateChange();
			}
		} else if (index == DataBrowserUI.THUMB_VIEW) {
			WellImageSet well = wm.getWell(row, column);
			
			if (well != null && well.isSampleValid()) {
				model.getBrowser().setSelectedDisplay(
					well.getSelectedWellSample(), multiSelection, false);
				setSelectedDisplay(well.getSelectedWellSample());
			}
		}
	}
	
	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#setThumbnailsFieldsFor(List, int, int)
	 */
	public void setThumbnailsFieldsFor(List list, int row, int column)
	{
		if (!(model instanceof WellsModel)) return;
		WellsModel wm = (WellsModel) model;
		if (!wm.isSameWell(row, column)) return;
		WellImageSet well = wm.getSelectedWell();
		List<WellSampleNode> nodes = well.getWellSamples();
		Iterator<WellSampleNode> j = nodes.iterator();
		WellSampleNode n; 
		Map<Long, WellSampleNode> map = new HashMap<Long, WellSampleNode>();
		WellSampleData data;
		while (j.hasNext()) {
			n = j.next();
			data = (WellSampleData) n.getHierarchyObject();
			if (data.getId() > 0) {
				map.put(data.getImage().getId(), n);
			}
		}
		//Check the data.
		Iterator i = list.iterator();
		ThumbnailData td;
		Thumbnail thumb;
		while (i.hasNext()) {
			td = (ThumbnailData) i.next();
			n = map.get(td.getImageID());
			if (n != null) {
				thumb = n.getThumbnail();
				thumb.setFullScaleThumb(td.getThumbnail());
				thumb.setValid(true);
			}
		}
		view.displayFields(nodes);
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#openWith(ApplicationData)
	 */
	public void openWith(ApplicationData data)
	{
		firePropertyChange(OPEN_EXTERNAL_APPLICATION_PROPERTY, new Object(), 
				data);
	}
	
	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#setExperimenter(ExperimenterData)
	 */
	public void setExperimenter(ExperimenterData exp)
	{
		model.setExperimenter(exp);
		view.onExperimenterSet();
	}
	
	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#getGridUI()
	 */
	public PlateGridUI getGridUI() { return view.getGridUI(); }
	
	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#getParentOfNodes()
	 */
	public Object getParentOfNodes() { return model.getParent(); }
	
	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#setTabularData(List)
	 */
	public void setTabularData(List<TableResult> data)
	{
		if (data == null || data.size() == 0) return;
		model.setTabularData(data);
	}
	
	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#layoutDisplay()
	 */
	public void layoutDisplay()
	{
		if (model instanceof WellsModel) return;
		model.layoutBrowser(model.getLayoutIndex());
	}
	
	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#viewDisplay(ImageDisplay, boolean)
	 */
	public void viewDisplay(ImageDisplay node, boolean internal)
	{
		if (!(node instanceof ImageNode)) return;
		EventBus bus = DataBrowserAgent.getRegistry().getEventBus();
		DataObject data = null;
		Object uo = node.getHierarchyObject();
		ViewImage event;
		Object go;
		ViewImageObject object;
		if (uo instanceof ImageData) {
			if (model instanceof SearchModel || model instanceof AdvancedResultSearchModel) {
				ImageData img = (ImageData) uo;
				SecurityContext ctx = new SecurityContext(img.getGroupId());
				object = new ViewImageObject(img);
				go =  view.getParentOfNodes();
				if (go instanceof DataObject) 
					data = (DataObject) go;
				object.setContext(data, null);
				if (DataBrowserAgent.runAsPlugin() == LookupNames.IMAGE_J) {
					ViewInPluginEvent evt = new ViewInPluginEvent(ctx,
							img, LookupNames.IMAGE_J);
					bus.post(evt);
				} else {
					bus.post(new ViewImage(ctx, object, null));
				}
			} else {
				if (internal)
					firePropertyChange(INTERNAL_VIEW_NODE_PROPERTY, null, uo);
				else firePropertyChange(VIEW_IMAGE_NODE_PROPERTY, null, uo);
			}
		} else if (uo instanceof WellSampleData) {
			WellSampleData wellSample = (WellSampleData) uo;
			object = new ViewImageObject(wellSample);
			WellSampleNode wsn = (WellSampleNode) node;
			Object parent = wsn.getParentObject();
			
			if (parent instanceof DataObject) {
				go =  view.getGrandParentOfNodes();
				if (go instanceof DataObject)
					data = (DataObject) go;
				object.setContext((DataObject) parent, data);
			}
			if (DataBrowserAgent.runAsPlugin() == LookupNames.IMAGE_J) {
				
				ViewInPluginEvent evt = new ViewInPluginEvent(
						model.getSecurityContext(),
						wellSample.getImage(), LookupNames.IMAGE_J);
				bus.post(evt);
			} else {
				bus.post(new ViewImage(model.getSecurityContext(), object, 
						null));
			}
		}
	}
	
	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#getSecurityContext()
	 */
	public SecurityContext getSecurityContext()
	{
		return model.getSecurityContext();
	}
	
	/** 
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#getDisplayMode()
	 */
	public int getDisplayMode() { return model.getDisplayMode(); }

	/** 
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#setDisplayMode(int)
	 */
	public void setDisplayMode(int displayMode)
	{
		model.setDisplayMode(displayMode);
	}
	
    /** 
     * Implemented as specified by the {@link DataBrowser} interface.
     * @see DataBrowser#getType()
     */
    public int getType() { return model.getType(); }

    /** 
     * Implemented as specified by the {@link DataBrowser} interface.
     * @see DataBrowser#getType()
     */
    public void activateUser(ExperimenterData exp)
    {
        firePropertyChange(ACTIVATE_USER_PROPERTY, null, exp);
    }

    /** 
     * Implemented as specified by the {@link DataBrowser} interface.
     * @see DataBrowser#isSystemUser(long)
     */
    public boolean isSystemUser(long userID)
    {
        return model.isSystemUser(userID);
    }

    /** 
     * Implemented as specified by the {@link DataBrowser} interface.
     * @see DataBrowser#isSystemUser(long, String)
     */
    public boolean isSystemUser(long userID, String key)
    {
        return model.isSystemUser(userID, key);
    }

    /** 
     * Implemented as specified by the {@link DataBrowser} interface.
     * @see DataBrowser#isSystemGroup(long, String)
     */
    public boolean isSystemGroup(long groupID, String key)
    {
        return model.isSystemGroup(groupID, key);
    }

    /** 
     * Implemented as specified by the {@link DataBrowser} interface.
     * @see DataBrowser#resetPassword()
     */
    public void resetPassword()
    {
        firePropertyChange(RESET_PASSWORD_PROPERTY, Boolean.FALSE,
                Boolean.TRUE);
    }

    /** 
     * Implemented as specified by the {@link DataBrowser} interface.
     * @see DataBrowser#getCurrentUser()
     */
    public ExperimenterData getCurrentUser()
    {
        return model.getCurrentUser();
    }

    /** 
     * Overridden to return the name of the instance to save. 
     * @see #toString()
     */
    public String toString() { return ""+model.getType(); }

}
