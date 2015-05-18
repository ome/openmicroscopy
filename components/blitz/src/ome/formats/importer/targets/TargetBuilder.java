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

import omero.model.IObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @since 5.1.2
 */
public class TargetBuilder {

    private final static Logger log = LoggerFactory.getLogger(TargetBuilder.class);

    private String target;

    private Class<? extends ImportTarget> type;

    @SuppressWarnings("unchecked")
    public TargetBuilder parse(String string) {
        if (target != null) {
            throw new IllegalStateException(String.format(
                    "Only one target supported: old=%s new=%s",
                    this.target, string));
        }
        target = string;
        int firstColon = string.indexOf(":");
        if (firstColon >= 0) {
            String prefix = string.substring(0, firstColon);
            Class<?> klass = tryClass(prefix);
            if (klass != null) {
                if (ImportTarget.class.isAssignableFrom(klass)) {
                    type = (Class<ImportTarget>) klass;
                    return this;
                } else if (IObject.class.isAssignableFrom(klass)) {
                    type = (Class<? extends ImportTarget>) ModelImportTarget.class;
                    return this;
                }
            }
            klass = tryClass("omero.model." + prefix);
            if (klass != null) {
                if (IObject.class.isAssignableFrom(klass)) {
                    type = (Class<? extends ImportTarget>) ModelImportTarget.class;
                    return this;
                }
            }

            if ("abspath".equals(prefix)) {
                type = AbspathImportTarget.class;
                return this;
            }
        }

        // Handles everything else.
        type = RelpathImportTarget.class;
        return this;
    }

   /**
    */
    public ImportTarget build() {
        ImportTarget inst = null;
        try {
            inst = type.newInstance();
        } catch (Exception e) {
            log.warn("Failed to instantiate for: {}", target, e);
            throw new RuntimeException("Could not create ImportTarget: " + target);
        }
        inst.init(target);
        return inst;
    }

    protected Class<?> tryClass(String string) {
        try {
            return Class.forName(string);
        } catch (ClassNotFoundException e) {
            log.debug(e.getMessage());
            return null;
        }
    }

}