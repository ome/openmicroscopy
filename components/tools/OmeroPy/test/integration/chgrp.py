#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2012 University of Dundee & Open Microscopy Environment.
#                      All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

import omero, omero.gateway
import integration.library as lib
import unittest
from omero.rtypes import *
from omero.cmd import Chgrp, DoAll
from omero.api import Save

PRIVATE = 'rw----'
READONLY = 'rwr---'
COLLAB = 'rwrw--'

class TestChgrp(lib.ITest):

    def doAllChgrp(self, requests, client):
        
        da = DoAll()
        da.requests = requests
        rsp = self.doSubmit(da, client)

    def testChgrpImportedImage(self):
        """
        Tests chgrp for an imported image, moving to a collaborative group
        """
        # One user in two groups
        client, exp = self.new_client_and_user()
        grp = self.new_group(experimenters=[exp], perms=COLLAB)
        gid = grp.id.val
        client.sf.getAdminService().getEventContext() # Reset session
        update = client.sf.getUpdateService()
        query = client.sf.getQueryService()

        # Import an image into the client context
        pixID = long(self.import_image(client=client)[0])
        pixels = client.sf.getQueryService().get("Pixels", pixID)
        imageId = pixels.getImage().getId().getValue()

        # Chgrp
        chgrp = omero.cmd.Chgrp(type="/Image", id=imageId, options=None, grp=gid)
        self.doSubmit(chgrp, client)

        # Change our context to new group...
        admin = client.sf.getAdminService()
        admin.setDefaultGroup(exp, omero.model.ExperimenterGroupI(gid, False))
        self.set_context(client, gid)
        # ...check image
        img = client.sf.getQueryService().get("Image", imageId)
        self.assertEqual(img.details.group.id.val, gid)


    def testChgrpImage(self):
        """
        Tests chgrp for a dummny image object (no Pixels)
        """
        # One user in two groups
        client, exp = self.new_client_and_user()
        grp = self.new_group([exp])
        gid = grp.id.val
        client.sf.getAdminService().getEventContext() # Reset session
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
        chgrp = omero.cmd.Chgrp(type="/Image", id=img.id.val, options=None, grp=first_gid)

        # Link to Save
        link = omero.model.DatasetImageLinkI()
        link.child = omero.model.ImageI(img.id.val, False)
        link.parent = omero.model.DatasetI(ds_id, False)
        save = Save()
        save.obj = link
        requests = [chgrp, save]        # we're going to chgrp THEN save DIlink

        # Change our context to original group...
        admin.setDefaultGroup(exp, omero.model.ExperimenterGroupI(first_gid, False))
        self.set_context(client, first_gid)

        # We have to be in destination group for link Save to work
        self.doAllChgrp(requests, client)

        # ...check image
        img = client.sf.getQueryService().get("Image", img.id.val)
        self.assertEqual(img.details.group.id.val, first_gid)
        # check Dataset
        query = "select link from DatasetImageLink link where link.child.id=%s" % img.id.val
        l = client.sf.getQueryService().findByQuery(query, None)
        self.assertTrue(l is not None, "New DatasetImageLink on image not found")
        self.assertEqual(l.details.group.id.val, first_gid, "Link Created in same group as Image target")
        

    def testChgrpPDI(self):
        """
        Tests chgrp for a Project, Dataset, Image hierarchy
        """
        # One user in two groups
        client, exp = self.new_client_and_user()
        grp = self.new_group([exp])
        gid = grp.id.val
        client.sf.getAdminService().getEventContext() # Reset session
        update = client.sf.getUpdateService()
        query = client.sf.getQueryService()

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
        l.setChild(dataset)
        l.setParent(project)
        links.append(l)
        update.saveAndReturnArray(links)

        # Move Project to new group
        chgrp = omero.cmd.Chgrp(type="/Project", id=project.id.val, options=None, grp=gid)
        self.doSubmit(chgrp, client)

        # Change our context to new group...
        admin = client.sf.getAdminService()
        admin.setDefaultGroup(exp, omero.model.ExperimenterGroupI(gid, False))
        self.set_context(client, gid)
        # ...check image
        img = client.sf.getQueryService().get("Image", img.id.val)
        self.assertEqual(img.details.group.id.val, gid)
        # check Project
        prj = client.sf.getQueryService().get("Project", project.id.val)
        self.assertEqual(prj.details.group.id.val, gid)

    def testChgrpRdef7825(self):

        # One user in two groups
        owner, owner_obj = self.new_client_and_user(perms="rwrw--")
        admin = owner.sf.getAdminService()
        ec = admin.getEventContext()
        source_grp = admin.getGroup(ec.groupId)

        target_grp = self.new_group([owner])
        target_gid = target_grp.id.val

        ec = admin.getEventContext() # Refresh

        # Add another user to the source group
        member = self.new_client(group=source_grp)

        # Create an image as the owner
        image = self.createTestImage(session=owner.sf)

        # Render as both users
        owner_g = omero.gateway.BlitzGateway(client_obj=owner)
        member_g = omero.gateway.BlitzGateway(client_obj=member)
        def render(g):
            g.getObject("Image", image.id.val).getThumbnail()
        render(owner_g)
        render(member_g)

        # Now chgrp and try to delete
        chgrp = omero.cmd.Chgrp(type="/Image", id=image.id.val, grp=target_gid)
        self.doSubmit(chgrp, owner)

        # Shouldn't be necessary to change group, but we're gonna
        owner_g.SERVICE_OPTS.setOmeroGroup("-1")
        handle = owner_g.deleteObjects("/Image", [image.id.val])
        self.waitOnCmd(owner_g.c, handle)


    def testBadCaseChgrpOneImage(self):
        """
        Simple example of the MIF chgrp bad case:
        A single fileset containing 2 images - we try to chgrp ONE image.
        Each sibling CANNOT be moved independently of the other.
        """
        # One user in two groups
        client, user = self.new_client_and_user(perms=PRIVATE)
        admin = client.sf.getAdminService()
        target_grp = self.new_group([user],perms=PRIVATE)
        target_gid = target_grp.id.val

        # 2 images sharing a fileset
        images = self.importMIF(2, client=client)

        # Lookup the fileset
        img = client.sf.getQueryService().get('Image', images[0].id.val)    # load first image
        filesetId =  img.fileset.id.val

        # Now chgrp
        chgrp = omero.cmd.Chgrp(type="/Image", id=images[0].id.val, grp=target_gid)
        rsp = self.doSubmit(chgrp, client, test_should_pass=False)

        # The chgrp should fail due to the fileset
        self.assertTrue('Fileset' in rsp.constraints, "chgrp should fail due to 'Fileset' constraints")
        failedFilesets = rsp.constraints['Fileset']
        self.assertEqual(len(failedFilesets), 1, "chgrp should fail due to a single Fileset")
        self.assertEqual(failedFilesets[0], filesetId, "chgrp should fail due to this Fileset")


    def testBadCaseChgrpAllImages(self):
        """
        Simple example of the MIF chgrp bad case:
        A single fileset containing 2 images
        can be moved to the same group together.
        """
        # One user in two groups
        client, user = self.new_client_and_user(perms=PRIVATE)
        admin = client.sf.getAdminService()
        target_grp = self.new_group([user],perms=PRIVATE)
        target_gid = target_grp.id.val

        images = self.importMIF(2, client=client)

        # chgrp should succeed
        chgrp1 = omero.cmd.Chgrp(type="/Image", id=images[0].id.val, grp=target_gid)
        chgrp2 = omero.cmd.Chgrp(type="/Image", id=images[1].id.val, grp=target_gid)
        self.doAllChgrp([chgrp1,chgrp2], client)


    def testBadCaseChgrpOneDataset(self):
        """
        Simple example of the MIF chgrp bad case:
        A single fileset containing 2 images is split among 2 datasets.
        We try to chgrp ONE Dataset.
        Each dataset CANNOT be moved independently of the other.
        """
        # One user in two groups
        client, user = self.new_client_and_user(perms=PRIVATE)
        admin = client.sf.getAdminService()
        target_grp = self.new_group([user],perms=PRIVATE)
        target_gid = target_grp.id.val

        update = client.sf.getUpdateService()
        datasets = self.createDatasets(2, "testBadCaseChgrpOneDataset", client=client)
        images = self.importMIF(2, client=client)
        for i in range(2):
            link = omero.model.DatasetImageLinkI()
            link.setParent(datasets[i])
            link.setChild(images[i])
            link = update.saveAndReturnObject(link)

        # Lookup the fileset
        img = client.sf.getQueryService().get('Image', images[0].id.val)    # load first image
        filesetId =  img.fileset.id.val

        # chgrp should fail...
        chgrp = omero.cmd.Chgrp(type="/Dataset", id=datasets[0].id.val, grp=target_gid)
        rsp = self.doSubmit(chgrp, client, test_should_pass=False)

        # ...due to the fileset
        self.assertTrue('Fileset' in rsp.constraints, "chgrp should fail due to 'Fileset' constraints")
        failedFilesets = rsp.constraints['Fileset']
        self.assertEqual(len(failedFilesets), 1, "chgrp should fail due to a single Fileset")
        self.assertEqual(failedFilesets[0], filesetId, "chgrp should fail due to this Fileset")


    def testBadCaseChgrpAllDatasets(self):
        """
        Simple example of the MIF chgrp bad case:
        a single fileset containing 2 images is split among 2 datasets.
        Datasets can be moved to the same group together.
        """
        # One user in two groups
        client, user = self.new_client_and_user(perms=PRIVATE)
        admin = client.sf.getAdminService()
        target_grp = self.new_group([user],perms=PRIVATE)
        target_gid = target_grp.id.val

        update = client.sf.getUpdateService()
        datasets = self.createDatasets(2, "testBadCaseChgrpAllDatasets", client=client)
        images = self.importMIF(2, client=client)
        for i in range(2):
            link = omero.model.DatasetImageLinkI()
            link.setParent(datasets[i])
            link.setChild(images[i])
            link = update.saveAndReturnObject(link)

        # Now chgrp, should succeed
        chgrp1 = omero.cmd.Chgrp(type="/Dataset", id=datasets[0].id.val, grp=target_gid)
        chgrp2 = omero.cmd.Chgrp(type="/Dataset", id=datasets[1].id.val, grp=target_gid)
        self.doAllChgrp([chgrp1,chgrp2], client)

        # Check both Datasets and Images moved
        queryService = client.sf.getQueryService()
        ctx = {'omero.group': str(target_gid)}      # query in the target group
        for i in range(2):
            dataset = queryService.get('Dataset', datasets[i].id.val, ctx)
            image = queryService.get('Image', images[i].id.val, ctx)
            self.assertEqual(target_gid, dataset.details.group.id.val, "Dataset should be in group: %s" % target_gid)
            self.assertEqual(target_gid, image.details.group.id.val, "Image should be in group: %s" % target_gid)


    def testGoodCaseChgrpDataset(self):
        """
        Simple example of the MIF chgrp good case:
        a single fileset containing 2 images in one dataset.
        The dataset can be moved.
        """
        # One user in two groups
        client, user = self.new_client_and_user(perms=PRIVATE)
        admin = client.sf.getAdminService()
        target_grp = self.new_group([user],perms=PRIVATE)
        target_gid = target_grp.id.val

        update = client.sf.getUpdateService()
        ds = omero.model.DatasetI()
        ds.name = rstring("testGoodCaseChgrpDataset")
        ds = update.saveAndReturnObject(ds)
        images = self.importMIF(2, client=client)
        for i in range(2):
            link = omero.model.DatasetImageLinkI()
            link.setParent(ds)
            link.setChild(images[i])
            link = update.saveAndReturnObject(link)

        # Now chgrp, should succeed
        chgrp = omero.cmd.Chgrp(type="/Dataset", id=ds.id.val, grp=target_gid)
        self.doSubmit(chgrp, client)

        # Check Dataset and both Images moved
        queryService = client.sf.getQueryService()
        ctx = {'omero.group': '-1'}      # query across groups
        dataset = queryService.get('Dataset', ds.id.val, ctx)
        self.assertEqual(target_gid, dataset.details.group.id.val, "Dataset should be in group: %s" % target_gid)
        for i in range(2):
            image = queryService.get('Image', images[i].id.val, ctx)
            img_gid = image.details.group.id.val
            self.assertEqual(target_gid, img_gid, "Image should be in group: %s, NOT %s" % (target_gid, img_gid))

    """
    Simple example of the MIF chgrp good case:
    a single fileset containing 2 images in one dataset.
    Each sibling CANNOT be moved independently of the other.
    """
    def testGoodCaseChgrpOneImage(self):
        # One user in two groups
        client, user = self.new_client_and_user(perms=PRIVATE)
        admin = client.sf.getAdminService()
        #admin.getEventContext()
        target_grp = self.new_group([user],perms=PRIVATE)
        target_gid = target_grp.id.val

        update = client.sf.getUpdateService()
        ds = omero.model.DatasetI()
        ds.name = rstring("testGoodCaseChgrpOneImage")
        ds = update.saveAndReturnObject(ds)
        images = self.importMIF(2, client=client)
        for i in range(2):
            link = omero.model.DatasetImageLinkI()
            link.setParent(ds)
            link.setChild(images[i])
            link = update.saveAndReturnObject(link)

        # Now chgrp
        chgrp = omero.cmd.Chgrp(type="/Image", id=images[0].id.val, grp=target_gid)
        self.doSubmit(chgrp, client)

        # The chgrp should fail.
        # What do we need to assert here? Will an exception be raised?

    """
    Simple example of the MIF chgrp good case:
    a single fileset containing 2 images in one dataset.
    Each sibling CANNOT be moved independently of the other,
    but they can be moved to the same group together.
    """
    def testGoodCaseChgrpAllImages(self):
        # One user in two groups
        client, user = self.new_client_and_user(perms=PRIVATE)
        admin = client.sf.getAdminService()
        #admin.getEventContext()
        target_grp = self.new_group([user],perms=PRIVATE)
        target_gid = target_grp.id.val

        update = client.sf.getUpdateService()
        ds = omero.model.DatasetI()
        ds.name = rstring("testGoodCaseChgrpAllImages")
        ds = update.saveAndReturnObject(ds)
        images = self.importMIF(2, client=client)
        for i in range(2):
            link = omero.model.DatasetImageLinkI()
            link.setParent(ds)
            link.setChild(images[i])
            link = update.saveAndReturnObject(link)

        # Now chgrp
        chgrp1 = omero.cmd.Chgrp(type="/Image", id=images[0].id.val, grp=target_gid)
        chgrp2 = omero.cmd.Chgrp(type="/Image", id=images[1].id.val, grp=target_gid)
        self.doAllChgrp([chgrp1,chgrp2], client)

        # Is this case actually possible? Should the chgrp succeed?
        # What do we need to assert here?

if __name__ == '__main__':
    unittest.main()
