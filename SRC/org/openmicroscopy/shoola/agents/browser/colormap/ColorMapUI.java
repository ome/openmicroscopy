/*
 * org.openmicroscopy.shoola.agents.browser.colormap.ColorMapUI
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

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 * The UI for the colormap.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class ColorMapUI extends JInternalFrame
                        implements ColorMapModelListener
{
    private ColorMapModel model;
    private ColorMapGroupBar groupBar;
    private ColorMapDispatcher dispatch;
    private ColorMapListUI listUI;
    
    public ColorMapUI()
    {
        init();
        buildUI();
    }
    
    public ColorMapUI(ColorMapModel model)
    {
        this.model = model;
        init();
        buildUI();
        groupBar.setCategoryTree(model.getTree());
    }
    
    public void init()
    {
        setTitle("View Phenotypes");
        groupBar = new ColorMapGroupBar();
        listUI = new ColorMapListUI();
        if(model != null)
        {
            dispatch = new ColorMapDispatcher(model,listUI);
            groupBar.addListener(dispatch);
            listUI.addListener(dispatch);
        }
    }
    
    public void reset()
    {
        if(model != null)
        {
            dispatch.categoriesDeselected(); 
            dispatch.groupsDeselected();
            groupBar.removeListener(dispatch);
            listUI.removeListener(dispatch);
            listUI.setModel(null);
        }
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.colormap.ColorMapModelListener#modelChanged(org.openmicroscopy.shoola.agents.browser.colormap.ColorMapModel)
     */
    public void modelChanged(ColorMapModel model)
    {
        reset();
        this.model = model;
        dispatch = new ColorMapDispatcher(model,listUI);
        groupBar.addListener(dispatch);
        groupBar.setCategoryTree(model.getTree());
        listUI.setModel(null);
        if(model != null)
        {
            groupBar.setEnabled(true);
        }
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.colormap.ColorMapModelListener#modelUpdated(org.openmicroscopy.shoola.agents.browser.colormap.ColorMapModel)
     */
    public void modelUpdated(ColorMapModel model)
    {
        dispatch.fireRedraw();
    }

    
    private void buildUI()
    {
        Container container = getContentPane();
        
        container.setLayout(new BorderLayout(2,2));
        container.add(groupBar,BorderLayout.NORTH);
        container.add(listUI,BorderLayout.CENTER);
        
        addInternalFrameListener(new InternalFrameAdapter()
        {
            public void internalFrameClosing(InternalFrameEvent arg0)
            {
                if(dispatch != null)
                {
                    dispatch.fireModeCancel();
                }
            }
            
            public void internalFrameOpened(InternalFrameEvent arg0)
            {
                if(dispatch != null)
                {
                    dispatch.fireModeReactivate();
                }
            }
        });
        
        pack();
    }

}