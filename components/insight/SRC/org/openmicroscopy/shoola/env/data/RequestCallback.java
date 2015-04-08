/*
 * org.openmicroscopy.shoola.env.data.RequestCallback 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import omero.ServerError;
import omero.client;
import omero.cmd.CmdCallbackI;
import omero.cmd.DoAllRsp;
import omero.cmd.ERR;
import omero.cmd.HandlePrx;
import omero.cmd.OK;
import omero.cmd.Response;
import omero.cmd.Status;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import Ice.Current;

/** 
 * A handle to a perform operation e.g. delete, move data between groups.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class RequestCallback
	extends CmdCallbackI
{

	/** Helper reference to the adapter to notify. */
	private DSCallAdapter adapter;
	
	/** Flag indicating that the operation has finished. */
	private boolean finished;
	
	/** Flag indicating that the results have been submitted. */
	private boolean submitted;
	
	/** 
	 * Handles the response of the data transfer.
	 * 
	 * @return The transformation of the response.
	 */
	private Object handleResponse()
	{
		Response response = getResponse();
		if (response == null) return Boolean.valueOf(false);
		if (response instanceof DoAllRsp) {
			List<Response> responses = ((DoAllRsp) response).responses;
			if (responses.size() == 1) return responses.get(0);
			return responses;
		}
		else if (response instanceof OK) return Boolean.valueOf(true);
		else if (response instanceof ERR)
			return new ProcessReport((ERR) response);
		return null;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param client Reference to the client.
	 * @param process The process to handle.
	 * @throws ServerError Thrown if an error occurred while initializing the
	 * 					   call-back.
	 */
	RequestCallback(client client, final HandlePrx process)
		throws ServerError
	{
		super(client, process);
	}
	
	/**
     * Creates a new instance.
     * @param ccb The CmdCallbackI to pass through
     * @throws ServerError Thrown if an error occurred while initializing the
     *                     call-back.
     */
    RequestCallback(CmdCallbackI ccb)
        throws ServerError
    {
        super(ccb);
    }
	
	/**
	 * Sets the adapter. 
	 * 
	 * @param adapter The value to set.
	 */
	public void setAdapter(DSCallAdapter adapter)
	{
		this.adapter = adapter;
		if (finished && adapter != null && !submitted) {
			Object ho = handleResponse();
			if (ho != null) adapter.handleResult(ho);
			try {
				close(false); 
			} catch (Exception e) {}
		}
	}
	
	/**
	 * Overridden to handle the end of the process.
	 * @see CmdCallbackI#onFinished(Response, Status, Current)
	 */
	public void onFinished(Response rsp, Status status, Current c)
	{
		super.onFinished(rsp, status, c);
		finished = true;
		if (adapter != null) {
			submitted = true;
			Object ho = handleResponse();
			if (ho != null) adapter.handleResult(ho);
		}
		if (submitted) {
			try {
				close(true);
			} catch (Exception e) {}
		}
	}

}
