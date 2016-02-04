/*
 * Copyright 2006-2016 University of Dundee. All rights reserved.
 * Use is subject to license terms supplied in LICENSE.txt
 */

package integration.chgrp;

import integration.AbstractServerTest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import omero.RLong;
import omero.ServerError;
import omero.cmd.Chgrp2;
import omero.cmd.graphs.ChildOption;
import omero.gateway.util.Requests;
import omero.model.Dataset;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.Folder;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Roi;
import omero.model.RoiI;
import omero.sys.EventContext;
import omero.sys.ParametersI;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
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

        final Chgrp2 dc = Requests.chgrp("Image", id, g.getId().getValue());
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

        final Chgrp2 dc = Requests.chgrp("Image", id, g.getId().getValue());
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

        final Chgrp2 dc = Requests.chgrp("Image", id, g.getId().getValue());
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

        final Chgrp2 dc = Requests.chgrp("Image", id, g.getId().getValue());
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

        final Chgrp2 dc = Requests.chgrp("Image", id, g.getId().getValue());
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

        final Chgrp2 dc = Requests.chgrp("Image", id, g.getId().getValue());
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
        final Chgrp2 dc = Requests.chgrp("Image", id, g.getId().getValue());
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
        final Chgrp2 dc = Requests.chgrp("Dataset", id, g.getId().getValue());
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
        final Chgrp2 dc = Requests.chgrp("Dataset", id, g.getId().getValue());
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
        final Chgrp2 dc = Requests.chgrp("Dataset", id, g.getId().getValue());
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
        final Chgrp2 dc = Requests.chgrp("Dataset", id, g.getId().getValue());
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
        final Chgrp2 dc = Requests.chgrp("Image", id, g.getId().getValue());
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

    /**
     * Test that moving a child folder out of a folder hierarchy moves its own child but leaves its parent behind.
     * @throws Exception unexpected
     */
    @Test
    public void testMoveChildFolder() throws Exception {

        /* set up user and groups */

        final EventContext user = newUserAndGroup("rw----");
        final ExperimenterGroup fromGroup = new ExperimenterGroupI(user.groupId, false);
        final ExperimenterGroup toGroup = newGroupAddUser("rw----", user.userId);

        /* work in first group */

        loginUser(fromGroup);

        /* create three levels of folder */

        Folder topFolder = saveAndReturnFolder(mmFactory.simpleFolder());
        Folder middleFolder = saveAndReturnFolder(mmFactory.simpleFolder());
        Folder bottomFolder = saveAndReturnFolder(mmFactory.simpleFolder());

        topFolder.addChildFolders(middleFolder);
        topFolder = saveAndReturnFolder(topFolder);
        middleFolder = returnFolder(middleFolder);

        middleFolder.addChildFolders(bottomFolder);
        middleFolder = saveAndReturnFolder(middleFolder);
        bottomFolder = returnFolder(bottomFolder);

        /* check that the three levels are connected as expected */

        topFolder = returnFolder(topFolder);
        Assert.assertNull(topFolder.getParentFolder());
        Assert.assertEquals(topFolder.copyChildFolders().size(), 1);
        Assert.assertEquals(topFolder.copyChildFolders().get(0).getId().getValue(), middleFolder.getId().getValue());

        middleFolder = returnFolder(middleFolder);
        Assert.assertEquals(middleFolder.getParentFolder().getId().getValue(), topFolder.getId().getValue());
        Assert.assertEquals(middleFolder.copyChildFolders().size(), 1);
        Assert.assertEquals(middleFolder.copyChildFolders().get(0).getId().getValue(), bottomFolder.getId().getValue());

        bottomFolder = returnFolder(bottomFolder);
        Assert.assertEquals(bottomFolder.getParentFolder().getId().getValue(), middleFolder.getId().getValue());
        Assert.assertTrue(bottomFolder.copyChildFolders().isEmpty());

        /* move the middle folder from the first group to the second */

        doChange(Requests.chgrp("Folder", middleFolder.getId().getValue(), toGroup.getId().getValue()));

        /* check that only the top folder remains */

        topFolder = returnFolder(topFolder);
        Assert.assertNull(topFolder.getParentFolder());
        Assert.assertTrue(topFolder.copyChildFolders().isEmpty());

        Assert.assertNull(returnFolder(middleFolder));

        Assert.assertNull(returnFolder(bottomFolder));

        disconnect();

        /* work in second group */

        loginUser(toGroup);

        /* check that only the middle and bottom folders have moved */

        Assert.assertNull(returnFolder(topFolder));

        middleFolder = returnFolder(middleFolder);
        Assert.assertNull(middleFolder.getParentFolder());
        Assert.assertEquals(middleFolder.copyChildFolders().size(), 1);
        Assert.assertEquals(middleFolder.copyChildFolders().get(0).getId().getValue(), bottomFolder.getId().getValue());

        bottomFolder = returnFolder(bottomFolder);
        Assert.assertEquals(bottomFolder.getParentFolder().getId().getValue(), middleFolder.getId().getValue());
        Assert.assertTrue(bottomFolder.copyChildFolders().isEmpty());

        disconnect();
    }

    /**
     * Assert that the given object is in the given group.
     * @param object a model object
     * @param group an experimenter group
     * @throws Exception unexpected
     */
    private void assertObjectInGroup(IObject object, ExperimenterGroup group) throws Exception {
        if (iAdmin.getEventContext().groupId != group.getId().getValue()) {
            disconnect();
            loginUser(group);
        }
        Class <? extends IObject> objectClass = object.getClass();
        while (objectClass.getSuperclass() != IObject.class) {
            objectClass = objectClass.getSuperclass().asSubclass(IObject.class);
        }
        object = iQuery.get(objectClass.getSimpleName(), object.getId().getValue());
        Assert.assertEquals(object.getDetails().getGroup().getId().getValue(), group.getId().getValue());
    }

    /**
     * Test moving folder hierarchies.
     * @param folderOption if the child option should target folders
     * @param includeOrphans how to set child options
     * @param fromReadWrite if the target folder starts in a read-write group, otherwise read-annotate
     * @param toPrivate if the target folder moves to a private group, otherwise the move is between groups of the same kind
     * @param myChildFolder if the child folder should share ownership with the top-level folder owned by the mover
     * @param myRoi if the ROI in the child folder should share ownership with the top-level folder owned by the mover
     * @throws Exception unexpected
     */
    @Test(dataProvider = "hierarchical folder test cases")
    public void testMoveTopLevelFolder(boolean folderOption, Boolean includeOrphans, boolean fromReadWrite, boolean toPrivate,
            boolean myChildFolder, boolean myRoi)
            throws Exception {

        /* set up user and groups */

        final EventContext mover = newUserAndGroup(fromReadWrite ? "rwrw--" : "rwra--");
        final EventContext other = newUserAndGroup(toPrivate ? "rw----" : fromReadWrite ? "rwrw--" : "rwra--");
        final ExperimenterGroup fromGroup = new ExperimenterGroupI(mover.groupId, false);
        final ExperimenterGroup toGroup = new ExperimenterGroupI(other.groupId, false);
        addUsers(fromGroup, Collections.singletonList(other.userId), false);
        addUsers(toGroup, Collections.singletonList(mover.userId), false);

        /* set up the data */

        loginUser(mover, fromGroup);
        Folder parentFolder = (Folder) saveAndReturnFolder(mmFactory.simpleFolder()).proxy();

        if (!myChildFolder) {
            disconnect();
            loginUser(myChildFolder ? mover : other, fromGroup);
        }

        Folder childFolder = mmFactory.simpleFolder();
        childFolder.setParentFolder(parentFolder);
        childFolder = (Folder) saveAndReturnFolder(childFolder).proxy();

        if (myChildFolder != myRoi) {
            disconnect();
            loginUser(myRoi ? mover : other, fromGroup);
        }

        Roi roi = new RoiI();
        roi.linkFolder(childFolder);
        roi = (Roi) iUpdate.saveAndReturnObject(roi).proxy();

        /* check that the three objects exist */

        assertExists(parentFolder);
        assertExists(childFolder);
        assertExists(roi);

        /* determine expectations for move */

        boolean childFolderMoves = (myChildFolder || fromReadWrite && !toPrivate) &&
                !(folderOption && Boolean.FALSE.equals(includeOrphans));
        boolean roiMoves = (myChildFolder && myRoi || fromReadWrite && !toPrivate) &&
                !Boolean.FALSE.equals(includeOrphans);

        /* perform the specified move */

        if (!myRoi) {
            disconnect();
            loginUser(mover, fromGroup);
        }

        final ChildOption option;
        if (Boolean.TRUE.equals(includeOrphans)) {
            option = Requests.option(folderOption ? "Folder" : "Roi", null);
        } else if (Boolean.FALSE.equals(includeOrphans)) {
            option = Requests.option(null, folderOption ? "Folder" : "Roi");
        } else {
            option = null;
        }
        final Chgrp2 move = new Chgrp2();
        move.groupId = toGroup.getId().getValue();
        move.targetObjects = ImmutableMap.of("Folder", Collections.singletonList(parentFolder.getId().getValue()));
        if (option != null) {
            move.childOptions = Collections.singletonList(option);
        }
        doChange(move);

        /* check which objects are now in which groups */

        assertObjectInGroup(parentFolder, toGroup);
        assertObjectInGroup(childFolder, childFolderMoves ? toGroup : fromGroup);
        assertObjectInGroup(roi, roiMoves ? toGroup : fromGroup);
    }

    /**
     * @return a variety of test cases for moving folder hierarchies
     */
    @DataProvider(name = "hierarchical folder test cases")
    public Object[][] provideFolderMoveCases() {
        int index = 0;
        final int ORPHAN_FOLDER = index++;
        final int INCLUDE_ORPHANS = index++;
        final int FROM_READ_WRITE = index++;
        final int TO_PRIVATE = index++;
        final int MY_CHILD_FOLDER = index++;
        final int MY_ROI = index++;

        final Boolean[] booleanCases = new Boolean[]{false, true};
        final Boolean[] booleanCasesWithNull = new Boolean[]{null, false, true};

        final List<Object[]> testCases = new ArrayList<Object[]>();

        for (final Boolean folderOption : booleanCases) {
            for (final Boolean includeOption : booleanCasesWithNull) {
                for (final Boolean fromReadWrite : booleanCases) {
                    for (final Boolean toPrivate : booleanCases) {
                        for (final Boolean myChildFolder : booleanCases) {
                            for (final Boolean myRoi : booleanCases) {
                                if (folderOption == true && includeOption == null ||
                                        fromReadWrite == false && (myChildFolder == false || myRoi == false)) {
                                    continue;
                                }
                                final Object[] testCase = new Object[index];
                                testCase[ORPHAN_FOLDER] = folderOption;
                                testCase[INCLUDE_ORPHANS] = includeOption;
                                testCase[FROM_READ_WRITE] = fromReadWrite;
                                testCase[TO_PRIVATE] = toPrivate;
                                testCase[MY_CHILD_FOLDER] = myChildFolder;
                                testCase[MY_ROI] = myRoi;
                                // DEBUG: if (folderOption == true && Boolean.TRUE.equals(includeOption) &&
                                //       fromReadWrite == true && toPrivate == true && myChildFolder == false && myRoi == false)
                                testCases.add(testCase);
                            }
                        }
                    }
                }
            }
        }

        return testCases.toArray(new Object[testCases.size()][]);
    }


    /**
     * Count how many specific instances of a class remain.
     * @param type the type of the instances
     * @param ids the IDs of the instances of interest
     * @return how many of the instances remain
     * @throws ServerError unexpected
     */
    private long countInstances(Class<? extends IObject> type, Collection<Long> ids) throws ServerError {
        if (ids.isEmpty()) {
            return 0;
        }
        final String query = "SELECT COUNT(*) FROM " + type.getSimpleName() + " WHERE id IN (:ids)";
        final omero.sys.Parameters params = new ParametersI().addIds(ids);
        return ((RLong) iQuery.projection(query, params).get(0).get(0)).getValue();
    }

    /**
     * Test moving ROIs that are on images and in folders.
     * @param folderCount how many folders the ROI should be in
     * @param imageCount how many images the ROI should be on (0 or 1)
     * @param targetFolder if the move should target a folder
     * @param targetImage if the move should target an image
     * @param includeOrphans how to set child options for moving ROIs
     * @throws Exception unexpected
     */
    @Test(dataProvider = "contained ROI test cases")
    public void testMoveRois(int folderCount, int imageCount, boolean targetFolder, boolean targetImage, Boolean includeOrphans)
            throws Exception {

        /* set up user and groups */

        final EventContext user = newUserAndGroup("rw----");
        final ExperimenterGroup fromGroup = new ExperimenterGroupI(user.groupId, false);
        final ExperimenterGroup toGroup = newGroupAddUser("rw----", user.userId);

        /* work in first group */

        loginUser(fromGroup);

        /* set up the data and note the IDs */

        final Roi roi = new RoiI();
        final List<Long> roiIds = new ArrayList<Long>();
        final List<Long> folderIds = new ArrayList<Long>();
        final List<Long> imageIds = new ArrayList<Long>();

        for (int f = 0; f < folderCount; f++) {
            final Folder folder = (Folder) iUpdate.saveAndReturnObject(mmFactory.simpleFolder());
            folderIds.add(folder.getId().getValue());
            roi.linkFolder((Folder) folder.proxy());
        }

        for (int i = 0; i < imageCount; i++) {
            final Image image = (Image) iUpdate.saveAndReturnObject(mmFactory.simpleImage());
            imageIds.add(image.getId().getValue());
            roi.setImage((Image) image.proxy());
        }

        roiIds.add(iUpdate.saveAndReturnObject(roi).proxy().getId().getValue());

        /* cannot move an ROI without its image */

        final boolean isLegalMove = targetImage || imageCount == 0 || !Boolean.TRUE.equals(includeOrphans);

        /* check that the object counts are as expected */

        int expectedRemainingRoiCount = 1;
        int expectedRemainingFolderCount = folderCount;
        int expectedRemainingImageCount = imageCount;

        int expectedMovedRoiCount = 0;
        int expectedMovedFolderCount = 0;
        int expectedMovedImageCount = 0;

        Assert.assertEquals(countInstances(Roi.class, roiIds), expectedRemainingRoiCount);
        Assert.assertEquals(countInstances(Folder.class, folderIds), expectedRemainingFolderCount);
        Assert.assertEquals(countInstances(Image.class, imageIds), expectedRemainingImageCount);

        /* perform the specified move and update the expected object counts */

        final Chgrp2 move = new Chgrp2();
        move.groupId = toGroup.getId().getValue();
        move.targetObjects = new HashMap<String, List<Long>>();
        if (targetFolder) {
            move.targetObjects.put("Folder", Collections.singletonList(folderIds.get(0)));
            expectedRemainingFolderCount--;
            expectedMovedFolderCount++;
        }
        if (targetImage) {
            move.targetObjects.put("Image", Collections.singletonList(imageIds.get(0)));
            expectedRemainingImageCount--;
            expectedMovedImageCount++;
        }
        if (Boolean.TRUE.equals(includeOrphans)) {
            final ChildOption option = new ChildOption();
            option.includeType = Collections.singletonList("Roi");
            move.childOptions = Collections.singletonList(option);
            expectedRemainingRoiCount--;
            expectedMovedRoiCount++;
        } else if (Boolean.FALSE.equals(includeOrphans)) {
            final ChildOption option = new ChildOption();
            option.excludeType = Collections.singletonList("Roi");
            move.childOptions = Collections.singletonList(option);
        } else if (expectedRemainingFolderCount + expectedRemainingImageCount == 0) {
            expectedRemainingRoiCount--;
            expectedMovedRoiCount++;
        }
        doChange(client, factory, move, isLegalMove);

        if (isLegalMove) {

            /* check that the counts of remaining objects are as expected */

            Assert.assertEquals(countInstances(Roi.class, roiIds), expectedRemainingRoiCount);
            Assert.assertEquals(countInstances(Folder.class, folderIds), expectedRemainingFolderCount);
            Assert.assertEquals(countInstances(Image.class, imageIds), expectedRemainingImageCount);

            disconnect();

            /* work in second group */

            loginUser(toGroup);

            /* check that the counts of moved objects are as expected */

            Assert.assertEquals(countInstances(Roi.class, roiIds), expectedMovedRoiCount);
            Assert.assertEquals(countInstances(Folder.class, folderIds), expectedMovedFolderCount);
            Assert.assertEquals(countInstances(Image.class, imageIds), expectedMovedImageCount);
        }

        disconnect();
    }

    /**
     * @return a variety of test cases for moving ROIs
     */
    @DataProvider(name = "contained ROI test cases")
    public Object[][] provideMoveRoiCases() {
        int index = 0;
        final int FOLDER_COUNT = index++;
        final int IMAGE_COUNT = index++;
        final int TARGET_FOLDER = index++;
        final int TARGET_IMAGE = index++;
        final int INCLUDE_ORPHANS = index++;

        final Integer[] folderCountCases = new Integer[]{0, 1, 2};
        final Integer[] imageCountCases = new Integer[]{0, 1};
        final Boolean[] targetCases = new Boolean[]{false, true};
        final Boolean[] includeCases = new Boolean[]{null, false, true};

        final List<Object[]> testCases = new ArrayList<Object[]>();

        for (final Integer folderCount : folderCountCases) {
            for (final Integer imageCount : imageCountCases) {
                for (final Boolean targetFolder : targetCases) {
                    for (final Boolean targetImage : targetCases) {
                        for (final Boolean includeOrphans : includeCases) {
                            if (targetFolder && folderCount == 0 || targetImage && imageCount == 0 ||
                                    !(targetFolder || targetImage)) {
                                continue;
                            }
                            final Object[] testCase = new Object[index];
                            testCase[FOLDER_COUNT] = folderCount;
                            testCase[IMAGE_COUNT] = imageCount;
                            testCase[TARGET_FOLDER] = targetFolder;
                            testCase[TARGET_IMAGE] = targetImage;
                            testCase[INCLUDE_ORPHANS] = includeOrphans;
                            // DEBUG: if (folderCount == 1 && imageCount == 1 && targetFolder == true && targetImage == false
                            //        && Boolean.TRUE.equals(includeOrphans))
                            testCases.add(testCase);
                        }
                    }
                }
            }
        }

        return testCases.toArray(new Object[testCases.size()][]);
    }
}
