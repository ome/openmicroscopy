/*
 * org.openmicroscopy.shoola.env.log.LogMessage
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.log;

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
 * platform-independent interface.
 * 
 * @see	org.openmicroscopy.shoola.env.log.Logger
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
{
	
	/**
	 * The new line sequence.
	 * You can use this to write a string that contains multiple lines.
	 */
	public static final String	NEW_LINE = System.getProperty("line.separator");
	
	/** The tabulation sequence. */
	public static final String	TAB = "\t";  //TODO: platform independent?
	
	
	/** The output stream where we write the message. */
	private StringWriter	message;
	
	/** Wraps {@link #message} to provide writing and buffering capabilities. */
	private PrintWriter		formatter;
		
	/**
	 * Creates a new empty message.
	 */
	public LogMessage() 
	{
		message = new StringWriter();
		formatter = new PrintWriter(message);
	}
	
	/**
	 * Appends the specified string to the current message's content.
	 * 
	 * @param s	The text to add.
	 */
	public void write(String s)
	{
		formatter.print(s);
	}

	/**
	 * Appends the specified string to the current message's content and then
	 * terminates the line.
	 * 
	 * @param s	The text to add.
	 */	
	public void writeln(String s) { formatter.println(s); }
	
	/** Terminates the current line. */	
	public void writeln() { formatter.println(); }
	
	/** Appends a tabulation to the current message's content. */	
	public void writetab() { formatter.print(TAB); }
	
	/**
	 * Appends the specified number of tabulations to the current message's
	 * content.
	 * 
	 * @param howMany	Specifies the number of tabs to add.  This method will
	 * 					do nothing if this number is not positive.
	 */
	public void writetab(int howMany)
	{
		for (int i = 0; i < howMany; ++i)	writetab();
	}
	
	/**
	 * Returns the current content of the message.
	 * After this call, the current content is flushed.  This means that all
	 * what has been written so far will be discarded.
	 * 
	 * @return	See above.
	 */
	public String toString()
	{
		formatter.flush();
		return message.toString();
	}

}
