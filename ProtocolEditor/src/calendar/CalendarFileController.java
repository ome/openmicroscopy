
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

import java.util.Observable;
import java.util.Observer;

import omeroCal.model.CalendarEvent;
import omeroCal.model.ICalendarModel;
import omeroCal.view.Controller;


/**
 * This class "links" events from the same CalendarFile.
 * It observes all events that are visible, and is notified when one becomes selected.
 * This class then notifies all other visible events (including the calendarFile_ID),
 * so that those events that belong to the same calendarFile can become highlighted.
 * 
 * @author will
 *
 */
public class CalendarFileController 
	extends Controller {
	
	public CalendarFileController(ICalendarModel monthModel) {
		
		super(monthModel);
	}

}
