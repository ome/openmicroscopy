/*
 * org.openmicroscopy.shoola.agents.hiviewer.view.HiViewerComponent
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.hiviewer.view;


//Java imports
import java.awt.image.BufferedImage;
import java.util.Set;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoard;
import org.openmicroscopy.shoola.agents.hiviewer.treeview.TreeView;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import pojos.ExperimenterData;

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
     * @see HiViewer#activate()
     */
    public void activate()
    {
        switch (model.getState()) {
            case NEW:
                model.fireHierarchyLoading();
                view.setOnScreen();
                fireStateChange();
                break;
            case DISCARDED:
                throw new IllegalStateException(
                        "This method can't be invoked in the DISCARDED state.");
            default:
                view.deIconify();
        }
    }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#setHierarchyRoots(java.util.Set)
     */
    public void setHierarchyRoots(Set roots)
    {
        if (model.getState() != LOADING_HIERARCHY)
            throw new IllegalStateException(
                    "This method can only be invoked in the LOADING_HIERARCHY "+
                    "state.");
        
        model.createBrowser(roots);
        model.createClipBoard();
        model.fireThumbnailLoading();
        //b/c fireThumbnailLoading() sets the state to READY if there is no
        //image.
        setStatus(HiViewer.PAINTING_TEXT, -1);
        fireStateChange();
    }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#setThumbnail(int, java.awt.image.BufferedImage)
     */
    public void setThumbnail(int imageID, BufferedImage thumb)
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
        int state = model.getState();
        switch (state) {
            case LOADING_THUMBNAILS:
            case READY:
                return model.getBrowser();
            default:
                throw new IllegalStateException(
                   "This method can only be invoked in the LOADING_THUMBNAILS "+
                        "or READY state.");
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
        int state = model.getState();
        switch (state) {
            case LOADING_THUMBNAILS:
            case READY:
                return view.getViewTitle();
            default:
                throw new IllegalStateException(
                   "This method can only be invoked in the LOADING_THUMBNAILS "+
                        "or READY state.");
        }       
    }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#getClipBoard()
     */
    public ClipBoard getClipBoard()
    {
        int state = model.getState();
        switch (state) {
            case LOADING_THUMBNAILS:
            case READY:
                return model.getClipBoard();
            default:
                throw new IllegalStateException(
                   "This method can only be invoked in the LOADING_THUMBNAILS "+
                        "or READY state.");
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
        if (state != LOADING_THUMBNAILS && state != READY)
            throw new IllegalStateException(
                   "This method can only be invoked in the LOADING_THUMBNAILS "+
                        "or READY state.");
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
     * @see HiViewer#getTreeView()
     */
    public TreeView getTreeView()
    {
        int state = model.getState();
        if (state != LOADING_THUMBNAILS && state != READY)
            throw new IllegalStateException(
                   "This method can only be invoked in the LOADING_THUMBNAILS "+
                        "or READY state.");
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
     * @see HiViewer#getRootLevel()
     */
    public Class getRootLevel()
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED state.");
        return model.getRootLevel();
    }

    /**
     * Implemented as specified by the {@link HiViewer} interface.
     * @see HiViewer#getRootID()
     */
    public int getRootID()
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED state.");
        return model.getRootID();
    }

}
