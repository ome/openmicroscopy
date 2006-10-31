/*
 * org.openmicroscopy.shoola.util.ui.TipDialog 
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.ui;

//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;

//Third-party libraries

//Application-internal dependencies

/** 
 * Tip dialog is a simple dialog used to display tool tips. 
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
public class TipDialog 
	extends JDialog
{
	/** The string to be displayed in the dialog. */
	String tipString;
	
	/** Label displaying the tipString in the dialog. */
	JLabel label;
	
	/**
	 * Contructor of the tipDialog. 
	 * 
	 * @param tip
	 */
	public TipDialog(String tip)
	{
		tipString = tip;
		this.setAlwaysOnTop(true);
		this.setUndecorated(true);
		label = new JLabel();
		label.setBackground(new Color(249, 255, 151));
		label.setOpaque(true);
		label.setBorder(BorderFactory.createLineBorder(Color.black));
		this.setLayout(new BorderLayout());
		this.add(label,BorderLayout.CENTER);
		this.setSize(40,13);
	}
	
	/**
	 * Overrriden, from {@link JDialog#paintComponents(Graphics)}
	 * Draw all the components in the dialog. 
	 */
	public void paint(Graphics g)
	{
		super.paintComponents(g);
	}
	
	/**
	 * Set the string to be displayed in the dialog.
	 * 
	 * @param tip see above.
	 */
	public void setTipString(String tip)
	{
		label.setText(tip);
		invalidate();
		repaint();
	}
}


