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
        omeName = cls.sf.getAdminService().getEventContext().userName
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
def _post_response(django_client, request_url, data, status_code=403):
    response = django_client.post(request_url, data=data)
    assert response.status_code == status_code
    return response


def _csrf_post_response(django_client, request_url, data, status_code=200):
    csrf_token = django_client.cookies['csrftoken'].value
    data['csrfmiddlewaretoken'] = csrf_token
    return _post_response(django_client, request_url, data, status_code)


def _get_response(django_client, request_url, query_string, status_code=405):
    query_string = urlencode(query_string.items())
    response = django_client.get('%s?%s' % (request_url, query_string))
    assert response.status_code == status_code
    return response


def _csrf_get_response(django_client, request_url, query_string,
                       status_code=200):
    csrf_token = django_client.cookies['csrftoken'].value
    query_string['csrfmiddlewaretoken'] = csrf_token
    return _get_response(django_client, request_url, query_string,
                         status_code)
