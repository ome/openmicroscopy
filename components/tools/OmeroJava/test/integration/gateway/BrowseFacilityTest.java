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
import java.util.Collection;
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
import omero.model.PlateAcquisition;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;

//Java imports

/**
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class BrowseFacilityTest extends GatewayTest {

    private GroupData group;
    private ExperimenterData user; // has proj, ds and img
    private ExperimenterData user2; // has proj, ds and img
    private ExperimenterData user3; // has no data

    private ProjectData proj;
    private DatasetData ds;
    private ScreenData screen;
    private PlateData plate;
    private ImageData img;
    private PlateAcquisition acq;

    private ProjectData proj2;
    private DatasetData ds2;
    private ScreenData screen2;
    private PlateData plate2;
    private ImageData img2;
    private PlateAcquisition acq2;

    // TODO: plateacquisition stuff still missing
    
    @Override
    @BeforeClass(alwaysRun = true)
    protected void setUp() throws Exception {
        super.setUp();
        initData();
    }

    @Test
    public void testGetDatasets() {
        SecurityContext ctx = new SecurityContext(group.getId());
        ctx.setExperimenter(user);
        ctx.sudo();

        Collection<DatasetData> result = browseFacility.getDatasets(ctx);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.iterator().next().getId(), ds.getId());

        Collection<Long> ids = new ArrayList<Long>(1);
        ids.add(ds.getId());
        result = browseFacility.getDatasets(ctx, ids);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.iterator().next().getId(), ds.getId());

        result = browseFacility.getDatasets(ctx, user.getId(), ids);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.iterator().next().getId(), ds.getId());

        // Get dataset for user2 with user1 context
        result = browseFacility.getDatasets(ctx, user2.getId());
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.iterator().next().getId(), ds2.getId());

        // Get dataset for user3 - has none
        result = browseFacility.getDatasets(ctx, user3.getId());
        Assert.assertEquals(result.size(), 0);
    }

    @Test
    public void testGetProjects() {
        SecurityContext ctx = new SecurityContext(group.getId());
        ctx.setExperimenter(user);
        ctx.sudo();

        Collection<ProjectData> result = browseFacility.getProjects(ctx);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.iterator().next().getId(), proj.getId());

        Collection<Long> ids = new ArrayList<Long>(1);
        ids.add(proj.getId());
        result = browseFacility.getProjects(ctx, ids);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.iterator().next().getId(), proj.getId());

        result = browseFacility.getProjects(ctx, user.getId(), ids);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.iterator().next().getId(), proj.getId());

        // Get project for user2 with user1 context
        result = browseFacility.getProjects(ctx, user2.getId());
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.iterator().next().getId(), proj2.getId());

        // Get project for user3 - has none
        result = browseFacility.getProjects(ctx, user3.getId());
        Assert.assertEquals(result.size(), 0);
    }

    @Test
    public void testGetScreens() {
        SecurityContext ctx = new SecurityContext(group.getId());
        ctx.setExperimenter(user);
        ctx.sudo();

        Collection<ScreenData> result = browseFacility.getScreens(ctx);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.iterator().next().getId(), screen.getId());

        Collection<Long> ids = new ArrayList<Long>(1);
        ids.add(screen.getId());
        result = browseFacility.getScreens(ctx, ids);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.iterator().next().getId(), screen.getId());

        result = browseFacility.getScreens(ctx, user.getId(), ids);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.iterator().next().getId(), screen.getId());

        // Get screen for user2 with user1 context
        result = browseFacility.getScreens(ctx, user2.getId());
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.iterator().next().getId(), screen2.getId());

        // Get screen for user3 - has none
        result = browseFacility.getScreens(ctx, user3.getId());
        Assert.assertEquals(result.size(), 0);
    }

    @Test
    public void testGetPlates() {
        SecurityContext ctx = new SecurityContext(group.getId());
        ctx.setExperimenter(user);
        ctx.sudo();

        Collection<PlateData> result = browseFacility.getPlates(ctx);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.iterator().next().getId(), plate.getId());

        Collection<Long> ids = new ArrayList<Long>(1);
        ids.add(plate.getId());
        result = browseFacility.getPlates(ctx, ids);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.iterator().next().getId(), plate.getId());

        result = browseFacility.getPlates(ctx, user.getId(), ids);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.iterator().next().getId(), plate.getId());

        // Get plate for user2 with user1 context
        result = browseFacility.getPlates(ctx, user2.getId());
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.iterator().next().getId(), plate2.getId());

        // Get plate for user3 - has none
        result = browseFacility.getPlates(ctx, user3.getId());
        Assert.assertEquals(result.size(), 0);
    }

    @Test
    public void testGetImages() {
        SecurityContext ctx = new SecurityContext(group.getId());
        ctx.setExperimenter(user);
        ctx.sudo();

        Collection<ImageData> result = browseFacility.getImages(ctx);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.iterator().next().getId(), img.getId());

        Collection<Long> ids = new ArrayList<Long>(1);
        ids.add(img.getId());
        result = browseFacility.getImages(ctx, ids);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.iterator().next().getId(), img.getId());

        result = browseFacility.getImages(ctx, user.getId(), ids);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.iterator().next().getId(), img.getId());

        // Get image for user2 with user1 context
        ids.clear();
        ids.add(this.img2.getId());
        result = browseFacility.getImages(ctx, user2.getId(), ids);
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.iterator().next().getId(), img2.getId());
    }

    private void initData() throws Exception {
        this.group = createGroup();
        this.user = createExperimenter(group);
        this.user2 = createExperimenter(group);
        this.user3 = createExperimenter(group);

        SecurityContext ctx = new SecurityContext(group.getId());
        ctx.setExperimenter(user);
        ctx.sudo();

        this.proj = createProject(ctx);
        this.ds = createDataset(ctx, proj);
        this.screen = createScreen(ctx);
        this.plate = createPlate(ctx, screen);
        this.img = createImage(ctx, ds);

        ctx = new SecurityContext(group.getId());
        ctx.setExperimenter(user2);
        ctx.sudo();

        this.proj2 = createProject(ctx);
        this.ds2 = createDataset(ctx, proj2);
        this.screen2 = createScreen(ctx);
        this.plate2 = createPlate(ctx, screen2);
        this.img2 = createImage(ctx, ds2);
    }

    private ProjectData createProject(SecurityContext ctx)
            throws DSOutOfServiceException, DSAccessException {
        ProjectData proj = new ProjectData();
        proj.setName(UUID.randomUUID().toString());
        return (ProjectData) datamanagerFacility.saveAndReturnObject(ctx, proj);
    }

    private ScreenData createScreen(SecurityContext ctx)
            throws DSOutOfServiceException, DSAccessException {
        ScreenData screen = new ScreenData();
        screen.setName(UUID.randomUUID().toString());
        return (ScreenData) datamanagerFacility
                .saveAndReturnObject(ctx, screen);
    }

    private DatasetData createDataset(SecurityContext ctx, ProjectData proj)
            throws DSOutOfServiceException, DSAccessException {
        DatasetData ds = new DatasetData();
        ds.setName(UUID.randomUUID().toString());
        if (proj != null) {
            Set<ProjectData> projs = new HashSet<ProjectData>(1);
            projs.add(proj);
            ds.setProjects(projs);
        }
        return (DatasetData) datamanagerFacility.saveAndReturnObject(ctx, ds);
    }

    private PlateData createPlate(SecurityContext ctx, ScreenData screen)
            throws DSOutOfServiceException, DSAccessException {
        PlateData plate = new PlateData();
        plate.setName(UUID.randomUUID().toString());
        if (screen != null) {
            Set<ScreenData> screens = new HashSet<ScreenData>(1);
            screens.add(screen);
            plate.setScreens(screens);
        }
        return (PlateData) datamanagerFacility.saveAndReturnObject(ctx, plate);
    }

    private ImageData createImage(SecurityContext ctx, DatasetData ds)
            throws Exception {
        long imgId = createImage(ctx);
        List<Long> ids = new ArrayList<Long>(1);
        ids.add(imgId);
        ImageData img = browseFacility.getImages(ctx, ids).iterator().next();

        List<ImageData> l = new ArrayList<ImageData>(1);
        l.add(img);
        datamanagerFacility.addImagesToDataset(ctx, l, ds);

        ids.clear();
        ids.add(ds.getId());
        ds = browseFacility.getDatasets(ctx, ids).iterator().next();

        return img;
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
