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
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;


/**
 * The Swing UI for the heat map controls.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public final class HeatMapUI extends JPanel
                             implements HeatMapModelListener,
                                        HeatMapDTListener
{
    private HeatMapModel model;
    private HeatMapStatusUI statusPanel;
    private HeatMapTreeUI treePanel;
    private HeatMapGradientUI gradPanel;
    private HeatMapModeBar modeBar;
    private HeatMapScaleBar scaleBar;
    private HeatMapDispatcher dispatcher;
    
    /**
     * Create an empty heat map UI.
     */
    public HeatMapUI()
    {
        super();
        treePanel = new HeatMapTreeUI(null);
        init();
        buildUI();
    }
    
    /**
     * Create a heat map UI based on the specified model.
     * @param model
     */
    public HeatMapUI(HeatMapModel model)
    {
        super();
        init();
        if(model == null)
        {
            return;
        }
        this.model = model;
        model.addListener(this);
        
        treePanel = new HeatMapTreeUI(model.getModel());
        dispatcher = new HeatMapDispatcher(model,statusPanel,gradPanel);
        dispatcher.addLoadListener(treePanel);
        dispatcher.setCurrentMode(modeBar.getCurrentMode());
        dispatcher.setCurrentScale(scaleBar.getCurrentScaleType());
        treePanel.addListener(dispatcher);
        modeBar.addListener(dispatcher);
        scaleBar.addListener(dispatcher);
        buildUI();
    }
    
    private void init()
    {
        gradPanel = new HeatMapGradientUI();
        gradPanel.addDTListener(this);
        statusPanel = new HeatMapStatusUI();
        modeBar = new HeatMapModeBar();
        scaleBar = new HeatMapScaleBar();
    }
    
    /**
     * Resets (clears) the current heat map.
     */
    public void reset()
    {
        model = null;
        treePanel = new HeatMapTreeUI(null);
        gradPanel = new HeatMapGradientUI();
        gradPanel.addDTListener(this);
        repaint();
    }
    
    private void buildUI()
    {
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        
        treePanel.addListener(new HeatMapTreeListener()
        {
            public void nodeSelected(SemanticTypeTree.TreeNode node)
            {
                if(!(node instanceof SemanticTypeTree.ElementNode))
                {
                    gradPanel.setEnabled(false);
                }
                else gradPanel.setEnabled(true);
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(treePanel);
        scrollPane.setPreferredSize(new Dimension(250,200));
        scrollPane.setSize(new Dimension(250,200));
        mainPanel.add(scrollPane,BorderLayout.CENTER);
        
        JPanel controlPanel = new JPanel();
        
        gradPanel.setPreferredSize(new Dimension(250,90));
        gradPanel.setSize(new Dimension(250,90));
        gradPanel.setEnabled(false);
        
        controlPanel.setLayout(new BorderLayout());
        controlPanel.add(gradPanel,BorderLayout.CENTER);
        
        JPanel barPanel = new JPanel();
        barPanel.add(modeBar,BorderLayout.CENTER);
        
        JPanel scalePanel = new JPanel();
        scalePanel.add(scaleBar,BorderLayout.CENTER);
        
        controlPanel.add(barPanel,BorderLayout.NORTH);
        controlPanel.add(scalePanel,BorderLayout.SOUTH);
        
        mainPanel.add(controlPanel,BorderLayout.SOUTH);
        
        add(mainPanel,BorderLayout.CENTER);
        add(statusPanel,BorderLayout.SOUTH);
        
        statusPanel.showMessage("Dataset attributes loaded.");
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
        treePanel.setModel(model.getModel());
        treePanel.removeListener(dispatcher);
        modeBar.removeListener(dispatcher);
        dispatcher = new HeatMapDispatcher(model,statusPanel,gradPanel);
        dispatcher.addLoadListener(treePanel);
        dispatcher.setCurrentMode(modeBar.getCurrentMode());
        dispatcher.setCurrentScale(scaleBar.getCurrentScaleType());
        treePanel.addListener(dispatcher);
        modeBar.addListener(dispatcher);
        scaleBar.addListener(dispatcher);
        gradPanel.setEnabled(false);
        revalidate();
        repaint();
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapDTListener#inBooleanMode()
     */
    public void inBooleanMode()
    {
        modeBar.setEnabled(false);
        scaleBar.setEnabled(false);
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapDTListener#inScalarMode()
     */
    public void inScalarMode()
    {
        modeBar.setEnabled(true);
        scaleBar.setEnabled(true);
    }
    
    /**
     * Instructs the heat map dispatcher to update the currently selected
     * browser to reflect that the heat map is no longer active.
     */
    public void fireModeCancel()
    {
        if(dispatcher != null) dispatcher.fireModeCancel();
    }
    
    /**
     * Instructs the heat map dispatcher to update the currently selected
     * browser to reflect that the heat map has become active.
     */
    public void fireModeReactivate()
    {
        if(dispatcher != null) dispatcher.fireModeReactivate();
    }

}
