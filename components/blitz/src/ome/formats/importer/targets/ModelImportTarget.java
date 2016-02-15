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

import static omero.rtypes.rstring;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportContainer;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.model.IObject;
import omero.sys.ParametersI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 5.1.2
 */
public class ModelImportTarget implements ImportTarget {

    private final static Logger log = LoggerFactory.getLogger(ModelImportTarget.class);

    /**
     * Valid omero.model classes for model import target.
     */
    private static final List<String> VALID_TYPES = Arrays.asList(
            "omero.model.Dataset", "omero.model.Screen");

    /**
     * omero.model class which can be used for instantiation.
     */
    private Class<? extends IObject> type;

    /**
     * String used for querying the database; must be ome.model based.
     */
    private String typeName;

    private String simpleName;

    private String prefix;

    private Long id;

    private String discriminator;

    private String template;

    private String name;

    @SuppressWarnings("unchecked")
    @Override
    public void init(String target) {
        // Builder is responsible for only passing valid files.
        String[] tokens = target.split(":",3);
        this.prefix = tokens[0];
        if (tokens.length == 2) {
            this.name = tokens[1];
            this.discriminator = "id";
        } else {
            this.name = tokens[2];
            this.discriminator = tokens[1];
        }
        type = tryClass(prefix);
        Class<?> k = omero.util.IceMap.OMEROtoOME.get(type);
        typeName = k.getName();
        simpleName = k.getSimpleName();
        // Reversing will take us from an abstract type to one constructible.
        type = omero.util.IceMap.OMEtoOMERO.get(k);
    }

    @SuppressWarnings("unchecked")
    private Class<? extends IObject> tryClass(String prefix) {
        Class<? extends IObject> klass = null;
        try {
            klass = (Class<? extends IObject>) Class.forName(prefix);
        } catch (Exception e) {
            try {
                klass = (Class<? extends IObject>) Class.forName("omero.model."+prefix);
            } catch (Exception e1) {
                throw new RuntimeException("Unknown class:" + prefix);
            }
        }
        if (!VALID_TYPES.contains(klass.getName())) {
            throw new RuntimeException("Not a valid container class:" + klass.getName());
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
        if (discriminator.matches("^[-+%@]?name$")) {
            IObject obj;
            String order = "desc";
            if (discriminator.startsWith("-")) {
                order = "asc";
            }
            List<IObject> objs = (List<IObject>) query.findAllByQuery(
                "select o from "+simpleName+" as o where o.name = :name"
                + " order by o.id " + order,
                new ParametersI().add("name", rstring(name)));
            if (objs.size() == 0 || discriminator.startsWith("@")) {
                obj = type.newInstance();
                Method m = type.getMethod("setName", omero.RString.class);
                m.invoke(obj, rstring(name));
                obj = update.saveAndReturnObject(obj);
            } else {
                if (discriminator.startsWith("%") && objs.size() > 1) {
                    log.error("No unique {} called {}", simpleName, name);
                    throw new RuntimeException("No unique "+simpleName+" available");
                } else {
                    obj = objs.get(0);
                }
            }
            id = obj.getId().getValue();
        } else if (discriminator.equals("id")) {
            id = Long.valueOf(name);
        } else {
            log.error("Unknown discriminator {}", discriminator);
            throw new RuntimeException("Unknown discriminator "+discriminator);
        }
        return query.get(type.getSimpleName(), id);
    }
}
