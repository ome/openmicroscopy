 /*
 * org.openmicroscopy.shoola.agents.editor.model.params.DateTimeParam 
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
package org.openmicroscopy.shoola.agents.editor.model.params;

//Java imports

import java.text.SimpleDateFormat;
import java.util.Date;

//Third-party libraries

//Application-internal dependencies

/** 
 * An experimental parameter that represents a Date (with optional Time).
 * Also an optional Alarm time can be set (in seconds before 
 * (negative value) or after the Date/Time).
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class DateTimeParam 
	extends AbstractParam 
{

	/**
	 * A string to define the DateTimeParam type. 
	 */
	public static final String 		DATE_TIME_PARAM = "DATE_TIME";
	
	/**
	 * Creates an instance. 
	  */
	public DateTimeParam() {
		super(DATE_TIME_PARAM);
	}
	
	/**
	 * Returns the absolute date as a String in the format YYYYMMDD
	 * If relative date, or no date set, returns ""
	 * 
	 * @return		see above
	 */
	public String getYYYYMMDD() 
	{
			
		String dateMillis = getAttribute(TextParam.PARAM_VALUE);
		if (dateMillis != null) 
		{
			long millis = new Long(dateMillis);
			Date date = new Date();
			date.setTime(millis);
			SimpleDateFormat dateF = new SimpleDateFormat("yyyyMMdd");
			return dateF.format(date);
		} else {
			return "";
		}
	}
	
	/**
	 * Returns a formatted string displaying the date (or relative date)
	 * and time (if set). 
	 * 
	 * @see Object#toString()
	 */
	public String toString() 
	{	
		String dateMillis = getParamValue();
		
		return formatDate(dateMillis);
	}
	
	/**
	 * Formats the date in millisecs to "yyyy, MMM d ' at ' HH:mm"
	 * @param dateMillis
	 * @return
	 */
	public static String formatDate(String dateMillis)
	{
		if (dateMillis == null) 	return "no date";
		
		try {
			long millis = new Long(dateMillis);
			Date date = new Date();
			date.setTime(millis);
			SimpleDateFormat dateF = new SimpleDateFormat(
				"yyyy, MMM d ' at ' HH:mm");
			return dateF.format(date);
		} catch (NumberFormatException ex) {
			return dateMillis;
		}
		
	}

}