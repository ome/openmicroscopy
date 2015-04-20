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
   etc/blitz/mail-server.example
"""

import library as lib
import omero


class TestMail(lib.ITest):

    def skipIfNot(self):
        self.skip_if("omero.mail.fake", lambda x: str(x).lower() != "true",
                     message="omero.mail.fake not configured")

        # If active, we make sure there is an admin
        # who will definitely have an email
        a = self.root.sf.getAdminService()
        r = a.getSecurityRoles()
        q = self.root.sf.getQueryService()
        with_email = q.findAllByQuery((
            "select e from Experimenter e "
            "join e.groupExperimenterMap m "
            "join m.parent g where g.id = :id "
            "and length(e.email) > 0"),
            omero.sys.ParametersI().addId(r.systemGroupId))

        if not with_email:
            self.new_user(system=True, email="random_user@localhost")

    def mailRequest(self, subject, body, everyone=False):
        req = omero.cmd.SendEmailRequest()
        req.subject = subject
        req.body = body
        req.everyone = everyone
        return req

    def assertMail(self, text):
        q = self.root.sf.getQueryService()
        assert q.findAllByQuery((
            "select a from MapAnnotation a where "
            "a.description like '%%%s%%'") % text, None,
            {"omero.group": "-1"})

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

    def testUserAdd(self):
        self.skipIfNot()
        uid = self.new_user().id.val
        self.assertMail("Experimenter:%s" % uid)

    def testComment(self):
        self.skipIfNot()

        group = self.new_group(perms="rwra--")  # TODO: fixture
        user = self.new_client(group=group)
        update = user.sf.getUpdateService()
        admin = user.sf.getAdminService()

        image = self.make_image(name="testOwnComments", client=user)
        ctx = admin.getEventContext()

        # Set own email
        exp = admin.getExperimenter(ctx.userId)
        exp.setEmail(omero.rtypes.rstring("tester@localhost"))
        admin.updateSelf(exp)

        commenter = self.new_client(group=group,
                                    email="commenter@localhost")
        link = omero.model.ImageAnnotationLinkI()
        comment = omero.model.CommentAnnotationI()
        link.parent = image.proxy()
        link.child = comment
        update = commenter.sf.getUpdateService()
        comment = update.saveAndReturnObject(link).child

        self.assertMail("CommentAnnotation:%s -" % comment.id.val)
