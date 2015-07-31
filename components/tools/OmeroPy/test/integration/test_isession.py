#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2008-2014 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
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
   Integration test focused on the omero.api.ISession interface.

"""
import os

import library as lib
import pytest
import traceback
import omero

from omero.rtypes import rint
from omero.rtypes import rlong
from omero.rtypes import rstring
from omero.rtypes import unwrap

from omero.cmd import UpdateSessionTimeoutRequest


class TestISession(lib.ITest):

    def testBasicUsage(self):
        self.client.sf.getSessionService()
        admin = self.client.sf.getAdminService()
        admin.getEventContext().sessionUuid

    def testManuallyClosingOwnSession(self):
        client = self.new_client()
        client.killSession()

    def testCreateSessionForUser(self):
        p = omero.sys.Parameters()
        p.theFilter = omero.sys.Filter()
        p.theFilter.limit = rint(1)
        user = self.root.sf.getQueryService().findByQuery(
            """
            select e from Experimenter e
            where e.id > 0 and e.omeName != 'guest'
            """, p)
        p = omero.sys.Principal()
        p.name = user.omeName.val
        p.group = "user"
        p.eventType = "Test"
        sess = self.root.sf.getSessionService().createSessionWithTimeout(
            p, 10000)  # 10 secs

        client = omero.client()  # ok rather than new_client since has __del__
        try:
            user_sess = client.createSession(sess.uuid, sess.uuid)
            uuid = user_sess.getAdminService().getEventContext().sessionUuid
            assert sess.uuid.val == uuid
            client.closeSession()
        finally:
            client.__del__()

    def testJoinSession_Helper(self):
        test_user = self.new_user()

        client = omero.client()  # ok rather than new_client since has __del__
        try:
            sf = client.createSession(test_user.omeName.val,
                                      test_user.omeName.val)
            a = sf.getAdminService()
            suuid = a.getEventContext().sessionUuid
            sf.detachOnDestroy()
            return suuid
        finally:
            client.__del__()

    def testJoinSession(self):
        suuid = self.testJoinSession_Helper()
        c1 = omero.client()  # ok rather than new_client since has __del__
        try:
            sf1 = c1.joinSession(suuid)
            a1 = sf1.getAdminService()
            s1uuid = a1.getEventContext().sessionUuid
            assert s1uuid == suuid
        finally:
            c1.__del__()

    @pytest.mark.parametrize("who", (
        ("root", -1, None),
        ("root", 1, None),
        ("user", -1, None),
        ("baduser", 1, None)))
    def testUpdateSessions(self, who):
        who, idlediff, livediff = who
        if who.startswith("root"):
            client = self.root
        else:
            client = self.client

        uuid = client.getSessionId()
        service = client.sf.getSessionService()
        obj_before = service.getSession(uuid)
        live = unwrap(obj_before.timeToLive)
        idle = unwrap(obj_before.timeToIdle)

        req = UpdateSessionTimeoutRequest()
        req.session = uuid
        if livediff is not None:
            req.timeToLive = rlong(live+livediff)
        if idlediff is not None:
            req.timeToIdle = rlong(idle+idlediff)
        try:
            cb = client.submit(req)
            cb.getResponse()
            cb.close(True)
            assert not who.startswith("bad")  # must throw
        except omero.CmdError, ce:
            if who.startswith("bad"):
                assert ce.err.name == "non-admin-increase"
                return
            else:
                print ce.err.parameters.get("stacktrace")
                raise Exception(ce.err.category,
                                ce.err.name)

        obj_after = client.sf.getQueryService().findByQuery(
            ("select s from Session s "
             "where s.id = %s") % obj_before.id.val, None)
        assert obj_before.id == obj_after.id
        assert obj_before.uuid == obj_after.uuid
        assert req.timeToLive is None \
            or req.timeToLive == obj_after.timeToLive
        assert req.timeToIdle is None \
            or req.timeToIdle == obj_after.timeToIdle

        # Now try again! (required SessionManager fix)
        obj_after = service.getSession(uuid)
        assert req.timeToIdle.val == obj_after.timeToIdle.val

    def testCreateSessionForGuest(self):
        p = omero.sys.Principal()
        p.name = "guest"
        p.group = "guest"
        p.eventType = "User"
        sess = self.root.sf.getSessionService().createSessionWithTimeout(
            p, 10000)  # 10 secs
        guest_client = omero.client()
        guest_client.joinSession(sess.uuid.val)
        guest_client.closeSession()

    def test1018CreationDestructionClosing(self):
        c1, c2, c3, c4 = None, None, None, None
        try:
            c1 = omero.client()  # ok rather than new_client since has __del__
            user = self.new_user()
            s1 = c1.createSession(user.omeName.val, user.omeName.val)
            s1.detachOnDestroy()
            uuid = s1.ice_getIdentity().name

            # Intermediate "disrupter"
            c2 = omero.client()  # ok rather than new_client since has __del__
            s2 = c2.createSession(uuid, uuid)
            s2.closeOnDestroy()
            s2.getAdminService().getEventContext()
            c2.closeSession()

            # 1 should still be able to continue
            s1.getAdminService().getEventContext()

            # Now if s1 exists another session should be able to connect
            c1.closeSession()
            c3 = omero.client()  # ok rather than new_client since has __del__
            s3 = c3.createSession(uuid, uuid)
            s3.closeOnDestroy()
            s3.getAdminService().getEventContext()

            # Guarantee that the session is closed.
            c3.killSession()

            # Now a connection should not be possible
            c4 = omero.client()  # ok rather than new_client since has __del__
            import Glacier2
            with pytest.raises(Glacier2.PermissionDeniedException):
                c4.createSession(uuid, uuid)
        finally:
            for c in (c1, c2, c3, c4):
                if c:
                    c.__del__()

    def testSimpleDestruction(self):
        c = omero.client()  # ok rather than new_client since has __del__
        try:
            c.ic.getImplicitContext().put(
                omero.constants.CLIENTUUID, "SimpleDestruction")
            user = self.new_user()
            s = c.createSession(user.omeName.val, user.omeName.val)
            s.closeOnDestroy()
            c.closeSession()
        finally:
            c.__del__()

    def testGetMySessionsTicket1975(self):
        svc = self.client.sf.getSessionService()
        svc.getMyOpenSessions()
        svc.getMyOpenAgentSessions("OMERO.web")
        svc.getMyOpenClientSessions()

    def testTicket2196SetSecurityContext(self):
        ec = self.client.sf.getAdminService().getEventContext()
        exp0 = omero.model.ExperimenterI(ec.userId, False)
        grp0 = omero.model.ExperimenterGroupI(ec.groupId, False)
        grp1 = self.new_group([exp0])

        # Change: should pass
        # Force reload #4011
        self.client.sf.getAdminService().getEventContext()
        self.client.sf.setSecurityContext(grp1)

        # Make a stateful service, and change again
        rfs = self.client.sf.createRawFileStore()
        with pytest.raises(omero.SecurityViolation):
            self.client.sf.setSecurityContext(grp0)
        rfs.close()

        # Service is now closed, should be ok
        self.client.sf.setSecurityContext(grp1)

    def testManageMySessions(self):
        adminCtx = self.client.sf.getAdminService().getEventContext()
        username = adminCtx.userName
        group = adminCtx.groupName

        self.root.sf.getAdminService().lookupExperimenter(username)
        p = omero.sys.Principal()
        p.name = username
        p.group = group
        p.eventType = "User"
        newConnId = self.root.sf.getSessionService().createSessionWithTimeout(
            p, 60000)

        # ok rather than new_client since has __del__
        c1 = omero.client(
            pmap=['--Ice.Config=' + (os.environ.get("ICE_CONFIG"))])
        try:
            host = c1.ic.getProperties().getProperty('omero.host')
            port = int(c1.ic.getProperties().getProperty('omero.port'))
            c1.__del__()  # Just used for parsing

            # ok rather than new_client since has __del__
            c1 = omero.client(host=host, port=port)
            s = c1.joinSession(newConnId.getUuid().val)
            s.detachOnDestroy()

            svc = self.client.sf.getSessionService()

            for s in svc.getMyOpenSessions():
                if (adminCtx.sessionUuid != s.uuid.val
                        and s.defaultEventType.val
                        not in ('Internal', 'Sessions')):
                    cc = None
                    try:
                        try:
                            # ok rather than new_client since has __del__
                            cc = omero.client(host, port)
                            cc.joinSession(s.uuid.val)
                            cc.killSession()
                        except:
                            self.assertRaises(traceback.format_exc())
                    finally:
                        cc.__del__()

            for s in svc.getMyOpenSessions():
                assert s.uuid.val != newConnId.getUuid().val
        finally:
            c1.__del__()

    def testSessionWithIP(self):
        c1 = omero.client(
            pmap=['--Ice.Config='+(os.environ.get("ICE_CONFIG"))])
        try:
            host = c1.ic.getProperties().getProperty('omero.host')
            port = int(c1.ic.getProperties().getProperty('omero.port'))
            rootpass = c1.ic.getProperties().getProperty('omero.rootpass')
        finally:
            c1.__del__()

        c = omero.client(host=host, port=port)
        try:
            c.setAgent("OMERO.py.root_test")
            c.setIP("127.0.0.1")
            s = c.createSession("root", rootpass)

            p = omero.sys.ParametersI()
            p.map = {}
            p.map["uuid"] = rstring(
                s.getAdminService().getEventContext().sessionUuid)
            res = s.getQueryService().findByQuery(
                "from Session where uuid=:uuid", p)

            assert "127.0.0.1" == res.getUserIP().val

            s.closeOnDestroy()
            c.closeSession()
        finally:
            c.__del__()
