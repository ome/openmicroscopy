/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
package omero.cmd;


/**
 * SPIOrthogonal interface hierarchy of types for working with the
 * {@link omero.cmd.Request} hierarchy.
 *
 * @since Beta4.3.2
 */
public class HandleFactory {

    private final long cancelTimeoutMs;

    public HandleFactory(long cancelTimeoutMs) {
        this.cancelTimeoutMs = cancelTimeoutMs;
    }

    public IHandle createHandle(Request request) {
        return null;
    }

}
