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
from weblibrary import IWebTest
from weblibrary import _csrf_post_response, _get_response
# from weblibrary import _csrf_delete_response, _delete_response

import json

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
        """Returns a new OMERO Dataset with required fields set."""
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

    def test_link_project_datasets(self, project, datasets):
        # Link Project to Datasets
        request_url = reverse("api_links")
        pid = project.id.val
        dids = [d.id.val for d in datasets]
        data = {
            'project': {pid: {'dataset': dids}}
        }
        json = _csrf_post_response_json(self.django_client, request_url, data)
        assert json == {"success": True}

        # Check links
        request_url = reverse("api_datasets")
        json = _get_response_json(self.django_client, request_url, {'id': pid})
        # Expect a single Dataset with correct id
        assert len(json['datasets']) == 2
        assert json['datasets'][0]['id'] == dids[0]
        assert json['datasets'][1]['id'] == dids[1]

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
        json = _csrf_post_response_json(self.django_client, request_url, data)
        assert json == {"success": True}

        # Check links
        request_url = reverse("api_images")
        # First Dataset has single image
        json = _get_response_json(self.django_client, request_url, {'id': dids[0]})
        assert len(json['images']) == 1
        assert json['images'][0]['id'] == iids[0]
        # Second Dataset has both images
        json = _get_response_json(self.django_client, request_url, {'id': dids[1]})
        assert len(json['images']) == 2
        assert json['images'][0]['id'] == iids[0]
        assert json['images'][1]['id'] == iids[1]


def _get_response_json(django_client, request_url, query_string):
    rsp = _get_response(django_client, request_url,
                        query_string, status_code=200)
    return json.loads(rsp.content)


def _csrf_post_response_json(django_client, request_url, data):
    rsp = _csrf_post_response(django_client,
                              request_url,
                              json.dumps(data),
                              content_type="application/json")
    return json.loads(rsp.content)
