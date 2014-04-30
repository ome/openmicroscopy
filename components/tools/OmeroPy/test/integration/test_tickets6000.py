#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2011-2014 Glencoe Software, Inc. All Rights Reserved.
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
    Integration tests for tickets between 5000 and 5999

"""

import pytest
import test.integration.library as lib

from omero.rtypes import rstring


class TestTickets6000(lib.ITest):

    @pytest.mark.xfail(reason="See ticket #11539")
    def test5684(self):
        """
        Similar to integration.tickets4000.TestTickets4000.test3138
        but here we check that using a valid session UUID does *not*
        cause a wait time.

        Note: this issue only appeared initially while running with
        LDAP enabled.
        """
        client, user = self.new_client_and_user()
        uuid = client.getSessionId()
        name = user.omeName.val

        admin = self.root.sf.getAdminService()
        admin.changeUserPassword(name, rstring("GOOD"))

        # First real password attempt is fast
        self.loginAttempt(name, 0.15, "GOOD", less=True)

        # First attempt with UUID is fast
        self.loginAttempt(uuid, 0.15, pw=uuid, less=True)

        # Second attempt with UUID should still be fast
        self.loginAttempt(uuid, 0.15, pw=uuid, less=True)

        print client.sf
        print uuid
