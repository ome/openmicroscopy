/*
 * org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapGraphUI
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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Specifies the action of the UI button.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2.1
 * @since OME2.2.1
 */
public final class HeatMapGraphUI extends JPanel
                                  implements HeatMapLoadListener
{
    private JButton graphButton;
    private Set listeners;
    
    /**
     * Constructs a heat map graph widget.
     */
    public HeatMapGraphUI()
    {
        listeners = new HashSet();
        buildUI();
    }
    
    /*
     * Builds the UI for the widget.
     */
    private void buildUI()
    {
        setLayout(new BorderLayout());
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        graphButton = new JButton("Show Graph");
        graphButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                for(Iterator iter = listeners.iterator(); iter.hasNext();)
                {
                    HeatMapGraphListener listener =
                        (HeatMapGraphListener)iter.next();
                    listener.showGraphView();
                }
            }
        });
        centerPanel.add(graphButton);
        add(centerPanel);
    }
    
    /**
     * Adds a graph listener to this UI.
     * @param listener The listener to add.
     */
    public void addListener(HeatMapGraphListener listener)
    {
        if(listener != null)
        {
            listeners.add(listener);
        }
    }
    
    /**
     * Removes a graph listener from this UI.
     * @param listener The listener to remove.
     */
    public void removeListener(HeatMapGraphListener listener)
    {
        if(listener != null)
        {
            listeners.remove(listener);
        }
    }
    
    /**
     * Disable the graph button in response to a load start.
     * @see org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapLoadListener#loadStarted()
     */
    public void loadStarted()
    {
        graphButton.setEnabled(false);
    }
    
    /**
     * Enable the graph button in response to a load finish and assign the
     * appropriate action.
     * @see org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapLoadListener#loadFinished()
     */
    public void loadFinished()
    {
        graphButton.setEnabled(true);
    }
}
