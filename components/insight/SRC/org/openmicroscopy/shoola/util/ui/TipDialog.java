/*
 * org.openmicroscopy.shoola.util.ui.TipDialog 
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
package org.openmicroscopy.shoola.util.ui;

//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
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
	
    /** The color of the tooltip. */
    private static final Color TIP_COLOR = new Color(249, 255, 151);
    
	/** Label displaying the tipString in the dialog. */
	private JLabel label;
	
	/**
	 * Creates a new instance. 
	 * 
	 * @param tip The string to be displayed in the dialog.
	 */
	public TipDialog(String tip)
	{
		this.setAlwaysOnTop(true);
		this.setUndecorated(true);
		label = new JLabel(tip);
		label.setBackground(TIP_COLOR);
		label.setOpaque(true);
		label.setBorder(BorderFactory.createLineBorder(Color.black));
		this.setLayout(new BorderLayout());
		getContentPane().add(label, BorderLayout.CENTER);
	}
    
	/**
	 * Sets the string to be displayed in the dialog.
	 * 
	 * @param tip see above.
	 */
	public void setTipString(String tip)
	{
		label.setText(tip);
		invalidate();
        FontMetrics fm = getFontMetrics(label.getFont());
        setSize(fm.stringWidth(tip)+4, fm.getHeight()+4);
		repaint();
	}
    
}
