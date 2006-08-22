/*
 * org.openmicroscopy.shoola.util.ui.TableIconRenderer
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

package org.openmicroscopy.shoola.util.ui.table;



//Java imports
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
public class TableIconRenderer
	extends DefaultTableCellRenderer 
{
	public Component getTableCellRendererComponent(JTable table, Object value,
				   boolean isSelected, boolean hasFocus, int row, int column)
	{
   		/* Inherit the colors and font from the header component. */
		if (table != null) {
	   		JTableHeader header = table.getTableHeader();
	   		if (header != null) {
		   		setForeground(header.getForeground());
		   		setBackground(header.getBackground());
		   		setFont(header.getFont());
	   		}
   		}
	
		setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		setHorizontalAlignment(JLabel.CENTER);
        
		if (value instanceof TableHeaderTextAndIcon) {
			TableHeaderTextAndIcon v = (TableHeaderTextAndIcon) value;
	   		setText(v.getText());
	   		setToolTipText(UIUtilities.formatToolTipText(v.getToolTipTxt()));
	   		if (v.isAscending()) setIcon(v.getIconUp());
	   		else setIcon(v.getIconDown());
   		} else {
   		    return (JComponent) value;
        }
	   	return this;
   }

}

