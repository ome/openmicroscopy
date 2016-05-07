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
Tests adding & removing annotations
"""

import omero
import json
import omero.clients

from weblibrary import IWebTest
from weblibrary import _csrf_post_response, _get_response

from django.core.urlresolvers import reverse
from omero.model import DatasetAnnotationLinkI, DatasetI

# @pytest.fixture(scope='function')
# def tags_userA_userB(request, userA, userB, groupA):
#     """
#     Returns new OMERO Tags
#     """
#     tags = []
#     ctx = {'omero.group': str(groupA.id.val)}
#     for name, user in zip(["userAtag", "userBtag"], [userA, userB]):
#         tag = TagAnnotationI()
#         tag.textValue = rstring(name)
#         tag = get_update_service(user).saveAndReturnObject(tag, ctx)
#         tags.append(tag)
#     tags.sort(cmp_id)
#     return tags


def annotate_dataset(dataset, ann, client):
    """
    Returns userA's Tag linked to userB's dataset
    by userA and userB
    """
    link = DatasetAnnotationLinkI()
    link.parent = DatasetI(dataset.id.val, False)
    link.child = ann
    update = client.sf.getUpdateService()
    link = update.saveAndReturnObject(link)
    return link


class TestTagging(IWebTest):
    """
    Tests adding and removing Tags with annotate_tags()
    """

    def test_annotate_tags(self):
        # conn = omero.gateway.BlitzGateway(client_obj=self.client)

        # print self.client.sf.getAdminService().getEventContext()
        groupId = self.client.sf.getAdminService().getEventContext().groupId
        ds = self.make_tag("user1_Dataset")
        tag1 = self.make_tag("user1_Tag")

        client2, user2 = self.new_client_and_user(
            group=omero.model.ExperimenterGroupI(groupId, False))
        # print client2.sf.getAdminService().getEventContext()
        # tag2 = self.make_tag("user2_Tag", client2)

        # # Both users add both Tags
        # annotate_dataset(ds1, tag1, self.client)
        # annotate_dataset(ds1, tag2, self.client)
        # annotate_dataset(ds, tag1, client2)
        # annotate_dataset(ds, tag2, client2)

        # user1 adds a tag
        request_url = reverse('annotate_tags')
        data = {
            "dataset": ds.id.val,
            "tags": tag1.id.val,
            "newtags-0-tagset": '',
            "newtags-0-description": '',
            "newtags-0-tag": "formset",
            "newtags-TOTAL_FORMS": '1',
            "newtags-INITIAL_FORMS": '0',
            "newtags-MIN_NUM_FORMS": '0',
            "newtags-MAX_NUM_FORMS": '1000',
            "filter_mode": "any",
            "filter_owner_mode": "all"
        }
        _csrf_post_response_json(self.django_client, request_url, data)

        # check tag got added
        request_url = reverse('api_annotations')
        data = {
            "dataset": ds.id.val
        }
        rsp = _get_response_json(self.django_client, request_url, data)
        print rsp

        tagIds = [t['id'] for t in rsp]
        assert tag1.id.val in tagIds


def _csrf_post_response_json(django_client, request_url, data):
    rsp = _csrf_post_response(django_client,
                              request_url,
                              json.dumps(data),
                              content_type="application/json")
    return json.loads(rsp.content)


def _get_response_json(django_client, request_url, query_string):
    rsp = _get_response(django_client, request_url,
                        query_string, status_code=200)
    return json.loads(rsp.content)
