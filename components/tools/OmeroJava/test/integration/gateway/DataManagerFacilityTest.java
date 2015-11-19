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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import omero.RLong;
import omero.api.IPixelsPrx;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.facility.Callback;
import omero.model.IObject;
import omero.model.PixelsType;

import org.testng.Assert;
import org.testng.annotations.Test;

import omero.gateway.model.AnnotationData;
import omero.gateway.model.BooleanAnnotationData;
import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.DoubleAnnotationData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.ImageData;
import omero.gateway.model.LongAnnotationData;
import omero.gateway.model.MapAnnotationData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.RatingAnnotationData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.model.TermAnnotationData;
import omero.gateway.model.TextualAnnotationData;
import omero.gateway.model.XMLAnnotationData;

/**
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class DataManagerFacilityTest extends GatewayTest {

    ProjectData proj;
    DatasetData ds;
    ImageData img;
    
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
    public void testDeleteObject() throws DSOutOfServiceException, DSAccessException {
        datamanagerFacility.deleteObject(rootCtx, img.asIObject());
        List<Long> ids = new ArrayList<Long>(1);
        ids.add(img.getId());
        Collection<ImageData> img = browseFacility.getImages(rootCtx, ids);
        Assert.assertTrue(img.isEmpty());
    }

    @Test
    public void testAttachFile() throws Exception {
        File tmp = File.createTempFile("tmp", "file");
        File tmp2 = File.createTempFile("tmp2", "file");
        BufferedWriter out = new BufferedWriter(new FileWriter(tmp));
        out.write("Just a test");
        out.close();
        out = new BufferedWriter(new FileWriter(tmp2));
        out.write("Just a test - 2");
        out.close();
        
        DatasetData ds = new DatasetData();
        ds.setName(UUID.randomUUID().toString());
        ds = (DatasetData) datamanagerFacility.saveAndReturnObject(rootCtx, ds);

        // async test
        Callback cb = new Callback() {

            @Override
            public void handleResult(Object result) {
                Assert.assertNotNull(result, "File upload was not successful!");
                Assert.assertTrue(((FileAnnotationData) result).getId() >= 0,
                        "File upload was not successful!");
            }

            @Override
            public void handleException(Throwable t) {
                Assert.assertTrue(false, "File upload was not successful!");
            }

        };

        datamanagerFacility.attachFile(rootCtx, tmp, "text/plain", "test", null, ds, cb);

        while (!cb.isFinished()) {
            Thread.sleep(100);
        }
        
        // sync test
        FileAnnotationData fa = datamanagerFacility.attachFile(rootCtx, tmp2, "text/plain", "test", null, ds, null);
        Assert.assertNotNull(fa, "File upload was not successful!");
        Assert.assertTrue(fa.getId() >= 0,
                "File upload was not successful!");
        
        tmp.delete();
        tmp2.delete();
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
