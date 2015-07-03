#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2014 Glencoe Software, Inc.
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
Tests copying and pasting of rendering settings in webclient
"""

import omero
import omero.clients

from weblibrary import IWebTest
from weblibrary import _csrf_post_response, _get_response

from django.core.urlresolvers import reverse


class TestRendering(IWebTest):
    """
    Tests copying and pasting of rendering settings from one image to another
    """

    def test_copy_past_rendering_settings_from_image(self):
        # Create 2 images with 2 channels each
        iid1 = self.createTestImage(sizeC=2, session=self.sf).id.val
        iid2 = self.createTestImage(sizeC=2, session=self.sf).id.val

        conn = omero.gateway.BlitzGateway(client_obj=self.client)

        # image1 set color model
        image1 = conn.getObject("Image", iid1)
        image1.resetDefaults()
        image1.setColorRenderingModel()
        image1.saveDefaults()
        image1 = conn.getObject("Image", iid1)

        # image2 set greyscale model
        image2 = conn.getObject("Image", iid2)
        image2.setGreyscaleRenderingModel()
        image2.saveDefaults()
        image2 = conn.getObject("Image", iid2)

        assert image1.isGreyscaleRenderingModel() is False
        assert image2.isGreyscaleRenderingModel() is True

        # copy rendering settings from image1 via ID
        request_url = reverse('webgateway.views.copy_image_rdef_json')
        data = {
            "fromid": iid1
        }
        _get_response(self.django_client, request_url, data, status_code=200)

        # paste rendering settings to image2
        data = {
            'toids': iid2
        }

        _csrf_post_response(self.django_client, request_url, data)

        image2 = conn.getObject("Image", iid2)
        assert image2.isGreyscaleRenderingModel() is False

    def test_copy_past_rendering_settings_from_url(self):
        # Create 2 images with 2 channels each
        iid1 = self.createTestImage(sizeC=2, session=self.sf).id.val
        iid2 = self.createTestImage(sizeC=2, session=self.sf).id.val

        conn = omero.gateway.BlitzGateway(client_obj=self.client)

        # image1 set color model
        image1 = conn.getObject("Image", iid1)
        image1.resetDefaults()
        image1.setColorRenderingModel()
        image1.saveDefaults()
        image1 = conn.getObject("Image", iid1)

        # image2 set greyscale model
        image2 = conn.getObject("Image", iid2)
        image2.setGreyscaleRenderingModel()
        image2.saveDefaults()
        image2 = conn.getObject("Image", iid2)

        assert image1.isGreyscaleRenderingModel() is False
        assert image2.isGreyscaleRenderingModel() is True

        def buildParamC(im):
            chs = []
            for i, ch in enumerate(im.getChannels()):
                act = "" if ch.isActive() else "-"
                start = ch.getWindowStart()
                end = ch.getWindowEnd()
                color = ch.getColor().getHtml()
                chs.append("%s%s|%s:%s$%s" % (act, i+1, start, end, color))
            return ",".join(chs)

        # build channel parameter e.g. 1|0:15$FF0000...
        old_c1 = buildParamC(image1)

        # copy rendering settings from image1 via URL
        request_url = reverse('webgateway.views.copy_image_rdef_json')
        data = {
            "imageId": iid1,
            "c": old_c1,
            "ia": 0,
            "m": "c",
            "p": "normal",
            "q": 0.9,
            "t": 1,
            "x": 0,
            "y": 0,
            "z": 1,
            "zm": 100
        }
        _get_response(self.django_client, request_url, data, status_code=200)

        # paste rendering settings to image2
        data = {
            'toids': iid2
        }
        _csrf_post_response(self.django_client, request_url, data)

        # reload image1
        image1 = conn.getObject("Image", iid1)

        # reload image2
        image2 = conn.getObject("Image", iid2)
        new_c2 = buildParamC(image2)

        # compare Channels
        # old image1 1|0:15$FF0000,2|0:15$00FF00
        # image2 1|0:15$00FF00,2|0:15$00FF00
        # new image1 1|0:15$FF0000,2|0:15$00FF00
        # image2 1|0:15$FF0000,2|0:15$00FF00
        assert old_c1 == new_c2
        # check if image2 rendering model changed from greyscale to color
        assert image2.isGreyscaleRenderingModel() is False
