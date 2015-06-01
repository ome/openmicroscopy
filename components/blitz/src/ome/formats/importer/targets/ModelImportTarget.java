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

package ome.formats.importer.targets;

import java.lang.reflect.Method;

import static omero.rtypes.rstring;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportContainer;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.model.IObject;


/**
 * @since 5.1.2
 */
public class ModelImportTarget implements ImportTarget {

    private Class<? extends IObject> type;

    private String prefix; 

    private String rest;

    private Long id;


    @Override
    public void init(String target) {
        // Builder is responsible for only passing valid files.
        int idx = target.indexOf(":");
        prefix = target.substring(0, idx);
        rest = target.substring(idx + 1);
        type = tryClass(prefix);
    }

    @SuppressWarnings("unchecked")
    private Class<? extends IObject> tryClass(String prefix) {
        Class<? extends IObject> klass = null;
        try {
            klass = (Class<? extends IObject>) Class.forName(prefix);
        } catch (ClassNotFoundException e) {
            try {
                klass = (Class<? extends IObject>) Class.forName("omero.model."+prefix);
            } catch (ClassNotFoundException e1) {
                throw new RuntimeException("Unknown class:" + prefix);
            }
        }
        return klass;
    }

    public Class<? extends IObject> getObjectType() {
        return type;
    }

    public Long getObjectId() {
        return id;
    }

    @Override
    public IObject load(OMEROMetadataStoreClient client, ImportContainer ic) throws Exception {
        IQueryPrx query = client.getServiceFactory().getQueryService();
        IUpdatePrx update = client.getServiceFactory().getUpdateService();
        if (rest.startsWith("name:")) {
            String name = rest.substring(5);
            IObject obj = query.findByString(type.getClass().getSimpleName(),
                "name", name);
            if (obj == null) {
                obj = type.newInstance();
                Method m = type.getMethod("setName", omero.RString.class);
                m.invoke(obj, rstring(name));
                obj = update.saveAndReturnObject(obj);
            }
            id = obj.getId().getValue();
        }
        return null;
    }

}
