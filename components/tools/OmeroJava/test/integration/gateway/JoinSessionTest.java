/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2022 University of Dundee. All rights reserved.
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

import omero.RLong;
import omero.api.IPixelsPrx;
import omero.client;
import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.facility.AdminFacility;
import omero.gateway.facility.BrowseFacility;
import omero.gateway.facility.DataManagerFacility;
import omero.gateway.facility.Facility;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageData;
import omero.gateway.model.ProjectData;
import omero.log.SimpleLogger;
import omero.model.IObject;
import omero.model.PixelsType;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Integration test for issue: https://github.com/ome/omero-insight/issues/293
 */
public class JoinSessionTest {

    String host;
    String port;

    long raId, rwId, roId, prId;
    String expName;
    String expPass = "test";

    @BeforeClass(alwaysRun = true)
    protected void setUp() throws Exception {

        // Login as root and create groups
        omero.client client = new omero.client();
        String pass = client.getProperty("omero.rootpass");
        host = client.getProperty("omero.host");
        port = client.getProperty("omero.port");

        LoginCredentials c = new LoginCredentials();
        c.getServer().setHost(host);
        c.getServer().setPort(Integer.parseInt(port));
        c.getUser().setUsername("root");
        c.getUser().setPassword(pass);

        Gateway gw = new Gateway(new SimpleLogger());
        ExperimenterData root = gw.connect(c);

        SecurityContext rootCtx = new SecurityContext(root.getDefaultGroup().getGroupId());
        rootCtx.setExperimenter(root);

        AdminFacility adminFacility = Facility.getFacility(AdminFacility.class, gw);

        GroupData ra = createGroup(rootCtx, GroupData.PERMISSIONS_GROUP_READ_LINK, adminFacility);
        GroupData rw = createGroup(rootCtx, GroupData.PERMISSIONS_GROUP_READ_WRITE, adminFacility);
        GroupData ro = createGroup(rootCtx, GroupData.PERMISSIONS_GROUP_READ, adminFacility);
        GroupData pr = createGroup(rootCtx, GroupData.PERMISSIONS_PRIVATE, adminFacility);
        raId = ra.getGroupId();
        roId = ro.getGroupId();
        rwId = rw.getGroupId();
        prId = pr.getGroupId();

        ArrayList<GroupData> groups = new ArrayList<>();
        groups.add(ra);
        groups.add(rw);
        groups.add(ro);
        groups.add(pr);
        ExperimenterData exp = createExperimenter(rootCtx, groups, adminFacility);
        expName = exp.getUserName();

        // Close the root session
        gw.close();

        // Login as user
        c = new LoginCredentials();
        c.getServer().setHost(host);
        c.getServer().setPort(Integer.parseInt(port));
        c.getUser().setUsername(expName);
        c.getUser().setPassword(expPass);
        gw = new Gateway(new SimpleLogger());
        exp = gw.connect(c);

        DataManagerFacility datamanagerFacility = Facility.getFacility(DataManagerFacility.class,
                gw);
        BrowseFacility browseFacility = Facility.getFacility(BrowseFacility.class, gw);

        SecurityContext ctx = new SecurityContext(raId);
        ctx.setExperimenter(exp);
        DatasetData ds = createDataset(ctx, null, datamanagerFacility);
        createImage(ctx, gw, ds, datamanagerFacility, browseFacility);
        System.out.println("Created image for dataset "+ds.getName()+" , group "+raId);

        ctx = new SecurityContext(roId);
        ctx.setExperimenter(exp);
        ds = createDataset(ctx, null, datamanagerFacility);
        createImage(ctx, gw, ds, datamanagerFacility, browseFacility);
        System.out.println("Created image for dataset "+ds.getName()+" , group "+roId);

        ctx = new SecurityContext(rwId);
        ctx.setExperimenter(exp);
        ds = createDataset(ctx, null, datamanagerFacility);
        createImage(ctx, gw, ds, datamanagerFacility, browseFacility);
        System.out.println("Created image for dataset "+ds.getName()+" , group "+rwId);

        ctx = new SecurityContext(prId);
        ctx.setExperimenter(exp);
        ds = createDataset(ctx, null, datamanagerFacility);
        createImage(ctx, gw, ds, datamanagerFacility, browseFacility);
        System.out.println("Created image for dataset "+ds.getName()+" , group "+prId);

        gw.close();
    }


