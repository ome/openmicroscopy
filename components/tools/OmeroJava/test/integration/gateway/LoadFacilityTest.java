/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2023 University of Dundee. All rights reserved.
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

import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.WellData;
import omero.model.Plate;
import omero.model.Well;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */

public class LoadFacilityTest extends GatewayTest {

    private GroupData group;
    private ExperimenterData user;
    private SecurityContext ctx;

    private ProjectData proj;
    private DatasetData ds;
    private ScreenData screen;
    private PlateData plate;
    private ImageData img;
    private ArrayList<Long> wellIds;

    @Override
    @BeforeClass(alwaysRun = true)
    protected void setUp() throws Exception {
        super.setUp();
        initData();
    }

    @Test
    public void testGetDataset() throws DSOutOfServiceException, DSAccessException {
        DatasetData obj = this.loadFacility.getDataset(this.ctx, this.ds.getId());
        Assert.assertEquals(obj.getName(), this.ds.getName());
    }

    @Test
    public void testGetDatasetsForPlate() throws DSOutOfServiceException, DSAccessException {
        DatasetData obj = this.loadFacility.getDatasets(this.ctx, this.proj.getId()).iterator().next();
        Assert.assertEquals(obj.getName(), this.ds.getName());
    }

    @Test
    public void testGetImage() throws DSOutOfServiceException, DSAccessException {
        ImageData obj = this.loadFacility.getImage(this.ctx, this.img.getId());
        Assert.assertEquals(obj.getName(), this.img.getName());
    }

    @Test
    public void testGetImagesForDataset() throws DSOutOfServiceException, DSAccessException {
        ImageData obj = this.loadFacility.getImages(this.ctx, this.ds.getId()).iterator().next();
        Assert.assertEquals(obj.getName(), this.img.getName());
    }

    @Test
    public void testGetPlate() throws DSOutOfServiceException, DSAccessException {
        PlateData obj = this.loadFacility.getPlate(this.ctx, this.plate.getId());
        Assert.assertEquals(obj.getName(), this.plate.getName());
    }

    @Test
    public void testGetPlatesForScreen() throws DSOutOfServiceException, DSAccessException {
        PlateData obj = this.loadFacility.getPlates(this.ctx, this.screen.getId()).iterator().next();
        Assert.assertEquals(obj.getName(), this.plate.getName());
    }

    @Test
    public void testGetProject() throws DSOutOfServiceException, DSAccessException {
        ProjectData obj = this.loadFacility.getProject(this.ctx, this.proj.getId());
        Assert.assertEquals(obj.getName(), this.proj.getName());
    }

    @Test
    public void testGetScreen() throws DSOutOfServiceException, DSAccessException {
        ScreenData obj = this.loadFacility.getScreen(this.ctx, this.screen.getId());
        Assert.assertEquals(obj.getName(), this.screen.getName());
    }

    @Test
    public void testGetWell() throws DSOutOfServiceException, DSAccessException {
        WellData test = this.plate.getWells().iterator().next();
        WellData obj = this.loadFacility.getWell(this.ctx, test.getId());
        Assert.assertEquals(obj.getColumn(), test.getColumn());
        Assert.assertEquals(obj.getRow(), test.getRow());
        Assert.assertEquals(obj.getWellSamples().get(0).getImage().getId(),
                test.getWellSamples().get(0).getImage().getId());
    }

    @Test
    public void testGetWellsForPlate() throws DSOutOfServiceException, DSAccessException {
        Set<String> test = new HashSet<>();
        for (WellData w : this.plate.getWells()) {
            String key = w.getColumn()+"|"+w.getRow()+"|"+w.getId();
            test.add(key);
        }
        for (WellData w : this.loadFacility.getWells(this.ctx, this.plate.getId())) {
            String key = w.getColumn()+"|"+w.getRow()+"|"+w.getId();
            Assert.assertTrue(test.contains(key));
            test.remove(key);
        }
        Assert.assertTrue(test.isEmpty());
    }
    
    private void initData() throws Exception {
        this.group = createGroup();
        this.user = createExperimenter(group);
        this.ctx = new SecurityContext(group.getId());

        this.proj = createProject(ctx);
        this.ds = createDataset(ctx, proj);
        this.img = createImage(ctx, ds);

        this.screen = createScreen(ctx);
        this.plate = createPlateWithWells(ctx, screen);
        System.out.println("ID: "+this.plate.getId());
        this.wellIds = new ArrayList<Long>();
        Plate p = this.plate.asPlate();
        for (Well w : p.copyWells()) {
            this.wellIds.add(w.getId().getValue());
        }
    }

}
