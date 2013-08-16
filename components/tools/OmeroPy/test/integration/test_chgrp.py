#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2012 University of Dundee & Open Microscopy Environment.
#                      All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

import omero, omero.gateway
import test.integration.library as lib
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


if __name__ == '__main__':
    unittest.main()
