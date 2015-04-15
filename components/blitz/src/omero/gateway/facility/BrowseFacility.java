/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package omero.gateway.facility;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import omero.api.IContainerPrx;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSOutOfServiceException;
import omero.sys.Parameters;
import omero.sys.ParametersI;
import pojos.DataObject;
import pojos.util.PojoMapper;

//Java imports

/**
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class BrowseFacility extends Facility {

    BrowseFacility(Gateway gateway) {
        super(gateway);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Set<DataObject> loadHierarchy(SecurityContext ctx,
            Class rootType, List<Long> rootIDs, Parameters options)
            throws DSOutOfServiceException {

        try {
            IContainerPrx service = gateway.getPojosService(ctx);
            return PojoMapper.asDataObjects(service.loadContainerHierarchy(
                    PojoMapper.getModelType(rootType).getName(),
                    rootIDs, options));
        } catch (Throwable t) {
            logError(this, "Could not load hierarchy", t);
        }

        return Collections.emptySet();
    }
}
