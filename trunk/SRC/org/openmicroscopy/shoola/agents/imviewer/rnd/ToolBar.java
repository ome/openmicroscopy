/*
 * org.openmicroscopy.shoola.agents.imviewer.rnd.ToolBar
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
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies

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
class ToolBar
    extends JToolBar
{

    /** Reference to the Control. */
    private RendererControl controller;
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        setFloatable(false);
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 5));
        putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        add(new JButton(
                controller.getAction(RendererControl.RESET_SETTINGS)));
        //add(new JButton(
         //       controller.getAction(RendererControl.SAVE_SETTINGS)));
    }
    
    /**
     * Creates a new instance.
     * 
     * @param controller    Reference to the controller.
     *                      Mustn't <code>null</code>.
     */
    ToolBar(RendererControl controller)
    {
        if (controller ==  null) throw new NullPointerException("No control.");
        this.controller = controller;
        buildGUI();
    }
    
}
