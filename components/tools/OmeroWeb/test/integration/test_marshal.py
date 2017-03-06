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

from django.core.urlresolvers import reverse

from omeroweb.testlib import IWebTest, _get_response


class TestImgDetail(IWebTest):

    """
    Tests json for webgateway/imgData/
    """

    def test_image_detail(self):
        """
        Download of archived files for a non-SPW Image.
        """
        user_name = "%s %s" % (self.user.firstName.val, self.user.lastName.val)

        # Import "tinyTest.d3d.dv" and get ImageID
        pids = self.import_image_with_metadata(client=self.client)
        pixels = self.query.get("Pixels", long(pids[0]))
        iid = pixels.image.id.val

        json_url = reverse('webgateway.views.imageData_json', args=[iid])
        data = {}
        img_data = _get_response_json(self.django_client, json_url,
                                      data, status_code=200)

        # Not a big image - tiles should be False with no other tiles metadata
        assert img_data['tiles'] is False
        assert 'levels' not in img_data
        assert 'zoomLevelScaling' not in img_data
        assert 'tile_size' not in img_data

        # Channels metadata
        assert len(img_data['channels']) == 1
        assert img_data['channels'][0] == {
            'color': "808080",
            'active': True,
            'window': {
                'max': 651,
                'end': 651,
                'start': 88,
                'min': 88
            },
            'reverseIntensity': False,
            'emissionWave': 500,
            'label': "500"
        }
        assert img_data['pixel_range'] == [-32768, 32767]
        assert img_data['nominalMagnification'] == 100
        assert img_data['rdefs'] == {
            'defaultT': 0,
            'model': "greyscale",
            'invertAxis': False,
            'projection': "normal",
            'defaultZ': 2
        }

        # Core image metadata
        assert img_data['size'] == {
            'width': 20,
            'c': 1,
            'z': 5,
            't': 6,
            'height': 20
        }
        assert img_data['meta']['pixelsType'] == "int16"
        assert img_data['meta']['projectName'] == "Multiple"
        assert img_data['meta']['imageId'] == iid
        assert img_data['meta']['imageAuthor'] == user_name
        assert img_data['meta']['datasetId'] is None
        assert img_data['meta']['projectDescription'] == ""
        assert img_data['meta']['datasetName'] == "Multiple"
        assert img_data['meta']['wellSampleId'] == ""
        assert img_data['meta']['projectId'] is None
        assert img_data['meta']['imageDescription'] == \
            "X:(88 107) Y:(169 188) Z:(9 13 1) T:(2 7 1)"
        assert img_data['meta']['wellId'] == ""
        assert img_data['meta']['imageName'] == "tinyTest.d3d.dv"
        assert img_data['meta']['datasetDescription'] == ""
        # Don't know exact timestamp of import
        assert 'imageTimestamp' in img_data['meta']

        # Permissions - User is owner. All perms are True
        assert img_data['perms'] == {
            'canAnnotate': True,
            'canEdit': True,
            'canDelete': True,
            'canLink': True
        }

        # Sizes for split channel image
        assert img_data['split_channel'] == {
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


# Helpers
def _get_response_json(django_client, request_url,
                       query_string, status_code=200):
    rsp = _get_response(django_client, request_url, query_string, status_code)
    # allow 'text/javascript'?
    # assert rsp.get('Content-Type') == 'application/json'
    return json.loads(rsp.content)
