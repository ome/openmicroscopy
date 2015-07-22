/*
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

package ome.formats.importer.targets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportContainer;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.IObject;
import omero.model.Screen;
import omero.model.ScreenI;

public class ServerTemplateImportTarget extends TemplateImportTarget {

    private final String sharedPath;

    public ServerTemplateImportTarget(String sharedPath) {
        this.sharedPath = sharedPath;
    }

    @Override
    public IObject load(OMEROMetadataStoreClient client, ImportContainer ic) throws Exception {
        return load(client, ic.getIsSPW());
    }

    public IObject load(OMEROMetadataStoreClient client, boolean spw) throws Exception {

        IQueryPrx query = client.getServiceFactory().getQueryService();
        IUpdatePrx update = client.getServiceFactory().getUpdateService();

        log.info("Checking '{}' against '{}'", sharedPath, getTemplate());
        Pattern pattern = Pattern.compile(getTemplate());
        Matcher m = pattern.matcher(sharedPath);
        if (!m.matches()) {
            log.warn("No match");
            return null;
        }

        String name = m.group("C1");
        if (name == null || name.trim().length() == 0) {
            log.warn("Empty name");
            return null;
        }

        if (spw) {
            Screen screen = (Screen) query.findByString("Screen", "name", name);
            if (screen == null) {
                screen = new ScreenI();
                screen.setName(omero.rtypes.rstring(name));
                screen = (Screen) update.saveAndReturnObject(screen);
            }
            return screen;
        } else {
            Dataset dataset = (Dataset) query.findByString("Dataset", "name", name);
            if (dataset == null) {
                dataset = new DatasetI();
                dataset.setName(omero.rtypes.rstring(name));
                dataset = (Dataset) update.saveAndReturnObject(dataset);
            }
            return dataset;
        }
    }
}
