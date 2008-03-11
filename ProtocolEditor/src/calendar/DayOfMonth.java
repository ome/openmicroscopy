
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

package calendar;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;


public class DayOfMonth extends JPanel {
	
	Box eventBox;
	
	public DayOfMonth(int dayOfMonth) {
		
		setLayout(new BorderLayout());
		setBorder(new BevelBorder(BevelBorder.LOWERED));
		
		Dimension daySize = new Dimension(100, 100);
		setMinimumSize(daySize);
		setPreferredSize(daySize);
		
		add(new JLabel(dayOfMonth + ""), BorderLayout.NORTH);
		
		eventBox = Box.createVerticalBox();
		add(eventBox, BorderLayout.CENTER);
	}
	
	public DayOfMonth() {
		
	}
	
	public void addEvent(CalendarEvent event) {
		JLabel eventLabel = new JLabel(event.getName());
		String time = CalendarDataBase.sqlDateTimeFormat.format(event.getTime());
		eventLabel.setToolTipText(time);
		eventBox.add(eventLabel);
	}

}
