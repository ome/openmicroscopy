/*
 * org.openmicroscopy.shoola.env.ui.NullTaskBar
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

package org.openmicroscopy.shoola.env.ui;

import javax.swing.AbstractButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class NullTaskBar
        implements TaskBar
{

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.ui.TaskBar#addToMenu(int, javax.swing.JMenuItem)
     */
    public void addToMenu(int menuID, JMenuItem entry)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.ui.TaskBar#removeFromMenu(int, javax.swing.JMenuItem)
     */
    public void removeFromMenu(int menuID, JMenuItem entry)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.ui.TaskBar#addToToolBar(int, javax.swing.AbstractButton)
     */
    public void addToToolBar(int toolBarID, AbstractButton entry)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.ui.TaskBar#removeFromToolBar(int, javax.swing.AbstractButton)
     */
    public void removeFromToolBar(int toolBarID, AbstractButton entry)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.ui.TaskBar#open()
     */
    public void open()
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.ui.TaskBar#getFrame()
     */
    public JFrame getFrame()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
