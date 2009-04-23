/*
 * ome.ij.data.KeepClientAlive 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package ome.ij.data;



//Java imports

//Third-party libraries
import Ice.ConnectionLostException;

//Application-internal dependencies

/** 
 * 
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
class KeepClientAlive 
	implements Runnable
{

	/** Reference to the gateway. */
	private Gateway gateway;
	
	
	/** Runs. */
	public void run()
	{
		try {
            synchronized (gateway) {
            	gateway.keepSessionAlive();
            }
        } catch (Throwable t) {
        	Throwable cause = t.getCause();
			int index = Gateway.SERVER_OUT_OF_SERVICE;
			if (cause instanceof ConnectionLostException)
				index = Gateway.LOST_CONNECTION;
        	try {
        		ServicesFactory f = ServicesFactory.getInstance();
            	if (f.isConnected()) f.sessionExpiredExit(index);
			} catch (Exception e) {}
        }
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param gateway   Reference to the gateway. Mustn't be <code>null</code>.
	 */
	KeepClientAlive(Gateway gateway)
	{
		if (gateway == null)
			throw new IllegalArgumentException("No gateway specified.");
		this.gateway = gateway;
	}
	
}
