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


//Java imports
import javax.swing.AbstractButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies

/** 
 * Implements the {@link TaskBar} interface to be a Null Object, that is to
 * do nothing.
 * So this implementation has no UI associated with it.
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

    /**
     * @see TaskBar#addToMenu(int, javax.swing.JMenuItem)
     */
    public void addToMenu(int menuID, JMenuItem entry) {}

    /**
     * @see TaskBar#removeFromMenu(int, javax.swing.JMenuItem)
     */
    public void removeFromMenu(int menuID, JMenuItem entry) {}

    /**
     * @see TaskBar#addToToolBar(int, javax.swing.AbstractButton)
     */
    public void addToToolBar(int toolBarID, AbstractButton entry) {}

    /**
     * @see TaskBar#removeFromToolBar(int, javax.swing.AbstractButton)
     */
    public void removeFromToolBar(int toolBarID, AbstractButton entry) {}

    /**
     * @see TaskBar#open()
     */
    public void open() {}

    /**
     * @see TaskBar#getFrame()
     */
    public JFrame getFrame() { return null; }

    /**
     * @see TaskBar#iconify()
     */
    public void iconify() {}

    public JMenu getMenu(int menuID)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public JToolBar getToolBar(int toolBarID)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
