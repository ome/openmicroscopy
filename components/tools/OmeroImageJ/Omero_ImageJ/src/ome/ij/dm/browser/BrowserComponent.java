/*
 * ome.ij.dm.browser.BrowserComponent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package ome.ij.dm.browser;


//Java imports
import ij.IJ;

import java.awt.Cursor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import ome.ij.data.DSAccessException;
import ome.ij.data.DSOutOfServiceException;
import ome.ij.data.DataService;
import ome.ij.data.ServicesFactory;
import ome.ij.dm.TreeViewerTranslator;

import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;

/** 
 * Implements the {@link Browser} interface to provide the functionality
 * required of the tree viewer component.
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
 * @since 3.0-Beta4
 */
class  BrowserComponent 
	extends AbstractComponent
	implements Browser
{

	/** The Model sub-component. */
    private BrowserModel    	model;
    
    /** The View sub-component. */
    private BrowserUI       	view;
    
    /** The Controller sub-component. */
    private BrowserControl  	controller;
    
    /**
     * Creates a new instance.
     * The {@link #initialize() initialize} method should be called straight 
     * after to complete the MVC set up.
     * 
     * @param model The Model sub-component.
     */
    BrowserComponent(BrowserModel model)
    {
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        controller = new BrowserControl(this);
        view = new BrowserUI();
    }
    
    /** 
     * Links up the MVC triad. 
     * 
     * @param exp The logged in experimenter.
     */
    void initialize(ExperimenterData exp)
    {
        model.initialize(this);
        controller.initialize(view);
        view.initialize(controller, model, exp);
    }
    
    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#accept(TreeImageDisplayVisitor)
     */
    public void accept(TreeImageDisplayVisitor visitor)
    {
        accept(visitor, TreeImageDisplayVisitor.ALL_NODES);
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#accept(TreeImageDisplayVisitor, int)
     */
    public void accept(TreeImageDisplayVisitor visitor, int algoType)
    {
        view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        view.getTreeRoot().accept(visitor, algoType);
        view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#viewImage(TreeImageDisplay)
     */
	public void viewImage(TreeImageDisplay node)
	{
		if (node == null) return;
		Object uo = node.getUserObject();
		if (uo instanceof ImageData)
			firePropertyChange(VIEW_DISPLAY_PROPERTY, null, uo);
	}
	
    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#activate()
     */
    public void activate()
    {
        int state = model.getState();
        switch (state) {
            case NEW:
            	view.loadExperimenterData();
                break;
            case DISCARDED:
                throw new IllegalStateException(
                        "This method can't be invoked in the DISCARDED state.");
            default:
                break;
        }
    }
    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#cancel()
     */
    public void cancel()
    {
    	int state = model.getState();
        if ((state == LOADING_DATA) || (state == LOADING_LEAVES)) {
            model.cancel();
            view.cancel(model.getLastSelectedDisplay()); 
            fireStateChange();
        }
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#countExperimenterImages(TreeImageDisplay)
     */
    public void countExperimenterImages(TreeImageDisplay expNode)
    {
    	// TODO Auto-generated method stub

    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#countExperimenterImages(TreeImageDisplay)
     */
    public void discard()
    {
    	 if (model.getState() != DISCARDED) {
             model.discard();
             fireStateChange();
         }
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#displaysImagesName()
     */
    public void displaysImagesName()
    {
    	if (model.getState() == DISCARDED)
			 throw new IllegalStateException("This method cannot be invoked "+
	                "in the DISCARDED state.");
    	NameVisitor visitor = new NameVisitor(view.isPartialName());
    	accept(visitor, TreeImageDisplayVisitor.TREEIMAGE_NODE_ONLY);
		view.repaint();
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getState()
     */
    public int getState() { return model.getState(); }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getUI()
     */
    public JComponent getUI()
    { 
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED state.");
        return view;
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#loadExperimenterData(TreeImageDisplay, TreeImageDisplay)
     */
    public void loadExperimenterData(TreeImageDisplay exp, TreeImageDisplay n)
    {
    	if (exp == null || !(exp.getUserObject() instanceof ExperimenterData))
			throw new IllegalArgumentException("Node not valid.");
		switch (model.getState()) {
			case DISCARDED:
			case LOADING_LEAVES:
				throw new IllegalStateException(
	                    "This method cannot be invoked in the DISCARDED or " +
	                    "LOADING_LEAVES state.");
		}   
		IJ.showStatus("Loading data....");
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		DataService ds = ServicesFactory.getInstance().getDataService();
		try {
			if (n == null) {
				model.fireExperimenterDataLoading((TreeImageSet) exp);
				setExperimenterData(exp, ds.loadProjects());
			} else {
				Object ho = n.getUserObject();
				if (ho instanceof DatasetData) {
					DatasetData d = (DatasetData) ho;
					model.fireLeavesLoading(exp, n);
					long id = d.getId();
					Collection r = ds.loadImages(id);
					Iterator i = r.iterator();
					DataObject object;
					Class klass = d.getClass();
					while (i.hasNext()) {
						object = (DataObject) i.next();
						if (object.getClass().equals(klass)
								&& object.getId() == id) {
							if (object instanceof DatasetData) {
								setLeaves(((DatasetData) object).getImages(), 
										n, exp);
							} 
						}
					}
				}
				
			}
		} catch (DSAccessException e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			IJ.showMessage("An error occurred while loading data.", sw.toString());
			view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			firePropertyChange(ERROR_EXIT_PROPERTY, Boolean.valueOf(false), 
					Boolean.valueOf(true));
			model.setState(READY);
		} catch (DSOutOfServiceException ex) {
			IJ.showMessage("Out of Service exception.\n" +
					"The plugin will exit.");
			firePropertyChange(ERROR_EXIT_PROPERTY, Boolean.valueOf(false), 
					Boolean.valueOf(true));
			model.setState(READY);
		}
		IJ.showStatus("");

        fireStateChange();
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setSelectedDisplay(TreeImageDisplay)
     */
    public void setSelectedDisplay(TreeImageDisplay display)
    {
    	switch (model.getState()) {
    	//case LOADING_DATA:
    	//case LOADING_LEAVES:
    	case DISCARDED:
    		throw new IllegalStateException(
    				"This method cannot be invoked in the "+
    		"DISCARDED state.");
    	}
    	//if (hasDataToSave(display)) return;
    	TreeImageDisplay oldDisplay = model.getLastSelectedDisplay();
    	//if (oldDisplay != null && oldDisplay.equals(display)) return; 
    	TreeImageDisplay exp = null;
    	if (display != null) {
    		Object ho = display.getUserObject();
    		if (ho instanceof ExperimenterData) {
    			exp = display;
    			display = null;
    		}
    	}
    	if (exp != null) model.setSelectedDisplay(exp, true);
    	else model.setSelectedDisplay(display, true);
    	if (display == null) view.setNullSelectedNode();
    	firePropertyChange(SELECTED_TREE_NODE_DISPLAY_PROPERTY, oldDisplay, 
    			display);
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#sortTreeNodes(int)
     */
    public void sortTreeNodes(int index)
    {
    	switch (index) {
			case SORT_NODES_BY_DATE:
			case SORT_NODES_BY_NAME:
				break;
			default:
				throw new IllegalArgumentException("Index not supported.");
		}
    	model.setSortingIndex(index);
    	view.sortNodes();
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#refresh()
     */
	public void refresh()
	{
		loadExperimenterData(view.getLoggedExperimenterNode(), null);
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#getSelectedObject()
	 */
	public DataObject getSelectedObject()
	{
		TreeImageDisplay node = model.getLastSelectedDisplay();
		if (node == null) return null;
		Object object = node.getUserObject();
		if (object instanceof DataObject)
			return (DataObject) object;
		return null;
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setExperimenterData(TreeImageDisplay, Collection)
	 */
	public void setExperimenterData(TreeImageDisplay expNode, Collection nodes)
	{
		int state = model.getState();
        if (state != LOADING_DATA)
            throw new IllegalStateException(
                    "This method can only be invoked in the LOADING_DATA "+
                    "state.");
        if (nodes == null) throw new NullPointerException("No nodes.");
      
        if (expNode == null)
        	throw new IllegalArgumentException("Experimenter node not valid.");
        IJ.showStatus("");

		view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        Object uo = expNode.getUserObject();
        if (!(uo instanceof ExperimenterData))
        	throw new IllegalArgumentException("Experimenter node not valid.");
        //depending on the type of browser, present data 
        Set convertedNodes = TreeViewerTranslator.transformHierarchy(nodes);
        view.setExperimenterData(convertedNodes, expNode);
        model.setState(READY);
        //countItems(null);

        fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setLeaves(Set, TreeImageDisplay, TreeImageDisplay)
	 */
	public void setLeaves(Set leaves, TreeImageDisplay parent, 
			TreeImageDisplay expNode) 
	{
		 if (model.getState() != LOADING_LEAVES) return;
		 if (leaves == null) throw new NullPointerException("No leaves.");
		 Object ho = expNode.getUserObject();
		 if (!(ho instanceof ExperimenterData))
			 throw new IllegalArgumentException("Experimenter not valid");
		 IJ.showStatus("");
		 view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		 Set visLeaves = TreeViewerTranslator.transformHierarchy(leaves);
		 view.setLeavesViews(visLeaves, (TreeImageSet) parent);

		 model.setState(READY);
		 fireStateChange();
	}

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#collapse(TreeImageDisplay)
     */
    public void collapse(TreeImageDisplay node)
    {
        if (node == null) return;
        view.collapsePath(node);
    }
    
}
