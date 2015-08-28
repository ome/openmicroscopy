/*
 * Copyright 2006-2015 University of Dundee. All rights reserved.
 * Use is subject to license terms supplied in LICENSE.txt
 */

package integration.chgrp;

import static omero.rtypes.rdouble;
import static omero.rtypes.rint;
import integration.AbstractServerTest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import omero.cmd.Chgrp2;
import omero.gateway.util.Requests;
import omero.grid.Column;
import omero.grid.LongColumn;
import omero.grid.TablePrx;
import omero.model.Channel;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.ExperimenterGroup;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Instrument;
import omero.model.LogicalChannel;
import omero.model.OriginalFile;
import omero.model.Pixels;
import omero.model.Plate;
import omero.model.PlateAcquisition;
import omero.model.PlateAnnotationLink;
import omero.model.PlateAnnotationLinkI;
import omero.model.PlateI;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.Reagent;
import omero.model.Rect;
import omero.model.RectI;
import omero.model.Roi;
import omero.model.RoiAnnotationLink;
import omero.model.RoiAnnotationLinkI;
import omero.model.RoiI;
import omero.model.Screen;
import omero.model.ScreenPlateLink;
import omero.model.ScreenPlateLinkI;
import omero.model.Shape;
import omero.model.StatsInfo;
import omero.model.Well;
import omero.model.WellSample;
import omero.sys.EventContext;
import omero.sys.ParametersI;

import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;
import pojos.FileAnnotationData;

/**
 * Collection of test to move hierarchies between groups.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since 3.0-Beta4
 */
public class HierarchyMoveTest extends AbstractServerTest {

