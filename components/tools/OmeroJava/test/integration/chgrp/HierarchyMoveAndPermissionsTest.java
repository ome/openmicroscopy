/*
 * $Id$
 *
 * Copyright 2006-2011 University of Dundee. All rights reserved.
 * Use is subject to license terms supplied in LICENSE.txt
 */
package integration.chgrp;

import integration.AbstractServerTest;
import integration.DeleteServiceTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import omero.cmd.Chgrp2;
import omero.model.Dataset;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.ExperimenterGroup;
import omero.model.Image;
import omero.sys.EventContext;
import omero.sys.ParametersI;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

import static org.testng.AssertJUnit.*;

/**
 * Tests that a group owners (source and destination) can move data between
 * groups.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since 3.0-Beta4
 */
public class HierarchyMoveAndPermissionsTest extends AbstractServerTest {

    /**
     * Test to move an image w/o pixels between 2 <code>RW----</code> groups.
     * The image is moved by the owner of the group who is not the owner of the
     * image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "broken")
    // Owners can no longer chgrp
    public void testMoveBasicImageRW() throws Exception {
        String perms = "rw----";
        // group and group owner.
        EventContext ctx = newUserAndGroup(perms, true);
        EventContext dataOwner = newUserInGroup();
        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        long id = img.getId().getValue();
        disconnect();
        ctx = init(ctx);

        // Create a new group and make owner of first group an owner.
        ExperimenterGroup g = newGroupAddUser(perms, ctx.userId, true);
        iAdmin.getEventContext(); // Refresh
        final Chgrp2 dc = new Chgrp2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(id));
        dc.groupId = g.getId().getValue();
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
        assertNotNull(iQuery.findByQuery(sb.toString(), param));
    }

    /**
     * Test to move an image w/o pixels between <code>RWR---</code> groups. The
     * image is moved by the owner of the group who is not the owner of the
     * image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "broken")
    // Owners can no longer chgrp
    public void testMoveBasicImageRWR() throws Exception {
        String perms = "rwr---";
        // group and group owner.
        EventContext ctx = newUserAndGroup(perms, true);
        EventContext dataOwner = newUserInGroup();
        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        long id = img.getId().getValue();
        disconnect();
        ctx = init(ctx);

        // Create a new group and make owner of first group an owner.
        ExperimenterGroup g = newGroupAddUser(perms, ctx.userId, true);
        iAdmin.getEventContext(); // Refresh

        final Chgrp2 dc = new Chgrp2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(id));
        dc.groupId = g.getId().getValue();
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
        assertNotNull(iQuery.findByQuery(sb.toString(), param));
    }

    /**
     * Test to move an image w/o pixels between <code>RWRW--</code> groups.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "broken")
    // Owners can no longer chgrp
    public void testMoveBasicImageRWRW() throws Exception {
        String perms = "rwrw--";
        // group and group owner.
        EventContext ctx = newUserAndGroup(perms, true);
        EventContext dataOwner = newUserInGroup();
        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        long id = img.getId().getValue();
        disconnect();
        ctx = init(ctx);

        // Create a new group and make owner of first group an owner.
        ExperimenterGroup g = newGroupAddUser(perms, ctx.userId, true);
        iAdmin.getEventContext(); // Refresh

        final Chgrp2 dc = new Chgrp2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(id));
        dc.groupId = g.getId().getValue();
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
        assertNotNull(iQuery.findByQuery(sb.toString(), param));
    }

    /**
     * Test to move an image w/o pixels between 2 groups. The owner of the
     * source group is NOT an owner or member of the destination group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "broken")
    // Owners can no longer chgrp
    public void testMoveBasicImageNotOwnerDestination() throws Exception {
        String perms = "rw----";
        // group and group owner.
        EventContext ctx = newUserAndGroup(perms, true);
        EventContext dataOwner = newUserInGroup();
        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        long id = img.getId().getValue();
        disconnect();
        ctx = init(ctx);

        // Create a new group and make owner of first group an owner.
        ExperimenterGroup g = newGroupAddUser(perms, ctx.userId, false);
        iAdmin.getEventContext(); // Refresh

        final Chgrp2 dc = new Chgrp2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(id));
        dc.groupId = g.getId().getValue();
        callback(true, client, dc);

        // Now check that the image is no longer in group
        ParametersI param = new ParametersI();
        param.addId(id);

        assertTrue(g.getId().getValue() != ctx.groupId);
        StringBuilder sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id = :id");
        // image should not have been moved.
        assertNotNull(iQuery.findByQuery(sb.toString(), param));

        EventContext ec = loginUser(g);
        assertNull(iQuery.findByQuery(sb.toString(), param));
    }

    /**
     * Test to move an image w/o pixels between 2 groups. The owner of the
     * source group is NOT an owner but IS a member of the destination group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "broken")
    // Owners can no longer chgrp
    public void testMoveBasicImageNotOwnerButMemberDestination()
            throws Exception {
        String perms = "rw----";
        // group and group owner.
        EventContext oldGroupOwner = newUserAndGroup(perms, true);
        EventContext dataOwner = newUserInGroup();
        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        long id = img.getId().getValue();
        disconnect();
        oldGroupOwner = init(oldGroupOwner);

        // Create a new group and make owner of first group an owner.
        ExperimenterGroup g = newGroupAddUser(perms, oldGroupOwner.userId,
                false);
        iAdmin.getEventContext(); // Refresh

        final Chgrp2 dc = new Chgrp2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(id));
        dc.groupId = g.getId().getValue();
        callback(true, client, dc);
        // Now check that the image is no longer in group
        ParametersI param = new ParametersI();
        param.addId(id);

        assertTrue(g.getId().getValue() != oldGroupOwner.groupId);
        StringBuilder sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id = :id");
        // image should not have been moved.
        assertNotNull(iQuery.findByQuery(sb.toString(), param));

        EventContext ec = loginUser(g);
        assertNull(iQuery.findByQuery(sb.toString(), param));
    }

    /**
     * Test to move an image w/o pixels between 2 groups: source
     * <code>RW----</code>, destination <code>RWR---</code>
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "broken")
    // Owners can no longer chgrp
    public void testMoveBasicImageRWToRWR() throws Exception {
        String perms = "rw----";
        // group and group owner.
        EventContext ctx = newUserAndGroup(perms, true);
        EventContext dataOwner = newUserInGroup();
        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        long id = img.getId().getValue();
        disconnect();
        ctx = init(ctx);

        // Create a new group and make owner of first group an owner.
        ExperimenterGroup g = newGroupAddUser("rwr---", ctx.userId, true);
        iAdmin.getEventContext(); // Refresh

        final Chgrp2 dc = new Chgrp2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(id));
        dc.groupId = g.getId().getValue();
        callback(true, client, dc);
        // Now check that the image is no longer in group
        ParametersI param = new ParametersI();
        param.addId(id);

        assertTrue(g.getId().getValue() != ctx.groupId);
        StringBuilder sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id = :id");
        // image should not have been moved.
        assertNull(iQuery.findByQuery(sb.toString(), param));

        EventContext ec = loginUser(g);
        assertNotNull(iQuery.findByQuery(sb.toString(), param));
    }

    /**
     * Test to move an image w/o pixels between 2 groups: source
     * <code>RWR---</code>, destination <code>RW----</code>
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "broken")
    // Owners can no longer chgrp
    public void testMoveBasicImageRWRToRW() throws Exception {
        String perms = "rwr---";
        // group and group owner.
        EventContext ctx = newUserAndGroup(perms, true);
        EventContext dataOwner = newUserInGroup();
        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        long id = img.getId().getValue();
        disconnect();
        ctx = init(ctx);

        // Create a new group and make owner of first group an owner.
        ExperimenterGroup g = newGroupAddUser("rw----", ctx.userId, true);
        iAdmin.getEventContext(); // Refresh

        final Chgrp2 dc = new Chgrp2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(id));
        dc.groupId = g.getId().getValue();
        callback(true, client, dc);
        // Now check that the image is no longer in group
        ParametersI param = new ParametersI();
        param.addId(id);

        assertTrue(g.getId().getValue() != ctx.groupId);
        StringBuilder sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id = :id");
        // image should not have been moved.
        assertNull(iQuery.findByQuery(sb.toString(), param));

        EventContext ec = loginUser(g);
        assertNotNull(iQuery.findByQuery(sb.toString(), param));
    }

    /**
     * Test to move an image w/o pixels between 2 <code>RWRW--</code>groups but
     * not owner of the groups.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "broken")
    // Owners can no longer chgrp
    public void testMoveBasicImageNotOwnerOfGroupsRWRW() throws Exception {
        String perms = "rwrw--";
        // group and group owner.
        EventContext ctx = newUserAndGroup(perms, false);
        EventContext dataOwner = newUserInGroup();
        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        long id = img.getId().getValue();
        disconnect();
        ctx = init(ctx);

        // Create a new group and make owner of first group an owner.
        ExperimenterGroup g = newGroupAddUser(perms, ctx.userId, false);
        iAdmin.getEventContext(); // Refresh

        final Chgrp2 dc = new Chgrp2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(id));
        dc.groupId = g.getId().getValue();
        callback(true, client, dc);
        // Now check that the image is no longer in group
        ParametersI param = new ParametersI();
        param.addId(id);

        assertTrue(g.getId().getValue() != ctx.groupId);
        StringBuilder sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id = :id");
        // image should not have been moved.
        assertNotNull(iQuery.findByQuery(sb.toString(), param));

        EventContext ec = loginUser(g);
        assertNull(iQuery.findByQuery(sb.toString(), param));
    }

    /**
     * Test to move an image w/o pixels between 2 groups. Only owner of the
     * destination group
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "broken")
    // Owners can no longer chgrp
    public void testMoveBasicImageOwnerOfDestinationOnlyRWRW() throws Exception {
        String perms = "rwrw--";
        // group and group owner.
        EventContext ctx = newUserAndGroup(perms, false);
        EventContext dataOwner = newUserInGroup();
        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        long id = img.getId().getValue();
        disconnect();
        ctx = init(ctx);

        // Create a new group and make owner of first group an owner.
        ExperimenterGroup g = newGroupAddUser(perms, ctx.userId, true);
        iAdmin.getEventContext(); // Refresh

        final Chgrp2 dc = new Chgrp2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(id));
        dc.groupId = g.getId().getValue();
        callback(true, client, dc);
        // Now check that the image is no longer in group
        ParametersI param = new ParametersI();
        param.addId(id);

        assertTrue(g.getId().getValue() != ctx.groupId);
        StringBuilder sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id = :id");
        // image should not have been moved.
        assertNotNull(iQuery.findByQuery(sb.toString(), param));

        EventContext ec = loginUser(g);
        assertNull(iQuery.findByQuery(sb.toString(), param));
    }

    /**
     * Test to move an image w/o pixels between 2 <code>RW----</code> groups.
     * The image is moved by the administrator who is not member of the
     * source/destination group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveBasicImageByAdmin() throws Exception {
        String perms = "rw----";
        // new user
        EventContext ctx = newUserAndGroup(perms, false);
        EventContext dataOwner = newUserInGroup();
        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        long id = img.getId().getValue();

        // Create a new group and make owner of first group an owner.
        ExperimenterGroup g = newGroupAddUser(perms, ctx.userId, false);
        // admin logs into first group
        disconnect();
        logRootIntoGroup(ctx);
        final Chgrp2 dc = new Chgrp2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(id));
        dc.groupId = g.getId().getValue();
        callback(true, client, dc);

        ParametersI param = new ParametersI();
        param.addId(id);
        StringBuilder sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id = :id");
        assertNull(iQuery.findByQuery(sb.toString(), param));
        disconnect();
        logRootIntoGroup(g.getId().getValue());
        assertNotNull(iQuery.findByQuery(sb.toString(), param));
    }

    /**
     * Test to move a graph D/I from <code>RWRW--</code> group to a
     * <code>RW----</code> group. The owner of the image creates the link with
     * another user's dataset. Attempt to move the dataset. None of the users
     * are owner of the groups. Only the first user is a member of the
     * destination group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    public void testMoveDatasetImageGraphLinkDoneByImageOwnerRWRWtoRW()
            throws Exception {
        String perms = "rw----"; // destination
        EventContext ctx = newUserAndGroup("rwrw--");
        Dataset dataset = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        long id = dataset.getId().getValue();
        omero.client user1 = disconnect();

        // new user
        EventContext user2 = newUserInGroup(ctx);
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        long imageId = image.getId().getValue();
        // now link the image and dataset.
        DatasetImageLink link = new DatasetImageLinkI();
        link.setChild((Image) image.proxy());
        link.setParent((Dataset) dataset.proxy());
        iUpdate.saveAndReturnObject(link);
        disconnect();
        ctx = init(ctx);

        // Create a new group and user1 to that group.
        ExperimenterGroup g = newGroupAddUser(perms, ctx.userId, false);
        iAdmin.getEventContext(); // Refresh

        // loginUser(ctx);
        // Now try to move the dataset.
        final Chgrp2 dc = new Chgrp2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Dataset.class.getSimpleName(),
                Collections.singletonList(id));
        dc.groupId = g.getId().getValue();
        callback(true, client, dc);
        ParametersI param = new ParametersI();
        param.addId(id);
        StringBuilder sb = new StringBuilder();
        sb.append("select i from Dataset i ");
        sb.append("where i.id = :id");
        // dataset should have been moved.
        assertNull(iQuery.findByQuery(sb.toString(), param));
        sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id = :id");
        param = new ParametersI();
        param.addId(imageId);
        // image should not have been moved.
        assertNotNull("#9496? anns", iQuery.findByQuery(sb.toString(), param));

        // destination group
        EventContext ec = loginUser(g); // log into second group
        sb = new StringBuilder();
        sb.append("select i from Dataset i ");
        sb.append("where i.id = :id");
        param = new ParametersI();
        param.addId(id);
        assertNotNull(iQuery.findByQuery(sb.toString(), param));
    }

    /**
     * Test to move a graph D/I from <code>RWRW--</code> group to a
     * <code>RWRW--</code> group. The owner of the image creates the link with
     * another user's dataset. Attempt to move the dataset. None of the users
     * are owner of the groups.Only the first user is a member of the
     * destination group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "broken")
    // Owners can no longer chgrp
    public void testMoveDatasetImageGraphLinkDoneByImageOwnerRWRWtoRWRW()
            throws Exception {
        String perms = "rwrw--"; // destination
        EventContext ctx = newUserAndGroup(perms);
        Dataset dataset = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        long id = dataset.getId().getValue();
        omero.client user1 = disconnect();

        // new user
        EventContext user2 = newUserInGroup(ctx);
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        long imageId = image.getId().getValue();
        // now link the image and dataset.
        DatasetImageLink link = new DatasetImageLinkI();
        link.setChild((Image) image.proxy());
        link.setParent((Dataset) dataset.proxy());
        iUpdate.saveAndReturnObject(link);
        disconnect();
        ctx = init(ctx);

        // Create a new group and user1 to that group.
        ExperimenterGroup g = newGroupAddUser(perms, ctx.userId, false);
        iAdmin.getEventContext(); // Refresh

        // loginUser(ctx);
        // Now try to move the dataset.
        final Chgrp2 dc = new Chgrp2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Dataset.class.getSimpleName(),
                Collections.singletonList(id));
        dc.groupId = g.getId().getValue();
        callback(true, client, dc);
        ParametersI param = new ParametersI();
        param.addId(id);
        StringBuilder sb = new StringBuilder();
        sb.append("select i from Dataset i ");
        sb.append("where i.id = :id");
        // dataset should have been moved.
        assertNull(iQuery.findByQuery(sb.toString(), param));
        sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id = :id");
        param = new ParametersI();
        param.addId(imageId);
        // image should not have been moved.
        assertNotNull(iQuery.findByQuery(sb.toString(), param));

        // destination group
        EventContext ec = loginUser(g); // log into second group
        sb = new StringBuilder();
        sb.append("select i from Dataset i ");
        sb.append("where i.id = :id");
        param = new ParametersI();
        param.addId(id);
        assertNotNull(iQuery.findByQuery(sb.toString(), param));
    }

    /**
     * Test to move a graph D/I from <code>RWRW--</code> group to a
     * <code>RWRW--</code> group. The owner of the image creates the link with
     * another user's dataset. Attempt to move the dataset. None of the users
     * are owner of the groups. Both users are members of destination group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "broken")
    public void testMoveDatasetImageGraphLinkDoneByImageOwnerRWRWtoRWRWBothMembers()
            throws Exception {
        String perms = "rwrw--"; // destination
        EventContext ctx = newUserAndGroup(perms);
        Dataset dataset = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        long id = dataset.getId().getValue();
        omero.client user1 = disconnect();

        // new user
        EventContext ctx2 = newUserInGroup(ctx);
        iAdmin.getEventContext(); // Refresh

        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        long imageId = image.getId().getValue();
        // now link the image and dataset.
        DatasetImageLink link = new DatasetImageLinkI();
        link.setChild((Image) image.proxy());
        link.setParent((Dataset) dataset.proxy());
        iUpdate.saveAndReturnObject(link);

        disconnect();
        ctx = init(ctx);
        List<Long> users = new ArrayList<Long>();
        users.add(ctx.userId);
        users.add(ctx2.userId);
        // Create a new group and user1 and user2 to that group.
        ExperimenterGroup g = newGroupAddUser(perms, users, false);
        iAdmin.getEventContext(); // Refresh

        // loginUser(ctx);
        // Now try to move the dataset.
        final Chgrp2 dc = new Chgrp2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Dataset.class.getSimpleName(),
                Collections.singletonList(id));
        dc.groupId = g.getId().getValue();
        callback(true, client, dc);
        ParametersI param = new ParametersI();
        param.addId(id);
        StringBuilder sb = new StringBuilder();
        sb.append("select i from Dataset i ");
        sb.append("where i.id = :id");
        // dataset should have been moved.
        assertNull(iQuery.findByQuery(sb.toString(), param));
        sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id = :id");
        param = new ParametersI();
        param.addId(imageId);
        // image should not have been moved.
        assertNotNull("#9496? anns", iQuery.findByQuery(sb.toString(), param));

        // destination group
        EventContext ec = loginUser(g); // log into second group
        sb = new StringBuilder();
        sb.append("select i from Dataset i ");
        sb.append("where i.id = :id");
        param = new ParametersI();
        param.addId(id);
        assertNotNull(iQuery.findByQuery(sb.toString(), param));
        sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id = :id");
        param = new ParametersI();
        param.addId(imageId);
        // image should not have been moved.
        assertNull(iQuery.findByQuery(sb.toString(), param));
    }

    /**
     * Test to move a graph D/I from <code>RWRW--</code> group to a
     * <code>RWRW--</code> group. The owner of the image creates the link with
     * another user's dataset. Attempt to move the dataset. None of the users
     * are owner of the groups. Both users are members of destination group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetImageGraphLinkDoneByImageOwnerRWRWtoRWRWBothMembersAndFirstUserOwner()
            throws Exception {
        String perms = "rwrw--"; // destination
        EventContext ctx = newUserAndGroup("rwrw--");
        Dataset dataset = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        long id = dataset.getId().getValue();
        omero.client user1 = disconnect();

        // new user
        EventContext ctx2 = newUserInGroup(ctx);

        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        long imageId = image.getId().getValue();
        // now link the image and dataset.
        DatasetImageLink link = new DatasetImageLinkI();
        link.setChild((Image) image.proxy());
        link.setParent((Dataset) dataset.proxy());
        iUpdate.saveAndReturnObject(link);

        disconnect();
        ctx = init(ctx);
        List<Long> users = new ArrayList<Long>();
        users.add(ctx.userId);
        users.add(ctx2.userId);
        // Create a new group and user1 and user2 to that group.
        ExperimenterGroup g = newGroupAddUser(perms, users, false);
        makeGroupOwner();

        // loginUser(ctx);
        // Now try to move the dataset.
        final Chgrp2 dc = new Chgrp2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Dataset.class.getSimpleName(),
                Collections.singletonList(id));
        dc.groupId = g.getId().getValue();
        callback(true, client, dc);
        ParametersI param = new ParametersI();
        param.addId(id);
        StringBuilder sb = new StringBuilder();
        sb.append("select i from Dataset i ");
        sb.append("where i.id = :id");
        // dataset should have been moved.
        assertNull(iQuery.findByQuery(sb.toString(), param));
        sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id = :id");
        param = new ParametersI();
        param.addId(imageId);
        // image should have been moved.
        assertNull(iQuery.findByQuery(sb.toString(), param));

        // destination group
        EventContext ec = loginUser(g); // log into second group
        sb = new StringBuilder();
        sb.append("select i from Dataset i ");
        sb.append("where i.id = :id");
        param = new ParametersI();
        param.addId(id);
        assertNotNull(iQuery.findByQuery(sb.toString(), param));
        sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id = :id");
        param = new ParametersI();
        param.addId(imageId);
        // image should be there
        assertNotNull(iQuery.findByQuery(sb.toString(), param));
    }

    /**
     * Test to move an image w/o pixels owned by another user between 2
     * <code>RWRW--</code>groups. 2 users, user 1 owner of an image. user 2 is
     * not. User 2 tries to move the image. Both user1 and user2 are members of
     * the target group but user2 not owner of the group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "broken")
    // Owners can no longer chgrp
    public void testMoveImageOwnedByOtherRWRWToRWRW() throws Exception {
        String perms = "rwrw--";
        // group and group owner.
        EventContext ctx1 = newUserAndGroup(perms, false);
        EventContext ctx2 = newUserInGroup();
        disconnect();
        loginUser(ctx1);
        // user 1 owner of the image.
        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        long id = img.getId().getValue();
        disconnect();

        List<Long> users = new ArrayList<Long>();
        users.add(ctx1.userId);
        users.add(ctx2.userId);
        ExperimenterGroup g = newGroupAddUser("rwrw--", users, false);

        // user2 tries to move it.
        ctx2 = init(ctx2);
        final Chgrp2 dc = new Chgrp2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(id));
        dc.groupId = g.getId().getValue();
        callback(true, client, dc);
        // Now check that the image is no longer in group
        ParametersI param = new ParametersI();
        param.addId(id);

        StringBuilder sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id = :id");
        // image should not have been moved.
        assertNotNull(iQuery.findByQuery(sb.toString(), param));

        loginUser(g);
        assertNull(iQuery.findByQuery(sb.toString(), param));
    }

}
