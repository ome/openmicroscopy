/*
 * org.openmicroscopy.shoola.svc.proxy.MessengerFileRequest
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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


import java.io.File;

import org.openmicroscopy.shoola.util.CommonsLangUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

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
        if (CommonsLangUtils.isBlank(reader)) reader = "unknown";
        this.reader = reader;
        this.file = file;
    }

    /**
     * Prepares the <code>method</code> to post.
     * @see Request#marshal(String)
     */
    public HttpUriRequest marshal(String path)
            throws TransportException
    {
        //Create request.
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart(FILE, new FileBody(file,
                ContentType.APPLICATION_OCTET_STREAM, file.getName()));
        builder.addPart(TOKEN, new StringBody(token, ContentType.TEXT_PLAIN));
        builder.addPart(READER, new StringBody(reader, ContentType.TEXT_PLAIN));
        HttpPost request = new HttpPost(path);
        request.setEntity(builder.build());
        return request;
    }

}
