/*
 * Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
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

package ome.formats.importer.exclusions;

import ome.formats.importer.transfers.AbstractFileTransfer.Transfers;
import ome.formats.importer.transfers.FileTransfer;
import ome.services.blitz.util.ChecksumAlgorithmMapper;
import ome.util.checksum.ChecksumProvider;
import ome.util.checksum.ChecksumProviderFactory;
import ome.util.checksum.ChecksumProviderFactoryImpl;
import omero.model.ChecksumAlgorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base {@link FileExclusion} implementation primarily providing a factory
 * for {@link FileExclusion} implementations via {@link #createExclusion(String)}.
 *
 * @since 5.1
 */
public abstract class AbstractFileExclusion implements FileExclusion {

    /**
     * Enum of well-known {@link FileExclusion} names.
     */
    public enum Exclusion {
        filename(FilenameExclusion.class),
        clientpath(ClientPathExclusion.class);
        Class<?> kls;
        Exclusion(Class<?> kls) {
            this.kls = kls;
        }
    }

    /**
     * Factory method for instantiating {@link FileTransfer} objects from
     * a string. Supported values can be found in the {@link Transfers} enum.
     * Otherwise, a FQN for a class on the classpath should be passed in.
     * @param arg non-null
     */
    public static FileExclusion createExclusion(String arg) {
        Logger tmp = LoggerFactory.getLogger(AbstractFileExclusion.class);
        tmp.debug("Loading file exclusion class {}", arg);
        try {
            try {
                return (FileExclusion) Exclusion.valueOf(arg).kls.newInstance();
            } catch (Exception e) {
                // Assume not in the enum
            }
            Class<?> c = Class.forName(arg);
            return (FileExclusion) c.newInstance();
        } catch (Exception e) {
            tmp.error("Failed to load file exclusion class " + arg);
            throw new RuntimeException(e);
        }
    }

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected String checksum(String filename, ChecksumAlgorithm checksumAlgorithm) {
        final ChecksumProviderFactory checksumProviderFactory = new ChecksumProviderFactoryImpl();
        final ChecksumProvider cp = checksumProviderFactory.getProvider(
                ChecksumAlgorithmMapper.getChecksumType(checksumAlgorithm));
        cp.putFile(filename);
        return cp.checksumAsString();
    }

}
