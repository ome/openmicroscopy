/*
 * Copyright (C) 2018 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.io.bioformats;

import java.io.File;

import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.Memoizer;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * While appearing to be a {@link Memoizer} this class actually wraps multiple memoizers.
 * If this instance's memoizer does not already have a memo file available on {@link #setId(String)}
 * then a copy is first taken from one of the other memoizers, preferring the earlier.
 * @author m.t.b.carroll@dundee.ac.uk
 */
public class MemoizerFallback extends Memoizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoizerFallback.class);

    private final Collection<MemoizerReadOnly> fallbacks;

    /**
     * As {@link Memoizer#Memoizer(IFormatReader, long, File)} with an additional {@code fallbacks} argument
     * that provides other memoizers from which this instance may copy a memo. Those <q>fallback</q> memoizers
     * have <em>only</em> {@link #getMemoFile(String)} called, to find their memo to copy.
     */
    public MemoizerFallback(IFormatReader reader, long minimumElapsed, File directory, Collection<MemoizerReadOnly> fallbacks) {
        super(reader, minimumElapsed, directory);
        this.fallbacks = fallbacks;
    }

    @Override
    public void setId(String id) throws FormatException, IOException {
        final File memoRW = getMemoFile(id);
        if (!(memoRW == null || memoRW.canRead())) {
            /* This memoizer would use but does not yet have a memo file for the given image. */
            for (final MemoizerReadOnly fallback : fallbacks) {
                final File memoRO = fallback.getMemoFile(id);
                if (memoRO != null && memoRO.canRead()) {
                    /* A fallback memoizer does have a memo file so we copy it. */
                    LOGGER.info("for setId({}) copying {} to {}", id, memoRO, memoRW);
                    try {
                        Files.copy(memoRO.toPath(), memoRW.toPath());
                        break;
                    } catch (IOException e) {
                        LOGGER.warn("copy failed, continuing anyway", e);
                    }
                }
            }
        }
        super.setId(id);
    }
}
