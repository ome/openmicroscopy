/*
 * integration.chmod.RolesTest 
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
package integration.chmod;


import java.util.Collections;
import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

import static org.testng.AssertJUnit.*;
import static omero.rtypes.rstring;
import omero.cmd.Delete2;
import omero.cmd.graphs.ChildOption;
import omero.model.Annotation;
import omero.model.CommentAnnotation;
import omero.model.CommentAnnotationI;
import omero.model.Dataset;
import omero.model.DatasetAnnotationLink;
import omero.model.DatasetAnnotationLinkI;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Permissions;
import omero.sys.EventContext;
import omero.sys.ParametersI;
import pojos.DatasetData;
import integration.AbstractServerTest;
import integration.DeleteServiceTest;

/**
 * Tests the can edit, can annotate.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class RolesTest extends AbstractServerTest {

    /**
     * Since we are creating a new client on each invocation, we should also
     * clean it up. Note: {@link #newUserAndGroup(String)} also closes, but not
     * the very last invocation.
     */
    @AfterMethod
    public void close() throws Exception {
        clean();
    }

    // Group RW---- i.e. private
    /**
     * Test the interaction with an object in a RW member
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testInteractionByMemberRW() throws Exception {
        EventContext ec = newUserAndGroup("rw----");
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        Permissions perms = d.getDetails().getPermissions();
        long id = d.getId().getValue();
        // make sure data owner can do everything
        assertTrue(perms.canEdit());
        assertTrue(perms.canAnnotate());
        assertTrue(perms.canDelete());
        assertTrue(perms.canLink());

        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        // Create link
        DatasetImageLink l = new DatasetImageLinkI();
        l.link(new DatasetI(d.getId().getValue(), false), img);
        l = (DatasetImageLink) iUpdate.saveAndReturnObject(l);

        // Create annotation
        CommentAnnotation annotation = new CommentAnnotationI();
        annotation.setTextValue(omero.rtypes.rstring("comment"));
        Annotation ann = (Annotation) iUpdate.saveAndReturnObject(annotation);
        DatasetAnnotationLink dl = new DatasetAnnotationLinkI();
        dl.link(new DatasetI(d.getId().getValue(), false), ann);
        dl = (DatasetAnnotationLink) iUpdate.saveAndReturnObject(dl);

        disconnect();
        // Now a new member to the group.
        newUserInGroup(ec);
        String sql = "select i from Dataset as i ";
        sql += "where i.id = :id";
        ParametersI param = new ParametersI();
        param.addId(id);
        List<IObject> datasets = iQuery.findAllByQuery(sql, param);
        // Cannot view the data.
        assertEquals(datasets.size(), 0);

        // Create a link canLink
        // Try to delete the link i.e. canDelete
        try {
            Delete2 dc = new Delete2();
            dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                    DatasetImageLink.class.getSimpleName(),
                    Collections.singletonList(l.getId().getValue()));
            callback(false, client, dc);
        } catch (Exception e) {

            fail("Member should not be allowed to delete "
                    + "an image/dataset link.");
        }

        try {
            Delete2 dc = new Delete2();
            dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                    DatasetAnnotationLink.class.getSimpleName(),
                    Collections.singletonList(dl.getId().getValue()));
            callback(false, client, dc);
        } catch (Exception e) {
            fail("Member should not be allowed to delete "
                    + "an image/dataset link.");
        }

        // Try to delete the annotation i.e. canDelete
        try {
            Delete2 dc = new Delete2();
            dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                    Annotation.class.getSimpleName(),
                    Collections.singletonList(ann.getId().getValue()));
            callback(false, client, dc);
        } catch (Exception e) {
            fail("Member should not be allowed to delete " + "the annotation.");
        }


        // Try to link an image i.e. canLink
        try {
            img = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage());
            l = new DatasetImageLinkI();
            l.link(new DatasetI(id, false), img);
            iUpdate.saveAndReturnObject(l);
            fail("Member should not be allowed to create an image/dataset link.");
        } catch (Exception e) {
        }

        // Try to create the annotation i.e. canAnnotate
        try {
            annotation = new CommentAnnotationI();
            annotation.setTextValue(omero.rtypes.rstring("comment"));
            ann = (Annotation) iUpdate.saveAndReturnObject(annotation);
            dl = new DatasetAnnotationLinkI();
            dl.link(new DatasetI(d.getId().getValue(), false), ann);
            dl = (DatasetAnnotationLink) iUpdate.saveAndReturnObject(dl);
            fail("Member should not be allowed to annotate a dataset.");
        } catch (Exception e) {
        }

        // Try to edit i.e. canEdit
        try {
            d.setName(rstring("newNAme"));
            iUpdate.saveAndReturnObject(d);
            fail("Member should not be allowed to edit a dataset.");
        } catch (Exception e) {
        }
    }

    /**
     * Test the interaction with an object in a RW group by the owner
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testInteractionByGroupOwnerRW() throws Exception {
        EventContext ec = newUserAndGroup("rw----");
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        Permissions perms = d.getDetails().getPermissions();
        long id = d.getId().getValue();
        assertTrue(perms.canEdit());
        assertTrue(perms.canAnnotate());
        assertTrue(perms.canDelete());
        assertTrue(perms.canLink());

        // create image link

        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        DatasetImageLink l = new DatasetImageLinkI();
        l.link(new DatasetI(d.getId().getValue(), false), img);
        l = (DatasetImageLink) iUpdate.saveAndReturnObject(l);

        // Create annotation
        CommentAnnotation annotation = new CommentAnnotationI();
        annotation.setTextValue(omero.rtypes.rstring("comment"));
        Annotation ann = (Annotation) iUpdate.saveAndReturnObject(annotation);
        DatasetAnnotationLink dl = new DatasetAnnotationLinkI();
        dl.link(new DatasetI(id, false), ann);
        dl = (DatasetAnnotationLink) iUpdate.saveAndReturnObject(dl);

        disconnect();
        // Now a new member to the group.
        newUserInGroup(ec);
        makeGroupOwner();
        String sql = "select i from Dataset as i ";
        sql += "where i.id = :id";
        ParametersI param = new ParametersI();
        param.addId(id);
        List<IObject> datasets = iQuery.findAllByQuery(sql, param);
        assertEquals(datasets.size(), 1);
        d = (Dataset) datasets.get(0);
        perms = d.getDetails().getPermissions();

        assertTrue(perms.canEdit());
        assertFalse(perms.canAnnotate());
        assertTrue(perms.canDelete());
        assertFalse(perms.canLink());

        // Create a link canLink
        // Try to delete the link i.e. canDelete
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                DatasetImageLink.class.getSimpleName(),
                Collections.singletonList(l.getId().getValue()));
        callback(true, client, dc);

        // Try to delete the annotation link i.e. canDelete
        dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                DatasetAnnotationLink.class.getSimpleName(),
                Collections.singletonList(dl.getId().getValue()));
        callback(true, client, dc);

        // Try to delete the annotation i.e. canDelete
        dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Annotation.class.getSimpleName(),
                Collections.singletonList(ann.getId().getValue()));
        callback(true, client, dc);

        // Try to link an image i.e. canLink
        try {
            img = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage());
            l = new DatasetImageLinkI();
            l.link(new DatasetI(d.getId().getValue(), false), img);
            iUpdate.saveAndReturnObject(l);
            fail("Group owner should not be allowed to create "
                    + "an image/dataset link.");
        } catch (Exception e) {
        }

        // Try to create the annotation i.e. canAnnotate
        try {
            annotation = new CommentAnnotationI();
            annotation.setTextValue(omero.rtypes.rstring("comment"));
            ann = (Annotation) iUpdate.saveAndReturnObject(annotation);
            dl = new DatasetAnnotationLinkI();
            dl.link(new DatasetI(d.getId().getValue(), false), ann);
            dl = (DatasetAnnotationLink) iUpdate.saveAndReturnObject(dl);
            fail("Group owner should not be allowed to annotate "
                    + "a dataset.");
        } catch (Exception e) {
        }

        // Try to edit i.e. canEdit
        d.setName(rstring("newNAme"));
        iUpdate.saveAndReturnObject(d);
    }

    /**
     * Test the interaction with an object in a RW group by the owner
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testInteractionByAdminRW() throws Exception {
        EventContext ec = newUserAndGroup("rw----");
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        Permissions perms = d.getDetails().getPermissions();
        long id = d.getId().getValue();

        assertTrue(perms.canEdit());
        assertTrue(perms.canAnnotate());
        assertTrue(perms.canDelete());
        assertTrue(perms.canLink());

        // create image link

        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        DatasetImageLink l = new DatasetImageLinkI();
        l.link(new DatasetI(id, false), img);
        l = (DatasetImageLink) iUpdate.saveAndReturnObject(l);

        // Create annotation
        CommentAnnotation annotation = new CommentAnnotationI();
        annotation.setTextValue(omero.rtypes.rstring("comment"));
        Annotation ann = (Annotation) iUpdate.saveAndReturnObject(annotation);
        DatasetAnnotationLink dl = new DatasetAnnotationLinkI();
        dl.link(new DatasetI(id, false), ann);
        dl = (DatasetAnnotationLink) iUpdate.saveAndReturnObject(dl);

        disconnect();
        logRootIntoGroup(ec);

        String sql = "select i from Dataset as i ";
        sql += "where i.id = :id";
        ParametersI param = new ParametersI();
        param.addId(id);
        List<IObject> datasets = iQuery.findAllByQuery(sql, param);
        assertEquals(datasets.size(), 1);
        d = (Dataset) datasets.get(0);
        perms = d.getDetails().getPermissions();

        assertFalse(perms.canAnnotate());
        assertTrue(perms.canDelete());
        assertTrue(perms.canEdit());
        assertFalse(perms.canLink());

        // Create a link canLink
        // Try to delete the link i.e. canDelete
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                DatasetImageLink.class.getSimpleName(),
                Collections.singletonList(l.getId().getValue()));
        callback(true, client, dc);

        // Try to delete the annotation link i.e. canDelete
        dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                DatasetAnnotationLink.class.getSimpleName(),
                Collections.singletonList(dl.getId().getValue()));
        callback(true, client, dc);

        // Try to delete the annotation i.e. canDelete
        dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Annotation.class.getSimpleName(),
                Collections.singletonList(ann.getId().getValue()));
        callback(true, client, dc);

        // Try to link an image i.e. canLink
        try {
            img = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage());
            l = new DatasetImageLinkI();
            l.link(new DatasetI(id, false), img);
            iUpdate.saveAndReturnObject(l);
            fail("Admin should not be allowed to create an image/dataset link.");
        } catch (Exception e) {
        }

        // Try to create the annotation i.e. canAnnotate
        try {
            annotation = new CommentAnnotationI();
            annotation.setTextValue(omero.rtypes.rstring("comment"));
            ann = (Annotation) iUpdate.saveAndReturnObject(annotation);
            dl = new DatasetAnnotationLinkI();
            dl.link(new DatasetI(id, false), ann);
            dl = (DatasetAnnotationLink) iUpdate.saveAndReturnObject(dl);
            fail("Admin should not be allowed to annotate a dataset.");
        } catch (Exception e) {
        }

        // Try to edit i.e. canEdit
        d.setName(rstring("newNAme"));
        iUpdate.saveAndReturnObject(d);
    }

    // Group RWR---
    /**
     * Test the interaction with an object in a RWR group by a member
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testInteractionByMemberRWR() throws Exception {
        EventContext ec = newUserAndGroup("rwr---");
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        long id = d.getId().getValue();

        Permissions perms = d.getDetails().getPermissions();
        // make sure data owner can do everything
        assertTrue(perms.canEdit());
        assertTrue(perms.canAnnotate());
        assertTrue(perms.canDelete());
        assertTrue(perms.canLink());

        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        // Create link
        DatasetImageLink l = new DatasetImageLinkI();
        l.link(new DatasetI(id, false), img);
        l = (DatasetImageLink) iUpdate.saveAndReturnObject(l);

        // Create annotation
        CommentAnnotation annotation = new CommentAnnotationI();
        annotation.setTextValue(omero.rtypes.rstring("comment"));
        Annotation ann = (Annotation) iUpdate.saveAndReturnObject(annotation);
        DatasetAnnotationLink dl = new DatasetAnnotationLinkI();
        dl.link(new DatasetI(id, false), ann);
        dl = (DatasetAnnotationLink) iUpdate.saveAndReturnObject(dl);

        disconnect();

        // Now a new member to the group.
        newUserInGroup(ec);
        String sql = "select i from Dataset as i ";
        sql += "where i.id = :id";
        ParametersI param = new ParametersI();
        param.addId(id);
        List<IObject> datasets = iQuery.findAllByQuery(sql, param);
        assertEquals(datasets.size(), 1);

        // Just a member should be able to neither (for the moment)
        // Reload the perms (from the object that the member loaded)
        // and check status.
        d = (Dataset) datasets.get(0);

        perms = d.getDetails().getPermissions();

        assertFalse(perms.canEdit());
        assertFalse(perms.canAnnotate());
        assertFalse(perms.canDelete());
        assertFalse(perms.canLink());

        DatasetData data = new DatasetData(d);
        assertFalse(data.canEdit());
        assertFalse(data.canAnnotate());
        assertFalse(data.canDelete());
        assertFalse(data.canLink());
        // Create a link canLink
        // Try to delete the link i.e. canDelete
        try {
            Delete2 dc = new Delete2();
            dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                    DatasetImageLink.class.getSimpleName(),
                    Collections.singletonList(l.getId().getValue()));
            callback(false, client, dc);
        } catch (Exception e) {

            fail("Member should not be allowed to delete "
                    + "an image/dataset link.");
        }

        try {
            Delete2 dc = new Delete2();
            dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                    DatasetAnnotationLink.class.getSimpleName(),
                    Collections.singletonList(dl.getId().getValue()));
            callback(false, client, dc);
        } catch (Exception e) {
            fail("Member should not be allowed to delete "
                    + "an image/dataset link.");
        }

        // Try to delete the annotation i.e. canDelete
        try {
            Delete2 dc = new Delete2();
            dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                    Annotation.class.getSimpleName(),
                    Collections.singletonList(ann.getId().getValue()));
            callback(false, client, dc);
        } catch (Exception e) {
            fail("Member should not be allowed to delete " + "the annotation.");
        }

        // Try to link an image i.e. canLink
        try {
            img = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage());
            l = new DatasetImageLinkI();
            l.link(new DatasetI(id, false), img);
            iUpdate.saveAndReturnObject(l);
            fail("Member should not be allowed to create an image/dataset link.");
        } catch (Exception e) {
        }

        // Try to create the annotation i.e. canAnnotate
        try {
            annotation = new CommentAnnotationI();
            annotation.setTextValue(omero.rtypes.rstring("comment"));
            ann = (Annotation) iUpdate.saveAndReturnObject(annotation);
            dl = new DatasetAnnotationLinkI();
            dl.link(new DatasetI(id, false), ann);
            dl = (DatasetAnnotationLink) iUpdate.saveAndReturnObject(dl);
            fail("Member should not be allowed to annotate a dataset.");
        } catch (Exception e) {
        }

        // Try to edit i.e. canEdit
        try {
            d.setName(rstring("newNAme"));
            iUpdate.saveAndReturnObject(d);
            fail("Member should not be allowed to edit a dataset.");
        } catch (Exception e) {
        }
    }

    /**
     * Test the interaction with an object in a RWR group by the owner
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testInteractionByGroupOwnerRWR() throws Exception {
        EventContext ec = newUserAndGroup("rwr---");
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        long id = d.getId().getValue();
        Permissions perms = d.getDetails().getPermissions();
        // make sure owner can do everything
        assertTrue(perms.canEdit());
        assertTrue(perms.canAnnotate());
        assertTrue(perms.canDelete());
        assertTrue(perms.canLink());

        // create image link

        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        DatasetImageLink l = new DatasetImageLinkI();
        l.link(new DatasetI(id, false), img);
        l = (DatasetImageLink) iUpdate.saveAndReturnObject(l);

        // Create annotation
        CommentAnnotation annotation = new CommentAnnotationI();
        annotation.setTextValue(omero.rtypes.rstring("comment"));
        Annotation ann = (Annotation) iUpdate.saveAndReturnObject(annotation);
        DatasetAnnotationLink dl = new DatasetAnnotationLinkI();
        dl.link(new DatasetI(id, false), ann);
        dl = (DatasetAnnotationLink) iUpdate.saveAndReturnObject(dl);

        disconnect();
        // Now a new member to the group.
        newUserInGroup(ec);
        makeGroupOwner();
        String sql = "select i from Dataset as i ";
        sql += "where i.id = :id";
        ParametersI param = new ParametersI();
        param.addId(id);
        List<IObject> datasets = iQuery.findAllByQuery(sql, param);
        assertEquals(datasets.size(), 1);
        d = (Dataset) datasets.get(0);

        perms = d.getDetails().getPermissions();

        // Check what the group owner can do
        assertTrue(perms.canEdit());
        assertTrue(perms.canAnnotate());
        assertTrue(perms.canDelete());
        assertTrue(perms.canLink());

        // Try to delete the link i.e. canLink
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                DatasetImageLink.class.getSimpleName(),
                Collections.singletonList(l.getId().getValue()));
        callback(true, client, dc);

        // Try to delete the annotation link i.e. canDelete
        dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                DatasetAnnotationLink.class.getSimpleName(),
                Collections.singletonList(dl.getId().getValue()));
        callback(true, client, dc);

        // Try to delete the annotation i.e. canDelete
        dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Annotation.class.getSimpleName(),
                Collections.singletonList(ann.getId().getValue()));
        callback(true, client, dc);

        // Try to link an image i.e. canLink
        img = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage());
        l = new DatasetImageLinkI();
        l.link(new DatasetI(d.getId().getValue(), false), img);
        iUpdate.saveAndReturnObject(l);

        // Try to create the annotation i.e. canAnnotate
        annotation = new CommentAnnotationI();
        annotation.setTextValue(omero.rtypes.rstring("comment"));
        ann = (Annotation) iUpdate.saveAndReturnObject(annotation);
        dl = new DatasetAnnotationLinkI();
        dl.link(new DatasetI(id, false), ann);
        dl = (DatasetAnnotationLink) iUpdate.saveAndReturnObject(dl);

        // Try to edit i.e. canEdit
        d.setName(rstring("newNAme"));
        iUpdate.saveAndReturnObject(d);
    }

    /**
     * Test the interaction with an object in a RWR group by the admin
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testInteractionByAdminRWR() throws Exception {
        EventContext ec = newUserAndGroup("rwr---");
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        Permissions perms = d.getDetails().getPermissions();
        long id = d.getId().getValue();

        assertTrue(perms.canEdit());
        assertTrue(perms.canAnnotate());
        assertTrue(perms.canDelete());
        assertTrue(perms.canLink());

        // create image link
        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        DatasetImageLink l = new DatasetImageLinkI();
        l.link(new DatasetI(id, false), img);
        l = (DatasetImageLink) iUpdate.saveAndReturnObject(l);

        // Create annotation
        CommentAnnotation annotation = new CommentAnnotationI();
        annotation.setTextValue(omero.rtypes.rstring("comment"));
        Annotation ann = (Annotation) iUpdate.saveAndReturnObject(annotation);
        DatasetAnnotationLink dl = new DatasetAnnotationLinkI();
        dl.link(new DatasetI(id, false), ann);
        dl = (DatasetAnnotationLink) iUpdate.saveAndReturnObject(dl);

        disconnect();
        // Now a new member to the group.
        logRootIntoGroup(ec);
        String sql = "select i from Dataset as i ";
        sql += "where i.id = :id";
        ParametersI param = new ParametersI();
        param.addId(id);
        List<IObject> datasets = iQuery.findAllByQuery(sql, param);
        assertEquals(datasets.size(), 1);
        d = (Dataset) datasets.get(0);
        id = d.getId().getValue();

        perms = d.getDetails().getPermissions();

        // Check what the group owner can do
        assertTrue(perms.canEdit());
        assertTrue(perms.canAnnotate());
        assertTrue(perms.canDelete());
        assertTrue(perms.canLink());

        // Try to delete the link i.e. canLink
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                DatasetImageLink.class.getSimpleName(),
                Collections.singletonList(l.getId().getValue()));
        callback(true, client, dc);

        // Try to delete the annotation link i.e. canDelete
        dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                DatasetAnnotationLink.class.getSimpleName(),
                Collections.singletonList(dl.getId().getValue()));
        callback(true, client, dc);

        // Try to delete the annotation i.e. canDelete
        dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Annotation.class.getSimpleName(),
                Collections.singletonList(ann.getId().getValue()));
        callback(true, client, dc);

        // Try to link an image i.e. canLink
        img = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage());
        l = new DatasetImageLinkI();
        l.link(new DatasetI(id, false), img);
        iUpdate.saveAndReturnObject(l);

        // Try to create the annotation i.e. canAnnotate
        annotation = new CommentAnnotationI();
        annotation.setTextValue(omero.rtypes.rstring("comment"));
        ann = (Annotation) iUpdate.saveAndReturnObject(annotation);
        dl = new DatasetAnnotationLinkI();
        dl.link(new DatasetI(id, false), ann);
        dl = (DatasetAnnotationLink) iUpdate.saveAndReturnObject(dl);

        // Try to edit i.e. canEdit
        d.setName(rstring("newNAme"));
        iUpdate.saveAndReturnObject(d);
    }

    // Group RWRA--
    /**
     * Test the interaction with an object in a RWRA group by a member
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testInteractionByMemberRWRA() throws Exception {
        EventContext ec = newUserAndGroup("rwra--");
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        Permissions perms = d.getDetails().getPermissions();
        long id = d.getId().getValue();

        assertTrue(perms.canAnnotate());
        assertTrue(perms.canEdit());
        assertTrue(perms.canLink());
        assertTrue(perms.canAnnotate());

        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        // Create link
        DatasetImageLink l = new DatasetImageLinkI();
        l.link(new DatasetI(id, false), img);
        l = (DatasetImageLink) iUpdate.saveAndReturnObject(l);

        // Create annotation
        CommentAnnotation annotation = new CommentAnnotationI();
        annotation.setTextValue(omero.rtypes.rstring("comment"));
        Annotation ann = (Annotation) iUpdate.saveAndReturnObject(annotation);
        DatasetAnnotationLink dl = new DatasetAnnotationLinkI();
        dl.link(new DatasetI(id, false), ann);
        dl = (DatasetAnnotationLink) iUpdate.saveAndReturnObject(dl);

        disconnect();

        // Now a new member to the group.
        newUserInGroup(ec);
        String sql = "select i from Dataset as i ";
        sql += "where i.id = :id";
        ParametersI param = new ParametersI();
        param.addId(id);
        List<IObject> datasets = iQuery.findAllByQuery(sql, param);
        assertEquals(datasets.size(), 1);

        // Just a member should be able to neither (for the moment)
        // Reload the perms (from the object that the member loaded)
        // and check status.
        d = (Dataset) datasets.get(0);

        perms = d.getDetails().getPermissions();

        assertFalse(perms.canEdit());
        assertTrue(perms.canAnnotate());
        assertFalse(perms.canDelete());
        assertFalse(perms.canLink());

        // Create a link canLink
        // Try to delete the link i.e. canDelete
        try {
            Delete2 dc = new Delete2();
            dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                    DatasetImageLink.class.getSimpleName(),
                    Collections.singletonList(l.getId().getValue()));
            callback(false, client, dc);
        } catch (Exception e) {

            fail("Member should not be allowed to delete "
                    + "an image/dataset link.");
        }

        try {
            Delete2 dc = new Delete2();
            dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                    DatasetAnnotationLink.class.getSimpleName(),
                    Collections.singletonList(dl.getId().getValue()));
            callback(false, client, dc);
        } catch (Exception e) {
            fail("Member should not be allowed to delete "
                    + "an image/dataset link.");
        }

        // Try to delete the annotation i.e. canDelete
        try {
            Delete2 dc = new Delete2();
            dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                    Annotation.class.getSimpleName(),
                    Collections.singletonList(ann.getId().getValue()));
            callback(false, client, dc);
        } catch (Exception e) {
            fail("Member should not be allowed to delete " + "the annotation.");
        }

        // Try to link an image i.e. canLink
        try {
            img = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage());
            l = new DatasetImageLinkI();
            l.link(new DatasetI(d.getId().getValue(), false), img);
            iUpdate.saveAndReturnObject(l);
            fail("Member should not be allowed to create an image/dataset link.");
        } catch (Exception e) {
        }

        // Try to create the annotation i.e. canAnnotate
        annotation = new CommentAnnotationI();
        annotation.setTextValue(omero.rtypes.rstring("comment"));
        ann = (Annotation) iUpdate.saveAndReturnObject(annotation);
        dl = new DatasetAnnotationLinkI();
        dl.link(new DatasetI(id, false), ann);
        dl = (DatasetAnnotationLink) iUpdate.saveAndReturnObject(dl);

        // Try to edit i.e. canEdit
        try {
            d.setName(rstring("newNAme"));
            iUpdate.saveAndReturnObject(d);
            fail("Member should not be allowed to edit a dataset.");
        } catch (Exception e) {
        }
    }

    /**
     * Test the interaction with an object in a RWRA group by the owner
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testInteractionByGroupOwnerRWRA() throws Exception {
        EventContext ec = newUserAndGroup("rwra--");
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        Permissions perms = d.getDetails().getPermissions();
        // make sure owner can do everything
        assertTrue(perms.canEdit());
        assertTrue(perms.canAnnotate());
        assertTrue(perms.canDelete());
        assertTrue(perms.canLink());

        long id = d.getId().getValue();

        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        // Create link
        DatasetImageLink l = new DatasetImageLinkI();
        l.link(new DatasetI(id, false), img);
        l = (DatasetImageLink) iUpdate.saveAndReturnObject(l);

        // Create annotation
        CommentAnnotation annotation = new CommentAnnotationI();
        annotation.setTextValue(omero.rtypes.rstring("comment"));
        Annotation ann = (Annotation) iUpdate.saveAndReturnObject(annotation);
        DatasetAnnotationLink dl = new DatasetAnnotationLinkI();
        dl.link(new DatasetI(id, false), ann);
        dl = (DatasetAnnotationLink) iUpdate.saveAndReturnObject(dl);

        disconnect();
        // Now a new member to the group.
        newUserInGroup(ec);
        makeGroupOwner();
        String sql = "select i from Dataset as i ";
        sql += "where i.id = :id";
        ParametersI param = new ParametersI();
        param.addId(id);
        List<IObject> datasets = iQuery.findAllByQuery(sql, param);
        assertEquals(datasets.size(), 1);
        d = (Dataset) datasets.get(0);

        perms = d.getDetails().getPermissions();

        // Check what the group owner can do
        assertTrue(perms.canEdit());
        assertTrue(perms.canAnnotate());
        assertTrue(perms.canDelete());
        assertTrue(perms.canLink());

        // Try to delete the link i.e. canLink
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                DatasetImageLink.class.getSimpleName(),
                Collections.singletonList(l.getId().getValue()));
        callback(true, client, dc);

        // Try to delete the annotation link i.e. canDelete
        dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                DatasetAnnotationLink.class.getSimpleName(),
                Collections.singletonList(dl.getId().getValue()));
        callback(true, client, dc);

        // Try to delete the annotation i.e. canDelete
        dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Annotation.class.getSimpleName(),
                Collections.singletonList(ann.getId().getValue()));
        callback(true, client, dc);

        // Try to link an image i.e. canLink
        img = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage());
        l = new DatasetImageLinkI();
        l.link(new DatasetI(d.getId().getValue(), false), img);
        iUpdate.saveAndReturnObject(l);

        // Try to create the annotation i.e. canAnnotate
        annotation = new CommentAnnotationI();
        annotation.setTextValue(omero.rtypes.rstring("comment"));
        ann = (Annotation) iUpdate.saveAndReturnObject(annotation);
        dl = new DatasetAnnotationLinkI();
        dl.link(new DatasetI(id, false), ann);
        dl = (DatasetAnnotationLink) iUpdate.saveAndReturnObject(dl);

        // Try to edit i.e. canEdit
        d.setName(rstring("newNAme"));
        iUpdate.saveAndReturnObject(d);
    }

    /**
     * Test the interaction with an object in a RWRA group by the admin
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testInteractionByAdminRWRA() throws Exception {
        EventContext ec = newUserAndGroup("rwr---");
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());

        long id = d.getId().getValue();

        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        // Create link
        DatasetImageLink l = new DatasetImageLinkI();
        l.link(new DatasetI(d.getId().getValue(), false), img);
        l = (DatasetImageLink) iUpdate.saveAndReturnObject(l);

        // Create annotation
        CommentAnnotation annotation = new CommentAnnotationI();
        annotation.setTextValue(omero.rtypes.rstring("comment"));
        Annotation ann = (Annotation) iUpdate.saveAndReturnObject(annotation);
        DatasetAnnotationLink dl = new DatasetAnnotationLinkI();
        dl.link(new DatasetI(id, false), ann);
        dl = (DatasetAnnotationLink) iUpdate.saveAndReturnObject(dl);

        Permissions perms = d.getDetails().getPermissions();
        assertTrue(perms.canEdit());
        assertTrue(perms.canAnnotate());
        assertTrue(perms.canDelete());
        assertTrue(perms.canLink());

        disconnect();
        // Now a new member to the group.
        logRootIntoGroup(ec);
        String sql = "select i from Dataset as i ";
        sql += "where i.id = :id";
        ParametersI param = new ParametersI();
        param.addId(id);
        List<IObject> datasets = iQuery.findAllByQuery(sql, param);
        assertEquals(datasets.size(), 1);

        d = (Dataset) datasets.get(0);

        perms = d.getDetails().getPermissions();
        // Check what the group owner can do
        assertTrue(perms.canEdit());
        assertTrue(perms.canAnnotate());
        assertTrue(perms.canDelete());
        assertTrue(perms.canLink());

        // Try to delete the link i.e. canLink
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                DatasetImageLink.class.getSimpleName(),
                Collections.singletonList(l.getId().getValue()));
        callback(true, client, dc);

        // Try to delete the annotation link i.e. canDelete
        dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                DatasetAnnotationLink.class.getSimpleName(),
                Collections.singletonList(dl.getId().getValue()));
        callback(true, client, dc);

        // Try to delete the annotation i.e. canDelete
        dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Annotation.class.getSimpleName(),
                Collections.singletonList(ann.getId().getValue()));
        callback(true, client, dc);

        // Try to link an image i.e. canLink
        img = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage());
        l = new DatasetImageLinkI();
        l.link(new DatasetI(id, false), img);
        iUpdate.saveAndReturnObject(l);

        // Try to create the annotation i.e. canAnnotate
        annotation = new CommentAnnotationI();
        annotation.setTextValue(omero.rtypes.rstring("comment"));
        ann = (Annotation) iUpdate.saveAndReturnObject(annotation);
        dl = new DatasetAnnotationLinkI();
        dl.link(new DatasetI(id, false), ann);
        dl = (DatasetAnnotationLink) iUpdate.saveAndReturnObject(dl);

        // Try to edit i.e. canEdit
        d.setName(rstring("newNAme"));
        iUpdate.saveAndReturnObject(d);
    }

    // Group RWRW--
    /**
     * Test the interaction with an object in a RWRW group by a member
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testInteractionByMemberRWRW() throws Exception {
        EventContext ec = newUserAndGroup("rwrw--");
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        Permissions perms = d.getDetails().getPermissions();
        long id = d.getId().getValue();
        assertTrue(perms.canEdit());
        assertTrue(perms.canAnnotate());
        assertTrue(perms.canDelete());
        assertTrue(perms.canLink());

        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        // Create link
        DatasetImageLink l = new DatasetImageLinkI();
        l.link(new DatasetI(id, false), img);
        l = (DatasetImageLink) iUpdate.saveAndReturnObject(l);

        // Create annotation
        CommentAnnotation annotation = new CommentAnnotationI();
        annotation.setTextValue(omero.rtypes.rstring("comment"));
        Annotation ann = (Annotation) iUpdate.saveAndReturnObject(annotation);
        DatasetAnnotationLink dl = new DatasetAnnotationLinkI();
        dl.link(new DatasetI(id, false), ann);
        dl = (DatasetAnnotationLink) iUpdate.saveAndReturnObject(dl);

        disconnect();
        // Now a new member to the group.
        newUserInGroup(ec);
        String sql = "select i from Dataset as i ";
        sql += "where i.id = :id";
        ParametersI param = new ParametersI();
        param.addId(id);
        List<IObject> datasets = iQuery.findAllByQuery(sql, param);
        assertEquals(datasets.size(), 1);
        d = (Dataset) datasets.get(0);

        perms = d.getDetails().getPermissions();

        // Check what the group owner can do
        assertTrue(perms.canEdit());
        assertTrue(perms.canAnnotate());
        assertTrue(perms.canDelete());
        assertTrue(perms.canLink());

        // Try to delete the link i.e. canLink
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                DatasetImageLink.class.getSimpleName(),
                Collections.singletonList(l.getId().getValue()));
        callback(true, client, dc);

        // Try to delete the annotation link i.e. canDelete
        dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                DatasetAnnotationLink.class.getSimpleName(),
                Collections.singletonList(dl.getId().getValue()));
        callback(true, client, dc);

        // Try to delete the annotation i.e. canDelete
        dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Annotation.class.getSimpleName(),
                Collections.singletonList(ann.getId().getValue()));
        callback(true, client, dc);
        // Try to delete the dataset i.e. canDelete

        // Try to link an image i.e. canLink
        img = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage());
        l = new DatasetImageLinkI();
        l.link(new DatasetI(id, false), img);
        iUpdate.saveAndReturnObject(l); // The dataset's been deleted??

        // Try to create the annotation i.e. canAnnotate
        annotation = new CommentAnnotationI();
        annotation.setTextValue(omero.rtypes.rstring("comment"));
        ann = (Annotation) iUpdate.saveAndReturnObject(annotation);
        dl = new DatasetAnnotationLinkI();
        dl.link(new DatasetI(id, false), ann);
        dl = (DatasetAnnotationLink) iUpdate.saveAndReturnObject(dl);

        // Try to edit i.e. canEdit
        d.setName(rstring("newNAme"));
        iUpdate.saveAndReturnObject(d);
        dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Dataset.class.getSimpleName(), Collections.singletonList(id));
        callback(true, client, dc);
    }

    /**
     * Test the interaction with an object in a RW group by the owner
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testInteractionByGroupOwnerRWRW() throws Exception {
        EventContext ec = newUserAndGroup("rwrw--");
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        Permissions perms = d.getDetails().getPermissions();
        long id = d.getId().getValue();

        // make sure data owner can do everything
        assertTrue(perms.canEdit());
        assertTrue(perms.canAnnotate());
        assertTrue(perms.canDelete());
        assertTrue(perms.canLink());

        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        // Create link
        DatasetImageLink l = new DatasetImageLinkI();
        l.link(new DatasetI(id, false), img);
        l = (DatasetImageLink) iUpdate.saveAndReturnObject(l);

        // Create annotation
        CommentAnnotation annotation = new CommentAnnotationI();
        annotation.setTextValue(omero.rtypes.rstring("comment"));
        Annotation ann = (Annotation) iUpdate.saveAndReturnObject(annotation);
        DatasetAnnotationLink dl = new DatasetAnnotationLinkI();
        dl.link(new DatasetI(id, false), ann);
        dl = (DatasetAnnotationLink) iUpdate.saveAndReturnObject(dl);

        disconnect();
        // Now a new member to the group.
        newUserInGroup(ec);
        makeGroupOwner();
        String sql = "select i from Dataset as i ";
        sql += "where i.id = :id";
        ParametersI param = new ParametersI();
        param.addId(id);
        List<IObject> datasets = iQuery.findAllByQuery(sql, param);
        assertEquals(datasets.size(), 1);
        d = (Dataset) datasets.get(0);

        perms = d.getDetails().getPermissions();

        // Check what the group owner can do
        assertTrue(perms.canEdit());
        assertTrue(perms.canAnnotate());
        assertTrue(perms.canDelete());
        assertTrue(perms.canLink());

        // Try to delete the link i.e. canLink
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                DatasetImageLink.class.getSimpleName(),
                Collections.singletonList(l.getId().getValue()));
        callback(true, client, dc);

        // Try to delete the annotation link i.e. canDelete
        dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                DatasetAnnotationLink.class.getSimpleName(),
                Collections.singletonList(dl.getId().getValue()));
        callback(true, client, dc);

        // Try to delete the annotation i.e. canDelete
        dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Annotation.class.getSimpleName(),
                Collections.singletonList(ann.getId().getValue()));
        callback(true, client, dc);

        // Try to link an image i.e. canLink
        img = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage());
        l = new DatasetImageLinkI();
        l.link(new DatasetI(id, false), img);
        iUpdate.saveAndReturnObject(l);

        // Try to create the annotation i.e. canAnnotate
        annotation = new CommentAnnotationI();
        annotation.setTextValue(omero.rtypes.rstring("comment"));
        ann = (Annotation) iUpdate.saveAndReturnObject(annotation);
        dl = new DatasetAnnotationLinkI();
        dl.link(new DatasetI(id, false), ann);
        dl = (DatasetAnnotationLink) iUpdate.saveAndReturnObject(dl);

        // Try to edit i.e. canEdit
        d.setName(rstring("newNAme"));
        iUpdate.saveAndReturnObject(d);
    }

    /**
     * Test the interaction with an object in a RWRW group by the admin
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testInteractionByAdminRWRW() throws Exception {
        EventContext ec = newUserAndGroup("rwrw--");
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        Permissions perms = d.getDetails().getPermissions();
        long id = d.getId().getValue();

        // Check what the group owner can do
        assertTrue(perms.canEdit());
        assertTrue(perms.canAnnotate());
        assertTrue(perms.canDelete());
        assertTrue(perms.canLink());

        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        // Create link
        DatasetImageLink l = new DatasetImageLinkI();
        l.link(new DatasetI(d.getId().getValue(), false), img);
        l = (DatasetImageLink) iUpdate.saveAndReturnObject(l);

        // Create annotation
        CommentAnnotation annotation = new CommentAnnotationI();
        annotation.setTextValue(omero.rtypes.rstring("comment"));
        Annotation ann = (Annotation) iUpdate.saveAndReturnObject(annotation);
        DatasetAnnotationLink dl = new DatasetAnnotationLinkI();
        dl.link(new DatasetI(id, false), ann);
        dl = (DatasetAnnotationLink) iUpdate.saveAndReturnObject(dl);

        disconnect();
        // Now a new member to the group.
        logRootIntoGroup(ec);

        String sql = "select i from Dataset as i ";
        sql += "where i.id = :id";
        ParametersI param = new ParametersI();
        param.addId(id);
        List<IObject> datasets = iQuery.findAllByQuery(sql, param);
        assertEquals(datasets.size(), 1);
        d = (Dataset) datasets.get(0);

        // Check what the group owner can do
        assertTrue(perms.canEdit());
        assertTrue(perms.canAnnotate());
        assertTrue(perms.canDelete());
        assertTrue(perms.canLink());

        // Try to delete the link i.e. canLink
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                DatasetImageLink.class.getSimpleName(),
                Collections.singletonList(l.getId().getValue()));
        callback(true, client, dc);

        // Try to delete the annotation link i.e. canDelete
        dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                DatasetAnnotationLink.class.getSimpleName(),
                Collections.singletonList(dl.getId().getValue()));
        callback(true, client, dc);

        // Try to delete the annotation i.e. canDelete
        dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Annotation.class.getSimpleName(),
                Collections.singletonList(ann.getId().getValue()));
        callback(true, client, dc);

        // Try to link an image i.e. canLink
        img = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage());
        l = new DatasetImageLinkI();
        l.link(new DatasetI(id, false), img);
        iUpdate.saveAndReturnObject(l);

        // Try to create the annotation i.e. canAnnotate
        annotation = new CommentAnnotationI();
        annotation.setTextValue(omero.rtypes.rstring("comment"));
        ann = (Annotation) iUpdate.saveAndReturnObject(annotation);
        dl = new DatasetAnnotationLinkI();
        dl.link(new DatasetI(id, false), ann);
        dl = (DatasetAnnotationLink) iUpdate.saveAndReturnObject(dl);

        // Try to edit i.e. canEdit
        d.setName(rstring("newNAme"));
        iUpdate.saveAndReturnObject(d);
    }

}
