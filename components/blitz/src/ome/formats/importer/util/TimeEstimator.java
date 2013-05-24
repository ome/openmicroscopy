/*
 * Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
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


package ome.formats.importer.util;

/**
 * Estimates the time left (ETA) during image resource upload to the server.
 * Uses a exponential moving average as the calculation algorithm. Internally,
 * the object keeps track of previous estimations in a circular buffer.
 *
 * The user of this API is responsible for instantiating a new object every
 * time the state has to be reset.
 *
 * @author Blazej Pindelski, bpindelski at dundee.ac.uk
 * @since 5.0
 */
public interface TimeEstimator {

    /**
     * Starts the measurement with a specific time value.
     *
     * @param startTime The start time.
     */
    void start(long mesurementStart);

    /**
     * Stops the measurement at the provided stop time.
     *
     * @param stopTime The end time.
     */
    void stop(long measurementStop);

    /**
     * Return the estimated time left based on the calls to
     * {@link TimeEstimator#start(long) start} and
     * {@link TimeEstimator#stop(long) stop} methods.
     *
     * @return The estimated time remaining.
     */
    long getUploadTimeLeft();
}
