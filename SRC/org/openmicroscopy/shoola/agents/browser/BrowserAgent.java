/*
 * org.openmicroscopy.shoola.agents.browser.BrowserAgent
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
package org.openmicroscopy.shoola.agents.browser;

import org.openmicroscopy.shoola.agents.events.LoadDataset;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.DataManagementService;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.TopFrame;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

/**
 * The agent class that connects the browser to the rest of the client
 * system, and receives events triggered by other parts of the client.
 * Subscribes and places events on the EventBus.
 * 
 * The BrowserAgent responds to the following events: (list events)
 * 
 * The BrowserAgent places the following events on the queue: (list)
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br><br>
 * <b>Internal Version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class BrowserAgent implements Agent, AgentEventListener
{
    private Registry registry;
    private EventBus eventBus;
    private BrowserEnvironment env;
    private TopFrame tf;

    /**
     * Initialize the browser controller and register the OMEBrowerAgent with
     * the EventBus.
     */
    public BrowserAgent()
    {
        env = BrowserEnvironment.getInstance();
        env.setBrowserAgent(this);
    }
    
    /**
     * Does activation stuff (incomplete).
     * 
     * @see org.openmicroscopy.shoola.env.Agent#activate()
     */
    public void activate()
    {
        // for now, do nothing; wait until triggered to load
        // this will be different if there's save info in the context file
    }
    
    /**
     * Checks if termination is possible (incomplete)
     * 
     * @see org.openmicroscopy.shoola.env.Agent#canTerminate()
     */
    public boolean canTerminate()
    {
        // TODO Auto-generated method stub
        return true;
    }
    
    /**
     * Does termination stuff (incomplete)
     * 
     * @see org.openmicroscopy.shoola.env.Agent#terminate()
     */
    public void terminate()
    {
        // TODO Auto-generated method stub
    }
    
    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.Agent#setContext(org.openmicroscopy.shoola.env.config.Registry)
     */
    public void setContext(Registry ctx)
    {
        this.registry = ctx;
        this.eventBus = ctx.getEventBus();
        this.tf = ctx.getTopFrame();
        eventBus.register(this,LoadDataset.class);
    }
    
    /**
     * Instructs the agent to load the Dataset with the given ID into
     * a new browser window.
     * @param browserIndex The ID (primary key) of the dataset to load.
     * @return Whether or not the dataset was succesfully loaded.
     */
    public boolean loadDataset(int datasetID)
    {
        DataManagementService dms = registry.getDataManagementService();
        DatasetData dataset;
        try
        {
            dataset = dms.retrieveDataset(datasetID);
            return loadDataset(dataset);
        }
        catch(DSAccessException dsae)
        {
            UserNotifier notifier = registry.getUserNotifier();
            notifier.notifyError("Data retrieval failure",
                "Unable to retrieve dataset (id = " + datasetID + ")", dsae);
            return false;
        }
        catch(DSOutOfServiceException dsoe)
        {
            // pop up new login window (eventually caught)
            throw new RuntimeException(dsoe);
        }
    }
    
    // loads the information from the Dataset into a BrowserModel, and the
    // also is responsible for triggering the mechanism that loads all the
    // images.
    private boolean loadDataset(DatasetData datasetModel)
    {
        // TODO fill in this mother
        return true;
    }

    /**
     * Instructs the agent to load the Dataset with the given ID into the
     * specified browser.
     * 
     * @param browserIndex The index of the browser window to load.
     * @param datasetID The ID of the dataset to load.
     * @return true If the load was successful, false if not.
     */
    public boolean loadDataset(int browserIndex, int datasetID)
    {
        // TODO: fill in loadDataset(int)
        return true;
    }

    /**
     * Instruct the BrowserAgent to fire a LoadImage event, to be handled
     * by another part of the client.
     * 
     * @param imageID The ID of the image to load (in a viewer, for example)
     */
    public void loadImage(int imageID)
    {
        // TODO: fill in loadImage(int)
    }

    /**
     * Instruct the BrowserAgent to fire a LoadImages event, to be handled
     * by another part of the client.
     * 
     * @param IDs The IDs of the image to load (in a viewer, for example)
     */
    public void loadImages(int[] IDs)
    {
        if (IDs == null || IDs.length == 0)
        {
            return;
        }
        // TODO: fill in loadImages(int[])
    }
    
    /**
     * Responds to an event on the event bus.
     * 
     * @see org.openmicroscopy.shoola.env.event.AgentEventListener#eventFired(org.openmicroscopy.shoola.env.event.AgentEvent)
     */
    public void eventFired(AgentEvent e)
    {
        if(e instanceof LoadDataset)
        {
            LoadDataset event = (LoadDataset)e;
            // TODO: how to trigger a response
            
        }
    }
}
