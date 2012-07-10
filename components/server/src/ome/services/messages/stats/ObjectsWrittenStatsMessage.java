/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.messages.stats;


/**
 * 
 * @see <a
 *      href="https://trac.openmicroscopy.org.uk/ome/wiki/OmeroThrottling">OmeroThrottling</a>
 */
public class ObjectsWrittenStatsMessage extends
        AbstractStatsMessage {

    private final long objectsWritten;
    
    public ObjectsWrittenStatsMessage(Object source, long objectsWritten) {
        super(source);
        this.objectsWritten = objectsWritten;
    }

    public long getObjectsWritten() {
        return objectsWritten;
    }
}
