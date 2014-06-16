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
 * No-op version of {@link Metrics}
 */
public class NullMetrics extends Metrics {
    
    private static class NullHistogram extends Histogram {

        public NullHistogram() {
            super(null);
        }

        @Override
        public void update(int done) {
            // no-op
        }
    }

    private static class NullTimerContext extends Timer.Context {

        public NullTimerContext() {
            super(null);
        }

        @Override
        public long stop() {
            return -1;
        }
    }

    private static class NullTimer extends Timer {

        private final NullTimerContext c;

        public NullTimer(NullTimerContext c) {
            super(null);
            this.c = c;
        }

        @Override
        public Context time() {
            return c;
        }
    }

    private static class NullCounter extends Counter {
        
        public NullCounter() {
            super(null);
        }
        
        @Override
        public void inc() {
            // no-op
        }

        public void dec() {
            // no-op
        }

        @Override
        public long getCount() {
            return -1l;
        }
    }

    private final static NullTimerContext X = new NullTimerContext();

    private final static NullHistogram H = new NullHistogram();

    private final static NullCounter C = new NullCounter();

    private final static NullTimer T = new NullTimer(X);

    @Override
    public Histogram histogram(Object obj, String name) {
        return H;
    }

    @Override
    public Timer timer(Object obj, String name) {
        return T;
    }

    @Override
    public Counter counter(Object obj, String name) {
        return C;
    }

}
