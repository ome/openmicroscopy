/*
 * org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapManager
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
package org.openmicroscopy.shoola.agents.browser.heatmap;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages instances of the heat maps (keeps track as to not provoke
 * reloading from the DB, and to make sure that the lazy initialization
 * information in each heat map remains updated)
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public final class HeatMapManager
{
    private Map datasetModelMap;
    private HeatMapUI embeddedUI;
    
    public static final int NO_DATASET_SHOWN = -1;

    private int datasetShown = NO_DATASET_SHOWN;
    
    /**
     * Constructs the heat map manager.
     */
    public HeatMapManager()
    {
        datasetModelMap = new HashMap();
        embeddedUI = new HeatMapUI();
    }
    
    /**
     * Puts the heat map model into the model.  The key is computed by
     * getting the dataset ID of the heat map model's backing browser model.
     * 
     * @param model The heat map model to store in this manager.
     */
    public void putHeatMapModel(HeatMapModel model)
    {
        if(model == null) return;
        int datasetID = model.getInfoSource().getDataset().getID();
        datasetModelMap.put(new Integer(datasetID),model);
    }
    
    /**
     * Gets the heat map model associated with the dataset with the
     * specified ID.
     * @param datasetID The ID of the dataset to retrieve the heat map for.
     * @return See above.
     */
    public HeatMapModel getModel(int datasetID)
    {
        Integer intVal = new Integer(datasetID);
        return (HeatMapModel)datasetModelMap.get(intVal);
    }
    
    /**
     * Removes the heat map model associated with the dataset with the
     * specified ID from the manager.
     * @param datasetID The ID of the dataset to remove.
     */
    public void removeModel(int datasetID)
    {
        Integer intVal = new Integer(datasetID);
        datasetModelMap.remove(intVal);
        if(datasetID == datasetShown)
        {
            embeddedUI.reset();
        }
    }
    
    /**
     * Shows the current information/heatmap for the dataset with the
     * specified ID.
     * @param datasetID The ID of the dataset to show.
     */
    public void showModel(int datasetID)
    {
        HeatMapModel model = getModel(datasetID);
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
    
    /**
     * Returns a reference to the heat map UI component.
     * @return See above.
     */
    public HeatMapUI getUI()
    {
        return embeddedUI;
    }
}
