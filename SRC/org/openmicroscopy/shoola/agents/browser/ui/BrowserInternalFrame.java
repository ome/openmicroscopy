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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.openmicroscopy.shoola.agents.browser.BrowserController;
import org.openmicroscopy.shoola.agents.browser.BrowserEnvironment;
import org.openmicroscopy.shoola.agents.browser.IconManager;
import org.openmicroscopy.shoola.agents.browser.UIConstants;

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
            this.env = BrowserEnvironment.getInstance();
        }
        
        JPanel toolbarPanel = new JPanel();
        toolbarPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        IconManager manager = env.getIconManager();
        JButton zoomButton = new JButton(manager.getSmallIcon(IconManager.ZOOM_BAR));
        zoomButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                controller.getOverlayModel().showPalette(UIConstants.ZOOM_PALETTE_NAME);
            }
        });
        zoomButton.setToolTipText("Show Zoom Menu");
        
        JButton optionsButton =
            new JButton(manager.getSmallIcon(IconManager.OPTIONS_BAR));
        optionsButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                controller.getOverlayModel().showPalette(UIConstants.OPTIONS_PALETTE_NAME);
            }
        });
        optionsButton.setToolTipText("Show Options Menu");
        
        toolbarPanel.add(zoomButton);
        toolbarPanel.add(optionsButton);
        
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        
        JScrollPane scrollPane = new JScrollPane();
        JScrollBar horizontalBar = new JScrollBar();
        
        
        container.add(embeddedView,BorderLayout.CENTER);
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
