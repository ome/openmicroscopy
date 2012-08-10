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
 * {@link InternalMessage} raised when a servant should be removed from the
 * {@link Ice.ObjectAdapter adapter}. This is most likely a result of a call to
 * "service.close()" from within
 * {@link ome.services.blitz.util.IceMethodInvoker}
 *
 * Though this instance is {@link java.io.Serializable} through inheritance, it
 * is not intended to be stored anywhere, but should be acted upon and discarded
 * immediately. The {@link Ice.Current} instance is not
 * {@link java.io.Serializable}
 *
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class UnregisterServantMessage extends InternalMessage {

    private static final long serialVersionUID = 3409582093802L;

    private final transient Ice.Current curr;

    private final transient ServantHolder holder;

    public UnregisterServantMessage(Object source, Ice.Current current,
        ServantHolder holder) {
        super(source);
        this.curr = current;
        this.holder = holder;
    }

    public Ice.Current getCurrent() {
        return this.curr;
    }

    public ServantHolder getHolder() {
        return this.holder;
    }
}
