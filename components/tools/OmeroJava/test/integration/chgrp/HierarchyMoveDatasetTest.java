/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee & Open Microscopy Environment.
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

/**
 * @author Scott Littlewood, <a href="mailto:sylittlewood@dundee.ac.uk">sylittlewood@dundee.ac.uk</a>
 * @since Beta4.4
 */
import integration.AbstractServerTest;

import java.util.ArrayList;
import java.util.List;

import omero.api.Save;
import omero.cmd.Chgrp2;
import omero.cmd.DoAll;
import omero.cmd.Request;
import omero.gateway.util.Requests;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.ProjectI;
import omero.sys.EventContext;
import omero.sys.ParametersI;

import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

public class HierarchyMoveDatasetTest extends AbstractServerTest {

    /** Indicates to move but not to link. */
    static final int LINK_NONE = 0;

    /** Indicates to move and create a new object and link it. */
    static final int LINK_NEW = 1;

    /** Indicates to move and link to an existing object. */
    static final int LINK_EXISTING = 2;

    public HierarchyMoveDatasetTest() {
        super();
    }

    /**
     * Call {@link #moveDataDatasetToProject(String, String, int, int, boolean)}
     * with false for chownLink which maintains the previous usage.
     */
    private void moveDataDatasetToProject(String source, String target,
            int linkLevel, int memberLevel) throws Exception {
        moveDataDatasetToProject(source, target, linkLevel, memberLevel, false);
    }

    /**
     * Tests the move of a dataset to a new group, a project in the new group is
     * selected and the dataset should be linked to that project.
     *
     * @param source
     *            The permissions of the source group.
     * @param target
     *            The permissions of the destination group.
     * @param newDestinationObject
     *            Pass <code>true</code> if the project has to be created,
     *            <code>false</code> otherwise
     * @param asAdmin
     *            Pass <code>true</code> to move the data as admin
     *            <code>false</code> otherwise.
     */
    private void moveDataDatasetToProject(String source, String target,
            int linkLevel, int memberLevel, boolean chownLink) throws Exception {
        // Step 1
        // Create the group with the dataset
        EventContext ctx = newUserAndGroup(source);

        if (memberLevel == GROUP_OWNER)
            makeGroupOwner();
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());

        // log out
        disconnect();

        // Create a new group, the user is now a member of the new group.
        ExperimenterGroup g = newGroupAddUser(target, ctx.userId);

        loginUser(g);
        if (memberLevel == GROUP_OWNER)
            makeGroupOwner();
        // Create project in the new group.
        Project p = (Project) iUpdate.saveAndReturnObject(mmFactory
                .simpleProjectData().asIObject());

        // log out
        disconnect();

        // Step 2: log into source group to perform the move // No. See below.
        switch (memberLevel) {
            case MEMBER:
            case GROUP_OWNER:
            default:
                loginUser(ctx);
                break;
            case ADMIN:
                logRootIntoGroup(ctx.groupId);
        }

        // Step 3: if this is a private group and we're an admin, then we'll
        // need to assert specific ownership of the links and new objects to
        // be the data owner
        Experimenter o = null;
        if (target.equals("rw----") && chownLink) {
            switch (memberLevel) {
                case MEMBER:
                default:
                    // no-op
                    break;
                case GROUP_OWNER:
                case ADMIN:
                    o = new omero.model.ExperimenterI(ctx.userId, false);
                    break;
            }
        }

        // Create commands to move and create the link in target
        List<Request> list = new ArrayList<Request>();
        final Chgrp2 dc = Requests.chgrp("Dataset", d.getId().getValue(), g.getId().getValue());
        list.add(dc);

        ProjectDatasetLink link = null;
        switch (linkLevel) {
            case LINK_NEW:
                link = new ProjectDatasetLinkI();
                link.setChild(new DatasetI(d.getId().getValue(), false));
                ProjectI prj = new ProjectI();
                String n = "prj for Dataset:" + d.getId().getValue();
                prj.setName(omero.rtypes.rstring(n));
                link.setParent(prj);
                if (o != null) {
                    link.getDetails().setOwner(o);
                    prj.getDetails().setOwner(o);
                }
                break;
            case LINK_EXISTING:
                link = new ProjectDatasetLinkI();
                link.setChild(new DatasetI(d.getId().getValue(), false));
                link.setParent(new ProjectI(p.getId().getValue(), false));
                if (o != null) {
                    link.getDetails().setOwner(o);
                }
        }

