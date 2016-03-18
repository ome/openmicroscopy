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
Tests creation, linking, editing and deletion of Tags
"""

import omero
import omero.clients
from omero.rtypes import rstring
from weblibrary import IWebTest
from weblibrary import _csrf_post_response, _post_response
from weblibrary import _get_response

import json
from django.core.urlresolvers import reverse


class TestCsrf(IWebTest):
    """
    Tests creation, linking, editing and deletion of Tags
    """

    def new_tag(self):
        """
        Returns a new Tag objects
        """
        tag = omero.model.TagAnnotationI()
        tag.textValue = rstring(self.uuid())
        tag.ns = rstring("pytest")
        return self.sf.getUpdateService().saveAndReturnObject(tag)

    def test_create_tag_and_tagset(self):
        # Create Tagset
        request_url = reverse("manage_action_containers",
                              args=["addnewcontainer"])
        data = {
            'folder_type': 'tagset',
            'name': 'testTagset'
        }
        response = _csrf_post_response(self.django_client, request_url, data)
        tagsetId = json.loads(response.content).get("id")

        # Add tag to the tagset
        request_url = reverse("manage_action_containers",
                              args=["addnewcontainer", "tagset", tagsetId])
        data = {
            'folder_type': 'tag',
            'name': 'tagInTagset'
        }
        _post_response(self.django_client, request_url, data)
        response2 = _csrf_post_response(self.django_client, request_url, data)
        tagId = json.loads(response2.content).get("id")

        # Check that tag is listed under tagset...
        request_url = reverse("api_tags_and_tagged")
        data = {'id': tagsetId}
        data = _get_response_json(self.django_client, request_url, data)
        assert len(data['tags']) == 1
        assert data['tags'][0]['id'] == tagId

    def test_add_edit_and_remove_tag(self):

        # Add tag
        img = self.make_image()
        tag = self.new_tag()
        request_url = reverse('annotate_tags')
        data = {
            'image': img.id.val,
            'filter_mode': 'any',
            'filter_owner_mode': 'all',
            'index': 0,
            'newtags-0-description': '',
            'newtags-0-tag': 'foobar',
            'newtags-0-tagset': '',
            'newtags-INITIAL_FORMS': 0,
            'newtags-MAX_NUM_FORMS': 1000,
            'newtags-TOTAL_FORMS': 1,
            'tags': tag.id.val
        }
        _post_response(self.django_client, request_url, data)
        _csrf_post_response(self.django_client, request_url, data)

        # Edit tag, see save container name and description
        # http://localhost/webclient/action/savename/tag/ID/
        # http://localhost/webclient/action/savedescription/tag/ID/

        # Remove tag
        request_url = reverse("manage_action_containers",
                              args=["remove", "tag", tag.id.val])
        data = {
            'index': 0,
            'parent': "image-%i" % img.id.val
        }
        _post_response(self.django_client, request_url, data)
        _csrf_post_response(self.django_client, request_url, data)

        # Delete tag
        request_url = reverse("manage_action_containers",
                              args=["delete", "tag", tag.id.val])
        _post_response(self.django_client, request_url, {})
        _csrf_post_response(self.django_client, request_url, {})


def _get_response_json(django_client, request_url, query_string):
    rsp = _get_response(django_client, request_url,
                        query_string, status_code=200)
    return json.loads(rsp.content)
