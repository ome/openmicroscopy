/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.fire;

import ome.system.Principal;

/**
 * {@link Principal} subclass which contains the current session name since for
 * each session there will be exactly one {@link SessionPrincipal}.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class SessionPrincipal extends Principal {
    
    /**
     * 
     */
    private static final long serialVersionUID = 3430511325360701367L;
    private final String session;

    public SessionPrincipal(String name, String group, String type, String sess) {
        super(name, group, type);
        this.session = sess;
    }

    public String getSession() {
        return session;
    }

}