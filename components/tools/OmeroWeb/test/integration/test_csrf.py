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
import test.integration.library as lib

from urllib import urlencode

from django.test import Client
from django.core.urlresolvers import reverse


@pytest.fixture(scope='function')
def itest(request):
    """
    Returns a new L{test.integration.library.ITest} instance.  With
    attached finalizer so that pytest will clean it up.
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
def image_with_channels(request, itest, client):
    """
    Returns a new foundational Image with Channel objects attached for
    view method testing.
    """
    pixels = itest.pix(client=client)
    for the_c in range(pixels.getSizeC().val):
        channel = omero.model.ChannelI()
        channel.logicalChannel = omero.model.LogicalChannelI()
        pixels.addChannel(channel)
    image = pixels.getImage()
    return client.getSession().getUpdateService().saveAndReturnObject(image)


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
    return django_client


class TestCsrf(object):
    """
    Tests to ensure that Django CSRF middleware for OMERO.web is enabled and
    working correctly.
    """

    def test_csrf_middleware_enabled(self, client):
        """
        If the CSRF middleware is enabled login attempts that do not include
        the CSRF token should fail with an HTTP 403 (forbidden) status code.
        """
        login_url = reverse('weblogin')
        # https://docs.djangoproject.com/en/dev/ref/contrib/csrf/#testing
        django_client = Client(enforce_csrf_checks=True)

        data = {
            'server': 1,
            'username': client.getProperty('omero.user'),
            'password': client.getProperty('omero.pass')
        }
        response = django_client.post(login_url, data)
        assert response.status_code == 403

    def test_edit_channel_names(
            self, client, django_client, image_with_channels):
        """
        CSRF protection does not check `GET` requests so we need to be sure
        that this request results in an HTTP 405 (method not allowed) status
        code.
        """
        data = {'channel0': 'foobar'}
        query_string = urlencode(data.items())
        request_url = reverse(
            'edit_channel_names', args=[image_with_channels.id.val]
        )

        response = django_client.get('%s?%s' % (request_url, query_string))
        assert response.status_code == 405

        csrf_token = django_client.cookies['csrftoken'].value
        data['csrfmiddlewaretoken'] = csrf_token
        response = django_client.post(request_url, data=data)
        assert response.status_code == 200
