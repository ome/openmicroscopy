/*
 * org.openmicroscopy.shoola.agents.browser.ui.BrowserPanel
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.BoundedRangeModel;
import javax.swing.Box;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollBar;

import org.openmicroscopy.shoola.agents.browser.BrowserController;
import org.openmicroscopy.shoola.agents.browser.BrowserEnvironment;
import org.openmicroscopy.shoola.agents.browser.IconManager;
import org.openmicroscopy.shoola.agents.browser.events.BrowserActions;

/**
 * Class that bumps the bulk of the browser UI code from the JFrame level
 * to the JPanel level, so that it can be better wrapped for both
 * MDI and multiple-window configurations.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2.2
 * @since OME2.2.2
 */
public class BrowserPanel extends JPanel
{
    private BrowserController controller;
    private BrowserView embeddedView;
    private BrowserEnvironment env;
    
    /**
     * Constructs a browser panel with the view/model from the
     * specified "controller".
     * 
     * @param controller The reference to the browser model/view to
     *                   be shown in the browser panel.
     */
    public BrowserPanel(BrowserController theController)
    {
        setPreferredSize(new Dimension(600,560));
        
        // initializes the browser view.
        if(theController != null)
        {
            this.controller = theController;
            this.embeddedView = controller.getView();
            embeddedView.setZoomToScale(true);
            this.env = BrowserEnvironment.getInstance();
        }
        
        // initialize the overall layout (border layout)
        setLayout(new BorderLayout());
        setupToolbar();
        setupScrollWindow();
        setupStatusView();
    }
    
    /**
     * Perform the initialization routine for the browser view's toolbar
     * panel.
     */
    private void setupToolbar()
    {
        IconManager iconManager = env.getIconManager();
        
        // initialize the toolbar panel, including the autonomous
        // zoom button panel.
        JPanel toolbarPanel = new JPanel();
        toolbarPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        ZoomButtonPanel panel = new ZoomButtonPanel(embeddedView);
        toolbarPanel.add(panel);
        
        // initialize the heat map button.
        JButton heatMapButton =
            new JButton(iconManager.getSmallIcon(IconManager.HEAT_MAP_ICON));
            
        heatMapButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                BrowserActions.SHOW_HEATMAP_ACTION.execute();
            }
        });
        
        heatMapButton.setToolTipText("Show heat map");
        toolbarPanel.add(heatMapButton);
        
        // initialize the color map button.
        JButton colorMapButton = 
            new JButton(iconManager.getSmallIcon(IconManager.COLOR_MAP_ICON));
        
        colorMapButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                BrowserActions.SHOW_COLORMAP_ACTION.execute();
            }
        });
        
        colorMapButton.setToolTipText("View phenotypes (colormap)");
        toolbarPanel.add(colorMapButton);
        
        // add it to the panel.
        this.add(toolbarPanel,BorderLayout.NORTH);
    }
    
    // make a scroll window with scroll bars and the embedded view.
    private void setupScrollWindow()
    {
        final JScrollBar horizontalBar =
            new JScrollBar(JScrollBar.HORIZONTAL);
            
        final JScrollBar verticalBar =
            new JScrollBar(JScrollBar.VERTICAL);
        
        embeddedView.getViewCamera().addCameraListener(new CameraListener()
        {
            // update the scroll bars based on changing camera bounds.
            public void cameraBoundsChanged(double valueX, double valueY,
                                            double extentX, double extentY,
                                            double width, double height)
            {
                int iX = (int)Math.round(valueX);
                int iY = (int)Math.round(valueY);
                int iEX = (int)Math.round(extentX);
                int iEY = (int)Math.round(extentY);
                int iW = (int)Math.round(width);
                int iH = (int)Math.round(height);
                
                BoundedRangeModel horizModel = horizontalBar.getModel();
                BoundedRangeModel vertModel = verticalBar.getModel();
                
                if(iEX+iX > iW) horizontalBar.setEnabled(false);
                else
                {
                    horizontalBar.setEnabled(true);
                    if(iW != horizModel.getMaximum() ||
                       iEX != horizModel.getExtent())
                    {
                        try
                        {
                            BoundedRangeModel hModel =
                                new DefaultBoundedRangeModel(iX,iEX,0,iW);
                            horizontalBar.setModel(hModel);
                        }
                        catch(IllegalArgumentException iae)
                        {
                            System.err.println("illegal width:["+iX+","+iEX+",0,"
                                               +iW+"]");
                            iae.printStackTrace();
                        }
                    }
                    else
                    {
                        horizModel.setValue(iX);
                    }
                }
                
                if(iEY+iY > iH) verticalBar.setEnabled(false);
                else
                {
                    verticalBar.setEnabled(true);
                    if(iH != vertModel.getMaximum() ||
                       iEY != vertModel.getExtent())
                    {
                        try
                        {
                            BoundedRangeModel vModel =
                                new DefaultBoundedRangeModel(iY,iEY,0,iH);
                            verticalBar.setModel(vModel);
                        }
                        catch(IllegalArgumentException iae)
                        {
                            System.err.println("illegal height:["+iY+","+iEY+",0,"
                                               +iH+"]");
                            iae.printStackTrace();
                        }
                    }
                    else
                    {
                        vertModel.setValue(iY);
                    }
                }

            }
        });

        horizontalBar.setEnabled(false);
        horizontalBar.setModel(new DefaultBoundedRangeModel(0,0,0,0));
        horizontalBar.addAdjustmentListener(new AdjustmentListener()
        {
            public void adjustmentValueChanged(AdjustmentEvent e)
            {
                int value = e.getValue();
                embeddedView.getViewCamera().setX(value);
            }

        });
        
        verticalBar.setEnabled(false);
        verticalBar.setModel(new DefaultBoundedRangeModel(0,0,0,0));
        verticalBar.addAdjustmentListener(new AdjustmentListener()
        {
            public void adjustmentValueChanged(AdjustmentEvent e)
            {
                int value = e.getValue();
                embeddedView.getViewCamera().setY(value);
            }

        });
        
        JPanel fakeScrollPane = new JPanel();
        JPanel verticalPanel = new JPanel();
        verticalPanel.setLayout(new BorderLayout());
        verticalPanel.add(embeddedView,BorderLayout.CENTER);
        verticalPanel.add(verticalBar,BorderLayout.EAST);
        
        JPanel horizontalPanel = new JPanel();
        horizontalPanel.setLayout(new BorderLayout());
        horizontalPanel.add(horizontalBar,BorderLayout.CENTER);
        horizontalPanel.add(Box.createHorizontalStrut(verticalBar.getPreferredSize().width),
                            BorderLayout.EAST);
        
        verticalPanel.add(horizontalPanel,BorderLayout.SOUTH);
        
        fakeScrollPane.setLayout(new BorderLayout());
        fakeScrollPane.add(verticalPanel,BorderLayout.CENTER);
        this.add(fakeScrollPane,BorderLayout.CENTER);
    }
    
    // setup the status view.
    private void setupStatusView()
    {
        if(controller.getStatusView() != null)
        {
            this.add(controller.getStatusView(),BorderLayout.SOUTH);
        }
    }
}
