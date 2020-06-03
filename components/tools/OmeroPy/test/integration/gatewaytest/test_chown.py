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
   Gateway integration test for changing ownership of objects.
"""

from omero.testlib import ITest
from omero.gateway import BlitzGateway
from omero.model import ExperimenterGroupI
from omero.callbacks import CmdCallbackI
from omero.cmd import State, ERR, OK


class TestChown(ITest):

    def wait_on_callback(self, conn, proxy, test_should_pass=True):
        """
        Handles callback on proxy handle and checks that the
        result is not an error.
        """

        cb = CmdCallbackI(conn.c, proxy)
        try:
            for i in range(10):
                cb.loop(20, 500)
                if proxy.getResponse() is not None:
                    break

            assert proxy.getResponse() is not None
            proxy.getStatus()
            rsp = proxy.getResponse()

            if test_should_pass:
                assert not isinstance(rsp, ERR)
                assert State.FAILURE not in proxy.getStatus().flags
            else:
                assert not isinstance(rsp, OK)
                assert State.FAILURE in proxy.getStatus().flags
            return rsp
        finally:
            cb.close(True)

    def test_chown_project(self):
        """
        Tests chown for a Project
        """
        # Two users in new group. First user is group owner.
        client, exp = self.new_client_and_user(owner=True)
        conn = BlitzGateway(client_obj=client)
        gid = client.sf.getAdminService().getEventContext().groupId
        new_owner = self.new_user(ExperimenterGroupI(gid, False))
        oid = new_owner.id.val

        # project belongs to first user
        project = self.make_project(name="chown-test", client=client)
        assert project.details.owner.id.val == exp.id.val

        # Chown
        proxy = conn.chownObjects("Project", [project.id.val], oid)
        self.wait_on_callback(conn, proxy)

        # check owner...
        pro = client.sf.getQueryService().get("Project", project.id.val)
        assert pro.details.owner.id.val == oid

    def test_chown_pdi(self):
        """
        Tests chown for a Project, Dataset and Image hierarchy
        """
        # Two users in new group. First user is group owner.
        client, exp = self.new_client_and_user(owner=True)
        conn = BlitzGateway(client_obj=client)
        gid = client.sf.getAdminService().getEventContext().groupId
        new_owner = self.new_user(ExperimenterGroupI(gid, False))
        oid = new_owner.id.val

        # project belongs to first user
        project = self.make_project(name="chown-test", client=client)
        assert project.details.owner.id.val == exp.id.val

        # Data Setup (image in the P/D hierarchy)
        project = self.make_project(name="chown-test", client=client)
        dataset = self.make_dataset(name="chown-test", client=client)
        image = self.make_image(client=client)
        self.link(dataset, image, client=client)
        self.link(project, dataset, client=client)

        # Chown
        conn.chownObjects("Project", [project.id.val], oid, wait=True)

        # check owner of each object...
        project = client.sf.getQueryService().get("Project", project.id.val)
        assert project.details.owner.id.val == oid
        dataset = client.sf.getQueryService().get("Dataset", dataset.id.val)
        assert dataset.details.owner.id.val == oid
        image = client.sf.getQueryService().get("Image", image.id.val)
        assert image.details.owner.id.val == oid
