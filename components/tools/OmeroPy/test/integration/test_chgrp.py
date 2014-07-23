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
import test.integration.library as lib
import pytest
from omero.rtypes import rstring
from omero.api import Save

PRIVATE = 'rw----'
READONLY = 'rwr---'
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
        chgrp = omero.cmd.Chgrp(
            type="/Image", id=image.id.val, options=None, grp=gid)
        self.doAllSubmit([chgrp], client)

        # Change our context to new group...
        admin = client.sf.getAdminService()
        admin.setDefaultGroup(exp, omero.model.ExperimenterGroupI(gid, False))
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
        ds = omero.model.DatasetI()
        ds.name = rstring("testChgrpImage_target")
        ds = update.saveAndReturnObject(ds)
        ds_id = ds.id.val

        # Change our context to new group and create image
        admin.setDefaultGroup(exp, omero.model.ExperimenterGroupI(gid, False))
        self.set_context(client, gid)
        update = client.sf.getUpdateService()   # do we need to get this again?
        img = self.new_image()
        img = update.saveAndReturnObject(img)

        # Move image to new group
        chgrp = omero.cmd.Chgrp(
            type="/Image", id=img.id.val, options=None, grp=first_gid)

        # Link to Save
        link = omero.model.DatasetImageLinkI()
        link.child = omero.model.ImageI(img.id.val, False)
        link.parent = omero.model.DatasetI(ds_id, False)
        save = Save()
        save.obj = link
        requests = [chgrp, save]        # we're going to chgrp THEN save DIlink

        # Change our context to original group...
        admin.setDefaultGroup(
            exp, omero.model.ExperimenterGroupI(first_gid, False))
        self.set_context(client, first_gid)

        # We have to be in destination group for link Save to work
        self.doAllSubmit(requests, client)

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
        project = omero.model.ProjectI()
        project.setName(rstring("chgrp-test"))
        project = update.saveAndReturnObject(project)
        dataset = omero.model.DatasetI()
        dataset.setName(rstring("chgrp-test"))
        dataset = update.saveAndReturnObject(dataset)
        links = []
        link = omero.model.DatasetImageLinkI()
        link.setChild(img)
        link.setParent(dataset)
        links.append(link)
        l = omero.model.ProjectDatasetLinkI()
        l.setChild(dataset.proxy())
        l.setParent(project.proxy())
        links.append(l)
        update.saveAndReturnArray(links)

        # Move Project to new group
        chgrp = omero.cmd.Chgrp(
            type="/Project", id=project.id.val, options=None, grp=gid)
        self.doAllSubmit([chgrp], client)

        # Change our context to new group...
        admin = client.sf.getAdminService()
        admin.setDefaultGroup(exp, omero.model.ExperimenterGroupI(gid, False))
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
        chgrp = omero.cmd.Chgrp(type="/Image", id=image.id.val, grp=target_gid)
        self.doAllSubmit([chgrp], owner)

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
        chgrp = omero.cmd.Chgrp(
            type="/Image", id=images[0].id.val, grp=target_gid)
        self.doAllSubmit([chgrp], client, test_should_pass=False)

        # 10846 - multiple constraints are no longer being collected.
        # in fact, even single constraints are not being directly directed
        # since fileset cleanup is happening at the end of the transaction
        # disabling and marking in ticket.
        # The delete should fail due to the fileset
        # The chgrp should fail due to the fileset
        # ## assert 'Fileset' in rsp.constraints,\
        # ##     "chgrp should fail due to 'Fileset' constraints"
        # ## failedFilesets = rsp.constraints['Fileset']
        # ## assert len(failedFilesets) ==  1,\
        # ##     "chgrp should fail due to a single Fileset"
        # ## assert failedFilesets[0] ==  filesetId,\
        # ##     "chgrp should fail due to this Fileset"

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
        chgrp1 = omero.cmd.Chgrp(
            type="/Image", id=images[0].id.val, grp=target_gid)
        chgrp2 = omero.cmd.Chgrp(
            type="/Image", id=images[1].id.val, grp=target_gid)
        self.doAllSubmit([chgrp1, chgrp2], client)

        # Check both Images moved
        queryService = client.sf.getQueryService()
        ctx = {'omero.group': '-1'}      # query across groups
        for i in images:
            image = queryService.get('Image', i.id.val, ctx)
            img_gid = image.details.group.id.val
            assert target_gid == img_gid,\
                "Image should be in group: %s, NOT %s" % (target_gid,  img_gid)

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
            link = omero.model.DatasetImageLinkI()
            link.setParent(datasets[i].proxy())
            link.setChild(images[i].proxy())
            link = update.saveAndReturnObject(link)

        # chgrp should fail...
        chgrp = omero.cmd.Chgrp(
            type="/Dataset", id=datasets[0].id.val, grp=target_gid)
        self.doAllSubmit([chgrp], client, test_should_pass=False)

        # 10846 - multiple constraints are no longer being collected.
        # in fact, even single constraints are not being directly directed
        # since fileset cleanup is happening at the end of the transaction
        # disabling and marking in ticket.
        # The delete should fail due to the fileset
        # ...due to the fileset
        # ## assert 'Fileset' in rsp.constraints,
        # ##     "chgrp should fail due to 'Fileset' constraints"
        # ## failedFilesets = rsp.constraints['Fileset']
        # ## assert len(failedFilesets) ==  1,\
        # ##     "chgrp should fail due to a single Fileset"
        # ## assert failedFilesets[0] ==  filesetId,
        # ##     "chgrp should fail due to this Fileset"

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
            link = omero.model.DatasetImageLinkI()
            link.setParent(datasets[i].proxy())
            link.setChild(images[i].proxy())
            link = update.saveAndReturnObject(link)

        # Now chgrp, should succeed
        chgrp1 = omero.cmd.Chgrp(
            type="/Dataset", id=datasets[0].id.val, grp=target_gid)
        chgrp2 = omero.cmd.Chgrp(
            type="/Dataset", id=datasets[1].id.val, grp=target_gid)
        self.doAllSubmit([chgrp1, chgrp2], client)

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
        ds = omero.model.DatasetI()
        ds.name = rstring("testChgrpOneDatasetFilesetOK")
        ds = update.saveAndReturnObject(ds)
        images = self.importMIF(2, client=client)
        for i in range(2):
            link = omero.model.DatasetImageLinkI()
            link.setParent(ds.proxy())
            link.setChild(images[i].proxy())
            link = update.saveAndReturnObject(link)

        # Now chgrp, should succeed
        chgrp = omero.cmd.Chgrp(type="/Dataset", id=ds.id.val, grp=target_gid)
        self.doAllSubmit([chgrp], client)

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
        chgrp1 = omero.cmd.Chgrp(
            type="/Image", id=imagesFsOne[0].id.val, grp=target_gid)
        chgrp2 = omero.cmd.Chgrp(
            type="/Image", id=imagesFsTwo[0].id.val, grp=target_gid)
        self.doAllSubmit([chgrp1, chgrp2], client, test_should_pass=False)

        # 10846 - multiple constraints are no longer being collected.
        # in fact, even single constraints are not being directly directed
        # since fileset cleanup is happening at the end of the transaction
        # disabling and marking in ticket.
        # The delete should fail due to the fileset
        # ...due to the filesets
        # ## assert 'Fileset' in rsp.constraints,
        # ##     "chgrp should fail due to 'Fileset' constraints"
        # ## failedFilesets = rsp.constraints['Fileset']
        # ## assert len(failedFilesets) ==  2,\
        # ##     "chgrp should fail due to a Two Filesets"
        # ## self.assertTrue(filesetOneId in failedFilesets)
        # ## self.assertTrue(filesetTwoId in failedFilesets)

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
        ds = omero.model.DatasetI()
        ds.name = rstring("testChgrpDatasetTwoFilesetsErr")
        ds = update.saveAndReturnObject(ds)
        self.importMIF(2, client=client)
        for i in (imagesFsOne, imagesFsTwo):
            link = omero.model.DatasetImageLinkI()
            link.setParent(ds.proxy())
            link.setChild(i[0].proxy())
            link = update.saveAndReturnObject(link)

        # chgrp should fail...
        chgrp = omero.cmd.Chgrp(type="/Dataset", id=ds.id.val, grp=target_gid)
        self.doAllSubmit([chgrp], client, test_should_pass=False)

        # 10846 - multiple constraints are no longer being collected.
        # in fact, even single constraints are not being directly directed
        # since fileset cleanup is happening at the end of the transaction
        # disabling and marking in ticket.
        # The delete should fail due to the fileset
        # ...due to the filesets
        # ## assert 'Fileset' in rsp.constraints,
        # ##     "chgrp should fail due to 'Fileset' constraints"
        # ## failedFilesets = rsp.constraints['Fileset']
        # ## assert len(failedFilesets) ==  2,\
        # ##     "chgrp should fail due to a Two Filesets"
        # ## self.assertTrue(filesetOneId in failedFilesets)
        # ## self.assertTrue(filesetTwoId in failedFilesets)

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
        ds = omero.model.DatasetI()
        ds.name = rstring("testChgrpDatasetCheckFsGroup")
        ds = update.saveAndReturnObject(ds)
        images = self.importMIF(2, client=client)
        for i in range(2):
            link = omero.model.DatasetImageLinkI()
            link.setParent(ds.proxy())
            link.setChild(images[i].proxy())
            link = update.saveAndReturnObject(link)

        # Now chgrp, should succeed
        chgrp = omero.cmd.Chgrp(type="/Dataset", id=ds.id.val, grp=target_gid)
        self.doAllSubmit([chgrp], client)

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
        chgrp = omero.cmd.Chgrp(type="/Fileset", id=fsId, grp=target_gid)
        self.doAllSubmit([chgrp], client)

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
        ds = omero.model.DatasetI()
        ds.name = rstring("testChgrp11000")
        ds = update.saveAndReturnObject(ds)
        images = self.importMIF(2, client=client)
        for i in range(2):
            link = omero.model.DatasetImageLinkI()
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
            link = omero.model.ImageAnnotationLinkI()
            ann = omero.model.FileAnnotationI()
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
        plate = omero.model.PlateI()
        plate.name = rstring("testChgrp11109")
        screen = omero.model.ScreenI()
        screen.name = rstring("testChgrp11109")
        link = screen.linkPlate(plate)
        link = update.saveAndReturnObject(link)

        # Now chgrp, should succeed
        chgrp = omero.cmd.Chgrp(
            type="/Plate", id=link.child.id.val, grp=target_gid)
        self.doAllSubmit([chgrp], client)

        # Check that the links have been destroyed
        query = client.sf.getQueryService()
        with pytest.raises(omero.ValidationException):
            query.get("ScreenPlateLink", link.id.val, {"omero.group": "-1"})


