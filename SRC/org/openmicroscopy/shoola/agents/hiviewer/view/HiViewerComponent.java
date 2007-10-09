/*
 * org.openmicroscopy.shoola.agents.hiviewer.view.HiViewerComponent
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.hiviewer.view;


//Java imports
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.HiTranslator;
import org.openmicroscopy.shoola.agents.hiviewer.HiViewerAgent;
import org.openmicroscopy.shoola.agents.hiviewer.actions.SortByAction;
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoard;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.AnnotateCmd;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.DataSaveVisitor;
import org.openmicroscopy.shoola.agents.hiviewer.layout.Layout;
import org.openmicroscopy.shoola.agents.hiviewer.layout.LayoutFactory;
import org.openmicroscopy.shoola.agents.hiviewer.saver.ContainerSaver;
import org.openmicroscopy.shoola.agents.hiviewer.treeview.TreeView;
import org.openmicroscopy.shoola.agents.util.DataHandler;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.ImageData;

/** 
 * Implements the {@link HiViewer} interface to provide the functionality
 * required of the hierarchy viewer component.
 * This class is the component hub and embeds the component's MVC triad.
 * It manages the component's state machine and fires state change 
 * notifications as appropriate, but delegates actual functionality to the
 * MVC sub-components.
 *
 * @see org.openmicroscopy.shoola.agents.hiviewer.view.HiViewerModel
 * @see org.openmicroscopy.shoola.agents.hiviewer.view.HiViewerWin
 * @see org.openmicroscopy.shoola.agents.hiviewer.view.HiViewerControl
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class HiViewerComponent
    extends AbstractComponent
    implements HiViewer
{

    /** The Model sub-component. */
    private HiViewerModel   model;
    
    /** The View sub-component. */
    private HiViewerWin     view;
    
    /** The Controller sub-component. */
    private HiViewerControl controller;
    
    /**
     * Creates a new instance.
     * The {@link #initialize() initialize} method should be called straigh 
     * after to complete the MVC set up.
     * 
     * @param model The Model sub-component.
     */
    HiViewerComponent(HiViewerModel model)
    {
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        controller = new HiViewerControl(this);
        view = new HiViewerWin();
    }
    
    /** Links up the MVC triad. */
    void initialize()
    {
        model.initialize(this);
        controller.initialize(view);
        view.initialize(controller, model);
    }
    
    /**
     * Returns <code>true</code> if edited data to save,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean hasEditedDataToSave()
    {
    	return model.getClipBoard().hasEditedDataToSave();
    }
     
    /**
     * Returns <code>true</code> if annotated data to save,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean hasAnnotatedDataToSave()
    {
    	return model.getClipBoard().hasAnnotationToSave();
    }
    
    /**
     * Returns the title associated to this viewer.
     * 
     * @return See above.
     */
    String getTitle() { return view.getTitle(); }
    
    /**
     * Returns the Model sub-component.
     * 
     * @return See above.
     */
    HiViewerModel getModel() { return model; }
    
    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#getState()
     */
    public int getState() { return model.getState(); }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#getHierarchyType()
     */
    public int getHierarchyType() { return model.getHierarchyType(); }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#activate(Rectangle)
     */
    public void activate(Rectangle bounds)
    {
        switch (model.getState()) {
            case NEW:
                model.fireHierarchyLoading(false);
                view.setComponentBounds(bounds);
                fireStateChange();
                break;
            case DISCARDED:
            	break;
            //    throw new IllegalStateException(
            //            "This method can't be invoked in the DISCARDED state.");
            default:
                view.deIconify();
        }
    }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#setHierarchyRoots(Set, boolean, boolean)
     */
    public void setHierarchyRoots(Set roots, boolean flat, boolean refresh)
    {
        if (model.getState() != LOADING_HIERARCHY)
            throw new IllegalStateException(
                    "This method can only be invoked in the LOADING_HIERARCHY "+
                    "state.");
        if (!refresh) {
        	model.createBrowser(roots, flat);
            model.createClipBoard();
            model.getClipBoard().addPropertyChangeListener(controller);
            model.fireThumbnailLoading();
            //b/c fireThumbnailLoading() sets the state to READY if there is no
            //image.
            if (model.getBrowser().getImages().size() == 0) 
            	setStatus("Done", -1);
            else setStatus(HiViewer.PAINTING_TEXT, -1);
            fireStateChange();
        } else {
        	Browser browser = model.getBrowser();
        	if (browser == null)
        		throw new NullPointerException("The browser cannot be NULL.");
        	boolean isClipBoardDisplay = false;
        	ClipBoard clipBoard = model.getClipBoard();
        	if (clipBoard != null) 
        		isClipBoardDisplay = clipBoard.isDisplay();
        	
        	view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        	model.refreshBrowser(roots, flat);
        	model.createClipBoard();
            model.getClipBoard().addPropertyChangeListener(controller);
        	model.fireThumbnailLoading();
            //b/c fireThumbnailLoading() sets the state to READY if there is no
            //image.
            if (model.getBrowser().getImages().size() == 0) 
            	setStatus("Done", -1);
            else setStatus(HiViewer.PAINTING_TEXT, -1);
            fireStateChange();
            model.getBrowser().setSelectedDisplay(null);
            TreeView tv = model.getTreeView();
        	if (tv != null) {
        		model.createTreeView();
        		view.showTreeView(tv.isDisplay());
        	}
        	view.showClipBoard(isClipBoardDisplay);
        	view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#setThumbnail(long, BufferedImage)
     */
    public void setThumbnail(long imageID, BufferedImage thumb)
    {
        int state = model.getState();
        switch (state) {
            case LOADING_THUMBNAILS:
                model.setThumbnail(imageID, thumb);
                //setThumbnail will set state to READY when done.
                if (model.getState() == READY) fireStateChange();
                break;
            case READY:
                model.setThumbnail(imageID, thumb);
                break;
            default:
                throw new IllegalStateException(
                   "This method can only be invoked in the LOADING_THUMBNAILS "+
                        "or READY state.");
        }
    }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#setStatus(String, int)
     */
    public void setStatus(String description, int perc)
    {
        int state = model.getState();
        if (state == LOADING_HIERARCHY || state == LOADING_THUMBNAILS)
            view.setStatus(description, false, perc);
        else view.setStatus(description, true, perc);
    }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#getBrowser()
     */
    public Browser getBrowser()
    {
        switch (model.getState()) {
            case NEW:
            case DISCARDED:
                throw new IllegalStateException(
                   "This method cannot be invoked in the NEW "+
                        "or DISCARDED state.");
            default:
            	return model.getBrowser();
        }
    }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#discard()
     */
    public void discard()
    { 
        if (model.getState() != DISCARDED) {
            model.discard();
            fireStateChange();
        }
    }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#getUI()
     */
    public JFrame getUI()
    { 
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED state.");
        return view;
    }
     
    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#getViewTitle()
     */
    public String getViewTitle()
    {
        switch (model.getState()) {
	        case NEW:
	        case DISCARDED:
	            throw new IllegalStateException(
	               "This method cannot be invoked in the NEW "+
	                    "or DISCARDED state.");
	        default:
	        	return view.getViewTitle();
        }       
    }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#getClipBoard()
     */
    public ClipBoard getClipBoard()
    {
        switch ( model.getState()) {
	        case NEW:
	        case DISCARDED:
	        	throw new IllegalStateException(
	        			"This method cannot be invoked in the NEW "+
	        			"or DISCARDED state.");
	        default:
	        	return model.getClipBoard();
        }
    }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#getUserDetails()
     */
    public ExperimenterData getUserDetails()
    {
        return model.getUserDetails();
    }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#showTreeView(boolean)
     */
    public void showTreeView(boolean b)
    {
        int state = model.getState();
        if (state == NEW && state == DISCARDED)
            throw new IllegalStateException(
            		"This method cannot be invoked in the NEW "+
        			"or DISCARDED state.");
        if (getBrowser() == null) return;
        TreeView treeView = model.getTreeView();
        if (treeView == null) {
            model.createTreeView();
            treeView = model.getTreeView();
            treeView.addPropertyChangeListener(controller);
        }
        if (treeView.isDisplay() == b) return;
        treeView.setDisplay(b);
        view.showTreeView(b);
    }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#showClipBoard(boolean)
     */
    public void showClipBoard(boolean b)
    {
        int state = model.getState();
        if (state == NEW && state == DISCARDED)
            throw new IllegalStateException(
            		"This method cannot be invoked in the NEW "+
        			"or DISCARDED state.");
        ClipBoard cb = model.getClipBoard();
        if (cb == null) return;
        if (cb.isDisplay() == b) return;
        cb.setDisplay(b);
        view.showClipBoard(b);
    }
    
    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#getTreeView()
     */
    public TreeView getTreeView()
    {
        int state = model.getState();
        
        if (state == NEW && state == DISCARDED)
            throw new IllegalStateException(
            		"This method cannot be invoked in the NEW "+
        			"or DISCARDED state.");
        return model.getTreeView();
    }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#createTreeView()
     */
	public TreeView createTreeView()
	{
		int state = model.getState();
		if (state == NEW && state == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the NEW "+
			"or DISCARDED state.");
		TreeView treeView = model.getTreeView();
		if (treeView == null) {
			model.createTreeView();
			treeView = model.getTreeView();
			treeView.addPropertyChangeListener(controller);
		}
		return treeView;
	}

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#getExperimenterID()
     */
    public long getExperimenterID()
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED state.");
        return model.getRootID();
    }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#moveToBack()
     */
    public void moveToBack()
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED state.");
        view.toBack();
    }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#getHierarchyObject()
     */
    public Object getHierarchyObject()
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED state.");
        ImageDisplay node = model.getBrowser().getLastSelectedDisplay();
        if (node == null) return null;
        return node.getHierarchyObject();
    }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#scrollToNode(ImageDisplay)
     */
    public void scrollToNode(ImageDisplay node)
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED state.");
        if (node == null) throw new IllegalArgumentException("No node.");
        controller.scrollToNode(node);
    }
    
    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#isObjectReadable(DataObject)
     */
    public boolean isObjectReadable(DataObject ho)
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
            "This method cannot be invoked in the DISCARDED state.");
        return HiTranslator.isReadable(ho, getUserDetails().getId(), 
                                            getExperimenterID());
    }
    
    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#isObjectWritable(DataObject)
     */
    public boolean isObjectWritable(DataObject ho)
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
            "This method cannot be invoked in the DISCARDED state.");
        return HiTranslator.isWritable(ho, getUserDetails().getId(), 
                                        getExperimenterID());
    }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#saveObject(DataObject)
     */
    public void saveObject(DataObject object)
    {
        if (object == null)
            throw new IllegalArgumentException("No object to update");
        //TODO check state.
        model.fireDataObjectUpdate(object);
    }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#showProperties(DataObject)
     */
    public void showProperties(DataObject object)
    {
        if (object == null)
            throw new IllegalArgumentException("No object to update");
        model.getClipBoard().showProperties(object);
    }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#setRollOver(boolean)
     */
    public void setRollOver(boolean b)
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
            "This method cannot be invoked in the DISCARDED state.");
        model.setRollOver(b);
    }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#isRollOver()
     */
    public boolean isRollOver()
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
            "This method cannot be invoked in the DISCARDED state.");
        return model.isRollOver();
    }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#removeObjects(List)
     */
    public void removeObjects(List toRemove)
    {
        //TODO: Check state
        if (toRemove == null || toRemove.size() == 0) {
            UserNotifier un = 
                HiViewerAgent.getRegistry().getUserNotifier();
            un.notifyInfo("Node remoal", "No nodes to remove.");
            return;
        }
        model.fireDataObjectsRemoval(toRemove);
        fireStateChange();
    }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#setLayout(int)
     */
    public void setLayout(int layoutIndex)
    {
        view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Browser browser = getBrowser();
        switch (layoutIndex) {
            case LayoutFactory.SQUARY_LAYOUT:
                browser.setSelectedLayout(layoutIndex);
                browser.resetChildDisplay();
                browser.accept(LayoutFactory.createLayout(
                        LayoutFactory.SQUARY_LAYOUT, model.getSorter()),
                        ImageDisplayVisitor.IMAGE_SET_ONLY);
                break;
            case LayoutFactory.FLAT_LAYOUT:
                browser.setSelectedLayout(layoutIndex);
                browser.resetChildDisplay();
                Layout l = LayoutFactory.createLayout(layoutIndex, 
                                                        model.getSorter());
                browser.accept(l);
                browser.setSelectedLayout(LayoutFactory.FLAT_LAYOUT);
                l.doLayout();
        }
        view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#sortBy(int)
     */
    public void sortBy(int index)
    {
        model.getSorter().setByDate(SortByAction.BY_DATE == index);
        view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Browser browser = getBrowser();
        switch (browser.getSelectedLayout()) {
            case LayoutFactory.SQUARY_LAYOUT:
                browser.accept(LayoutFactory.createLayout(
                        LayoutFactory.SQUARY_LAYOUT, model.getSorter()),
                        ImageDisplayVisitor.IMAGE_SET_ONLY);
                break;
            case LayoutFactory.FLAT_LAYOUT:
                browser.resetChildDisplay();
                Layout l = LayoutFactory.createLayout(LayoutFactory.FLAT_LAYOUT, 
                                                        model.getSorter());
                browser.accept(l);
                l.doLayout();
        }
        TreeView tv = model.getTreeView();
        if (tv != null) 
            tv.sortNodes(index, (ImageDisplay) model.getBrowser().getUI());
        view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    
    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#layoutZoomedNodes()
     */
    public void layoutZoomedNodes()
    {
        view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Browser browser = getBrowser();
        if (browser == null) return;
        ImageDisplay d = browser.getLastSelectedDisplay();
        switch (browser.getSelectedLayout()) {
            case LayoutFactory.SQUARY_LAYOUT:
                d.accept(LayoutFactory.createLayout(
                        LayoutFactory.SQUARY_LAYOUT, model.getSorter()),
                        ImageDisplayVisitor.IMAGE_SET_ONLY);
                break;
            case LayoutFactory.FLAT_LAYOUT:
            	browser.resetChildDisplay();
                Layout l = LayoutFactory.createLayout(LayoutFactory.FLAT_LAYOUT, 
                                                        model.getSorter());
                browser.accept(l);
                l.doLayout();
        }
        view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#setFoundResults(Set)
     */
	public void setFoundResults(Set foundNodes)
	{
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		List l = model.getSorter().sort(foundNodes);
		model.getClipBoard().setFoundResults(l);
		view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#annotateDataObjects(Set)
     */
	public void annotateDataObjects(Set nodes)
	{
		if (model.getState() == DISCARDED)
            throw new IllegalStateException("This method cannot be invoked " +
                    "in the DISCARDED state.");
		if (nodes == null)
			throw new IllegalArgumentException("No dataObject to annotate");
		if (nodes.size() == 1) {
			AnnotateCmd cmd = new AnnotateCmd(this, null);
	        cmd.execute();
	        return;
		}
		DataHandler dh = model.annotateDataObjects(view, nodes);
		dh.addPropertyChangeListener(controller);
		dh.activate();
	}

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#classifyImages(ImageData[], int)
     */
	public void classifyImages(ImageData[] images, int mode)
	{
		if (model.getState() == DISCARDED)
            throw new IllegalStateException("This method cannot be invoked " +
                    "in the DISCARDED state.");
		if (images == null || images.length == 0)
			throw new IllegalArgumentException("No image to classify.");
		DataHandler dh = model.classifyImageObjects(view, images, mode);
		dh.addPropertyChangeListener(controller);
		dh.activate();
	}

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#onDataObjectSave(List)
     */
	public void onDataObjectSave(List nodes)
	{
		switch (model.getState()) {
			case DISCARDED:
			case NEW:
			case LOADING_THUMBNAILS:
				return;
			default:
				break;
		}
		if (nodes == null) {
			model.onDataObjectSave();
			fireStateChange();
			return;
		}
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		DataSaveVisitor visitor = new DataSaveVisitor(this, nodes);
		Browser browser = model.getBrowser();
		browser.accept(visitor);
		JComponent c = browser.getUI();
		c.validate();
		c.repaint();
		browser.setSelectedDisplay(browser.getLastSelectedDisplay());
		TreeView tv = model.getTreeView();
		if (tv != null) tv.repaint();
        ThumbWinManager.updateDisplayNodes(nodes);
		model.onDataObjectSave();
		view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		fireStateChange();
	}

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#refresh()
     */
	public void refresh()
	{
		switch (model.getState()) {
			case DISCARDED:
				throw new IllegalStateException("This method cannot be" +
						"invoked in the DISCARDED state.");
			case NEW:
				return;
		}
		model.fireHierarchyLoading(true);
		fireStateChange();
	}

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#saveThumbnails(Set)
     */
	public void saveThumbnails(Set thumbnails)
	{
		if (thumbnails == null || thumbnails.size() == 0)
			throw new IllegalArgumentException("No images to save.");
		List l = model.getSorter().sort(thumbnails);
		Iterator i = l.iterator();
		List<BufferedImage> thumbs = new ArrayList<BufferedImage>(l.size());
		while (i.hasNext()) {
			thumbs.add(
					((ImageNode) i.next()).getThumbnail().getDisplayedImage());
		}
		ContainerSaver saver = new ContainerSaver(view, thumbs);
        saver.pack();
        UIUtilities.centerAndShow(saver);
	}

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#getExperimenter()
     */
	public ExperimenterData getExperimenter()
	{
		if (model.getState() == DISCARDED)
            throw new IllegalStateException("This method cannot be invoked " +
                    "in the DISCARDED state.");
		return model.getExperimenter();
	}

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#annotateChildren(ImageDisplay)
     */
	public void annotateChildren(ImageDisplay node)
	{
		if (model.getState() == DISCARDED)
            throw new IllegalStateException("This method cannot be invoked " +
                    "in the DISCARDED state.");
		if (node == null)
			throw new IllegalArgumentException("No specified container.");
		DataHandler dh = model.annotateChildren(view, node);
		dh.addPropertyChangeListener(controller);
		dh.activate();
	}

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#classifyChildren(ImageDisplay)
     */
	public void classifyChildren(ImageDisplay node)
	{
		if (model.getState() == DISCARDED)
            throw new IllegalStateException("This method cannot be invoked " +
                    "in the DISCARDED state.");
		if (node == null)
			throw new IllegalArgumentException("No specified container.");
		DataHandler dh = model.classifyChildren(view, node);
		dh.addPropertyChangeListener(controller);
		dh.activate();
		
	}

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#resetLayout()
     */
	public void resetLayout()
	{
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Browser browser = getBrowser();
        switch (model.getBrowser().getSelectedLayout()) {
            case LayoutFactory.SQUARY_LAYOUT:
                browser.accept(LayoutFactory.createLayout(
                        LayoutFactory.SQUARY_LAYOUT, model.getSorter()),
                        ImageDisplayVisitor.IMAGE_SET_ONLY);
                break;
            case LayoutFactory.FLAT_LAYOUT:
                browser.resetChildDisplay();
                Layout l = LayoutFactory.createLayout(LayoutFactory.FLAT_LAYOUT, 
                                                        model.getSorter());
                browser.accept(l);
                l.doLayout();
        }
        view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#isMouseOver()
     */
	public boolean isMouseOver() { return model.isMouseOver(); }

	/**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#mouseOver(boolean)
     */
	public void mouseOver(boolean b) { model.setMouseOver(b); }

	/**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#hasRndSettings()
     */
	public boolean hasRndSettings()
	{
		return (HiViewerFactory.getRefPixelsID() != -1);
	}

	/**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#pasteRndSettings()
     */
	public void pasteRndSettings()
	{
		if (!hasRndSettings()) {
			UserNotifier un = HiViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Paste settings", "No rendering settings to" +
					"paste. Please first copy settings.");
			return;
		}
		Set nodes = model.getBrowser().getSelectedDisplays();
		if (nodes == null || nodes.size() == 0){
			UserNotifier un = HiViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Paste settings", "Please select the nodes" +
					"you wish to apply the settings to.");
			return;
		}
		Iterator i = nodes.iterator();
		ImageDisplay node;
		Object ho;
		Set<Long> ids = new HashSet<Long>();
		Class klass = null;
		while (i.hasNext()) {
			node = (ImageDisplay) i.next();
			ho = node.getHierarchyObject();
			klass = ho.getClass();
			if (ho instanceof DataObject) {
				ids.add(((DataObject) ho).getId());
			}
		}
		model.firePasteRenderingSettings(klass, ids);
	}

	/**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#resetRndSettings()
     */
	public void resetRndSettings()
	{
		Set nodes = model.getBrowser().getSelectedDisplays();
		if (nodes == null || nodes.size() == 0){
			UserNotifier un = HiViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Reset settings", "Please select the nodes first.");
			return;
		}
		Iterator i = nodes.iterator();
		ImageDisplay node;
		Object ho;
		Set<Long> ids = new HashSet<Long>();
		Class klass = null;
		while (i.hasNext()) {
			node = (ImageDisplay) i.next();
			ho = node.getHierarchyObject();
			klass = ho.getClass();
			if (ho instanceof DataObject) {
				ids.add(((DataObject) ho).getId());
			}
		}
		model.fireResetRenderingSettings(klass, ids);
	}
	
	/**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#rndSettingsPasted(Map)
     */
	public void rndSettingsPasted(Map map)
	{
		if (map == null || map.size() != 2) return;
		Collection failure = (Collection) map.get(Boolean.FALSE);
		UserNotifier un = HiViewerAgent.getRegistry().getUserNotifier();
		if (failure.size() == 0) {
			un.notifyInfo("Paste settings", "Rendering settings have been " +
					"applied to all selected images.");
		} else {
			String s = "";
			Iterator i = failure.iterator();
			int index = 0;
			int size = failure.size()-1;
			while (i.hasNext()) {
				s += (Long) i.next();
				if (index != size) {
					if (index%10 == 0) s += "\n";
					else s += ", ";
				}
				
				index++;
			}
			s.trim();
			un.notifyInfo("Paste settings", "Rendering settings couldn't be " +
							"applied to the following images: \n"+s);
		}
	}
	
}