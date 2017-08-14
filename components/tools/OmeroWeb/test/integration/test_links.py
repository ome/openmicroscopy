#!/usr/bin/env python
# -*- coding: utf-8 -*-

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
Tests creation and deletion of links between e.g. Projects & Datasets etc.
"""

import omero

from omero.rtypes import rstring
from omeroweb.testlib import IWebTest
from omeroweb.testlib import post_json, get_json, delete_json

from time import sleep

from django.core.urlresolvers import reverse

import pytest


class TestLinks(IWebTest):
    """
    Tests creation and deletion of links between
    e.g. Projects & Datasets etc.
    """

    @pytest.fixture
    def project(self):
        """Returns a new OMERO Project with required fields set."""
        project = omero.model.ProjectI()
        project.name = rstring(self.uuid())
        return self.update.saveAndReturnObject(project)

    @pytest.fixture
    def dataset(self):
        """Returns a new OMERO Dataset with required fields set."""
        dataset = omero.model.DatasetI()
        dataset.name = rstring(self.uuid())
        return self.update.saveAndReturnObject(dataset)

    @pytest.fixture
    def datasets(self):
        """Returns 2 new OMERO Datasets with required fields set."""
        dataset = omero.model.DatasetI()
        dataset.name = rstring("A_%s" % self.uuid())
        dataset2 = omero.model.DatasetI()
        dataset2.name = rstring("B_%s" % self.uuid())
        return self.update.saveAndReturnArray([dataset, dataset2])

    @pytest.fixture
    def images(self):
        image = self.new_image(name="A_%s" % self.uuid())
        image2 = self.new_image(name="B_%s" % self.uuid())
        return self.update.saveAndReturnArray([image, image2])

    @pytest.fixture
    def screens(self):
        """Returns 2 new OMERO Screens with required fields set."""
        screen = omero.model.ScreenI()
        screen.name = rstring("A_%s" % self.uuid())
        screen2 = omero.model.ScreenI()
        screen2.name = rstring("B_%s" % self.uuid())
        return self.update.saveAndReturnArray([screen, screen2])

    @pytest.fixture
    def plates(self):
        """Returns 2 new OMERO Plates with required fields set."""
        plate = omero.model.PlateI()
        plate.name = rstring("A_%s" % self.uuid())
        plate2 = omero.model.PlateI()
        plate2.name = rstring("B_%s" % self.uuid())
        return self.update.saveAndReturnArray([plate, plate2])

    def test_link_project_datasets(self, project, datasets):
        # Link Project to Datasets
        request_url = reverse("api_links")
        pid = project.id.val
        dids = [d.id.val for d in datasets]
        data = {
            'project': {pid: {'dataset': dids}}
        }
        rsp = post_json(self.django_client, request_url, data)
        assert rsp == {"success": True}

        # Check links
        request_url = reverse("api_datasets")
        rsp = get_json(self.django_client, request_url, {'id': pid})
        # Expect a single Dataset with correct id
        assert len(rsp['datasets']) == 2
        assert rsp['datasets'][0]['id'] == dids[0]
        assert rsp['datasets'][1]['id'] == dids[1]

    def test_link_datasets_images(self, datasets, images):
        # Link Datasets to Images
        request_url = reverse("api_links")
        dids = [d.id.val for d in datasets]
        iids = [i.id.val for i in images]
        # Link first dataset to first image,
        # Second dataset linked to both images
        data = {
            'dataset': {dids[0]: {'image': [iids[0]]},
                        dids[1]: {'image': iids}}
        }
        rsp = post_json(self.django_client, request_url, data)
        assert rsp == {"success": True}

        # Check links
        images_url = reverse("api_images")
        # First Dataset has single image
        rsp = get_json(self.django_client, images_url, {'id': dids[0]})
        assert len(rsp['images']) == 1
        assert rsp['images'][0]['id'] == iids[0]
        # Second Dataset has both images
        rsp = get_json(self.django_client, images_url, {'id': dids[1]})
        assert len(rsp['images']) == 2
        assert rsp['images'][0]['id'] == iids[0]
        assert rsp['images'][1]['id'] == iids[1]

        # Link BOTH images to first Dataset. Shouldn't get any
        # Validation Exception, even though first image is already linked
        data = {
            'dataset': {dids[0]: {'image': iids}}
        }
        rsp = post_json(self.django_client, request_url, data)
        assert rsp == {"success": True}
        # Check first Dataset now has both images
        rsp = get_json(self.django_client, images_url, {'id': dids[0]})
        assert len(rsp['images']) == 2
        assert rsp['images'][0]['id'] == iids[0]
        assert rsp['images'][1]['id'] == iids[1]

    def test_link_unlink_tagset_tags(self):
        """
        Tests linking of tagset to tag, then unlinking
        """
        tag = self.make_tag()
        tagset = self.make_tag(ns=omero.constants.metadata.NSINSIGHTTAGSET)
        tagId = tag.id.val
        tagsetId = tagset.id.val

        links_url = reverse("api_links")
        # Link tagset to tag
        data = {
            'tagset': {tagsetId: {'tag': [tagId]}}
        }
        rsp = post_json(self.django_client, links_url, data)
        assert rsp == {"success": True}

        # Check that tag is listed under tagset...
        tags_url = reverse("api_tags_and_tagged")
        r = get_json(self.django_client, tags_url, {'id': tagsetId})
        assert len(r['tags']) == 1
        assert r['tags'][0]['id'] == tagId

        # Unlink first Tag from Tagset
        # data {} is same as for creating link above
        response = delete_json(self.django_client, links_url, data)
        assert response["success"]

        # Since the Delete is ansync - need to check repeatedly for deletion
        for i in range(10):
            rsp = get_json(self.django_client, tags_url, {'id': tagsetId})
            if len(rsp['tags']) == 0:
                break
            sleep(0.5)
        # Check that link has been deleted
        assert len(rsp['tags']) == 0

    def test_unlink_screen_plate(self, screens, plates):
        # Link both plates to both screens
        request_url = reverse("api_links")
        sids = [s.id.val for s in screens]
        pids = [p.id.val for p in plates]
        data = {
            'screen': {sids[0]: {'plate': pids},
                       sids[1]: {'plate': pids}}
        }
        rsp = post_json(self.django_client, request_url, data)
        assert rsp == {"success": True}

        # Confirm that first Screen linked to 2 Plates
        plates_url = reverse("api_plates")
        rsp = get_json(self.django_client, plates_url, {'id': sids[0]})
        assert len(rsp['plates']) == 2

        # Unlink first Plate from first Screen
        request_url = reverse("api_links")
        data = {
            'screen': {sids[0]: {'plate': pids[:1]}}
        }
        response = delete_json(self.django_client, request_url, data)
        # Returns remaining link from 2nd Screen to first Plate
        assert response == {"success": True,
                            "screen": {str(sids[1]): {"plate": pids[:1]}}
                            }

        # Since the Delete is ansync - need to check repeatedly for deletion
        # by counting plates under screen...
        plates_url = reverse("api_plates")
        for i in range(10):
            rsp = get_json(self.django_client, plates_url, {'id': sids[0]})
            if len(rsp['plates']) == 1:
                break
            sleep(0.5)

        # Check that link has been deleted, leaving 2nd plate under 1st screen
        assert len(rsp['plates']) == 1
        assert rsp['plates'][0]['id'] == pids[1]
