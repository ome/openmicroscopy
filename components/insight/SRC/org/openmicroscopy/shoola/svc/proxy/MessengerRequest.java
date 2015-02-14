/*
 * org.openmicroscopy.shoola.svc.proxy.MessengerRequest 
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.util.CommonsLangUtils;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;

//Application-internal dependencies
import org.openmicroscopy.shoola.svc.transport.TransportException;

/** 
 * Prepares a request to post.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
class MessengerRequest
    extends Request
{

    /** Identifies the <code>e-mail</code> address. */
    private static final String EMAIL = "email";

    /** Identifies the <code>comment</code> sent. */
    private static final String COMMENT = "comment";

    /** Identifies the <code>error</code> message. */
    private static final String ERROR = "error";

    /** Identifies the <code>extra information</code>. */
    private static final String EXTRA = "extra";

    /** Identifies the <code>client</code> invoking the service. */
    private static final String INVOKER = "type";

    /** Identifies the name of <code>main file</code>. */
    private static final String MAIN_FILE_NAME = "selected_file";

    /** Identifies the path <code>main file</code>. */
    private static final String MAIN_FILE_PATH = "absolute_path";

    /** Identifies the name of <code>additional file</code>. */
    private static final String ADDITIONAL_FILE_NAME = "additional_files";

    /** Identifies the path of <code>additional file</code>. */
    private static final String ADDITIONAL_FILE_PATH = "additional_files_path";

    /** Identifies the size of <code>additional file</code>. */
    private static final String ADDITIONAL_FILE_SIZE = "additional_files_size";

    /** The error message. */
    private String error;

    /** The e-mail address of the user reporting an error. */
    private String email;

    /** The comment entered by the user. */
    private String comment;

    /** The extra information entered by the user. */
    private String extra;

    /** The client posting the message. */
    private String invoker;

    /** The number associated to the application. */
    private String applicationNumber;

    /** The version of the application. */
    private String applicationVersion;

    /** The main file. */
    private File mainFile;

    /** The associated files. */
    private List<File> associatedFiles;

    /** Checks if the <code>null</code> values.*/
    private void checkForNull()
    {
        if (email == null) email = "";
        if (comment == null) comment = "";
        if (extra == null) extra = "";
        if (invoker == null) invoker = "";
    }

    /**
     * Creates a new instance.
     *
     * @param email The e-mail address of the user reporting an error.
     * @param comment The comment entered by the user.
     * @param extra The extra information entered by the user.
     * @param error The error message.
     * @param applicationNumber The reference number for the application.
     * @param invoker The client posting the message.
     * @param applicationVersion The version of the application.
     * @param filesInfo The information about the files to submit or
     *                  <code>null</code>.
     */
    MessengerRequest(String email, String comment, String extra, String error,
            String applicationNumber, String invoker, String applicationVersion,
            File mainFile, List<File> associatedFiles)
    {
        super();
        this.error = error;
        this.email = email;
        this.comment = comment;
        this.extra = extra;
        this.invoker = invoker;
        this.applicationNumber = applicationNumber;
        this.applicationVersion = applicationVersion;
        this.mainFile = mainFile;
        this.associatedFiles = associatedFiles;
        checkForNull();
    }

    /**
     * Prepares the <code>method</code> to post.
     * @see Request#marshal(String)
     */
    public HttpUriRequest marshal(String path)
            throws TransportException
    {
        //Create request.
        if (CommonsLangUtils.isBlank(path))
            throw new TransportException("No path specified.");
        HttpPost request = new HttpPost(path);
        request.addHeader("Accept", "text/plain");
        request.addHeader("Content-type", "application/x-www-form-urlencoded");
        List<BasicNameValuePair> p = new ArrayList<BasicNameValuePair>();
        p.add(new BasicNameValuePair(COMMENT, comment));
        p.add(new BasicNameValuePair(EMAIL, email));
        p.add(new BasicNameValuePair(ERROR, error));
        p.add(new BasicNameValuePair(EXTRA, extra));
        p.add(new BasicNameValuePair(INVOKER, invoker));
        p.add(new BasicNameValuePair(ProxyUtil.APP_NAME,
                applicationNumber));
        p.add(new BasicNameValuePair(ProxyUtil.APP_NAME, applicationNumber));
        p.add(new BasicNameValuePair(ProxyUtil.APP_VERSION, applicationVersion));
        
        Map<String, String> info = ProxyUtil.collectInfo();
        Entry<String, String> entry;
        Iterator<Entry<String, String>> k = info.entrySet().iterator();
        while (k.hasNext()) {
            entry = k.next();
            p.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        if (mainFile != null) {
            p.add(new BasicNameValuePair(MAIN_FILE_NAME, mainFile.getName()));
            p.add(new BasicNameValuePair(MAIN_FILE_PATH,
                    mainFile.getAbsolutePath()));
        }
        if (CollectionUtils.isNotEmpty(associatedFiles)) {
            Iterator<File> i = associatedFiles.iterator();
            File f;
            while (i.hasNext()) {
                f = i.next();
                p.add(new BasicNameValuePair(ADDITIONAL_FILE_NAME,
                        f.getName()));
                if (f.getParent() != null) 
                    p.add(new BasicNameValuePair(ADDITIONAL_FILE_PATH,
                            f.getParent()));
                p.add(new BasicNameValuePair(ADDITIONAL_FILE_SIZE,
                        ((Long) f.length()).toString()));
            }
        }

        try {
            request.setEntity(new UrlEncodedFormEntity(p));
        } catch (Exception e) {
            throw new TransportException("Cannot prepare parameters", e);
        }
        return request;
    }

}
