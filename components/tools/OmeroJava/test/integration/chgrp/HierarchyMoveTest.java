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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import omero.api.Save;
import omero.cmd.Chgrp;
import omero.cmd.DoAll;
import omero.cmd.Request;
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
import omero.model.ProjectI;
import omero.model.Reagent;
import omero.model.Rectangle;
import omero.model.RectangleI;
import omero.model.ROI;
import omero.model.ROIAnnotationLink;
import omero.model.ROIAnnotationLinkI;
import omero.model.ROII;
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

import integration.AbstractServerTest;
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
	extends AbstractServerTest
{

	/** Indicates to move but not to link.*/
	private static final int LINK_NONE = 0;
	
	/** Indicates to move and create a new object and link it.*/
	private static final int LINK_NEW = 1;
	
	/** Indicates to move and link to an existing object.*/
	private static final int LINK_EXISTING = 2;
	
	/** Performs the move as data owner.*/
	private static final int DATA_OWNER = 100;
	
	/** Performs the move as group owner.*/
	private static final int GROUP_OWNER = 101;
	
	/** Performs the move as group owner.*/
	private static final int ADMIN = 102;
	
	/**
     * Tests the move of a dataset to a new group, a project in the new group
     * is selected and the dataset should be linked to that project.
     * 
     * @param source The permissions of the source group.
     * @param target The permissions of the destination group.
     * @param newDestinationObject Pass <code>true</code> if the project has to 
     * be created, <code>false</code> otherwise
     * @param asAdmin Pass <code>true</code> to move the data as admin
     * <code>false</code> otherwise. 
     */
    private void moveDataDatasetToProject(String source, String target, 
    		int linkLevel, int memberLevel)
    throws Exception
    {
    	//Step 1
    	//Create the group with the dataset
    	EventContext ctx = newUserAndGroup(source);
    	
    	if (memberLevel == GROUP_OWNER)
    		makeGroupOwner();
    	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
    			mmFactory.simpleDatasetData().asIObject());
    	
    	//log out
    	disconnect();
    	
    	//Create a new group, the user is now a member of the new group.
    	ExperimenterGroup g = newGroupAddUser(target, ctx.userId);
    	
    	loginUser(g);
    	if (memberLevel == GROUP_OWNER)
    		makeGroupOwner();
        //Create project in the new group.
    	Project p = (Project) iUpdate.saveAndReturnObject(
    			mmFactory.simpleProjectData().asIObject());

    	//log out
    	disconnect();
    	
    	//Step 2: log into source group to perform the move
    	switch (memberLevel) {
			case DATA_OWNER:
			case GROUP_OWNER:
				default:
				loginUser(ctx);
				break;
			case ADMIN:
				logRootIntoGroup(ctx.groupId);
		}
    	
    	
    	//Create commands to move and create the link in target 
    	List<Request> list = new ArrayList<Request>();
    	list.add(new Chgrp(DeleteServiceTest.REF_DATASET,
    			d.getId().getValue(), null, g.getId().getValue()));
    	
    	ProjectDatasetLink link = null;
    	switch (linkLevel) {
			case LINK_NEW:
				link = new ProjectDatasetLinkI();
		    	link.setChild(new DatasetI(d.getId().getValue(), false));
		    	link.setParent(new ProjectI());
				break;
			case LINK_EXISTING:
				link = new ProjectDatasetLinkI();
		    	link.setChild(new DatasetI(d.getId().getValue(), false));
		    	link.setParent(new ProjectI(p.getId().getValue(), false));
		}
    	
    	if (link != null) {
    		Save cmd = new Save();
        	cmd.obj = link;
        	list.add(cmd);
    	}
    	DoAll all = new DoAll();
    	all.requests = list;
    	
    	//Do the move.
    	doChange(all);

   
    	//Check if the dataset has been removed.
    	ParametersI param = new ParametersI();
    	param.addId(d.getId().getValue());
    	String sql = "select i from Dataset as i where i.id = :id";
    	assertNull(iQuery.findByQuery(sql, param));

    	//log out from source group
    	disconnect();
    	
    	//Step 3:
    	
    	//Connect to target group
    	
    	//Step 2: log into source group to perform the move
    	switch (memberLevel) {
			case DATA_OWNER:
			case GROUP_OWNER:
				default:
				loginUser(g);
				break;
			case ADMIN:
				logRootIntoGroup(g.getId().getValue());
		}
    	param = new ParametersI();
    	param.addId(d.getId().getValue());
    	sql = "select i from Dataset as i where i.id = :id";
    	
    	//Check if the dataset is in the target group.
    	assertNotNull(iQuery.findByQuery(sql, param));

    	//Check the link exists.
    	if (link != null) {
    		param = new ParametersI();
        	param.map.put("childID", d.getId());
        	if (linkLevel == LINK_EXISTING) {
        		param.map.put("parentID", p.getId());
            	sql = "select i from ProjectDatasetLink as i where " +
            			"i.child.id = :childID and i.parent.id = :parentID";
        	} else {
        		sql = "select i from ProjectDatasetLink as i where " +
    			"i.child.id = :childID";
        	}
        	assertNotNull(iQuery.findByQuery(sql, param));
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
	iAdmin.getEventContext(); // Refresh
	Image img = createBasicImage();
	long id = img.getId().getValue();
	doChange(new Chgrp(DeleteServiceTest.REF_IMAGE, id,
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
	iAdmin.getEventContext(); // Refresh
	Image img = createBasicImage();
	Pixels pixels = img.getPixels();
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
			info = channel.getStatsInfo();
			assertNotNull(info);
			infos.add(info.getId().getValue());
		}

	//Move the image
	doChange(new Chgrp(DeleteServiceTest.REF_IMAGE, id,
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
	iAdmin.getEventContext(); // Refresh

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
	doChange(new Chgrp(DeleteServiceTest.REF_IMAGE,
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
     * Test to move a populated plate. Plate with plate acquisition.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMovePlate() throws Exception
    {
	String perms = "rw----";
	EventContext ctx = newUserAndGroup(perms);
	ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);
	iAdmin.getEventContext(); // Refresh

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
	    List results = iQuery.findAllByQuery(sb.toString(), param);
	
	    sb = new StringBuilder();
	    sb.append("select pa from PlateAcquisition as pa " +
				"where pa.plate.id = :plateID");
	    pa = (PlateAcquisition) iQuery.findByQuery(sb.toString(), param);
	
	    Iterator j = results.iterator();
	    List<Long> wellSampleIds = new ArrayList<Long>();
	    List<Long> imageIds = new ArrayList<Long>();
	    
	    Iterator<WellSample> k;
	    while (j.hasNext()) {
			well = (Well) j.next();
			k = well.copyWellSamples().iterator();
			while (k.hasNext()) {
				field = k.next();
				wellSampleIds.add(field.getId().getValue());
				assertNotNull(field.getImage());
				imageIds.add(field.getImage().getId().getValue());
			}
		}
        //Now delete the plate
      //Move the plate.
	doChange(new Chgrp(DeleteServiceTest.REF_PLATE,
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
     * Test to move a populated plate. Plate with no plate acquisition.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMovePlateNoPlateAcquisition() throws Exception
    {
	String perms = "rw----";
	EventContext ctx = newUserAndGroup(perms);
	ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);
	iAdmin.getEventContext(); // Refresh

	Plate p;
	List results;
	StringBuilder sb;
	Well well;
	WellSample field;
	Iterator j;
		ParametersI param;
		List<Long> wellSampleIds;
		List<Long> imageIds;

		Plate p = mmFactory.createPlate(1, 1, 1, 0, false);
		p = savePlate(iUpdate, p, false);
		
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
        Iterator<WellSample> k;
        while (j.hasNext()) {
			well = (Well) j.next();
			k = well.copyWellSamples().iterator();
			while (k.hasNext()) {
				field = k.next();
				wellSampleIds.add(field.getId().getValue());
				assertNotNull(field.getImage());
				imageIds.add(field.getImage().getId().getValue());
			}
		}
        //Now delete the plate
      //Move the plate.
	doChange(new Chgrp(DeleteServiceTest.REF_PLATE,
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
    }

    /**
     * Tests to move a screen containing 2 plates, one w/o plate acquisition
     * and one with plate acquisition.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveScreen() throws Exception
    {
	String perms = "rw----";
	EventContext ctx = newUserAndGroup(perms);
	ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);
	iAdmin.getEventContext(); // Refresh

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


	doChange(new Chgrp(DeleteServiceTest.REF_SCREEN,
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
    public void testMoveScreenWithReagent() throws Exception
    {
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

	doChange(new Chgrp(DeleteServiceTest.REF_SCREEN,
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
     * Tests to move a plate with a reagent. The test now passes with or w/o
     * the FORCE option. Similar to delete
     * @throws Exception Thrown if an error occurred.
     */
    @Test(enabled = true)
    public void testMovePlateWithReagent() throws Exception
    {
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
	Map<String, String> options = new HashMap<String, String>();
	options.put("/Well/WellReagentLink", DeleteServiceTest.FORCE);
	doChange(new Chgrp(DeleteServiceTest.REF_PLATE,
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
     * Test to move a plate with ROI on images. The ROI will have
     * measurements.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testDeletePlateWithROIMeasurements() throws Exception
	{
	String perms = "rw----";
	EventContext ctx = newUserAndGroup(perms);
	ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);
	iAdmin.getEventContext(); // Refresh

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
            rect = new RectangleI();
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
		ROIAnnotationLink rl = new ROIAnnotationLinkI();
		rl.setChild(new FileAnnotationI(id, false));
		rl.setParent(new ROII(roi.getId().getValue(), false));
		links.add(rl);
		PlateAnnotationLink il = new PlateAnnotationLinkI();
		il.setChild(new FileAnnotationI(id, false));
		il.setParent(new PlateI(p.getId().getValue(), false));
		links.add(il);
		iUpdate.saveAndReturnArray(links);

		doChange(new Chgrp(DeleteServiceTest.REF_PLATE,
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

    /**
     * Tests to move a project containing a dataset with images.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveProject() throws Exception
    {
	String perms = "rw----";
	EventContext ctx = newUserAndGroup(perms);
	ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);
	iAdmin.getEventContext(); // Refresh

	Project p = (Project) iUpdate.saveAndReturnObject(
			mmFactory.simpleProjectData().asIObject());
	Dataset d = (Dataset) iUpdate.saveAndReturnObject(
			mmFactory.simpleDatasetData().asIObject());
	Image image1 = (Image) iUpdate.saveAndReturnObject(
			mmFactory.simpleImage(0));
	Image image2 = (Image) iUpdate.saveAndReturnObject(
			mmFactory.simpleImage(0));
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


        doChange(new Chgrp(DeleteServiceTest.REF_PROJECT,
			p.getId().getValue(), null, g.getId().getValue()));
	
		//Check if objects have been deleted
		ParametersI param = new ParametersI();
		param.addIds(ids);
		String sql = "select i from Image as i where i.id in (:ids)";
		List results = iQuery.findAllByQuery(sql, param);
		assertTrue(results.size() == 0);
	
		param = new ParametersI();
		param.addId(d.getId().getValue());
		sql = "select i from Dataset as i where i.id = :id";
		assertNull(iQuery.findByQuery(sql, param));
	
		param = new ParametersI();
		param.addId(p.getId().getValue());
		sql = "select i from Project as i where i.id = :id";
		assertNull(iQuery.findByQuery(sql, param));
	
		//Log in to other group
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
     * Tests to move a screen containing a plate also contained
     * in another screen. The screen should be moved but not the plate.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveScreenWithSharedPlate() throws Exception
    {
	String perms = "rw----";
	EventContext ctx = newUserAndGroup(perms);
	ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);
	iAdmin.getEventContext(); // Refresh

	Screen s1 = (Screen) iUpdate.saveAndReturnObject(
			mmFactory.simpleScreenData().asIObject());

	Screen s2 = (Screen) iUpdate.saveAndReturnObject(
			mmFactory.simpleScreenData().asIObject());

	//Plate w/o plate acquisition
	Plate p1 = (Plate) iUpdate.saveAndReturnObject(
			mmFactory.createPlate(1, 1, 1, 0, false));
	//Plate with plate acquisition
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


	doChange(new Chgrp(DeleteServiceTest.REF_SCREEN,
			s1.getId().getValue(), null, g.getId().getValue()));


	List<Long> ids = new ArrayList<Long>();
	ids.add(p1.getId().getValue());

	//Check if the plates exist.
	ParametersI param = new ParametersI();
	param.addIds(ids);
	String sql = "select i from Plate as i where i.id in (:ids)";
	List results = iQuery.findAllByQuery(sql, param);
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
	//plate should not have moved.
	sql = "select i from Plate as i where i.id in (:ids)";
	results = iQuery.findAllByQuery(sql, param);
	assertEquals(results.size(), 0);

	//screen should move
	param = new ParametersI();
	param.addId(s1.getId().getValue());
	sql = "select i from Screen as i where i.id = :id";
	assertNotNull(iQuery.findByQuery(sql, param));
    }

    /**
     * Tests to move a dataset containing an image also contained
     * in another dataset. The dataset should be moved but not the image.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetWithSharedImage() throws Exception
    {
	String perms = "rw----";
	EventContext ctx = newUserAndGroup(perms);
	ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);
	iAdmin.getEventContext(); // Refresh

	Dataset s1 = (Dataset) iUpdate.saveAndReturnObject(
			mmFactory.simpleDatasetData().asIObject());

	Dataset s2 = (Dataset) iUpdate.saveAndReturnObject(
			mmFactory.simpleDatasetData().asIObject());

	//Plate w/o plate acquisition
	Image i1 = (Image) iUpdate.saveAndReturnObject(
			mmFactory.simpleImage(10));
	//Plate with plate acquisition
	List<IObject> links = new ArrayList<IObject>();
	DatasetImageLink link = new DatasetImageLinkI();
	link.setChild((Image) i1.proxy());
	link.setParent(s1);
	links.add(link);
	link = new DatasetImageLinkI();
	link.setChild((Image) i1.proxy());
	link.setParent(s2);
	links.add(link);
	iUpdate.saveAndReturnArray(links);


	doChange(new Chgrp(DeleteServiceTest.REF_DATASET,
			s1.getId().getValue(), null, g.getId().getValue()));


	List<Long> ids = new ArrayList<Long>();
	ids.add(i1.getId().getValue());


	ParametersI param = new ParametersI();
	param.addIds(ids);
	String sql = "select i from Image as i where i.id in (:ids)";
	List results = iQuery.findAllByQuery(sql, param);
	assertEquals(results.size(), ids.size());

	//S1 should have moved
	param = new ParametersI();
	param.addId(s1.getId().getValue());
	sql = "select i from Dataset as i where i.id = :id";
	assertNull(iQuery.findByQuery(sql, param));

	param = new ParametersI();
	param.addId(s2.getId().getValue());
	assertNotNull(iQuery.findByQuery(sql, param));

	//Check that the data moved
	loginUser(g);
	param = new ParametersI();
	param.addIds(ids);
	sql = "select i from Image as i where i.id in (:ids)";
	results = iQuery.findAllByQuery(sql, param);
	assertEquals(results.size(), 0);

	param = new ParametersI();
	param.addId(s1.getId().getValue());
	sql = "select i from Dataset as i where i.id = :id";
	assertNotNull(iQuery.findByQuery(sql, param));
    }
    
    /**
     *  Tests to move a dataset to a project contained in the target group.
     * <code>RW----</code> to <code>RW----</code>. The move is done by the
     * owner of the data.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToProjectRWtoRW()
	throws Exception
    {
    	moveDataDatasetToProject("rw----", "rw----", LINK_EXISTING, DATA_OWNER);
    }
    
    /**
     *  Tests to move a dataset to a project contained in the target group.
     * <code>RWR---</code> to <code>RWR---</code>. The move is done by the
     * owner of the data.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToProjectRWRtoRWR()
	throws Exception
    {
    	moveDataDatasetToProject("rwr---", "rwr---", LINK_EXISTING, DATA_OWNER);
    }
    
    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWR---</code> to <code>RW----</code>. The move is done by the
     * owner of the data.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToProjectRWRtoRW()
	throws Exception
    {
    	moveDataDatasetToProject("rwr---", "rw----", LINK_EXISTING, DATA_OWNER);
    }
    
    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWRA--</code> to <code>RW----</code>. The move is done by the
     * owner of the data.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToProjectRWRAtoRW()
	throws Exception
    {
    	moveDataDatasetToProject("rwra--", "rw----", LINK_EXISTING, DATA_OWNER);
    }
    
    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWRA--</code> to <code>RWRA--</code>. The move is done by the
     * owner of the data.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToProjectRWRAtoRWRA()
	throws Exception
    {
    	moveDataDatasetToProject("rwra--", "rwra--", LINK_EXISTING, DATA_OWNER);
    }
    
    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RW----</code> to <code>RWR---</code>. The move is done by the
     * owner of the data.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToProjectRWtoRWR()
	throws Exception
    {
    	moveDataDatasetToProject("rw----", "rwr---", LINK_EXISTING, DATA_OWNER);
    }
    
    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWR---</code> to <code>RWRA--</code>. The move is done by the
     * owner of the data.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToProjectRWtoRWRA()
	throws Exception
    {
    	moveDataDatasetToProject("rw----", "rwra--", LINK_EXISTING, DATA_OWNER);
    }
    
    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWR---</code> to <code>RWRA--</code>. The move is done by the
     * owner of the data.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToProjectRWRtoRWRA()
	throws Exception
    {
    	moveDataDatasetToProject("rwr---", "rwra--", LINK_EXISTING, DATA_OWNER);
    }
    
    //Move by the owner but in that case a new project is created in the new
    //group
    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWR---</code> to <code>RW----</code>. The move is done by the
     * owner of the data. A new Project will be created.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToNewProjectRWtoRW()
	throws Exception
    {
    	moveDataDatasetToProject("rw----", "rw----", LINK_NEW, DATA_OWNER);
    }
    
    /**
     *  Tests to move a dataset to a project contained in the target group.
     * <code>RWR---</code> to <code>RWR---</code>. The move is done by the
     * owner of the data.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToNewProjectRWRtoRWR()
	throws Exception
    {
    	moveDataDatasetToProject("rwr---", "rwr---", LINK_NEW, DATA_OWNER);
    }
    
    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWR---</code> to <code>RW----</code>. The move is done by the
     * owner of the data.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToNewProjectRWRtoRW()
	throws Exception
    {
    	moveDataDatasetToProject("rwr---", "rw----", LINK_NEW, DATA_OWNER);
    }
    
    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWRA--</code> to <code>RW----</code>. The move is done by the
     * owner of the data.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToNewProjectRWRAtoRW()
	throws Exception
    {
    	moveDataDatasetToProject("rwra--", "rw----", LINK_NEW, DATA_OWNER);
    }
    
    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWRA--</code> to <code>RWRA--</code>. The move is done by the
     * owner of the data.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToNewProjectRWRAtoRWRA()
	throws Exception
    {
    	moveDataDatasetToProject("rwra--", "rwra--", LINK_NEW, DATA_OWNER);
    }
    
    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RW----</code> to <code>RWR---</code>. The move is done by the
     * owner of the data.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToNewProjectRWtoRWR()
	throws Exception
    {
    	moveDataDatasetToProject("rw----", "rwr---", LINK_NEW, DATA_OWNER);
    }
    
    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWR---</code> to <code>RWRA--</code>. The move is done by the
     * owner of the data.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToNewProjectRWtoRWRA()
	throws Exception
    {
    	moveDataDatasetToProject("rw----", "rwra--", LINK_NEW, DATA_OWNER);
    }
    
    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWR---</code> to <code>RWRA--</code>. The move is done by the
     * owner of the data.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToNewProjectRWRtoRWRA()
	throws Exception
    {
    	moveDataDatasetToProject("rwr---", "rwra--", LINK_NEW, DATA_OWNER);
    }
    
	//Test the data move by an admin
    
    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RW----</code> to <code>RW----</code>. The move is done by the
     * administrator.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToProjectRWtoRWByAdmin()
	throws Exception
    {
    	try {
    		moveDataDatasetToProject("rw----", "rw----", LINK_EXISTING, ADMIN);
    		fail("A security Violation should have been thrown." +
    			"Admin not allowed to create a link in private group.");
		} catch (Exception e) {
			
		}
    }
    
    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RW----</code> to <code>RW----</code>. The move is done by the
     * administrator.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToNewProjectRWtoRWByAdmin()
	throws Exception
    {
    	try {
    		moveDataDatasetToProject("rw----", "rw----", LINK_NEW, ADMIN);
    		fail("A security Violation should have been thrown." +
    			"Admin not allowed to create a link in private group.");
		} catch (Exception e) {
			
		}
    }
    
    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RW----</code> to <code>RW----</code>. The move is done by the
     * administrator.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetRWtoRWByAdmin()
	throws Exception
    {
    	moveDataDatasetToProject("rw----", "rw----", LINK_NONE, ADMIN);
    }
    
    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RW----</code> to <code>RW----</code>. The move is done by the
     * administrator.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToProjectRWRtoRWByAdmin()
	throws Exception
    {
    	try {
    		moveDataDatasetToProject("rwr---", "rw----", LINK_EXISTING, ADMIN);
    		fail("A security Violation should have been thrown." +
    			"Admin not allowed to create a link in private group.");
		} catch (Exception e) {
			
		}
    }
    
    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RW----</code> to <code>RW----</code>. The move is done by the
     * administrator.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToNewProjectRWRtoRWByAdmin()
	throws Exception
    {
    	try {
    		moveDataDatasetToProject("rwr---", "rw----", LINK_NEW, ADMIN);
    		fail("A security Violation should have been thrown." +
    			"Admin not allowed to create a link in private group.");
		} catch (Exception e) {
			
		}
    }
    
    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RW----</code> to <code>RW----</code>. The move is done by the
     * administrator.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetRWRtoRWByAdmin()
	throws Exception
    {
    	moveDataDatasetToProject("rwr---", "rw----", LINK_NONE, ADMIN);
    }

    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWRA--</code> to <code>RW----</code>. The move is done by the
     * administrator.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToProjectRWRAtoRWByAdmin()
	throws Exception
    {
    	try {
    		moveDataDatasetToProject("rwra--", "rw----", LINK_EXISTING, ADMIN);
    		fail("A security Violation should have been thrown." +
    			"Admin not allowed to create a link in private group.");
		} catch (Exception e) {
			
		}
    }
    
    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWRA--</code> to <code>RW----</code>. The move is done by the
     * administrator.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToNewProjectRWRAtoRWByAdmin()
	throws Exception
    {
    	try {
    		moveDataDatasetToProject("rwr---", "rw----", LINK_NEW, ADMIN);
    		fail("A security Violation should have been thrown." +
    			"Admin not allowed to create a link in private group.");
		} catch (Exception e) {
			
		}
    }
    
    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWRA--</code> to <code>RW----</code>. The move is done by the
     * administrator.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetRWRAtoRWByAdmin()
	throws Exception
    {
    	moveDataDatasetToProject("rwra--", "rwr---", LINK_NONE, ADMIN);
    }
    
    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RW----</code> to <code>RWR---</code>. The move is done by the
     * administrator.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetRWtoRWRByAdmin()
	throws Exception
    {
    	moveDataDatasetToProject("rwra--", "rwr---", LINK_NONE, ADMIN);
    }
    
    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RW----</code> to <code>RWRA---</code>. The move is done by the
     * administrator.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetRWtoRWRAByAdmin()
	throws Exception
    {
    	moveDataDatasetToProject("rw----", "rwra--", LINK_NONE, ADMIN);
    }
    
    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWR---</code> to <code>RWRA--</code>. The move is done by the
     * administrator.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetRWRtoRWRAByAdmin()
	throws Exception
    {
    	moveDataDatasetToProject("rwr---", "rwra--", LINK_NONE, ADMIN);
    }
    
    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWRA--</code> to <code>RWR---</code>. The move is done by the
     * administrator.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetRWRAtoRWRByAdmin()
	throws Exception
    {
    	moveDataDatasetToProject("rwra--", "rwr---", LINK_NONE, ADMIN);
    }

}
