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
import omero
from omero.rtypes import rdouble
from omeroweb.testlib import IWebTest
from omeroweb.testlib import get

from cStringIO import StringIO
import pytest
from django.core.urlresolvers import reverse
try:
    from PIL import Image
except Exception:
    import Image


class TestThumbnails(IWebTest):
    """Tests loading of thumbnails."""

    def assert_no_leaked_rendering_engines(self):
        """
        Assert no rendering engine stateful services are left open for the
        current session.
        """
        for v in self.client.getSession().activeServices():
            assert 'RenderingEngine' not in v, 'Leaked rendering engine!'

    @pytest.mark.parametrize("size", [None, 100])
    def test_default_thumb_size(self, size):
        """
        Test that the default size of thumbnails is correct.

        Default size is 96.
        """
        # Create a square image
        image_id = self.create_test_image(size_x=125, size_y=125,
                                          session=self.sf).getId().getValue()
        args = [image_id]
        if size is not None:
            args.append(size)
        request_url = reverse('webgateway.views.render_thumbnail', args=args)
        try:
            rsp = get(self.django_client, request_url)
        finally:
            self.assert_no_leaked_rendering_engines()

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
        try:
            rsp = get(self.django_client, request_url)
            thumb = json.dumps(
                "data:image/jpeg;base64,%s" % base64.b64encode(rsp.content))
        finally:
            self.assert_no_leaked_rendering_engines()

        request_url = reverse('webgateway.views.get_thumbnail_json',
                              args=args)
        try:
            b64rsp = get(self.django_client, request_url).content
            assert thumb == b64rsp
        finally:
            self.assert_no_leaked_rendering_engines()

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
            try:
                rsp = get(self.django_client, request_url)
                expected_thumbs[i] = \
                    "data:image/jpeg;base64,%s" % base64.b64encode(rsp.content)
            finally:
                self.assert_no_leaked_rendering_engines()

        iids = {'id': images}
        request_url = reverse('webgateway.views.get_thumbnails_json')
        try:
            b64rsp = get(self.django_client, request_url, iids).content
            assert cmp(json.loads(b64rsp),
                       json.loads(json.dumps(expected_thumbs))) == 0
            assert json.dumps(expected_thumbs) == b64rsp
        finally:
            self.assert_no_leaked_rendering_engines()

    def test_render_birds_eye_view(self):
        size = 100
        image_id = self.create_test_image(size_x=125, size_y=125,
                                          session=self.sf).getId().getValue()
        args = [image_id]
        args.append(size)
        request_url = reverse('webgateway.views.render_birds_eye_view', args=args)
        try:
            rsp = get(self.django_client, request_url)
            thumb = Image.open(StringIO(rsp.content))
            assert thumb.size == (size, size)
        finally:
            self.assert_no_leaked_rendering_engines()

    def test_render_roi_thumbnail(self):
        image_id = self.create_test_image(size_x=125, size_y=125,
                                          session=self.sf).getId().getValue()
        size = 40
        img = omero.model.ImageI(image_id, False)
        roi = omero.model.RoiI()
        rect = omero.model.RectangleI()
        rect.x = rdouble(0)
        rect.y = rdouble(0)
        rect.width = rdouble(size)
        rect.height = rdouble(size)
        roi.addShape(rect)
        roi.setImage(img)

        roi = self.update.saveAndReturnObject(roi)
        args = [roi.id.val]
        request_url = reverse('webgateway.views.render_roi_thumbnail', args=args)
        try:
            rsp = get(self.django_client, request_url)
        finally:
            self.assert_no_leaked_rendering_engines()

    def test_render_shape_thumbnail(self):
        image_id = self.create_test_image(size_x=125, size_y=125,
                                          session=self.sf).getId().getValue()
        size = 40
        img = omero.model.ImageI(image_id, False)
        roi = omero.model.RoiI()
        rect = omero.model.RectangleI()
        rect.x = rdouble(0)
        rect.y = rdouble(0)
        rect.width = rdouble(size)
        rect.height = rdouble(size)
        roi.addShape(rect)
        roi.setImage(img)

        roi = self.update.saveAndReturnObject(roi)
        shape = roi.copyShapes()[0]
        args = [shape.id.val]
        request_url = reverse('webgateway.views.render_shape_thumbnail', args=args)
        try:
            rsp = get(self.django_client, request_url)
        finally:
            self.assert_no_leaked_rendering_engines()
