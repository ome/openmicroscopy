#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
# All rights reserved.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

"""
Tests creation, linking, editing & deletion of containers
"""

import omero
import omero.clients
from omero.rtypes import rtime
from omeroweb.testlib import IWebTest
from omeroweb.testlib import _csrf_post_response, _post_response
from omeroweb.testlib import _csrf_delete_response, _delete_response

import json

from django.core.urlresolvers import reverse


class TestContainers(IWebTest):
    """
    Tests creation, linking, editing & deletion of containers
    """

    def blank_image(self):
        """
        Returns a new foundational Image with Channel objects attached for
        view method testing.
        """
        print dir(self)

        pixels = self.pix(client=self.client)
        for the_c in range(pixels.getSizeC().val):
            channel = omero.model.ChannelI()
            channel.logicalChannel = omero.model.LogicalChannelI()
            pixels.addChannel(channel)
        image = pixels.getImage()
        return self.sf.getUpdateService().saveAndReturnObject(image)

    def test_add_and_rename_container(self):

        # Add project
        request_url = reverse("manage_action_containers",
                              args=["addnewcontainer"])
        data = {
            'folder_type': 'project',
            'name': 'foobar'
        }
        _post_response(self.django_client, request_url, data)
        response = _csrf_post_response(self.django_client, request_url, data)
        pid = json.loads(response.content).get("id")

        # Add dataset to the project
        request_url = reverse("manage_action_containers",
                              args=["addnewcontainer", "project", pid])
        data = {
            'folder_type': 'dataset',
            'name': 'foobar'
        }
        _post_response(self.django_client, request_url, data)
        _csrf_post_response(self.django_client, request_url, data)

        # Rename project
        request_url = reverse("manage_action_containers",
                              args=["savename", "project", pid])
        data = {
            'name': 'anotherfoobar'
        }
        _post_response(self.django_client, request_url, data)
        _csrf_post_response(self.django_client, request_url, data)

        # Change project description
        request_url = reverse("manage_action_containers",
                              args=["savedescription", "project", pid])
        data = {
            'description': 'anotherfoobar'
        }
        _post_response(self.django_client, request_url, data)
        _csrf_post_response(self.django_client, request_url, data)

    def test_paste_move_remove_deletamany_image(self):

        # Add dataset
        request_url = reverse("manage_action_containers",
                              args=["addnewcontainer"])
        data = {
            'folder_type': 'dataset',
            'name': 'foobar'
        }
        _post_response(self.django_client, request_url, data)
        response = _csrf_post_response(self.django_client, request_url, data)
        did = json.loads(response.content).get("id")

        img = self.make_image()
        print img

        # Link image to Dataset
        request_url = reverse("api_links")
        data = {
            'dataset': {did: {'image': [img.id.val]}}
        }

        _post_response(self.django_client, request_url, data)
        _csrf_post_response(self.django_client,
                            request_url,
                            json.dumps(data),
                            content_type="application/json")

        # Unlink image from Dataset
        request_url = reverse("api_links")
        data = {
            'dataset': {did: {'image': [img.id.val]}}
        }
        _delete_response(self.django_client, request_url, data)
        response = _csrf_delete_response(self.django_client,
                                         request_url,
                                         json.dumps(data),
                                         content_type="application/json")
        # Response will contain remaining links from image (see test_links.py)
        response = json.loads(response.content)
        assert response == {"success": True}

    def test_create_share(self):

        img = self.make_image()
        request_url = reverse("manage_action_containers",
                              args=["add", "share"])
        data = {
            'enable': 'on',
            'image': img.id.val,
            'members': self.user.id.val,
            'message': 'foobar'
        }

        _post_response(self.django_client, request_url, data)
        _csrf_post_response(self.django_client, request_url, data)

    def test_edit_share(self):

        # create images
        images = [self.create_test_image(session=self.sf),
                  self.create_test_image(session=self.sf)]

        sid = self.sf.getShareService().createShare(
            "foobar", rtime(None), images, [self.user], [], True)

        request_url = reverse("manage_action_containers",
                              args=["save", "share", sid])

        data = {
            'enable': 'on',
            'image': [i.id.val for i in images],
            'members': self.user.id.val,
            'message': 'another foobar'
        }
        _post_response(self.django_client, request_url, data)
        _csrf_post_response(self.django_client, request_url, data)

        # remove image from share
        request_url = reverse("manage_action_containers",
                              args=["removefromshare", "share", sid])
        data = {
            'source': images[1].id.val,
        }
        _post_response(self.django_client, request_url, data)
        _csrf_post_response(self.django_client, request_url, data)
