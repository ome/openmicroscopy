/*
 * org.openmicroscopy.shoola.agents.browser.BrowserManager
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
import java.util.Collections;
import java.util.List;

/**
 * A class that manages multiple instances of browser windows.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since 2.2
 */
public class BrowserManager
{
  private List browserList;
  
  /**
   * Constructs a new browser manager.
   */
  public BrowserManager()
  {
    browserList = new ArrayList();
  }
  
  /**
   * Adds a browser to the manager.
   * @param browser The controller to the browser to add.
   */
  public void addBrowser(BrowserController browser)
  {
    if(browser != null)
    {
      browserList.add(browser);
    }
  }
  
  /**
   * Gets the browser at the specified index.  This will return null
   * if the index is invalid.
   * @param index The index of the browser to access.
   * @return The accessed browser.
   */
  public BrowserController getBrowser(int index)
  {
    try
    {
      return (BrowserController)browserList.get(index);
    }
    catch(Exception e)
    {
      // TODO: log message here
      return null;
    }
  }
  
  /**
   * Gets the number of browsers in the manager.
   * @return
   */
  public int getBrowserCount()
  {
    return browserList.size();
  }
  
  /**
   * Returns an unmodifiable list of all the browsers in the manager.
   * @return See above.
   */
  public List getAllBrowsers()
  {
    return Collections.unmodifiableList(browserList);
  }
  
  /**
   * Removes the specified browser from the manager, if it is present.
   * @param browser The browser to remove.
   */
  public void removeBrowser(BrowserController browser)
  {
    if(browser != null && browserList.contains(browser))
    {
      browserList.remove(browser);
    }
  }
}
