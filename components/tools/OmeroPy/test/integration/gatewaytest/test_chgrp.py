#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Copyright (C) 2012 University of Dundee & Open Microscopy Environment.
                      All Rights Reserved.
   Copyright 2013 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

   pytest fixtures used as defined in conftest.py:
   - gatewaywrapper

"""

import omero
from omero.rtypes import rstring
from omero.testlib import ITest
from omero.cmd import State, ERR, OK
from omero.callbacks import CmdCallbackI
from omero.gateway import BlitzGateway
from omero.cmd import Chgrp2
from future.utils import native_str

PRIVATE = 'rw----'
READONLY = 'rwr---'
COLLAB = 'rwrw--'


def doChange(gateway, obj_type, obj_ids, group_id, container_id=None,
             test_should_pass=True, return_complete=True):
    """
    Performs the change-group action, waits on completion and checks that the
    result is not an error.
    """
    prx = gateway.chgrpObjects(obj_type, obj_ids, group_id, container_id)

    #obj = {}
    #obj[obj_type] = obj_ids
    #chgrp = Chgrp2(targetObjects=obj, groupId=group_id)
    #prx = gateway.c.sf.submit(chgrp, {'omero.group': native_str(None)})

    if not return_complete:
        return prx

    cb = CmdCallbackI(gateway.c, prx)
    try:
        for i in range(10):
            cb.loop(20, 500)
            if prx.getResponse() is not None:
                break

        assert prx.getResponse() is not None

        prx.getStatus()
        rsp = prx.getResponse()

        if test_should_pass:
            assert not isinstance(rsp, ERR), \
                "Found ERR when test_should_pass==true: %s (%s) params=%s" \
                % (rsp.category, rsp.name, rsp.parameters)
            assert State.FAILURE not in prx.getStatus().flags
        else:
            assert not isinstance(rsp, OK), \
                "Found OK when test_should_pass==false: %s" % rsp
            assert State.FAILURE in prx.getStatus().flags
        return rsp
    finally:
        cb.close(True)


class TestChgrp(ITest):
    def testImageChgrp(self):
        """
        Create a new group with the User as member. Test move the Image to new
        group.
        """

        # One user in two groups
        client, exp = self.new_client_and_user()
        conn = BlitzGateway(client_obj=client)
        grp = self.new_group(experimenters=[exp], perms=COLLAB)
        gid = grp.id.val
        client.sf.getAdminService().getEventContext()  # Reset session

        # Import an image into the client context
        images = self.import_fake_file(name="gatewaytestChgrpImportedImage",
                                       client=client)
        image = images[0]
        image_id = image.id.val

        assert conn.getObject("Image", image_id) is not None

        # Do the Chgrp
        doChange(conn, "Image", [image_id], gid)
        # chgrp = Chgrp2(targetObjects={'Image': [image_id]}, groupId=gid)
        # self.do_submit(chgrp, client)

        # Image should no-longer be available in current group
        assert conn.getObject("Image", image_id) is None, \
            "Image should not be available in original group"

        # Switch to new group - confirm that image is there.
        conn.setGroupForSession(gid)
        img = conn.getObject("Image", image_id)
        assert img is not None, "Image should be available in new group"
        assert img.getDetails().getGroup().id == gid, \
            "Image group.id should match new group"

    def testDatasetChgrp(self):
        """
        Create a new group with the User as member. Test move the
        Dataset/Image to new group.
        """

        # One user in two groups
        client, exp = self.new_client_and_user()
        conn = BlitzGateway(client_obj=client)
        grp = self.new_group([exp])
        gid = grp.id.val
        conn.getAdminService().getEventContext()  # Reset session

        # Data Setup (Image in a Dataset)
        img = self.make_image(client=client)
        image_id = img.id.val
        dataset = self.make_dataset(name="chgrp-gatewaytest", client=client)
        dataset_id = dataset.id.val
        self.link(dataset, img, client=client)

        assert conn.getObject("Image", image_id) is not None

        # Do the Chgrp
        doChange(conn, "Dataset", [dataset_id], gid)

        # Dataset should no-longer be available in current group
        assert conn.getObject("Dataset", dataset_id) is None, \
            "Dataset should not be available in original group"

        # Switch to new group - confirm that Dataset, Image is there.
        conn.setGroupForSession(gid)
        ds = conn.getObject("Dataset", dataset_id)
        assert ds is not None, "Dataset should be available in new group"

        img = conn.getObject("Image", image_id)
        assert img is not None, "Image should be available in new group"
        assert img.getDetails().getGroup().id == gid, \
            "Image group.id should match new group"

    def testPDIChgrp(self):
        """
        Create a new group with the User as member. Test move the
        Project/Dataset/Image to new group.
        """
        # One user in two groups
        client, exp = self.new_client_and_user()
        conn = BlitzGateway(client_obj=client)
        grp = self.new_group([exp])
        gid = grp.id.val
        conn.getAdminService().getEventContext()  # Reset session

        # Data Setup (image in the P/D hierarchy)
        img = self.make_image(client=client)
        image_id = img.id.val
        project = self.make_project(name="chgrp-gatewaytest", client=client)
        dataset = self.make_dataset(name="chgrp-gatewaytest", client=client)
        self.link(dataset, img, client=client)
        self.link(project, dataset, client=client)

        assert conn.getObject("Image", image_id) is not None

        # Do the Chgrp
        doChange(conn, "Project", [project.id.val], gid)

        # Image should no-longer be available in current group
        assert conn.getObject("Image", image_id) is None, \
            "Image should not be available in original group"

        # Switch to new group - confirm that Project, Dataset, Image is there.
        conn.setGroupForSession(gid)
        prj = conn.getObject("Project", project.id.val)
        assert prj is not None, "Project should be available in new group"

        ds = conn.getObject("Dataset", dataset.id.val)
        assert ds is not None, "Dataset should be available in new group"

        img = conn.getObject("Image", image_id)
        assert img is not None, "Image should be available in new group"
        assert img.getDetails().getGroup().id == gid, \
            "Image group.id should match new group"

        # Change it all back
        doChange(conn, "Project", [project.id.val], grp.id.val)

        # Image should again be available in current group
        assert conn.getObject("Image", image_id) \
            is not None, "Image should be available in original group"

    def testTwoDatasetsChgrpToProject(self):
        """
        Create a new group with the User as member. Image has 2 Dataset
        Parents. Test move one Dataset to new group. Image does not move.
        Move 2nd Dataset - Image moves.
        """
        # One user in two groups
        client, exp = self.new_client_and_user()
        conn = BlitzGateway(client_obj=client)
        grp = self.new_group([exp])
        gid = grp.id.val
        conn.getAdminService().getEventContext()  # Reset session

        # Data Setup (Image in 2 Datasets)
        img = self.make_image(client=client)
        image_id = img.id.val
        dataset1 = self.make_dataset(name="chgrp-gatewaytest1", client=client)
        dataset1_id = dataset1.id.val
        orig_gid = dataset1.details.group.id.val
        self.link(dataset1, img, client=client)
        dataset2 = self.make_dataset(name="chgrp-gatewaytest2", client=client)
        dataset2_id = dataset2.id.val
        self.link(dataset2, img, client=client)

        assert conn.getObject("Dataset", dataset1_id) is not None

        # create Project in destination group
        conn.setGroupForSession(gid)
        p = omero.model.ProjectI()
        p.name = rstring("testTwoDatasetsChgrpToProject")
        p = conn.getUpdateService().saveAndReturnObject(p)
        assert p.details.group.id.val == gid, \
            "Project should be created in target group"
        conn.setGroupForSession(orig_gid)   # switch back

        # Do the Chgrp with one of the parents
        doChange(conn, "Dataset", [dataset1_id], gid)

        # Dataset should no-longer be available in current group
        assert conn.getObject("Dataset", dataset1_id) is None, \
            "Dataset should not be available in original group"
        assert conn.getObject("Dataset", dataset2_id) \
            is not None, "Other Dataset should still be in original group"
        # But Image should
        img = conn.getObject("Image", image_id)
        assert img is not None, \
            "Image should still be available in original group"

        # Do the Chgrp with the OTHER parent
        # switch BEFORE doChange to allow Project link Save
        conn.setGroupForSession(gid)
        doChange(conn, "Dataset", [dataset2_id], gid,
                 container_id=p.id.val)

        # Confirm that Dataset AND Image is now in new group
        ds = conn.getObject("Dataset", dataset2_id)
        projects = list(ds.listParents())
        assert len(projects) == 1, \
            "Dataset should have one parent Project in new group"
        assert projects[0].getId() == p.id.val, \
            "Check Dataset parent is Project created above"
        assert ds is not None, "Dataset should now be available in new group"
        assert ds.getDetails().getGroup().id == gid, \
            "Dataset group.id should match new group"

        img = conn.getObject("Image", image_id)
        assert img is not None, "Image should now be available in new group"
        assert img.getDetails().getGroup().id == gid, \
            "Image group.id should match new group"

    def testMultiDatasetDoAll(self):
        """
        Need to enable chgrp independently of EventContext group being the
        destination group.
        Other tests that do not set omero.group require this for DoAll Save to
        work.
        """
        # One user in two groups
        client, exp = self.new_client_and_user()
        conn = BlitzGateway(client_obj=client)
        grp = self.new_group([exp])
        gid = grp.id.val
        conn.getAdminService().getEventContext()  # Reset session

        update = conn.getUpdateService()

        new_ds = omero.model.DatasetI()
        new_ds.name = rstring("testMultiDatasetDoAll")
        new_ds = update.saveAndReturnObject(new_ds)

        new_ds2 = omero.model.DatasetI()
        new_ds2.name = rstring("testMultiDatasetDoAll2")
        new_ds2 = update.saveAndReturnObject(new_ds2)

        # create Project in new group
        conn.SERVICE_OPTS.setOmeroGroup(gid)
        p = omero.model.ProjectI()
        p.name = rstring("testMultiChgrp")
        p = conn.getUpdateService().saveAndReturnObject(
            p, conn.SERVICE_OPTS)
        assert p.details.group.id.val == gid, \
            "Project should be created in target group"

        # Chgrp
        dsIds = [new_ds.id.val, new_ds2.id.val]
        doChange(conn, "Dataset", dsIds, gid,
                 container_id=p.id.val)

        # Check all objects in destination group
        # we can get objects from either group...
        conn.SERVICE_OPTS.setOmeroGroup(-1)
        p = conn.getObject("Project", p.id.val)
        datasets = list(p.listChildren())
        assert len(datasets) == 2, "Project should have 2 new Datasets"
        for d in datasets:
            assert d.details.group.id.val == gid, \
                "Dataset should be in new group"
            assert d.getId() in dsIds, "Checking Datasets by ID"
