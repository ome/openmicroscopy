/*
 * org.openmicroscopy.shoola.svc.proxy.CommunicatorProxy 
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
package org.openmicroscopy.shoola.svc.proxy;



//Java imports
import java.io.IOException;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.svc.communicator.Communicator;
import org.openmicroscopy.shoola.svc.transport.HttpChannel;
import org.openmicroscopy.shoola.svc.transport.TransportException;

/** 
 * Acitvates the {@link Communicator}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class CommunicatorProxy
	extends AbstractProxy
	implements Communicator
{

	/** The tool invoking the service. */
	private static final String INVOKER_ERROR = "insight_bug";
	
	/** The tool invoking the service. */
	private static final String INVOKER_COMMENT = "insight_comment";
	
	/**
	 * Creates a new instance.
	 * 
	 * @param channel The communication link.
	 */
	public CommunicatorProxy(HttpChannel channel)
	{
		super(channel);
	}

	/**
	 * Implemented as specified by the {@link Communicator} interface.
	 * @see Communicator#submitComment(String, String, String, String)
	 */
	public void submitComment(String email, String comment, String extra, 
							String reply) 
		throws TransportException 
	{
		MessengerRequest out = new MessengerRequest(email, comment, extra, 
													null, INVOKER_COMMENT);
		MessengerReply in = new MessengerReply(reply);
        
        try {
            channel.exchange(out, in);
        } catch (IOException ioe) {
            throw new TransportException(
                    "Couldn't communicate with server (I/O error).", ioe);
        }
	}

	/**
	 * Implemented as specified by the {@link Communicator} interface.
	 * @see Communicator#submitError(String, String, String, String, String)
	 */
	public void submitError(String email, String comment, String extra, 
							String error, String reply) 
		throws TransportException
	{
		MessengerRequest out = new MessengerRequest(email, comment, extra, 
														error, INVOKER_ERROR);
		MessengerReply in = new MessengerReply(reply);
        
        try {
            channel.exchange(out, in);
        } catch (IOException ioe) {
            throw new TransportException(
                    "Couldn't communicate with server (I/O error).", ioe);
        }
		
	}
	
}
