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

import java.util.ArrayList;
import java.util.List;

import org.openmicroscopy.shoola.env.data.dto.Dataset;

/* 
 * TODO: Get AgentEventListener/EventBus architecture into main CVS tree;
 * talk to J-M and Andrea about this
 */

/**
 * The agent class that connects the browser to the rest of the client
 * system, and receives events triggered by other parts of the client.
 * Subscribes and places events on the EventBus.
 * 
 * The OMEBrowserAgent responds to the following events: (list events)
 * 
 * The OMEBrowserAgent places the following events on the queue: (list)
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
 * <b>Internal Version:</b> $Revision$ $Date$
 * @version 2.2
 * @since 2.2
 */
public class BrowserAgent // implements AgentEventListener
{
  private List controllers;
  
  /**
   * Initialize the browser controller and register the OMEBrowerAgent with
   * the EventBus.
   */
  public BrowserAgent()
  {
    /* 
     * TODO: initialize browser agent (initialize controller, initialize data
     * agent, register with event bus, etc.
     */
     controllers = new ArrayList();
  }
  
  /**
   * Returns a reference to the browser controller at the specified index.
   * 
   * @param index The index of the browser to access.
   * @return The browser controller.
   */
  public BrowserController getBrowser(int index)
  {
    return (BrowserController)controllers.get(index);
  }
  
  /**
   * Returns the number of active browser windows.
   * @return See above.
   */
  public int getBrowserCount()
  {
    return controllers.size();
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
   * Instructs the agent to load the specified Dataset into the specified
   * browser window.
   * 
   * @param browserIndex The index of the browser to load.
   * @param dataset The dataset to load.
   * @return true If the load was successful, false if not or if dataset was
   *         null.
   */
  public boolean loadDataset(int browserIndex, Dataset dataset)
  {
    if(dataset == null)
    {
      return false; // maybe unload?  nah...
    }
    // TODO: fill in loadDataset(Dataset)
    return true;
  }
  
  /**
   * Responds to an event posted in the EventBus's queue.
   * 
   * @param event The event to be processed.
   * @see (insert reference to EventBus, AgentEventListener)
   * TODO: insert javadoc reference to EventBus classes
   */
  public void eventFired(/* AgentEvent event */)
  {
    // TODO: fill in eventFired, integrate AgentEvent into main CVS tree
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
    if(IDs == null || IDs.length == 0)
    {
      return;
    }
    // TODO: fill in loadImages(int[])
  }
  
  /**
   * Instruct the BrowserAgent to make the browser components visible.
   * @param browserIndex The index of the browser to show.
   */
  public void showBrowser(int browserIndex)
  {
    // TODO: fill in showBrowser()
  }
  
  /**
   * Instruct the OMEBrowserAgent to make the browser components hidden.
   * @param browserIndex The index of the browser to hide.
   */
  public void hideBrowser(int browserIndex)
  {
    // TODO: fill in hideBrowser()
  }
  
}
