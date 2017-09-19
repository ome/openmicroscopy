/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.messages.stats;

import ome.util.messages.InternalMessage;

/**
 * Published when some limit in user/group/session activity has been reached.
 * For example, when a single thread has attempted to load a pre-defined number
 * of objects (10,000; 100,000) a message can be raised which can then be used
 * by other subsystems to slow down, or "throttle", execution.
 * 
 * @see <a
 *      href="https://docs.openmicroscopy.org/latest/omero/developers/Server/Throttling.html">OMERO Throttling</a>
 */
public abstract class AbstractStatsMessage extends InternalMessage {

    /**
     * By default, a message raised is "hard" in that the limit will cause
     * an exception to be thrown.
     */
    private final boolean hard;
    
    public AbstractStatsMessage(Object source) {
        this(source, true);
    }
    
    public AbstractStatsMessage(Object source, boolean hard) {
        super(source);
        this.hard = hard;
    }
    
    public boolean isHard() {
        return hard;
    }

}
