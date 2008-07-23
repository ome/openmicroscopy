 /*
 * fields.DateTimeValueObject 
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
package fields;

import tree.DataFieldConstants;

//Java imports

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
public class DateTimeParam extends AbstractParam {

	public static final String DATE_ATTRIBUTE = DataFieldConstants.UTC_MILLISECS;
	
	public static final String TIME_ATTRIBUTE = DataFieldConstants.SECONDS;
	
	public static final String ALARM_SECONDS = DataFieldConstants.ALARM_SECONDS;
	
	/**
	 * Creates an instance. 
	 * 
	 * @param fieldType		The String defining the field type
	 */
	public DateTimeParam(String fieldType) {
		super(fieldType);
	}
	
	
	/**
	 * The value attribute is a single value
	 */
	public String[] getValueAttributes() {
		
		return new String[] {DATE_ATTRIBUTE, 
				TIME_ATTRIBUTE, ALARM_SECONDS};
	}

	/**
	 * This field is filled if the DATE value isn't null, and 
	 * is not an empty string. 
	 */
	public boolean isParamFilled() {
		String dateValue = getAttribute(DATE_ATTRIBUTE);
		
		return (dateValue != null && dateValue.length() > 0);
	}

}