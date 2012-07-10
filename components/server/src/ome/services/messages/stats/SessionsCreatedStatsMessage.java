/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.messages.stats;


/**
 * Published when some limit in user/group/session activity has been reached.
 * For example, when a single thread has attempted to load a pre-defined number
 * of objects (10,000; 100,000) a message can be raised which can then be used
 * by other subsystems to slow down, or "throttle", execution.
 * 
 * @see <a
 *      href="http://trac.openmicroscopy.org.uk/ome/wiki/OmeroThrottling">OmeroThrottling</a>
 */
public class SessionsCreatedStatsMessage extends
        AbstractStatsMessage {

    private final long sessionsCreated;
    
    public SessionsCreatedStatsMessage(Object source, long sessionsCreated) {
        super(source);
        this.sessionsCreated = sessionsCreated;
    }

}
