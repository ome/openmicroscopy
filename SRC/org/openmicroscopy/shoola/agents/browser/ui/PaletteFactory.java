/*
 * org.openmicroscopy.shoola.agents.browser.ui.PaletteFactory
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
 
package org.openmicroscopy.shoola.agents.browser.ui;

import org.openmicroscopy.shoola.agents.browser.BrowserMode;
import org.openmicroscopy.shoola.agents.browser.BrowserModel;
import org.openmicroscopy.shoola.agents.browser.BrowserTopModel;
import org.openmicroscopy.shoola.agents.browser.events.MouseDownActions;
import org.openmicroscopy.shoola.agents.browser.events.PiccoloAction;
import org.openmicroscopy.shoola.agents.browser.events.PiccoloActionFactory;
import org.openmicroscopy.shoola.agents.browser.events.PiccoloModifiers;

/**
 * Makes the appropriate palettes for each browser window.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class PaletteFactory
{
    /**
     * Construct the main palette.
     * @param model The BrowserTopModel to use as a callback/reference.
     * @return A main palette tied to the specified model.
     */
    public static BPalette getMainPalette(final BrowserModel backingModel,
                                          final BrowserTopModel model)
    {
        if(model == null)
        {
            return null;
        }
        
        BPalette palette = new BPalette(model,"Modes");
        BIcon defaultIcon = new BIcon("Normal");
        MouseDownActions defaultActions = new MouseDownActions();
        
        // add the default select icon (finally, piecing together the reusable
        // UI action stuff)
        PiccoloAction defaultSelectAction =
            PiccoloActionFactory.getModeChangeAction(backingModel,
                                                     BrowserModel.MAJOR_UI_MODE_NAME,
                                                     BrowserMode.DEFAULT_MODE);
                                                     
        defaultActions.setMouseClickAction(PiccoloModifiers.NORMAL,
                                           defaultSelectAction);
        defaultIcon.setMouseDownActions(defaultActions);
        
        
        // add the annotate mode select icon
        BIcon annotateIcon = new BIcon("Annotate");
        MouseDownActions annotateActions = new MouseDownActions();
        PiccoloAction annotateSelectAction =
            PiccoloActionFactory.getModeChangeAction(backingModel,
                                                     BrowserModel.MAJOR_UI_MODE_NAME,
                                                     BrowserMode.ANNOTATE_MODE);
                   
        annotateActions.setMouseClickAction(PiccoloModifiers.NORMAL,
                                           annotateSelectAction);
        annotateIcon.setMouseDownActions(annotateActions);
        
        
        // add the classify mode select icon
        BIcon classifyIcon = new BIcon("Classify");
        MouseDownActions classifyActions = new MouseDownActions();
        PiccoloAction classifySelectAction =
            PiccoloActionFactory.getModeChangeAction(backingModel,
                                                     BrowserModel.MAJOR_UI_MODE_NAME,
                                                     BrowserMode.CLASSIFY_MODE);
                                             
        annotateActions.setMouseClickAction(PiccoloModifiers.NORMAL,
                                           classifySelectAction);
        annotateIcon.setMouseDownActions(classifyActions);
        
        
        // add the graph mode select icon
        BIcon graphIcon = new BIcon("Graph");
        MouseDownActions graphActions = new MouseDownActions();
        PiccoloAction graphSelectAction =
            PiccoloActionFactory.getModeChangeAction(backingModel,
                                                     BrowserModel.MAJOR_UI_MODE_NAME,
                                                     BrowserMode.GRAPH_MODE);
                                                     
        annotateActions.setMouseClickAction(PiccoloModifiers.NORMAL,
                                           graphSelectAction);
        annotateIcon.setMouseDownActions(graphActions);
        
        
        palette.addIcon(defaultIcon);
        palette.addIcon(annotateIcon);
        palette.addIcon(classifyIcon);
        palette.addIcon(graphIcon);
        return palette;
    }
}
