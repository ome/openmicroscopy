#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2011-2014 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

"""
Integration tests for tickets between 5000 and 5999
"""

import pytest
import library as lib

from omero.rtypes import rstring


class TestTickets6000(lib.ITest):

    @pytest.mark.broken(ticket="11539")
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
