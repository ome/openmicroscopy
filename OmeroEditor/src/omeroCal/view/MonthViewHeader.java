 /*
 * omeroCal.view.MonthViewHeader 
 *
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
 */
package omeroCal.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import omeroCal.util.ImageFactory;

import ui.components.AlignedComponent;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class MonthViewHeader 
	extends JPanel { 
	
	JLabel monthYearLabel;
	
	JButton prevMonthButton;
	
	JButton nextMonthButton;
	
	SimpleDateFormat monthYearFormat;
	
	public static final String[] D_ARRAY =  {"M", "T","W","T","F","S","S"};
	
	public static final String[] DAYS_ARRAY = {"Monday","Tuesday","Wednesday",
		"Thursday","Friday","Saturday","Sunday"};
	
	public MonthViewHeader() {
		this(DAYS_ARRAY, 13);
	}
	
	public MonthViewHeader(String[] dayNames, int fontSize) {
		
		super(new BorderLayout());
		
		monthYearFormat = new SimpleDateFormat("MMMMMM yyyy");
		
		JPanel daysOfWeekHeader = new JPanel(new GridLayout(0, 7));
		
		for (int i=0; i<dayNames.length; i++) {
			daysOfWeekHeader.add(new AlignedComponent(
					new CalendarLabel(dayNames[i], fontSize)));
		}
		
		Box titleButtonsBox = Box.createHorizontalBox();
		
		ImageFactory imF = ImageFactory.getInstance();
		prevMonthButton = new JButton(imF.getIcon(ImageFactory.PREV_ICON));
		prevMonthButton.setActionCommand(MonthView.PREV_MONTH_CMD);
		prevMonthButton.setBorder(null);
		
		nextMonthButton = new JButton(imF.getIcon(ImageFactory.NEXT_ICON));
		nextMonthButton.setActionCommand(MonthView.NEXT_MONTH_CMD);
		nextMonthButton.setBorder(null);
		
		monthYearLabel = new CalendarLabel("", fontSize);
		
		titleButtonsBox.add(prevMonthButton);
		titleButtonsBox.add(Box.createHorizontalStrut(10));
		titleButtonsBox.add(monthYearLabel);
		titleButtonsBox.add(Box.createHorizontalStrut(10));
		titleButtonsBox.add(nextMonthButton);
		
		this.add(new AlignedComponent(titleButtonsBox, 5), BorderLayout.NORTH);
		this.add(daysOfWeekHeader, BorderLayout.CENTER);
		
	}
	
	public void addActionListener(ActionListener al) {
		prevMonthButton.addActionListener(al);
		nextMonthButton.addActionListener(al);
	}
	
	public void refreshHeader(Calendar currentDate) {
		String monthYear = monthYearFormat.format(currentDate.getTime());
		monthYearLabel.setText(monthYear);
	}

}
