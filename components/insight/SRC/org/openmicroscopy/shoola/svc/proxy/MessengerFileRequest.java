/*
 * org.openmicroscopy.shoola.svc.proxy.MessengerFileRequest
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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

//Third-party libraries

//Application-internal dependencies

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.lang.StringUtils;
import org.openmicroscopy.shoola.svc.transport.TransportException;

/**
 * Prepares a request to post a file.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
class MessengerFileRequest
    extends Request
{

    /** Identifies the token returned by the server. */
    private static final String TOKEN = "token";

    /** Identifies the reader used. */
    private static final String READER = "file_format";

    /** Identifies the <code>file</code> to send. */
    private static final String FILE = "Filedata";

    /** The token returned by the server. */
    private String token;

    /** The type of reader used to import the file. */
    private String reader;

    /** The file to send. */
    private File file;

    /**
     * Creates a new instance.
     * 
     * @param token The e-mail address of the user reporting an error.
     * @param file The extra information entered by the user.
     * @param reader The reader used to import the file.
     */
    MessengerFileRequest(String token, File file, String reader)
    {
        super();
        this.token = token;
        if (StringUtils.isBlank(reader)) reader = "unknown";
        this.reader = reader;
        this.file = file;
    }

    /**
     * Prepares the <code>method</code> to post.
     * @see Request#marshal()
     */
    public HttpMethod marshal() 
            throws TransportException
    {
        //Create request.
        PostMethod request = new PostMethod();
        try {
            Part[] parts = {new StringPart(TOKEN, token),
                    new StringPart(READER, reader),
                    new SubmittedFilePart(FILE,  file)
            };
            request.setRequestEntity(new MultipartRequestEntity(
                    parts, request.getParams()));
        } catch (Exception e) {
            throw new TransportException("Cannot prepare file to submit", e);
        }
        return request;
    }

}
