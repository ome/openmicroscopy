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

from omeroweb.testlib import IWebTest
from omeroweb.testlib import get, _get_response
from omeroweb.webgateway.marshal import acquisitionMetadataMarshal

from django.core.urlresolvers import reverse
from omero.constants.namespaces import NSBULKANNOTATIONS
from omero.model.enums import UnitsLength
from omero_model_ImageI import ImageI

from omero.rtypes import rstring, rdouble
from omero.rtypes import wrap


class TestAcquisitionMetadata(IWebTest):

    def test_objective_settings(self):

        iid = self.create_test_image(size_c=2, session=self.sf).id.val
        conn = omero.gateway.BlitzGateway(client_obj=self.client)
        image = conn.getObject("Image", iid)

        # Create Objective
        update = conn.getUpdateService()
        objective = omero.model.ObjectiveI()
        objective.model = rstring("SuperZoom ABC")
        objective.manufacturer = rstring("ImageCo")
        objective.lensNA = rdouble(1.4)
        objective.nominalMagnification = rdouble(100)
        objective.calibratedMagnification = rdouble(100.1)
        objective.lotNumber = rstring("123")
        objective.serialNumber = rstring("abcdefX")

        immersions = list(conn.getEnumerationEntries("ImmersionI"))
        corrections = list(conn.getEnumerationEntries("CorrectionI"))
        objective.correction = corrections[0]._obj
        objective.immersion = immersions[0]._obj
        objective.instrument = update.saveAndReturnObject(
            omero.model.InstrumentI())
        objective = update.saveAndReturnObject(objective)

        settings = omero.model.ObjectiveSettingsI()
        settings.objective = objective
        settings = update.saveAndReturnObject(settings)

        image._obj.objectiveSettings = settings
        update.saveAndReturnObject(image._obj)

        # reload...
        image = conn.getObject("Image", iid)
        json_data = acquisitionMetadataMarshal(image)
        print(json_data)
        assert json_data["objectiveSettings"] == {
            "id": settings.id.val,
            "objective": {
                "id": objective.id.val,
                "model": "SuperZoom ABC",
                "manufacturer": "ImageCo",
                "lensNA": 1.4,
                "nominalMagnification": 100,
                "calibratedMagnification": 100.1,
                "lotNumber": "123",
                "serialNumber": "abcdefX",
                "immersion": immersions[0].value,
                "correction": corrections[0].value
            }
        }


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
        html = rsp.content.decode("utf-8")
        # Units are µm by default
        value = "Pixels Size (XYZ) ("+u"µm):"
        assert value in html

        # Now save units as PIXELs and view again
        conn = omero.gateway.BlitzGateway(client_obj=self.client)
        i = conn.getObject("Image", iid)
        u = omero.model.LengthI(1.2, UnitsLength.PIXEL)
        p = i.getPrimaryPixels()._obj
        p.setPhysicalSizeX(u)
        p.setPhysicalSizeY(u)
        conn.getUpdateService().saveObject(p)

        # Should now be showing pixels
        rsp = get(self.django_client, request_url, data, status_code=200)
        html = rsp.content.decode("utf-8")
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
        assert "no_pixels" in rsp.content.decode("utf-8")


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
        fa = self.make_file_annotation(name=name, namespace=ns)
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
