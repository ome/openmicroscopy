/*
 * org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapDispatcher
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmicroscopy.ds.dto.SemanticType;
import org.openmicroscopy.shoola.agents.browser.BrowserAgent;
import org.openmicroscopy.shoola.agents.browser.BrowserEnvironment;
import org.openmicroscopy.shoola.agents.browser.BrowserModel;
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.SemanticTypesService;

/**
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class HeatMapDispatcher implements HeatMapTreeListener
{
    private HeatMapModel model;
    
    public HeatMapDispatcher(HeatMapModel model)
    {
        this.model = model;    
    }
    
    /**
     * Fills in stuff in the browser model accordingly.
     * @see org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapTreeListener#nodeSelected(org.openmicroscopy.shoola.agents.browser.heatmap.SemanticTypeTree.TreeNode)
     */
    public void nodeSelected(SemanticTypeTree.TreeNode node)
    {
        if(node == null) return;
        if(node.getFQName() == null) return;
        if(!(node instanceof SemanticTypeTree.ElementNode)) return;
        if(node.isLazilyInitialized())
        {
            displayInformation(node);
            return;
        }
        else
        {
            node.markAsInitialized(true);
            BrowserEnvironment env = BrowserEnvironment.getInstance();
            BrowserAgent agent = env.getBrowserAgent();
            SemanticTypesService sts = agent.getSemanticTypesService();
            
            SemanticTypeTree.TreeNode pathNode = node;
            SemanticTypeTree.TypeNode parentNode = null;
            while(pathNode.getParent() instanceof SemanticTypeTree.TypeNode &&
                  pathNode.getParent() != null)
            {
                parentNode = (SemanticTypeTree.TypeNode)node.getParent();
                parentNode.markAsInitialized(true);
                Set children = parentNode.getChildren();
                for(Iterator iter = children.iterator(); iter.hasNext();)
                {
                    Object o = iter.next();
                    // NOTE: this depends on the contract that the STS
                    // retrieves the attribute as a whole, which is correct
                    // but hard to explicitly formalize and check
                    if(o instanceof SemanticTypeTree.ElementNode)
                    {
                        ((SemanticTypeTree.ElementNode)o).
                            markAsInitialized(true);
                    }
                    pathNode = parentNode;
                }
            }
            
            SemanticType parentType = parentNode.getType();
            String name = parentType.getName();
            BrowserModel source = model.getInfoSource();
            
            Map imageIDMap = source.getImageDataMap();
            List imageIDList = new ArrayList(imageIDMap.keySet());
            Collections.sort(imageIDList);
            
            try
            {
                List attributeList =
                    sts.retrieveImageAttributes(name,node.getFQName(),imageIDList);
                System.err.println("got " + attributeList.size() + " records.");
            }
            catch(DSOutOfServiceException dso)
            {
                System.err.println("could not retrieve data.");
            }
            catch(DSAccessException dsa)
            {
                System.err.println("could not retrieve data.");
            }
        }
    }
    
    private void displayInformation(SemanticTypeTree.TreeNode node)
    {
        System.err.println("display instead.");
    }
}