    /**
     * Test to move an image w/o pixels between 2 private groups.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveBasicImage() throws Exception {
        String perms = "rw----";
        EventContext ctx = newUserAndGroup(perms);
        ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);
        iAdmin.getEventContext(); // Refresh
        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        long id = img.getId().getValue();
        final Chgrp2 dc = Requests.chgrp("Image", id, g.getId().getValue());
        callback(true, client, dc);
        // Now check that the image is no longer in group
        ParametersI param = new ParametersI();
        param.addId(id);

        assertTrue(g.getId().getValue() != ctx.groupId);
        StringBuilder sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id = :id");
        assertNull(iQuery.findByQuery(sb.toString(), param));

        EventContext ec = loginUser(g);
        assertEquals(g.getId().getValue(), ec.groupId);
        assertNotNull(iQuery.findByQuery(sb.toString(), param));
    }

    /**
     * Test to move an image w/o pixels between 2 private groups.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveImage() throws Exception {
        String perms = "rw----";
        EventContext ctx = newUserAndGroup(perms);
        ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);
        iAdmin.getEventContext(); // Refresh
        Image img = mmFactory.createImage();
        img = (Image) iUpdate.saveAndReturnObject(img);
        Pixels pixels = img.getPrimaryPixels();
        long pixId = pixels.getId().getValue();
        // method already tested in PixelsServiceTest
        // make sure objects are loaded.
        pixels = factory.getPixelsService().retrievePixDescription(pixId);
        // channels.
        long id = img.getId().getValue();

        List<Long> channels = new ArrayList<Long>();
        List<Long> logicalChannels = new ArrayList<Long>();
        List<Long> infos = new ArrayList<Long>();
        Channel channel;
        LogicalChannel lc;
        StatsInfo info;
        for (int i = 0; i < pixels.getSizeC().getValue(); i++) {
            channel = pixels.getChannel(i);
            assertNotNull(channel);
            channels.add(channel.getId().getValue());
            lc = channel.getLogicalChannel();
            assertNotNull(lc);
            logicalChannels.add(lc.getId().getValue());
            info = channel.getStatsInfo();
            assertNotNull(info);
            infos.add(info.getId().getValue());
        }

        // Move the image
        final Chgrp2 dc = Requests.chgrp("Image", id, g.getId().getValue());
        callback(true, client, dc);
        ParametersI param = new ParametersI();
        param.addId(id);

        StringBuilder sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id = :id");
        assertNull(iQuery.findByQuery(sb.toString(), param));
        sb = new StringBuilder();
        param = new ParametersI();
        param.addId(pixId);
        sb.append("select i from Pixels i ");
        sb.append("where i.id = :id");
        assertNull(iQuery.findByQuery(sb.toString(), param));
        Iterator<Long> i = channels.iterator();
        while (i.hasNext()) {
            id = i.next();
            param = new ParametersI();
            param.addId(id);
            sb = new StringBuilder();
            sb.append("select i from Channel i ");
            sb.append("where i.id = :id");
            assertNull(iQuery.findByQuery(sb.toString(), param));
        }
        i = infos.iterator();
        while (i.hasNext()) {
            id = i.next();
            param = new ParametersI();
            param.addId(id);
            sb = new StringBuilder();
            sb.append("select i from StatsInfo i ");
            sb.append("where i.id = :id");
            assertNull(iQuery.findByQuery(sb.toString(), param));
        }
        i = logicalChannels.iterator();
        while (i.hasNext()) {
            id = i.next();
            param = new ParametersI();
            param.addId(id);
            sb = new StringBuilder();
            sb.append("select i from LogicalChannel i ");
            sb.append("where i.id = :id");
            assertNull(iQuery.findByQuery(sb.toString(), param));
        }
        // Check that the data moved
        loginUser(g);

        id = img.getId().getValue();
        param = new ParametersI();
        param.addId(id);

        sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id = :id");
        assertNotNull(iQuery.findByQuery(sb.toString(), param));
        sb = new StringBuilder();
        param = new ParametersI();
        param.addId(pixId);
        sb.append("select i from Pixels i ");
        sb.append("where i.id = :id");
        assertNotNull(iQuery.findByQuery(sb.toString(), param));
        i = channels.iterator();
        while (i.hasNext()) {
            id = i.next();
            param = new ParametersI();
            param.addId(id);
            sb = new StringBuilder();
            sb.append("select i from Channel i ");
            sb.append("where i.id = :id");
            assertNotNull(iQuery.findByQuery(sb.toString(), param));
        }
        i = infos.iterator();
        while (i.hasNext()) {
            id = i.next();
            param = new ParametersI();
            param.addId(id);
            sb = new StringBuilder();
            sb.append("select i from StatsInfo i ");
            sb.append("where i.id = :id");
            assertNotNull(iQuery.findByQuery(sb.toString(), param));
        }
        i = logicalChannels.iterator();
        while (i.hasNext()) {
            id = i.next();
            param = new ParametersI();
            param.addId(id);
            sb = new StringBuilder();
            sb.append("select i from LogicalChannel i ");
            sb.append("where i.id = :id");
            assertNotNull(iQuery.findByQuery(sb.toString(), param));
        }
    }

    /**
     * Test to move an image with ROis.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveImageWithROIs() throws Exception {
        String perms = "rw----";
        EventContext ctx = newUserAndGroup(perms);
        ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);
        iAdmin.getEventContext(); // Refresh

        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        Roi roi = new RoiI();
        roi.setImage(image);
        Rect rect;
        Roi serverROI = (Roi) iUpdate.saveAndReturnObject(roi);
        for (int i = 0; i < 3; i++) {
            rect = new RectI();
            rect.setX(rdouble(10));
            rect.setY(rdouble(10));
            rect.setWidth(rdouble(10));
            rect.setHeight(rdouble(10));
            rect.setTheZ(rint(i));
            rect.setTheT(rint(0));
            serverROI.addShape(rect);
        }
        serverROI = (RoiI) iUpdate.saveAndReturnObject(serverROI);
        List<Long> shapeIds = new ArrayList<Long>();
        Shape shape;
        for (int i = 0; i < serverROI.sizeOfShapes(); i++) {
            shape = serverROI.getShape(i);
            shapeIds.add(shape.getId().getValue());
        }
        // Move the image.
        final Chgrp2 dc = Requests.chgrp("Image", image.getId().getValue(), g.getId().getValue());
        callback(true, client, dc);

        // check if the objects have been delete.

        ParametersI param = new ParametersI();
        param.addId(serverROI.getId().getValue());
        String sql = "select d from Roi as d where d.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        // shapes
        param = new ParametersI();
        param.addIds(shapeIds);
        sql = "select d from Shape as d where d.id in (:ids)";
        List<IObject> results = iQuery.findAllByQuery(sql, param);
        assertEquals(results.size(), 0);

        // Check that the data moved
        loginUser(g);
        param = new ParametersI();
        param.addId(serverROI.getId().getValue());
        sql = "select d from Roi as d where d.id = :id";
        assertNotNull(iQuery.findByQuery(sql, param));

        // shapes
        param = new ParametersI();
        param.addIds(shapeIds);
        sql = "select d from Shape as d where d.id in (:ids)";
        results = iQuery.findAllByQuery(sql, param);
        assertTrue(results.size() > 0);
    }

    /**
     * Test to move a populated plate. Plate with plate acquisition.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMovePlate() throws Exception {
        String perms = "rw----";
        EventContext ctx = newUserAndGroup(perms);
        ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);
        iAdmin.getEventContext(); // Refresh

        Plate p;
        List<IObject> results;
        PlateAcquisition pa = null;
        StringBuilder sb;
        Well well;
        WellSample field;
        Iterator<IObject> j;
        ParametersI param;
        List<Long> wellSampleIds;
        List<Long> imageIds;
        p = (Plate) iUpdate.saveAndReturnObject(mmFactory.createPlate(1, 1, 1,
                1, false));
        param = new ParametersI();
        param.addLong("plateID", p.getId().getValue());
        sb = new StringBuilder();
        sb.append("select well from Well as well ");
        sb.append("left outer join fetch well.plate as pt ");
        sb.append("left outer join fetch well.wellSamples as ws ");
        sb.append("left outer join fetch ws.image as img ");
        sb.append("where pt.id = :plateID");
        results = iQuery.findAllByQuery(sb.toString(), param);

        sb = new StringBuilder();
        sb.append("select pa from PlateAcquisition as pa "
                + "where pa.plate.id = :plateID");
        pa = (PlateAcquisition) iQuery.findByQuery(sb.toString(), param);

        j = results.iterator();
        wellSampleIds = new ArrayList<Long>();
        imageIds = new ArrayList<Long>();
        while (j.hasNext()) {
            well = (Well) j.next();
            for (int k = 0; k < well.sizeOfWellSamples(); k++) {
                field = well.getWellSample(k);
                wellSampleIds.add(field.getId().getValue());
                assertNotNull(field.getImage());
                imageIds.add(field.getImage().getId().getValue());
            }
        }
        // Move the plate.
        final Chgrp2 dc = Requests.chgrp("Plate", p.getId().getValue(), g.getId().getValue());
        callback(true, client, dc);

        // check the well
        param = new ParametersI();
        param.addLong("plateID", p.getId().getValue());
        sb = new StringBuilder();
        sb.append("select well from Well as well ");
        sb.append("left outer join fetch well.plate as pt ");
        sb.append("where pt.id = :plateID");
        results = iQuery.findAllByQuery(sb.toString(), param);
        assertEquals(results.size(), 0);

        // check the well samples.
        sb = new StringBuilder();
        param = new ParametersI();
        param.addIds(wellSampleIds);
        sb.append("select p from WellSample as p where p.id in (:ids)");
        results = iQuery.findAllByQuery(sb.toString(), param);
        assertEquals(results.size(), 0);

        // check the image.
        sb = new StringBuilder();
        param = new ParametersI();
        param.addIds(imageIds);
        sb.append("select p from Image as p where p.id in (:ids)");
        results = iQuery.findAllByQuery(sb.toString(), param);
        assertEquals(results.size(), 0);
        if (pa != null) {
            param = new ParametersI();
            param.addId(pa.getId().getValue());
            sb = new StringBuilder();
            // check the plate
            sb.append("select p from PlateAcquisition as p "
                    + "where p.id = :id");
            assertNull(iQuery.findByQuery(sb.toString(), param));
        }
        loginUser(g);
        // check the well
        param = new ParametersI();
        param.addLong("plateID", p.getId().getValue());
        sb = new StringBuilder();
        sb.append("select well from Well as well ");
        sb.append("left outer join fetch well.plate as pt ");
        sb.append("where pt.id = :plateID");
        results = iQuery.findAllByQuery(sb.toString(), param);
        assertTrue(results.size() > 0);

        // check the well samples.
        sb = new StringBuilder();
        param = new ParametersI();
        param.addIds(wellSampleIds);
        sb.append("select p from WellSample as p where p.id in (:ids)");
        results = iQuery.findAllByQuery(sb.toString(), param);
        assertTrue(results.size() > 0);

        // check the image.
        sb = new StringBuilder();
        param = new ParametersI();
        param.addIds(imageIds);
        sb.append("select p from Image as p where p.id in (:ids)");
        results = iQuery.findAllByQuery(sb.toString(), param);
        assertTrue(results.size() > 0);
        if (pa != null) {
            param = new ParametersI();
            param.addId(pa.getId().getValue());
            sb = new StringBuilder();
            // check the plate
            sb.append("select p from PlateAcquisition as p "
                    + "where p.id = :id");
            assertNotNull(iQuery.findByQuery(sb.toString(), param));
        }
    }

    /**
     * Test to move a populated plate. Plate with no plate acquisition.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMovePlateNoPlateAcquisition() throws Exception {
        String perms = "rw----";
        EventContext ctx = newUserAndGroup(perms);
        ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);
        iAdmin.getEventContext(); // Refresh

        Plate p;
        List<IObject> results;
        StringBuilder sb;
        Well well;
        WellSample field;
        Iterator<IObject> j;
        ParametersI param;
        List<Long> wellSampleIds;
        List<Long> imageIds;
        p = (Plate) iUpdate.saveAndReturnObject(mmFactory.createPlate(1, 1, 1,
                0, false));
        param = new ParametersI();
        param.addLong("plateID", p.getId().getValue());
        sb = new StringBuilder();
        sb.append("select well from Well as well ");
        sb.append("left outer join fetch well.plate as pt ");
        sb.append("left outer join fetch well.wellSamples as ws ");
        sb.append("left outer join fetch ws.image as img ");
        sb.append("where pt.id = :plateID");
        results = iQuery.findAllByQuery(sb.toString(), param);

        j = results.iterator();
        wellSampleIds = new ArrayList<Long>();
        imageIds = new ArrayList<Long>();
        while (j.hasNext()) {
            well = (Well) j.next();
            for (int k = 0; k < well.sizeOfWellSamples(); k++) {
                field = well.getWellSample(k);
                wellSampleIds.add(field.getId().getValue());
                assertNotNull(field.getImage());
                imageIds.add(field.getImage().getId().getValue());
            }
        }

        // Move the plate.
        final Chgrp2 dc = Requests.chgrp("Plate", p.getId().getValue(), g.getId().getValue());
        callback(true, client, dc);

        // check the well
        param = new ParametersI();
        param.addLong("plateID", p.getId().getValue());
        sb = new StringBuilder();
        sb.append("select well from Well as well ");
        sb.append("left outer join fetch well.plate as pt ");
        sb.append("where pt.id = :plateID");
        results = iQuery.findAllByQuery(sb.toString(), param);
        assertEquals(results.size(), 0);

        // check the well samples.
        sb = new StringBuilder();
        param = new ParametersI();
        param.addIds(wellSampleIds);
        sb.append("select p from WellSample as p where p.id in (:ids)");
        results = iQuery.findAllByQuery(sb.toString(), param);
        assertEquals(results.size(), 0);

        // check the image.
        sb = new StringBuilder();
        param = new ParametersI();
        param.addIds(imageIds);
        sb.append("select p from Image as p where p.id in (:ids)");
        results = iQuery.findAllByQuery(sb.toString(), param);
        assertEquals(results.size(), 0);

        loginUser(g);
        // check the well
        param = new ParametersI();
        param.addLong("plateID", p.getId().getValue());
        sb = new StringBuilder();
        sb.append("select well from Well as well ");
        sb.append("left outer join fetch well.plate as pt ");
        sb.append("where pt.id = :plateID");
        results = iQuery.findAllByQuery(sb.toString(), param);
        assertTrue(results.size() > 0);

        // check the well samples.
        sb = new StringBuilder();
        param = new ParametersI();
        param.addIds(wellSampleIds);
        sb.append("select p from WellSample as p where p.id in (:ids)");
        results = iQuery.findAllByQuery(sb.toString(), param);
        assertTrue(results.size() > 0);

        // check the image.
        sb = new StringBuilder();
        param = new ParametersI();
        param.addIds(imageIds);
        sb.append("select p from Image as p where p.id in (:ids)");
        results = iQuery.findAllByQuery(sb.toString(), param);
        assertTrue(results.size() > 0);
    }

    /**
     * Tests to move a screen containing 2 plates, one w/o plate acquisition and
     * one with plate acquisition.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveScreen() throws Exception {
        String perms = "rw----";
        EventContext ctx = newUserAndGroup(perms);
        ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);
        iAdmin.getEventContext(); // Refresh

        Screen screen = (Screen) iUpdate.saveAndReturnObject(mmFactory
                .simpleScreenData().asIObject());
        // Plate w/o plate acquisition
        Plate p1 = (Plate) iUpdate.saveAndReturnObject(mmFactory.createPlate(1,
                1, 1, 0, false));
        // Plate with plate acquisition
        Plate p2 = (Plate) iUpdate.saveAndReturnObject(mmFactory.createPlate(1,
                1, 1, 1, false));
        List<IObject> links = new ArrayList<IObject>();
        ScreenPlateLink link = new ScreenPlateLinkI();
        link.setChild(p1);
        link.setParent(screen);
        links.add(link);
        link = new ScreenPlateLinkI();
        link.setChild(p2);
        link.setParent(screen);
        links.add(link);
        iUpdate.saveAndReturnArray(links);

        final Chgrp2 dc = Requests.chgrp("Screen", screen.getId().getValue(), g.getId().getValue());
        callback(true, client, dc);

        List<Long> ids = new ArrayList<Long>();
        ids.add(p1.getId().getValue());
        ids.add(p2.getId().getValue());

        // Check if the plates exist.
        ParametersI param = new ParametersI();
        param.addIds(ids);
        String sql = "select i from Plate as i where i.id in (:ids)";
        List<IObject> results = iQuery.findAllByQuery(sql, param);
        assertEquals(results.size(), 0);

        param = new ParametersI();
        param.addId(screen.getId().getValue());
        sql = "select i from Screen as i where i.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        // Check that the data moved
        loginUser(g);
        param = new ParametersI();
        param.addIds(ids);
        sql = "select i from Plate as i where i.id in (:ids)";
        results = iQuery.findAllByQuery(sql, param);
        assertTrue(results.size() > 0);

        param = new ParametersI();
        param.addId(screen.getId().getValue());
        sql = "select i from Screen as i where i.id = :id";
        assertNotNull(iQuery.findByQuery(sql, param));
    }

    /**
     * Tests to move screen with a plate and a reagent.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveScreenWithReagent() throws Exception {
        String perms = "rw----";
        EventContext ctx = newUserAndGroup(perms);
        ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);
        iAdmin.getEventContext(); // Refresh

        Screen s = mmFactory.simpleScreenData().asScreen();
        Reagent r = mmFactory.createReagent();
        s.addReagent(r);
        Plate p = mmFactory.createPlateWithReagent(1, 1, 1, r);
        s.linkPlate(p);
        s = (Screen) iUpdate.saveAndReturnObject(s);
        long screenId = s.getId().getValue();
        // reagent first
        String sql = "select r from Reagent as r ";
        sql += "join fetch r.screen as s ";
        sql += "where s.id = :id";
        ParametersI param = new ParametersI();
        param.addId(screenId);
        r = (Reagent) iQuery.findByQuery(sql, param);
        long reagentID = r.getId().getValue();
        //
        sql = "select s from ScreenPlateLink as s ";
        sql += "join fetch s.child as c ";
        sql += "join fetch s.parent as p ";
        sql += "where p.id = :id";
        param = new ParametersI();
        param.addId(screenId);
        ScreenPlateLink link = (ScreenPlateLink) iQuery.findByQuery(sql, param);
        p = link.getChild();
        long plateID = p.getId().getValue();

        final Chgrp2 dc = Requests.chgrp("Screen", screenId, g.getId().getValue());
        callback(true, client, dc);

        sql = "select r from Screen as r ";
        sql += "where r.id = :id";
        param = new ParametersI();
        param.addId(screenId);
        assertNull(iQuery.findByQuery(sql, param));

        sql = "select r from Reagent as r ";
        sql += "where r.id = :id";
        param = new ParametersI();
        param.addId(reagentID);
        assertNull(iQuery.findByQuery(sql, param));

        sql = "select r from Plate as r ";
        sql += "where r.id = :id";
        param = new ParametersI();
        param.addId(plateID);
        assertNull(iQuery.findByQuery(sql, param));

        // Check data moved
        loginUser(g);
        sql = "select r from Screen as r ";
        sql += "where r.id = :id";
        param = new ParametersI();
        param.addId(screenId);
        assertNotNull(iQuery.findByQuery(sql, param));

        sql = "select r from Reagent as r ";
        sql += "where r.id = :id";
        param = new ParametersI();
        param.addId(reagentID);
        assertNotNull(iQuery.findByQuery(sql, param));

        sql = "select r from Plate as r ";
        sql += "where r.id = :id";
        param = new ParametersI();
        param.addId(plateID);
        assertNotNull(iQuery.findByQuery(sql, param));
    }

    /**
     * Tests to move a plate with a reagent. The test now passes with or w/o the
     * FORCE option. Similar to delete
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMovePlateWithReagent() throws Exception {
        String perms = "rw----";
        EventContext ctx = newUserAndGroup(perms);
        ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);
        iAdmin.getEventContext(); // Refresh

        Screen s = mmFactory.simpleScreenData().asScreen();
        Reagent r = mmFactory.createReagent();
        s.addReagent(r);
        Plate p = mmFactory.createPlateWithReagent(1, 1, 1, r);
        s.linkPlate(p);
        s = (Screen) iUpdate.saveAndReturnObject(s);
        long screenId = s.getId().getValue();
        // reagent first
        String sql = "select r from Reagent as r ";
        sql += "join fetch r.screen as s ";
        sql += "where s.id = :id";
        ParametersI param = new ParametersI();
        param.addId(screenId);
        r = (Reagent) iQuery.findByQuery(sql, param);
        long reagentID = r.getId().getValue();
        //
        sql = "select s from ScreenPlateLink as s ";
        sql += "join fetch s.child as c ";
        sql += "join fetch s.parent as p ";
        sql += "where p.id = :id";
        param = new ParametersI();
        param.addId(screenId);
        ScreenPlateLink link = (ScreenPlateLink) iQuery.findByQuery(sql, param);
        p = link.getChild();
        long plateID = p.getId().getValue();
        final Chgrp2 dc = Requests.chgrp("Plate", plateID, g.getId().getValue());
        callback(true, client, dc);
        sql = "select r from Screen as r ";
        sql += "where r.id = :id";
        param = new ParametersI();
        param.addId(screenId);
        assertNotNull(iQuery.findByQuery(sql, param));

        sql = "select r from Reagent as r ";
        sql += "where r.id = :id";
        param = new ParametersI();
        param.addId(reagentID);
        assertNotNull(iQuery.findByQuery(sql, param));

        sql = "select r from Plate as r ";
        sql += "where r.id = :id";
        param = new ParametersI();
        param.addId(plateID);
        assertNull(iQuery.findByQuery(sql, param));

        // Check move
        loginUser(g);
        sql = "select r from Screen as r ";
        sql += "where r.id = :id";
        param = new ParametersI();
        param.addId(screenId);
        assertNull(iQuery.findByQuery(sql, param));

        sql = "select r from Reagent as r ";
        sql += "where r.id = :id";
        param = new ParametersI();
        param.addId(reagentID);
        assertNull(iQuery.findByQuery(sql, param));

        sql = "select r from Plate as r ";
        sql += "where r.id = :id";
        param = new ParametersI();
        param.addId(plateID);
        assertNotNull(iQuery.findByQuery(sql, param));
    }

    /**
     * Test to move a plate with ROI on images. The ROI will have measurements.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeletePlateWithROIMeasurements() throws Exception {
        String perms = "rw----";
        EventContext ctx = newUserAndGroup(perms);
        ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);
        iAdmin.getEventContext(); // Refresh

        Plate p = (Plate) iUpdate.saveAndReturnObject(mmFactory.createPlate(1,
                1, 1, 0, false));
        List<Well> results = loadWells(p.getId().getValue(), true);
        Well well = (Well) results.get(0);
        // create the roi.
        Image image = well.getWellSample(0).getImage();
        Roi roi = new RoiI();
        roi.setImage(image);
        Rect rect;
        roi = (Roi) iUpdate.saveAndReturnObject(roi);
        for (int i = 0; i < 3; i++) {
            rect = new RectI();
            rect.setX(rdouble(10));
            rect.setY(rdouble(10));
            rect.setWidth(rdouble(10));
            rect.setHeight(rdouble(10));
            rect.setTheZ(rint(i));
            rect.setTheT(rint(0));
            roi.addShape(rect);
        }
        // First create a table
        String uuid = "Measurement_" + UUID.randomUUID().toString();
        TablePrx table = factory.sharedResources().newTable(1, uuid);
        Column[] columns = new Column[1];
        columns[0] = new LongColumn("Uid", "", new long[1]);
        table.initialize(columns);
        assertNotNull(table);
        OriginalFile of = table.getOriginalFile();
        assertTrue(of.getId().getValue() > 0);
        FileAnnotation fa = new FileAnnotationI();
        fa.setNs(omero.rtypes.rstring(FileAnnotationData.MEASUREMENT_NS));
        fa.setFile(of);
        fa = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
        long id = fa.getId().getValue();
        // link fa to ROI
        List<IObject> links = new ArrayList<IObject>();
        RoiAnnotationLink rl = new RoiAnnotationLinkI();
        rl.setChild(new FileAnnotationI(id, false));
        rl.setParent(new RoiI(roi.getId().getValue(), false));
        links.add(rl);
        PlateAnnotationLink il = new PlateAnnotationLinkI();
        il.setChild(new FileAnnotationI(id, false));
        il.setParent(new PlateI(p.getId().getValue(), false));
        links.add(il);
        iUpdate.saveAndReturnArray(links);

        final Chgrp2 dc = Requests.chgrp("Plate", p.getId().getValue(), g.getId().getValue());
        callback(true, client, dc);
 
        // Shouldn't have measurements
        ParametersI param = new ParametersI();
        param.addId(id);
        StringBuilder sb = new StringBuilder();
        sb.append("select a from Annotation as a ");
        sb.append("where a.id = :id");
        assertEquals(iQuery.findAllByQuery(sb.toString(), param).size(), 0);

        loginUser(g);
        assertTrue(iQuery.findAllByQuery(sb.toString(), param).size() > 0);
    }

    /**
     * Tests to move a project containing a dataset with images.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveProject() throws Exception {
        String perms = "rw----";
        EventContext ctx = newUserAndGroup(perms);
        ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);
        iAdmin.getEventContext(); // Refresh

        Project p = (Project) iUpdate.saveAndReturnObject(mmFactory
                .simpleProjectData().asIObject());
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        Image image1 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        Image image2 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        List<IObject> links = new ArrayList<IObject>();
        DatasetImageLink link = new DatasetImageLinkI();
        link.setChild(image1);
        link.setParent(d);
        links.add(link);

        link = new DatasetImageLinkI();
        link.setChild(image2);
        link.setParent(d);
        links.add(link);

        ProjectDatasetLink l = new ProjectDatasetLinkI();
        l.setChild(d);
        l.setParent(p);
        links.add(l);
        iUpdate.saveAndReturnArray(links);

        List<Long> ids = new ArrayList<Long>();
        ids.add(image1.getId().getValue());
        ids.add(image2.getId().getValue());

        final Chgrp2 dc = Requests.chgrp("Project", p.getId().getValue(), g.getId().getValue());
        callback(true, client, dc);

        // Check if objects have been deleted
        ParametersI param = new ParametersI();
        param.addIds(ids);
        String sql = "select i from Image as i where i.id in (:ids)";
        List<IObject> results = iQuery.findAllByQuery(sql, param);
        assertEquals(results.size(), 0);

        param = new ParametersI();
        param.addId(d.getId().getValue());
        sql = "select i from Dataset as i where i.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        param = new ParametersI();
        param.addId(p.getId().getValue());
        sql = "select i from Project as i where i.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        // Logger in to other group
        loginUser(g);

        param = new ParametersI();
        param.addIds(ids);
        sql = "select i from Image as i where i.id in (:ids)";
        results = iQuery.findAllByQuery(sql, param);
        assertEquals(results.size(), ids.size());

        param = new ParametersI();
        param.addId(d.getId().getValue());
        sql = "select i from Dataset as i where i.id = :id";
        assertNotNull(iQuery.findByQuery(sql, param));

        param = new ParametersI();
        param.addId(p.getId().getValue());
        sql = "select i from Project as i where i.id = :id";
        assertNotNull(iQuery.findByQuery(sql, param));
    }

    /**
     * Tests to move a screen containing a plate also contained in another
     * screen. The screen should be moved but not the plate.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveScreenWithSharedPlate() throws Exception {
        String perms = "rw----";
        EventContext ctx = newUserAndGroup(perms);
        ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);
        iAdmin.getEventContext(); // Refresh

        Screen s1 = (Screen) iUpdate.saveAndReturnObject(mmFactory
                .simpleScreenData().asIObject());

        Screen s2 = (Screen) iUpdate.saveAndReturnObject(mmFactory
                .simpleScreenData().asIObject());

        // Plate w/o plate acquisition
        Plate p1 = (Plate) iUpdate.saveAndReturnObject(mmFactory.createPlate(1,
                1, 1, 0, false));
        // Plate with plate acquisition
        List<IObject> links = new ArrayList<IObject>();
        ScreenPlateLink link = new ScreenPlateLinkI();
        link.setChild((Plate) p1.proxy());
        link.setParent(s1);
        links.add(link);
        link = new ScreenPlateLinkI();
        link.setChild((Plate) p1.proxy());
        link.setParent(s2);
        links.add(link);
        iUpdate.saveAndReturnArray(links);

        final Chgrp2 dc = Requests.chgrp("Screen", s1.getId().getValue(), g.getId().getValue());
        callback(true, client, dc);

        List<Long> ids = new ArrayList<Long>();
        ids.add(p1.getId().getValue());

        // Check if the plates exist.
        ParametersI param = new ParametersI();
        param.addIds(ids);
        String sql = "select i from Plate as i where i.id in (:ids)";
        List<IObject> results = iQuery.findAllByQuery(sql, param);
        assertEquals(results.size(), ids.size());

        param = new ParametersI();
        param.addId(s1.getId().getValue());
        sql = "select i from Screen as i where i.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        param = new ParametersI();
        param.addId(s2.getId().getValue());
        assertNotNull(iQuery.findByQuery(sql, param));

        loginUser(g);
        param = new ParametersI();
        param.addIds(ids);
        // plate should not have moved.
        sql = "select i from Plate as i where i.id in (:ids)";
        results = iQuery.findAllByQuery(sql, param);
        assertEquals(results.size(), 0);

        // screen should move
        param = new ParametersI();
        param.addId(s1.getId().getValue());
        sql = "select i from Screen as i where i.id = :id";
        assertNotNull(iQuery.findByQuery(sql, param));
    }

    /**
     * Test to move a dataset containing an image whose projection is not in the dataset.
     * The image should be left behind but others should still move.
     * @throws Exception unexpected
     */
    @Test
    public void testMoveDatasetWithProjectedImage() throws Exception {
        /* prepare a pair of groups for the user */
        newUserAndGroup("rwr---");
        final EventContext ctx = iAdmin.getEventContext();
        final ExperimenterGroup source = iAdmin.getGroup(ctx.groupId);
        final ExperimenterGroup destination = newGroupAddUser("rwr---", ctx.userId);

        /* start in the source group */
        loginUser(source);

        /* create a dataset */
        Dataset dataset = new DatasetI();
        dataset.setName(omero.rtypes.rstring("dataset"));
        dataset = (Dataset) iUpdate.saveAndReturnObject(dataset).proxy();

        /* create an instrument */
        Instrument instrument = mmFactory.createInstrument();
        instrument = (Instrument) iUpdate.saveAndReturnObject(instrument).proxy();

        /* the original and its projection are related and share an instrument */
        Image original = mmFactory.createImage();
        original.setInstrument(instrument);
        original = (Image) iUpdate.saveAndReturnObject(original);
        Image projection = mmFactory.createImage();
        projection.setInstrument(instrument);
        projection.getPrimaryPixels().setRelatedTo((Pixels) original.getPrimaryPixels().proxy());
        projection = (Image) iUpdate.saveAndReturnObject(projection);

        original = (Image) original.proxy();
        projection = (Image) projection.proxy();

        /* create another image */
        Image other = mmFactory.createImage();
        other = (Image) iUpdate.saveAndReturnObject(other).proxy();

        /* only the original and the other are in the dataset; the projection is not */
        for (final Image image : new Image[] {original, other}) {
            final DatasetImageLink link = new DatasetImageLinkI();
            link.setParent(dataset);
            link.setChild(image);
            iUpdate.saveAndReturnObject(link);
        }

        /* move the dataset */
        final Chgrp2 chgrp = Requests.chgrp("Dataset", dataset.getId().getValue(), destination.getId().getValue());
        callback(true, client, chgrp);

        /* check what remains in the source group */
        assertNull(iQuery.findByQuery("FROM Dataset WHERE id = :id", new ParametersI().addId(dataset.getId())));
        assertNotNull(iQuery.findByQuery("FROM Image WHERE id = :id", new ParametersI().addId(original.getId())));
        assertNotNull(iQuery.findByQuery("FROM Image WHERE id = :id", new ParametersI().addId(projection.getId())));
        assertNull(iQuery.findByQuery("FROM Image WHERE id = :id", new ParametersI().addId(other.getId())));

        /* switch to the destination group */
        loginUser(destination);

        /* check what was moved to the destination group */
        assertNotNull(iQuery.findByQuery("FROM Dataset WHERE id = :id", new ParametersI().addId(dataset.getId())));
        assertNull(iQuery.findByQuery("FROM Image WHERE id = :id", new ParametersI().addId(original.getId())));
        assertNull(iQuery.findByQuery("FROM Image WHERE id = :id", new ParametersI().addId(projection.getId())));
        assertNotNull(iQuery.findByQuery("FROM Image WHERE id = :id", new ParametersI().addId(other.getId())));
    }
}
