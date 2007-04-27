/*
 * measurement.ui.objectinspector.ColourRenderer 
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
package org.openmicroscopy.shoola.util.ui.measurement.ui.objectinspector;


//Java imports
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public 	class 		ColourRenderer 
		extends 	JLabel
		implements 	TableCellRenderer 
{
	
    public ColourRenderer() 
    {
        setOpaque(true); //MUST do this for background to show up
    }

    public Component getTableCellRendererComponent(
                            JTable table, Object object,
                            boolean isSelected, boolean hasFocus,
                            int row, int column) 
    {
    	if(object instanceof Integer)
    	{
    		this.setOpaque(true);
    		this.setText((Integer)object+"");
    	}
    	if(object instanceof Long)
    	{
    		this.setOpaque(true);
    		this.setText((Long)object+"");
    	}
    	if(object instanceof Double)
    	{
    		this.setOpaque(true);
    		this.setText((Double)object+"");
    	}
    	if(object instanceof String)
    	{
    		this.setOpaque(true);
    		this.setText((String)object);
    	}
    	if(object instanceof Color)
    	{
    		this.setOpaque(true);
    		this.setBorder(BorderFactory.createLineBorder(Color.darkGray));
    		this.setBackground((Color)object);
    	}
    	return this;
    }
    
}
