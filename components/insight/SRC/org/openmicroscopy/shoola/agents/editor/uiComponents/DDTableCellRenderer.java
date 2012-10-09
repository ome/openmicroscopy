 /*
 * org.openmicroscopy.shoola.agents.editor.uiComponents.DDTableCellRenderer 
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
package org.openmicroscopy.shoola.agents.editor.uiComponents;

//Java imports
import javax.swing.Icon;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.editor.IconManager;

/** 
 * This renders a Table cell to look like it is a drop-down menu. 
 * Simply extends the {@link DefaultTableCellRenderer} to add a
 * drop-down menu icon. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class DDTableCellRenderer 
	extends DefaultTableCellRenderer {

	private Icon 			ddIcon;
	
	public DDTableCellRenderer() {
		
		IconManager iM = IconManager.getInstance();
		ddIcon = iM.getIcon(IconManager.UP_DOWN_9_12);
		
		setIcon(ddIcon);
		setHorizontalTextPosition(SwingConstants.LEFT);
		setIconTextGap(2);
		setHorizontalAlignment(SwingConstants.RIGHT);
	}
}
