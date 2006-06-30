/*
 * org.openmicroscopy.shoola.agents.imviewer.rnd.RendererUI
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

package org.openmicroscopy.shoola.agents.imviewer.rnd;


//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

//Third-party libraries

//Application-internal dependencies


import org.openmicroscopy.shoola.env.ui.TopWindow;


/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class RendererUI
    extends TopWindow
{
    
    /** Reference to the control. */
    private RendererControl     controller;
    
    /** Reference to the model. */
    private RendererModel       model;

    /** The tool bar composing the display. */
    private ToolBar             toolBar;
    
    /**
     * Creates the menu bar.
     * 
     * @return See above
     */
    private JMenuBar createMenuBar()
    {
        JMenuBar menuBar = new JMenuBar(); 
        menuBar.add(createControlsMenu());
        return menuBar;
    }
    
    /**
     * Helper method to create the <code>Controls</code> menu.
     * 
     * @return See above.
     */
    private JMenu createControlsMenu()
    {
        JMenu menu = new JMenu("Controls");
        return menu;
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        Container c = getContentPane();
        c.setLayout(new BorderLayout(0, 0));
        c.add(toolBar, BorderLayout.NORTH);
        pack();
    }
    
    /**
     * Creates a new instance. The method 
     * {@link #initialize(RendererControl, RendererModel) initialize}
     * should be called straight after.
     * 
     * @param title The name of the image.
     */
    RendererUI(String title)
    {
        super("Renderer:  "+title);
    }
    
    /**
     * Links the MVC triad.
     * 
     * @param controller    Reference to the Control.
     *                      Mustn't be <code>null</code>.
     * @param model         Reference to the Model.
     *                      Mustn't be <code>null</code>.
     */
    void initialize(RendererControl controller, RendererModel model)
    {
        if (controller == null) throw new NullPointerException("No control.");
        if (model == null) throw new NullPointerException("No model.");
        this.controller = controller;
        this.model = model;
        setJMenuBar(createMenuBar());
        toolBar = new ToolBar(controller);
        buildGUI();
    }

    void onStateChange(boolean b)
    {
        // TODO Auto-generated method stub
        
    }
    
}
