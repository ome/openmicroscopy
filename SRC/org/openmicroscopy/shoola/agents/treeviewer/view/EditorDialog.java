/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.EditorDialog
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.treeviewer.view;



//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

//Third-party libraries

//Application-internal dependencies

/** 
 * Basic modal dialog brought up when the use wants to create a new 
 * container.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class EditorDialog
    extends JDialog
{

    /** The default size of the dialog. */
    private static final Dimension WIN_DIM = new Dimension(600, 350);
    
    /**
     * Creates a new instance.
     * 
     * @param owner The owner of the frame.
     */
    EditorDialog(JFrame owner)
    {
        super(owner);
        setModal(true);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }
    
    /** 
     * Adds the specified component.
     * 
     * @param c The component to add.
     */
    void addComponent(JComponent c)
    {
        getContentPane().add(c, BorderLayout.NORTH);
        setSize(WIN_DIM);
    }
    
    /** Closes and disposes. */
    void close()
    {
        setVisible(false);
        dispose();
    } 
    
}
