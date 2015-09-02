/*
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Browser;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Thumbnail;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.WellSampleNode;
import org.openmicroscopy.shoola.agents.dataBrowser.layout.Layout;
import org.openmicroscopy.shoola.agents.dataBrowser.visitor.MagnificationVisitor;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImageObject;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.ApplicationData;
import org.openmicroscopy.shoola.env.data.util.FilterContext;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.util.ui.ScrollablePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.search.SearchObject;
import omero.gateway.model.DataObject;
import omero.gateway.model.ImageData;

/** 
 * The {@link DataBrowser}'s View. Embeds the <code>Browser</code>'s UI.
 * Also provides a menu bar and a status bar.
 * 
 * @see Browser
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
class DataBrowserUI
	extends ScrollablePanel//JPanel
{

	/** ID to select the thumbnail view. */
	static final int			THUMB_VIEW = 0;
	
	/** ID to select the columns view. */
	static final int			COLUMNS_VIEW = 1;
	
	/** ID to select the fields view. */
	static final int			FIELDS_VIEW = 2;
	
	/** ID to select the search view. */
	static final int                        SEARCH_VIEW = 3;
	
	/** ID to sort the node alphabetically. */
	static final int			SORT_BY_NAME = 2;
	
	/** ID to sort the node by date. */
	static final int			SORT_BY_DATE = 3;
	
	/** Reference to the tool bar. */
	private DataBrowserToolBar 		toolBar;
	
	/** Reference to the tool bar for Plates. */
	private DataBrowserWellToolBar 	wellToolBar;
	
	/** Reference to the tool bar. */
	private DataBrowserStatusBar 	statusBar;
	
	/** Reference to the tool bar. */
	private PlateGridUI 			plateGridUI;
	
	/** Reference to the model. */
	private DataBrowserModel		model;
	
	/** Reference to the control. */
	private DataBrowserControl		controller;
	
	/** The slide show view. */
	private SlideShowView 			slideShowView;
	
	/** The selected view. */
	private int						selectedView;
	
	 /** The pop-up menu. */
	private PopupMenu				popupMenu;
	
	/** Component displaying the fields. */
	private WellFieldsView			fieldsView;
	
	/** The magnification factor. */
	private double					factor;
	
	/** Creates a new instance. */
	DataBrowserUI()
	{
		super(true);
	}
	
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
		//if (model.getType() == DataBrowserModel.WELLS)
		wellToolBar = new DataBrowserWellToolBar(this, controller);
		toolBar = new DataBrowserToolBar(model, this, controller);
		if (model.getType() == DataBrowserModel.WELLS)
			plateGridUI = new PlateGridUI((WellsModel) model, controller);
		statusBar = new DataBrowserStatusBar(this);
		if (model.getType() == DataBrowserModel.SEARCH) {
		    selectedView = SEARCH_VIEW;
		}
		else {
		    selectedView = THUMB_VIEW;
		}
		factor = Thumbnail.SCALING_FACTOR;
		setNumberOfImages(-1);
		setLayout(new BorderLayout(0, 0));
		buildGUI(true);
	}
	
	/** 
	 * Builds and lays out the UI. 
	 * 
	 * @param full  Pass <code>true</code> to add all the components,
	 * 				<code>false</code> otherwise.
	 */
	void buildGUI(boolean full)
	{
		removeAll();
		if (full) {
			if (model.getType() == DataBrowserModel.WELLS) {
				add(wellToolBar, BorderLayout.NORTH);
			} else {
				add(toolBar, BorderLayout.NORTH);
			}
			add(statusBar, BorderLayout.SOUTH);
			statusBar.setVisible(model.getType() != DataBrowserModel.SEARCH);
		}
		add(model.getBrowser().getUI(), BorderLayout.CENTER);
	}
	
	/**
	 * Returns the grid representing the plate.
	 * 
	 * @return See above.
	 */
	PlateGridUI getGridUI() { return plateGridUI; }
	
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
	 */
	void slideShowView(boolean create)
	{
		toolBar.enableSlideShow(!create);
		if (!create) {
			if (slideShowView != null) {
				model.cancelSlideShow();
				slideShowView.close();
			}
			return;
		}
		
		//if (slideShowView != null) return;
		Browser browser = model.getBrowser();
		List<ImageNode> nodes;
		Iterator i;
		Collection selected = browser.getSelectedDisplays();
		if (selected != null && selected.size() > 0) {
			nodes = new ArrayList<ImageNode>();
			i = selected.iterator();
			Object n;
			while (i.hasNext()) {
				n = i.next();
				if (n instanceof ImageNode)
					nodes.add((ImageNode) n);
			}
		} else {
			nodes = browser.getVisibleImageNodes();
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
		slideShowView = new SlideShowView(reg.getTaskBar().getFrame(), 
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
     *                      display. If negative, it is interpreted as
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
				break;
			case COLUMNS_VIEW:
				ImageTableView v = model.getTableView();
				if (v != null) 
				    v.refreshTable();
				break;
			case SEARCH_VIEW:
                            SearchResultView sv = model.getSearchView();
                            if (sv != null) 
                                sv.refreshTable();
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
    	double f = Thumbnail.SCALING_FACTOR;
    	switch (index) {
			case THUMB_VIEW:
				selectedView = index;
				if (model.getType() == DataBrowserModel.WELLS) {
					add(wellToolBar, BorderLayout.NORTH);
					wellToolBar.displayFieldsOptions(false);
				} else {
					add(toolBar, BorderLayout.NORTH);
					layoutUI();
				}
				add(model.getBrowser().getUI(), BorderLayout.CENTER);
				f = factor;
				break;
			case FIELDS_VIEW:
				selectedView = index;
				add(wellToolBar, BorderLayout.NORTH);
				if (fieldsView == null) {
					f = Thumbnail.MAX_SCALING_FACTOR;
					fieldsView  = new WellFieldsView((WellsModel) model, 
							controller, f);//statusBar.getMagnificationFactor());
				}
				wellToolBar.displayFieldsOptions(true);
				add(fieldsView, BorderLayout.CENTER);
				f = fieldsView.getMagnification();
				break;
			case COLUMNS_VIEW:
				selectedView = index;
				add(toolBar, BorderLayout.NORTH);
				
				ImageTableView existed = model.getTableView();
				ImageTableView v = model.createImageTableView();
				if (existed != null && v != null) v.refreshTable();
				//if (existed == null) {
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
				//}
				if (existed == null) v.addPropertyChangeListener(controller);
				v.validate();
				v.repaint();
				add(v, BorderLayout.CENTER);
				break;
			case SEARCH_VIEW:
                            selectedView = index;
                            SearchResultView sv = model.createSearchResultView();
                            sv.addPropertyChangeListener(controller);
                            add(sv, BorderLayout.CENTER);
                            sv.refreshTable();
                            break;
		}
    	add(statusBar, BorderLayout.SOUTH);
    	toolBar.setSelectedViewIndex(selectedView);
    	statusBar.setSelectedViewIndex(selectedView, f);
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
		if (layout != null) {
			layout.setImagesPerRow(number);
			browser.accept(layout, ImageDisplayVisitor.IMAGE_SET_ONLY);
		}
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
	 * @param index The sorting index.
	 */
	void sortBy(int index)
	{
		model.getSorter().setByDate(SORT_BY_DATE == index);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Browser browser = model.getBrowser();
        Layout layout = browser.getSelectedLayout();
        if (layout != null)
        	browser.accept(layout, ImageDisplayVisitor.IMAGE_SET_ONLY);
        
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
		MagnificationVisitor visitor;
		Browser browser;
		switch (selectedView) {
			case THUMB_VIEW:
				visitor = new MagnificationVisitor(factor);
				browser = model.getBrowser();
				browser.accept(visitor, ImageDisplayVisitor.IMAGE_NODE_ONLY);
				browser.accept(browser.getSelectedLayout(), 
								ImageDisplayVisitor.IMAGE_SET_ONLY);
				break;
			case COLUMNS_VIEW:
				visitor = new MagnificationVisitor(factor);
				browser = model.getBrowser();
				browser.accept(visitor, ImageDisplayVisitor.IMAGE_NODE_ONLY);
				ImageTableView v = model.getTableView();
				if (v != null) {
					v.setMagnification(factor);
					v.refreshTable();
				}
				break;
			case FIELDS_VIEW:
				if (fieldsView != null)
					fieldsView.setMagnificationFactor(factor);
				break;
		}
		
		//if (model.getType() == DataBrowserModel.WELLS);
		//	browser.getSelectedLayout().doLayout();
	}
	
	/**
	 * Sets the magnification the <code>Fields View </code> is selected.
	 * 
	 * @param factor The value to set.
	 */
	void setMagnificationUnscaled(double factor)
	{
		if (selectedView == FIELDS_VIEW && fieldsView != null) 
			fieldsView.setMagnificationUnscaled(factor);
	}
	
    /**
     * Brings up the pop-up menu on top of the specified component at the
     * specified point.
     * 
     * @param p The point at which to display the menu, relative to the 
     *          <code>component</code>'s coordinates.         
     */
    void showPopup(Point p)
    { 
    	if (popupMenu == null) popupMenu = new PopupMenu(controller, model);
    	Component comp = null;
    	switch (selectedView) {
			case THUMB_VIEW:
				if (model.getBrowser() != null)
					comp = model.getBrowser().getUI();
				break;
			case COLUMNS_VIEW:
				comp = model.getTableView();
			case SEARCH_VIEW:
                            comp = model.getSearchView();
		}
    	if (comp != null) {
    		popupMenu.populateOpenWith();
    		popupMenu.show(comp, p.x, p.y);
    	}
    }
    
	/**
	 * Returns the collections of applications.
	 * 
	 * @return See above.
	 */
    List<ApplicationData> getApplications() { return model.getApplications(); }
    
    /** Views the selected node only if it is an image. */
    void viewSelectedNode()
    {
    	ImageDisplay node = model.getBrowser().getLastSelectedDisplay();
    	if (!(node instanceof ImageNode)) return;
    	ImageData data = (ImageData) node.getHierarchyObject();
    	EventBus bus = DataBrowserAgent.getRegistry().getEventBus();
    	ViewImage evt = new ViewImage(model.getSecurityContext(),
    			new ViewImageObject(data), null);
    	evt.setPlugin(DataBrowserAgent.runAsPlugin());
    	bus.post(evt);
    }
    
    /**
     * Adjusts the status bar according to the specified arguments.
     * 
     * @param status Textual description to display.
     * @param hideProgressBar Whether or not to hide the progress bar.
     * @param progressPerc  The percentage value the progress bar should
     *                      display.  If negative, it is interpreted as
     *                      not available and the progress bar will be
     *                      set to indeterminate mode.  This argument is
     *                      only taken into consideration if the progress
     *                      bar shouldn't be hidden.
     */
    void setStatus(String status, boolean hideProgressBar, int progressPerc)
    {
        statusBar.setStatus(status);
        statusBar.setProgress(hideProgressBar, progressPerc);
    }

    /**
     * Sets the filtering context.
     * 
     * @param context The context to handle.
     */
	void filterByContext(FilterContext context)
	{
		if (context == null) return;
		toolBar.filterByContext(context);
	}
    
	/**
	 * Sets the filtering status.
	 * 
	 * @param busy  Pass <code>true</code> if filtering, <code>false</code>
	 * 				otherwise.
	 */
	void setFilterStatus(boolean busy)
	{
		toolBar.setFilterStatus(busy);
	}
	
	/**
	 * Sets the text of the filtered label.
	 * 
	 * @param value The value to set.
	 */
	void setFilterLabel(String value) { toolBar.setFilterLabel(value); }
	
	/**
	 * Returns the number of fields per well.
	 * 
	 * @return See above.
	 */
	int getFieldsNumber()
	{
		if (model instanceof WellsModel)
			return ((WellsModel) model).getFieldsNumber();
		return -1;
	}
	
	/**
	 * Returns the selected field, the default value is <code>0</code>.
	 * 
	 * @return See above.
	 */
	int getSelectedField()
	{
		if (model instanceof WellsModel)
			return ((WellsModel) model).getSelectedField();
		return 0;
	}

	/** Updates the view when a new field is selected. */
	void viewField()
	{
		setMagnificationFactor(statusBar.getMagnificationFactor());
	}

	/**
	 * Indicates the status of the fields loading.
	 * 
	 * @param status Pass <code>true</code> while loading the fields,
	 * 				 <code>false</code> otherwise.
	 */
	void setFieldsStatus(boolean status) { wellToolBar.setStatus(status); }

	/**
	 * Displays the passed fields.
	 * 
	 * @param nodes The nodes hosting the fields.
	 */
	void displayFields(List<WellSampleNode> nodes)
	{
		if (fieldsView == null) return;
		setFieldsStatus(false);
		if (selectedView == FIELDS_VIEW)
			fieldsView.displayFields(nodes);
	}
	
	/** Invokes when a well is selected. */
	void onSelectedWell()
	{
		if (!(model instanceof WellsModel)) return;
		plateGridUI.onSelectedWell();
	}
	
	/**
	 * Sets the layout used to display the fields.
	 * 
	 * @param index The index of the layout.
	 */
	void setSelectedFieldLayout(int index)
	{
		if (fieldsView == null) return;
		fieldsView.setLayoutFields(index);
		if (selectedView == FIELDS_VIEW)
			fieldsView.displayFields(fieldsView.getNodes());
	}
	
	/** Invokes when the parent has been set. */
	void onExperimenterSet() { toolBar.onExperimenterSet(); }
	
    /**
     * Returns the parent of the nodes if any.
     * 
     * @return See above.
     */
    Object getParentOfNodes() { return model.getParent(); }
    
    /**
     * Returns the parent of the nodes if any.
     * 
     * @return See above.
     */
    Object getGrandParentOfNodes() { return model.getGrandParent(); }
	
}
