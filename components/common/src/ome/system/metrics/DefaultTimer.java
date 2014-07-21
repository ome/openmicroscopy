/*
 * Copyright (C) 2014 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.system.metrics;


/**
 * Thin wrapper around {@link com.codahale.metrics.Timer}
 */
public class DefaultTimer implements Timer {

    /**
     * @see com.codahale.metrics.Timer.Context
     */
    public static class Context implements Timer.Context {

        private final com.codahale.metrics.Timer.Context c;

        public Context(com.codahale.metrics.Timer.Context c) {
            this.c = c;
        }

        public long stop() {
            return this.c.stop();
        }

    }

    private final com.codahale.metrics.Timer t;

    public DefaultTimer(com.codahale.metrics.Timer t) {
        this.t = t;
    }

    /**
     * @see com.codahale.metrics.Timer#time
     */
    public Timer.Context time() {
        return new Context(t.time());
    }

    /**
     * @see com.codahale.metrics.Timer#getCount()
     */
    public long getCount() {
        return this.t.getCount();
    }
}
