/*
 * org.openmicroscopy.shoola.util.ui.MessengerDetails 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui;

import java.io.File;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Helper class where details to send are stored.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class MessengerDetails
{

	/** The e-mail address of the user. */
	private String email;
	
	/** The comment to send. */
	private String comment;
	
	/** Extra information. Not yet implemented. */
	private String extra;
	
	/** The error message. */
	private String error;
	
	/** The object to submit. */
	private Object toSubmit;

	/** Flag indicating to submit the exception but not the files. */
	private boolean exceptionOnly;
	
	/** The log file to submit.*/
	private File logFile;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param email		The e-mail address of the user.
	 * @param comment	The comment to send.
	 */
	public MessengerDetails(String email, String comment)
	{
		this.email = email;
		this.comment = comment;
		error = null;
		exceptionOnly = false;
		logFile = null;
	}

	/**
	 * Returns the error message.
	 * 
	 * @return See above.
	 */
	public String getError() { return error; }

	/**
	 * Sets the error message.
	 * 
	 * @param error The value to set.
	 */
	public void setError(String error) { this.error = error; }
	
	/**
	 * Sets the object to submit to the development team.
	 * 
	 * @param toSubmit The value to set.
	 */
	public void setObjectToSubmit(Object toSubmit) { this.toSubmit = toSubmit; }

	/**
	 * Sets the flag indicating to submit only the exception not the files.
	 * 
	 * @param exceptionOnly Pass <code>true</code> to only send the exception
	 *						and not the files, <code>false</code> otherwise.
	 */
	public void setExceptionOnly(boolean exceptionOnly)
	{
		this.exceptionOnly = exceptionOnly;
	}
	
	/**
	 * Returns <code>true</code> if only the exception should be sent,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isExceptionOnly()
	{ 
		return exceptionOnly && (logFile == null); 
	}
	
	/** 
	 * Returns the object to submit.
	 * 
	 * @return See above.
	 */
	public Object getObjectToSubmit() { return toSubmit; }
	
	/**
	 * Returns the comment to send.
	 * 
	 * @return See above.
	 */
	public String getComment() { return comment; }

	/**
	 * Returns the e-mail address.
	 * 
	 * @return See above.
	 */
	public String getEmail() { return email; }

	/**
	 * Returns the extra information.
	 * 
	 * @return See above.
	 */
	public String getExtra() { return extra; }
	
	/**
	 * Sets the extra information.
	 * 
	 * @param extra The value to set.
	 */
	public void setExtra(String extra) { this.extra = extra; }
	
	/**
	 * Sets the flag indicating to submit the log file.
	 * 
	 * @param logFile The log file to send or <code>null</code>.
	 */
	public void setLogFile(File logFile)
	{
		this.logFile = logFile;
	}
	
	/**
	 * Returns the log file or <code>null</code>.
	 * 
	 * @return See above.
	 */
	public File getLogFile() { return logFile; }
	
	/**
	 * Returns <code>true</code> if the main file has to be submitted,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isSubmitMainFile() { return !exceptionOnly; }

}
