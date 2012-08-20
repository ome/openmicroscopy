/*
 * org.openmicroscopy.shoola.env.data.views.ProcessCallback 
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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.data.views;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.RequestCallback;
import org.openmicroscopy.shoola.env.data.ScriptCallback;
import org.openmicroscopy.shoola.env.data.ProcessException;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;

/** 
 * Wraps the various call handle.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ProcessCallback 
{

	/** The length of the unit of work. */
	static final long UNIT_OF_WORK = 500;
	
	/** The actual call-back to handle. */
	private Object callback;
	
	/**
	 * Checks if the passed handle is supported.
	 * 
	 * @param callback The call-back to check.
	 */
	private void checkHandle(Object callback)
	{
		if (callback instanceof ScriptCallback) return;
		if (callback instanceof RequestCallback) return;
		throw new omero.IllegalArgumentException("Call back not supported.");
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param callback The call-back to check.
	 */
	public ProcessCallback(Object callback)
	{
		if (callback == null)
			throw new IllegalArgumentException("Handle cannot be null.");
		checkHandle(callback);
		this.callback = callback;
	}
	
	/**
	 * Sets the adapter. 
	 * 
	 * @param adapter The value to set.
	 */
	public void setAdapter(DSCallAdapter adapter)
	{
		if (callback instanceof ScriptCallback)
			((ScriptCallback) callback).setAdapter(adapter);
		else if (callback instanceof RequestCallback)
			((RequestCallback) callback).setAdapter(adapter);
	}
	
	/** Cancels the on-going process. */
	public void cancel()
		throws ProcessException
	{
		if (callback instanceof ScriptCallback)
			((ScriptCallback) callback).cancel();
	}
	
	/**
	 * Blocks to do a unit of work and returns the corresponding action.
	 * 
	 * @param value The length of the block.
	 * @return See above.
	 * @throws Exception If an error occurs.
	 */
	Object block(long value)
		throws Exception
	{
		if (callback instanceof ScriptCallback)
			return ((ScriptCallback) callback).block(value);
		if (callback instanceof RequestCallback)
			return ((RequestCallback) callback).block(value);
		return null;
	}
	
	/** 
	 * Closes the handle.
	 * @throws Exception If an error occurs.
	 */
	void close()
		throws Exception
	{
		if (callback instanceof ScriptCallback) {
			((ScriptCallback) callback).close();
		} else if (callback instanceof RequestCallback) {
			((RequestCallback) callback).close(true);
		}
	}
	
}
