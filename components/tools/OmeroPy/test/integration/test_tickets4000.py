#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration tests for tickets between 3000 and 3999

   Copyright 2010-2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import omero
import library as lib
import pytest

from omero.rtypes import rstring


class TestTickets4000(lib.ITest):

    @pytest.mark.broken(ticket="11539")
    def test3138(self):
        """
        Try multiple logins to see if they slow down
        """
        user = self.new_user()
        name = user.omeName.val

        self.root.sf.getAdminService().changeUserPassword(
            name, rstring("GOOD"))

        self.loginAttempt(name, 0.15, less=True)
        self.loginAttempt(name, 3.0)
        self.loginAttempt(name, 0.15, "GOOD", less=True)
        self.loginAttempt(name, 0.15, less=True)
        self.loginAttempt(name, 3.0)

    def testChangeActiveGroup(self):
        client = self.new_client()
        admin = client.sf.getAdminService()

        assert 2 == len(admin.getEventContext().memberOfGroups)

        # AS ROOT: adding user to extra group
        admin_root = self.root.sf.getAdminService()
        exp = admin_root.getExperimenter(admin.getEventContext().userId)
        grp = self.new_group()
        admin_root.addGroups(exp, [grp])

        assert 3 == len(admin.getEventContext().memberOfGroups)

        proxies = dict()
        # creating stateful services
        proxies['search'] = client.sf.createSearchService()
        proxies['thumbnail'] = client.sf.createThumbnailStore()
        proxies['admin'] = client.sf.getAdminService()

        # changing group
        for k in proxies.keys():
            try:
                proxies[k].close()
            except AttributeError:
                pass

        client.sf.setSecurityContext(
            omero.model.ExperimenterGroupI(grp.id.val, False))
        admin.setDefaultGroup(admin.getExperimenter(
            admin.getEventContext().userId),
            omero.model.ExperimenterGroupI(grp.id.val, False))
        assert grp.id.val == \
            client.sf.getAdminService().getEventContext().groupId

    def testChangeActiveGroupWhenConnectionLost(self):
        import os
        client = self.new_client()
        admin = client.sf.getAdminService()
        uuid = client.sf.getAdminService().getEventContext().sessionUuid
        assert 2 == len(admin.getEventContext().memberOfGroups)

        # AS ROOT: adding user to extra group
        admin_root = self.root.sf.getAdminService()
        exp = admin_root.getExperimenter(admin.getEventContext().userId)
        grp = self.new_group()
        admin_root.addGroups(exp, [grp])

        assert 3 == len(admin.getEventContext().memberOfGroups)

        proxies = dict()
        # creating stateful services
        proxies['search'] = client.sf.createSearchService()  # 1A
        proxies['thumbnail'] = client.sf.createThumbnailStore()  # 1B
        proxies['admin'] = client.sf.getAdminService()
        copy = dict(proxies)

        # loosing the connection
        # ...

        # joining session

        c = omero.client(
            pmap=['--Ice.Config=' + (os.environ.get("ICE_CONFIG"))])
        host = c.ic.getProperties().getProperty('omero.host')
        port = int(c.ic.getProperties().getProperty('omero.port'))
        c = omero.client(host=host, port=port)
        sf = c.joinSession(uuid)

        # retriving stateful services
        proxies['search'] = sf.createSearchService()  # 2A
        proxies['thumbnail'] = sf.createThumbnailStore()  # 2B
        proxies['admin'] = sf.getAdminService()

        # changing group
        for k in proxies.keys():
            prx = proxies[k]
            if isinstance(prx, omero.api.StatefulServiceInterfacePrx):
                prx.close()

        """
        A security violation must be thrown here because the first instances
        which are stored in proxies (#1A and #1B) are never closed since #2A
        and #2B overwrite them. Using the copy instance, we can close them.
        """
        with pytest.raises(omero.SecurityViolation):
            sf.setSecurityContext(
                omero.model.ExperimenterGroupI(grp.id.val, False))

        for k in copy.keys():
            prx = copy[k]
            if isinstance(prx, omero.api.StatefulServiceInterfacePrx):
                prx.close()

        sf.setSecurityContext(
            omero.model.ExperimenterGroupI(grp.id.val, False))

        ec = admin.getEventContext()
        sf.getAdminService().setDefaultGroup(
            sf.getAdminService().getExperimenter(ec.userId),
            omero.model.ExperimenterGroupI(grp.id.val, False))
        assert grp.id.val == ec.groupId

    def test3201(self):
        import Glacier2

        def testLogin(username, password):
            import os
            c = omero.client(
                pmap=['--Ice.Config=' + (os.environ.get("ICE_CONFIG"))])
            host = c.ic.getProperties().getProperty('omero.host')
            port = int(c.ic.getProperties().getProperty('omero.port'))
            omero.client(host=host, port=port)
            sf = c.createSession(username, password)
            sf.getAdminService().getEventContext()
            c.closeSession()

        admin_root = self.root.sf.getAdminService()

        client = self.new_client()
        admin = client.sf.getAdminService()
        omeName = admin.getEventContext().userName

        # change password as user
        admin.changePassword(rstring("aaa"))

        testLogin(omeName, "aaa")

        # change password as root
        admin_root.changeUserPassword(omeName, rstring("ome"))

        with pytest.raises(Glacier2.PermissionDeniedException):
            testLogin(omeName, "aaa")

        testLogin(omeName, "ome")

        admin.changePasswordWithOldPassword(rstring("ome"), rstring("ccc"))

        testLogin(omeName, "ccc")

    def test3131(self):
        _ = omero.rtypes.rstring
        la = omero.model.LongAnnotationI()
        la.ns = _(self.uuid())
        la = self.update.saveAndReturnObject(la)
        la.ns = _(self.uuid())
        la = self.update.saveAndReturnObject(la)
        la.ns = _(self.uuid())
        la = self.update.saveAndReturnObject(la)
        assert -1 == la.details.updateEvent.session.sizeOfEvents()
