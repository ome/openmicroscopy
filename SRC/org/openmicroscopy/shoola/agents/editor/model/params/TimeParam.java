 /*
 * treeModel.fields.TimeParam 
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

import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.HashMap;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * The Parameter data object for a Time value. 
 * Stores the value of time in seconds in the SECONDS attribute.
 * This is the only "Value attribute".
 * It is possible to add a timer object, which will be done by the UI.
 * This is so that the timer can continue to run even while this time
 * parameter is rendered by lots of different (new) panels while the user
 * is browsing the JTree.  
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class TimeParam 
	extends AbstractParam {
	
	/**
	 * This defines a time parameter, in seconds. 
	 * Equivalent to the "TimeField" of Beta 3.0
	 */
	public static final String TIME_PARAM = "TIME";
	
	/**
	 * This is the attribute Name for the value of this field. 
	 */
	public static final String SECONDS = "seconds";
	
	/**
	 * A reference to a timer, which can be added to this class. 
	 */
	private Object timer;
	
	/**
	 * Creates an instance. 
	 * 
	 * @param fieldType		The String defining the field type
	 */
	public TimeParam() {
		super(TIME_PARAM);
	}
	
	/**
	 * Sets a timer object, so that a timer can be associated with this
	 * data object, independently of the UI. 
	 * 
	 * @param timer		The timer object.
	 */
	public void setTimer(Object timer) {
		this.timer = timer;
	}
	
	/**
	 * A way for the UI to get a timer (if any). 
	 * 
	 * @return	The timer object associated with this parameter. 
	 */
	public Object getTimer() {
		return timer;
	}

	@Override
	/**
	 * Returns the single attribute: SECONDS
	 */
	public String[] getParamAttributes() {
		return new String[] {SECONDS};
	}

	@Override
	/**
	 * Returns a single attribute name that identifies the default value
	 */
	public String[] getDefaultAttributes() {
		return new String [] {TextParam.DEFAULT_VALUE};
	}

	@Override
	/**
	 * This field is filled if the value isn't null, and 
	 * is not an empty string. 
	 */
	public boolean isParamFilled() {
		String textValue = getAttribute(SECONDS);
		
		return (textValue != null && textValue.length() > 0);
	}
	
	/**
	 * Returns the Hrs:Mins:Secs. 
	 */
	public String toString()
	{
		String time = getParamValue();
		
		if (time == null) {
			time = super.toString();
		}
		
		return  time;
	}
	
	/**
	 * Implemented as specified by the {@link IParam} interface.
	 * 
	 *  @see IParam#getParamValue()
	 */
	public String getParamValue() 
	{
		String seconds = getAttribute(SECONDS);
		if (seconds != null)
		{
			int secs = Integer.parseInt(seconds);
			
			return secsToString(secs);
		} 
		return null;
	}
	
	/**
	 * Returns a string representation of the seconds, in the form
	 * HH:MM:SS
	 * 
	 * @param secs	number of seconds. 
	 * @return	see above. 
	 */
	public static String secsToString(int secs) 
	{
		int hrs = secs/ 3600;
		secs = secs - (hrs * 3600);
		int mins = secs / 60;
		secs = secs - (mins * 60);
		
		String time = (hrs < 10 ? "0"+hrs : hrs)  + ":" +
		(mins < 10 ? "0"+mins : mins)  + ":" +
		(secs < 10 ? "0"+secs : secs);
		
		return time;
	}
	
	/**
	 * Implemented as specified by {@link IParam#loadDefaultValues()}
	 * 
	 * Copies the value of the {@link #SingleParam.DEFAULT_VALUE} (if not null)
	 * to the value of the {@link #SECONDS} attribute. 
	 * 
	 * @see IParam#loadDefaultValues()
	 */
	public HashMap<String, String> loadDefaultValues() 
	{	
		HashMap<String,String> oldValues = new HashMap<String, String>();
		
		String defValue = getAttribute(TextParam.DEFAULT_VALUE);
		if (defValue != null) {
			oldValues.put(SECONDS, getAttribute(SECONDS));
			setAttribute(SECONDS, defValue);
		}
		
		return oldValues;
	}
}

