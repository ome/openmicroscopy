/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerComponent
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

package org.openmicroscopy.shoola.agents.treeviewer.view;





//Java imports

//Third-party libraries

//Application-internal dependencies
import java.util.Iterator;
import java.util.Map;

import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerTranslator;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;

import pojos.DataObject;

/** 
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class TreeViewerComponent
    extends AbstractComponent
    implements TreeViewer
{
    
    /** The Model sub-component. */
    private TreeViewerModel     model;
    
    /** The Controller sub-component. */
    private TreeViewerControl   controller;
    
    /** The View sub-component. */
    private TreeViewerWin       view;
    
    /**
     * Creates a new instance.
     * The {@link #initialize() initialize} method should be called straight 
     * after to complete the MVC set up.
     * 
     * @param model The Model sub-component.
     */
    TreeViewerComponent(TreeViewerModel model)
    {
        if (model == null) throw new NullPointerException("No model."); 
        this.model = model;
        controller = new TreeViewerControl(this);
        view = new TreeViewerWin();
    }
    
    /** Links up the MVC triad. */
    void initialize()
    {
        controller.initialize(view);
        view.initialize(controller, model);
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#getState()
     */
    public int getState() { return model.getState(); }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#activate()
     */
    public void activate()
    {
        // TODO Auto-generated method stub
        
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#getBrowsers()
     */
    public Map getBrowsers() { return model.getBrowsers(); }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#discard()
     */
    public void discard()
    {
        Map browsers = getBrowsers();
        Iterator i = browsers.values().iterator();
        while (i.hasNext())
            ((Browser) i.next()).discard();
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#getSelectedBrowser()
     */
    public Browser getSelectedBrowser() { return model.getSelectedBrowser(); }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#getSelectedBrowser()
     */
    public void addBrowser(int browserType)
    {
        int state = model.getState();
        if ((state == DISCARDED))
            throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED state");
        Map browsers = model.getBrowsers();
        Browser browser = (Browser) browsers.get(new Integer(browserType));
        if (browser != null) {
            model.setSelectedBrowser(browser);
            view.addBrowser(browser);
        }
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#createDataObject(Class)
     */
    public void createDataObject(Class doType)
    {
        //model.setDataObjectType(doType);
        view.createDataObject(doType); 
    }

    /**
     * Implemented as specified by the {@link TreeViewer} interface.
     * @see TreeViewer#createDataObject(Class)
     */
    public void createObject(DataObject object)
    {
        if (object == null)
            throw new IllegalArgumentException("No object to create.");
        model.fireDataObjectCreation(object);
        //Should be done there but for the moment.
        Browser browser = model.getSelectedBrowser();
        if (browser != null)
            browser.setCreatedNode(
                    TreeViewerTranslator.transformDataObject(object));
        view.cancelDataObjectCreation();
    }
    
}
