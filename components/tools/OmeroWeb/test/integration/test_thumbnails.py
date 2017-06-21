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

"""Tests rendering of thumbnails."""

import base64
import json
from omeroweb.testlib import IWebTest
from omeroweb.testlib import _get_response

from cStringIO import StringIO
import pytest
from django.core.urlresolvers import reverse
try:
    from PIL import Image
except:
    import Image


class TestThumbnails(IWebTest):
    """Tests loading of thumbnails."""

    @pytest.mark.parametrize("size", [None, 100])
    def test_default_thumb_size(self, size):
        """
        Test that the default size of thumbnails is correct.

        Default size is 96.
        """
        # Create a square image
        iId = self.create_test_image(size_x=125, size_y=125,
                                     session=self.sf).getId().getValue()
        args = [iId]
        if size is not None:
            args.append(size)
        request_url = reverse('webgateway.views.render_thumbnail', args=args)
        rsp = _get_response(self.django_client, request_url, {},
                            status_code=200)

        thumb = Image.open(StringIO(rsp.content))
        # Should be 96 on both sides
        if size is None:
            assert thumb.size == (96, 96)
        else:
            assert thumb.size == (size, size)

    @pytest.mark.parametrize("size", [None, 100])
    def test_base64_thumb(self, size):
        """
        Test base64 encoded retrival of single thumbnail
        """
        # Create a square image
        iid = self.create_test_image(size_x=256, size_y=256,
                                     session=self.sf).id.val
        args = [iid]
        if size is not None:
            args.append(size)
        request_url = reverse('webgateway.views.render_thumbnail', args=args)
        rsp = _get_response(self.django_client, request_url, {},
                            status_code=200)
        thumb = json.dumps(
            "data:image/jpeg;base64,%s" % base64.b64encode(rsp.content))

        request_url = reverse('webgateway.views.get_thumbnail_json',
                              args=args)
        b64rsp = _get_response(self.django_client, request_url, {},
                               status_code=200).content
        assert thumb == b64rsp

    def test_base64_thumb_set(self):
        """
        Test base64 encoded retrival of thumbnails in a batch
        """
        # Create a square image
        images = []
        for i in range(2, 5):
            iid = self.create_test_image(size_x=64*i, size_y=64*i,
                                         session=self.sf).id.val
            images.append(iid)

        expected_thumbs = {}
        for i in images:
            request_url = reverse('webgateway.views.render_thumbnail',
                                  args=[i])
            rsp = _get_response(self.django_client, request_url, {},
                                status_code=200)

            expected_thumbs[i] = \
                "data:image/jpeg;base64,%s" % base64.b64encode(rsp.content)

        iids = {'id': images}
        request_url = reverse('webgateway.views.get_thumbnails_json')
        b64rsp = _get_response(self.django_client, request_url, iids,
                               status_code=200).content

        assert cmp(json.loads(b64rsp),
                   json.loads(json.dumps(expected_thumbs))) == 0
        assert json.dumps(expected_thumbs) == b64rsp
