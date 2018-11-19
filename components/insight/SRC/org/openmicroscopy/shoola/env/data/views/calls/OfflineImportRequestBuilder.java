/*
 * org.openmicroscopy.shoola.env.data.views.calls.OfflineImportRequestBuilder
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

import omero.gateway.model.DataObject;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.TagAnnotationData;
import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.AdminService;
import org.openmicroscopy.shoola.env.data.model.ImportRequestData;
import org.openmicroscopy.shoola.env.data.model.ImportableFile;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Builds an {@link ImportRequestData} object to submit to the OMERO Import
 * Proxy.
 */
class OfflineImportRequestBuilder {

    private final Registry context;
    private final ImportableObject importContainer;
    private final ImportableFile importData;

    OfflineImportRequestBuilder(Registry context,
                                ImportableObject importContainer,
                                ImportableFile importData) {
        if (context == null) throw new NullPointerException("No registry.");
        if (importContainer == null) throw new NullPointerException("No importContainer.");
        if (importData == null) throw new NullPointerException("No importData.");

        this.context = context;
        this.importContainer = importContainer;
        this.importData = importData;
    }

    private void fillInUserAndServer(ImportRequestData data) {
        AdminService svc = context.getAdminService();
        ExperimenterData exp = importData.getUser();
        if (exp == null) {
            exp = svc.getUserDetails();
        }
        data.experimenterEmail = exp.getEmail();
        data.omeroHost = svc.getServerName();
        if (svc.getPort() > 0) {
            data.omeroPort = "" + svc.getPort();
        }
    }

    private void mapImportData(ImportRequestData data) {
        data.targetUri = importData.getOriginalFile().getAbsolutePath();
        DataObject target = importData.getDataset();
        if (target != null && target.getId() > 0) {
            data.datasetId = "" + target.getId();
        }
        target = importData.getParent();
        if (target != null) {
            if (target instanceof ScreenData) {
                data.screenId = "" + target.getId();
            }
        }
        Collection<TagAnnotationData> tags = importContainer.getTags();
        if (CollectionUtils.isNotEmpty(tags)) {
            List<String> ids = new ArrayList<>();
            for (TagAnnotationData tag : tags) {
                if (tag.getId() > 0) {
                    ids.add("" + tag.getId());
                }
            }
            data.annotationIds = ids.toArray(new String[ids.size()]);
        }
    }

    ImportRequestData buildRequest(String sessionKey) {
        ImportRequestData data = new ImportRequestData();
        fillInUserAndServer(data);
        mapImportData(data);
        data.sessionKey = sessionKey;

        return data;
    }

}
