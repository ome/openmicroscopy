/*
 * org.openmicroscopy.shoola.agents.browser.events.ClassificationHandler
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmicroscopy.ds.st.Classification;
import org.openmicroscopy.shoola.agents.browser.BrowserController;
import org.openmicroscopy.shoola.agents.browser.BrowserEnvironment;
import org.openmicroscopy.shoola.agents.browser.BrowserManager;
import org.openmicroscopy.shoola.agents.browser.BrowserModel;
import org.openmicroscopy.shoola.agents.browser.colormap.ColorMapManager;
import org.openmicroscopy.shoola.agents.browser.datamodel.AttributeMap;
import org.openmicroscopy.shoola.agents.browser.images.ThumbnailDataModel;
import org.openmicroscopy.shoola.agents.browser.ui.BrowserWrapper;
import org.openmicroscopy.shoola.agents.classifier.events.ClassifyImage;
import org.openmicroscopy.shoola.agents.classifier.events.ClassifyImages;
import org.openmicroscopy.shoola.agents.classifier.events.DeclassifyImage;
import org.openmicroscopy.shoola.agents.classifier.events.DeclassifyImages;
import org.openmicroscopy.shoola.agents.classifier.events.ImagesClassified;
import org.openmicroscopy.shoola.agents.classifier.events.ReclassifyImage;
import org.openmicroscopy.shoola.agents.classifier.events.ReclassifyImages;
import org.openmicroscopy.shoola.env.event.CompletionHandler;
import org.openmicroscopy.shoola.env.event.RequestEvent;
import org.openmicroscopy.shoola.env.event.ResponseEvent;

/**
 * Handles the classification/reclassification of images by telling all
 * relevant browsers to update their views.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2.1
 * @since OME2.2
 */
public class ClassificationHandler implements CompletionHandler
{
    public void handle(RequestEvent request, ResponseEvent response)
    {
        if(!(request instanceof ClassifyImage) &&
           !(request instanceof ClassifyImages) &&
           !(request instanceof ReclassifyImage) &&
           !(request instanceof ReclassifyImages) &&
           // BUG 117 FIX
           !(request instanceof DeclassifyImage) &&
           !(request instanceof DeclassifyImages))
        {
            throw new IllegalArgumentException("Cannot handle this message");
        }
        if(!(response instanceof ImagesClassified))
        {
            throw new IllegalArgumentException("Cannot handle this message");
        }
        
        ImagesClassified ic = (ImagesClassified)response;
        
        BrowserEnvironment env = BrowserEnvironment.getInstance();
        BrowserManager manager = env.getBrowserManager();
        ColorMapManager cmm = env.getColorMapManager();
        
        if(ic.getClassifications() == null ||
           ic.getClassifications().size() == 0)
        {
            return; // nothing to do
        }
        
        List browsers = manager.getAllBrowsers();
        List imageMaps = new ArrayList();
        for(Iterator iter = browsers.iterator(); iter.hasNext();)
        {
            BrowserWrapper wrapper = (BrowserWrapper)iter.next();
            BrowserController controller = wrapper.getController();
            BrowserModel model = controller.getBrowserModel();
            Map imageIDMap = model.getImageDataMap();
            imageMaps.add(imageIDMap);
        }
        
        Set classifications = ic.getClassifications();
        
        for(Iterator iter = classifications.iterator(); iter.hasNext();)
        {
            Classification c = (Classification)iter.next();
            Integer key = new Integer(c.getImage().getID());
            for(Iterator iter2 = imageMaps.iterator(); iter2.hasNext();)
            {
                Map imageMap = (Map)iter2.next();
                if(imageMap.containsKey(key))
                {
                    ThumbnailDataModel tdm =
                        (ThumbnailDataModel)imageMap.get(key);
                    AttributeMap attrs = tdm.getAttributeMap();
                    attrs.putAttribute(c);
                }
            }
        }
        
        cmm.updateModel();
    }
}
