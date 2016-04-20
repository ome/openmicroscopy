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

package omero.gateway.facility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import omero.RType;
import omero.ServerError;
import omero.api.IQueryPrx;
import omero.api.RawFileStorePrx;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.ImageData;
import omero.model.Fileset;
import omero.model.FilesetEntry;
import omero.model.OriginalFile;
import omero.sys.ParametersI;

import org.apache.commons.collections.CollectionUtils;

/**
 * Encapsulates some functionality needed by the {@link TransferFacility}
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class TransferFacilityHelper {

    /** Maximum size of bytes read at once. */
    private static final int INC = 262144;//

    private BrowseFacility browse;

    private Gateway gateway;

    /**
     * Creates a new instance.
     *
     * @param gateway
     *            Reference to the gateway.
     * @param datamanager
     *            Reference to the manager facility.
     * @param parent
     *            Reference to the parent.
     * @throws ExecutionException
     */
    TransferFacilityHelper(Gateway gateway) throws ExecutionException {
        this.gateway = gateway;
        this.browse = gateway.getFacility(BrowseFacility.class);
    }

    /**
     * Downloads the original file of an image from the server.
     *
     * @param context
     *            The security context.
     * @param targetPath
     *            Path to the file.
     * @param imageId
     *            The identifier of the image.
     * @return See above
     * @throws DSOutOfServiceException
     * @throws DSAccessException
     */
    List<File> downloadImage(SecurityContext context, String targetPath,
            long imageId) throws DSAccessException, DSOutOfServiceException {
        List<File> files = new ArrayList<File>();

        ImageData image = browse.findObject(context, ImageData.class, imageId,
                true);

        String query;
        List<?> filesets;
        try {
            IQueryPrx service = gateway.getQueryService(context);
            ParametersI param = new ParametersI();
            long id;
            if (image.isFSImage()) {
                id = image.getId();
                List<RType> l = new ArrayList<RType>();
                l.add(omero.rtypes.rlong(id));
                param.add("imageIds", omero.rtypes.rlist(l));
                query = createFileSetQuery();
            } else {// Prior to FS
                if (image.isArchived()) {
                    StringBuffer buffer = new StringBuffer();
                    id = image.getDefaultPixels().getId();
                    buffer.append("select ofile from OriginalFile as ofile ");
                    buffer.append("join fetch ofile.hasher ");
                    buffer.append("left join ofile.pixelsFileMaps as pfm ");
                    buffer.append("left join pfm.child as child ");
                    buffer.append("where child.id = :id");
                    param.map.put("id", omero.rtypes.rlong(id));
                    query = buffer.toString();
                } else
                    return null;
            }
            filesets = service.findAllByQuery(query, param);
        } catch (Exception e) {
            throw new DSAccessException("Cannot retrieve original file", e);
        }

        if (CollectionUtils.isEmpty(filesets))
            return files;
        Iterator<?> i;
        List<OriginalFile> values = new ArrayList<OriginalFile>();
        if (image.isFSImage()) {
            i = filesets.iterator();
            Fileset set;
            List<FilesetEntry> entries;
            Iterator<FilesetEntry> j;
            while (i.hasNext()) {
                set = (Fileset) i.next();
                entries = set.copyUsedFiles();
                j = entries.iterator();
                while (j.hasNext()) {
                    FilesetEntry fs = j.next();
                    values.add(fs.getOriginalFile());
                }
            }
        } else
            values.addAll((List<OriginalFile>) filesets);

        RawFileStorePrx store = null;
        OriginalFile of;
        long size;
        FileOutputStream stream = null;
        long offset = 0;
        i = values.iterator();
        File f = null;

        while (i.hasNext()) {
            of = (OriginalFile) i.next();

            try {
                store = gateway.getRawFileService(context);
                store.setFileId(of.getId().getValue());

                f = new File(targetPath, of.getName().getValue());
                files.add(f);

                stream = new FileOutputStream(f);
                size = of.getSize().getValue();
                try {
                    try {
                        for (offset = 0; (offset + INC) < size;) {
                            stream.write(store.read(offset, INC));
                            offset += INC;
                        }
                    } finally {
                        stream.write(store.read(offset, (int) (size - offset)));
                        stream.close();
                    }
                } catch (Exception e) {
                    if (stream != null)
                        stream.close();
                    if (f != null) {
                        f.delete();
                        files.remove(f);
                    }
                }
            } catch (IOException e) {
                if (f != null) {
                    f.delete();
                    files.remove(f);
                }
                throw new DSAccessException("Cannot create file in folderPath",
                        e);
            } catch (Throwable t) {
                throw new DSAccessException("ServerError on retrieveArchived",
                        t);
            } finally {
                try {
                    store.close();
                } catch (ServerError e) {
                }
            }
        }

        return files;
    }

    /**
     * Creates the query to load the file set corresponding to a given image.
     *
     * @return See above.
     */
    private String createFileSetQuery() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("select fs from Fileset as fs ");
        buffer.append("join fetch fs.images as image ");
        buffer.append("left outer join fetch fs.usedFiles as usedFile ");
        buffer.append("join fetch usedFile.originalFile as f ");
        buffer.append("join fetch f.hasher ");
        buffer.append("where image.id in (:imageIds)");
        return buffer.toString();
    }
}
