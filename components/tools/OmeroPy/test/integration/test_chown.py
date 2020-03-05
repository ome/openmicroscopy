#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2020 University of Dundee & Open Microscopy Environment.
# All rights reserved. Use is subject to license terms supplied in LICENSE.txt
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
   Integration test for changing ownership of objects.
"""

from omero.testlib import ITest
from omero.cmd import Chown2
from omero.model import ExperimenterGroupI


class TestChown(ITest):

    def test_chown_project(self):
        """
        Tests chown for a Project
        """
        # Two users in new group. First user is group owner.
        client, exp = self.new_client_and_user(owner=True)
        gid = client.sf.getAdminService().getEventContext().groupId
        new_owner = self.new_user(ExperimenterGroupI(gid, False))
        oid = new_owner.id.val

        # project belongs to first user
        project = self.make_project(name="chown-test", client=client)
        assert project.details.owner.id.val == exp.id.val

        # Chown
        chown = Chown2(targetObjects={'Project': [project.id.val]})
        chown.userId = oid
        self.do_submit(chown, client)

        # check owner...
        pro = client.sf.getQueryService().get("Project", project.id.val)
        assert pro.details.owner.id.val == oid
