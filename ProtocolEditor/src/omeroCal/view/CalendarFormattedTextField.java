
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.JTextComponent;
import javax.swing.text.MaskFormatter;


public class CalendarFormattedTextField 
	extends JFormattedTextField {
	
	public static final Color BLUE_HIGHLIGHT = new Color(181,213,255);
	
	Font timeFont = new Font("SansSerif", Font.PLAIN, 10);
	
	Border selectedBorder;
	Border unSelectedBorder;
	
	public CalendarFormattedTextField() {
		super(createFormatter("##"));
		
		int padding = 2;
		Border blackBorder = new LineBorder(Color.black);
		Border whiteBorder = new LineBorder(Color.white);
		Border emptyBorder = new EmptyBorder(padding,padding,padding,padding);
		selectedBorder = BorderFactory.createCompoundBorder(
				   blackBorder, emptyBorder);
		unSelectedBorder = BorderFactory.createCompoundBorder(
				   whiteBorder, emptyBorder);
		
		Dimension fieldSize = new Dimension(19, 18);
		this.setPreferredSize(fieldSize);
		this.setMaximumSize(fieldSize);
		
		this.setFont(timeFont);
		this.setBorder(unSelectedBorder);
		this.setSelectionColor(BLUE_HIGHLIGHT);
		this.setSelectedTextColor(Color.black);
		
		this.addFocusListener(new TimeFocusListener());
	}
	
	public class TimeFocusListener implements FocusListener {

		public void focusGained(FocusEvent e) {
			JTextComponent source = (JTextComponent)e.getSource();
			source.setBorder(selectedBorder);
			source.setSelectionStart(0);
			source.setSelectionEnd(2);
		}

		public void focusLost(FocusEvent e) {
			JTextComponent source = (JTextComponent)e.getSource();
			source.setBorder(unSelectedBorder);
			source.setSelectionStart(0);
			source.setSelectionEnd(0);
		}
		
	}
	
	
	//A convenience method for creating a MaskFormatter.
    protected static MaskFormatter createFormatter(String s) {
        MaskFormatter formatter = null;
        try {
            formatter = new MaskFormatter(s);
        } catch (java.text.ParseException exc) {
            System.err.println("formatter is bad: " + exc.getMessage());
            System.exit(-1);
        }
        return formatter;
    }

}
