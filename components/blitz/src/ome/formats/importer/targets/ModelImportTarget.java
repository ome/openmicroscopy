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

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportContainer;
import omero.model.IObject;


/**
 * @since 5.1.2
 */
public class ModelImportTarget implements ImportTarget {

    private Class<? extends IObject> type;

    private Long id;

    @Override
    public void init(String target) {
        
    }

    public Class<? extends IObject> getObjectType() {
        return type;
    }

    public Long getObjectId() {
        return id;
    }

    @Override
    public IObject load(OMEROMetadataStoreClient client, ImportContainer ic) {
        // TODO Auto-generated method stub
        return null;
    }

}
