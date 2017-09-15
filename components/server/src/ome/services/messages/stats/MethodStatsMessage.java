/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.messages.stats;

/**
 * Published when more than the specified number of active method calls is
 * reached.
 *
 * @see <a
 *      href="https://docs.openmicroscopy.org/latest/omero/developers/Server/Throttling.html">OMERO Throttling</a>
 */
public class MethodStatsMessage extends AbstractStatsMessage {

    private final long methods;

    public MethodStatsMessage(Object source, long methods) {
        super(source);
        this.methods = methods;
    }

    public long getMethodCount() {
        return this.methods;
    }

}
