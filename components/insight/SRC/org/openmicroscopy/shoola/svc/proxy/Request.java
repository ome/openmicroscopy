/*
 * org.openmicroscopy.shoola.svc.proxy.Request 
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

import org.openmicroscopy.shoola.util.CommonsLangUtils;
import org.apache.http.client.methods.HttpUriRequest;

import org.openmicroscopy.shoola.svc.transport.TransportException;

/** 
 * Top-class that each <code>Request</code> class should extend.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public abstract class Request
{

    /** Identifies the <code>method</code> parameter. */
    protected static final String METHOD_FIELD = "Method";

    /** The method used via http. */
    protected String method;

    /** Creates a new instance. */
    protected Request() {}

    /**
     * Creates a new instance.
     * 
     * @param method The method invoked. Mustn't be <code>null</code>.
     */
    protected Request(String method)
    {
        if (CommonsLangUtils.isBlank(method))
            throw new NullPointerException("No method name.");
        this.method = method;
    }

    /**
     * Prepares the <code>http</code> method.
     * 
     * @param path The server path.
     * @return See above.
     * @throws TransportException If an error occurred while preparing the
     *                            method.
     */
    public abstract HttpUriRequest marshal(String path)
            throws TransportException;

}
