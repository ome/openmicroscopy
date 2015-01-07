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
Simple integration tests to ensure that the CSRF middleware is enabled and
working correctly.
"""

import omero
import omero.clients

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
    o.setup_method(None)

    def finalizer():
        o.teardown_method(None)
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


class TestCsrf(object):
    """
    Tests to ensure that Django CSRF middleware for OMERO.web is enabled and
    working correctly.
    """

    def test_copy_past_rendering_settings_from_image(self, itest, client,
                                                     django_client):
        # Create 2 images with 2 channels each
        iid1 = itest.createTestImage(sizeC=2,
                                     session=client.getSession()).id.val
        iid2 = itest.createTestImage(sizeC=2,
                                     session=client.getSession()).id.val

        conn = omero.gateway.BlitzGateway(client_obj=client)

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

        assert False == image1.isGreyscaleRenderingModel()
        assert True == image2.isGreyscaleRenderingModel()

        # copy rendering settings from image1 via ID
        request_url = reverse('webgateway.views.copy_image_rdef_json')
        data = {
            "fromid": iid1
        }
        _get_reponse(django_client, request_url, data, status_code=200)

        # paste rendering settings to image2
        data = {
            'toids': iid2
        }

        _csrf_post_reponse(django_client, request_url, data)

        image2 = conn.getObject("Image", iid2)
        assert False == image2.isGreyscaleRenderingModel()

    def test_copy_past_rendering_settings_from_url(self, itest, client,
                                                   django_client):
        # Create 2 images with 2 channels each
        iid1 = itest.createTestImage(sizeC=2,
                                     session=client.getSession()).id.val
        iid2 = itest.createTestImage(sizeC=2,
                                     session=client.getSession()).id.val

        conn = omero.gateway.BlitzGateway(client_obj=client)

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

        assert False == image1.isGreyscaleRenderingModel()
        assert True == image2.isGreyscaleRenderingModel()

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
        _get_reponse(django_client, request_url, data, status_code=200)

        # paste rendering settings to image2
        data = {
            'toids': iid2
        }
        _csrf_post_reponse(django_client, request_url, data)

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
        assert False == image2.isGreyscaleRenderingModel()


# Helpers
def _post_reponse(django_client, request_url, data, status_code=403):
    response = django_client.post(request_url, data=data)
    assert response.status_code == status_code
    return response


def _csrf_post_reponse(django_client, request_url, data, status_code=200):
    csrf_token = django_client.cookies['csrftoken'].value
    data['csrfmiddlewaretoken'] = csrf_token
    return _post_reponse(django_client, request_url, data, status_code)


def _get_reponse(django_client, request_url, query_string, status_code=405):
    query_string = urlencode(query_string.items())
    response = django_client.get('%s?%s' % (request_url, query_string))
    assert response.status_code == status_code
    return response


def _csrf_get_reponse(django_client, request_url, query_string,
                      status_code=200):
    csrf_token = django_client.cookies['csrftoken'].value
    query_string['csrfmiddlewaretoken'] = csrf_token
    return _get_reponse(django_client, request_url, query_string, status_code)
