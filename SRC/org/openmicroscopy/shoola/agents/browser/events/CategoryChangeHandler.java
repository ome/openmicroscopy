/*
 * org.openmicroscopy.shoola.agents.browser.events.CategoryChangeHandler
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
package org.openmicroscopy.shoola.agents.browser.events;

import java.util.Iterator;
import java.util.List;

import org.openmicroscopy.shoola.agents.browser.BrowserAgent;
import org.openmicroscopy.shoola.agents.browser.BrowserController;
import org.openmicroscopy.shoola.agents.browser.BrowserEnvironment;
import org.openmicroscopy.shoola.agents.browser.BrowserManager;
import org.openmicroscopy.shoola.agents.browser.BrowserModel;
import org.openmicroscopy.shoola.agents.browser.ui.UIWrapper;
import org.openmicroscopy.shoola.agents.classifier.events.CategoriesChanged;
import org.openmicroscopy.shoola.agents.classifier.events.LoadCategories;
import org.openmicroscopy.shoola.env.event.CompletionHandler;
import org.openmicroscopy.shoola.env.event.RequestEvent;
import org.openmicroscopy.shoola.env.event.ResponseEvent;

/**
 * Completion handler for LoadCategories/CategoriesChanged event pairs.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class CategoryChangeHandler implements CompletionHandler
{
    public void handle(RequestEvent request, ResponseEvent response)
    {
        if(!(request instanceof LoadCategories) ||
           !(response instanceof CategoriesChanged))
        {
            System.err.println("Invalid ACT types for CategoryChangeHandler");
            return;
        }
        
        System.err.println("handling complete in CCH");
        LoadCategories lc = (LoadCategories)request;
        CategoriesChanged cc = (CategoriesChanged)response;
        
        if(!cc.isDirty()) return; // no changes to be made
        
        BrowserEnvironment env = BrowserEnvironment.getInstance();
        BrowserManager manager = env.getBrowserManager();
        List browserList = manager.getAllBrowsers();
        
        System.err.println("checking browsers");
        for(Iterator iter = browserList.iterator(); iter.hasNext();)
        {
            UIWrapper wrapper = (UIWrapper)iter.next();
            BrowserController controller = wrapper.getController();
            BrowserModel model = controller.getBrowserModel();
            int browserID = model.getDataset().getID();
            if(browserID == cc.getDatasetID())
            {
                System.err.println("found browser");
                BrowserAgent agent = env.getBrowserAgent();
                model.setCategoryTree(agent.loadCategoryTree(browserID));
                model.fireModelUpdated();
            }
        }
    }
}
