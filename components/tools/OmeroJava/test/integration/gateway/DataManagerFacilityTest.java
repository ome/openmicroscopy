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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import omero.LockTimeout;
import omero.ServerError;
import omero.cmd.CmdCallbackI;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.model.IObject;

import omero.gateway.model.AnnotationData;
import omero.gateway.model.BooleanAnnotationData;
import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.DoubleAnnotationData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.FolderData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageData;
import omero.gateway.model.LongAnnotationData;
import omero.gateway.model.MapAnnotationData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ROIData;
import omero.gateway.model.RatingAnnotationData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.model.TermAnnotationData;
import omero.gateway.model.TextualAnnotationData;
import omero.gateway.model.XMLAnnotationData;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class DataManagerFacilityTest extends GatewayTest {

    /** Number of attachment files to generate */
    final int nAttachmentFiles = 25;

    /** File size of an attachment */
    final int fileSizeInMb = 25;

    ProjectData proj;
    DatasetData ds;
    ImageData img;
    
    File[] attachments;
    
    @Override
    @BeforeClass(alwaysRun = true)
    protected void setUp() throws Exception {
        super.setUp();
        attachments = new File[nAttachmentFiles];
        for (int i = 0; i < attachments.length; i++) {
            attachments[i] = createFile(fileSizeInMb);
        }
    }

    @AfterClass(alwaysRun = true)
    protected void teardown() {
        if (attachments != null) {
            for (int i = 0; i < attachments.length; i++) {
                attachments[i].delete();
            }
        }
    }
    
    @Test
    public void testSaveAndReturnObject()
            throws DSOutOfServiceException, DSAccessException {
        ProjectData proj = new ProjectData();
        proj.setName(UUID.randomUUID().toString());
        this.proj = (ProjectData) datamanagerFacility.saveAndReturnObject(
                rootCtx, proj);
        Assert.assertTrue(this.proj.getId() > -1);
        
        DatasetData ds = new DatasetData();
        ds.setName(UUID.randomUUID().toString());
        Set<ProjectData> projs = new HashSet<ProjectData>(1);
        projs.add(this.proj);
        ds.setProjects(projs);
        this.ds = (DatasetData) datamanagerFacility.saveAndReturnObject(
                rootCtx, ds);
        Assert.assertTrue(this.ds.getId() > -1);
    }

    @Test(dependsOnMethods = { "testSaveAndReturnObject" })
    public void testAddImage() throws Exception {
        long imgId = createImage(rootCtx);
        List<Long> ids = new ArrayList<Long>(1);
        ids.add(imgId);
        img = browseFacility.getImages(rootCtx, ids).iterator()
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
    
    @Test(dependsOnMethods = { "testAddImage" })
    public void testUpdateObject() throws DSOutOfServiceException, DSAccessException {
        Timestamp timestamp = img.getUpdated();
        String newName = UUID.randomUUID().toString();
        img.setName(newName);
        datamanagerFacility.updateObject(rootCtx, img.asIObject(), null);
        img = browseFacility.getImage(rootCtx, img.getId());
        Assert.assertEquals(img.getName(), newName);
        Assert.assertTrue(img.getUpdated().after(timestamp));
    }
    
    @Test(dependsOnMethods = { "testUpdateObject" })
    public void testDeleteObject() throws DSOutOfServiceException, DSAccessException, LockTimeout, InterruptedException {
        datamanagerFacility.delete(rootCtx, img.asIObject()).loop(10, 500);
        List<Long> ids = new ArrayList<Long>(1);
        ids.add(img.getId());
        Collection<ImageData> img = browseFacility.getImages(rootCtx, ids);
        Assert.assertTrue(img.isEmpty());
    }
    
    @Test
    public void testDeleteFolders() throws DSOutOfServiceException,
            DSAccessException, InterruptedException, ServerError, ExecutionException {

        // 1) Test recursive delete
        FolderData f1 = new FolderData();
        f1.setName("f1");
        f1 = (FolderData) datamanagerFacility.saveAndReturnObject(rootCtx, f1);

        FolderData f11 = new FolderData();
        f11.setName("f11");
        f11.setParentFolder(f1.asFolder());
        f11 = (FolderData) datamanagerFacility
                .saveAndReturnObject(rootCtx, f11);

        FolderData f111 = new FolderData();
        f111.setName("f111");
        f111.setParentFolder(f11.asFolder());
        f111 = (FolderData) datamanagerFacility
                .saveAndReturnObject(rootCtx, f111);

        final long f1Id = f1.getId();
        final long f11Id = f11.getId();
        final long f111Id = f111.getId();

        Assert.assertTrue(f1Id >= 0);
        Assert.assertTrue(f11Id >= 0);
        Assert.assertTrue(f111Id >= 0);
        CmdCallbackI cb = datamanagerFacility.deleteFolders(rootCtx,
                Collections.singletonList(f1), true, false);
        cb.block(10000);

        
        DataObject notFound = browseFacility.findObject(rootCtx, "FolderData", f1Id);
        Assert.assertNull(notFound);
        
        notFound = browseFacility.findObject(rootCtx, "FolderData", f11Id);
        Assert.assertNull(notFound);

        notFound = browseFacility.findObject(rootCtx, "FolderData", f111Id);
        Assert.assertNull(notFound);
        
        // 2) Test non recursive delete (orphan sub folder)
        f1 = new FolderData();
        f1.setName("f1");
        f1 = (FolderData) datamanagerFacility.saveAndReturnObject(rootCtx, f1);

        f11 = new FolderData();
        f11.setName("f11");
        f11.setParentFolder(f1.asFolder());
        f11 = (FolderData) datamanagerFacility
                .saveAndReturnObject(rootCtx, f11);
        
        final long f1Id2 = f1.getId();
        final long f11Id2 = f11.getId();
        
        Assert.assertTrue(f1Id2 >= 0);
        Assert.assertTrue(f11Id2 >= 0);
        
        cb = datamanagerFacility.deleteFolders(rootCtx,
                Collections.singletonList(f1), false, false);
        cb.block(10000);

        notFound = browseFacility.findObject(rootCtx, "FolderData", f1Id2);
        Assert.assertNull(notFound);
        
        f11 = browseFacility
                .loadFolders(rootCtx, Collections.singleton(f11Id2))
                .iterator().next();
        Assert.assertEquals(f11Id2, f11.getId());
        Assert.assertNull(f11.getParentFolder());
        
        // 3) Test delete content (ROIs)
        long imgId = createImage().getId();
        ROIData rd = createRectangleROI(5, 5,5,5, imgId);
        FolderData f = createRoiFolder(rootCtx, Collections.singleton(rd));
        final long fId = f.getId();
        f = browseFacility
                .loadFolders(rootCtx, Collections.singleton(fId))
                .iterator().next();
        Assert.assertEquals(f.roiCount(), 1);
        cb = datamanagerFacility.deleteFolders(rootCtx,
                Collections.singletonList(f), true, true);
        cb.block(10000);
       
        notFound = browseFacility.findObject(rootCtx, "FolderData", fId);
        Assert.assertNull(notFound);

        notFound = browseFacility.findObject(rootCtx, "ROIData", rd.getId());
        Assert.assertNull(notFound);
        
        // 4) Test orphan content (ROIs)
        imgId = createImage().getId();
        rd = createRectangleROI(5, 5,5,5, imgId);
        FolderData f2 = createRoiFolder(rootCtx, Collections.singleton(rd));
        final long f2Id = f2.getId();
        f2 = browseFacility
                .loadFolders(rootCtx, Collections.singleton(f2Id))
                .iterator().next();
        Assert.assertEquals(f2.roiCount(), 1);
        cb = datamanagerFacility.deleteFolders(rootCtx,
                Collections.singletonList(f2), true, false);
        cb.block(10000);
       
        notFound = browseFacility.findObject(rootCtx, "FolderData", f2Id);
        Assert.assertNull(notFound);
        
        ROIData rdReloaded = roiFacility.loadROI(rootCtx, rd.getId()).getROIs().iterator().next();
        Assert.assertEquals(rdReloaded.getShapeCount(), 1);
    }

    @Test
    public void testAttachFile() throws Exception {
        File tmp = File.createTempFile("attachedFile", "file");
        BufferedWriter out = new BufferedWriter(new FileWriter(tmp));
        out.write("Just a test");
        out.close();

        DatasetData ds = new DatasetData();
        ds.setName(UUID.randomUUID().toString());
        ds = (DatasetData) datamanagerFacility.saveAndReturnObject(rootCtx, ds);

        Future<FileAnnotationData> cb = datamanagerFacility.attachFile(rootCtx,
                tmp, "text/plain", "test", null, ds);
        FileAnnotationData fa = cb.get();

        Assert.assertTrue(fa.getFileName().startsWith("attachedFile"));
        tmp.delete();
    }
    
    @Test
    public void testAttachAnnotation() throws Exception {
        Queue<DataObject> targets = new LinkedList<DataObject>();
        DatasetData ds = new DatasetData();
        ds.setName(UUID.randomUUID().toString());
        targets.add(ds);
        ProjectData proj = new ProjectData();
        proj.setName(UUID.randomUUID().toString());
        targets.add(proj);
        ScreenData s = new ScreenData();
        s.setName(UUID.randomUUID().toString());
        targets.add(s);
        PlateData p = new PlateData();
        p.setName(UUID.randomUUID().toString());
        targets.add(p);
        
        DataObject dob = targets.poll();
        while(dob.getId()<0) {
            dob = datamanagerFacility.saveAndReturnObject(rootCtx, dob);
            targets.add(dob);
            dob = targets.poll();
        }
        targets.add(dob);
        
        long imgId = createImage(rootCtx);
        targets.add(browseFacility.getImage(rootCtx, imgId));
        
        Collection<AnnotationData> annos = new ArrayList<AnnotationData>();
        annos.add(new BooleanAnnotationData(true));
        annos.add(new DoubleAnnotationData(5d));
        annos.add(new LongAnnotationData(1));
        annos.add(new MapAnnotationData());
        annos.add(new RatingAnnotationData(3));
        annos.add(new TagAnnotationData("test"));
        annos.add(new TermAnnotationData("test2"));
        annos.add(new TextualAnnotationData("test3"));
        annos.add(new XMLAnnotationData("<test4/>"));
        
        for(DataObject target : targets) {
            for(AnnotationData anno : annos) {
                anno = datamanagerFacility.attachAnnotation(rootCtx, anno, target);
                Assert.assertNotNull(anno);
                Assert.assertTrue(anno.getId() >= 0);
            }
        }
    }
    
    @Test
    /**
     * Test the move method by moving annotations from one dataset, project,
     * etc. to another.
     * 
     * @throws Exception
     */
    public void testMoveAnnotation() throws Exception {
        List<DataObject> sources = new ArrayList<DataObject>();
        DatasetData ds = new DatasetData();
        ds.setName(UUID.randomUUID().toString());
        sources.add(datamanagerFacility.saveAndReturnObject(rootCtx, ds));
        ProjectData proj = new ProjectData();
        proj.setName(UUID.randomUUID().toString());
        sources.add(datamanagerFacility.saveAndReturnObject(rootCtx, proj));
        ScreenData s = new ScreenData();
        s.setName(UUID.randomUUID().toString());
        sources.add(datamanagerFacility.saveAndReturnObject(rootCtx, s));
        PlateData p = new PlateData();
        p.setName(UUID.randomUUID().toString());
        sources.add(datamanagerFacility.saveAndReturnObject(rootCtx, p));
        long imgId = createImage(rootCtx);
        List<Long> ids = new ArrayList<Long>(1);
        ids.add(imgId);
        sources.add(browseFacility.getImages(rootCtx, ids).iterator().next());

        List<DataObject> targets = new ArrayList<DataObject>();
        ds = new DatasetData();
        ds.setName(UUID.randomUUID().toString());
        targets.add(datamanagerFacility.saveAndReturnObject(rootCtx, ds));
        proj = new ProjectData();
        proj.setName(UUID.randomUUID().toString());
        targets.add(datamanagerFacility.saveAndReturnObject(rootCtx, proj));
        s = new ScreenData();
        s.setName(UUID.randomUUID().toString());
        targets.add(datamanagerFacility.saveAndReturnObject(rootCtx, s));
        p = new PlateData();
        p.setName(UUID.randomUUID().toString());
        targets.add(datamanagerFacility.saveAndReturnObject(rootCtx, p));
        imgId = createImage(rootCtx);
        ids = new ArrayList<Long>(1);
        ids.add(imgId);
        targets.add(browseFacility.getImages(rootCtx, ids).iterator().next());

        Collection<AnnotationData> annos = new ArrayList<AnnotationData>();
        annos.add(new BooleanAnnotationData(true));
        annos.add(new DoubleAnnotationData(5d));
        annos.add(new LongAnnotationData(1));
        annos.add(new MapAnnotationData());
        annos.add(new RatingAnnotationData(3));
        annos.add(new TagAnnotationData("test"));
        annos.add(new TermAnnotationData("test2"));
        annos.add(new TextualAnnotationData("test3"));
        annos.add(new XMLAnnotationData("<test4/>"));

        for (int i = 0; i < sources.size(); i++) {
            DataObject src = sources.get(i);
            DataObject tar = targets.get(i);
            List<AnnotationData> attached = new ArrayList<AnnotationData>();
            annos.forEach(anno -> {
                try {
                    attached.add(datamanagerFacility.attachAnnotation(rootCtx, anno, src));
                } catch (DSOutOfServiceException | DSAccessException e) {
                    Assert.fail("Couldn't attach annotations.");
                }
            });
            
            datamanagerFacility.move(rootCtx, attached, src, tar);

            // check all annotations are attached to tar
            List<AnnotationData> loaded = metadataFacility.getAnnotations(rootCtx, tar);
            Assert.assertEquals(attached.size(), loaded.size());
            Set<Long> check = attached.stream().map(a -> a.getId()).collect(Collectors.toSet());
            loaded.forEach(a -> Assert.assertTrue(check.contains(a.getId())));

            // check none of the annotations are still attached to src
            loaded = metadataFacility.getAnnotations(rootCtx, src);
            Assert.assertEquals(loaded.size(), 0);

            List<IObject> del = attached.stream().map(obj -> obj.asIObject()).collect(Collectors.toList());
            datamanagerFacility.delete(rootCtx, del);
        }
    }

    @Test
    /**
     * Test the move method by moving images from one dataset to another
     * 
     * @throws Exception
     */
    public void testMoveImage() throws Exception {
        DatasetData ds1 = new DatasetData();
        ds1.setName(UUID.randomUUID().toString());
        ds1 = (DatasetData) datamanagerFacility.saveAndReturnObject(rootCtx,
                ds1);

        DatasetData ds2 = new DatasetData();
        ds2.setName(UUID.randomUUID().toString());
        ds2 = (DatasetData) datamanagerFacility.saveAndReturnObject(rootCtx,
                ds2);

        long imgId = createImage(rootCtx);
        ImageData img = browseFacility.getImage(rootCtx, imgId);
        long imgId2 = createImage(rootCtx);
        ImageData img2 = browseFacility.getImage(rootCtx, imgId2);

        datamanagerFacility.addImagesToDataset(rootCtx,
                Arrays.asList(img, img2), ds1);

        ds1 = browseFacility
                .getDatasets(rootCtx, Collections.singleton(ds1.getId()))
                .iterator().next();

        Assert.assertEquals(ds1.getImages().size(), 2);

        datamanagerFacility.move(rootCtx, Arrays.asList(img, img2), ds1, ds2);

        ds1 = browseFacility
                .getDatasets(rootCtx, Collections.singleton(ds1.getId()))
                .iterator().next();
        ds2 = browseFacility
                .getDatasets(rootCtx, Collections.singleton(ds2.getId()))
                .iterator().next();
        Assert.assertTrue(ds1.getImages().isEmpty());
        Assert.assertEquals(ds2.getImages().size(), 2);
    }

    @Test
    /**
     * Test the move method by moving datasets from one project to another
     * 
     * @throws Exception
     */
    public void testMoveContainer() throws Exception {
        ProjectData proj1 = new ProjectData();
        proj1.setName(UUID.randomUUID().toString());
        proj1 = (ProjectData) datamanagerFacility.saveAndReturnObject(rootCtx,
                proj1);

        DatasetData ds = new DatasetData();
        ds.setName(UUID.randomUUID().toString());
        ds = datamanagerFacility.createDataset(rootCtx, ds, proj1);
        proj1 = browseFacility.getProjects(rootCtx, Collections.singleton(proj1.getId())).iterator().next();

        DatasetData ds2 = new DatasetData();
        ds2.setName(UUID.randomUUID().toString());
        ds2 = datamanagerFacility.createDataset(rootCtx, ds2, proj1);

        proj1 = browseFacility.getProjects(rootCtx, Collections.singleton(proj1.getId())).iterator().next();
        Assert.assertEquals(proj1.getDatasets().size(), 2);

        ProjectData proj2 = new ProjectData();
        proj2.setName(UUID.randomUUID().toString());
        proj2 = (ProjectData) datamanagerFacility.saveAndReturnObject(rootCtx,
                proj2);

        datamanagerFacility.move(rootCtx, Arrays.asList(ds, ds2), proj1, proj2);

        proj1 = browseFacility
                .getProjects(rootCtx, Collections.singleton(proj1.getId()))
                .iterator().next();
        proj2 = browseFacility
                .getProjects(rootCtx, Collections.singleton(proj2.getId()))
                .iterator().next();
        Set<DatasetData> dsLoaded = proj2.getDatasets();
        Assert.assertEquals(dsLoaded.size(), 2);
        for (DatasetData d : dsLoaded)
            Assert.assertTrue(d.getId() == ds.getId() || d.getId() == ds2.getId());
        Assert.assertTrue(proj1.getDatasets().isEmpty());
    }

    @Test
    public void testChangeGroup() throws DSOutOfServiceException, DSAccessException {
        GroupData group = createGroup();
        DatasetData ds = new DatasetData();
        ds.setName(UUID.randomUUID().toString());
        ds = datamanagerFacility.createDataset(rootCtx, ds, proj);
        DatasetData ds2 = new DatasetData();
        ds2.setName(UUID.randomUUID().toString());
        ds2 = datamanagerFacility.createDataset(rootCtx, ds2, proj);
        Assert.assertEquals(ds.getGroupId(), rootCtx.getGroupID());
        Assert.assertEquals(ds2.getGroupId(), rootCtx.getGroupID());
        List<DatasetData> l = new ArrayList<>();
        l.add(ds);
        l.add(ds2);
        datamanagerFacility.changeGroup(rootCtx, l, group.getId());
        SecurityContext ctx2 = new SecurityContext(group.getId());
        Collection<Long> ids = l.stream().map(d -> d.getId()).collect(Collectors.toList());
        Collection<DatasetData> res = browseFacility.getDatasets(ctx2, ids);
        Assert.assertEquals(res.size(), 2);
        for (DatasetData d : res)
            Assert.assertEquals(group.getId(), d.getGroupId());
    }
    
    @Test
    public void testCreateDataset() throws DSOutOfServiceException, DSAccessException {
        //create dataset only
        DatasetData ds = new DatasetData();
        ds.setName(UUID.randomUUID().toString());
        ds = datamanagerFacility.createDataset(rootCtx, ds, null);
        Assert.assertNotNull(ds);
        
        //create dataset and project
        ds = new DatasetData();
        ds.setName(UUID.randomUUID().toString());
        ProjectData proj = new ProjectData();
        proj.setName(UUID.randomUUID().toString());
        ds = datamanagerFacility.createDataset(rootCtx, ds, proj);
        Assert.assertNotNull(ds);
        
        boolean found = false;
        Collection<ProjectData> projects = browseFacility.getProjects(rootCtx);
        for(ProjectData p : projects)
            if(p.getName().equals(proj.getName())) {
                found = true;
                proj = p;
                break;
            }
        Assert.assertTrue(found, "Project was not created!");
        
        found = false;
        Collection<DatasetData> datasets = proj.getDatasets();
        for(DatasetData d : datasets) 
            if(d.getName().equals(ds.getName())) {
                found = true;
                break;
            }
        Assert.assertTrue(found, "Project and Dataset not successfully linked!");
        
        // create dataset and add to existing project
        ds = new DatasetData();
        ds.setName(UUID.randomUUID().toString());
        ds = datamanagerFacility.createDataset(rootCtx, ds, proj);
        Assert.assertNotNull(ds);
        
        proj = browseFacility.getProjects(rootCtx, Arrays.asList(new Long[]{proj.getId()})).iterator().next();
        
        found = false;
        datasets = proj.getDatasets();
        for(DatasetData d : datasets) 
            if(d.getName().equals(ds.getName())) {
                found = true;
                break;
            }
        Assert.assertTrue(found, "Project and Dataset not successfully linked!");

        proj = new ProjectData();
        proj.setName(UUID.randomUUID().toString());
        proj = (ProjectData)datamanagerFacility.saveAndReturnObject(rootCtx, proj);
        // add one dataset
        ds = new DatasetData();
        ds.setName(UUID.randomUUID().toString());
        datamanagerFacility.createDataset(rootCtx, ds, proj);
        // do not reload and add another one
        ds = new DatasetData();
        ds.setName(UUID.randomUUID().toString());
        datamanagerFacility.createDataset(rootCtx, ds, proj);
        // check that the project contains the two datasets
        proj = browseFacility.getProjects(rootCtx, Arrays.asList(new Long[]{proj.getId()})).iterator().next();
        Assert.assertEquals(proj.getDatasets().size(), 2);
    }
}
