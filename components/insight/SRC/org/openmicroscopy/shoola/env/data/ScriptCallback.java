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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.model.ParamData;
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
	private long		scriptID;
	
	/** Helper reference to the adapter to notify. */
	private DSCallAdapter adapter;
	
	/** Flag indicating that the operation has finished. */
	private boolean finished;
	
	/** Flag indicating that the results have been submitted. */
	private boolean submitted;
	
	/** The results of the script. */
	private Map<String, Object> results;
	
	public ScriptCallback(long scriptID, ProcessCallbackI pcb)
        throws ServerError
    {
	    super(pcb);
        this.scriptID = scriptID;
        results = null;
    }
	
	/**
	 * Creates a new instance.
	 * 
	 * @param scriptID The identifier of the script to run.
	 * @param client   Reference to the client.
	 * @param process  The process to handle.
	 * @throws ServerError Thrown if an error occurred while initializing the
	 * 					   call-back.
	 */
	public ScriptCallback(long scriptID, client client, 
			final ScriptProcessPrx process)
		throws ServerError
	{
		super(client, process);
		this.scriptID = scriptID;
		results = null;
	}
	
	/**
	 * Sets the adapter. 
	 * 
	 * @param adapter The value to set.
	 */
	public void setAdapter(DSCallAdapter adapter)
	{
		this.adapter = adapter;
		if (finished && adapter != null) {
			if (!submitted) {
				adapter.handleResult(results);
				try {
					close();
				} catch (Exception e) {}
			}
		}	
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
			RString desc = 
				((ScriptProcessPrx) process).getJob().getDescription();
			if (desc != null) value = desc.getValue();
		} catch (Exception e) {
		}
		return value;
	}
	
	/** Cancels the on-going process. */
	public void cancel()
		throws ProcessException
	{
		try {
			process.cancel();
			close();
		} catch (Exception e) {
			throw new ProcessException("Cannot cancel the following " +
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
		finished = true;
		try {
			Map<String, RType> r = ((ScriptProcessPrx) process).getResults(0);
			if (r != null) {
				results = new HashMap<String, Object>();
				Iterator i = r.entrySet().iterator();
				RType type;
				Entry entry;
				while (i.hasNext()) {
					entry = (Entry) i.next();
					results.put((String) entry.getKey(), 
						ParamData.convertRType((RType) entry.getValue()));
				}
			}
			if (adapter != null) {
				submitted = true;
				adapter.handleResult(results);
			}
		} catch (Exception e) {
			finished = false;
		}
		
		if (finished && submitted) {
			try {
				close();
			} catch (Exception e) {}
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
