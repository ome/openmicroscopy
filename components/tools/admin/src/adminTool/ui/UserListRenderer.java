/*
 * adminTool.ListRenderer 
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package src.adminTool.ui;

//Java imports
import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import src.adminTool.model.Model;

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
public class UserListRenderer 
		extends JLabel  
		implements ListCellRenderer  
{
		
		private Model 	model;
		/**
		 * Border of the cell.
		 */
		private Border emptyBorder = 
									BorderFactory.createEmptyBorder(2, 2, 2, 2);
			
		/**
		 * Constructor for the ColourListRenderer(sets the backgroud ot opaque.
		 */
		public UserListRenderer(Model model)
		{
			this.model = model;
			setOpaque(true);
		}
		
		/** Overridden method
		 * @see javax.swing.ListCellRenderer#getListCellRendererComponent
		 * (javax.swing.JList, java.lang.Object, int, boolean, boolean)
		 */
		public Component getListCellRendererComponent(JList list, Object value, 
				int index, boolean isSelected, boolean hasFocus) 
		{
			this.setVerticalAlignment(SwingConstants.CENTER);
			setText((String) value);
			this.setHorizontalAlignment(SwingConstants.LEFT);
			if(isSelected)
			{
				try {
					if(model.isSystemUser((String)value))
					{
						setForeground(Color.red);
						setBackground(list.getSelectionBackground());
					}
					else
					{
						setForeground(list.getSelectionForeground());
						setBackground(list.getSelectionBackground());
					}
				} catch (Exception e){}
			}
			else
			{
				try {
					if(model.isSystemUser((String)value))
						setForeground(Color.red);
					else
						setForeground(list.getForeground());
				} catch (Exception e) {	}

				if(index % 2 == 0 )
					setBackground(Color.white);
				else
					setBackground(new Color(236, 243, 254));
			}
			
			setBorder(emptyBorder);
			
			return this;
		}
}


