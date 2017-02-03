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

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.model.BlitzInstanceProvider;
import ome.units.UNITS;
import ome.units.quantity.Time;
import ome.util.LSID;
import ome.xml.model.primitives.NonNegativeInteger;
import omero.api.ServiceFactoryPrx;
import omero.model.PlaneInfo;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class PlaneInfoProcessorTest
{
    private OMEROMetadataStoreClient store;

    private static final int IMAGE_INDEX = 1;

    private static final int PLANE_INFO_INDEX = 1;

    @BeforeMethod
    protected void setUp() throws Exception
    {
        Time onesec = new Time(1, UNITS.SECOND);
        ServiceFactoryPrx sf = new TestServiceFactory().proxy();
        store = new OMEROMetadataStoreClient();
        store.initialize(sf);
        store.setEnumerationProvider(new TestEnumerationProvider());
        store.setInstanceProvider(
                new BlitzInstanceProvider(store.getEnumerationProvider()));
        store.setPlaneTheC(new NonNegativeInteger(0), IMAGE_INDEX, PLANE_INFO_INDEX);
        store.setPlaneTheZ(new NonNegativeInteger(0), IMAGE_INDEX, PLANE_INFO_INDEX);
        store.setPlaneTheT(new NonNegativeInteger(0), IMAGE_INDEX, PLANE_INFO_INDEX);
        store.setPlaneTheC(new NonNegativeInteger(1), IMAGE_INDEX, PLANE_INFO_INDEX + 1);
        store.setPlaneTheZ(new NonNegativeInteger(1), IMAGE_INDEX, PLANE_INFO_INDEX + 1);
        store.setPlaneTheT(new NonNegativeInteger(1), IMAGE_INDEX, PLANE_INFO_INDEX + 1);
        store.setPlaneTheC(new NonNegativeInteger(2), IMAGE_INDEX, PLANE_INFO_INDEX + 2);
        store.setPlaneTheZ(new NonNegativeInteger(2), IMAGE_INDEX, PLANE_INFO_INDEX + 2);
        store.setPlaneTheT(new NonNegativeInteger(2), IMAGE_INDEX, PLANE_INFO_INDEX + 2);
        store.setPlaneDeltaT(onesec, IMAGE_INDEX, PLANE_INFO_INDEX +2);
    }

    @Test
    public void testPlaneInfoExists()
    {
        Assert.assertEquals(store.countCachedContainers(PlaneInfo.class, null), 3);
        LSID planeInfoLSID1 = new LSID(PlaneInfo.class, IMAGE_INDEX, PLANE_INFO_INDEX);
        LSID planeInfoLSID2 = new LSID(PlaneInfo.class, IMAGE_INDEX, PLANE_INFO_INDEX + 1);
        LSID planeInfoLSID3 = new LSID(PlaneInfo.class, IMAGE_INDEX, PLANE_INFO_INDEX + 2);
        PlaneInfo pi1 = (PlaneInfo) store.getSourceObject(planeInfoLSID1);
        PlaneInfo pi2 = (PlaneInfo) store.getSourceObject(planeInfoLSID2);
        PlaneInfo pi3 = (PlaneInfo) store.getSourceObject(planeInfoLSID3);
        Assert.assertNotNull(pi1);
        Assert.assertNotNull(pi2);
        Assert.assertNotNull(pi3);
        Assert.assertEquals(pi1.getTheC().getValue(), 0);
        Assert.assertEquals(pi1.getTheZ().getValue(), 0);
        Assert.assertEquals(pi1.getTheT().getValue(), 0);
        Assert.assertEquals(pi2.getTheC().getValue(), 1);
        Assert.assertEquals(pi2.getTheZ().getValue(), 1);
        Assert.assertEquals(pi2.getTheT().getValue(), 1);
        Assert.assertEquals(pi3.getTheC().getValue(), 2);
        Assert.assertEquals(pi3.getTheZ().getValue(), 2);
        Assert.assertEquals(pi3.getTheT().getValue(), 2);
        Assert.assertEquals(pi3.getDeltaT().getValue(), 1.0);
    }

    @Test
    public void testPlaneInfoCleanup()
    {
        store.postProcess();
        Assert.assertEquals(1, store.countCachedContainers(PlaneInfo.class, null));
        LSID planeInfoLSID = new LSID(PlaneInfo.class, IMAGE_INDEX, PLANE_INFO_INDEX + 2);
        PlaneInfo pi = (PlaneInfo) store.getSourceObject(planeInfoLSID);
        Assert.assertNotNull(pi);
        Assert.assertEquals(pi.getTheC().getValue(), 2);
        Assert.assertEquals(pi.getTheZ().getValue(), 2);
        Assert.assertEquals(pi.getTheT().getValue(), 2);
        Assert.assertEquals(pi.getDeltaT().getValue(), 1.0);
    }
}
