/*
 * org.openmicroscopy.shoola.agents.browser.ui.BrowserInternalFrame
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
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.beans.PropertyVetoException;

import javax.swing.BoundedRangeModel;
import javax.swing.Box;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.openmicroscopy.shoola.agents.browser.BrowserController;
import org.openmicroscopy.shoola.agents.browser.BrowserEnvironment;
import org.openmicroscopy.shoola.agents.browser.IconManager;

/**
 * Wraps a BrowserView in a JInternalFrame for use in MDI applications.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class BrowserInternalFrame extends JInternalFrame
                                  implements UIWrapper
{
    private IntraDragAdapter dragAdapter;
    private BrowserController controller;
    private BrowserView embeddedView;
    private BrowserEnvironment env;
    
    /**
     * Constructs a JInternalFrame that wraps the BrowserController and its
     * BrowserView (which ultimately extends JPanel)
     * 
     * @param controller The controller to wrap.
     */
    public BrowserInternalFrame(BrowserController theController)
    {
        setSize(600,600);
        setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        
        if(theController != null)
        {
            this.controller = theController;
            String title = controller.getName();
            setTitle("Image Browser: "+title);
            this.embeddedView = controller.getView();
            embeddedView.setZoomToScale(true);
            this.env = BrowserEnvironment.getInstance();
        }
        
        setJMenuBar(new BrowserMenuBar(theController.getBrowserModel()));
        
        JPanel toolbarPanel = new JPanel();
        toolbarPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        IconManager manager = env.getIconManager();
        
        ZoomButtonPanel panel = new ZoomButtonPanel(embeddedView);
        toolbarPanel.add(panel);

        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        
        final JScrollBar horizontalBar =
            new JScrollBar(JScrollBar.HORIZONTAL);
            
        final JScrollBar verticalBar =
            new JScrollBar(JScrollBar.VERTICAL);
        
        embeddedView.getViewCamera().addCameraListener(new CameraListener()
        {
            /* (non-Javadoc)
             * @see org.openmicroscopy.shoola.agents.browser.ui.CameraListener#cameraBoundsChanged(double, double, double, double, double, double)
             */
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
        container.add(fakeScrollPane,BorderLayout.CENTER);
        container.add(toolbarPanel,BorderLayout.NORTH);
        if(controller.getStatusView() != null)
        {
            container.add(controller.getStatusView(),BorderLayout.SOUTH);
        }
        this.addFocusListener(new CommonFocusAdapter(this));
        
        final UIWrapper refCopy = this;
        this.addInternalFrameListener(new InternalFrameAdapter()
        {
            /* (non-Javadoc)
             * @see javax.swing.event.InternalFrameAdapter#internalFrameClosing(javax.swing.event.InternalFrameEvent)
             */
            public void internalFrameClosing(InternalFrameEvent arg0)
            {
                env.getBrowserManager().removeBrowser(refCopy);
            }
        });

    }
    
    /**
     * Returns the embedded controller.
     * 
     * @see org.openmicroscopy.shoola.agents.browser.ui.UIWrapper#getController()
     */
    public BrowserController getController()
    {
        return controller;
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.ui.UIWrapper#select()
     */
    public void select()
    {
        try
        {
            setLayer(JDesktopPane.PALETTE_LAYER);
            setSelected(true);
        }
        catch(PropertyVetoException ex) {}
    }

    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.ui.UIWrapper#setBrowserTitle(java.lang.String)
     */
    public void setBrowserTitle(String title)
    {
        setTitle(title);
    }
}