        if (link != null) {
            Save cmd = new Save();
            cmd.obj = link;
            list.add(cmd);
        }
        DoAll all = new DoAll();
        all.requests = list;

        // Do the move.
        doChange(all, g.getId().getValue()); // Login to target group

        // Check if the dataset has been removed.
        ParametersI param = new ParametersI();
        param.addId(d.getId().getValue());
        String sql = "select i from Dataset as i where i.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        // log out from source group
        disconnect();

        // Step 3:

        // Connect to target group

        // Step 2: log into source group to perform the move
        switch (memberLevel) {
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

        // Check the link exists.
        if (link != null) {
            param = new ParametersI();
            param.map.put("childID", d.getId());
            if (linkLevel == LINK_EXISTING) {
                param.map.put("parentID", p.getId());
                sql = "select i from ProjectDatasetLink as i where "
                        + "i.child.id = :childID and i.parent.id = :parentID";
            } else {
                sql = "select i from ProjectDatasetLink as i where "
                        + "i.child.id = :childID";
            }
            assertNotNull(iQuery.findByQuery(sql, param));
        }
    }

    /**
     * Tests to move a dataset containing an image also contained in another
     * dataset. The dataset should be moved but not the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    public void testMoveDatasetWithSharedImage() throws Exception {
        String perms = "rw----";
        EventContext ctx = newUserAndGroup(perms);
        ExperimenterGroup g = newGroupAddUser(perms, ctx.userId);
        iAdmin.getEventContext(); // Refresh

        Dataset s1 = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());

        Dataset s2 = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());

        // Plate w/o plate acquisition
        Image i1 = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        // Plate with plate acquisition
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

        final Chgrp2 dc = Requests.chgrp("Dataset", s1.getId().getValue(), g.getId().getValue());
        callback(true, client, dc);

        List<Long> ids = new ArrayList<Long>();
        ids.add(i1.getId().getValue());

        ParametersI param = new ParametersI();
        param.addIds(ids);
        String sql = "select i from Image as i where i.id in (:ids)";
        List<IObject> results = iQuery.findAllByQuery(sql, param);
        assertEquals(results.size(), ids.size());

        // S1 should have moved
        param = new ParametersI();
        param.addId(s1.getId().getValue());
        sql = "select i from Dataset as i where i.id = :id";
        assertNull(iQuery.findByQuery(sql, param));

        param = new ParametersI();
        param.addId(s2.getId().getValue());
        assertNotNull(iQuery.findByQuery(sql, param));

        // Check that the data moved
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
     * Tests to move a dataset to a project contained in the target group.
     * <code>RW----</code> to <code>RW----</code>. The move is done by the owner
     * of the data.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToProjectRWtoRW() throws Exception {
        moveDataDatasetToProject("rw----", "rw----", LINK_EXISTING, MEMBER);
    }

    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWR---</code> to <code>RWR---</code>. The move is done by the owner
     * of the data.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToProjectRWRtoRWR() throws Exception {
        moveDataDatasetToProject("rwr---", "rwr---", LINK_EXISTING, MEMBER);
    }

    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWR---</code> to <code>RW----</code>. The move is done by the owner
     * of the data.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToProjectRWRtoRW() throws Exception {
        moveDataDatasetToProject("rwr---", "rw----", LINK_EXISTING, MEMBER);
    }

    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWRA--</code> to <code>RW----</code>. The move is done by the owner
     * of the data.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToProjectRWRAtoRW() throws Exception {
        moveDataDatasetToProject("rwra--", "rw----", LINK_EXISTING, MEMBER);
    }

    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWRA--</code> to <code>RWRA--</code>. The move is done by the owner
     * of the data.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToProjectRWRAtoRWRA() throws Exception {
        moveDataDatasetToProject("rwra--", "rwra--", LINK_EXISTING, MEMBER);
    }

    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RW----</code> to <code>RWR---</code>. The move is done by the owner
     * of the data.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToProjectRWtoRWR() throws Exception {
        moveDataDatasetToProject("rw----", "rwr---", LINK_EXISTING, MEMBER);
    }

    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWR---</code> to <code>RWRA--</code>. The move is done by the owner
     * of the data.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToProjectRWtoRWRA() throws Exception {
        moveDataDatasetToProject("rw----", "rwra--", LINK_EXISTING, MEMBER);
    }

    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWR---</code> to <code>RWRA--</code>. The move is done by the owner
     * of the data.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToProjectRWRtoRWRA() throws Exception {
        moveDataDatasetToProject("rwr---", "rwra--", LINK_EXISTING, MEMBER);
    }

    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWR---</code> to <code>RW----</code>. The move is done by the owner
     * of the data. A new Project will be created.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToNewProjectRWtoRW() throws Exception {
        moveDataDatasetToProject("rw----", "rw----", LINK_NEW, MEMBER);
    }

    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWR---</code> to <code>RWR---</code>. The move is done by the owner
     * of the data.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToNewProjectRWRtoRWR() throws Exception {
        moveDataDatasetToProject("rwr---", "rwr---", LINK_NEW, MEMBER);
    }

    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWR---</code> to <code>RW----</code>. The move is done by the owner
     * of the data.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToNewProjectRWRtoRW() throws Exception {
        moveDataDatasetToProject("rwr---", "rw----", LINK_NEW, MEMBER);
    }

    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWRA--</code> to <code>RW----</code>. The move is done by the owner
     * of the data.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToNewProjectRWRAtoRW() throws Exception {
        moveDataDatasetToProject("rwra--", "rw----", LINK_NEW, MEMBER);
    }

    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWRA--</code> to <code>RWRA--</code>. The move is done by the owner
     * of the data.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToNewProjectRWRAtoRWRA() throws Exception {
        moveDataDatasetToProject("rwra--", "rwra--", LINK_NEW, MEMBER);
    }

    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RW----</code> to <code>RWR---</code>. The move is done by the owner
     * of the data.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToNewProjectRWtoRWR() throws Exception {
        moveDataDatasetToProject("rw----", "rwr---", LINK_NEW, MEMBER);
    }

    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWR---</code> to <code>RWRA--</code>. The move is done by the owner
     * of the data.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToNewProjectRWtoRWRA() throws Exception {
        moveDataDatasetToProject("rw----", "rwra--", LINK_NEW, MEMBER);
    }

    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWR---</code> to <code>RWRA--</code>. The move is done by the owner
     * of the data.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToNewProjectRWRtoRWRA() throws Exception {
        moveDataDatasetToProject("rwr---", "rwra--", LINK_NEW, MEMBER);
    }

    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RW----</code> to <code>RW----</code>. The move is done by the
     * administrator.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToProjectRWtoRWByAdmin() throws Exception {
        try {
            moveDataDatasetToProject("rw----", "rw----", LINK_EXISTING, ADMIN);
            fail("A security Violation should have been thrown."
                    + "Admin not allowed to create a link in private group.");
        } catch (AssertionError e) {
            if (!e.getMessage().contains("Found ERR")) {
                throw e;
            }
        }
        moveDataDatasetToProject("rw----", "rw----", LINK_EXISTING, ADMIN, true);
    }

    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RW----</code> to <code>RW----</code>. The move is done by the
     * administrator.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToNewProjectRWtoRWByAdmin() throws Exception {
        try {
            moveDataDatasetToProject("rw----", "rw----", LINK_NEW, ADMIN);
            fail("A security Violation should have been thrown."
                    + "Admin not allowed to create a link in private group.");
        } catch (AssertionError e) {
            if (!e.getMessage().contains("Found ERR")) {
                throw e;
            }
        }
        moveDataDatasetToProject("rw----", "rw----", LINK_NEW, ADMIN, true);
    }

    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RW----</code> to <code>RW----</code>. The move is done by the
     * administrator.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetRWtoRWByAdmin() throws Exception {
        moveDataDatasetToProject("rw----", "rw----", LINK_NONE, ADMIN);
    }

    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RW----</code> to <code>RW----</code>. The move is done by the
     * administrator.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToProjectRWRtoRWByAdmin() throws Exception {
        try {
            moveDataDatasetToProject("rwr---", "rw----", LINK_EXISTING, ADMIN);
            fail("A security Violation should have been thrown."
                    + "Admin not allowed to create a link in private group.");
        } catch (AssertionError e) {
            if (!e.getMessage().contains("Found ERR")) {
                throw e;
            }
        }
        moveDataDatasetToProject("rwr---", "rw----", LINK_EXISTING, ADMIN, true);
    }

    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RW----</code> to <code>RW----</code>. The move is done by the
     * administrator.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToNewProjectRWRtoRWByAdmin() throws Exception {
        try {
            moveDataDatasetToProject("rwr---", "rw----", LINK_NEW, ADMIN);
            fail("A security Violation should have been thrown."
                    + "Admin not allowed to create a link in private group.");
        } catch (AssertionError e) {
            if (!e.getMessage().contains("Found ERR")) {
                throw e;
            }
        }
        moveDataDatasetToProject("rwr---", "rw----", LINK_NEW, ADMIN, true);
    }

    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RW----</code> to <code>RW----</code>. The move is done by the
     * administrator.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetRWRtoRWByAdmin() throws Exception {
        moveDataDatasetToProject("rwr---", "rw----", LINK_NONE, ADMIN);
    }

    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWRA--</code> to <code>RW----</code>. The move is done by the
     * administrator.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToProjectRWRAtoRWByAdmin() throws Exception {
        try {
            moveDataDatasetToProject("rwra--", "rw----", LINK_EXISTING, ADMIN);
            fail("A security Violation should have been thrown."
                    + "Admin not allowed to create a link in private group.");
        } catch (AssertionError e) {
            if (!e.getMessage().contains("Found ERR")) {
                throw e;
            }
        }
        moveDataDatasetToProject("rwra--", "rw----", LINK_EXISTING, ADMIN, true);
    }

    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWRA--</code> to <code>RW----</code>. The move is done by the
     * administrator.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetToNewProjectRWRAtoRWByAdmin() throws Exception {
        try {
            moveDataDatasetToProject("rwr---", "rw----", LINK_NEW, ADMIN);
            fail("A security Violation should have been thrown."
                    + "Admin not allowed to create a link in private group.");
        } catch (AssertionError e) {
            if (!e.getMessage().contains("Found ERR")) {
                throw e;
            }
        }
        moveDataDatasetToProject("rwr---", "rw----", LINK_NEW, ADMIN, true);
    }

    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWRA--</code> to <code>RW----</code>. The move is done by the
     * administrator.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetRWRAtoRWByAdmin() throws Exception {
        moveDataDatasetToProject("rwra--", "rwr---", LINK_NONE, ADMIN);
    }

    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RW----</code> to <code>RWR---</code>. The move is done by the
     * administrator.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetRWtoRWRByAdmin() throws Exception {
        moveDataDatasetToProject("rwra--", "rwr---", LINK_NONE, ADMIN);
    }

    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RW----</code> to <code>RWRA---</code>. The move is done by the
     * administrator.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetRWtoRWRAByAdmin() throws Exception {
        moveDataDatasetToProject("rw----", "rwra--", LINK_NONE, ADMIN);
    }

    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWR---</code> to <code>RWRA--</code>. The move is done by the
     * administrator.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetRWRtoRWRAByAdmin() throws Exception {
        moveDataDatasetToProject("rwr---", "rwra--", LINK_NONE, ADMIN);
    }

    /**
     * Tests to move a dataset to a project contained in the target group.
     * <code>RWRA--</code> to <code>RWR---</code>. The move is done by the
     * administrator.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMoveDatasetRWRAtoRWRByAdmin() throws Exception {
        moveDataDatasetToProject("rwra--", "rwr---", LINK_NONE, ADMIN);
    }

}