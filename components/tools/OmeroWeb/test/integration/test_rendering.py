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

import json

import omero
import omero.clients

from omeroweb.testlib import IWebTest
from omeroweb.testlib import _csrf_post_response, _get_response

from django.core.urlresolvers import reverse


class TestRendering(IWebTest):
    """
    Tests copying and pasting of rendering settings from one image to another
    """

    def test_copy_past_rendering_settings_from_image(self):
        # Create 2 images with 2 channels each
        iid1 = self.create_test_image(size_c=2, session=self.sf).id.val
        iid2 = self.create_test_image(size_c=2, session=self.sf).id.val

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
        iid1 = self.create_test_image(size_c=2, session=self.sf).id.val
        iid2 = self.create_test_image(size_c=2, session=self.sf).id.val

        conn = omero.gateway.BlitzGateway(client_obj=self.client)

        # image1 set color model
        image1 = conn.getObject("Image", iid1)
        image1.resetDefaults()
        image1.setColorRenderingModel()
        image1.setActiveChannels([1, 2], [[20, 300], [50, 100]],
                                 ['00FF00', 'FF0000'], [True, False])
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
                start = int(ch.getWindowStart())
                end = int(ch.getWindowEnd())
                rev = 'r' if ch.isReverseIntensity() else '-r'
                color = ch.getColor().getHtml()
                chs.append("%s%s|%s:%s%s$%s" % (act, i+1, start, end,
                                                rev, color))
            return ",".join(chs)

        # build channel parameter e.g. 1|0:15$FF0000...
        old_c1 = buildParamC(image1)
        # Check it is what we expect
        assert old_c1 == "1|20:300r$00FF00,2|50:100-r$FF0000"

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

    """
    Tests retrieving all rendering defs for an image (given id)
    """
    def test_all_rendering_defs(self):
        # Create image with 3 channels
        iid = self.create_test_image(size_c=3, session=self.sf).id.val

        conn = omero.gateway.BlitzGateway(client_obj=self.client)

        image = conn.getObject("Image", iid)
        image.resetDefaults()
        image.setColorRenderingModel()
        image.setReverseIntensity(0, True)
        image.saveDefaults()
        image = conn.getObject("Image", iid)

        assert image.isGreyscaleRenderingModel() is False

        # request the rendering def via the method we want to test
        request_url = reverse(
            'webgateway.views.get_image_rdefs_json', args=[iid])
        response = _get_response(
            self.django_client, request_url, {}, status_code=200)

        # check expected response
        assert response is not None and response.content is not None

        # json => dict for convenience
        rdefDict = json.loads(response.content)
        assert isinstance(rdefDict, dict)
        rdefs = rdefDict.get('rdefs')

        # there has to be one rgb image with 3 channels
        assert isinstance(rdefs, list) and len(rdefs) == 1
        assert rdefs[0].get("model") == "rgb"
        channels = rdefs[0].get("c")
        assert isinstance(channels, list) and len(channels) == 3

        # channel info is supposed to match
        expChannels = image.getChannels()
        revs = [True, False, False]    # expected reverse intensity flags
        for i, c in enumerate(channels):
            assert c['active'] == expChannels[i].isActive()
            assert c['start'] == expChannels[i].getWindowStart()
            assert c['end'] == expChannels[i].getWindowEnd()
            assert c['color'] == expChannels[i].getColor().getHtml()
            assert c['reverseIntensity'] == revs[i]

        # id and owner check
        assert rdefs[0].get("id") is not None
        owner = rdefs[0].get("owner")
        assert isinstance(owner, dict)
        fullOwner = owner.get("firstName", "") + " " +\
            owner.get("lastName", "")
        assert fullOwner == conn.getUser().getFullName()


class TestRenderImageRegion(IWebTest):
    """
    Tests rendering of image regions
    """

    def assert_no_leaked_rendering_engines(self):
        """
        Assert no rendering engine stateful services are left open for the
        current session.
        """
        for v in self.client.getSession().activeServices():
            assert 'RenderingEngine' not in v, 'Leaked rendering engine!'

    def test_render_image_region_incomplete_request(self):
        """
        Either `tile` or `region` is a required request argument to
        `render_image_region()`.  If `c` is also passed, the rendering
        engine will also be initialised.  This test ensure that the correct
        HTTP status code is used and that consequently, any and all
        rendering engines that were created servicing the request are closed.
        """
        image_id = self.create_test_image(size_c=1, session=self.sf).id.val

        request_url = reverse(
            'webgateway.views.render_image_region',
            kwargs={'iid': str(image_id), 'z': '0', 't': '0'}
        )
        data = {'c': '1|0:255$FF0000'}
        django_client = self.new_django_client_from_session_id(
            self.client.getSessionId()
        )
        try:
            _get_response(django_client, request_url, data, status_code=400)
        finally:
            self.assert_no_leaked_rendering_engines()

    def test_render_image_region_malformed_tile_argument(self):
        """
        Either `tile` or `region` is a required request argument to
        `render_image_region()`.  This test ensure that if a malformed `tile`
        is requested the correct HTTP status code is used and that
        consequently, any and all rendering engines that were created
        servicing the request are closed.
        """
        image_id = self.create_test_image(size_c=1, session=self.sf).id.val

        request_url = reverse(
            'webgateway.views.render_image_region',
            kwargs={'iid': str(image_id), 'z': '0', 't': '0'}
        )
        data = {'tile': 'malformed'}
        django_client = self.new_django_client_from_session_id(
            self.client.getSessionId()
        )
        try:
            _get_response(django_client, request_url, data, status_code=400)
        finally:
            self.assert_no_leaked_rendering_engines()

    def test_render_image_region_malformed_region_argument(self):
        """
        Either `tile` or `region` is a required request argument to
        `render_image_region()`.  This test ensure that if a malformed
        `region` is requested the correct HTTP status code is used and that
        consequently, any and all rendering engines that were created
        servicing the request are closed.
        """
        image_id = self.create_test_image(size_c=1, session=self.sf).id.val

        request_url = reverse(
            'webgateway.views.render_image_region',
            kwargs={'iid': str(image_id), 'z': '0', 't': '0'}
        )
        data = {'region': 'malformed'}
        django_client = self.new_django_client_from_session_id(
            self.client.getSessionId()
        )
        try:
            _get_response(django_client, request_url, data, status_code=400)
        finally:
            self.assert_no_leaked_rendering_engines()
