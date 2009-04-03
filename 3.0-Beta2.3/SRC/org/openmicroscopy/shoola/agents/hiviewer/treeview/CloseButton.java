/*
 * org.openmicroscopy.shoola.agents.hiviewer.treeview.CloseButton
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

package org.openmicroscopy.shoola.agents.hiviewer.treeview;



//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;

/** 
 * The close button in the <code>ToolBar</code>.
 * This is a small MVC component that is aggregated into the bigger MVC set
 * of the {@link TreeView}. The MVC parts of this button are all collapsed
 * in this class.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$Date: )
 * </small>
 * @since OME2.2
 */
class CloseButton
	extends JButton
	implements ActionListener
{

    /** The Model this button is working with. */
    private TreeView model;
    
    /**
     * Creates a new instance.
     * 
     * @param model The Model this button is working with.
     *              Mustn't be <code>null</code>.
     */
    CloseButton(TreeView model)
    {
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        setRolloverEnabled(true);
        IconManager im = IconManager.getInstance();
        setIcon(im.getIcon(IconManager.CLOSE_VIEW));
        addActionListener(this);
    }
    
    /** 
     * Removes the component from the display depending on the current state of
     * the component
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e) { model.close(); }

}
