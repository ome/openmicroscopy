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

import org.openmicroscopy.shoola.agents.browser.BrowserModel;

/**
 * Manages the various models shown by the colormap (there should be only
 * one visible instance of the color map.)
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class ColorMapManager
{
    private Map datasetModelMap;
    private ColorMapUI embeddedUI;
    
    public static final int NO_DATASET_SHOWN = -1;
    
    private int datasetShown = NO_DATASET_SHOWN;
    
    /**
     * Constructs the color map manager.
     *
     */
    public ColorMapManager()
    {
        datasetModelMap = new HashMap();
        embeddedUI = new ColorMapUI();
    }
    
    public ColorMapModel getModel(int datasetID)
    {
        Integer integer = new Integer(datasetID);
        return (ColorMapModel)datasetModelMap.get(integer);
    }
    
    public ColorMapUI getUI()
    {
        return embeddedUI;
    }
    
    public void putColorMapModel(ColorMapModel model)
    {
        if(model == null) return;
        BrowserModel browserModel = model.getSource();
        int datasetID = browserModel.getDataset().getID();
        datasetModelMap.put(new Integer(datasetID),model);
    }
    
    public void removeModel(int datasetID)
    {
        datasetModelMap.remove(new Integer(datasetID));
        if(datasetID == datasetShown)
        {
            datasetShown = NO_DATASET_SHOWN;
            embeddedUI.reset();
        }
    }
    
    public void showModel(int datasetID)
    {
        ColorMapModel model = getModel(datasetID);
        if(model != null)
        {
            datasetShown = datasetID;
            embeddedUI.modelChanged(model);
        }
        else
        {
            datasetShown = NO_DATASET_SHOWN;
            embeddedUI.reset();
        }
    }
    
    public void updateModel()
    {
        ColorMapModel model = getModel(datasetShown);
        if(model != null)
        {
            model.fireUpdated();
        }
    }
}
