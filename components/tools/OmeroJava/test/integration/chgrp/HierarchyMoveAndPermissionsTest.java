/*
 * $Id$
 *
 * Copyright 2006-2011 University of Dundee. All rights reserved.
 * Use is subject to license terms supplied in LICENSE.txt
 */
package integration.chgrp;

import omero.cmd.Chgrp;
import omero.model.ExperimenterGroup;
import omero.model.Image;
import omero.sys.EventContext;
import omero.sys.ParametersI;

import org.testng.annotations.Test;

import integration.AbstractTest;
import integration.DeleteServiceTest;


/**
 * Tests that a group owners (source and destination) can move data between
 * groups.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class HierarchyMoveAndPermissionsTest
	extends AbstractTest
{

    /**
     * Test to move an image w/o pixels between 2 <code>RW----</code> groups.
     * The image is moved by the owner of the group who is not the owner of the
     * image.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveBasicImageRW()
	throws Exception
    {
	String perms = "rw----";
	//group and group owner.
	EventContext ctx = newUserAndGroup(perms, true);
	EventContext dataOwner = newUserInGroup();
	Image img = (Image) iUpdate.saveAndReturnObject(
			mmFactory.createImage());
	long id = img.getId().getValue();
	disconnect();
	ctx = init(ctx);

	//Create a new group and make owner of first group an owner.
	ExperimenterGroup g = newGroupAddUser(perms, ctx.userId, true);


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
	assertNotNull(iQuery.findByQuery(sb.toString(), param));
    }

    /**
     * Test to move an image w/o pixels between <code>RWR---</code> groups.
     * The image is moved by the owner of the group who is not the owner of the
     * image.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveBasicImageRWR()
	throws Exception
    {
	String perms = "rwr---";
	//group and group owner.
	EventContext ctx = newUserAndGroup(perms, true);
	EventContext dataOwner = newUserInGroup();
	Image img = (Image) iUpdate.saveAndReturnObject(
			mmFactory.createImage());
	long id = img.getId().getValue();
	disconnect();
	ctx = init(ctx);

	//Create a new group and make owner of first group an owner.
	ExperimenterGroup g = newGroupAddUser(perms, ctx.userId, true);


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
	assertNotNull(iQuery.findByQuery(sb.toString(), param));
    }

    /**
     * Test to move an image w/o pixels between <code>RWRW--</code> groups.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveBasicImageRWRW()
	throws Exception
    {
	String perms = "rwrw--";
	//group and group owner.
	EventContext ctx = newUserAndGroup(perms, true);
	EventContext dataOwner = newUserInGroup();
	Image img = (Image) iUpdate.saveAndReturnObject(
			mmFactory.createImage());
	long id = img.getId().getValue();
	disconnect();
	ctx = init(ctx);

	//Create a new group and make owner of first group an owner.
	ExperimenterGroup g = newGroupAddUser(perms, ctx.userId, true);


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
	assertNotNull(iQuery.findByQuery(sb.toString(), param));
    }

    /**
     * Test to move an image w/o pixels between 2 groups. The owner of the
     * source group is NOT an owner of the destination group.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveBasicImageNotOwnerDestination()
	throws Exception
    {
	String perms = "rw----";
	//group and group owner.
	EventContext ctx = newUserAndGroup(perms, true);
	EventContext dataOwner = newUserInGroup();
	Image img = (Image) iUpdate.saveAndReturnObject(
			mmFactory.createImage());
	long id = img.getId().getValue();
	disconnect();
	ctx = init(ctx);

	//Create a new group and make owner of first group an owner.
	ExperimenterGroup g = newGroupAddUser(perms, ctx.userId, false);

	doChange(new Chgrp(ctx.sessionUuid, DeleteServiceTest.REF_IMAGE, id,
			null, g.getId().getValue()));
		//Now check that the image is no longer in group
	ParametersI param = new ParametersI();
	param.addId(id);

	assertTrue(g.getId().getValue() != ctx.groupId);
	StringBuilder sb = new StringBuilder();
	sb.append("select i from Image i ");
	sb.append("where i.id = :id");
	//image should not have been moved.
	assertNotNull(iQuery.findByQuery(sb.toString(), param));

	EventContext ec = loginUser(g);
	assertNull(iQuery.findByQuery(sb.toString(), param));
    }

    /**
     * Test to move an image w/o pixels between 2 groups:
     * source <code>RW----</code>, destination <code>RWR---</code>
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveBasicImageRWToRWR()
	throws Exception
    {
	String perms = "rw----";
	//group and group owner.
	EventContext ctx = newUserAndGroup(perms, true);
	EventContext dataOwner = newUserInGroup();
	Image img = (Image) iUpdate.saveAndReturnObject(
			mmFactory.createImage());
	long id = img.getId().getValue();
	disconnect();
	ctx = init(ctx);

	//Create a new group and make owner of first group an owner.
	ExperimenterGroup g = newGroupAddUser("rwr---", ctx.userId, true);

	doChange(new Chgrp(ctx.sessionUuid, DeleteServiceTest.REF_IMAGE, id,
			null, g.getId().getValue()));
		//Now check that the image is no longer in group
	ParametersI param = new ParametersI();
	param.addId(id);

	assertTrue(g.getId().getValue() != ctx.groupId);
	StringBuilder sb = new StringBuilder();
	sb.append("select i from Image i ");
	sb.append("where i.id = :id");
	//image should not have been moved.
	assertNull(iQuery.findByQuery(sb.toString(), param));

	EventContext ec = loginUser(g);
	assertNotNull(iQuery.findByQuery(sb.toString(), param));
    }

    /**
     * Test to move an image w/o pixels between 2 groups:
     * source <code>RWR---</code>, destination <code>RW----</code>
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveBasicImageRWRToRW()
	throws Exception
    {
	String perms = "rwr---";
	//group and group owner.
	EventContext ctx = newUserAndGroup(perms, true);
	EventContext dataOwner = newUserInGroup();
	Image img = (Image) iUpdate.saveAndReturnObject(
			mmFactory.createImage());
	long id = img.getId().getValue();
	disconnect();
	ctx = init(ctx);

	//Create a new group and make owner of first group an owner.
	ExperimenterGroup g = newGroupAddUser("rw----", ctx.userId, true);

	doChange(new Chgrp(ctx.sessionUuid, DeleteServiceTest.REF_IMAGE, id,
			null, g.getId().getValue()));
		//Now check that the image is no longer in group
	ParametersI param = new ParametersI();
	param.addId(id);

	assertTrue(g.getId().getValue() != ctx.groupId);
	StringBuilder sb = new StringBuilder();
	sb.append("select i from Image i ");
	sb.append("where i.id = :id");
	//image should not have been moved.
	assertNull(iQuery.findByQuery(sb.toString(), param));

	EventContext ec = loginUser(g);
	assertNotNull(iQuery.findByQuery(sb.toString(), param));
    }

    /**
     * Test to move an image w/o pixels between 2 <code>RWRW--</code>groups but
     * not owner of the groups.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveBasicImageNotOwnerOfGroupsRWRW()
	throws Exception
    {
	String perms = "rwrw--";
	//group and group owner.
	EventContext ctx = newUserAndGroup(perms, false);
	EventContext dataOwner = newUserInGroup();
	Image img = (Image) iUpdate.saveAndReturnObject(
			mmFactory.createImage());
	long id = img.getId().getValue();
	disconnect();
	ctx = init(ctx);

	//Create a new group and make owner of first group an owner.
	ExperimenterGroup g = newGroupAddUser(perms, ctx.userId, false);

	doChange(new Chgrp(ctx.sessionUuid, DeleteServiceTest.REF_IMAGE, id,
			null, g.getId().getValue()));
		//Now check that the image is no longer in group
	ParametersI param = new ParametersI();
	param.addId(id);

	assertTrue(g.getId().getValue() != ctx.groupId);
	StringBuilder sb = new StringBuilder();
	sb.append("select i from Image i ");
	sb.append("where i.id = :id");
	//image should not have been moved.
	assertNotNull(iQuery.findByQuery(sb.toString(), param));

	EventContext ec = loginUser(g);
	assertNull(iQuery.findByQuery(sb.toString(), param));
    }

    /**
     * Test to move an image w/o pixels between 2 groups.
     * Only owner of the destination group
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testMoveBasicImageOwnerOfDestinationOnlyRWRW()
	throws Exception
    {
	String perms = "rwrw--";
	//group and group owner.
	EventContext ctx = newUserAndGroup(perms, false);
	EventContext dataOwner = newUserInGroup();
	Image img = (Image) iUpdate.saveAndReturnObject(
			mmFactory.createImage());
	long id = img.getId().getValue();
	disconnect();
	ctx = init(ctx);

	//Create a new group and make owner of first group an owner.
	ExperimenterGroup g = newGroupAddUser(perms, ctx.userId, true);

	doChange(new Chgrp(ctx.sessionUuid, DeleteServiceTest.REF_IMAGE, id,
			null, g.getId().getValue()));
		//Now check that the image is no longer in group
	ParametersI param = new ParametersI();
	param.addId(id);

	assertTrue(g.getId().getValue() != ctx.groupId);
	StringBuilder sb = new StringBuilder();
	sb.append("select i from Image i ");
	sb.append("where i.id = :id");
	//image should not have been moved.
	assertNotNull(iQuery.findByQuery(sb.toString(), param));

	EventContext ec = loginUser(g);
	assertNull(iQuery.findByQuery(sb.toString(), param));
    }

}