    @DataProvider(name = "dp")
    public Object[][] dp() {
        String[] permissions = {"RA", "RW", "RO", "PRIVATE"};
        String [] logintypes = {"normal", "args"};
        List<Object[]> result = new ArrayList<>();
        for (String p : permissions) {
            for (String l : logintypes)
            {
                result.add(new String[] {p, l});
            }
        }
        return result.toArray(new Object[result.size()][]);
    }

    @Test(dataProvider = "dp")
    public void testJoinSession(String permission, String logintype) throws Exception {
        omero.client client = new client(host, Integer.parseInt(port));
        client.createSession(expName, expPass);
        String sessionId = client.getSessionId();
        System.out.println("Created session "+sessionId);

        LoginCredentials c = null;
        if (logintype.equals("normal")) {
            c = new LoginCredentials();
            c.getServer().setHost(host);
            c.getServer().setPort(Integer.parseInt(port));
            c.getUser().setUsername(sessionId);
        }
        else {
            String[] args = new String[] {
                    "omero.host="+host,
                    "omero.port="+port,
                    "omero.user="+sessionId
            };
            c = new LoginCredentials(args);
        }

        Gateway gw = new Gateway(new SimpleLogger());
        ExperimenterData exp = gw.connect(c);

        BrowseFacility b = gw.getFacility(BrowseFacility.class);

        long groupId = -1;
        if (permission.equals("RA"))
            groupId = raId;
        else if (permission.equals("RO"))
            groupId = roId;
        else if (permission.equals("RW"))
            groupId = rwId;
        else if (permission.equals("PRIVATE"))
            groupId = prId;

        System.out.println("Testing group "+permission+" "+groupId);

        SecurityContext ctx = new SecurityContext(groupId);
        ctx.setExperimenter(exp);
        Collection<DatasetData> datasets = b.getDatasets(ctx);
        Assert.assertEquals(datasets.size(), 1);
        Collection<Long> datasetIds = datasets.stream().map(d -> d.getId()).collect(Collectors.toList());
        Collection<ImageData> images = b.getImagesForDatasets(ctx, datasetIds);
        Assert.assertEquals(images.size(), 1);

        gw.close();

        client.closeSession();
    }

    GroupData createGroup(SecurityContext ctx, int permission, AdminFacility adminFacility) throws DSOutOfServiceException,
            DSAccessException {
        GroupData group = new GroupData();
        group.setName(UUID.randomUUID().toString());
        return adminFacility.createGroup(ctx, group, null,
                permission);
    }

    DatasetData createDataset(SecurityContext ctx, ProjectData proj, DataManagerFacility datamanagerFacility)
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

    ExperimenterData createExperimenter(SecurityContext ctx, ArrayList<GroupData> groups, AdminFacility adminFacility)
            throws DSOutOfServiceException, DSAccessException {
        ExperimenterData exp = new ExperimenterData();
        exp.setFirstName("Test");
        exp.setLastName("User");
        return adminFacility.createExperimenter(ctx, exp, UUID.randomUUID()
                .toString(), "test", groups, false, true);
    }

    ImageData createImage(SecurityContext ctx, Gateway gw, DatasetData ds, DataManagerFacility datamanagerFacility, BrowseFacility browseFacility)
            throws Exception {
        long imgId = createImage(ctx, gw);
        List<Long> ids = new ArrayList<Long>(1);
        ids.add(imgId);
        ImageData img = browseFacility.getImages(ctx, ids).iterator().next();

        if (ds != null) {
            List<ImageData> l = new ArrayList<ImageData>(1);
            l.add(img);
            datamanagerFacility.addImagesToDataset(ctx, l, ds);

            ids.clear();
            ids.add(ds.getId());
            ds = browseFacility.getDatasets(ctx, ids).iterator().next();
        }
        return img;
    }

    long createImage(SecurityContext ctx, Gateway gw) throws Exception {
        String name = UUID.randomUUID().toString();
        IPixelsPrx svc = gw.getPixelsService(ctx);
        List<IObject> types = gw.getTypesService(ctx)
                .allEnumerations(PixelsType.class.getName());
        List<Integer> channels = new ArrayList<Integer>();
        for (int i = 0; i < 3; i++) {
            channels.add(i);
        }
        RLong id = svc.createImage(10, 10, 10, 10, channels,
                (PixelsType) types.get(1), name, "");
        return id.getValue();
    }

}
