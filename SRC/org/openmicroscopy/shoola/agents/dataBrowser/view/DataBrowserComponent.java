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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
import org.openmicroscopy.shoola.agents.events.metadata.ViewMetadata;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.env.data.util.FilterContext;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.RegExFactory;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import pojos.DataObject;
import pojos.ImageData;

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
		model.loadData(false);
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
		if (object instanceof DataObject) 
			firePropertyChange(SELECTED_NODE_DISPLAY_PROPERTY, null, object);
	}

	/**
	 * Implemented as specified by the {@link DataBrowser} interface.
	 * @see DataBrowser#setSelectedNodes(List)
	 */
	public void setSelectedNodes(List<DataObject> objects)
	{
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
		model.layoutBrowser();
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
		RegexFinder finder = new RegexFinder(RegExFactory.createPattern(text));
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
			//browser.visitOriginal(finder);
			browser.accept(finder);
			nodes = finder.getFoundNodes();
		} else {
			nodes = new ArrayList<ImageDisplay>();
		}
		browser.setFilterNodes(nodes);
		model.layoutBrowser();
		browser.getUI().repaint();
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
	 * @see DataBrowser#annotate(int)
	 */
	public void annotate(int index)
	{
		Browser browser = model.getBrowser();
		Collection nodes = null;
		switch (index) {
			case ANNOTATE_CHILDREN:
				
				//TODO
			case ANNOTATE_IMAGES:
				nodes = browser.getVisibleImages();
			case ANNOTATE_SELECTION:
				Set display = browser.getSelectedDisplays();
				if (display != null) {
					Iterator i = display.iterator();
					ImageDisplay node;
					nodes = new HashSet();
					Object ho;
					while (i.hasNext()) {
						node = (ImageDisplay) i.next();
						ho = node.getHierarchyObject();
						if (ho instanceof ImageData) {
							nodes.add(ho);
						}
					}
				}
				break;
	
			default:
				throw new IllegalArgumentException("Annotation index " +
												"not supported.");
		}
		EventBus bus = MetadataViewerAgent.getRegistry().getEventBus();
		System.err.println(nodes);
		//if (nodes != null)
		//	bus.post(new ViewMetadata(nodes));
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
	
}
