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

import javax.swing.JFrame;
import javax.swing.JScrollPane;

/**
 * The Swing UI for the heat map controls.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public final class HeatMapUI extends JFrame
                             implements HeatMapModelListener
{
    private HeatMapModel model;
    private HeatMapTreeUI treePanel;
    private HeatMapGradientUI gradPanel;
    private String title;
    
    public HeatMapUI(HeatMapModel model)
    {
        super("Compare Images");
        if(model == null)
        {
            return;
        }
        this.model = model;
        model.addListener(this);
        
        treePanel = new HeatMapTreeUI(model.getModel());
        gradPanel = new HeatMapGradientUI();
        buildUI();
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
        
        setTitle("HeatMap: " + model.getInfoSource().getDataset().getName());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // change?
        pack();
    }
    
    /**
     * Responds to a model change.
     */
    public void modelChanged(SemanticTypeTree tree)
    {
        treePanel.setModel(tree);
        repaint();
    }

}
