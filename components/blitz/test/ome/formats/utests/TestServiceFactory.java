/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2017 University of Dundee. All rights reserved.
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
package ome.formats.utests;

import omero.ServerError;
import omero.api.MetadataStorePrx;
import omero.api.ServiceInterfacePrx;
import omero.api.ServiceFactoryPrx;
import omero.constants.METADATASTORE;

import org.jmock.Mock;
import org.jmock.core.stub.DefaultResultStub;

public class TestServiceFactory
{

    /**
     * Using an instance method to return a proxy.
     * Rather than using the instance itself, we create
     * an actual mock which saves us from needing to
     * implement each method, which breaks fairly
     * often. Only the methods which need overriding
     */
    public ServiceFactoryPrx proxy()
    {
        Mock mock = new Mock(ServiceFactoryPrx.class);
        mock.setDefaultStub(new DefaultResultStub());
        return (ServiceFactoryPrx) mock.proxy();
    }

    public ServiceInterfacePrx getByName(String arg0) throws ServerError
    {
	if (arg0.equals(METADATASTORE.value))
	{
            Mock mock = new Mock(MetadataStorePrx.class);
            mock.setDefaultStub(new DefaultResultStub());
            return (MetadataStorePrx) mock.proxy();
	}
        return null;
    }

}
