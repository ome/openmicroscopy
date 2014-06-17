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
 * Thin wrapper around {@link com.codahale.metrics.Snapshot}
 */
public class DefaultSnapshot implements Snapshot {

    private final com.codahale.metrics.Snapshot s;

    public DefaultSnapshot(com.codahale.metrics.Snapshot s) {
        this.s = s;
    }

    public double get75thPercentile() {
        return s.get75thPercentile();
    }

    public double get95thPercentile() {
        return s.get95thPercentile();
    }

    public double get98thPercentile() {
        return s.get98thPercentile();
    }

    public double get999thPercentile() {
        return s.get999thPercentile();
    }

    public double get99thPercentile() {
        return s.get99thPercentile();
    }

    public long getMax() {
        return s.getMax();
    }

    public double getMean() {
        return s.getMean();
    }

    public double getMedian() {
        return s.getMedian();
    }

    public long getMin() {
        return s.getMin();
    }

    public double getStdDev() {
        return s.getStdDev();
    }

    public double getValue(double quantile) {
        return s.getValue(quantile);
    }

    public long[] getValues() {
        return s.getValues();
    }

    public int size() {
        return s.size();
    }

}
