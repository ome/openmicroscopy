/*
 * org.openmicroscopy.shoola.env.data.DeleteCallback 
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
package org.openmicroscopy.shoola.env.data;


//Java imports

//Third-party libraries

//Application-internal dependencies
import Ice.Current;
import omero.ServerError;
import omero.client;
import omero.api.delete.DeleteHandlePrx;
import omero.grid.DeleteCallbackI;
import omero.grid.ProcessCallbackI;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;

/** 
 * A handle to a delete computation.
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
public class DeleteCallback 
	extends DeleteCallbackI
{

	/** Helper reference to the adapter to notify. */
	private DSCallAdapter adapter;
	
	/** Helper reference to the process. */
	private DeleteHandlePrx  process;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param client   Reference to the client.
	 * @param process  The process to handle.
	 * @throws ServerError Thrown if an error occurred while initializing the
	 * 					   call-back.
	 */
	DeleteCallback(client client, final DeleteHandlePrx process)
		throws ServerError
	{
		super(client, process);
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
	 * Overridden to handle the end of the process.
	 * @see ProcessCallbackI#processFinished(int, Current)
	 */
	public void processFinished(int value, Current current)
	{
		//super.processFinished(value, current);
		if (adapter == null) return;
		try {
			if (adapter != null) {
				adapter.handleResult(process.finished());
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
	
}
