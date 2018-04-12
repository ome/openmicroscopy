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

import loci.formats.IFormatReader;
import loci.formats.Memoizer;

/**
 * Adapts {@link Memoizer} to tolerate cache directories that it may not write.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.4.6
 */
public class MemoizerReadOnly extends Memoizer {

    public MemoizerReadOnly(IFormatReader reader, long minimumElapsed, File directory) {
        super(reader, minimumElapsed, directory);
    }

    /**
     * {@inheritDoc}
     * n.b.: Overridden to test only read access, <strong>not</strong> write access.
     */
    @Override
    protected boolean isWritableDirectory(File writeDirectory) {
        return writeDirectory.canRead() && writeDirectory.isDirectory();
      }
}
