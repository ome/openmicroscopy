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
   Library for Web integration tests
"""

import library as lib

from django.test import Client
from django.test.client import MULTIPART_CONTENT
from django.core.urlresolvers import reverse
from urllib import urlencode


class IWebTest(lib.ITest):
    """
    Abstract class derived from ITest which implements helpers for creating
    Django clients using django.test
    """

    @classmethod
    def setup_class(cls):
        """Returns a logged in Django test client."""
        super(IWebTest, cls).setup_class()
        cls.django_clients = []
        omeName = cls.ctx.userName
        cls.django_client = cls.new_django_client(omeName, omeName)
        rootpass = cls.root.ic.getProperties().getProperty('omero.rootpass')
        cls.django_root_client = cls.new_django_client("root", rootpass)

    @classmethod
    def teardown_class(cls):
        logout_url = reverse('weblogout')
        for client in cls.django_clients:
            data = {'csrfmiddlewaretoken': client.cookies['csrftoken'].value}
            response = client.post(logout_url, data=data)
            assert response.status_code == 302
        super(IWebTest, cls).teardown_class()

    @classmethod
    def new_django_client(cls, name, password):
        django_client = Client(enforce_csrf_checks=True)
        login_url = reverse('weblogin')

        response = django_client.get(login_url)
        assert response.status_code == 200
        csrf_token = django_client.cookies['csrftoken'].value

        data = {
            'server': 1,
            'username': name,
            'password': password,
            'csrfmiddlewaretoken': csrf_token
        }
        response = django_client.post(login_url, data)
        assert response.status_code == 302
        cls.django_clients.append(django_client)
        return django_client


# Helpers
def _response(django_client, request_url, method, data, status_code=403,
              content_type=MULTIPART_CONTENT, **extra):
    response = getattr(django_client, method)(request_url,
                                              data=data,
                                              content_type=content_type,
                                              **extra)
    assert response.status_code == status_code, response
    return response


# POST
def _post_response(django_client, request_url, data, status_code=403,
                   content_type=MULTIPART_CONTENT, **extra):
    return _response(django_client, request_url, method='post', data=data,
                     status_code=status_code, content_type=content_type,
                     **extra)


def _csrf_post_response(django_client, request_url, data, status_code=200,
                        content_type=MULTIPART_CONTENT):
    csrf_token = django_client.cookies['csrftoken'].value
    extra = {'HTTP_X_CSRFTOKEN': csrf_token}
    return _post_response(django_client, request_url, data=data,
                          status_code=status_code, content_type=content_type,
                          **extra)


# DELETE
def _delete_response(django_client, request_url, data, status_code=403,
                     content_type=MULTIPART_CONTENT, **extra):
    return _response(django_client, request_url, method='delete', data=data,
                     status_code=status_code, content_type=content_type,
                     **extra)


def _csrf_delete_response(django_client, request_url, data, status_code=200,
                          content_type=MULTIPART_CONTENT):
    csrf_token = django_client.cookies['csrftoken'].value
    extra = {'HTTP_X_CSRFTOKEN': csrf_token}
    return _delete_response(django_client, request_url, data=data,
                            status_code=status_code, content_type=content_type,
                            **extra)


# GET
def _get_response(django_client, request_url, query_string, status_code=405):
    query_string = urlencode(query_string.items())
    response = django_client.get('%s?%s' % (request_url, query_string))
    assert response.status_code == status_code, response
    return response


def _csrf_get_response(django_client, request_url, query_string,
                       status_code=200):
    csrf_token = django_client.cookies['csrftoken'].value
    query_string['csrfmiddlewaretoken'] = csrf_token
    return _get_response(django_client, request_url, query_string,
                         status_code)
