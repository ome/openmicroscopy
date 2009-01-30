/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowserComponent 
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
import java.awt.Cursor; 
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.agents.dataBrowser.ThumbnailProvider;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Browser;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageFinder;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.visitor.NodesFinder;
import org.openmicroscopy.shoola.agents.dataBrowser.visitor.RegexFinder;
import org.openmicroscopy.shoola.agents.dataBrowser.visitor.ResetNodesVisitor;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.data.util.FilterContext;
import org.openmicroscopy.shoola.env.data.util.StructuredDataResults;
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.env.log.Logger;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.file.ExcelWriter;
import org.openmicroscopy.shoola.util.ui.RegExFactory;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import pojos.DataObject;
import pojos.ImageData;
import pojos.TagAnnotationData;
import pojos.TextualAnnotationData;

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
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class DataBrowserComponent 
	extends AbstractComponent
	implements DataBrowser
{

	/** The Model sub-component. */
	private DataBrowserModel     model;

	/** The Controller sub-component. */
	private DataBrowserControl   controller;

	/** The View sub-component. */
	private DataBrowserUI       view;
	
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
		//Determine the view depending on the 
		Integer max = (Integer) DataBrowserAgent.getRegistry().lookup(
				          "/views/MAX_ENTRIES");
		if (model.getNumberOfImages() < max.intValue())
			model.loadData(false, null); 
		else view.setSelectedView(DataBrowserUI.COLUMNS_VIEW);
		if (model.getBrowser() != null) {
			Browser browser = model.getBrowser();
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
		model.discard();
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
	 * @see DataBrowser#setThumbnail(long, BufferedImage, int)
	 */
	public void setThumbnail(long imageID, BufferedImage thumb, int maxEntries)
	{
		int previousState = model.getState();
		model.setThumbnail(imageID, thumb, maxEntries);
		if (previousState != model.getState()) fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#setSelectedDisplay(ImageDisplay)
	 */
	public void setSelectedDisplay(ImageDisplay node)
	{
		if (node == null) return;
		Object object = node.getHierarchyObject();
		List<Object> objects = new ArrayList<Object>();
		//objects.add(model.getBrowser().isMultiSelection());
		
		List<Object> others = new ArrayList<Object>(); 
		
		Collection selected = model.getBrowser().getSelectedDisplays();
		Iterator i = selected.iterator();
		ImageDisplay n;
		while (i.hasNext()) {
			n = (ImageDisplay) i.next();
			if (n != node) others.add(n.getHierarchyObject());
		}
		
		objects.add(others);
		//Root node
		if (node.equals(model.getBrowser().getUI())) {
			objects.add(model.parent);
		} else objects.add(object);
		if (object instanceof DataObject) {
			ImageDisplay p = node.getParentDisplay();
			Object parent = p.getHierarchyObject();
			if (!(parent instanceof DataObject))
				parent = model.getParent();
			objects.add(parent);
		}
		firePropertyChange(SELECTED_NODE_DISPLAY_PROPERTY, null, objects);
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#setSelectedNodes(List)
	 */
	public void setSelectedNodes(List<DataObject> objects)
	{
		ImageTableView tbView = model.getTableView();
		if (tbView != null) tbView.setSelectedNodes(objects);
		model.getBrowser().setSelectedNodes(objects);
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#getUI()
	 */
	public JComponent getUI()
	{
		// TODO Auto-generated method stub
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
			un.notifyInfo("Filtering", "Currenlty filering data. Please wait.");
			return;
		}
		Browser browser = model.getBrowser();
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		ImageFinder finder = new ImageFinder();
		browser.accept(finder, ImageDisplayVisitor.IMAGE_NODE_ONLY);
		model.fireFilteringByRate(rate, finder.getImages());
		fireStateChange();
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
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#filterByComments(List)
	 */
	public void filterByComments(List<String> comments)
	{
		if (model.getState() == FILTERING) {
			UserNotifier un = DataBrowserAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Filtering", "Currenlty filering data. Please wait.");
			return;
		}
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		Browser browser = model.getBrowser();
		model.fireFilteringByComments(comments, browser.getOriginal());
		fireStateChange();
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
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		RegexFinder finder = new RegexFinder(pattern);
		browser.accept(finder);
		List<ImageDisplay> nodes = finder.getFoundNodes();
		browser.setFilterNodes(nodes);
		view.layoutUI();
		view.setNumberOfImages(nodes.size());
		view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#filterByTags(List)
	 */
	public void filterByTags(List<String> tags)
	{
		if (model.getState() == FILTERING) {
			UserNotifier un = DataBrowserAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Filtering", "Currenlty filering data. Please wait.");
			return;
		}
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		Browser browser = model.getBrowser();
		model.fireFilteringByTags(tags, browser.getOriginal());
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#setFilteredNodes(List)
	 */
	public void setFilteredNodes(List<DataObject> objects)
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
		browser.setFilterNodes(nodes);
		view.layoutUI();
		view.setNumberOfImages(nodes.size());
		model.setState(READY);
		view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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
			un.notifyInfo("Filtering", "Currenlty filering data. Please wait.");
			return;
		}
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		view.filterByContext(context);
		Browser browser = model.getBrowser();
		model.fireFilteringByContext(context, browser.getOriginal());
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#loadExistingTags()
	 */
	public void loadExistingTags()
	{
		model.fireTagsLoading();
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#setExistingTags(Collection)
	 */
	public void setExistingTags(Collection tags)
	{
		model.setTags(tags);
		view.setTags(tags);
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
	public void createDataObject(DataObject data, boolean visible)
	{
		if (data == null) return;
		//TODO: check state.
		Browser browser = model.getBrowser();
		Collection images;
		if (visible) images = browser.getVisibleImages();
		else {
			images = new HashSet();
			Collection set = browser.getSelectedDisplays();
			if (set != null) {
				Iterator i = set.iterator();
				ImageDisplay display;
				Object ho;
				while (i.hasNext()) {
					display = (ImageDisplay) i.next();
					ho = display.getHierarchyObject();
					if (ho instanceof DataObject) {
						images.add(ho);
					}
				}
			}
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
		List<Object> objects = new ArrayList<Object>();
		objects.add(model.getBrowser().isMultiSelection());
		objects.add(object);
		
		if (object instanceof DataObject) {
			ImageDisplay p = node.getParentDisplay();
			Object parent = p.getHierarchyObject();
			if (!(parent instanceof DataObject))
				parent = model.getParent();
			objects.add(parent);
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
			if (!(o instanceof ImageData)) return;
			ImageData img = (ImageData) o;
			firePropertyChange(COPY_RND_SETTINGS_PROPERTY, null, img);
		} else {
			firePropertyChange(COPY_RND_SETTINGS_PROPERTY, Boolean.FALSE, null);
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
	 * @see DataBrowser#isObjectWritable(Object)
	 */
	public boolean isObjectWritable(Object ho)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		//Check if current user can write in object
		long id = DataBrowserAgent.getUserDetails().getId();
		long groupId = -1;
		return EditorUtil.isWritable(ho, id, groupId);
	}
	
	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#isReadable(DataObject)
	 */
	public boolean isReadable(DataObject ho)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		//Check if current user can write in object
		long id = DataBrowserAgent.getUserDetails().getId();
		long groupId = -1;
		return EditorUtil.isReadable(ho, id, groupId);
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
			firePropertyChange(SET__ORIGINAL_RND_SETTINGS_PROPERTY, 
					Boolean.FALSE, Boolean.TRUE);
		}
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#filterByTagged(boolean)
	 */
	public void filterByTagged(boolean tagged)
	{
		if (model.getState() == FILTERING) {
			UserNotifier un = DataBrowserAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Filtering", "Currenlty filering data. Please wait.");
			return;
		}
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		Browser browser = model.getBrowser();
		model.fireFilteringByAnnotated(TagAnnotationData.class, tagged, 
				                browser.getOriginal());
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#filterByCommented(boolean)
	 */
	public void filterByCommented(boolean commented)
	{
		if (model.getState() == FILTERING) {
			UserNotifier un = DataBrowserAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Filtering", "Currenlty filering data. Please wait.");
			return;
		}
		Browser browser = model.getBrowser();
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		model.fireFilteringByAnnotated(TextualAnnotationData.class, commented, 
                browser.getOriginal());
		fireStateChange();
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
		((WellsModel) model).viewField(selectedIndex);
		view.repaint();
		model.loadData(false, null); 
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#saveThumbnails(String)
	 */
	public void saveThumbnails(String name)
	{
		if (!isImagesModel()) return;
		Browser browser = model.getBrowser();
		List<ImageNode> nodes = browser.getVisibleImageNodes();
		UserNotifier un = DataBrowserAgent.getRegistry().getUserNotifier();
		if (nodes == null || nodes.size() == 0) {
			un.notifyInfo("Create Report", "No images to save");
			return;
		}
		Iterator<ImageNode> i = nodes.iterator();
		ImageNode node;
		try {
			ExcelWriter writer = new ExcelWriter(name);
			writer.openFile();
			writer.createSheet("Thumbnails");
//			ready to build report
			BufferedImage thumbnail;
			int n = model.getBrowser().getSelectedLayout().getImagesPerRow();
			int row = 0;
			int col = 0;
			int w = ThumbnailProvider.THUMB_MAX_WIDTH/2;
			int h = ThumbnailProvider.THUMB_MAX_HEIGHT/2;
			int count = 0;
			long id;
			String imageName;
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
	        un.notifyInfo("Report", "An error occurs while saving the file.");
		}
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
	 * @see DataBrowser#hasDataToCopy()
	 */
	public Class hasDataToCopy()
	{
		return DataBrowserFactory.hasDataToCopy();
	}
	
}
