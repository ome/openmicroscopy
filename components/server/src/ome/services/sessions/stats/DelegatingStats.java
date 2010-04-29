/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sessions.stats;



/**
 * Delegates to a {@link SessionStats} which is acquired from {@link #stats()}.
 * Also intended for subclassing.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4
 */
public class DelegatingStats implements SessionStats {

    private final SessionStats[] stats;
    
    public DelegatingStats() {
        this.stats = new SessionStats[0];
    }
    
    public DelegatingStats(SessionStats[] stats) {
        if (stats == null) {
            this.stats = new SessionStats[0];
        } else {
            this.stats = new SessionStats[stats.length];
            System.arraycopy(stats, 0, this.stats, 0, stats.length);
        }
    }
    
    /**
     * Intended to be overwritten by subclasses.
     */
    protected SessionStats[] stats() {
        return stats;
    }

    public void methodIn() {
        for (SessionStats stats : stats()) {
            stats.methodIn();
        }
    }

    public long methodCount() {
        long count = 0;
        for (SessionStats stats : stats()) {
            count = Math.max(count, stats.methodCount());
        }
        return count;
    }

    public void methodOut() {
        for (SessionStats stats : stats()) {
            stats.methodOut();
        }
    }

    public final void loadedObjects(int objects) {
        for (SessionStats stats : stats()) {
            stats.loadedObjects(objects);
        }
    }

    public final void readBytes(int bytes) {
        for (SessionStats stats : stats()) {
            stats.readBytes(bytes);
        }
    }

    public final void updatedObjects(int objects) {
        for (SessionStats stats : stats()) {
            stats.updatedObjects(objects);
        }
    }

    public final void writtenBytes(int bytes) {
        for (SessionStats stats : stats()) {
            stats.writtenBytes(bytes);
        }
    }

}
