#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
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
#
# Author: Aleksandra Tarkowska <A(dot)Tarkowska(at)dundee(dot)ac(dot)uk>.
#

"""
Simple integration tests to ensure that the CSRF middleware is enabled and
working correctly.
"""
import pytest
from weblibrary import IWebTest
from weblibrary import _get_response

from omero.gateway import BlitzGateway

from django.core.urlresolvers import reverse


class TestShare(IWebTest):

    """
    Tests to ensure that sharing service implemented in  OMERO.web is working
    correctly.
    """

    def create_share(self, objects=[], description=None, timeout=None,
                     experimenters=[], guests=[], enabled=True, client=None):
        """
        Creates share
        """
        if client is None:
            client = self.client
        share = client.sf.getShareService()
        return share.createShare(description, timeout, objects,
                                 experimenters, guests, enabled)

    def test_load_share_content(self):
        """
        Test if share content is loaded to the view
        """

        client, user = self.new_client_and_user()

        image = self.sf.getUpdateService() \
                       .saveAndReturnObject(self.new_image(name=self.uuid()))
        share_id = self.create_share(objects=[image],
                                     description="description",
                                     experimenters=[user])

        assert len(self.client.sf.getShareService()
                                 .getContents(share_id)) == 1

        # load share content
        request_url = reverse('load_public', args=[share_id])
        data = {
            "view": "icon"
        }
        _get_response(self.django_client, request_url, data, status_code=200)

    def test_unsupported_share_content(self):
        """
        Test if share content is loaded to the view
        even when share contain unsuported objects
        """

        client, user = self.new_client_and_user()

        dataset = self.new_dataset(name=self.uuid())
        image = self.new_image(name=self.uuid())
        link = self.link(dataset, image)
        share_id = self.create_share(objects=[link, link.getChild(),
                                              link.getParent()],
                                     description="description",
                                     experimenters=[user])

        assert len(self.client.sf.getShareService()
                                 .getContents(share_id)) == 3

        # load share content
        request_url = reverse('load_public', args=[share_id])
        data = {
            "view": "icon"
        }
        _get_response(self.django_client, request_url, data, status_code=200)

    @pytest.mark.parametrize('func', ['canEdit', 'canAnnotate', 'canDelete',
                                      'canLink'])
    def test_canDoAction(self, func):
        """
        Test if canEdit returns appropriate flag
        """

        client, user = self.new_client_and_user()

        image = self.sf.getUpdateService() \
                       .saveAndReturnObject(self.new_image(name=self.uuid()))
        share_id = self.create_share(objects=[image],
                                     description="description",
                                     experimenters=[user])

        assert len(self.client.sf.getShareService()
                                 .getContents(share_id)) == 1

        # test action by member
        user_conn = BlitzGateway(client_obj=client)
        # user CANNOT see image if not in share
        assert None == user_conn.getObject("Image", image.id.val)
        # activate share
        user_conn.SERVICE_OPTS.setOmeroShare(share_id)
        assert False == getattr(user_conn.getObject("Image",
                                                    image.id.val), func)()

        # test action by owner
        owner_conn = BlitzGateway(client_obj=self.client)
        # owner CAN do action on the object when not in share
        assert True == getattr(owner_conn.getObject("Image",
                                                    image.id.val), func)()
        # activate share
        owner_conn.SERVICE_OPTS.setOmeroShare(share_id)
        # owner CANNOT do action on the object when in share
        assert False == getattr(owner_conn.getObject("Image",
                                                     image.id.val), func)()
