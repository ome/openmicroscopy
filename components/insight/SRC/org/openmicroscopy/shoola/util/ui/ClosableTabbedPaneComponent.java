/*
 * org.openmicroscopy.shoola.util.ui.ClosableTabbedPaneComponent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui;


//Java imports
import javax.swing.Icon;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

/** 
 * A component usually added to a <code>ClosabeTabbedPane</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ClosableTabbedPaneComponent 	
	extends JPanel
{

	/** The index of the tab pane. */
	private final int 	index;
	
	/** The name of the tab component. */
	private String		name;
	
	/** The description of the tab component. */
	private String		description;
	
	/** The name of the tab component. */
	private Icon		icon;
	
	/** Flag indicating if the component can be removed. */
	private boolean		closable;
	
	/** Flag indicating to show the close icon. */
	private boolean		closeVisible;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param index The index of the component.
	 */
	public ClosableTabbedPaneComponent(int index)
	{
		this(index, "", null, "");
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param index	The index of the component.
	 * @param name	The name of the tab component.
	 */
	public ClosableTabbedPaneComponent(int index, String name)
	{
		this(index, name, null, "");
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param index	The index of the component.
	 * @param name	The name of the tab component.
	 * @param description	The name of the tab component.
	 */
	public ClosableTabbedPaneComponent(int index, String name, String 
			description)
	{
		this(index, name, null, description);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param index	The index of the component.
	 * @param name	The name of the tab component.
	 * @param icon	The icon related to the tab component.
	 */
	public ClosableTabbedPaneComponent(int index, String name, Icon icon)
	{
		this(index, name, icon, "");
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param index			The index of the component.
	 * @param name			The name of the tab component.
	 * @param icon			The icon related to the tab component.
	 * @param description	The name of the tab component.
	 */
	public ClosableTabbedPaneComponent(int index, String name, Icon icon, 
										String description)
	{
		this.index = index;
		this.name = name;
		this.icon = icon;
		this.description = description;
		closable = true;
		closeVisible = true;
	}
	
	/**
	 * Sets to <code>true</code> if the icon is shown, <code>false</code>
	 * otherwise.
	 * 
	 * @param closeVisible Pass <code>true</code> if the icon is shown,
	 * 						<code>false</code> otherwise.
	 */
	public void setCloseVisible(boolean closeVisible)
	{
		this.closeVisible = closeVisible;
	}
	
	/**
	 * Returns <code>true</code> if the icon is shown, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isCloseVisible() { return closeVisible; }
	
	
	/**
	 * Sets to <code>true</code> if the component can be closed,
	 * <code>false</code>
	 * 
	 * @param closable Pass <code>true</code> to allow to close the component,
	 * 				  <code>false</code> otherwise.
	 */
	public void setClosable(boolean closable) { this.closable = closable; }
	
	/**
	 * Returns <code>true</code> if the component can be closed, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isClosable() { return closable; }
	
	/**
	 * Returns the index of the tab component.
	 * 
	 * @return See above.
	 */
	public int getIndex() { return index; }

	/**
	 * Returns the index of the tab component.
	 * 
	 * @return See above.
	 */
	public String getDescription() { return description; }

	/**
	 * Returns the icon of the tab component.
	 * 
	 * @return See above.
	 */
	public Icon getIcon() { return icon; }

	/**
	 * Returns the name of the tab component.
	 * 
	 * @return See above.
	 */
	public String getName() { return name; }
	
}
