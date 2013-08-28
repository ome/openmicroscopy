/*
 *   $Id$
 *
 *   Copyright (c) 2007 Glencoe Software Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.util;

import ome.util.messages.InternalMessage;
import omero.util.ServantHolder;

/**
 * {@link InternalMessage} raised when a servant should be registered for clean.
 * This is possibly the result of a redirect performed within a servant or of
 * the creation of another servant from SharedResourcesI.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @see ticket:2253
 */
public class RegisterServantMessage extends InternalMessage {

    private static final long serialVersionUID = 3409582093802L;

    private final transient Ice.Object servant;

    private final transient Ice.Current curr;

    private transient Ice.ObjectPrx prx;

    private transient ServantHolder holder;

    /**
     * Create a new message
     * @param source the source of the message
     * @param servant the servant that is to be registered
     * @param current the Ice context
     */
    public RegisterServantMessage(Object source, Ice.Object servant,
            Ice.Current current) {
        super(source);
        this.servant = servant;
        this.curr = current;
    }

    /**
     * @return the servant to register
     */
    public Ice.Object getServant() {
        return this.servant;
    }

    /**
     * @return the Ice context
     */
    public Ice.Current getCurrent() {
        return this.curr;
    }

    /**
     * @param prx the proxy object to set
     */
    public void setProxy(Ice.ObjectPrx prx) {
        if (this.prx != null) {
            throw new RuntimeException("Proxy can be set only once!");
        }
        this.prx = prx;
    }

    /**
     * @return the proxy object
     */
    public Ice.ObjectPrx getProxy() {
        return this.prx;
    }

    /**
     * @param holder the servant holder to set
     */
    public void setHolder(ServantHolder holder) {
        if (this.holder != null) {
            throw new RuntimeException("Holder can be set only once!");
        }
        this.holder = holder;
    }

    /**
     * @return the servant holder
     */
    public ServantHolder getHolder() {
        return this.holder;
    }
}
