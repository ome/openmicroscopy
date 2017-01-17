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
from omeroweb.testlib import _get_response_json

import pytest
from django.core.urlresolvers import reverse


class TestHistogram(IWebTest):
    """Tests loading of histogram json data."""

    @pytest.mark.parametrize("bins", [None, 10])
    def test_histogram_bin_count(self, bins):
        """
        Test that we get histogram json of the expected size.

        Default size is 256 bins.
        """
        size_x = 125
        size_y = 125
        img_id = self.create_test_image(size_x=size_x, size_y=size_y,
                                        session=self.sf).id.val
        the_c = 0
        args = [img_id, the_c]
        payload = {}
        if bins is not None:
            payload['bins'] = bins
        request_url = reverse('histogram_json', args=args)
        json = _get_response_json(self.django_client, request_url, payload,
                                  status_code=200)
        data = json['data']
        # Sum of all pixel counts should equal number of pixels in image
        assert sum(data) == size_x * size_y
        # Number of bins should equal the 'bins' parameter (256 by default)
        if bins is None:
            assert len(data) == 256
        else:
            assert len(data) == bins
