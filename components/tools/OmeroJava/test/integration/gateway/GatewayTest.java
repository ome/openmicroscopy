/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015-2021 University of Dundee. All rights reserved.
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import integration.ModelMockFactory;
import omero.RLong;
import omero.ServerError;
import omero.api.IPixelsPrx;
import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.facility.AdminFacility;
import omero.gateway.facility.BrowseFacility;
import omero.gateway.facility.DataManagerFacility;
import omero.gateway.facility.Facility;
import omero.gateway.facility.MetadataFacility;
import omero.gateway.facility.ROIFacility;
import omero.gateway.facility.RawDataFacility;
import omero.gateway.facility.SearchFacility;
import omero.gateway.facility.TablesFacility;
import omero.gateway.facility.TransferFacility;
import omero.gateway.model.ROIData;
import omero.gateway.model.RectangleData;
import omero.log.SimpleLogger;
import omero.model.Folder;
import omero.model.IObject;
import omero.model.Image;
import omero.model.PixelsType;

import omero.model.Roi;
import omero.sys.ParametersI;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.FolderData;
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

public class GatewayTest {

    /** Identifies the <code>user</code> group. */
    public String USER_GROUP = "user";

    Gateway gw = null;
    ExperimenterData root = null;
    SecurityContext rootCtx = null;

    AdminFacility adminFacility = null;
    BrowseFacility browseFacility = null;
    RawDataFacility rawdataFacility = null;
    SearchFacility searchFacility = null;
    TransferFacility transferFacility = null;
    DataManagerFacility datamanagerFacility = null;
    MetadataFacility metadataFacility = null;
    ROIFacility roiFacility = null;
    TablesFacility tablesFacility = null;

    @Test
    public void testConnected() throws DSOutOfServiceException {
        String version = gw.getServerVersion();
        Assert.assertTrue(version != null && version.trim().length() > 0);
    }
    
