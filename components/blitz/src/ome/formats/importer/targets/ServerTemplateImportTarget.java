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

import static omero.rtypes.rstring;

import java.util.List;
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
import omero.sys.ParametersI;

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

        if (!getDiscriminator().matches("^[-+%@]?name$")) {
            log.warn("Invalid discriminator: {}", getDiscriminator());
            return null;
        }

        String name = m.group("Container1");
        if (name == null || name.trim().length() == 0) {
            log.warn("Empty name");
            return null;
        }

        String order = "desc";
        if (getDiscriminator().startsWith("-")) {
            order = "asc";
        }
        if (spw) {
            Screen screen;
            List<IObject> screens = (List<IObject>) query.findAllByQuery(
                "select o from Screen as o where o.name = :name"
                + " order by o.id " + order,
                new ParametersI().add("name", rstring(name)));
            if (screens.size() == 0 || getDiscriminator().startsWith("@")) {
                screen = new ScreenI();
                screen.setName(omero.rtypes.rstring(name));
                screen = (Screen) update.saveAndReturnObject(screen);
            } else {
                if (getDiscriminator().startsWith("%") && screens.size() > 1) {
                    log.warn("No unique Screen called {}", name);
                    return null;
                } else {
                    screen = (Screen) screens.get(0);
                }
            }
            return screen;
        } else {
            Dataset dataset;
            List<IObject> datasets = (List<IObject>) query.findAllByQuery(
                "select o from Dataset as o where o.name = :name"
                + " order by o.id " + order,
                new ParametersI().add("name", rstring(name)));
            if (datasets.size() == 0 || getDiscriminator().startsWith("@")) {
                dataset = new DatasetI();
                dataset.setName(omero.rtypes.rstring(name));
                dataset = (Dataset) update.saveAndReturnObject(dataset);
            } else {
                if (getDiscriminator().startsWith("%") && datasets.size() > 1) {
                    log.warn("No unique Dataset called {}", name);
                    return null;
                } else {
                    dataset = (Dataset) datasets.get(0);
                }
            }
            return dataset;
        }
    }
}
