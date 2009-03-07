/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.auth;

import java.security.Permissions;

import org.springframework.util.Assert;

import ome.security.SecuritySystem;

/**
 * Composite class which delegates
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @see SecuritySystem
 * @see Permissions
 * @since 4.0
 */

public class PasswordChangeException extends Exception {

    public PasswordChangeException(String msg) {
        super(msg);
    }

}
