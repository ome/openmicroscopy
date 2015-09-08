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
package integration.gateway;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import ome.model.units.BigResult;
import omero.api.IPixelsPrx;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.facility.BrowseFacility;
import omero.gateway.facility.DataManagerFacility;
import omero.gateway.facility.MetadataFacility;
import omero.model.IObject;
import omero.model.ImagingEnvironment;
import omero.model.ImagingEnvironmentI;
import omero.model.PixelsType;
import omero.model.Temperature;
import omero.model.TemperatureI;
import omero.model.enums.UnitsTemperature;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import omero.gateway.model.ChannelData;
import omero.gateway.model.ImageAcquisitionData;
import omero.gateway.model.ImageData;

/**
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class MetadataFacilityTest extends GatewayTest {

    private ImageData img;
    private Temperature temp;

    @Override
    @BeforeClass(alwaysRun = true)
    protected void setUp() throws Exception {
        super.setUp();
        initData();
    }

    @Test
    public void testGetImageAcquisitionData() throws ExecutionException,
            BigResult {
        MetadataFacility mdf = gw.getFacility(MetadataFacility.class);
        ImageAcquisitionData d = mdf.getImageAcquisitionData(rootCtx,
                img.getId());
        Assert.assertEquals(d.getTemperature(UnitsTemperature.CELSIUS), temp);
    }

    @Test
    public void testgetChannelData() throws ExecutionException,
            DSOutOfServiceException, DSAccessException {
        MetadataFacility mdf = gw.getFacility(MetadataFacility.class);
        List<ChannelData> channels = mdf.getChannelData(rootCtx, img.getId());
        Assert.assertEquals(channels.size(), 3);
    }

    private void initData() throws Exception {
        String name = UUID.randomUUID().toString();
        IPixelsPrx svc = gw.getPixelsService(rootCtx);
        List<IObject> types = svc
                .getAllEnumerations(PixelsType.class.getName());
        List<Integer> channels = new ArrayList<Integer>();
        for (int i = 0; i < 3; i++) {
            channels.add(i);
        }
        long imgId = svc.createImage(100, 100, 1, 1, channels,
                (PixelsType) types.get(1), name, "").getValue();

        img = gw.getFacility(BrowseFacility.class).getImage(rootCtx, imgId);

        temp = new TemperatureI(20, UnitsTemperature.CELSIUS);
        ImagingEnvironment env = new ImagingEnvironmentI();
        env.setTemperature(temp);
        img.asImage().setImagingEnvironment(env);

        gw.getFacility(DataManagerFacility.class).saveAndReturnObject(rootCtx,
                img);
    }
}
