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

"""
Test download of data.
"""

from omero.model import PlateI, WellI, WellSampleI
from omero.rtypes import rstring

import pytest
from django.core.urlresolvers import reverse

from weblibrary import IWebTest, _get_response


class TestDownload(IWebTest):
    """
    Tests to check download is disabled where specified.
    """

    @pytest.fixture
    def image_well_plate(self):
        """
        Returns a new OMERO Project, linked Dataset and linked Image populated
        by an L{test.integration.library.ITest} instance with required fields
        set.
        """
        plate = PlateI()
        plate.name = rstring(self.uuid())
        plate = self.update.saveAndReturnObject(plate)

        well = WellI()
        well.plate = plate
        well = self.update.saveAndReturnObject(well)

        image = self.new_image(name=self.uuid())

        ws = WellSampleI()
        ws.image = image
        ws.well = well
        well.addWellSample(ws)
        ws = self.update.saveAndReturnObject(ws)
        return ws.image

    def test_spw_download(self, image_well_plate):
        """
        Download of an Image that is part of a plate should be disabled,
        and return a 404 response.
        """

        image = image_well_plate
        # download archived files
        request_url = reverse('webgateway.views.archived_files')
        data = {
            "image": image.id.val}
        _get_response(self.django_client, request_url, data, status_code=404)

    def test_image_download(self):
        """
        Download of archived files for a non-SPW Image.
        """

        image = self.importSingleImage()

        # download archived files
        request_url = reverse('webgateway.views.archived_files')
        data = {
            "image": image.id.val}
        _get_response(self.django_client, request_url, data, status_code=200)
