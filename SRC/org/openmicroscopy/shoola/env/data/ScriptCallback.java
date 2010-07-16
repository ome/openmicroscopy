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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.model.ParamData;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;

import Ice.Current;
import omero.RString;
import omero.RType;
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
	private long              scriptID;
	
	/** Helper reference to the process. */
	private ScriptProcessPrx  process;
	
	private DSCallAdapter adapter;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param scriptID The identifier of the script to run.
	 * @param client   Reference to the client.
	 * @param process  The process to handle.
	 * @throws ServerError Thrown if an error occurred while initializing the
	 * 					   call-back.
	 */
	public ScriptCallback(long scriptID, client client, final ScriptProcessPrx process)
		throws ServerError
	{
		super(client, process);
		this.scriptID = scriptID;
		this.process = process;
	}
	
	/**
	 * Sets the adapter. 
	 * 
	 * @param adapter The value to set.
	 */
	public void setAdapter(DSCallAdapter adapter)
	{
		this.adapter = adapter;
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
		if (adapter == null) return;
		try {
			if (adapter != null) {
				Map<String, RType> results = process.getResults(0);
				if (results == null)
					adapter.handleResult(null);
				else {
					Map<String, Object> r = new HashMap<String, Object>();
					Iterator i = results.entrySet().iterator();
					RType type;
					Entry entry;
					while (i.hasNext()) {
						entry = (Entry) i.next();
						r.put((String) entry.getKey(), 
							ParamData.convertRType((RType) entry.getValue()));
					}
					adapter.handleResult(r);
				}
			}
		} catch (Exception e) {
		    if (adapter != null) adapter.handleResult(null);
		}
		
		try {
			close();
		} catch (Exception e) {
			//ignore the exception.
		}
	}
	
	/**
	 * Overridden to handle the cancellation of the process.
	 * @see ProcessCallbackI#processCancelled(boolean, Current)
	 */
	public void processCancelled(boolean value, Current current)
	{
		super.processCancelled(value, current);
		if (adapter != null) adapter.handleResult(null);
	}
	

	/**
	 * Overridden to handle the fact of the process has been killed.
	 * @see ProcessCallbackI#processKilled(boolean, Current)
	 */
	public void processKilled(boolean value, Current current)
	{
		super.processKilled(value, current);
		if (adapter != null) adapter.handleResult(null);
	}
	
}
