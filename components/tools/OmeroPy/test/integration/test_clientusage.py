#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2010-2014 Glencoe Software, Inc. All Rights Reserved.
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
   Tests for the demonstrating client usage

"""

import omero
import library as lib

from omero.rtypes import rstring


class TestClientUsage(lib.ITest):

    """
    Note: this is the only test which should use 'omero.client()'
    All others should use the new_client(user=) syntax from lib.ITest
    """

    def testClientClosedAutomatically(self):
        client = omero.client()
        user = self.new_user()
        client.createSession(user.omeName.val, user.omeName.val)
        client.getSession().closeOnDestroy()

    def testClientClosedManually(self):
        client = omero.client()
        user = self.new_user()
        client.createSession(user.omeName.val, user.omeName.val)
        client.getSession().closeOnDestroy()
        client.closeSession()

    def testUseSharedMemory(self):
        client = omero.client()
        user = self.new_user()
        client.createSession(user.omeName.val, user.omeName.val)

        assert 0 == len(client.getInputKeys())
        client.setInput("a", rstring("b"))
        assert 1 == len(client.getInputKeys())
        assert "a" in client.getInputKeys()
        assert "b" == client.getInput("a").getValue()

        client.closeSession()

    def testCreateInsecureClientTicket2099(self):
        secure = omero.client()
        assert secure.isSecure()
        try:
            user = self.new_user()
            s = secure.createSession(
                user.omeName.val, user.omeName.val)
            s.getAdminService().getEventContext()
            insecure = secure.createClient(False)
            try:
                insecure.getSession().getAdminService().getEventContext()
                assert not insecure.isSecure()
            finally:
                insecure.closeSession()
        finally:
            secure.closeSession()

    def testGetStatefulServices(self):
        root = self.root
        sf = root.sf
        sf.setSecurityContext(omero.model.ExperimenterGroupI(0, False))
        sf.createRenderingEngine()
        srvs = root.getStatefulServices()
        assert 1 == len(srvs)
        try:
            sf.setSecurityContext(omero.model.ExperimenterGroupI(1, False))
            assert False, "Should not be allowed"
        except:
            pass  # good
        srvs[0].close()
        srvs = root.getStatefulServices()
        assert 0 == len(srvs)
        sf.setSecurityContext(omero.model.ExperimenterGroupI(1, False))
