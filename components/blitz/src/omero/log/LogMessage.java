/*
 * org.openmicroscopy.shoola.env.log.LogMessage
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

package omero.log;

//Java imports
import java.io.PrintWriter;
import java.io.StringWriter;

//Third-party libraries

//Application-internal dependencies

/** 
 * Writes a multi-line log message into a single string.
 * This class should be used to write a log message on multiple lines.  In fact,
 * writing text that spans multiple lines is a platform-dependent operation.
 * This class conveniently encapsulates this process while providing a 
 * platform-independent interface.  This class extends {@link PrintWriter} to
 * inherit all sort of useful <code>print</code> methods and adds another 
 * {@link #print(Throwable) print} method to write a stack trace into the log
 * message.
 * 
 * @see	omero.log.shoola.env.log.Logger
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class LogMessage
	extends PrintWriter
{
	
	/**
	 * The new line sequence.
	 * You can use this to write a string that contains multiple lines.
	 */
	public static final String	NEW_LINE = System.getProperty("line.separator");
	
		
	/** Creates a new empty message. */
	public LogMessage() 
	{
		super(new StringWriter());
	}
	
	public LogMessage(String msg, Throwable t) {
	    this();
	    print(msg);
        print(t);
	}
	
	/**
	 * Writes a stack trace into this log message.
	 * The information from the exception context is extracted and formatted
	 * into a diagnostic message, then printed into the log message.
	 * This information includes the exception class name, the exception
	 * message, a snapshot of the current stack, and the name of the current
	 * thread.
	 * 
	 * @param t	The exception.
	 */
	public void print(Throwable t)
	{
		t.printStackTrace(this);
		print(out.toString());
		print("Exception in thread \"");
		print(Thread.currentThread().getName());	
		println("\"");
	}
	
	/**
	 * Returns the current content of the message.
	 * After this call, the current content is flushed. This means that all
	 * what has been written so far will be discarded.
	 * 
	 * @return	See above.
	 */
	public String toString()
	{
		flush();
		return out.toString();
	}

}
