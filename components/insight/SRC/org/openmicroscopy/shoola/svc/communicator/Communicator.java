/*
 * org.openmicroscopy.shoola.svc.communicator.Communicator 
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
package org.openmicroscopy.shoola.svc.communicator;


import java.io.File;
import java.util.List;

import org.openmicroscopy.shoola.svc.transport.TransportException;

/** 
 * Provides methods to send debug messages when an error occurred or 
 * to send comments.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public interface Communicator
{

    /**
     * Sends a message to the server collecting the errors and comments.
     * 
     * @param invoker The client posting the message.
     * @param email The <code>e-mail</code> address of the user submitting the
     *              bug.
     * @param comment The comment entered by the user.
     * @param extra Extra information related to the bug.
     * @param error The error message.
     * @param applicationName The name of the application.
     * @param applicationVersion The version of the application.
     * @param reply The result of the post.
     * @throws TransportException Thrown if an error occurred while trying
     *                            to submit the error.
     */
    public void submitError(String invoker, String email, String comment,
            String extra, String error, String applicationName,
            String applicationVersion, StringBuilder reply)
                    throws TransportException;

    /**
     * Sends a message to the server collecting the errors and comments.
     * 
     * @param invoker The client posting the message.
     * @param email The <code>e-mail</code> address of the user submitting the
     *              bug.
     * @param comment The comment entered by the user.
     * @param extra Extra information related to the bug.
     * @param error The error message.
     * @param applicationName The name of the application.
     * @param applicationVersion The version of the application.
     * @param mainFile The main file.
     * @param associatedFiles The associated files.
     * @param reply The result of the post.
     * @throws TransportException Thrown if an error occurred while trying
     *                            to submit the error.
     */
    public void submitFilesError(String invoker, String email, String comment,
            String extra, String error, String applicationName,
            String applicationVersion, File mainFile,
            List<File> associatedFiles, StringBuilder reply)
                    throws TransportException;

    /**
     * Sends a comment to the server collecting the errors and comments.
     * 
     * @param invoker The client posting the message.
     * @param email The <code>e-mail</code> address of the user
     *              submitting the bug.
     * @param comment The comment entered by the user.
     * @param extra Extra information related to the bug.
     * @param applicationName The name of the application.
     * @param applicationVersion The version of the application.
     * @param reply The result of the post.
     * @throws TransportException Thrown if an error occurred while trying
     *                            to submit the error.
     */
    public void submitComment(String invoker, String email, String comment,
            String extra, String applicationName,
            String applicationVersion, StringBuilder reply)
                    throws TransportException;

    /**
     * Submits the file that failed to import and its possible related files.
     * 
     * @param token The token returned by previous call
     * @param file The file to send.
     * @param reader The reader used.
     * @param reply The result of the post.
     * @throws TransportException Thrown if an error occurred while trying
     *                            to submit the error.
     */
    public void submitFile(String token, File file, String reader,
            StringBuilder reply)
                    throws TransportException;

}
