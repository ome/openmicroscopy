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
import time

PRIVATE = 'rw----'
READONLY = 'rwr---'
READANN = 'rwra--'
READWRITE = 'rwrw--'

import gatewaytest.library as lib


class HistoryTest (lib.GTest):

    def setUp (self):
        """ Create a group with User """
        dbhelpers.USERS['history_test_user'] = dbhelpers.UserEntry('history_test_user','ome', firstname='history', lastname='user',
                   groupname="rw_history", groupperms=READWRITE)
        # Calling the superclass setUp processes the dbhelpers.USERS etc to populate DB
        super(HistoryTest, self).setUp()


    def searchHistory(self, start, end, dtype="Dataset"):
        
        tm = self.gateway.getTimelineService()
        count = tm.countByPeriod([dtype], rtime(long(start)), rtime(long(end)), None, self.gateway.CONFIG['SERVICE_OPTS'])
        data = tm.getByPeriod([dtype], rtime(long(start)), rtime(long(end)), None, True, self.gateway.CONFIG['SERVICE_OPTS'])
        
        logs = tm.getEventLogsByPeriod(rtime(start), rtime(end), None, self.gateway.CONFIG['SERVICE_OPTS'])
        entityType = 'ome.model.containers.%s' % dtype
        filteredLogs = [{'id':i.entityId.val, 'action': i.action.val} for i in logs if i.entityType.val == entityType]

        typeCount = count[dtype]
        dataCount = len(data[dtype])
        logCount = len(filteredLogs)
        
        self.assertEqual(typeCount, dataCount, "Period count should match number of objects")
        self.assertEqual(logCount, dataCount, "Logs count should match number of objects")


    def testCreateHistory(self):
        
        # Login as user...
        self.doLogin(dbhelpers.USERS['history_test_user'])
        userId = self.gateway.getEventContext().userId
        uuid = self.gateway.getEventContext().sessionUuid
        default_groupId = self.gateway.getEventContext().groupId
        
        start = int(round(time.time() * 1000)) - 1000

        # Create Dataset in 'default' group
        update = self.gateway.getUpdateService()
        new_ds = omero.model.DatasetI()
        dataset_name = "history_test_%s" % uuid
        new_ds.name = rstring(dataset_name)
        new_ds = update.saveAndReturnObject(new_ds)
        new_ds_Id = new_ds.id.val
        
        # As Admin, create a second group with this user & upload script
        self.loginAsAdmin()
        gid = self.gateway.createGroup("history-test-%s" % uuid, member_Ids=[userId], perms=READWRITE)

        # login as User
        self.doLogin(dbhelpers.USERS['history_test_user'])

        end = int(round(time.time() * 1000)) + 1000
        self.searchHistory(start, end)
        
        # switch user into new group
        switched = self.gateway.c.sf.setSecurityContext(omero.model.ExperimenterGroupI(gid, False))
        self.assertTrue(switched, "Failed to switch into new group")
        # Shouldn't be able to access Dataset...
        self.searchHistory(start, end)
        self.assertEqual(None, self.gateway.getObject("Dataset", new_ds_Id))
        self.gateway.CONFIG['SERVICE_OPTS'] = {'omero.group':str(default_groupId)}
        self.assertNotEqual(None, self.gateway.getObject("Dataset", new_ds_Id))

        self.searchHistory(start, end)
        
        # Create Another Dataset in new group
        update = self.gateway.getUpdateService()
        new_ds = omero.model.DatasetI()
        dataset_name = "history_test_%s" % uuid
        new_ds.name = rstring(dataset_name)
        new_ds = update.saveAndReturnObject(new_ds)
        new_ds_Id = new_ds.id.val
        
        end = int(round(time.time() * 1000)) + 1000
        self.searchHistory(start, end)


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
        print "Running test..."     # generate stdout
        try:
            dataset = qs.find("Dataset", ds_Id)
            ds_Name = dataset.name.val
            print ds_Name
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
        process = svc.runScript(scriptID, wrap({"datasetId":new_ds_Id}).val,
                None, self.gateway.CONFIG['SERVICE_OPTS'])
        cb = omero.scripts.ProcessCallbackI(self.gateway.c, process)
        while cb.block(500) is None:
            pass
        results = process.getResults(0)
        self.assertTrue('stdout' in results, "Failed to return stdout Original File. #8614")
        self.assertEqual(results["gid"].val, default_groupId, \
                "We want script to have eventContext of group:%s not %s" % \
                (default_groupId, results["gid"].val))
        self.assertEqual(results["datasetName"].val, dataset_name, \
                "Script should be able to access Dataset")


if __name__ == '__main__':
    unittest.main()
