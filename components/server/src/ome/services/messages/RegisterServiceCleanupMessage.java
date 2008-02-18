/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.messages;

import ome.util.messages.InternalMessage;

/**
 * Published when some resource must be cleaned up after method execution.
 */
public abstract class RegisterServiceCleanupMessage extends InternalMessage {

    final public Object resource;

    public RegisterServiceCleanupMessage(Object source, Object resource) {
        super(source);
        this.resource = resource;
    }

    /**
     * Used to close the passed in resource. May NOT throw an exception.
     */
    public abstract void close();

}