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

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.buffer.CircularFifoBuffer;

public class TimeEstimatorImpl implements TimeEstimator {

    private long start, stop, imageContainerSize, imageContainerUploadedBytes;

    private Buffer timeSamples;

    private float alpha, chunkTime;

    public TimeEstimatorImpl(long imageContainerSize, int sampleSize) {
        timeSamples = new CircularFifoBuffer(sampleSize);
        this.imageContainerSize = imageContainerSize;
    }

    public void start() {
        // Due to weirdness with System.nanoTime() on multi-core
        // CPUs, falling back to currentTimeMillis()
        start = System.currentTimeMillis();
    }

    public void stop() {
        if (start == 0) {
            throw new IllegalStateException("Calling stop() before start().");
        }
        stop = System.currentTimeMillis();
        timeSamples.add(stop - start);
        alpha = 2f / (timeSamples.size() + 1);
    }

    public long getUploadTimeLeft(long uploadedChunk, long uploadedBytes) {
        if (uploadedChunk == 0) {
            return 0;
        }
        imageContainerUploadedBytes += uploadedChunk;
        for (int i = 0; i < timeSamples.size(); i++) {
            chunkTime = alpha * (Long) timeSamples.get() + (1 - alpha)
                    * chunkTime;
        }
        return (long) chunkTime
                * ((imageContainerSize - imageContainerUploadedBytes)
                        / uploadedChunk);
    }

}
