/*
 * org.openmicroscopy.shoola.svc.proxy.CommunicatorProxy 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.svc.communicator.Communicator;
import org.openmicroscopy.shoola.svc.transport.HttpChannel;
import org.openmicroscopy.shoola.svc.transport.TransportException;

/** 
 * Activates the {@link Communicator}.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class CommunicatorProxy
    extends AbstractProxy
    implements Communicator
{

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
     * @see Communicator#submitComment(String, String, String, String, String,
     *                              String, StringBuilder)
     */
    public void submitComment(String invoker, String email, String comment,
            String extra, String applicationName, String applicationVersion,
            StringBuilder reply)
                    throws TransportException
    {
        MessengerRequest out = new MessengerRequest(email, comment, extra,
                null, applicationName, invoker, applicationVersion, null, null);
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
     * @see Communicator#submitError(String, String, String, String, String,
     *                          String, String, Map, StringBuilder)
     */
    public void submitError(String invoker, String email, String comment,
            String extra, String error, String applicationName,
            String applicationVersion, StringBuilder reply) 
                    throws TransportException
    {
        submitFilesError(invoker, email, comment, extra, error, applicationName,
                applicationVersion, null, null, reply);
    }

    /**
     * Implemented as specified by the {@link Communicator} interface.
     * @see Communicator#submitFilesError(String, String, String, String,
     *                      String, String, String, File, List, StringBuilder)
     */
    public void submitFilesError(String invoker, String email, String comment,
            String extra, String error, String applicationName,
            String applicationVersion, File mainFile,
            List<File> associatedFiles, StringBuilder reply)
                    throws TransportException
    {
        MessengerRequest out = new MessengerRequest(email, comment, extra,
                error, applicationName, invoker, applicationVersion, 
                mainFile, associatedFiles);
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
     * @see Communicator#submitFile(String, File, String, StringBuilder)
     */
    public void submitFile(String token, File file, String reader,
            StringBuilder reply)
                    throws TransportException
    {
        if (token == null)
            throw new IllegalArgumentException("No token specified.");
        if (file == null)
            throw new IllegalArgumentException("No file to submit.");
        //Get a token
        MessengerFileRequest out;
        MessengerReply in = new MessengerReply(reply);
        try {
            out = new MessengerFileRequest(token, file, reader);
            channel.exchange(out, in);
        } catch (IOException ioe) {
            throw new TransportException(
                    "Couldn't communicate with server (I/O error).", ioe);
        }
    }

}
