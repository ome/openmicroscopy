/*
 * org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapModel
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openmicroscopy.ds.dto.SemanticType;
import org.openmicroscopy.shoola.agents.browser.BrowserModel;

/**
 * Creates a basis for the heat map, based on a particular browser model.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class HeatMapModel
{
    private BrowserModel source;
    
    private SemanticTypeTree treeModel;
    
    private Set modelListeners;
    
    /**
     * Creates an initial heat map model for the particular browser agent.
     * Picks out the scalar values for display (and classifications)
     * @param source The BrowserModel to base the HeatMapModel off.  Since
     *               the HeatMapModel will draw from the BrowserModel's
     *               relevant semantic types, setSemanticTypes() should have
     *               already been called on the backing model before
     *               instantiating the heat map model.
     */
    public HeatMapModel(BrowserModel source)
    {
        this.source = source;
        modelListeners = new HashSet();
        updateTreeModel();
    }
    
    public BrowserModel getInfoSource()
    {
        return source;
    }
    
    /**
     * Gets the backing ST model.
     * @return
     */
    public SemanticTypeTree getModel()
    {
        return treeModel;
    }
    
    public void setInfoSource(BrowserModel source)
    {
        this.source = source; 
        updateTreeModel();
    }
    
    public void addListener(HeatMapModelListener listener)
    {
        if(listener != null)
        {
            modelListeners.add(listener);
        }
    }
    
    public void removeListener(HeatMapModelListener listener)
    {
        if(listener != null)
        {
            modelListeners.remove(listener);
        }
    }
    
    private void updateTreeModel()
    {
        if(source == null)
        {
            treeModel = new SemanticTypeTree("(empty)");
            return;
        }
        List typesList = source.getRelevantTypes();
        SemanticType[] types = new SemanticType[typesList.size()];
        typesList.toArray(types);
        treeModel = new SemanticTypeTree(source.getDataset().getName()+
                                         " Attributes",types);
        HeatMapFilter.filter(treeModel);
        
        for(Iterator iter = modelListeners.iterator(); iter.hasNext();)
        {
            HeatMapModelListener listener = (HeatMapModelListener)iter.next();
            listener.modelChanged(treeModel);
        }
    }
}
