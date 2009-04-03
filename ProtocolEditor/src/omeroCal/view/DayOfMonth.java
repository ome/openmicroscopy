
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import omeroCal.model.CalendarEvent;




public class DayOfMonth extends JPanel {
	
	Box eventBox;
	
	JLabel dayLabel;
	
	Color grey = new Color(200, 200, 200);
	
	/**
	 * A highlight color to indicate TODAY, if the current month is displayed
	 */
	protected static Color todayBackground = new Color(255, 225, 225);
	
	public DayOfMonth(int dayOfMonth) {
		
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createMatteBorder(1,0,0,1, grey));
		
		Dimension daySize = new Dimension(115, 105);
		setMinimumSize(daySize);
		setPreferredSize(daySize);
		
		this.setBackground(Color.WHITE);
		
		dayLabel = new JLabel(dayOfMonth + "");
		add(dayLabel , BorderLayout.NORTH);
		
		eventBox = Box.createVerticalBox();
		add(eventBox, BorderLayout.CENTER);
	}
	
	public DayOfMonth() {
		
	}
	
	public void addEventLabel(EventLabel event) {

		eventBox.add(event);
	}
	
	public void setDayFromOtherMonth(boolean dayFromOtherMonth) {
		dayLabel.setForeground(dayFromOtherMonth ? grey : Color.black);
		setBackground(dayFromOtherMonth ? new Color(247,247,247) : Color.white);
	}
	
	public void setToday(boolean today) {
		setBackground(today ? todayBackground : Color.white);
	}

}
