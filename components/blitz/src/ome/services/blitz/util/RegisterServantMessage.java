/*
 *   $Id$
 *
 *   Copyright (c) 2007 Glencoe Software Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.util;

import java.util.UUID;

import ome.services.blitz.impl.ServiceFactoryI;
import ome.util.messages.InternalMessage;

/**
 * {@link InternalMessage} raised when a servant should be registered for clean.
 * This is possibly the result of a redirect performed within a servant or of
 * the creation of another servant from SharedResourcesI.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @see ticket:2253
 */
public class RegisterServantMessage extends FindServiceFactoryMessage {

    private static final long serialVersionUID = 3409582093802L;

    private final transient Ice.Object servant;

    private transient Ice.ObjectPrx prx;

    public RegisterServantMessage(Object source, Ice.Object servant,
            Ice.Current current) {
        super(source, current);
        this.servant = servant;
    }

    public Ice.Object getServant() {
        return this.servant;
    }

    private void setProxy(Ice.ObjectPrx prx) {
        if (this.prx != null) {
            throw new RuntimeException("Proxy can only be set once!");
        }
        this.prx = prx;
    }

    public Ice.ObjectPrx getProxy() {
        return this.prx;
    }

    public void setServiceFactory(Ice.Identity id, ServiceFactoryI sf)
            throws omero.ServerError {
        super.setServiceFactory(id, sf);
        if (sf != null) {
            final Ice.Identity newId = new Ice.Identity(UUID.randomUUID().toString(), id.name);
            final Ice.Object servant = getServant();
            sf.configureServant(servant); // Sets holder
            setProxy(sf.registerServant(newId, servant));
        }
    }

}
