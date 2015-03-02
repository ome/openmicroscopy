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
import pytest
import library as lib

from urllib import urlencode
from django.test import Client
from django.core.urlresolvers import reverse


@pytest.fixture(scope='function')
def itest(request):
    """
    Returns a new L{test.integration.library.ITest} instance. With attached
    finalizer so that pytest will clean it up.
    """
    o = lib.ITest()
    o.setup_class()

    def finalizer():
        o.teardown_class()
    request.addfinalizer(finalizer)
    return o


@pytest.fixture(scope='function')
def client(request, itest):
    """Returns a new user client in a read-only group."""
    # Use group read-only permissions (not private) by default
    return itest.new_client(perms='rwr---')


@pytest.fixture(scope='function')
def django_client(request, client):
    """Returns a logged in Django test client."""
    django_client = Client(enforce_csrf_checks=True)
    login_url = reverse('weblogin')

    response = django_client.get(login_url)
    assert response.status_code == 200
    csrf_token = django_client.cookies['csrftoken'].value

    data = {
        'server': 1,
        'username': client.getProperty('omero.user'),
        'password': client.getProperty('omero.pass'),
        'csrfmiddlewaretoken': csrf_token
    }
    response = django_client.post(login_url, data)
    assert response.status_code == 302

    def finalizer():
        logout_url = reverse('weblogout')
        data = {'csrfmiddlewaretoken': csrf_token}
        response = django_client.post(logout_url, data=data)
        assert response.status_code == 302
    request.addfinalizer(finalizer)
    return django_client


@pytest.fixture(scope='function')
def update_service(request, client):
    """Returns a new OMERO update service."""
    return client.getSession().getUpdateService()


class TestImgDetail(object):
    """
    Tests json for webgateway/imgData/
    """

    def test_image_detail(self, itest, client, django_client):
        """
        Download of archived files for a non-SPW Image.
        """

        image = itest.importSingleImage(client=client)

        json_url = reverse('webgateway.views.imageData_json', args=[image.id.val])
        data = {}
        imgData = _get_response_json(django_client, json_url, data, status_code=200)
        print imgData
        # Not a big image - tiles should be False with no other tiles metadata
        assert imgData['tiles'] == False
        assert 'levels' not in imgData
        assert 'zoomLevelScaling' not in imgData
        assert 'tile_size' not in imgData

        # Channels metadata
        assert len(imgData['channels']) == 1
        assert imgData['channels'][0] == {
                                    'color': "808080",
                                    'active': True,
                                    'window': {
                                        'max': 255,
                                        'end': 255,
                                        'start': 0,
                                        'min': 0
                                    },
                                    'emissionWave': None,
                                    'label': "0"
                                }
        assert imgData['pixel_range'] == [0, 255]
        assert imgData['rdefs'] == {
                            'defaultT': 0,
                            'model': "greyscale",
                            'invertAxis': False,
                            'projection': "normal",
                            'defaultZ': 0
                        }

        # Core image metadata
        assert imgData['size'] == {
                                    'width': 512,
                                    'c': 1,
                                    'z': 1,
                                    't': 1,
                                    'height': 512
                                }
        assert imgData['meta']['pixelsType'] == "uint8"


# Helpers
def _post_response(django_client, request_url, data, status_code=200):
    response = django_client.post(request_url, data=data)
    assert response.status_code == status_code
    return response


def _csrf_post_response(django_client, request_url, data, status_code=200):
    csrf_token = django_client.cookies['csrftoken'].value
    data['csrfmiddlewaretoken'] = csrf_token
    return _post_response(django_client, request_url, data, status_code)


def _get_response(django_client, request_url, query_string, status_code=200):
    query_string = urlencode(query_string.items())
    response = django_client.get('%s?%s' % (request_url, query_string))
    assert response.status_code == status_code
    return response


def _get_response_json(django_client, request_url,
                       query_string, status_code=200):
    rsp = _get_response(django_client, request_url, query_string, status_code)
    # allow 'text/javascript'?
    # assert rsp.get('Content-Type') == 'application/json'
    return json.loads(rsp.content)


def _login_django_client(request, client, username, password):

    django_client = Client(enforce_csrf_checks=True)
    login_url = reverse('weblogin')

    response = django_client.get(login_url)
    assert response.status_code == 200
    csrf_token = django_client.cookies['csrftoken'].value

    data = {
        'server': 1,
        'username': username,
        'password': password,
        'csrfmiddlewaretoken': csrf_token
    }
    response = django_client.post(login_url, data)
    assert response.status_code == 302

    def finalizer():
        logout_url = reverse('weblogout')
        data = {'csrfmiddlewaretoken': csrf_token}
        response = django_client.post(logout_url, data=data)
        assert response.status_code == 302
    request.addfinalizer(finalizer)
    return django_client
