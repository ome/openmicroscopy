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
from time import sleep

from weblibrary import IWebTest
from weblibrary import _csrf_post_response, _get_response

from django.core.urlresolvers import reverse

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


class TestTagging(IWebTest):
    """
    Tests adding and removing Tags with annotate_tags()
    """

    def annotate_dataset(self, django_client, dsId, tagIds):
        """
        Returns userA's Tag linked to userB's dataset
        by userA and userB
        """
        # 'newtags-0-description': '',
        # 'newtags-0-tag': 'foobar',
        # 'newtags-0-tagset': '',
        request_url = reverse('annotate_tags')
        data = {
            'dataset': dsId,
            'filter_mode': 'any',
            'filter_owner_mode': 'all',
            'index': 0,
            'newtags-INITIAL_FORMS': 0,
            'newtags-MAX_NUM_FORMS': 1000,
            'newtags-TOTAL_FORMS': 0,
            'tags': ",".join([str(i) for i in tagIds])
        }
        _csrf_post_response(django_client, request_url, data)

    def test_annotate_tag(self):

        # Create User in a Read-Annotate group
        client1, user1 = self.new_client_and_user(perms='rwrw--')
        # conn = omero.gateway.BlitzGateway(client_obj=client1)
        omeName = client1.sf.getAdminService().getEventContext().userName
        django_client1 = self.new_django_client(omeName, omeName)

        # User1 creates Tag and Dataset
        ds = self.make_dataset("user1_Dataset", client=client1)
        tag = self.make_tag("test_annotate_tag", client=client1)

        # User2...
        groupId = client1.sf.getAdminService().getEventContext().groupId
        client2, user2 = self.new_client_and_user(
            group=omero.model.ExperimenterGroupI(groupId, False))
        # ...creates Tag
        tag2 = self.make_tag("user2_tag", client=client2)

        # User1 adds 2 tags to Dataset
        self.annotate_dataset(django_client1, ds.id.val,
                              [tag.id.val, tag2.id.val])

        # check tags got added
        request_url = reverse('api_annotations')
        data = {
            "dataset": ds.id.val
        }
        rsp = _get_response_json(django_client1, request_url, data)

        tagIds = [t['id'] for t in rsp['annotations']]
        assert tag.id.val in tagIds
        assert tag2.id.val in tagIds

        # We can remove tags by not including them
        # E.g. move from Right to Left column in the UI
        self.annotate_dataset(django_client1, ds.id.val, [tag2.id.val])

        # Since tag link deletion is async, we need to wait to be sure that
        # tag is removed.
        sleep(1)
        rsp = _get_response_json(django_client1, request_url, data)
        tagIds = [t['id'] for t in rsp['annotations']]
        assert tag.id.val not in tagIds
        assert tag2.id.val in tagIds


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
