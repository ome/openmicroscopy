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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Browser;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageFinder;
import org.openmicroscopy.shoola.agents.dataBrowser.visitor.NodesFinder;
import org.openmicroscopy.shoola.agents.dataBrowser.visitor.RegexFinder;
import org.openmicroscopy.shoola.agents.hiviewer.HiViewerAgent;
import org.openmicroscopy.shoola.env.data.util.FilterContext;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.RegExFactory;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import pojos.DataObject;

/** 
 * 
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
		view.initialize(model, controller);
		controller.initialize(this, view);
	}
	
	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#activate()
	 */
	public void activate()
	{
		//Determine the view depending on the 
		if (model.getNumberOfImages() < MAX_ENTRIES)
			model.loadData(false); 
		else view.setSelectedView(DataBrowserUI.COLUMNS_VIEW);
		if (model.getBrowser() != null)
			model.getBrowser().addPropertyChangeListener(controller);
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

	public int getHierarchyType() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#getState()
	 */
	public int getState() { return model.getState(); }

	public void setStatus(String description, int perc) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#setThumbnail(long, BufferedImage)
	 */
	public void setThumbnail(long imageID, BufferedImage thumb)
	{
		model.setThumbnail(imageID, thumb);
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#setSelectedDisplay(ImageDisplay)
	 */
	public void setSelectedDisplay(ImageDisplay node)
	{
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
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		String text = "";
		Iterator<String> i = terms.iterator();
		while (i.hasNext()) 
			text += i.next().trim();
		
		Browser browser = model.getBrowser();
		Pattern pattern;
		try {
			pattern = RegExFactory.createPattern(text);
		} catch (PatternSyntaxException pse) {
            UserNotifier un = DataBrowserAgent.getRegistry().getUserNotifier();
            un.notifyInfo("Find", "The phrase cannot contain +, ? or *");
            return;
        }
		
		RegexFinder finder = new RegexFinder(pattern);
		//browser.visitOriginal(finder);
		browser.accept(finder);
		List<ImageDisplay> nodes = finder.getFoundNodes();
		browser.setFilterNodes(nodes);
		model.layoutBrowser();
		browser.getUI().repaint();
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
		//
		//model.layoutBrowser();
		//browser.getUI().repaint();
		model.setState(READY);
		view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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
	public void createDataObject(DataObject data)
	{
		if (data == null) return;
		//TODO: check state.
		Browser browser = model.getBrowser();
		Collection images = browser.getVisibleImages();
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
	
}
