/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.messages;

import ome.util.messages.InternalMessage;

/**
 * Published when any check is performed against a password. The success field
 * may be null in which case the implementation did not know the user.
 */
public class LoginAttemptMessage extends InternalMessage {

    private static final long serialVersionUID = 109845928435209L;

    public final String user;

    public final Boolean success;

    public LoginAttemptMessage(Object source, String user, Boolean success) {
        super(source);
        this.user = user;
        this.success = success;
    }

}