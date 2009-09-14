/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.TaskPaneBrowser
 *
 *------------------------------------------------------------------------------
 * Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.treeviewer.view;


//Java imports
import java.awt.Container;
import javax.swing.BorderFactory;
import javax.swing.JComponent;

//Third-party libraries
import org.jdesktop.swingx.JXTaskPane;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.util.ui.UIUtilities;


/**
 * Utility class.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class TaskPaneBrowser
	extends JXTaskPane
{

	/** The browser to host. */
	private Browser browser;
	
	/** Initializes the component. */
	private void initialize()
	{
		Container c = getContentPane();
		c.setBackground(UIUtilities.BACKGROUND_COLOR);
		if (c instanceof JComponent) 
			((JComponent) c).setBorder(BorderFactory.createEmptyBorder(
					1, 1, 1, 1));
		setBackground(UIUtilities.BACKGROUND_COLOR);
		setTitle(browser.getTitle());
		setIcon(browser.getIcon());
		setCollapsed(true);
		add(browser.getUI());
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param browser The browser to host.
	 */
	TaskPaneBrowser(Browser browser)
	{
		super();
		this.browser = browser;
		initialize();
	}
	
	/**
	 * Returns the browser.
	 * 
	 * @return See above.
	 */
	Browser getBrowser() { return browser; }
	
}
