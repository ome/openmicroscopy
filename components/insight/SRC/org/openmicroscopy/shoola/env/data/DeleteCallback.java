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
import java.util.ArrayList;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import omero.ServerError;
import omero.client;
import omero.api.delete.DeleteHandlePrx;
import omero.api.delete.DeleteReport;
import omero.grid.DeleteCallbackI;
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
	
	/** List handling the reports. */
	private List<DeleteReport> reports;
	
	/** Flag indicating that the operation has finished. */
	private boolean finished;
	
	/** Flag indicating that the results have been submitted. */
	private boolean submitted;
	
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
		reports = null;
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
			if (!submitted) adapter.handleResult(reports);
			try {
				close();
			} catch (Exception e) {}
		}
	}
	
	/**
	 * Overridden to handle the end of the process.
	 * @see DeleteCallbackI#finished(int)
	 */
	public void finished(int value)
	{
		super.finished(value);
		finished = true;
		try {
			DeleteReport[] reports = handle.report();
			this.reports = new ArrayList<DeleteReport>();
			if (handle.errors() != 0) {
				for (int i = 0; i < reports.length; i++) 
					this.reports.add(reports[i]);
			}
			if (adapter != null) {
				submitted = true;
				adapter.handleResult(this.reports);
			}
		} catch (Exception e) {
			finished = false;
		    //if (adapter != null) adapter.handleResult(null);
		}
		
		if (finished && adapter != null) {
			try {
				close();
			} catch (Exception e) {
				//ignore the exception.
			}
		}
	}
	
}
