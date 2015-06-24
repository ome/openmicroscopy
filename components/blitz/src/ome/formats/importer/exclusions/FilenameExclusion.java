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

import java.util.List;

import ome.formats.importer.ImportContainer;
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
 * {@link FileExclusion Voter} which checks the filename (not full path) of a
 * given {@link java.io.File} along with the checksum to detect duplicates. If
 * either the checksum or the checksum algorithm are null, then no detection
 * will be attempted, i.e. the voter will return a null to abstain.
 *
 * @since 5.1
 */
public class FilenameExclusion extends AbstractFileExclusion {

    private final static Logger log = LoggerFactory.getLogger(FilenameExclusion.class);

    public Boolean suggestExclusion(ServiceFactoryPrx factory,
            ImportContainer container) throws ServerError {
        IQueryPrx query = factory.getQueryService();
        String fullpath = container.getFile().getAbsolutePath();
        String filename = container.getFile().getName();
        List<IObject> files = query.findAllByQuery(
                "select o from OriginalFile o "
                + "join fetch o.hasher "
                + "where o.name = :name",
                new ParametersI().add("name", rstring(filename)));
       for (IObject obj : files) {
           OriginalFile ofile = (OriginalFile) obj;
           log.debug("Found original file: {}", ofile.getId().getValue());
           ChecksumAlgorithm algo = ofile.getHasher();
           String chksm = ofile.getHash() == null ? null : ofile.getHash().getValue();
           if (algo == null) {
               log.debug("No hasher: no vote");
               return null;
           } else if (chksm == null) {
               log.debug("No hash: no vote");
               return null;
           } else {
               String checksum = checksum(fullpath, algo);
               if (checksum == null) {
                   log.debug("Null checksum: no vote");
               } else {
                   if (checksum.equals(chksm)) {
                       log.info("Checksum match for filename: {}", filename);
                       return true;
                   }
               }
           }
       }
       return false; // Everything fine as far as we're concerned.
    }

}