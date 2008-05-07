/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowserUI 
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
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Browser;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.layout.Layout;
import org.openmicroscopy.shoola.agents.dataBrowser.visitor.MagnificationVisitor;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.search.SearchObject;
import pojos.DataObject;

/** 
 * The view.
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
class DataBrowserUI
	extends JPanel
{

	/** ID to select the thumbnail view. */
	static final int			THUMB_VIEW = 0;
	
	/** ID to select the columns view. */
	static final int			COLUMNS_VIEW = 1;
	
	/** ID to sort the node alphabetically. */
	static final int			SORT_BY_NAME = 2;
	
	/** ID to sort the node by date. */
	static final int			SORT_BY_DATE = 3;
	
	/** Reference to the tool bar. */
	private DataBrowserToolBar 		toolBar;
	
	/** Reference to the tool bar. */
	private DataBrowserStatusBar 	statusBar;
	
	/** Reference to the model. */
	private DataBrowserModel		model;
	
	/** Reference to the control. */
	private DataBrowserControl		controller;
	
	/** The slide show view. */
	private SlideShowView 			slideShowView;
	
	/** The selected view. */
	private int						selectedView;
	
	 /** The popup menu. */
	private PopupMenu				popupMenu;
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setLayout(new BorderLayout(0, 0));
		add(toolBar, BorderLayout.NORTH);
		add(model.getBrowser().getUI(), BorderLayout.CENTER);
		add(statusBar, BorderLayout.SOUTH);
	}
	
	/** Creates a new instance. */
	DataBrowserUI() {}
	
	/**
	 * Links the components composing the MVC triad.
	 * 
	 * @param model			Reference to the model. 
	 * 						Mustn't be <code>null</code>.
	 * @param controller	Reference to the control. 
	 * 						Mustn't be <code>null</code>.
	 */
	void initialize(DataBrowserModel model, DataBrowserControl controller)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		if (controller == null)
			throw new IllegalArgumentException("No control.");
		this.model = model;
		this.controller = controller;
		toolBar = new DataBrowserToolBar(this, controller);
		statusBar = new DataBrowserStatusBar(this);
		popupMenu = new PopupMenu(controller);
		selectedView = THUMB_VIEW;
		setNumberOfImages(-1);
		buildGUI();
	}
	
	/**
	 * Returns the selected object in order to filter the node.
	 * 
	 * @return See above.
	 */
	SearchObject getSelectedFilter() { return toolBar.getSelectedFilter(); }

	/**
	 * Returns the collection of existing tags.
	 * 
	 * @return See above.
	 */
	Collection getExistingTags() { return model.getExistingTags(); }
	
	/**
	 * Updates the UI elements when the tags are loaded.
	 * 
	 * @param tags The collection of tags to display.
	 */
	void setTags(Collection tags) { toolBar.setTags(tags); }
	
	/**
	 * Creates or deletes the slide show view.
	 * 
	 * @param create	Pass <code>true</code> to create a new dialog,
	 * 					<code>false</code> to delete it.
	 * @param images	The images to display.
	 */
	void slideShowView(boolean create, boolean images)
	{
		toolBar.enableSlideShow(!create);
		if (!create) {
			if (slideShowView != null) {
				model.cancelSlideShow();
				slideShowView.close();
			}
			//slideShowView = null; 
			return;
		}
		
		//if (slideShowView != null) return;
		Browser browser = model.getBrowser();
		
		List<ImageNode> nodes;
		Iterator i;
		if (images) nodes = browser.getVisibleImageNodes();
		else {
			Collection selection = browser.getSelectedDisplays();
			nodes = new ArrayList<ImageNode>();
			if (selection != null) {
				i = selection.iterator();
				Object n;
				while (i.hasNext()) {
					n = i.next();
					if (n instanceof ImageNode)
						nodes.add((ImageNode) n);
				}
			}
			
		}
		if (nodes == null || nodes.size() == 0) {
			toolBar.enableSlideShow(true);
			return;
		}
		List<ImageNode> selection = new ArrayList<ImageNode>(nodes.size());
		ImageNode n;
		i = nodes.iterator();
		while (i.hasNext()) {
			n = (ImageNode) i.next();
			selection.add(n.copy());
		}
		Registry reg = DataBrowserAgent.getRegistry();
		slideShowView = new SlideShowView(
										reg.getTaskBar().getFrame(), 
										selection);
		slideShowView.addPropertyChangeListener(controller);
		model.fireFullSizeLoading(selection);
		UIUtilities.centerAndShow(slideShowView);
		if (model.getState() != DataBrowser.LOADING_SLIDE_VIEW)
			setSlideViewStatus(true, -1);
	}
	
	/**
     * Adjusts the status bar according to the specified arguments.
     * 
     * @param hideProgressBar Whether or not to hide the progress bar.
     * @param progressPerc  The percentage value the progress bar should
     *                      display.  If negative, it is iterpreted as
     *                      not available and the progress bar will be
     *                      set to indeterminate mode.  This argument is
     *                      only taken into consideration if the progress
     *                      bar shouldn't be hidden.
     */
    void setSlideViewStatus(boolean hideProgressBar, int progressPerc)
    {
    	if (slideShowView != null) 
    		slideShowView.setProgress(hideProgressBar, progressPerc);
    }
    
    /**
     * Returns <code>true</code> if the roll flag is on, <code>false</code>
     * otherwise.
     * 
     * @return See above.
     */
    boolean isRollOver() { return model.isRollOver(); }
    
    /** Lays out the selected component. */
    void layoutUI()
    {
    	switch (selectedView) {
			case THUMB_VIEW:
				Browser b = model.getBrowser();
				model.layoutBrowser();
				b.getUI().repaint();
				model.loadData(false);
				b.getUI().repaint();
				break;
			case COLUMNS_VIEW:
				ImageTableView v = model.getTableView();
				if (v != null) v.refreshTable();
				break;
    	}
    }
    
    /**
     * Sets the selected view.
     * 
     * @param index The value to set.
     */
    void setSelectedView(int index) 
    {
    	removeAll();
    	switch (index) {
			case THUMB_VIEW:
				selectedView = index;
				layoutUI();
				add(toolBar, BorderLayout.NORTH);
				add(model.getBrowser().getUI(), BorderLayout.CENTER);
				break;
			case COLUMNS_VIEW:
				selectedView = index;
				add(toolBar, BorderLayout.NORTH);
				
				ImageTableView existed = model.getTableView();
				ImageTableView v = model.createImageTableView();
				if (existed == null) {
					Collection nodes = model.getBrowser().getSelectedDisplays();
					if (nodes != null) {
						Iterator i = nodes.iterator();
						List<DataObject> objects = new ArrayList<DataObject>();
						ImageDisplay display;
						Object ho;
						while (i.hasNext()) {
							display = (ImageDisplay) i.next();
							ho = display.getHierarchyObject();
							if (ho instanceof DataObject)
								objects.add((DataObject) ho);
						}
						v.setSelectedNodes(objects);
					}
				}
				v.addPropertyChangeListener(controller);
				add(v, BorderLayout.CENTER);
				break;
		}
    	add(statusBar, BorderLayout.SOUTH);
    	toolBar.setSelectedViewIndex(selectedView);
    	statusBar.setSelectedViewIndex(selectedView);
    	revalidate();
    	repaint();
    }
    
    /**
     * Returns the selected view index.
     * 
     * @return See above.
     */
    int getSelectedView() { return selectedView; }

	/**
	 * Sets to <code>true</code> to zoom the image when the user
	 *  mouses over an {@link ImageNode}, to <code>false</code> otherwise.
	 * 
	 * @param rollOver  Pass <code>true</code> to zoom the image when the user
	 *                  mouses over an {@link ImageNode}, 
	 *                  <code>false</code> otherwise.
	 */
	void setRollOver(boolean rollOver)
	{
		model.getBrowser().setRollOver(rollOver);
	}

	/**
	 * Sets the number of images displayed in a row.
	 * 
	 * @param number The number of images per row.
	 */
	void setItemsPerRow(int number)
	{
		Browser browser = model.getBrowser();
		Layout layout = browser.getSelectedLayout();
		layout.setImagesPerRow(number);
		//if (selectedView == THUMB_VIEW)
		browser.accept(layout, ImageDisplayVisitor.IMAGE_SET_ONLY);
	}
    
	/**
	 * Sets the number of images.
	 * 
	 * @param value The number of images displayed.
	 */
	void setNumberOfImages(int value)
	{
		if (value < 0) value = model.getNumberOfImages();
		toolBar.setNumberOfImages(value, model.getNumberOfImages());
	}
	
	/** 
	 * Sorts the thumbnails either alphabetically or by date.
	 * 
	 * @param index Th e sorting index.
	 */
	void sortBy(int index)
	{
		model.getSorter().setByDate(SORT_BY_DATE == index);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Browser browser = model.getBrowser();
        browser.accept(browser.getSelectedLayout(), 
        					ImageDisplayVisitor.IMAGE_SET_ONLY);
        
        ImageTableView v = model.getTableView();
		if (v != null) v.refreshTable();
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	/**
	 * Magnifies the images nodes.
	 * 
	 * @param factor The magnification factor.
	 */
	void setMagnificationFactor(double factor)
	{
		MagnificationVisitor visitor = new MagnificationVisitor(factor);
		Browser browser = model.getBrowser();
		browser.accept(visitor, ImageDisplayVisitor.IMAGE_NODE_ONLY);
		browser.accept(browser.getSelectedLayout(), 
						ImageDisplayVisitor.IMAGE_SET_ONLY);
	}
	
    /**
     * Brings up the popup menu on top of the specified component at the
     * specified point.
     * 
     * @param p The point at which to display the menu, relative to the 
     *          <code>component</code>'s coordinates.         
     */
    void showPopup(Point p)
    { 
    	ImageDisplay c = model.getBrowser().getLastSelectedDisplay();
    	if (c != null) popupMenu.show(c, p.x, p.y); 
    }
    
}
