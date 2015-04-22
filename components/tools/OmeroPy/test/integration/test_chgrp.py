#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2012-2014 University of Dundee & Open Microscopy Environment.
# All rights reserved. Use is subject to license terms supplied in LICENSE.txt
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

"""
   Integration test for moving objects between groups.

"""

import omero
import omero.gateway
import library as lib
import pytest
from omero.cmd import Chgrp2
from omero.cmd.graphs import ChildOption
from omero.model import DatasetI, DatasetImageLinkI, ExperimenterGroupI, ImageI
from omero.model import FileAnnotationI, ImageAnnotationLinkI, TagAnnotationI
from omero.model import ProjectDatasetLinkI, ProjectI, PlateI, ScreenI
from omero.model import ExperimenterI
from omero.rtypes import rstring, unwrap
from omero.api import Save

PRIVATE = 'rw----'
READONLY = 'rwr---'
READANNOTATE = 'rwra--'
COLLAB = 'rwrw--'


class TestChgrp(lib.ITest):

    def testChgrpImportedImage(self):
        """
        Tests chgrp for an imported image, moving to a collaborative group
        """
        # One user in two groups
        client, exp = self.new_client_and_user()
        grp = self.new_group(experimenters=[exp], perms=COLLAB)
        gid = grp.id.val
        client.sf.getAdminService().getEventContext()  # Reset session

        # Import an image into the client context
        image = self.importSingleImage(
            name="testChgrpImportedImage", client=client)

        # Chgrp
        chgrp = Chgrp2(targetObjects={'Image': [image.id.val]}, groupId=gid)
        self.doSubmit(chgrp, client)

        # Change our context to new group...
        admin = client.sf.getAdminService()
        admin.setDefaultGroup(exp, ExperimenterGroupI(gid, False))
        self.set_context(client, gid)
        # ...check image
        img = client.sf.getQueryService().get("Image", image.id.val)
        assert img.details.group.id.val == gid

    def testChgrpImage(self):
        """
        Tests chgrp for a dummny image object (no Pixels)
        """
        # One user in two groups
        client, exp = self.new_client_and_user()
        grp = self.new_group([exp])
        gid = grp.id.val
        client.sf.getAdminService().getEventContext()  # Reset session
        update = client.sf.getUpdateService()
        query = client.sf.getQueryService()
        admin = client.sf.getAdminService()
        first_gid = admin.getEventContext().groupId

        # Create a dataset in the 'first group'
        ds = DatasetI()
        ds.name = rstring("testChgrpImage_target")
        ds = update.saveAndReturnObject(ds)
        ds_id = ds.id.val

        # Change our context to new group and create image
        admin.setDefaultGroup(exp, ExperimenterGroupI(gid, False))
        self.set_context(client, gid)
        update = client.sf.getUpdateService()   # do we need to get this again?
        img = self.new_image()
        img = update.saveAndReturnObject(img)

        # Move image to new group
        chgrp = Chgrp2(
            targetObjects={'Image': [img.id.val]}, groupId=first_gid)

        # Link to Save
        link = DatasetImageLinkI()
        link.child = ImageI(img.id.val, False)
        link.parent = DatasetI(ds_id, False)
        save = Save()
        save.obj = link
        requests = [chgrp, save]        # we're going to chgrp THEN save DIlink

        # Change our context to original group...
        admin.setDefaultGroup(exp, ExperimenterGroupI(first_gid, False))
        self.set_context(client, first_gid)

        # We have to be in destination group for link Save to work
        self.doSubmit(requests, client)

        # ...check image
        img = client.sf.getQueryService().get("Image", img.id.val)
        assert img.details.group.id.val == first_gid
        # check Dataset
        query = "select link from DatasetImageLink link\
            where link.child.id=%s" % img.id.val
        l = client.sf.getQueryService().findByQuery(query, None)
        assert l is not None, "New DatasetImageLink on image not found"
        assert l.details.group.id.val == first_gid,\
            "Link Created in same group as Image target"

    def testChgrpPDI(self):
        """
        Tests chgrp for a Project, Dataset, Image hierarchy
        """
        # One user in two groups
        client, exp = self.new_client_and_user()
        grp = self.new_group([exp])
        gid = grp.id.val
        client.sf.getAdminService().getEventContext()  # Reset session
        update = client.sf.getUpdateService()

        # Data Setup (image in the P/D hierarchy)
        img = self.new_image()
        img = update.saveAndReturnObject(img)
        project = ProjectI()
        project.setName(rstring("chgrp-test"))
        project = update.saveAndReturnObject(project)
        dataset = DatasetI()
        dataset.setName(rstring("chgrp-test"))
        dataset = update.saveAndReturnObject(dataset)
        links = []
        link = DatasetImageLinkI()
        link.setChild(img)
        link.setParent(dataset)
        links.append(link)
        l = ProjectDatasetLinkI()
        l.setChild(dataset.proxy())
        l.setParent(project.proxy())
        links.append(l)
        update.saveAndReturnArray(links)

        # Move Project to new group
        chgrp = Chgrp2(
            targetObjects={'Project': [project.id.val]}, groupId=gid)
        self.doSubmit(chgrp, client)

        # Change our context to new group...
        admin = client.sf.getAdminService()
        admin.setDefaultGroup(exp, ExperimenterGroupI(gid, False))
        self.set_context(client, gid)
        # ...check image
        img = client.sf.getQueryService().get("Image", img.id.val)
        assert img.details.group.id.val == gid
        # check Project
        prj = client.sf.getQueryService().get("Project", project.id.val)
        assert prj.details.group.id.val == gid

    def testChgrpRdef7825(self):

        # One user in two groups
        owner, owner_obj = self.new_client_and_user(perms="rwrw--")
        admin = owner.sf.getAdminService()
        ec = admin.getEventContext()
        source_grp = admin.getGroup(ec.groupId)

        target_grp = self.new_group([owner])
        target_gid = target_grp.id.val

        ec = admin.getEventContext()  # Refresh

        # Add another user to the source group
        member = self.new_client(group=source_grp)

        # Create an image as the owner
        image = self.importSingleImage(name="testChgrpRdef7825", client=owner)

        # Render as both users
        owner_g = omero.gateway.BlitzGateway(client_obj=owner)
        member_g = omero.gateway.BlitzGateway(client_obj=member)

        def render(g):
            g.getObject("Image", image.id.val).getThumbnail()

        render(owner_g)
        render(member_g)

        # Now chgrp and try to delete
        chgrp = Chgrp2(
            targetObjects={'Image': [image.id.val]}, groupId=target_gid)
        self.doSubmit(chgrp, owner)

        # Shouldn't be necessary to change group, but we're gonna
        owner_g.SERVICE_OPTS.setOmeroGroup("-1")
        handle = owner_g.deleteObjects("/Image", [image.id.val])
        self.waitOnCmd(owner_g.c, handle)

    def testChgrpOneImageFilesetErr(self):
        """
        Simple example of the MIF chgrp bad case:
        A single fileset containing 2 images - we try to chgrp ONE image.
        Each sibling CANNOT be moved independently of the other.
        """
        # One user in two groups
        client, user = self.new_client_and_user(perms=PRIVATE)
        target_grp = self.new_group([user], perms=PRIVATE)
        target_gid = target_grp.id.val

        # 2 images sharing a fileset
        images = self.importMIF(2, client=client)

        # Now chgrp
        chgrp = Chgrp2(
            targetObjects={'Image': [images[0].id.val]}, groupId=target_gid)
        self.doSubmit(chgrp, client, test_should_pass=False)

    def testChgrpAllImagesFilesetOK(self):
        """
        Simple example of the MIF chgrp bad case:
        A single fileset containing 2 images
        can be moved to the same group together.
        """
        # One user in two groups
        client, user = self.new_client_and_user(perms=PRIVATE)
        target_grp = self.new_group([user], perms=PRIVATE)
        target_gid = target_grp.id.val

        images = self.importMIF(2, client=client)

        # chgrp should succeed
        ids = [images[0].id.val, images[1].id.val]
        chgrp = Chgrp2(targetObjects={'Image': ids}, groupId=target_gid)
        self.doSubmit(chgrp, client)

        # Check both Images moved
        queryService = client.sf.getQueryService()
        ctx = {'omero.group': '-1'}      # query across groups
        for i in images:
            image = queryService.get('Image', i.id.val, ctx)
            img_gid = image.details.group.id.val
            assert target_gid == img_gid,\
                "Image should be in group: %s, NOT %s" % (target_gid,  img_gid)

    def testChgrpAllImagesFilesetTwoCommandsErr(self):
        """
        Simple example of the MIF chgrp bad case with Chgrp2:
        A single fileset containing 2 images cannot be moved
        to the same group together using two commands
        See testChgrpAllImagesFilesetOK for the good.
        """
        # One user in two groups
        client, user = self.new_client_and_user(perms=PRIVATE)
        target_grp = self.new_group([user], perms=PRIVATE)
        target_gid = target_grp.id.val

        images = self.importMIF(2, client=client)

        # chgrp should succeed
        chgrp1 = Chgrp2(
            targetObjects={'Image': [images[0].id.val]}, groupId=target_gid)
        chgrp2 = Chgrp2(
            targetObjects={'Image': [images[1].id.val]}, groupId=target_gid)
        self.doSubmit([chgrp1, chgrp2], client, test_should_pass=False)

    def testChgrpOneDatasetFilesetErr(self):
        """
        Simple example of the MIF chgrp bad case:
        A single fileset containing 2 images is split among 2 datasets.
        We try to chgrp ONE Dataset.
        Each dataset CANNOT be moved independently of the other.
        """
        # One user in two groups
        client, user = self.new_client_and_user(perms=PRIVATE)
        target_grp = self.new_group([user], perms=PRIVATE)
        target_gid = target_grp.id.val

        update = client.sf.getUpdateService()
        datasets = self.createDatasets(
            2, "testChgrpOneDatasetFilesetErr", client=client)
        images = self.importMIF(2, client=client)
        for i in range(2):
            link = DatasetImageLinkI()
            link.setParent(datasets[i].proxy())
            link.setChild(images[i].proxy())
            link = update.saveAndReturnObject(link)

        # chgrp should succeed with the first Dataset only
        chgrp = Chgrp2(
            targetObjects={"Dataset": [datasets[0].id.val]},
            groupId=target_gid)
        self.doSubmit(chgrp, client)

        queryService = client.sf.getQueryService()

        # Check Images not moved
        for i in range(2):
            image = queryService.get('Image', images[i].id.val)
            assert target_gid != image.details.group.id.val,\
                "Image should not be in group: %s" % target_gid

        # Check second Dataset not moved
        dataset = queryService.get('Dataset', datasets[1].id.val)
        assert target_gid != dataset.details.group.id.val,\
            "Dataset should not be in group: %s" % target_gid

        ctx = {'omero.group': str(target_gid)}  # query in the target group

        # Check first Dataset moved
        dataset = queryService.get('Dataset', datasets[0].id.val, ctx)
        assert target_gid == dataset.details.group.id.val,\
            "Dataset should be in group: %s" % target_gid

    def testChgrpAllDatasetsFilesetOK(self):
        """
        Simple example of the MIF chgrp bad case:
        a single fileset containing 2 images is split among 2 datasets.
        Datasets can be moved to the same group together.
        """
        # One user in two groups
        client, user = self.new_client_and_user(perms=PRIVATE)
        target_grp = self.new_group([user], perms=PRIVATE)
        target_gid = target_grp.id.val

        update = client.sf.getUpdateService()
        datasets = self.createDatasets(
            2, "testChgrpAllDatasetsFilesetOK", client=client)
        images = self.importMIF(2, client=client)
        for i in range(2):
            link = DatasetImageLinkI()
            link.setParent(datasets[i].proxy())
            link.setChild(images[i].proxy())
            link = update.saveAndReturnObject(link)

        # Now chgrp, should succeed
        ids = [datasets[0].id.val, datasets[1].id.val]
        chgrp = Chgrp2(targetObjects={"Dataset": ids}, groupId=target_gid)
        self.doSubmit(chgrp, client)

        # Check both Datasets and Images moved
        queryService = client.sf.getQueryService()
        ctx = {'omero.group': str(target_gid)}  # query in the target group
        for i in range(2):
            dataset = queryService.get('Dataset', datasets[i].id.val, ctx)
            image = queryService.get('Image', images[i].id.val, ctx)
            assert target_gid == dataset.details.group.id.val,\
                "Dataset should be in group: %s" % target_gid
            assert target_gid == image.details.group.id.val,\
                "Image should be in group: %s" % target_gid

    def testChgrpOneDatasetFilesetOK(self):
        """
        Simple example of the MIF chgrp good case:
        a single fileset containing 2 images in one dataset.
        The dataset can be moved.
        """
        # One user in two groups
        client, user = self.new_client_and_user(perms=PRIVATE)
        target_grp = self.new_group([user], perms=PRIVATE)
        target_gid = target_grp.id.val

        update = client.sf.getUpdateService()
        ds = DatasetI()
        ds.name = rstring("testChgrpOneDatasetFilesetOK")
        ds = update.saveAndReturnObject(ds)
        images = self.importMIF(2, client=client)
        for i in range(2):
            link = DatasetImageLinkI()
            link.setParent(ds.proxy())
            link.setChild(images[i].proxy())
            link = update.saveAndReturnObject(link)

        # Now chgrp, should succeed
        chgrp = Chgrp2(
            targetObjects={"Dataset": [ds.id.val]}, groupId=target_gid)
        self.doSubmit(chgrp, client)

        # Check Dataset and both Images moved
        queryService = client.sf.getQueryService()
        ctx = {'omero.group': '-1'}  # query across groups
        dataset = queryService.get('Dataset', ds.id.val, ctx)
        assert target_gid == dataset.details.group.id.val,\
            "Dataset should be in group: %s" % target_gid
        for i in range(2):
            image = queryService.get('Image', images[i].id.val, ctx)
            img_gid = image.details.group.id.val
            assert target_gid == img_gid,\
                "Image should be in group: %s, NOT %s" % (target_gid,  img_gid)

    def testChgrpImagesTwoFilesetsErr(self):
        """
        If we try to 'split' 2 Filesets, both should be returned
        by the chgrp error
        """
        # One user in two groups
        client, user = self.new_client_and_user(perms=PRIVATE)
        target_grp = self.new_group([user], perms=PRIVATE)
        target_gid = target_grp.id.val

        imagesFsOne = self.importMIF(2, client=client)
        imagesFsTwo = self.importMIF(2, client=client)

        # chgrp should fail...
        ids = [imagesFsOne[0].id.val, imagesFsTwo[0].id.val]
        chgrp = Chgrp2(targetObjects={"Image": ids}, groupId=target_gid)
        self.doSubmit(chgrp, client, test_should_pass=False)

    def testChgrpDatasetTwoFilesetsErr(self):
        """
        If we try to 'split' 2 Filesets, both should be returned
        by the chgrp error
        """
        # One user in two groups
        client, user = self.new_client_and_user(perms=PRIVATE)
        target_grp = self.new_group([user], perms=PRIVATE)
        target_gid = target_grp.id.val

        imagesFsOne = self.importMIF(2, client=client)
        imagesFsTwo = self.importMIF(2, client=client)

        update = client.sf.getUpdateService()
        ds = DatasetI()
        ds.name = rstring("testChgrpDatasetTwoFilesetsErr")
        ds = update.saveAndReturnObject(ds)
        self.importMIF(2, client=client)
        for i in (imagesFsOne, imagesFsTwo):
            link = DatasetImageLinkI()
            link.setParent(ds.proxy())
            link.setChild(i[0].proxy())
            link = update.saveAndReturnObject(link)

        # chgrp should succeed with the Dataset only
        chgrp = Chgrp2(
            targetObjects={"Dataset": [ds.id.val]}, groupId=target_gid)
        self.doSubmit(chgrp, client)

        queryService = client.sf.getQueryService()

        # Check Images not moved
        for i in (imagesFsOne[0], imagesFsTwo[0]):
            image = queryService.get('Image', i.id.val)
            assert target_gid != image.details.group.id.val,\
                "Image should not be in group: %s" % target_gid

        ctx = {'omero.group': str(target_gid)}  # query in the target group

        # Check Dataset moved
        dataset = queryService.get('Dataset', ds.id.val, ctx)
        assert target_gid == dataset.details.group.id.val,\
            "Dataset should be in group: %s" % target_gid

    def testChgrpDatasetCheckFsGroup(self):
        """
        Move a Dataset of MIF images into a new group,
        then check that the Fileset group is the same as the target group.
        From 'Security Violation'
        Bug https://github.com/openmicroscopy/openmicroscopy/pull/1139
        """
        # One user in two groups
        client, user = self.new_client_and_user(perms=PRIVATE)
        target_grp = self.new_group([user], perms=PRIVATE)
        target_gid = target_grp.id.val

        update = client.sf.getUpdateService()
        ds = DatasetI()
        ds.name = rstring("testChgrpDatasetCheckFsGroup")
        ds = update.saveAndReturnObject(ds)
        images = self.importMIF(2, client=client)
        for i in range(2):
            link = DatasetImageLinkI()
            link.setParent(ds.proxy())
            link.setChild(images[i].proxy())
            link = update.saveAndReturnObject(link)

        # Now chgrp, should succeed
        chgrp = Chgrp2(
            targetObjects={"Dataset": [ds.id.val]}, groupId=target_gid)
        self.doSubmit(chgrp, client)

        # Check the group of the fileset is in sync with image.
        ctx = {'omero.group': '-1'}
        qs = client.sf.getQueryService()
        image1 = qs.get("Image", images[0].id.val, ctx)
        fsId = image1.fileset.id.val
        image_gid = image1.details.group.id.val
        fileset_gid = qs.get("Fileset", fsId, ctx).details.group.id.val
        assert image_gid == fileset_gid,\
            "Image group: %s and Fileset group: %s don't match" %\
            (image_gid, fileset_gid)

    def testChgrpFilesetOK(self):
        """
        Move a Fileset of MIF images into a new group,
        then check that the Fileset group is the same as the target group.
        """
        # One user in two groups
        client, user = self.new_client_and_user(perms=PRIVATE)
        query = client.sf.getQueryService()
        target_grp = self.new_group([user], perms=PRIVATE)
        target_gid = target_grp.id.val

        images = self.importMIF(2, client=client)
        fsId = query.get("Image", images[0].id.val).fileset.id.val

        # Now chgrp, should succeed
        chgrp = Chgrp2(targetObjects={"Fileset": [fsId]}, groupId=target_gid)
        self.doSubmit(chgrp, client)

        # Check Fileset and both Images moved and
        # thus the Fileset is in sync with Images.
        ctx = {'omero.group': '-1'}  # query across groups
        fileset = query.get('Fileset', fsId, ctx)
        assert target_gid == fileset.details.group.id.val,\
            "Fileset should be in group: %s" % target_gid
        for i in range(2):
            image = query.get('Image', images[i].id.val, ctx)
            img_gid = image.details.group.id.val
            assert target_gid == img_gid,\
                "Image should be in group: %s, NOT %s" % (target_gid,  img_gid)

    def testChgrp11000(self):
        """
        Move a Dataset of MIF images *with a companion file* into a new group.
        Note: once FakeReader supports companion files this logic can be
        simplified.
        """
        # One user in two groups
        client, user = self.new_client_and_user(perms=PRIVATE)

        update = client.sf.getUpdateService()
        ds = DatasetI()
        ds.name = rstring("testChgrp11000")
        ds = update.saveAndReturnObject(ds)
        images = self.importMIF(2, client=client)
        for i in range(2):
            link = DatasetImageLinkI()
            link.setParent(ds.proxy())
            link.setChild(images[i].proxy())
            link = update.saveAndReturnObject(link)

        # Perform the extra companion file logic
        fs = client.sf.getQueryService().findByQuery("""
            select fs from Image i
              join i.fileset fs
              join fetch fs.usedFiles as uf
              join fetch uf.originalFile
            where i.id = %s
        """ % images[0].id.val, None)
        entry1 = fs.getFilesetEntry(0)
        ofile = entry1.getOriginalFile()
        for i in range(2):
            link = ImageAnnotationLinkI()
            ann = FileAnnotationI()
            ann.file = ofile.proxy()
            link.setParent(images[i].proxy())
            link.setChild(ann)
            link = update.saveAndReturnObject(link)

    def testChgrp11109(self):
        """
        Place a plate in a single screen and attempt to move it.
        """
        # One user in two groups
        client, user = self.new_client_and_user(perms=PRIVATE)
        admin = client.sf.getAdminService()
        target_grp = self.new_group([user], perms=PRIVATE)
        target_gid = target_grp.id.val
        admin.getEventContext()  # Refresh

        update = client.sf.getUpdateService()
        plate = PlateI()
        plate.name = rstring("testChgrp11109")
        screen = ScreenI()
        screen.name = rstring("testChgrp11109")
        link = screen.linkPlate(plate)
        link = update.saveAndReturnObject(link)

        # Now chgrp, should succeed
        chgrp = Chgrp2(
            targetObjects={"Plate": [link.child.id.val]}, groupId=target_gid)
        self.doSubmit(chgrp, client)

        # Check that the links have been destroyed
        query = client.sf.getQueryService()
        with pytest.raises(omero.ValidationException):
            query.get("ScreenPlateLink", link.id.val, {"omero.group": "-1"})

    def testChgrpDatasetWithImage(self):
        """
        D->I
        ChGrp D

        See https://trac.openmicroscopy.org.uk/ome/ticket/12452
        """
        client, user = self.new_client_and_user(perms=PRIVATE)
        admin = client.sf.getAdminService()
        target_grp = self.new_group([user], perms=PRIVATE)
        target_gid = target_grp.id.val
        admin.getEventContext()  # Refresh

        query = client.sf.getQueryService()
        update = client.sf.getUpdateService()

        d = self.make_dataset(client=client)
        i = self.new_image()
        i = update.saveAndReturnObject(i)
        self.link(d, i, client)
        self.change_group([d], target_gid, client)

        ctx = {'omero.group': '-1'}
        assert target_gid == query.get("Image",
                                       i.id.val, ctx).details.group.id.val
        assert target_gid == query.get("Dataset",
                                       d.id.val, ctx).details.group.id.val

    def testChgrpPDIReverseLinkOrder(self):
        """
        P->D->I
        ChGrp P

        See https://trac.openmicroscopy.org.uk/ome/ticket/12452
        """
        client, user = self.new_client_and_user(perms=PRIVATE)
        admin = client.sf.getAdminService()
        target_grp = self.new_group([user], perms=PRIVATE)
        target_gid = target_grp.id.val
        admin.getEventContext()  # Refresh

        query = client.sf.getQueryService()
        update = client.sf.getUpdateService()

        p = self.make_project(client=client)
        d = self.make_dataset(client=client)
        i = self.new_image()
        i = update.saveAndReturnObject(i)
        self.link(p, d, client)
        self.link(d, i, client)
        self.change_group([p], target_gid, client)

        ctx = {'omero.group': '-1'}
        assert target_gid == query.get("Project",
                                       p.id.val, ctx).details.group.id.val
        assert target_gid == query.get("Dataset",
                                       d.id.val, ctx).details.group.id.val
        assert target_gid == query.get("Image",
                                       i.id.val, ctx).details.group.id.val

    def testChgrpTwoDatasetsLinkedToSingleImageDefault(self):
        """
        D1->I
        D2->I
        ChGrp D1

        See https://trac.openmicroscopy.org.uk/ome/ticket/12452
        """
        client, user = self.new_client_and_user(perms=PRIVATE)
        admin = client.sf.getAdminService()
        target_grp = self.new_group([user], perms=PRIVATE)
        target_gid = target_grp.id.val
        admin.getEventContext()  # Refresh

        query = client.sf.getQueryService()
        update = client.sf.getUpdateService()

        d1 = self.make_dataset(client=client)
        d2 = self.make_dataset(client=client)
        i = self.new_image()
        i = update.saveAndReturnObject(i)
        self.link(d1, i, client)
        self.link(d2, i, client)
        self.change_group([d1], target_gid, client)

        ctx = {'omero.group': '-1'}
        assert target_gid == query.get("Dataset",
                                       d1.id.val, ctx).details.group.id.val
        assert target_gid != query.get("Dataset",
                                       d2.id.val, ctx).details.group.id.val
        assert target_gid != query.get("Image",
                                       i.id.val, ctx).details.group.id.val

    def testChgrpTwoDatasetsLinkedToSingleImageHard(self):
        """
        D1->I
        D2->I
        ChGrp D1

        See https://trac.openmicroscopy.org.uk/ome/ticket/12452
        """
        client, user = self.new_client_and_user(perms=PRIVATE)
        admin = client.sf.getAdminService()
        target_grp = self.new_group([user], perms=PRIVATE)
        target_gid = target_grp.id.val
        admin.getEventContext()  # Refresh

        query = client.sf.getQueryService()
        update = client.sf.getUpdateService()

        d1 = self.make_dataset(client=client)
        d2 = self.make_dataset(client=client)
        i = self.new_image()
        i = update.saveAndReturnObject(i)
        self.link(d1, i, client)
        self.link(d2, i, client)

        hard = ChildOption(includeType=["Image"])
        chgrp = Chgrp2(
            targetObjects={"Dataset": [d1.id.val]}, childOptions=[hard],
            groupId=target_gid)
        self.doSubmit(chgrp, client)

        ctx = {'omero.group': '-1'}
        assert target_gid == query.get("Dataset",
                                       d1.id.val, ctx).details.group.id.val
        assert target_gid != query.get("Dataset",
                                       d2.id.val, ctx).details.group.id.val
        assert target_gid == query.get("Image",
                                       i.id.val, ctx).details.group.id.val

    def testChgrpProjectWithDatasetLinkedToImageWithOtherDatasetDefault(self):
        """
        P->D1->I
           D2->I
        ChGrp P

        See https://trac.openmicroscopy.org.uk/ome/ticket/12452
        """
        client, user = self.new_client_and_user(perms=PRIVATE)
        admin = client.sf.getAdminService()
        target_grp = self.new_group([user], perms=PRIVATE)
        target_gid = target_grp.id.val
        admin.getEventContext()  # Refresh

        query = client.sf.getQueryService()
        update = client.sf.getUpdateService()

        p = self.make_project(client=client)
        d1 = self.make_dataset(client=client)
        d2 = self.make_dataset(client=client)
        i = self.new_image()
        i = update.saveAndReturnObject(i)
        self.link(d1, i, client)
        self.link(d2, i, client)
        self.link(p, d1, client)
        self.change_group([p], target_gid, client)

        ctx = {'omero.group': '-1'}
        assert target_gid == query.get("Project",
                                       p.id.val, ctx).details.group.id.val
        assert target_gid == query.get("Dataset",
                                       d1.id.val, ctx).details.group.id.val
        assert target_gid != query.get("Image",
                                       i.id.val, ctx).details.group.id.val

    def testChgrpProjectWithDatasetLinkedToImageWithOtherDatasetHard(self):
        """
        P->D1->I
           D2->I
        ChGrp P

        See https://trac.openmicroscopy.org.uk/ome/ticket/12452
        """
        client, user = self.new_client_and_user(perms=PRIVATE)
        admin = client.sf.getAdminService()
        target_grp = self.new_group([user], perms=PRIVATE)
        target_gid = target_grp.id.val
        admin.getEventContext()  # Refresh

        query = client.sf.getQueryService()
        update = client.sf.getUpdateService()

        p = self.make_project(client=client)
        d1 = self.make_dataset(client=client)
        d2 = self.make_dataset(client=client)
        i = self.new_image()
        i = update.saveAndReturnObject(i)
        self.link(d1, i, client)
        self.link(d2, i, client)
        self.link(p, d1, client)

        hard = ChildOption(includeType=["Image"])
        chgrp = Chgrp2(
            targetObjects={"Project": [p.id.val]}, childOptions=[hard],
            groupId=target_gid)
        self.doSubmit(chgrp, client)

        ctx = {'omero.group': '-1'}
        assert target_gid == query.get("Project",
                                       p.id.val, ctx).details.group.id.val
        assert target_gid == query.get("Dataset",
                                       d1.id.val, ctx).details.group.id.val
        assert target_gid != query.get("Dataset",
                                       d2.id.val, ctx).details.group.id.val
        assert target_gid == query.get("Image",
                                       i.id.val, ctx).details.group.id.val

    def testChgrpDatasetWithImageLinkedToTwoProjects(self):
        """
        P1->D->I
        P2->D->I
        ChGrp D

        See https://trac.openmicroscopy.org.uk/ome/ticket/12452
        """
        client, user = self.new_client_and_user(perms=PRIVATE)
        admin = client.sf.getAdminService()
        target_grp = self.new_group([user], perms=PRIVATE)
        target_gid = target_grp.id.val
        admin.getEventContext()  # Refresh

        query = client.sf.getQueryService()
        update = client.sf.getUpdateService()

        p1 = self.make_project(client=client)
        p2 = self.make_project(client=client)
        d = self.make_dataset(client=client)
        i = self.new_image()
        i = update.saveAndReturnObject(i)
        self.link(p1, d, client)
        self.link(p2, d, client)
        self.link(d, i, client)
        self.change_group([d], target_gid, client)

        ctx = {'omero.group': '-1'}

        assert not target_gid == query.get("Project",
                                           p1.id.val, ctx).details.group.id.val
        assert not target_gid == query.get("Project",
                                           p2.id.val, ctx).details.group.id.val
        assert target_gid == query.get("Dataset",
                                       d.id.val, ctx).details.group.id.val
        assert target_gid == query.get("Image",
                                       i.id.val, ctx).details.group.id.val

    def testChgrpProjectLinkedToDatasetAndImageDefault(self):
        """
        P1->D->I
        P2->D->I
        ChGrp P1

        See https://trac.openmicroscopy.org.uk/ome/ticket/12452
        """
        client, user = self.new_client_and_user(perms=PRIVATE)
        admin = client.sf.getAdminService()
        target_grp = self.new_group([user], perms=PRIVATE)
        target_gid = target_grp.id.val
        admin.getEventContext()  # Refresh

        query = client.sf.getQueryService()
        update = client.sf.getUpdateService()

        p1 = self.make_project(client=client)
        p2 = self.make_project(client=client)
        d = self.make_dataset(client=client)
        i = self.new_image()
        i = update.saveAndReturnObject(i)
        self.link(p1, d, client)
        self.link(p2, d, client)
        self.link(d, i, client)
        self.change_group([p1], target_gid, client)

        ctx = {'omero.group': '-1'}
        assert target_gid == query.get("Project",
                                       p1.id.val, ctx).details.group.id.val
        assert target_gid != query.get("Dataset",
                                       d.id.val, ctx).details.group.id.val
        assert target_gid != query.get("Image",
                                       i.id.val, ctx).details.group.id.val

    def testChgrpProjectLinkedToDatasetAndImageHard(self):
        """
        P1->D->I
        P2->D->I
        ChGrp P1

        See https://trac.openmicroscopy.org.uk/ome/ticket/12452
        """
        client, user = self.new_client_and_user(perms=PRIVATE)
        admin = client.sf.getAdminService()
        target_grp = self.new_group([user], perms=PRIVATE)
        target_gid = target_grp.id.val
        admin.getEventContext()  # Refresh

        query = client.sf.getQueryService()
        update = client.sf.getUpdateService()

        p1 = self.make_project(client=client)
        p2 = self.make_project(client=client)
        d = self.make_dataset(client=client)
        i = self.new_image()
        i = update.saveAndReturnObject(i)
        self.link(p1, d, client)
        self.link(p2, d, client)
        self.link(d, i, client)

        hard = ChildOption(includeType=["Dataset"])
        chgrp = Chgrp2(
            targetObjects={"Project": [p1.id.val]}, childOptions=[hard],
            groupId=target_gid)
        self.doSubmit(chgrp, client)

        ctx = {'omero.group': '-1'}
        assert target_gid == query.get("Project",
                                       p1.id.val, ctx).details.group.id.val
        assert target_gid != query.get("Project",
                                       p2.id.val, ctx).details.group.id.val
        assert target_gid == query.get("Dataset",
                                       d.id.val, ctx).details.group.id.val
        assert target_gid == query.get("Image",
                                       i.id.val, ctx).details.group.id.val

    def testChgrpProjectLinkedToDatasetDefault(self):
        """
        P1->D
        P2->D
        ChGrp P1

        See https://trac.openmicroscopy.org.uk/ome/ticket/12452
        """
        client, user = self.new_client_and_user(perms=PRIVATE)
        admin = client.sf.getAdminService()
        target_grp = self.new_group([user], perms=PRIVATE)
        target_gid = target_grp.id.val
        admin.getEventContext()  # Refresh

        query = client.sf.getQueryService()

        p1 = self.make_project(client=client)
        p2 = self.make_project(client=client)
        d = self.make_dataset(client=client)
        self.link(p1, d, client)
        self.link(p2, d, client)
        self.change_group([p1], target_gid, client)

        ctx = {'omero.group': '-1'}
        assert target_gid == query.get("Project",
                                       p1.id.val, ctx).details.group.id.val
        assert target_gid != query.get("Dataset",
                                       d.id.val, ctx).details.group.id.val

    def testChgrpProjectLinkedToDatasetHard(self):
        """
        P1->D
        P2->D
        ChGrp P1

        See https://trac.openmicroscopy.org.uk/ome/ticket/12452
        """
        client, user = self.new_client_and_user(perms=PRIVATE)
        admin = client.sf.getAdminService()
        target_grp = self.new_group([user], perms=PRIVATE)
        target_gid = target_grp.id.val
        admin.getEventContext()  # Refresh

        query = client.sf.getQueryService()

        p1 = self.make_project(client=client)
        p2 = self.make_project(client=client)
        d = self.make_dataset(client=client)
        self.link(p1, d, client)
        self.link(p2, d, client)

        hard = ChildOption(includeType=["Dataset"])
        chgrp = Chgrp2(
            targetObjects={"Project": [p1.id.val]}, childOptions=[hard],
            groupId=target_gid)
        self.doSubmit(chgrp, client)

        ctx = {'omero.group': '-1'}
        assert target_gid == query.get("Project",
                                       p1.id.val, ctx).details.group.id.val
        assert target_gid != query.get("Project",
                                       p2.id.val, ctx).details.group.id.val
        assert target_gid == query.get("Dataset",
                                       d.id.val, ctx).details.group.id.val

    def testChgrpProjectLinkedToTwoDatasetsAndImage(self):
        """
        P->D1->I
        P->D2->I
        ChGrp P

        See https://trac.openmicroscopy.org.uk/ome/ticket/12452
        """
        client, user = self.new_client_and_user(perms=PRIVATE)
        admin = client.sf.getAdminService()
        target_grp = self.new_group([user], perms=PRIVATE)
        target_gid = target_grp.id.val
        admin.getEventContext()  # Refresh

        query = client.sf.getQueryService()
        update = client.sf.getUpdateService()

        p = self.make_project(client=client)
        d1 = self.make_dataset(client=client)
        d2 = self.make_dataset(client=client)
        i = self.new_image()
        i = update.saveAndReturnObject(i)
        self.link(p, d1, client)
        self.link(p, d2, client)
        self.link(d1, i, client)
        self.link(d2, i, client)
        self.change_group([p], target_gid, client)

        ctx = {'omero.group': '-1'}
        assert target_gid == query.get("Project",
                                       p.id.val, ctx).details.group.id.val
        assert target_gid == query.get("Dataset",
                                       d1.id.val, ctx).details.group.id.val
        assert target_gid == query.get("Dataset",
                                       d2.id.val, ctx).details.group.id.val
        assert target_gid == query.get("Image",
                                       i.id.val, ctx).details.group.id.val

    def testIntergroupLinks(self):
        # create read-annotate group 'read-annotate' with implicit owner
        ra_group = self.new_group(perms=READANNOTATE)
        self.new_user(group=ra_group, owner=True)

        # create private group 'private' with implicit owner
        p_group = self.new_group(perms=PRIVATE)
        self.new_user(group=p_group, owner=True)

        # create new user 'image-owner' who is a member of both 'read-annotate'
        # and 'private'
        io_client, image_owner = self.new_client_and_user(group=ra_group)
        self.add_groups(image_owner, [p_group])

        # create new user 'tag-owner' who is a member of both 'read-annotate'
        # and 'private'
        to_client, tag_owner = self.new_client_and_user(group=ra_group)
        self.add_groups(tag_owner, [p_group])

        # switch user to 'image-owner'
        # import two images into 'read-annotate'
        images = []
        for x in range(0, 2):
            images.append(self.importSingleImage(client=io_client))
            image = io_client.sf.getQueryService().get("Image",
                                                       images[x].id.val)
            assert ra_group.id.val == image.details.group.id.val

        # switch user to tag-owner
        # tag both image-owner's images with the same new tag
        tag = TagAnnotationI()
        tag.textValue = rstring("tag from user %s" %
                                tag_owner.getOmeName().val)
        tag = to_client.sf.getUpdateService().saveAndReturnObject(tag)
        assert tag_owner.id.val == tag.details.owner.id.val
        links = []
        for image in images:
            link = ImageAnnotationLinkI()
            link.setParent(image)
            link.setChild(tag)
            links.append(to_client.sf.getUpdateService()
                         .saveAndReturnObject(link))

        # (shell) as root
        # run bin/omero hql --all 'select parent.details.group.id,
        # child.details.group.id from ImageAnnotationLink'
        # and observe that for each row
        # the group ID in Col1 matches that in Col2
        for link in links:
            assert link.parent.details.group.id == link.child.details.group.id

        # switch user to image-owner
        # right-click one of the images and move it to private
        self.change_group([images[0]], p_group.id.val, io_client)

        # (shell) as root
        # run bin/omero hql --all 'select parent.details.group.id,
        # child.details.group.id from ImageAnnotationLink' and recoil in horror
        params = omero.sys.ParametersI()
        params.addId(tag.id.val)
        ctx = {"omero.group": "-1"}
        query = "select parent.details.group.id,"
        query += " child.details.group.id from ImageAnnotationLink"
        query += " where child.id = :id"
        links = unwrap(self.root.sf.getQueryService().projection(query, params,
                                                                 ctx))
        assert links is not None
        for link in links:
            assert link[0] == link[1]


