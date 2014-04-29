/*
 *   $Id$
 *
 *   Copyright 2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.messages;

import ome.util.messages.InternalMessage;

/**
 * A trivial copy of RegisterServiceCleanupMessage with a different name so that
 * we can catch files opened during search indexing and close them at the end of
 * parsing.
 *
 * @author Josh Ballanco, jballanc at glencoesoftware.com
 * @since 5.0.0
 */


public abstract class ParserOpenFileMessage extends InternalMessage {

    final public Object resource;

    public ParserOpenFileMessage(Object source, Object resource) {
        super(source);
        this.resource = resource;
    }

    /**
     * Used to close the passed in resource. May NOT throw an exception.
     */
    public abstract void close();
}
