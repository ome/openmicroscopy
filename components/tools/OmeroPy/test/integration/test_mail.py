#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2015 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# This program is free software you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

"""
   Integration test for mail testing. Requires that the server
   have been deployed with the fake smtp server as described in
   etc/spring-cfg/mail.xml
"""

import library as lib
import pytest
import omero


class TestMail(lib.ITest):

    def skipIfNot(self):
        cfg = self.root.sf.getConfigService()
        cfg = cfg.getConfigValue("omero.mail.fake")
        if "true" != str(cfg).lower():
            pytest.skip("omero.mail.fake not configured")

    def mailRequest(self, subject, body, everyone=False):
        req = omero.cmd.SendEmailRequest()
        req.subject = subject
        req.body = body
        req.everyone = everyone
        return req

    def assertMail(self, uuid):
        q = self.root.sf.getQueryService()
        assert q.findByQuery((
            "select a from MapAnnotation a where "
            "a.description like '%%%s\n'") % uuid, None)

    def testEveryone(self):
        self.skipIfNot()
        uuid = self.uuid()
        req = self.mailRequest(subject=uuid,
                               body="test",
                               everyone=True)
        try:
            self.root.submit(req)
        except omero.CmdError, ce:
            raise Exception(ce.err)

        self.assertMail(uuid)
