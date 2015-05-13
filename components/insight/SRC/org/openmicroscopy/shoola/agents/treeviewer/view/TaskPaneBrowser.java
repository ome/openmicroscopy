/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.TaskPaneBrowser
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
import java.awt.Container;
import javax.swing.BorderFactory;
import javax.swing.JComponent;

//Third-party libraries
import org.jdesktop.swingx.JXTaskPane;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.actions.SearchAction;
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
	private Object browser;
	
	/** Initializes the component. */
	private void initialize()
	{
		setAnimated(false);
		setSpecial(false);
		setCollapsed(true);
		
		Container container = getContentPane();
		if (container instanceof JComponent) 
			((JComponent) container).setBorder(BorderFactory.createEmptyBorder(
					1, 1, 1, 1));
		setBackground(UIUtilities.BACKGROUND_COLOR);
		container.setLayout(new BorderLayout());
		if (browser instanceof Browser) {
			Browser b = (Browser) browser;
			setTitle(b.getTitle());
			setIcon(b.getIcon());
			container.add(b.getUI(), BorderLayout.CENTER);
		} else {
			setTitle(SearchAction.NAME);
			IconManager icons = IconManager.getInstance();
			setIcon(icons.getIcon(IconManager.SEARCH));
			container.add((JComponent) browser, BorderLayout.CENTER);
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param browser The browser to host.
	 * @param name What kind of entity this task pane browses.
	 */
	TaskPaneBrowser(Object browser, String name)
	{
		super();
		this.browser = browser;
		initialize();
		setName(name + " browser");
	}
	
	/**
	 * Returns the browser.
	 * 
	 * @return See above.
	 */
	Browser getBrowser()
	{ 
		if (browser instanceof Browser) 
		    return (Browser) browser;
		return null; 
	}
	
}
