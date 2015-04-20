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
from weblibrary import IWebTest
from weblibrary import _get_response

from django.core.urlresolvers import reverse


class TestShare(IWebTest):

    """
    Tests to ensure that sharing service implemented in  OMERO.web is working
    correctly.
    """

    def test_load_share_content(self):
        """
        Test if share content is loaded to the view
        """

        client, user = self.new_client_and_user()

        image = self.make_image()
        share_id = self.create_share(
            objects=[image], description="description", experimenters=[user])
        share = self.client.sf.getShareService()
        assert len(share.getContents(share_id)) == 1

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
        share_id = self.create_share(
            objects=[link, link.getChild(), link.getParent()],
            description="description", experimenters=[user])
        share = self.client.sf.getShareService()
        assert len(share.getContents(share_id)) == 3

        # load share content
        request_url = reverse('load_public', args=[share_id])
        data = {
            "view": "icon"
        }
        _get_response(self.django_client, request_url, data, status_code=200)
