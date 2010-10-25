#!/usr/bin/env python

"""
   Integration tests for tickets between 3000 and 3999

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import unittest, time
import integration.library as lib
import Glacier2

from omero.rtypes import *


class TestTickets4000(lib.ITest):

    def test3138(self):
        """
        Try multiple logins to see if they slow down
        """
        user = self.new_user()
        name = user.omeName.val

        self.root.sf.getAdminService().changeUserPassword(name, rstring("GOOD"))

        self.loginAttempt(name, 0.1, less=True)
        self.loginAttempt(name, 3.0)
        self.loginAttempt(name, 0.1, "GOOD", less=True)
        self.loginAttempt(name, 0.1, less=True)
        self.loginAttempt(name, 3.0)

    def loginAttempt(self, name, t, pw="BAD", less=False):
        c = omero.client()
        try:
            t1 = time.time()
            try:
                c.createSession(name, pw)
                if pw == "BAD":
                    self.fail("Should not reach this point")
            except Glacier2.PermissionDeniedException:
                if pw != "BAD":
                    raise
            t2 = time.time()
            T = (t2-t1)
            if less:
                self.assertTrue(T < t, "%s > %s" % (T, t))
            else:
                self.assertTrue(T > t, "%s < %s" % (T, t))
        finally:
            c.__del__()

    def testChangeActiveGroup(self):
        admin = self.client.sf.getAdminService()

        self.assertEquals(2, len(admin.getEventContext().memberOfGroups))

        # AS ROOT: adding user to extra group
        admin_root = self.root.sf.getAdminService()
        exp = admin_root.getExperimenter(admin.getEventContext().userId)
        grp = self.new_group()
        admin_root.addGroups(exp, [grp])

        self.assertEquals(3, len(admin.getEventContext().memberOfGroups))

        proxies = dict()
        # creating stateful services
        proxies['search'] = self.client.sf.createSearchService()
        proxies['thumbnail'] = self.client.sf.createThumbnailStore()
        proxies['admin'] = self.client.sf.getAdminService()

        # changing group
        for k in proxies.keys():
            try:
                proxies[k].close()
            except AttributeError:
                pass

        self.client.sf.setSecurityContext(omero.model.ExperimenterGroupI(grp.id.val, False))
        admin.setDefaultGroup(admin.getExperimenter(admin.getEventContext().userId), omero.model.ExperimenterGroupI(grp.id.val, False))
        self.assertEquals(grp.id.val, self.client.sf.getAdminService().getEventContext().groupId)

    def testChageActiveGroupWhenConnectionLost(self):
        import os
        admin = self.client.sf.getAdminService()
        uuid = self.client.sf.getAdminService().getEventContext().sessionUuid
        self.assertEquals(2, len(admin.getEventContext().memberOfGroups))

        # AS ROOT: adding user to extra group
        admin_root = self.root.sf.getAdminService()
        exp = admin_root.getExperimenter(admin.getEventContext().userId)
        grp = self.new_group()
        admin_root.addGroups(exp, [grp])

        self.assertEquals(3, len(admin.getEventContext().memberOfGroups))

        proxies = dict()
        # creating stateful services
        proxies['search'] = self.client.sf.createSearchService() #1A
        proxies['thumbnail'] = self.client.sf.createThumbnailStore() #1B
        proxies['admin'] = self.client.sf.getAdminService()
        copy = dict(proxies)

        # loosing the connection
        # ...

        # joining session

        c = omero.client(pmap=['--Ice.Config='+(os.environ.get("ICE_CONFIG"))])
        host = c.ic.getProperties().getProperty('omero.host')
        port = int(c.ic.getProperties().getProperty('omero.port'))
        c = omero.client(host=host, port=port)
        sf = c.joinSession(uuid)

        # retriving stateful services
        proxies['search'] = sf.createSearchService() #2A
        proxies['thumbnail'] = sf.createThumbnailStore() #2B
        proxies['admin'] = sf.getAdminService()

        # changing group
        for k in proxies.keys():
            prx = proxies[k]
            if isinstance(prx, omero.api.StatefulServiceInterfacePrx):
                prx.close()

        try:
            sf.setSecurityContext(omero.model.ExperimenterGroupI(grp.id.val, False))
            self.fail("""
            A security violation must be thrown here because the first instances
            which are stored in proxies (#1A and #1B) are never closed since #2A
            and #2B overwrite them. Using the copy instance, we can close them.
            """)
        except omero.SecurityViolation, sv:
            pass


        for k in copy.keys():
            prx = copy[k]
            if isinstance(prx, omero.api.StatefulServiceInterfacePrx):
                prx.close()

        sf.setSecurityContext(omero.model.ExperimenterGroupI(grp.id.val, False))

        ec = admin.getEventContext()
        sf.getAdminService().setDefaultGroup(sf.getAdminService().getExperimenter(ec.userId), omero.model.ExperimenterGroupI(grp.id.val, False))
        self.assertEquals(grp.id.val, ec.groupId)


if __name__ == '__main__':
    unittest.main()
