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
from omero.rtypes import rstring
import pytest
import test.integration.library as lib

import json

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
def new_tag(request, itest, client):
    """
    Returns a new Tag objects
    """
    tag = omero.model.TagAnnotationI()
    tag.textValue = rstring(itest.uuid())
    tag.ns = rstring("pytest")
    return client.getSession().getUpdateService().saveAndReturnObject(tag)

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

        logout_url = reverse('weblogout')
        response = django_client.post(logout_url)
        assert response.status_code == 403

    def test_add_comment(
            self, client, django_client, image_with_channels):
        """
        CSRF protection does not check `GET` requests so we need to be sure
        that this request results in an HTTP 405 (method not allowed) status
        code.
        """
        request_url = reverse('annotate_comment')
        data = {
            'comment': 'foobar',
            'image': image_with_channels.id.val
        }
        self.csrf_post_reponse(django_client, request_url, data)

    def test_add_and_rename_container(
            self, client, django_client):
        """
        CSRF protection does not check `GET` requests so we need to be sure
        that this request results in an HTTP 405 (method not allowed) status
        code.
        """
        # Add project
        request_url = reverse("manage_action_containers", args=["addnewcontainer"])
        data = {
            'folder_type': 'project',
            'name': 'foobar'
        }
        response = self.csrf_post_reponse(django_client, request_url, data)
        pid = json.loads(response.content).get("id")

        # Add dataset to the project
        request_url = reverse("manage_action_containers", args=["addnewcontainer", "project", pid])
        data = {
            'folder_type': 'dataset',
            'name': 'foobar'
        }
        self.csrf_post_reponse(django_client, request_url, data)

        # Rename project
        request_url = reverse("manage_action_containers", args=["savename", "project", pid])
        data = {
            'name': 'anotherfoobar'
        }
        self.csrf_post_reponse(django_client, request_url, data)

        # Change project description
        request_url = reverse("manage_action_containers", args=["savedescription", "project", pid])
        data = {
            'description': 'anotherfoobar'
        }
        self.csrf_post_reponse(django_client, request_url, data)

    def test_add_and_remove_tag(
            self, client, django_client, image_with_channels, new_tag):
        """
        CSRF protection does not check `GET` requests so we need to be sure
        that this request results in an HTTP 405 (method not allowed) status
        code.
        """
        # Add tag
        request_url = reverse('annotate_tags')
        data = {
            'image': image_with_channels.id.val,
            'filter_mode': 'any',
            'filter_owner_mode': 'all',
            'index': 0,
            'newtags-0-description': '',
            'newtags-0-tag': 'foobar',
            'newtags-0-tagset': '',
            'newtags-INITIAL_FORMS': 0,
            'newtags-MAX_NUM_FORMS': 1000,
            'newtags-TOTAL_FORMS': 1,
            'tags': new_tag.id.val
        }
        self.csrf_post_reponse(django_client, request_url, data)

        # Remove tag
        request_url = reverse("manage_action_containers", args=["remove", "tag", new_tag.id.val])
        data = {
            'index': 0,
            'parent': "image-%i" % image_with_channels.id.val
        }
        self.csrf_post_reponse(django_client, request_url, data)

        # Delete tag
        request_url = reverse("manage_action_containers", args=["delete", "tag", new_tag.id.val])
        self.csrf_post_reponse(django_client, request_url, data={})

    def test_paste_move_remove_image(
            self, client, django_client, image_with_channels):
        """
        CSRF protection does not check `GET` requests so we need to be sure
        that this request results in an HTTP 405 (method not allowed) status
        code.
        """
        # Add dataset
        request_url = reverse("manage_action_containers", args=["addnewcontainer"])
        data = {
            'folder_type': 'dataset',
            'name': 'foobar'
        }
        response = self.csrf_post_reponse(django_client, request_url, data)
        did = json.loads(response.content).get("id")

        # Copy image
        request_url = reverse("manage_action_containers", args=["paste", "image", image_with_channels.id.val])
        data = {
            'destination': "dataset-%i" % did
        }
        self.csrf_post_reponse(django_client, request_url, data)

        # Move image
        request_url = reverse("manage_action_containers", args=["move", "image", image_with_channels.id.val])
        data = {
            'destination': 'orphaned-0',
            'parent': 'dataset-%i' % did
        }
        self.csrf_post_reponse(django_client, request_url, data)

        # Remove image
        request_url = reverse("manage_action_containers", args=["remove", "image", image_with_channels.id.val])
        data = {
            'parent': 'dataset-%i' % did
        }
        self.csrf_post_reponse(django_client, request_url, data)

        # Delete image
        request_url = reverse("manage_action_containers", args=["deletemany"])
        data = {
            'child': 'on',
            'dataset': did
        }
        self.csrf_post_reponse(django_client, request_url, data)

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
        self.csrf_get_reponse(django_client, request_url, query_string, data)


    # Helpers
    def csrf_post_reponse(self, django_client, request_url, data):
        response = django_client.post(request_url, data=data)
        assert response.status_code == 403

        csrf_token = django_client.cookies['csrftoken'].value
        data['csrfmiddlewaretoken'] = csrf_token
        response = django_client.post(request_url, data=data)
        assert response.status_code == 200
        return response

    def csrf_get_reponse(self, django_client, request_url, query_string, data):
        response = django_client.get('%s?%s' % (request_url, query_string))
        assert response.status_code == 405

        csrf_token = django_client.cookies['csrftoken'].value
        data['csrfmiddlewaretoken'] = csrf_token
        response = django_client.post(request_url, data=data)
        assert response.status_code == 200
        return response
