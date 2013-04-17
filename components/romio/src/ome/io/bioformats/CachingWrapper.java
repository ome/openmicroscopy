/*
 * Copyright (C) 2013 Glencoe Software, Inc. All rights reserved.
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
package ome.io.bioformats;

import java.io.File;

import loci.formats.ChannelFiller;
import loci.formats.ChannelSeparator;
import loci.formats.IFormatReader;
import loci.formats.Memoizer;
import loci.formats.MinMaxCalculator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Standard {@ReaderWrapper} stack that is used throughout OMERO.
 * Includes the proper ordering of {@link ChannelFiller},
 * {@link ChannelSeparator}, and {@link Memoizer}.
 *
 * @see ome.formats.importer.OMEROWrapper
 */
public class CachingWrapper extends MinMaxCalculator {

    @SuppressWarnings("unused")
    private final static Logger log = LoggerFactory.getLogger(CachingWrapper.class);

    protected Memoizer memoizer;

    public CachingWrapper(int minimumElapsed, File directory)
    {
        super();
        init(minimumElapsed, directory);
    }

    public CachingWrapper(IFormatReader reader, int minimumElapsed, File directory)
    {
        super(reader);
        init(minimumElapsed, directory);
    }

    public Memoizer getMemoizer()
    {
        return memoizer;
    }

    protected void init(int minimumElapsed, File directory)
    {
        ChannelFiller filler = new ChannelFiller(reader);
        ChannelSeparator separator  = new ChannelSeparator(filler);
        memoizer = new Memoizer(separator, minimumElapsed, directory);
        reader = memoizer;

        // Force unreadable characters to be removed from metadata key/value pairs
        setMetadataFiltered(true);
        // Force images with multiple sub-resolutions to not "duplicate" their
        // series.
        setFlattenedResolutions(false);
    }

}