class TestChgrpTarget(lib.ITest):

    def createDSInGroup(self, gid, name=None, client=None):
        if name is None:
            name = self.uuid()
        if client is None:
            client = self.client
        ctx = {'omero.group': str(gid)}
        update = client.sf.getUpdateService()
        ds = DatasetI()
        ds.name = rstring(name)
        return update.saveAndReturnObject(ds, ctx)

    def chgrpImagesToTargetDataset(self, imgCount):
        """
        Helper method to test chgrp of image(s) to target Dataset
        """
        # One user in two groups
        client, user = self.new_client_and_user(perms=PRIVATE)
        admin = client.sf.getAdminService()
        target_grp = self.new_group([user], perms=PRIVATE)
        target_gid = target_grp.id.val

        images = self.importMIF(imgCount, client=client)
        ds = self.createDSInGroup(target_gid, client=client)

        # each chgrp includes a 'save' link to target dataset
        saves = []
        ids = []
        for i in images:
            ids.append(i.id.val)
            link = DatasetImageLinkI()
            link.child = ImageI(i.id.val, False)
            link.parent = DatasetI(ds.id.val, False)
            save = Save()
            save.obj = link
            saves.append(save)
        chgrp = Chgrp2(
            targetObjects={"Image": ids}, groupId=target_gid)
        requests = [chgrp]
        requests.extend(saves)
        self.doSubmit(requests, client, omero_group=target_gid)

        # Check Images moved to correct group
        queryService = client.sf.getQueryService()
        ctx = {'omero.group': '-1'}  # query across groups
        for i in images:
            image = queryService.get('Image', i.id.val, ctx)
            img_gid = image.details.group.id.val
            assert target_gid == img_gid,\
                "Image should be in group: %s, NOT %s" % (target_gid,  img_gid)
        # Check Dataset has images linked
        dsImgs = client.sf.getContainerService().getImages(
            'Dataset', [ds.id.val], None, ctx)
        assert len(dsImgs) == len(images),\
            "All Images should be in target Dataset"

        previous_gid = admin.getEventContext().groupId
        return (ds, images, client, user, previous_gid, target_gid)

    def testChgrpImageToTargetDataset(self):
        """ Chgrp a single Image to target Dataset """
        self.chgrpImagesToTargetDataset(1)

    def testChgrpMifImagesToTargetDataset(self):
        """ Chgrp 2 images in a MIF to target Dataset """
        self.chgrpImagesToTargetDataset(2)

    def testChgrpImageToTargetDatasetAndBackNoDS(self):
        """
        Chgrp a single Image to target Dataset and then back
        No target is provided on the way back.
        see ticket:11118
        """
        ds, images, client, user, old_gid, new_gid =\
            self.chgrpImagesToTargetDataset(1)
        chgrp = Chgrp2(
            targetObjects={"Image": [images[0].id.val]}, groupId=old_gid)
        self.doSubmit(chgrp, client, omero_group=old_gid)

    def testChgrpImageToTargetDatasetAndBackDS(self):
        """
        Chgrp a single Image to target Dataset and then back
        see ticket:11118
        """
        new_ds, images, client, user, old_gid, new_gid =\
            self.chgrpImagesToTargetDataset(1)

        # create Dataset in original group
        old_ds = self.createDSInGroup(old_gid, client=client)
        link = DatasetImageLinkI()
        link.parent = old_ds.proxy()
        link.child = images[0].proxy()

        chgrp = Chgrp2(
            targetObjects={"Image": [images[0].id.val]}, groupId=old_gid)
        save = Save(link)
        self.doSubmit([chgrp, save], client, omero_group=old_gid)

        dils = client.sf.getQueryService().findAllByQuery(
            "select dil from DatasetImageLink dil where dil.child.id = :id",
            omero.sys.ParametersI().addId(images[0].id.val),
            {"omero.group": "-1"})
        assert 1 == len(dils)

    @pytest.mark.parametrize("credentials", ["user", "admin"])
    def testChgrpDatasetToTargetProject(self, credentials):
        """
        Tests that an Admin can move a user's Dataset to a private
        group and link it to an existing user's Project there.
        Also tests that the user can do the same chgrp themselves.
        """

        # One user in two groups
        client, user = self.new_client_and_user(perms=PRIVATE)
        target_grp = self.new_group([user], perms=PRIVATE)
        eCtx = client.sf.getAdminService().getEventContext()  # Reset session
        userId = eCtx.userId
        target_gid = target_grp.id.val

        # User creates Dataset in current group...
        update = client.sf.getUpdateService()
        ds = DatasetI()
        ds.name = rstring(self.uuid())
        ds = update.saveAndReturnObject(ds)
        # ...and Project in target group
        ctx = {'omero.group': str(target_gid)}
        pr = ProjectI()
        pr.name = rstring(self.uuid())
        pr = update.saveAndReturnObject(pr, ctx)

        requests = []
        saves = []
        chgrp = Chgrp2(
            targetObjects={"Dataset": [ds.id.val]}, groupId=target_gid)
        requests.append(chgrp)
        link = ProjectDatasetLinkI()
        link.details.owner = ExperimenterI(userId, False)
        link.child = DatasetI(ds.id.val, False)
        link.parent = ProjectI(pr.id.val, False)
        save = Save()
        save.obj = link
        saves.append(save)
        requests.extend(saves)

        if credentials == "user":
            c = client
        else:
            c = self.root
        self.doSubmit(requests, c, omero_group=target_gid)

        queryService = client.sf.getQueryService()
        ctx = {'omero.group': '-1'}  # query across groups
        dataset = queryService.get('Dataset', ds.id.val, ctx)
        ds_gid = dataset.details.group.id.val
        assert target_gid == ds_gid,\
            "Dataset should be in group: %s, NOT %s" % (target_gid, ds_gid)
