
/*
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package omeroCal.view;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JComboBox;

public class CalendarComboBox extends JComboBox {
	

	Font timeFont = new Font("SansSerif", Font.PLAIN, 10);
	
	private int width;
	private int height;
	
	private int maximumWidth =0;
	
	public CalendarComboBox(String[] items) {
		super(items);
		customiseLookAndFeel();
	}
	public CalendarComboBox() {
		super();
		customiseLookAndFeel();
	}
	
	
	public void customiseLookAndFeel() {
		setBackground(null);
		this.setFont(timeFont);
		
		//JComponent editor = (JComponent)this.getEditor();
		//editor.setBorder(new LineBorder(Color.black));
	}
	
	public void setMaximumWidth(int maxWidth) {
		maximumWidth = maxWidth;
		int h = (int)super.getPreferredSize().getHeight();
		
		setMaximumSize(new Dimension(maximumWidth, h));
	}

	public Dimension getPreferredSize() {
		
		Dimension size = super.getPreferredSize();

		if (maximumWidth == 0) {
			width = (int)size.getWidth() + 20;
		} else {
			width = maximumWidth;
		}
		height = (int)size.getHeight();
		
		// setMaximumSize(new Dimension(300, height));
		
		return new Dimension(width, height);
	}

}
