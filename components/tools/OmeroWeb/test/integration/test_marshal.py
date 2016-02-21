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
Test json methods of webgateway
"""

import json

from omero.model import DatasetI, DatasetImageLinkI, ImageI
from omero.rtypes import rstring
from django.core.urlresolvers import reverse

from weblibrary import IWebTest, _get_response


class TestImgDetail(IWebTest):
    """
    Tests json for webgateway/imgData/
    """

    def test_image_detail(self):
        """
        Tests imageData json for an imported image
        """
        userName = "%s %s" % (self.user.firstName.val, self.user.lastName.val)

        # Import "tinyTest.d3d.dv" and get ImageID
        pids = self.import_image(client=self.client, skip=None)
        pixels = self.query.get("Pixels", long(pids[0]))
        iid = pixels.image.id.val

        json_url = reverse('webgateway.views.imageData_json', args=[iid])
        data = {}
        imgData = _get_response_json(self.django_client, json_url,
                                     data, status_code=200)

        # Useful for debugging
        print imgData

        # Not a big image - tiles should be False with no other tiles metadata
        assert imgData['tiles'] is False
        assert 'levels' not in imgData
        assert 'zoomLevelScaling' not in imgData
        assert 'tile_size' not in imgData

        # Channels metadata
        assert len(imgData['channels']) == 1
        assert imgData['channels'][0] == {
            'color': "808080",
            'active': True,
            'window': {
                'max': 651,
                'end': 651,
                'start': 88,
                'min': 88
            },
            'emissionWave': 500,
            'label': "500"
        }
        assert imgData['pixel_range'] == [-32768, 32767]
        assert imgData['nominalMagnification'] == 100
        assert imgData['rdefs'] == {
            'defaultT': 0,
            'model': "greyscale",
            'invertAxis': False,
            'projection': "normal",
            'defaultZ': 2
        }

        # Core image metadata
        assert imgData['size'] == {
            'width': 20,
            'c': 1,
            'z': 5,
            't': 6,
            'height': 20
        }
        assert imgData['meta']['pixelsType'] == "int16"
        assert imgData['meta']['projectName'] == "Multiple"
        assert imgData['meta']['imageId'] == iid
        assert imgData['meta']['imageAuthor'] == userName
        assert imgData['meta']['datasetId'] is None
        assert imgData['meta']['projectDescription'] == ""
        assert imgData['meta']['datasetName'] == "Multiple"
        assert imgData['meta']['wellSampleId'] == ""
        assert imgData['meta']['projectId'] is None
        assert imgData['meta']['imageDescription'] == \
            "X:(88 107) Y:(169 188) Z:(9 13 1) T:(2 7 1)"
        assert imgData['meta']['wellId'] == ""
        assert imgData['meta']['imageName'] == "tinyTest.d3d.dv"
        assert imgData['meta']['datasetDescription'] == ""
        assert imgData['meta']['datasets'] == []
        # Don't know exact timestamp of import
        assert 'imageTimestamp' in imgData['meta']

        # Permissions - User is owner. All perms are True
        assert imgData['perms'] == {
            'canAnnotate': True,
            'canEdit': True,
            'canDelete': True,
            'canLink': True
        }

        # Sizes for split channel image
        assert imgData['split_channel'] == {
            'c': {
                'width': 46,
                'gridy': 1,
                'border': 2,
                'gridx': 2,
                'height': 24
            },
            'g': {
                'width': 24,
                'gridy': 1,
                'border': 2,
                'gridx': 1,
                'height': 24
            }
        }

        # Now test image in Datasets
        datasetNames = ["Dataset1", "Dataset2"]
        datasetIds = []
        for dname in datasetNames:
            dataset = DatasetI()
            dataset.name = rstring(dname)
            link = DatasetImageLinkI()
            link.child = ImageI(iid, False)
            link.parent = dataset
            link = self.update.saveAndReturnObject(link)
            datasetIds.append(link.parent.id.val)
        datasets = [{'id': datasetIds[d],
                     'name': datasetNames[d],
                     'description': ''} for d in range(2)]

        imgData = _get_response_json(self.django_client, json_url,
                                     data, status_code=200)
        assert imgData['meta']['datasetId'] in datasetIds
        assert imgData['meta']['datasetName'] in datasetNames
        assert imgData['meta']['datasets'] == datasets


# Helpers
def _get_response_json(django_client, request_url,
                       query_string, status_code=200):
    rsp = _get_response(django_client, request_url, query_string, status_code)
    # allow 'text/javascript'?
    # assert rsp.get('Content-Type') == 'application/json'
    return json.loads(rsp.content)
