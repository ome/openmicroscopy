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

import javax.swing.JPanel;

import org.openmicroscopy.ds.st.CategoryGroup;

/**
 * The UI for the colormap.  Wrapped in a JPanel to promote layout
 * flexibility (between JIF and JFs)
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2.1
 * @since OME2.2
 */
public class ColorMapUI extends JPanel
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
        model.addModelListener(this);
        init();
        buildUI();
        groupBar.setCategoryTree(model.getTree());
    }
    
    public void init()
    {
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
            model.removeListener(this);
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
        model.addModelListener(this);
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
    
    /**
     * Tells the dispatcher to cancel the display.
     */
    public void fireModeCancel()
    {
        if(dispatch != null)
        {
            dispatch.fireModeCancel();
        }
    }
    
    /**
     * Tells the dispatcher to redisplay its previous settings, if
     * it had any.
     */
    public void fireModeReactivate()
    {
        if(dispatch != null)
        {
            dispatch.fireModeReactivate();
        }
    }
    
    /**
     * Instruct the view to display information about the specified
     * category group.
     * @param group The group to display in the UI.
     */
    public void fireGroupSelect(CategoryGroup group)
    {
        groupBar.selectGroup(group);
    }

    // build the user interface.
    private void buildUI()
    {
        setLayout(new BorderLayout(2,2));
        add(groupBar,BorderLayout.NORTH);
        add(listUI,BorderLayout.CENTER);
    }

}