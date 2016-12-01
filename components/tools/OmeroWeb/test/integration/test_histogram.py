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
        Test that the we get histogram json of the expected size.

        Default size is 256 bins.
        """
        sizeX = 125
        sizeY = 125
        iId = self.createTestImage(sizeX=sizeX, sizeY=sizeY,
                                   session=self.sf).id.val
        theC = 0
        args = [iId, theC]
        payload = {}
        if bins is not None:
            payload['bins'] = bins
        request_url = reverse('histogram_json', args=args)
        json = _get_response_json(self.django_client, request_url, payload,
                             status_code=200)
        data = json['data']
        # Sum of all pixel counts should equal number of pixels in image
        assert sum(data) == sizeX * sizeY
        # Number of bins should equal the 'bins' parameter (256 by default)
        if bins is None:
            assert len(data) == 256
        else:
            assert len(data) == bins
