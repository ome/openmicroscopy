/*
 * org.openmicroscopy.shoola.agents.browser.ome.OMEBrowserAgent
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
package org.openmicroscopy.shoola.agents.browser.ome;

import org.openmicroscopy.shoola.env.data.dto.Dataset;
import org.openmicroscopy.shoola.agents.browser.BrowserController;

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
public class OMEBrowserAgent // implements AgentEventListener
{
  private Dataset currentDataset;
  // private BrowserSettings browserSettings; (haven't figured this out yet)
  // TODO: Figure out if BrowserSettings class needed here
  private BrowserController controller;
  
  /**
   * Initialize the browser controller and register the OMEBrowerAgent with
   * the EventBus.
   */
  public OMEBrowserAgent()
  {
    /* 
     * TODO: initialize browser agent (initialize controller, initialize data
     * agent, register with event bus, etc.
     */
  }
  
  /**
   * Instructs the agent to load the Dataset with the given ID.
   * 
   * @param ID The ID of the dataset to load.
   * @return true If the load was successful, false if not.
   */
  public boolean loadDataset(int ID)
  {
    // TODO: fill in loadDataset(int)
    return true;
  }
  
  /**
   * Instructs the agent to load the specified Dataset.
   * @param dataset The dataset to load.
   * @return true If the load was successful, false if not or if dataset was
   *         null.
   */
  public boolean loadDataset(Dataset dataset)
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
   * Instruct the OMEBrowserAgent to fire a LoadImage event, to be handled
   * by another part of the client.
   * 
   * @param ID The ID of the image to load (in a viewer, for example)
   */
  public void loadImage(int ID) // make this boolean (for async response?)
  {
    // TODO: fill in loadImage(int)
  }
  
  /**
   * Instruct the OMEBrowserAgent to fire a LoadImages event, to be handled
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
   * Instruct the OMEBrowserAgent to make the browser components visible.
   */
  public void showBrowser()
  {
    // TODO: fill in showBrowser()
  }
  
  /**
   * Instruct the OMEBrowserAgent to make the browser components hidden.
   */
  public void hideBrowser()
  {
    // TODO: fill in hideBrowser()
  }
  
}