class TestChgrpTarget(lib.ITest):

    def createDSInGroup(self, gid, name=None, client=None):
        if name is None:
            name = self.uuid()
        if client is None:
            client = self.client
        ctx = {'omero.group': str(gid)}
        update = client.sf.getUpdateService()
        ds = omero.model.DatasetI()
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
        requests = []
        saves = []
        for i in images:
            chgrp = omero.cmd.Chgrp(type="/Image", id=i.id.val, grp=target_gid)
            requests.append(chgrp)
            link = omero.model.DatasetImageLinkI()
            link.child = omero.model.ImageI(i.id.val, False)
            link.parent = omero.model.DatasetI(ds.id.val, False)
            save = Save()
            save.obj = link
            saves.append(save)

        requests.extend(saves)
        self.doAllSubmit(requests, client, omero_group=target_gid)

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
        chgrp = omero.cmd.Chgrp(
            type="/Image", id=images[0].id.val, grp=old_gid)
        self.doAllSubmit([chgrp], client, omero_group=old_gid)

    def testChgrpImageToTargetDatasetAndBackDS(self):
        """
        Chgrp a single Image to target Dataset and then back
        see ticket:11118
        """
        new_ds, images, client, user, old_gid, new_gid =\
            self.chgrpImagesToTargetDataset(1)

        # create Dataset in original group
        old_ds = self.createDSInGroup(old_gid, client=client)
        link = omero.model.DatasetImageLinkI()
        link.parent = old_ds.proxy()
        link.child = images[0].proxy()

        chgrp = omero.cmd.Chgrp(
            type="/Image", id=images[0].id.val, grp=old_gid)
        save = Save(link)
        self.doAllSubmit([chgrp, save], client, omero_group=old_gid)

        dils = client.sf.getQueryService().findAllByQuery(
            "select dil from DatasetImageLink dil where dil.child.id = :id",
            omero.sys.ParametersI().addId(images[0].id.val),
            {"omero.group": "-1"})
        assert 1 == len(dils)
