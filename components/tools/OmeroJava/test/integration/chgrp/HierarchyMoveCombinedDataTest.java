/*
 * integration.chgrp.HierarchyMoveCombinedDataTest
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
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
package integration.chgrp;

import integration.AbstractServerTest;
import integration.DeleteServiceTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import omero.cmd.Chgrp2;
import omero.cmd.graphs.ChildOption;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.ExperimenterGroup;
import omero.model.IObject;
import omero.model.Image;
import omero.sys.EventContext;
import omero.sys.ParametersI;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

import static org.testng.AssertJUnit.*;

/**
 * Tests the move of data objects containing others members data.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class HierarchyMoveCombinedDataTest extends AbstractServerTest {

    /**
     * Moves a dataset containing an image owned by another user and an image
     * owned.
     *
     * @param source
     *            The permissions of the source group.
     * @param target
     *            The permissions of the destination group.
     * @param sourceRole
     *            The user's role in the source group.
     * @param targetRole
     *            The user's role in the target group.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    private void moveDatasetAndImage(String source, String target,
            int sourceRole, int targetLevel) throws Exception {
        // Step 1
        // Create a new group
        EventContext ctx = newUserAndGroup(source);
        // Create an image.
        Image img1 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .createImage());
        long user1 = img1.getDetails().getOwner().getId().getValue();
        disconnect();

        // Step 2
        // create a new user and add it to the group
        ctx = newUserInGroup(ctx);
        switch (sourceRole) {
            case GROUP_OWNER:
                makeGroupOwner();
                break;
            case ADMIN:
                logRootIntoGroup(ctx);
        }

        loginUser(ctx);
        // Create a dataset
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        // link the dataset and the image
        DatasetImageLink link = new DatasetImageLinkI();
        link.setChild(img1);
        link.setParent(new DatasetI(d.getId().getValue(), false));
        iUpdate.saveAndReturnObject(link);

        Image img2 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .createImage());
        link = new DatasetImageLinkI();
        link.setChild(img2);
        link.setParent(new DatasetI(d.getId().getValue(), false));
        iUpdate.saveAndReturnObject(link);

        long user2 = d.getDetails().getOwner().getId().getValue();
        assertTrue(user1 != user2);
        disconnect();

        // Step 3
        // Create a new group, the user is now a member of the new group.
        ExperimenterGroup g = newGroupAddUser(target, ctx.userId);
        loginUser(g);

        disconnect();

        // Step 4
        // reconnect to the source group.
        switch (sourceRole) {
            case MEMBER:
            case GROUP_OWNER:
            default:
                loginUser(ctx);
                break;
            case ADMIN:
                logRootIntoGroup(ctx.groupId);
        }
        // Create commands to move and create the link in target
        final Chgrp2 dc = new Chgrp2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Dataset.class.getSimpleName(),
                Collections.singletonList(d.getId().getValue()));
        dc.groupId = g.getId().getValue();
        callback(true, client, dc);

        // Check if the dataset has been removed.
        ParametersI param = new ParametersI();
        param.addId(d.getId().getValue());
        String sql = "select i from Dataset as i where i.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        List<Long> ids = new ArrayList<Long>();
        ids.add(img1.getId().getValue());
        ids.add(img2.getId().getValue());

        param = new ParametersI();
        param.addIds(ids);
        sql = "select i from Image as i where i.id in (:ids)";
        List<IObject> results = iQuery.findAllByQuery(sql, param);
        assertEquals(results.size(), 0);

        // log out from source group
        disconnect();

        // Step 5
        // log into source group to perform the move
        switch (sourceRole) {
            case MEMBER:
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

        // Check if the dataset is in the target group.
        assertNotNull(iQuery.findByQuery(sql, param));

        // Check
        param = new ParametersI();
        param.addIds(ids);
        sql = "select i from Image as i where i.id in (:ids)";
        results = iQuery.findAllByQuery(sql, param);
        assertEquals(results.size(), ids.size());
        Iterator<IObject> i = results.iterator();
        int count = 0;
        while (i.hasNext()) {
            if (ids.contains(i.next().getId().getValue()))
                count++;
        }
        assertEquals(count, ids.size());
        disconnect();
    }

    /**
     * Test to move by the group's owner a dataset containing one image owned by
     * another group's member from a <code>RWRA</code> group to
     * <code>RWRA</code> group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetByGroupOwnerRWRAtoRWRA() throws Exception {
        moveDatasetAndImage("rwra--", "rwra--", GROUP_OWNER, MEMBER);
    }

    /**
     * Test to move by the group's owner a dataset containing one image owned by
     * another group's member from a <code>RWR</code> group to <code>RWR</code>
     * group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetByGroupOwnerRWRtoRWR() throws Exception {
        moveDatasetAndImage("rwr---", "rwr---", GROUP_OWNER, MEMBER);
    }

    /**
     * Test to move by the group's owner a dataset containing one image owned by
     * another group's member from a <code>RWR</code> group to <code>RWR</code>
     * group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetByGroupOwnerRWRWtoRWRW() throws Exception {
        moveDatasetAndImage("rwrw--", "rwrw--", GROUP_OWNER, MEMBER);
    }

    /**
     * Test to move by an admin a dataset containing one image owned by another
     * group's member from a <code>RWRA</code> group to <code>RWRA</code> group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetByAdminRWRWtoRWRW() throws Exception {
        moveDatasetAndImage("rwrw--", "rwrw--", ADMIN, MEMBER);
    }

    /**
     * Test to move by the group's owner a dataset containing one image owned by
     * another group's member from a <code>RWR</code> group to <code>RWR</code>
     * group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetByMemberRWRWtoRWRW() throws Exception {
        moveDatasetAndImage("rwrw--", "rwrw--", MEMBER, MEMBER);
    }

}
