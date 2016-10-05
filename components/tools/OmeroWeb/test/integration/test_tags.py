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

import pytest
import json
from django.core.urlresolvers import reverse
from time import sleep


class TestTags(IWebTest):
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
        """
        Creates a Tagset then a Tag within the Tagset
        """

        tsValue = 'testTagset'
        request_url = reverse("manage_action_containers",
                              args=["addnewcontainer"])
        data = {
            'folder_type': 'tagset',
            'name': tsValue
        }
        response = _csrf_post_response(self.django_client, request_url, data)
        tagsetId = json.loads(response.content).get("id")

        # check creation
        tagset = self.query.get('TagAnnotationI', tagsetId)
        assert tagset is not None
        assert tagset.ns.val == omero.constants.metadata.NSINSIGHTTAGSET
        assert tagset.textValue.val == tsValue

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

    @pytest.mark.parametrize("dtype", ["tagset", "tag"])
    def test_edit_tag_and_tagset(self, dtype):
        """
        Creates Tag/Tagset and tests editing of name and description
        """

        request_url = reverse("manage_action_containers",
                              args=["addnewcontainer"])
        data = {
            'folder_type': dtype,
            'name': 'beforeEdit'
        }
        response = _csrf_post_response(self.django_client, request_url, data)
        tagId = json.loads(response.content).get("id")

        # Edit name
        request_url = reverse("manage_action_containers",
                              args=["savename", dtype, tagId])
        data = {
            'name': 'afterEdit'
        }
        response = _csrf_post_response(self.django_client, request_url, data)

        # Edit description
        request_url = reverse("manage_action_containers",
                              args=["savedescription", dtype, tagId])
        data = {
            'description': 'New description after editing'
        }
        response = _csrf_post_response(self.django_client, request_url, data)

        # check edited name and description
        tagset = self.query.get('TagAnnotationI', tagId)
        assert tagset is not None
        if dtype == "tagset":
            assert tagset.ns.val == omero.constants.metadata.NSINSIGHTTAGSET
        assert tagset.textValue.val == 'afterEdit'
        assert tagset.description.val == 'New description after editing'

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
        rsp = _csrf_post_response(self.django_client, request_url, data)
        rspJson = json.loads(rsp.content)
        assert len(rspJson['new']) == 1
        newTagId = rspJson['new'][0]
        assert rspJson['added'] == [tag.id.val]
        # Check that image is tagged with both tags
        request_url = reverse("api_annotations")
        data = {'image': img.id.val, 'type': 'tag'}
        data = _get_response_json(self.django_client, request_url, data)
        assert len(data['annotations']) == 2

        # Remove tag
        request_url = reverse("manage_action_containers",
                              args=["remove", "tag", tag.id.val])
        data = {
            'index': 0,
            'parent': "image-%i" % img.id.val
        }
        _post_response(self.django_client, request_url, data)
        _csrf_post_response(self.django_client, request_url, data)
        # Check that tag is removed - short delay to allow async delete
        sleep(0.1)
        request_url = reverse("api_annotations")
        data = {'image': img.id.val, 'type': 'tag'}
        data = _get_response_json(self.django_client, request_url, data)
        assert len(data['annotations']) == 1

        # Delete other tag
        request_url = reverse("manage_action_containers",
                              args=["delete", "tag", newTagId])
        _post_response(self.django_client, request_url, {})
        _csrf_post_response(self.django_client, request_url, {})
        # Check that tag is deleted from image
        sleep(1)
        request_url = reverse("api_annotations")
        data = {'image': img.id.val, 'type': 'tag'}
        rsp = _get_response_json(self.django_client, request_url, data)
        assert len(rsp['annotations']) == 0

    def test_add_remove_tags(self):
        # Test performance with lots of tags.
        # See https://github.com/openmicroscopy/openmicroscopy/pull/4842
        img = self.make_image()
        img_count = 200
        tag_count = 10
        iids = [self.make_image().id.val for i in range(img_count)]
        tagIds = [str(self.new_tag().id.val) for i in range(tag_count)]
        tagIds = ",".join(tagIds)
        request_url = reverse('annotate_tags')
        data = {
            'image': iids,
            'filter_mode': 'any',
            'filter_owner_mode': 'all',
            'index': 0,
            'newtags-INITIAL_FORMS': 0,
            'newtags-MAX_NUM_FORMS': 1000,
            'newtags-TOTAL_FORMS': 0,
            'tags': tagIds
        }
        _post_response(self.django_client, request_url, data)
        rsp = _csrf_post_response(self.django_client, request_url, data)
        rspJson = json.loads(rsp.content)
        assert len(rspJson['added']) == tag_count
        # Check that tags are added to all images
        anns_url = reverse("api_annotations")
        query_string = '&'.join(['image=%s' % i for i in iids])
        query_string += '&type=tag'
        query_string += '&page=0'  # disable pagination
        print query_string
        rsp = self.django_client.get('%s?%s' % (anns_url, query_string))
        rspJson = json.loads(rsp.content)
        assert len(rspJson['annotations']) == img_count * tag_count

        # Remove tags
        data = {
            'image': iids,
            'filter_mode': 'any',
            'filter_owner_mode': 'all',
            'index': 0,
            'newtags-INITIAL_FORMS': 0,
            'newtags-MAX_NUM_FORMS': 1000,
            'newtags-TOTAL_FORMS': 0,
            'tags': ''
        }
        _post_response(self.django_client, request_url, data)
        rsp = _csrf_post_response(self.django_client, request_url, data)
        rspJson = json.loads(rsp.content)

        # Async delete - Keep checking until all removed
        completed = False
        for t in range(10):
            rsp = self.django_client.get('%s?%s' % (anns_url, query_string))
            rspJson = json.loads(rsp.content)
            if len(rspJson['annotations']) == 0:
                completed = True
                break
            sleep(1)
        assert completed


def _get_response_json(django_client, request_url, query_string):
    rsp = _get_response(django_client, request_url,
                        query_string, status_code=200)
    return json.loads(rsp.content)
