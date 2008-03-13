
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

package omeroCal;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;



public class EventInfoPanel 
	extends JPanel 
	implements Observer {

	public EventInfoPanel(CalendarEvent calendarEvent) {
		
		if (calendarEvent instanceof Observable) {
			((Observable)calendarEvent).addObserver(this);
		}
		
		Box verticalBox = Box.createVerticalBox();
		
		// title
		String title = calendarEvent.getName();
		JLabel titleLabel = new CalendarLabel(title, 15);
		titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		verticalBox.add(titleLabel);
		
		// separator
		verticalBox.add(Box.createVerticalStrut(10));
		verticalBox.add(new JSeparator(JSeparator.HORIZONTAL));
		verticalBox.add(Box.createVerticalStrut(10));
		
		// display the date in a date-picker
		DatePicker datePicker = new DatePicker();
		datePicker.setDate(calendarEvent.getStartTime());
		datePicker.setEnabled(false);
		verticalBox.add(datePicker);
		
		
		verticalBox.setBorder(new EmptyBorder(10, 10, 10, 10));
		this.add(verticalBox);
		this.setBackground(Color.WHITE);
		this.setPreferredSize(new Dimension(300, 500));
	}

	public void update(Observable o, Object arg) {
		
	}
	
public static void main(String[] args) {
		
		JFrame frame = new JFrame("MonthView");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		CalendarEvent ce = new CalendarEvent("Title Here", new Date());
		frame.getContentPane().add(new EventInfoPanel(ce));
		
		frame.pack();
		frame.setVisible(true);
	}
	
}
