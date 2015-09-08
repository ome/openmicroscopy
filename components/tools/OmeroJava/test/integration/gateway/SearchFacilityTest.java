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

import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.SearchParameters;
import omero.gateway.model.SearchResultCollection;
import omero.gateway.model.SearchScope;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;


/**
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class SearchFacilityTest extends GatewayTest {

    private SecurityContext ctx;
    private GroupData group;
    private ExperimenterData user;

    private ProjectData proj;
    private DatasetData ds;
    private ScreenData screen;
    private PlateData plate;
    private ImageData img;

    @Override
    @BeforeClass(alwaysRun = true)
    protected void setUp() throws Exception {
        super.setUp();
        initData();
        // wait a little bit for the search indexer
        Thread.sleep(10000);
    }

    @Test
    public void testSearch() throws DSOutOfServiceException, DSAccessException {
        Set<SearchScope> scope = new HashSet<SearchScope>();
        scope.add(SearchScope.NAME);

        List<Class<? extends DataObject>> types = new ArrayList<Class<? extends DataObject>>();
        types.add(ProjectData.class);
        types.add(DatasetData.class);
        types.add(ScreenData.class);
        types.add(PlateData.class);
        types.add(ImageData.class);

        String query = proj.getName().substring(0, 6) + "*";
        SearchParameters param = new SearchParameters(scope, types, query);
        SearchResultCollection results = searchFacility.search(ctx, param);
        Assert.assertEquals(results.size(), 1);
        Assert.assertEquals(results.iterator().next().getType(),
                ProjectData.class);
        Assert.assertEquals(results.iterator().next().getObjectId(),
                proj.getId());

        query = ds.getName().substring(0, 6) + "*";
        param = new SearchParameters(scope, types, query);
        results = searchFacility.search(ctx, param);
        Assert.assertEquals(results.size(), 1);
        Assert.assertEquals(results.iterator().next().getType(),
                DatasetData.class);
        Assert.assertEquals(results.iterator().next().getObjectId(), ds.getId());

        query = screen.getName().substring(0, 6) + "*";
        param = new SearchParameters(scope, types, query);
        results = searchFacility.search(ctx, param);
        Assert.assertEquals(results.size(), 1);
        Assert.assertEquals(results.iterator().next().getType(),
                ScreenData.class);
        Assert.assertEquals(results.iterator().next().getObjectId(),
                screen.getId());

        query = plate.getName().substring(0, 6) + "*";
        param = new SearchParameters(scope, types, query);
        results = searchFacility.search(ctx, param);
        Assert.assertEquals(results.size(), 1);
        Assert.assertEquals(results.iterator().next().getType(),
                PlateData.class);
        Assert.assertEquals(results.iterator().next().getObjectId(),
                plate.getId());

        query = img.getName().substring(0, 6) + "*";
        param = new SearchParameters(scope, types, query);
        results = searchFacility.search(ctx, param);
        Assert.assertEquals(results.size(), 1);
        Assert.assertEquals(results.iterator().next().getType(),
                ImageData.class);
        Assert.assertEquals(results.iterator().next().getObjectId(),
                img.getId());

    }

    private void initData() throws Exception {
        this.group = createGroup();
        this.user = createExperimenter(group);

        ctx = new SecurityContext(group.getId());
        ctx.setExperimenter(user);
        ctx.sudo();

        this.proj = createProject(ctx);
        this.ds = createDataset(ctx, proj);
        this.screen = createScreen(ctx);
        this.plate = createPlate(ctx, screen);
        this.img = createImage(ctx, ds);
    }
}
