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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import omero.RLong;
import omero.api.IPixelsPrx;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.model.IObject;
import omero.model.PixelsType;

import org.testng.Assert;
import org.testng.annotations.Test;

import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;

//Java imports

/**
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class DataManagerFacilityTest extends GatewayTest {

    ProjectData proj;
    DatasetData ds;

    @Test
    public void testSaveAndReturnObjectProject()
            throws DSOutOfServiceException, DSAccessException {
        ProjectData proj = new ProjectData();
        proj.setName(UUID.randomUUID().toString());
        this.proj = (ProjectData) datamanagerFacility.saveAndReturnObject(
                rootCtx, proj);
        Assert.assertTrue(this.proj.getId() > -1);
    }

    @Test(dependsOnMethods = { "testSaveAndReturnObjectProject" })
    public void testSaveAndReturnObjectDataset()
            throws DSOutOfServiceException, DSAccessException {
        DatasetData ds = new DatasetData();
        ds.setName(UUID.randomUUID().toString());
        Set<ProjectData> projs = new HashSet<ProjectData>(1);
        projs.add(this.proj);
        ds.setProjects(projs);
        this.ds = (DatasetData) datamanagerFacility.saveAndReturnObject(
                rootCtx, ds);
        Assert.assertTrue(this.ds.getId() > -1);
    }

    @Test(dependsOnMethods = { "testSaveAndReturnObjectDataset" })
    public void testAddImage() throws Exception {
        long imgId = createImage(rootCtx);
        List<Long> ids = new ArrayList<Long>(1);
        ids.add(imgId);
        ImageData img = browseFacility.getImages(rootCtx, ids).iterator()
                .next();
        Assert.assertNotNull(img);

        List<ImageData> l = new ArrayList<ImageData>(1);
        l.add(img);
        datamanagerFacility.addImagesToDataset(rootCtx, l, ds);

        ids.clear();
        ids.add(ds.getId());
        ds = browseFacility.getDatasets(rootCtx, ids).iterator().next();
        Assert.assertEquals(ds.getImages().size(), 1);
    }

    private long createImage(SecurityContext ctx) throws Exception {
        IPixelsPrx svc = gw.getPixelsService(ctx);
        List<IObject> types = svc
                .getAllEnumerations(PixelsType.class.getName());
        List<Integer> channels = new ArrayList<Integer>();
        for (int i = 0; i < 3; i++) {
            channels.add(i);
        }
        RLong id = svc.createImage(10, 10, 10, 10, channels,
                (PixelsType) types.get(1), "test", "");
        return id.getValue();
    }

}
