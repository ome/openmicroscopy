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
        iId = self.createTestImage(sizeX=125, sizeY=125,
                                   session=self.sf).id.val
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
