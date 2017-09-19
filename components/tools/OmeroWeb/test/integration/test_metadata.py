#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2015 Glencoe Software, Inc.
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
Tests display of metadata in webclient
"""
import omero

from omeroweb.testlib import IWebTest
from omeroweb.testlib import get

from django.core.urlresolvers import reverse
from omero.model.enums import UnitsLength
from omero_model_ImageI import ImageI

from omero.rtypes import rstring


class TestCoreMetadata(IWebTest):
    """
    Tests display of core metatada
    """

    def test_pixel_size_units(self):
        # Create image
        iid = self.create_test_image(size_c=2, session=self.sf).id.val

        # show right panel for image
        request_url = reverse('load_metadata_details', args=['image', iid])
        data = {}
        rsp = get(self.django_client, request_url, data, status_code=200)
        html = rsp.content
        # Units are µm by default
        assert "Pixels Size (XYZ) (µm):" in html

        # Now save units as PIXELs and view again
        conn = omero.gateway.BlitzGateway(client_obj=self.client)
        i = conn.getObject("Image", iid)
        u = omero.model.LengthI(1.2, UnitsLength.PIXEL)
        p = i.getPrimaryPixels()._obj
        p.setPhysicalSizeX(u)
        p.setPhysicalSizeY(u)
        conn.getUpdateService().saveObject(p)

        # Should now be showning pixels
        rsp = get(self.django_client, request_url, data, status_code=200)
        html = rsp.content
        assert "Pixels Size (XYZ):" in html
        assert "1.20 (pixel)" in html

    def test_none_pixel_size(self):
        """
        Tests display of core metatada still works even when image
        doesn't have pixels data
        """
        img = ImageI()
        img.setName(rstring("no_pixels"))
        img.setDescription(rstring("empty image without pixels data"))

        conn = omero.gateway.BlitzGateway(client_obj=self.client)
        img = conn.getUpdateService().saveAndReturnObject(img)

        request_url = reverse('load_metadata_details',
                              args=['image', img.id.val])

        # Just check that the metadata panel is loaded
        rsp = get(self.django_client, request_url, status_code=200)
        assert "no_pixels" in rsp.content
