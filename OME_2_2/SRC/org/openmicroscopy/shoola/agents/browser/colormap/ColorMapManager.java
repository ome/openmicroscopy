/*
 * org.openmicroscopy.shoola.agents.browser.colormap.ColorMapManager
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser.colormap;

import java.util.HashMap;
import java.util.Map;

import org.openmicroscopy.shoola.agents.browser.ActiveWindowListener;
import org.openmicroscopy.shoola.agents.browser.BrowserController;
import org.openmicroscopy.shoola.agents.browser.BrowserEnvironment;
import org.openmicroscopy.shoola.agents.browser.BrowserManager;
import org.openmicroscopy.shoola.agents.browser.BrowserModel;
import org.openmicroscopy.shoola.agents.browser.IconManager;
import org.openmicroscopy.shoola.agents.browser.ui.BrowserWrapper;
import org.openmicroscopy.shoola.agents.browser.ui.UIWrapper;
import org.openmicroscopy.shoola.env.config.Registry;

/**
 * Manages the various models shown by the colormap (there should be only
 * one visible instance of the color map.)
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2.1
 * @since OME2.2.1
 */
public class ColorMapManager implements ActiveWindowListener
{
    private Map datasetModelMap;
    private ColorMapWrapper embeddedUI;
    private BrowserManager uiManager;
    private IconManager iconManager;
    private Registry registry;
    
    public static final int NO_DATASET_SHOWN = -1;
    
    private int datasetShown = NO_DATASET_SHOWN;
    
    /**
     * Constructs the color map manager.
     *
     * @param registry The context of the Shoola application.
     */
    public ColorMapManager(Registry registry)
    {
        datasetModelMap = new HashMap();
        this.registry = registry;
        BrowserEnvironment env = BrowserEnvironment.getInstance();
        this.uiManager = env.getBrowserManager();
        this.iconManager = env.getIconManager();
        uiManager.addSelectionListener(this);
        determineFrameMode();
    }
    
    // determines whether or not the application is running in internal
    // frame or JFrame mode and initializes the component accordingly.
    private void determineFrameMode()
    {
        if(uiManager.managesInternalFrames())
        {
            embeddedUI = new ColorMapInternalFrame();
            uiManager.addStaticWindow(BrowserManager.COLORMAP_KEY,embeddedUI,
                                      iconManager.getSmallIcon(IconManager.COLOR_MAP_ICON));
            
        }
        else
        {
            embeddedUI = new ColorMapFrame();
            uiManager.addStaticWindow(BrowserManager.COLORMAP_KEY,
                                      embeddedUI,
                                      iconManager.getSmallIcon(IconManager.COLOR_MAP_ICON));
        }
    }
    
    /**
     * Returns the ColorMapModel object for the dataset with the specified ID.
     * @param datasetID
     * @return
     */
    public ColorMapModel getModel(int datasetID)
    {
        Integer integer = new Integer(datasetID);
        return (ColorMapModel)datasetModelMap.get(integer);
    }
    
    /**
     * Returns a reference to the wrapped color map UI.
     * @return See above.
     */
    public ColorMapUI getUI()
    {
        return embeddedUI.getColorMapUI();
    }
    
    /**
     * Adds the specified ColorMapModel to the manager.
     * @param model The ColorMapModel to add.
     */
    public void putColorMapModel(ColorMapModel model)
    {
        if(model == null) return;
        BrowserModel browserModel = model.getSource();
        int datasetID = browserModel.getDataset().getID();
        datasetModelMap.put(new Integer(datasetID),model);
        if(datasetID == datasetShown)
        {
            embeddedUI.getColorMapUI().modelChanged(model);
        }
    }
    
    /**
     * Removes the color map model for the dataset with the specified ID from
     * the manager.
     * @param datasetID The ID of the dataset to remove.
     */
    public void removeModel(int datasetID)
    {
        datasetModelMap.remove(new Integer(datasetID));
        if(datasetID == datasetShown)
        {
            datasetShown = NO_DATASET_SHOWN;
            embeddedUI.getColorMapUI().reset();
        }
    }
    
    /**
     * Updates the wrapped UI to display the color map model of the dataset with
     * the specified ID.
     * @param datasetID See above.
     */
    public void showModel(int datasetID)
    {
        ColorMapModel model = getModel(datasetID);
        if(model != null)
        {
            datasetShown = datasetID;
            embeddedUI.getColorMapUI().modelChanged(model);
        }
        else
        {
            datasetShown = NO_DATASET_SHOWN;
            embeddedUI.getColorMapUI().reset();
        }
    }
    
    /**
     * Update the currently selected color data model.
     */
    public void updateModel()
    {
        ColorMapModel model = getModel(datasetShown);
        if(model != null)
        {
            model.fireUpdated();
        }
    }
    
    /**
     * Responds to window activation at the layout component level.
     * @see org.openmicroscopy.shoola.agents.browser.BrowserSelectionListener#browserSelected(org.openmicroscopy.shoola.agents.browser.BrowserController)
     */
    public void windowActive(UIWrapper wrapper)
    {
        browserSelected((BrowserWrapper)wrapper);
    }
    
    /*
     * Updates the color map UI.
     */
    private void browserSelected(BrowserWrapper wrapper)
    {
        try
        {
            BrowserController controller = wrapper.getController();
            int datasetID = controller.getBrowserModel().getDataset().getID();
            showModel(datasetID);
        }
        // hasn't been loaded yet; BrowserAgent will pick it up
        catch(NullPointerException npe) {}
    }
}
