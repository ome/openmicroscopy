/*
 * $Id$
 *
 * Copyright 2006-2011 University of Dundee. All rights reserved.
 * Use is subject to license terms supplied in LICENSE.txt
 */
package integration.chgrp;

import static omero.rtypes.rdouble;
import static omero.rtypes.rint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import omero.api.IRenderingSettingsPrx;
import omero.api.delete.DeleteCommand;
import omero.cmd.Chgrp;
import omero.cmd.HandlePrx;
import omero.cmd.State;
import omero.grid.Column;
import omero.grid.LongColumn;
import omero.grid.TablePrx;
import omero.model.Channel;
import omero.model.ExperimenterGroup;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.LogicalChannel;
import omero.model.OriginalFile;
import omero.model.Pixels;
import omero.model.Plate;
import omero.model.PlateAcquisition;
import omero.model.PlateAnnotationLink;
import omero.model.PlateAnnotationLinkI;
import omero.model.PlateI;
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

import pojos.FileAnnotationData;

import integration.AbstractTest;
import integration.DeleteServiceTest;

/**
 * Collection of test to move hierarchies between groups.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class HierarchyMoveTest
	extends AbstractTest
{

	/**
	 * Moves the data.
	 *
	 * @param change The object hosting information about data to move.
	 * @return See above.
	 * @throws Exception
	 */
	private void doChange(Chgrp change)
		throws Exception
	{
		HandlePrx prx = null;
		try {
			prx = factory.submit(change);
			assertFalse(prx.getStatus().flags.contains(State.FAILURE));
			block(prx, 20, 4000);
			assertNotNull(prx.getResponse());
		} catch (Exception e) {
			if (prx != null) prx.close();
			throw e;
		}
	}

	/**
	 * Waits for data to be transfered.
	 *
	 * @param handle The handle.
	 * @param loops The number of loops.
	 * @param pause The time to pause.
	 * @throws Exception
	 */
    private void block(HandlePrx handle, int loops, long pause)
	throws Exception {
		for (int i = 0; i < loops && null == handle.getResponse(); i++) {
			Thread.sleep(pause);
		}
    }

    /**
     * Test to move an image w/o pixels between 2 private groups.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveBasicImage()
	throws Exception
    {
	String perms = "rw----";
	EventContext ctx = newUserAndGroup(perms);
	ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);
	Image img = (Image) iUpdate.saveAndReturnObject(
			mmFactory.createImage());
	long id = img.getId().getValue();
	doChange(new Chgrp(ctx.sessionUuid, DeleteServiceTest.REF_IMAGE, id,
			null, g.getId().getValue()));
	//Now check that the image is no longer in group
	ParametersI param = new ParametersI();
	param.addId(id);

	assertTrue(g.getId().getValue() != ctx.groupId);
	StringBuilder sb = new StringBuilder();
	sb.append("select i from Image i ");
	sb.append("where i.id = :id");
	assertNull(iQuery.findByQuery(sb.toString(), param));

	EventContext ec = loginUser(g);
	assertTrue(g.getId().getValue() == ec.groupId);
	assertNotNull(iQuery.findByQuery(sb.toString(), param));
    }

    /**
     * Test to move an image w/o pixels between 2 private groups.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveImage()
	throws Exception
    {
	String perms = "rw----";
	EventContext ctx = newUserAndGroup(perms);
	ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);
	Image img = mmFactory.createImage();
	img = (Image) iUpdate.saveAndReturnObject(img);
	Pixels pixels = img.getPrimaryPixels();
	long pixId = pixels.getId().getValue();
	//method already tested in PixelsServiceTest
	//make sure objects are loaded.
	pixels = factory.getPixelsService().retrievePixDescription(pixId);
	//channels.
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

	//Move the image
	doChange(new Chgrp(ctx.sessionUuid, DeleteServiceTest.REF_IMAGE, id,
			null, g.getId().getValue()));
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
			id =  i.next();
			param = new ParametersI();
		param.addId(id);
		sb = new StringBuilder();
		sb.append("select i from Channel i ");
		sb.append("where i.id = :id");
		assertNull(iQuery.findByQuery(sb.toString(), param));
		}
	i = infos.iterator();
	while (i.hasNext()) {
			id =  i.next();
			param = new ParametersI();
		param.addId(id);
		sb = new StringBuilder();
		sb.append("select i from StatsInfo i ");
		sb.append("where i.id = :id");
		assertNull(iQuery.findByQuery(sb.toString(), param));
		}
	i = logicalChannels.iterator();
	while (i.hasNext()) {
			id =  i.next();
			param = new ParametersI();
		param.addId(id);
		sb = new StringBuilder();
		sb.append("select i from LogicalChannel i ");
		sb.append("where i.id = :id");
		assertNull(iQuery.findByQuery(sb.toString(), param));
		}
	//Check that the data moved
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
			id =  i.next();
			param = new ParametersI();
		param.addId(id);
		sb = new StringBuilder();
		sb.append("select i from Channel i ");
		sb.append("where i.id = :id");
		assertNotNull(iQuery.findByQuery(sb.toString(), param));
		}
	i = infos.iterator();
	while (i.hasNext()) {
			id =  i.next();
			param = new ParametersI();
		param.addId(id);
		sb = new StringBuilder();
		sb.append("select i from StatsInfo i ");
		sb.append("where i.id = :id");
		assertNotNull(iQuery.findByQuery(sb.toString(), param));
		}
	i = logicalChannels.iterator();
	while (i.hasNext()) {
			id =  i.next();
			param = new ParametersI();
		param.addId(id);
		sb = new StringBuilder();
		sb.append("select i from LogicalChannel i ");
		sb.append("where i.id = :id");
		assertNotNull(iQuery.findByQuery(sb.toString(), param));
		}
    }

    /**
     * Test to move an image w/o pixels between 2 private groups.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveImageWithRenderingSettings()
	throws Exception
    {
	String perms = "rw----";
	EventContext ctx = newUserAndGroup(perms);
	ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);

	Image img = mmFactory.createImage();
	img = (Image) iUpdate.saveAndReturnObject(img);
	Pixels pixels = img.getPrimaryPixels();
	//method already tested in RenderingSettingsServiceTest
	IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
	List<Long> ids = new ArrayList<Long>();
	ids.add(img.getId().getValue());
	prx.resetDefaultsInSet(Image.class.getName(), ids);
	//check if we have settings now.
	ParametersI param = new ParametersI();
	param.addLong("pid", pixels.getId().getValue());
	String sql = "select rdef from RenderingDef as rdef " +
			"where rdef.pixels.id = :pid";
	List<IObject> settings = iQuery.findAllByQuery(sql, param);
	//now delete the image
	//assertTrue(settings.size() > 0);


	long id = img.getId().getValue();
	//Move the image
	doChange(new Chgrp(ctx.sessionUuid, DeleteServiceTest.REF_IMAGE, id,
			null, g.getId().getValue()));

	//check if the settings have been deleted.
	Iterator<IObject> i = settings.iterator();
	IObject o;
	while (i.hasNext()) {
			o = i.next();
			param = new ParametersI();
			param.addId(o.getId().getValue());
			sql = "select rdef from RenderingDef as rdef " +
			"where rdef.id = :id";
			assertNull(iQuery.findByQuery(sql, param));
		}
	//Log in to other group
	loginUser(g);
	id = img.getId().getValue();
	param = new ParametersI();
	param.addId(id);

	StringBuilder sb = new StringBuilder();
	sb.append("select i from Image i ");
	sb.append("where i.id = :id");
	assertNotNull(iQuery.findByQuery(sb.toString(), param));
	i = settings.iterator();
	while (i.hasNext()) {
			o = i.next();
			param = new ParametersI();
			param.addId(o.getId().getValue());
			sql = "select rdef from RenderingDef as rdef " +
			"where rdef.id = :id";
			assertNotNull(iQuery.findByQuery(sql, param));
		}
    }

    /**
     * Test to move an image with ROis.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveImageWithROIs()
	throws Exception
    {
	String perms = "rw----";
	EventContext ctx = newUserAndGroup(perms);
	ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);

	Image image = (Image) iUpdate.saveAndReturnObject(
			mmFactory.simpleImage(0));
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
	//Move the image.
	doChange(new Chgrp(ctx.sessionUuid, DeleteServiceTest.REF_IMAGE,
			image.getId().getValue(), null, g.getId().getValue()));

	//check if the objects have been delete.

	ParametersI param = new ParametersI();
	param.addId(serverROI.getId().getValue());
	String sql = "select d from Roi as d where d.id = :id";
	assertNull(iQuery.findByQuery(sql, param));

	//shapes
	param = new ParametersI();
	param.addIds(shapeIds);
	sql = "select d from Shape as d where d.id in (:ids)";
	List results = iQuery.findAllByQuery(sql, param);
	assertTrue(results.size() == 0);

	//Check that the data moved
	loginUser(g);
	param = new ParametersI();
	param.addId(serverROI.getId().getValue());
	sql = "select d from Roi as d where d.id = :id";
	assertNotNull(iQuery.findByQuery(sql, param));

	//shapes
	param = new ParametersI();
	param.addIds(shapeIds);
	sql = "select d from Shape as d where d.id in (:ids)";
	results = iQuery.findAllByQuery(sql, param);
	assertTrue(results.size() > 0);
    }

    /**
     * Test to move a populated plate.
     * The boolean flag indicates to create or no plate acquisition.
     * The <code>deleteQueue</code> method is tested.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMovePlate()
	throws Exception
    {
	String perms = "rw----";
	EventContext ctx = newUserAndGroup(perms);
	ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);

	Plate p;
	List results;
	PlateAcquisition pa = null;
	StringBuilder sb;
	Well well;
	WellSample field;
	Iterator j;
		ParametersI param;
		List<Long> wellSampleIds;
		List<Long> imageIds;
		p = (Plate) iUpdate.saveAndReturnObject(
				mmFactory.createPlate(1, 1, 1, 1, false));
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
        sb.append("select pa from PlateAcquisition as pa " +
			"where pa.plate.id = :plateID");
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
        //Now delete the plate
      //Move the plate.
	doChange(new Chgrp(ctx.sessionUuid, DeleteServiceTest.REF_PLATE,
			p.getId().getValue(), null, g.getId().getValue()));

        //check the well
        param = new ParametersI();
        param.addLong("plateID", p.getId().getValue());
        sb = new StringBuilder();
		sb.append("select well from Well as well ");
		sb.append("left outer join fetch well.plate as pt ");
		sb.append("where pt.id = :plateID");
		results = iQuery.findAllByQuery(sb.toString(), param);
        assertTrue(results.size() == 0);

        //check the well samples.
        sb = new StringBuilder();
        param = new ParametersI();
        param.addIds(wellSampleIds);
        sb.append("select p from WellSample as p where p.id in (:ids)");
        results = iQuery.findAllByQuery(sb.toString(), param);
        assertTrue(results.size() == 0);

        //check the image.
        sb = new StringBuilder();
        param = new ParametersI();
        param.addIds(imageIds);
        sb.append("select p from Image as p where p.id in (:ids)");
        results = iQuery.findAllByQuery(sb.toString(), param);
        assertTrue(results.size() == 0);
        if (pa != null) {
		param = new ParametersI();
	        param.addId(pa.getId().getValue());
	        sb = new StringBuilder();
	        //check the plate
	        sb.append("select p from PlateAcquisition as p " +
				"where p.id = :id");
	        assertNull(iQuery.findByQuery(sb.toString(), param));
        }
        loginUser(g);
      //check the well
        param = new ParametersI();
        param.addLong("plateID", p.getId().getValue());
        sb = new StringBuilder();
		sb.append("select well from Well as well ");
		sb.append("left outer join fetch well.plate as pt ");
		sb.append("where pt.id = :plateID");
		results = iQuery.findAllByQuery(sb.toString(), param);
        assertTrue(results.size() > 0);

        //check the well samples.
        sb = new StringBuilder();
        param = new ParametersI();
        param.addIds(wellSampleIds);
        sb.append("select p from WellSample as p where p.id in (:ids)");
        results = iQuery.findAllByQuery(sb.toString(), param);
        assertTrue(results.size() > 0);

        //check the image.
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
	        //check the plate
	        sb.append("select p from PlateAcquisition as p " +
				"where p.id = :id");
	        assertNotNull(iQuery.findByQuery(sb.toString(), param));
        }
    }

    /**
     * Tests to move a screen containing 2 plates, one w/o plate acquisition
     * and one with plate acquisition.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveScreen()
	throws Exception
    {
	String perms = "rw----";
	EventContext ctx = newUserAndGroup(perms);
	ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);

	Screen screen = (Screen) iUpdate.saveAndReturnObject(
			mmFactory.simpleScreenData().asIObject());
	//Plate w/o plate acquisition
	Plate p1 = (Plate) iUpdate.saveAndReturnObject(
			mmFactory.createPlate(1, 1, 1, 0, false));
	//Plate with plate acquisition
	Plate p2 = (Plate) iUpdate.saveAndReturnObject(
			mmFactory.createPlate(1, 1, 1, 1, false));
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


	doChange(new Chgrp(ctx.sessionUuid, DeleteServiceTest.REF_SCREEN,
			screen.getId().getValue(), null, g.getId().getValue()));


	List<Long> ids = new ArrayList<Long>();
	ids.add(p1.getId().getValue());
	ids.add(p2.getId().getValue());

	//Check if the plates exist.
	ParametersI param = new ParametersI();
	param.addIds(ids);
	String sql = "select i from Plate as i where i.id in (:ids)";
	List results = iQuery.findAllByQuery(sql, param);
	assertEquals(results.size(), 0);

	param = new ParametersI();
	param.addId(screen.getId().getValue());
	sql = "select i from Screen as i where i.id = :id";
	assertNull(iQuery.findByQuery(sql, param));

	//Check that the data moved
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
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveScreenWithReagent()
	throws Exception
    {
	String perms = "rw----";
	EventContext ctx = newUserAndGroup(perms);
	ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);

	Screen s = mmFactory.simpleScreenData().asScreen();
	Reagent r = mmFactory.createReagent();
	s.addReagent(r);
	Plate p = mmFactory.createPlateWithReagent(1, 1, 1, r);
	s.linkPlate(p);
	s = (Screen) iUpdate.saveAndReturnObject(s);
	long screenId = s.getId().getValue();
	//reagent first
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

	doChange(new Chgrp(ctx.sessionUuid, DeleteServiceTest.REF_SCREEN,
			screenId, null, g.getId().getValue()));

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

	//Check data moved
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
     * Tests to move a plate with a reagent.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(enabled = false)
    public void testMovePlateWithReagent()
	throws Exception
    {
	String perms = "rw----";
	EventContext ctx = newUserAndGroup(perms);
	ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);

	Screen s = mmFactory.simpleScreenData().asScreen();
	Reagent r = mmFactory.createReagent();
	s.addReagent(r);
	Plate p = mmFactory.createPlateWithReagent(1, 1, 1, r);
	s.linkPlate(p);
	s = (Screen) iUpdate.saveAndReturnObject(s);
	long screenId = s.getId().getValue();
	//reagent first
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

	doChange(new Chgrp(ctx.sessionUuid, DeleteServiceTest.REF_PLATE,
			plateID, null, g.getId().getValue()));

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

	//Check move
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
     * Test to delete a plate with ROI on images. The ROI will have
     * measurements.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeletePlateWithROIMeasurements()
		throws Exception
	{
	String perms = "rw----";
	EventContext ctx = newUserAndGroup(perms);
	ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);

	Plate p = (Plate) iUpdate.saveAndReturnObject(
				mmFactory.createPlate(1, 1, 1, 0, false));
	List<Well> results = loadWells(p.getId().getValue(), true);
	Well well = (Well) results.get(0);
	//create the roi.
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
        //First create a table
		String uuid = "Measurement_"+UUID.randomUUID().toString();
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
		//link fa to ROI
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

		doChange(new Chgrp(ctx.sessionUuid, DeleteServiceTest.REF_PLATE,
			p.getId().getValue(), null, g.getId().getValue()));

		//Shouldn't have measurements
		ParametersI param = new ParametersI();
        param.addId(id);
        StringBuilder sb = new StringBuilder();
		sb.append("select a from Annotation as a ");
		sb.append("where a.id = :id");
		assertTrue(iQuery.findAllByQuery(sb.toString(), param).size() == 0);

		loginUser(g);
		assertTrue(iQuery.findAllByQuery(sb.toString(), param).size() > 0);
	}
}
