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
import omero.clients
from time import sleep

from omeroweb.testlib import IWebTest
from omeroweb.testlib import post, get_json

from django.core.urlresolvers import reverse


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
        post(django_client, request_url, data)

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
        rsp = get_json(django_client1, request_url, data)

        tagIds = [t['id'] for t in rsp['annotations']]
        assert tag.id.val in tagIds
        assert tag2.id.val in tagIds

        # We can remove tags by not including them
        # E.g. move from Right to Left column in the UI
        self.annotate_dataset(django_client1, ds.id.val, [tag2.id.val])

        # Since tag link deletion is async, we need to wait to be sure that
        # tag is removed.
        sleep(1)
        rsp = get_json(django_client1, request_url, data)
        tagIds = [t['id'] for t in rsp['annotations']]
        assert tag.id.val not in tagIds
        assert tag2.id.val in tagIds
