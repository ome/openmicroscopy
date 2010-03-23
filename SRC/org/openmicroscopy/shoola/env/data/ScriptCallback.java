/*
 * org.openmicroscopy.shoola.env.data.ScriptCallback 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.data;


//Java imports

//Third-party libraries

//Application-internal dependencies
import Ice.Current;
import omero.RString;
import omero.ServerError;
import omero.client;
import omero.grid.ProcessCallbackI;
import omero.grid.ScriptProcessPrx;

/** 
 * A handle to a script computation.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ScriptCallback 
	extends ProcessCallbackI
{

	/** The identifier of the script. */
	private long scriptID;
	
	/** Helper reference to the process. */
	private ScriptProcessPrx process;
	
	private Object result;
	
	/**
	 * 
	 * @param scriptID
	 * @param client
	 * @param process
	 * @throws ServerError
	 */
	ScriptCallback(long scriptID, client client, ScriptProcessPrx process) 
		throws ServerError
	{
		super(client, process);
		this.scriptID = scriptID;
		this.process = process;
	}
	
	/**
	 * Returns the name of the script.
	 * 
	 * @return See above.
	 */
	public String getName()
	{
		String value = "";
		try {
			RString desc = process.getJob().getDescription();
			if (desc != null) value = desc.getValue();
		} catch (Exception e) {
		}
		return value;
	}
	
	/** Cancels the on-going process. */
	public void cancel()
		throws ScriptingException
	{
		System.err.println("cancel");
		try {
			process.cancel();
			close();
		} catch (Exception e) {
			throw new ScriptingException("Cannot cancel the following " +
					"script:"+getName());
		}
	}
	
	/**
	 * Overridden to handle the end of the process.
	 * @see ProcessCallbackI#processFinished(int, Current)
	 */
	public void processFinished(int value, Current current)
	{
		super.processFinished(value, current);
		
		System.err.println("Done");
		try {
			System.err.println(process.getResults(0));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Overridden to handle the cancellation of the process.
	 * @see ProcessCallbackI#processCancelled(boolean, Current)
	 */
	public void processCancelled(boolean value, Current current)
	{
		super.processCancelled(value, current);
		System.err.println("Cancel");
	}
	

	/**
	 * Overridden to handle the fact of the process has been killed.
	 * @see ProcessCallbackI#processKilled(boolean, Current)
	 */
	public void processKilled(boolean value, Current current)
	{
		
	}
	
}
