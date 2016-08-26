/*
 * org.openmicroscopy.shoola.env.data.views.calls.OfflineImagesImporter
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.data.views.calls;

import com.google.gson.Gson;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.*;
import org.openmicroscopy.shoola.env.data.model.ImportRequestData;
import org.openmicroscopy.shoola.env.data.model.ImportableFile;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import org.openmicroscopy.shoola.svc.SvcRegistry;
import org.openmicroscopy.shoola.svc.communicator.Communicator;
import org.openmicroscopy.shoola.svc.communicator.CommunicatorDescriptor;
import org.openmicroscopy.shoola.svc.transport.HttpChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Offloads image imports to the OMERO Import Proxy.
 */
class OfflineImagesImporter extends BatchCallTree {

    private static String createImportSession(Registry context) {
        try {
            OmeroSessionService service = new OmeroSessionServiceImpl(context);
            return service.createOfflineImportSession();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The object hosting the information for the import.
     */
    private final ImportableObject target;

    /**
     * Map of result, key is the file to import, value is an object or a
     * string.
     */
    private final Map<ImportableFile, Object> partialResult;

    private final String importSessionKey;

    /**
     * Package private constructor only to be called by the {@link
     * ImagesImporter}'s factory method.
     * @param target the files to import.
     */
    OfflineImagesImporter(ImportableObject target) {
        this.target = target;
        this.partialResult = new HashMap<>();
        this.importSessionKey = createImportSession(context);
    }

    private boolean prepareImport(ImportableFile importData, boolean close) {
        try {
            //code not ready for sudo operation
            //check creation of tags and containers
            OmeroImageService os = context.getImageService();
            Object outcome = os.importFile(target, importData, close);

            if (outcome instanceof ImportException) {
                partialResult.put(importData, outcome);
                return false;
            }
            if (outcome instanceof Boolean) {
                Boolean b = (Boolean) outcome;
                if (!b || importData.getStatus().isMarkedAsDuplicate()) {
                    partialResult.put(importData, outcome);
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            partialResult.put(importData, e);
            return false;
        }
    }

    private ImportRequestData[] buildImportBatchRequest(
            List<ImportableFile> batch) {
        List<ImportRequestData> data = new ArrayList<>();
        for (ImportableFile importData : batch) {
            OfflineImportRequestBuilder builder =
                    new OfflineImportRequestBuilder(context, target, importData);
            data.add(builder.buildRequest(importSessionKey));
        }
        return data.toArray(new ImportRequestData[0]);
    }

    private void notifySubmissionError(List<ImportableFile> batch, Exception e) {
        for (ImportableFile f : batch) {
            partialResult.put(f, e);
        }
    }

    private void notifySubmissionSuccess(List<ImportableFile> batch) {
        for (ImportableFile f : batch) {
            f.getStatus().markedAsOffLineImport();
            partialResult.put(f, true);
        }
    }

    private void submit(List<ImportableFile> batch) {
        try {
            ImportRequestData[] data = buildImportBatchRequest(batch);
            String tokenURL = (String)
                    context.lookup(LookupNames.OFFLINE_IMPORT_URL);
            CommunicatorDescriptor desc = new CommunicatorDescriptor
                    (HttpChannel.CONNECTION_PER_REQUEST, tokenURL, -1);
            Communicator c = SvcRegistry.getCommunicator(desc);

            //Prepare json string
            Gson writer = new Gson();
            c.enqueueImport(writer.toJson(data), new StringBuilder());

            notifySubmissionSuccess(batch);
        } catch (Exception e) {
            notifySubmissionError(batch, e);
        }
    }

    private void enqueueImportBatch() {
        List<ImportableFile> batchToSubmit = new ArrayList<>();
        ImportableFile[] importData =
                target.getFiles().toArray(new ImportableFile[0]);

        for (int k = 0; k < importData.length; ++k) {
            boolean isLast = k == importData.length - 1;
            boolean succeeded = prepareImport(importData[k], isLast);
            if (succeeded) {
                batchToSubmit.add(importData[k]);
            }
        }
        submit(batchToSubmit);
    }

    @Override
    protected void buildTree() {
        add(new BatchCall("Importing files") {
            public void doCall() { enqueueImportBatch(); }
        });
    }

    @Override
    protected Object getPartialResult() {
        return partialResult;
    }

    @Override
    protected Object getResult() {
        return null;
    }

}
