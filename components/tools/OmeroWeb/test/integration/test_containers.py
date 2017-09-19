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
from omeroweb.api import api_settings
from omeroweb.testlib import IWebTest
from omeroweb.testlib import get_json, post, post_json, delete_json

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

        pixels = self.create_pixels(client=self.client)
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
        response = post(self.django_client, request_url, data)
        pid = json.loads(response.content).get("id")

        # Add dataset to the project
        request_url = reverse("manage_action_containers",
                              args=["addnewcontainer", "project", pid])
        data = {
            'folder_type': 'dataset',
            'name': 'foobar'
        }
        post(self.django_client, request_url, data)

        # Rename project
        request_url = reverse("manage_action_containers",
                              args=["savename", "project", pid])
        data = {
            'name': 'anotherfoobar'
        }
        post(self.django_client, request_url, data)

        # Change project description
        request_url = reverse("manage_action_containers",
                              args=["savedescription", "project", pid])
        data = {
            'description': 'anotherfoobar'
        }
        post(self.django_client, request_url, data)

    def test_add_owned_container(self):
        """Test root user creating a Dataset owned by another user."""
        request_url = reverse("manage_action_containers",
                              args=["addnewcontainer"])

        # Create user in 2 groups
        group1 = self.new_group()
        group2 = self.new_group()
        user = self.new_user(group=group1)
        self.add_groups(user, [group2])

        # Container will get added to active_group
        session = self.django_root_client.session
        session['active_group'] = group2.id.val
        session.save()
        data = {
            'folder_type': 'dataset',
            'name': 'ownedby',
            'owner': str(user.id.val)
        }
        response = post(self.django_root_client, request_url, data)
        did = json.loads(response.content).get("id")

        # Check that Dataset was created & has correct group and owner
        request_url = reverse("manage_action_containers",
                              args=["addnewcontainer"])
        version = api_settings.API_VERSIONS[-1]
        request_url = reverse('api_dataset', kwargs={'api_version': version,
                                                     'object_id': did})
        rsp_json = get_json(self.django_root_client, request_url, {})
        dataset = rsp_json['data']
        assert dataset['@id'] == did
        assert dataset['omero:details']['owner']['@id'] == user.id.val
        assert dataset['omero:details']['group']['@id'] == group2.id.val

    def test_paste_move_remove_deletamany_image(self):

        # Add dataset
        request_url = reverse("manage_action_containers",
                              args=["addnewcontainer"])
        data = {
            'folder_type': 'dataset',
            'name': 'foobar'
        }
        response = post(self.django_client, request_url, data)
        did = json.loads(response.content).get("id")

        img = self.make_image()
        print img

        # Link image to Dataset
        request_url = reverse("api_links")
        data = {
            'dataset': {did: {'image': [img.id.val]}}
        }

        post_json(self.django_client, request_url, data)

        # Unlink image from Dataset
        request_url = reverse("api_links")
        data = {
            'dataset': {did: {'image': [img.id.val]}}
        }
        response = delete_json(self.django_client, request_url, data)
        # Response will contain remaining links from image (see test_links.py)
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

        post(self.django_client, request_url, data)

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
        post(self.django_client, request_url, data)

        # remove image from share
        request_url = reverse("manage_action_containers",
                              args=["removefromshare", "share", sid])
        data = {
            'source': images[1].id.val,
        }
        post(self.django_client, request_url, data)
