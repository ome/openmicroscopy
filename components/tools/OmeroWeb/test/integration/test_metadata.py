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
import json
import pytest

from weblibrary import IWebTest
from weblibrary import _get_response

from django.core.urlresolvers import reverse
from omero.constants.namespaces import NSBULKANNOTATIONS
from omero.model.enums import UnitsLength
from omero.rtypes import wrap


class TestCoreMetadata(IWebTest):
    """
    Tests display of core metatada
    """

    def test_pixel_size_units(self):
        # Create image
        iid = self.createTestImage(sizeC=2, session=self.sf).id.val

        # show right panel for image
        request_url = reverse('load_metadata_details', args=['image', iid])
        data = {}
        rsp = _get_response(self.django_client, request_url,
                            data, status_code=200)
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
        rsp = _get_response(self.django_client,
                            request_url, data, status_code=200)
        html = rsp.content
        assert "Pixels Size (XYZ) (pixel):" in html


class TestBulkAnnotations(IWebTest):
    """
    Tests retrieval of bulk annotations and related metadata
    """

    @pytest.mark.parametrize("bulkann", [True, False])
    def test_nsbulkannotations_file(self, bulkann):
        if bulkann:
            ns = NSBULKANNOTATIONS
        else:
            ns = 'other'
        # Create plate
        p = omero.model.PlateI()
        p.setName(wrap(self.uuid()))
        update = self.client.sf.getUpdateService()
        p = update.saveAndReturnObject(p)

        # Create a file annotation
        name = self.uuid()
        fa = self.make_file_annotation(name=name, ns=ns)
        link = self.link(p, fa)

        # retrieve annotations
        request_url = reverse(
            "webgateway_annotations", args=["Plate", p.id.val])
        rsp = _get_response(
            self.django_client, request_url, {}, status_code=200)
        j = json.loads(rsp.content)

        if bulkann:
            assert len(j["data"]) == 1
            assert j["data"][0]["file"] == link.child.file.id.val
        else:
            assert len(j["data"]) == 0

    def test_nsbulkannotations_not_file(self):
        # Create plate
        p = omero.model.PlateI()
        p.setName(wrap(self.uuid()))
        update = self.client.sf.getUpdateService()
        p = update.saveAndReturnObject(p)

        # Create a non-file annotation
        tag = self.new_tag(ns=NSBULKANNOTATIONS)
        link = self.link(p, tag)
        assert link

        # retrieve annotations
        request_url = reverse(
            "webgateway_annotations", args=["Plate", p.id.val])
        rsp = _get_response(
            self.django_client, request_url, {}, status_code=200)
        j = json.loads(rsp.content)
        assert len(j["data"]) == 0
