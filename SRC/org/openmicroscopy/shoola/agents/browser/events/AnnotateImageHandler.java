/*
 * org.openmicroscopy.shoola.agents.browser.events.AnnotateImageHandler
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
import java.util.Map;

import org.openmicroscopy.shoola.agents.annotator.events.AnnotateImage;
import org.openmicroscopy.shoola.agents.annotator.events.ImageAnnotated;
import org.openmicroscopy.shoola.agents.browser.BrowserController;
import org.openmicroscopy.shoola.agents.browser.BrowserEnvironment;
import org.openmicroscopy.shoola.agents.browser.BrowserManager;
import org.openmicroscopy.shoola.agents.browser.BrowserModel;
import org.openmicroscopy.shoola.agents.browser.datamodel.AttributeMap;
import org.openmicroscopy.shoola.agents.browser.images.ThumbnailDataModel;
import org.openmicroscopy.shoola.agents.browser.ui.UIWrapper;
import org.openmicroscopy.shoola.env.event.CompletionHandler;
import org.openmicroscopy.shoola.env.event.RequestEvent;
import org.openmicroscopy.shoola.env.event.ResponseEvent;

/**
 * Standard handler for notifying all browsers that the annotation of a
 * particular image has changed.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class AnnotateImageHandler implements CompletionHandler
{
    /**
     * @see org.openmicroscopy.shoola.env.event.CompletionHandler#handle(org.openmicroscopy.shoola.env.event.RequestEvent, org.openmicroscopy.shoola.env.event.ResponseEvent)
     */
    public void handle(RequestEvent request, ResponseEvent response)
    {
        if(!(request instanceof AnnotateImage) ||
           !(response instanceof ImageAnnotated))
        {
            System.err.println("invalid ACT types for AnnotateImageHandler");
            return;
        }
        AnnotateImage ai = (AnnotateImage)request;
        ImageAnnotated ia = (ImageAnnotated)response;
        
        BrowserEnvironment env = BrowserEnvironment.getInstance();
        BrowserManager manager = env.getBrowserManager();
        
        // check all active and update annotation where applicable
        List browsers = manager.getAllBrowsers();
        Integer imageID = new Integer(ai.getID());
        
        // nothing doing (haven't specified how to delete annotations yet TODO)
        if(ia.getAnnotation() == null) return;
        
        // delve down to data model; then update.  Kind of a pain, for now.
        for(Iterator iter = browsers.iterator(); iter.hasNext();)
        {
            UIWrapper wrapper = (UIWrapper)iter.next();
            BrowserController controller = wrapper.getController();
            BrowserModel model = controller.getBrowserModel();
            Map imageIDMap = model.getImageDataMap();
            if(imageIDMap.containsKey(imageID))
            {
                ThumbnailDataModel tdm =
                    (ThumbnailDataModel)imageIDMap.get(imageID);
                AttributeMap attrs = tdm.getAttributeMap();
                attrs.putAttribute(ia.getAnnotation());
            }
            model.fireModelUpdated();
        }
    }

}
