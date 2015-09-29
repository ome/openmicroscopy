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

import static omero.rtypes.rstring;

import java.io.File;
import java.io.IOException;
import java.util.List;

import ome.formats.importer.ImportContainer;
import ome.services.blitz.repo.path.ClientFilePathTransformer;
import ome.services.blitz.repo.path.FilePathRestrictionInstance;
import ome.services.blitz.repo.path.FilePathRestrictions;
import ome.services.blitz.repo.path.FsFile;
import ome.services.blitz.repo.path.MakePathComponentSafe;
import omero.ServerError;
import omero.api.IQueryPrx;
import omero.api.ServiceFactoryPrx;
import omero.model.ChecksumAlgorithm;
import omero.model.IObject;
import omero.model.OriginalFile;
import omero.sys.ParametersI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FileExclusion Voter} which checks the original filepath from the client
 * ({@link omero.model.FilesetEntry#getClientPath()}) for existence.
 *
 * This check is most useful for imports from large, well-structured data directories
 * where continuously re-trying a number of imports is needed. The checksum for the
 * target file are <em>not</em> checked, meaning that modifications to the file will
 * not trigger a re-import.
 *
 * @since 5.2
 */
public class ClientPathExclusion extends AbstractFileExclusion {

    private final static Logger log = LoggerFactory.getLogger(ClientPathExclusion.class);

    public Boolean suggestExclusion(ServiceFactoryPrx factory,
            ImportContainer container) throws ServerError {

        final IQueryPrx query = factory.getQueryService();
        final String clientPath = calculateClientPath(container);

        List<IObject> files = query.findAllByQuery(
                "select o from FilesetEntry fe join fe.originalFile o "
                + "where fe.clientPath = :clientPath",
                new ParametersI().add("clientPath", rstring(clientPath)).page(0, 100));

        if (files.size() > 0) {
            log.info("ClientPath match for filename: {}", clientPath);
            for (IObject obj : files) {
                OriginalFile ofile = (OriginalFile) obj;
                log.debug("Found original file: {}", ofile.getId().getValue());
            }
            return true;
       }
       return false; // Everything fine as far as we're concerned.
    }

    private String calculateClientPath(ImportContainer container) {

        final String fullpath = container.getFile().getAbsolutePath();
        
        // Copied from ImportLibrary.java
        // TODO: allow looser sanitization according to server configuration
        final FilePathRestrictions portableRequiredRules =
                FilePathRestrictionInstance.getFilePathRestrictions(FilePathRestrictionInstance.WINDOWS_REQUIRED,
                                                                    FilePathRestrictionInstance.UNIX_REQUIRED);
        final ClientFilePathTransformer sanitizer = new ClientFilePathTransformer(new MakePathComponentSafe(portableRequiredRules));

        try {
            final FsFile fsPath = sanitizer.getFsFileFromClientFile(new File(fullpath), Integer.MAX_VALUE);
            final String clientPath = fsPath.toString();
            return clientPath;
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

}
