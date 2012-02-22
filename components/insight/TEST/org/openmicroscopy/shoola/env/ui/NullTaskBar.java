/*
 * org.openmicroscopy.shoola.env.ui.NullTaskBar
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

package org.openmicroscopy.shoola.env.ui;


//Java imports
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

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
     * @see TaskBar#getFrame()
     */
    public JFrame getFrame() { return null; }

    /**
     * @see TaskBar#getTaskBarMenuBar()
     */
    public JMenuBar getTaskBarMenuBar() { return null; }

    /**
     * @see TaskBar#getCopyMenuItem(int)
     */
	public JMenuItem getCopyMenuItem(int index) { return null; }

	/**
	 * @see TaskBar#login()
	 */
	public boolean login() { return false; }

	/**
	 * @see TaskBar#openURL(String)
	 */
	public void openURL(String path) {}

	/**
	 * @see TaskBar#sessionExpired(int)
	 */
	public void sessionExpired(int index) {}

	/**
	 * @see TaskBar#addToToolBar(int, JComponent)
	 */
	public void addToToolBar(int toolBarID, JComponent entry) {}

	/**
	 * @see TaskBar#getToolBarEntries(int)
	 */
	public List<JComponent> getToolBarEntries(int toolBarID) { return null; }

	/**
	 * @see TaskBar#getToolBarEntries(int)
	 */
	public JMenu getMenu(int menuID) { return null; }

}
