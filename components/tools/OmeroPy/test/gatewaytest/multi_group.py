#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2012 University of Dundee & Open Microscopy Environment.
#                      All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

import omero
import omero.scripts
import unittest
from omero.rtypes import *
from omero.cmd import *
from omero.callbacks import CmdCallbackI
from omero.gateway import BlitzGateway
from omero.gateway.scripts import dbhelpers

PRIVATE = 'rw----'
READONLY = 'rwr---'
READANN = 'rwra--'
READWRITE = 'rwrw--'

import gatewaytest.library as lib



class ScriptTest (lib.GTest):

    def setUp (self):
        """ Create a group with User """
        dbhelpers.USERS['script_test_user'] = dbhelpers.UserEntry('script_test_user','ome', firstname='script', lastname='user',
                   groupname="rw_script", groupperms=READWRITE)
        # Calling the superclass setUp processes the dbhelpers.USERS etc to populate DB
        super(ScriptTest, self).setUp()


    def testRunScript(self):
        
        # Login as user...
        self.doLogin(dbhelpers.USERS['script_test_user'])
        userId = self.gateway.getEventContext().userId
        uuid = self.gateway.getEventContext().sessionUuid
        default_groupId = self.gateway.getEventContext().groupId
        # Create Dataset in 'default' group
        update = self.gateway.getUpdateService()
        new_ds = omero.model.DatasetI()
        dataset_name = "script_test_%s" % uuid
        new_ds.name = rstring(dataset_name)
        new_ds = update.saveAndReturnObject(new_ds)
        new_ds_Id = new_ds.id.val
        
        # As Admin, create a second group with this user & upload script
        self.loginAsAdmin()
        gid = self.gateway.createGroup("script-test-%s" % uuid, member_Ids=[userId], perms=READWRITE)

        SCRIPT = """if True:
        import omero.scripts
        import omero.rtypes
        client = omero.scripts.client("ticket8573", \
                omero.scripts.Long("datasetId"), \
                omero.scripts.String("datasetName", out=True))
        ec = client.sf.getAdminService().getEventContext()
        gid = ec.groupId
        qs = client.sf.getQueryService()
        ds_Id = client.getInput("datasetId").getValue()
        try:
            dataset = qs.find("Dataset", ds_Id)
            ds_Name = dataset.name.val
        except:
            ds_Name = "Not Found"
        client.setOutput("gid", omero.rtypes.rlong(gid))
        client.setOutput("datasetName", omero.rtypes.rstring(ds_Name))
        """
        svc = self.gateway.getScriptService()
        scriptID = svc.uploadOfficialScript("/test/ticket8573/%s" % uuid, SCRIPT)

        # switch user into new group
        self.doLogin(dbhelpers.USERS['script_test_user'])
        switched = self.gateway.c.sf.setSecurityContext(omero.model.ExperimenterGroupI(gid, False))
        self.assertTrue(switched, "Failed to switch into new group")
        # Shouldn't be able to access Dataset...
        self.assertEqual(None, self.gateway.getObject("Dataset", new_ds_Id))
        self.gateway.CONFIG['SERVICE_OPTS'] = {'omero.group':str(default_groupId)}
        self.assertNotEqual(None, self.gateway.getObject("Dataset", new_ds_Id))

        # run script
        svc = self.gateway.getScriptService()
        process = svc.runScript(scriptID, wrap({"datasetId":new_ds_Id}).val, None)
        cb = omero.scripts.ProcessCallbackI(self.gateway.c, process)
        while cb.block(500) is None:
            pass
        results = process.getResults(0)
        self.assertEqual(results["gid"].val, default_groupId, "We want script to have eventContext of group:%s" % default_groupId)
        self.assertEqual(results["datasetName"].val, dataset_name, "Script should be able to access Dataset")


if __name__ == '__main__':
    unittest.main()