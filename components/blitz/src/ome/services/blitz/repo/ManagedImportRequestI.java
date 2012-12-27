/*
 * Copyright (C) 2012 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.blitz.repo;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.services.blitz.fire.Registry;

import omero.api.ServiceFactoryPrx;
import omero.cmd.ERR;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.OK;
import omero.cmd.Response;
import omero.grid.ImportLocation;
import omero.grid.ImportRequest;
import omero.grid.ImportResponse;
import omero.grid.ImportSettings;
import omero.model.FilesetJobLink;
import omero.model.Job;
import omero.model.MetadataImportJob;
import omero.model.Pixels;
import omero.model.ThumbnailGenerationJob;

/**
 * Wrapper around {@link FilesetJobLink} instances which need to be handled
 * on the server-side. This will primarily provide the step-location required
 * by {@link omero.cmd.Handle} by calling back to the
 * {@link ManagedImportProcessI} object.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.5.0
 */
public class ManagedImportRequestI extends ImportRequest implements IRequest {

    private static final long serialVersionUID = -303948503984L;

    private static Log log = LogFactory.getLog(ManagedImportRequestI.class);

    /**
     * Helper instance for this class. Will create a number of sub-helper
     * instances for each request.
     */
    private Helper helper;

    private final Registry reg;

    private final FilesetJobLink link;

    private final ImportSettings settings;

    private final CheckedPath checkedPath;

    private final Ice.Current current;

    public ManagedImportRequestI(Registry reg, ImportSettings settings,
            CheckedPath checkedPath, FilesetJobLink link, Ice.Current current) {
        this.reg = reg;
        this.settings = settings;
        this.checkedPath = checkedPath;
        this.link = link;
        this.current = current;
    }

    //
    // IRequest methods
    //

    public Map<String, String> getCallContext() {
        return null;
    }

    public void init(Helper helper) {
        this.helper = helper;
        helper.setSteps(1);
    }

    public Object step(int step) {
        helper.assertStep(step);
        try {
            Job j = link.getChild();
            if (j == null) {
                throw helper.cancel(new ERR(), null, "null-job");
            } else if (j instanceof MetadataImportJob) {
                return importMetadata();
            } else if (j instanceof ThumbnailGenerationJob) {
                throw helper.cancel(new ERR(), null, "NYI");
            } else {
                throw helper.cancel(new ERR(), null, "unknown-job-type",
                        "job-type", j.ice_id());
            }
        } catch (Throwable t) {
            throw helper.cancel(new ERR(), t, "import-request-failure");
        }
    }

    @SuppressWarnings("unchecked")
    public void buildResponse(int step, Object object) {
        helper.assertResponse(step);
        if (object instanceof List) {
            helper.setResponseIfNull(new ImportResponse((List<Pixels>) object));
        } else {
            helper.setResponseIfNull(new OK());
        }
    }

    public Response getResponse() {
        return helper.getResponse();
    }

    //
    // ACTIONS
    //


    /** Now an internal, trusted method */
    public List<Pixels> importMetadata() throws Throwable {

        ServiceFactoryPrx sf = null;
        OMEROMetadataStoreClient store = null;
        OMEROWrapper reader = null;
        List<Pixels> pix = null;
        try {
            final ImportConfig config = new ImportConfig();
            final String sessionUuid = current.ctx.get(omero.constants.SESSIONUUID.value);
            final String clientUuid = UUID.randomUUID().toString();

            sf = reg.getInternalServiceFactory(
                    sessionUuid, "unused", 3, 1, clientUuid);
            reader = new OMEROWrapper(config);
            store = new OMEROMetadataStoreClient();
            store.initialize(sf);
            ImportLibrary library = new ImportLibrary(store, reader);
            pix = library.importImageInternal(settings, 0, 0, 1, checkedPath.file);
        }
        finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Throwable e){
                log.error(e);
            }
            try {
                if (store != null) {
                    store.logout();
                }
            } catch (Throwable e) {
                log.error(e);
            }
            try {
                if (sf != null) {
                    sf.destroy();
                }
            } catch (Throwable e) {
                log.error(e);
            }
        }
        return pix;
    }

}
