/*
 * org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapUI
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
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;

/**
 * The Swing UI for the heat map controls.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public final class HeatMapUI extends JInternalFrame
                             implements HeatMapModelListener
{
    private HeatMapModel model;
    private HeatMapTreeUI treePanel;
    private HeatMapGradientUI gradPanel;
    
    /**
     * Create an empty heat map UI.
     */
    public HeatMapUI()
    {
        super();
        treePanel = new HeatMapTreeUI(null);
        gradPanel = new HeatMapGradientUI();
        buildUI();
    }
    
    /**
     * Create a heat map UI based on the specified model.
     * @param model
     */
    public HeatMapUI(HeatMapModel model)
    {
        super();
        if(model == null)
        {
            return;
        }
        this.model = model;
        model.addListener(this);
        
        treePanel = new HeatMapTreeUI(model.getModel());
        treePanel.addListener(new HeatMapDispatcher(model));
        gradPanel = new HeatMapGradientUI();
        buildUI();
    }
    
    /**
     * Resets (clears) the current heat map.
     */
    public void reset()
    {
        model = null;
        treePanel = new HeatMapTreeUI(null);
        gradPanel = new HeatMapGradientUI();
        repaint();
    }
    
    private void buildUI()
    {
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        
        JScrollPane scrollPane = new JScrollPane(treePanel);
        scrollPane.setPreferredSize(new Dimension(250,200));
        scrollPane.setSize(new Dimension(250,200));
        contentPane.add(scrollPane,BorderLayout.CENTER);
        
        gradPanel.setPreferredSize(new Dimension(250,100));
        gradPanel.setSize(new Dimension(250,100));
        gradPanel.setEnabled(false);
        contentPane.add(gradPanel,BorderLayout.SOUTH);
        
        if(model != null)
        {
            setTitle("HeatMap: " + model.getInfoSource().getDataset().getName());
        }
        else
        {
            setTitle("HeatMap: [no data]");
        }
        pack();
    }
    
    /**
     * Responds to a model change.
     */
    public void modelChanged(HeatMapModel model)
    {
        if(model == null)
        {
            return;
        }
        this.model = model;
        setTitle("HeatMap: " + model.getInfoSource().getDataset().getName());
        treePanel.setModel(model.getModel());
        treePanel.removeAllListeners();
        treePanel.addListener(new HeatMapDispatcher(model));
        gradPanel.setEnabled(false);
        revalidate();
        repaint();
    }

}