    /**
     * Initializes the Gateway.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @BeforeClass(alwaysRun = true)
    protected void setUp() throws Exception {

        omero.client client =  new omero.client();
        String pass = client.getProperty("omero.rootpass");
        String host = client.getProperty("omero.host");
        String port = client.getProperty("omero.port");
        
        LoginCredentials c = new LoginCredentials();
        c.getServer().setHost(host);
        c.getServer().setPort(Integer.parseInt(port));
        c.getUser().setUsername("root");
        c.getUser().setPassword(pass);

        gw = new Gateway(new SimpleLogger());
        root = gw.connect(c);

        rootCtx = new SecurityContext(root.getDefaultGroup().getGroupId());
        rootCtx.setExperimenter(root);

        adminFacility = Facility.getFacility(AdminFacility.class, gw);
        browseFacility = Facility.getFacility(BrowseFacility.class, gw);
        rawdataFacility = Facility.getFacility(RawDataFacility.class, gw);
        searchFacility = Facility.getFacility(SearchFacility.class, gw);
        transferFacility = Facility.getFacility(TransferFacility.class, gw);
        datamanagerFacility = Facility.getFacility(DataManagerFacility.class,
                gw);
        metadataFacility = Facility.getFacility(MetadataFacility.class,
                gw);
        roiFacility = Facility.getFacility(ROIFacility.class,
                gw);
        tablesFacility = Facility.getFacility(TablesFacility.class, gw);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        if (gw != null)
            gw.disconnect();
    }

    GroupData createGroup() throws DSOutOfServiceException,
            DSAccessException {
        GroupData group = new GroupData();
        group.setName(UUID.randomUUID().toString());
        return adminFacility.createGroup(rootCtx, group, null,
                GroupData.PERMISSIONS_GROUP_READ_WRITE);
    }

    ExperimenterData createExperimenter(GroupData group)
            throws DSOutOfServiceException, DSAccessException {
        ExperimenterData exp = new ExperimenterData();
        exp.setFirstName("Test");
        exp.setLastName("User");
        List<GroupData> groups = new ArrayList<GroupData>();
        if (group != null)
            groups.add(group);
        return adminFacility.createExperimenter(rootCtx, exp, UUID.randomUUID()
                .toString(), "test", groups, false, true);
    }

    FolderData createFolder(SecurityContext ctx)
            throws DSOutOfServiceException, DSAccessException {
        FolderData folder = new FolderData();
        folder.setName(UUID.randomUUID().toString());
        return (FolderData) datamanagerFacility.saveAndReturnObject(ctx, folder);
    }
    
    ProjectData createProject(SecurityContext ctx)
            throws DSOutOfServiceException, DSAccessException {
        ProjectData proj = new ProjectData();
        proj.setName(UUID.randomUUID().toString());
        return (ProjectData) datamanagerFacility.saveAndReturnObject(ctx, proj);
    }

    ScreenData createScreen(SecurityContext ctx)
            throws DSOutOfServiceException, DSAccessException {
        ScreenData screen = new ScreenData();
        screen.setName(UUID.randomUUID().toString());
        return (ScreenData) datamanagerFacility
                .saveAndReturnObject(ctx, screen);
    }

    DatasetData createDataset(SecurityContext ctx, ProjectData proj)
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

    PlateData createPlate(SecurityContext ctx, ScreenData screen)
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

    PlateData createPlateWithWells()  throws Exception {
        ModelMockFactory mf = new ModelMockFactory(gw.getTypesService(rootCtx));
        PlateData p = new PlateData(mf.createPlate(3, 3, 2, 1, false));
        return (PlateData) datamanagerFacility.saveAndReturnObject(rootCtx, p);
    }

    ImageData createImage(SecurityContext ctx, DatasetData ds)
            throws Exception {
        long imgId = createImage(ctx);
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

    long createImage(SecurityContext ctx) throws Exception {
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

    File createFile(int sizeInMb) {
        try {
            File tmp = File.createTempFile(System.currentTimeMillis()+"_attachedFile", "file");

            FileOutputStream fos = new FileOutputStream(tmp);

            Random r = new Random();
            byte[] data = new byte[1024*1024];
            r.nextBytes(data);

            int size = 0;
            while(size < (sizeInMb*1024*1024)) {
                fos.write(data);
                size += data.length;
            }
            fos.close();

            return tmp;
        } catch (IOException e) {
        }
        return null;
    }

    FolderData createRoiFolder(SecurityContext ctx,
                                       Collection<ROIData> rois) throws DSOutOfServiceException,
            DSAccessException {
        FolderData folder = new FolderData();
        folder.setName(UUID.randomUUID().toString());
        Folder f = folder.asFolder();
        for (ROIData roi : rois)
            f.linkRoi((Roi) roi.asIObject());
        return (FolderData) datamanagerFacility
                .saveAndReturnObject(ctx, folder);
    }

    ROIData createRectangleROI(int x, int y, int w, int h, long imgId)
            throws DSOutOfServiceException, DSAccessException,
            ExecutionException {
        ROIData roiData = new ROIData();
        RectangleData rectangle = new RectangleData(x, y, w, h);
        roiData.addShapeData(rectangle);
        return roiFacility
                .saveROIs(rootCtx, imgId, Collections.singleton(roiData))
                .iterator().next();
    }

    ImageData createImage() throws ServerError, DSOutOfServiceException {
        String name = UUID.randomUUID().toString();
        IPixelsPrx svc = gw.getPixelsService(rootCtx);
        List<IObject> types = gw.getTypesService(rootCtx)
                .allEnumerations(PixelsType.class.getName());
        List<Integer> channels = new ArrayList<Integer>();
        for (int i = 0; i < 3; i++) {
            channels.add(i);
        }
        long id = svc.createImage(10, 10, 1, 1, channels,
                (PixelsType) types.get(1), name, "").getValue();
        ParametersI param = new ParametersI();
        param.addId(id);
        Image img = (Image) gw.getQueryService(rootCtx).findByQuery(
                "select i from Image i where i.id = :id", param);
        return new ImageData(img);
    }
}
