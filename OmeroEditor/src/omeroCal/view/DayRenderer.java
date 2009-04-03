 /*
 * omeroCal.view.DayRenderer 
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

import java.util.Calendar;

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
public class DayRenderer {
	
	public static final int DAY_ICON = 0;
	
	public static final int DAY_PANEL = 1;
	
	int dayToDisplay;
	
	public DayRenderer(int dayToDisplay) {
		
		this.dayToDisplay = dayToDisplay;
	}
	

	public IDayDisplay getDayComponent(Calendar dayOfMonth) {
		
		switch (dayToDisplay) {
		
		case DAY_ICON: {
			
			return new DayIcon(dayOfMonth);
		}
		case DAY_PANEL: {
			
			return new DayOfMonth(dayOfMonth);
		}
		
		}
		
		/*
		 * If others fail, return this as default
		 */
		return new DayOfMonth(dayOfMonth);
	}
}
