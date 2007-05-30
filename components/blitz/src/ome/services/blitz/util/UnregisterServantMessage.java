/*
 *   $Id$
 *
 *   Copyright (c) 2007 Glencoe Software Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.util;

import ome.util.messages.InternalMessage;

/**
 * @author josh
 * 
 */
public class UnregisterServantMessage extends InternalMessage {

    private static final long serialVersionUID = 3409582093802L;

    String key;
    Ice.Current curr;
    
    public UnregisterServantMessage(Object source, String serviceKey, Ice.Current current) {
        super(source);
        this.key = serviceKey;
        this.curr = current;
    }
    
    public String getServiceKey() {
        return this.key;
    }
    
    public Ice.Current getCurrent() {
        return this.curr;
    }
    
}