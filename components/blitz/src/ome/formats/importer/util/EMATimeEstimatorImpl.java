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

import java.util.Iterator;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.commons.lang.time.StopWatch;

/**
 * Class implementing the {@link TimeEstimator} interface. Uses the Exponential
 * Moving Average equation to provide an estimate of the remaining upload time
 * of binary data. A correction factor is used for minimal overestimation.
 *
 * @author Blazej Pindelski, bpindelski at dundee.ac.uk
 * @since 5.0
 */
public class EMATimeEstimatorImpl implements TimeEstimator {

    private static int DEFAULT_BUFFER_SIZE = 10;

    private static long MILLISECOND_CORRECTION = 500;

    private long imageContainerSize = 0;

    private float alpha = 0, chunkTime = 0;

    private Buffer timeSamples;

    private StopWatch sw;

    public EMATimeEstimatorImpl(long imageContainerSize) {
        this(imageContainerSize, DEFAULT_BUFFER_SIZE);
    }

    public EMATimeEstimatorImpl(long imageContainerSize, int sampleSize) {
        timeSamples = new CircularFifoBuffer(sampleSize);
        sw = new StopWatch();
        this.imageContainerSize = imageContainerSize;
    }

    public void start() {
        sw.reset();
        sw.start();
    }

    public void stop() {
        sw.stop();
        timeSamples.add(sw.getTime());
        alpha = 2f / (timeSamples.size() + 1);
    }

    public long getUploadTimeLeft(long uploadedBytes) {
        imageContainerSize -= uploadedBytes;
        Iterator<Long> i = timeSamples.iterator();
        while (i.hasNext()) {
            chunkTime = alpha * i.next() + (1 - alpha) * chunkTime;
        }
        return (long) chunkTime * (imageContainerSize / uploadedBytes)
                + MILLISECOND_CORRECTION;
    }

}
