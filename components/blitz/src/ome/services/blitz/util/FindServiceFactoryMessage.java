/*
 * Copyright (C) 2013 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package ome.services.blitz.util;

import ome.services.blitz.impl.ServiceFactoryI;
import ome.util.messages.InternalMessage;

/**
 * {@link InternalMessage} raised when a servant needs to find
 * the {@link ServiceFactoryI} instance that the current user
 * is working with.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class FindServiceFactoryMessage extends InternalMessage {

    private static final long serialVersionUID = 345845093802L;

    protected final transient Ice.Current curr;

    protected transient Ice.Identity id;

    protected transient ServiceFactoryI sf;

    public FindServiceFactoryMessage(Object source, Ice.Identity id) {
        super(source);
        this.id = id;
        this.curr = null;
    }

    public FindServiceFactoryMessage(Object source, Ice.Current current) {
        super(source);
        this.curr = current;
    }

    public Ice.Identity getIdentity() {
        return this.id;
    }

    public Ice.Current getCurrent() {
        if (this.curr == null) {
            throw new RuntimeException(
                    "This instance was initialized with an Identity");
        }
        return this.curr;
    }

    /**
     * Store the information. Subclasses may try to make remote calls.
     *
     * @throws omero.ServerError
     *      so that subclasses can make use of remote methods.
     */
    public void setServiceFactory(Ice.Identity id, ServiceFactoryI sf)
        throws omero.ServerError {
        this.sf = sf;
        this.id = id;
    }

    public ServiceFactoryI getServiceFactory() {
        return sf;
    }

}
