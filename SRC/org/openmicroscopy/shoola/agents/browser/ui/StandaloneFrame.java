/*
 * org.openmicroscopy.shoola.agents.browser.ui.StandaloneFrame
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

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.JFrame;

import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * Parent class for all standalone (JFrame) UI classes.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2.1
 * @since OME2.2.1
 */
public abstract class StandaloneFrame extends JFrame
                                      implements UIWrapper
{
    /**
     * Parent constructor for the standalone frame.  Initializes the
     * event listeners and abides by the same construction rules
     * as the JFrame superclass.  Also sets the default closing
     * operation to JFrame.DISPOSE_ON_CLOSE.
     */
    public StandaloneFrame()
    {
        super();
        init();
        initEvents();
    }
    
    /**
     * Parent constructor for the standalone frame, with the title
     * of the window set at the time of construction.  Initializes the
     * event listeners and abides by the same construction rules
     * as the JFrame superclass.  Also sets the default closing
     * operation to JFrame.DISPOSE_ON_CLOSE.
     * 
     * @param title The text to display in the title bar.
     */
    public StandaloneFrame(String title)
    {
        super(title);
        init();
        initEvents();
    }
    
    /**
     * Returns the JFC/Swing component.
     * 
     * @see org.openmicroscopy.shoola.agents.browser.ui.UIWrapper#getRealUI()
     */
    public Component getRealUI()
    {
        return this;
    }

    /**
     * Default initialization procedure.  Sets the closing
     * window action to JFrame.DISPOSE_ON_CLOSE.
     */
    protected void init()
    {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    // event listening initialization.  All functions are
    // passed down to subclasses.
    private void initEvents()
    {
        addWindowFocusListener(new WindowFocusListener()
        {
            public void windowGainedFocus(WindowEvent arg0)
            {
                wrapperSelected();
            }
            
            // do nothing
            public void windowLostFocus(WindowEvent arg0) {}
        });
        
        addWindowListener(new WindowAdapter()
        {
            public void windowOpened(WindowEvent arg0)
            {
                super.windowOpened(arg0);
                wrapperOpened();
            }
            
            public void windowClosing(WindowEvent arg0)
            {
                super.windowClosing(arg0);
                wrapperClosed();
            }
        });
    }
    
    /**
     * Opens and centers the window.
     */
    public void open()
    {
        pack();
        UIUtilities.centerAndShow(this);
    }
}
