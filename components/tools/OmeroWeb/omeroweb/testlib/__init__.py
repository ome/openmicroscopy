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
   Library for Web integration tests
"""

import json
import warnings

from django.test import Client
from django.test.client import MULTIPART_CONTENT
from django.core.urlresolvers import reverse
from urllib import urlencode

from omero.testlib import ITest


class IWebTest(ITest):
    """
    Abstract class derived from ITest which implements helpers for creating
    Django clients using django.test
    """

    @classmethod
    def setup_class(cls):
        """Returns a logged in Django test client."""
        super(IWebTest, cls).setup_class()
        cls.django_clients = []
        ome_name = cls.ctx.userName
        cls.django_client = cls.new_django_client(ome_name, ome_name)
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

    @classmethod
    def new_django_client_from_session_id(cls, session_id):
        django_client = Client(enforce_csrf_checks=True)
        index_url = reverse('webindex')

        data = {
            'server': 1,
            'bsession': session_id,
        }
        response = django_client.get(index_url, data)
        assert response.status_code == 200
        cls.django_clients.append(django_client)
        return django_client

    def import_image_with_metadata(self, client=None):
        """
        Imports tinyTest. This should be replaced.
        """
        filename = self.omero_dist / ".." / \
            "components" / "common" / "test" / "tinyTest.d3d.dv"
        return self.import_image(filename=filename, client=client, skip=None)


# Helpers
def _response(django_client, request_url, method, data=None, status_code=200,
              content_type=MULTIPART_CONTENT, **extra):
    response = getattr(django_client, method)(request_url,
                                              data=data,
                                              content_type=content_type,
                                              **extra)
    assert response.status_code == status_code, response
    return response


def csrf_response(django_client, request_url, method, data=None,
                  status_code=200, content_type=MULTIPART_CONTENT,
                  test_csrf_required=True):
    """
    Helper for testing post/put/delete with and without CSRF.

    :param django_client:   Django test Client
    :param request_url:     The url to request
    :parma method:          Http method, e.g. 'post'
    :param data:            A dict of data to include as json content
    :param status_code:     Verify that the response has this status
    :param content_type:    Content type for request
    :param test_csrf_required:  If True (default) check that request fails
                                when CSRF token is not added

    """
    # First check that this would fail with 403 without CSRF token
    if test_csrf_required:
        _response(django_client, request_url, method=method, data=data,
                  status_code=403, content_type=content_type)

    # Should work as expected with CSRF token
    csrf_token = django_client.cookies['csrftoken'].value
    extra = {'HTTP_X_CSRFTOKEN': csrf_token}
    return _response(django_client, request_url, method=method, data=data,
                     status_code=status_code, content_type=content_type,
                     **extra)


# POST
def post(django_client, request_url, data=None, status_code=200,
         content_type=MULTIPART_CONTENT):
    """
    Performs a POST request, and returns the response.

    :param django_client:   Django test Client
    :param request_url:     The url to request
    :param data:            A dict of data to include as json content
    :param status_code:     Verify that the response has this status
    :param content_type:

    """
    return csrf_response(django_client, request_url, "post", data=data,
                         status_code=status_code, content_type=content_type)


def post_json(django_client, request_url, data=None, status_code=200):
    """
    Performs a POST request, and returns the JSON response as a dict.

    :param django_client:   Django test Client
    :param request_url:     The url to request
    :param data:            A dict of data to include as json content
    :param status_code:     Verify that the response has this status

    """
    rsp = post(django_client, request_url, json.dumps(data),
               status_code=status_code, content_type='application/json')
    assert rsp.get('Content-Type') == 'application/json'
    return json.loads(rsp.content)


def _post_response(django_client, request_url, data, status_code=403,
                   content_type=MULTIPART_CONTENT, **extra):
    warnings.warn(
        "This method is deprecated as of OMERO 5.4.0. Use post_json",
        DeprecationWarning)
    return _response(django_client, request_url, method='post', data=data,
                     status_code=status_code, content_type=content_type,
                     **extra)


def _post_response_json(django_client, request_url, data, status_code=403,
                        content_type=MULTIPART_CONTENT, **extra):
    warnings.warn(
        "This method is deprecated as of OMERO 5.4.0. Use post_json",
        DeprecationWarning)
    rsp = _response(django_client, request_url, method='post', data=data,
                    status_code=status_code, content_type=content_type,
                    **extra)
    assert rsp.get('Content-Type') == 'application/json'
    return json.loads(rsp.content)


def _csrf_post_response(django_client, request_url, data, status_code=200,
                        content_type=MULTIPART_CONTENT):
    warnings.warn(
        "This method is deprecated as of OMERO 5.4.0. Use post_json",
        DeprecationWarning)
    csrf_token = django_client.cookies['csrftoken'].value
    extra = {'HTTP_X_CSRFTOKEN': csrf_token}
    return _post_response(django_client, request_url, data=data,
                          status_code=status_code, content_type=content_type,
                          **extra)


def _csrf_post_response_json(django_client, request_url,
                             query_string, status_code=200):
    warnings.warn(
        "This method is deprecated as of OMERO 5.4.0. Use post_json",
        DeprecationWarning)
    rsp = _csrf_post_response(django_client, request_url,
                              query_string, status_code)
    assert rsp.get('Content-Type') == 'application/json'
    return json.loads(rsp.content)


# POST json encoded as a string
def _csrf_post_json(django_client, request_url, data,
                    status_code=200, content_type='application/json'):
    warnings.warn(
        "This method is deprecated as of OMERO 5.4.0. Use post_json",
        DeprecationWarning)
    csrf_token = django_client.cookies['csrftoken'].value
    extra = {'HTTP_X_CSRFTOKEN': csrf_token}
    rsp = django_client.post(request_url, json.dumps(data),
                             status_code=status_code,
                             content_type=content_type,
                             **extra)
    print rsp
    assert rsp.status_code == status_code
    assert rsp.get('Content-Type') == 'application/json'
    return json.loads(rsp.content)


def put_json(django_client, request_url, data=None, status_code=200):
    """
    Performs a PUT request, and returns the JSON response as a dict.

    :param django_client:   Django test Client
    :param request_url:     The url to request
    :param data:            A dict of data to include as json content
    :param status_code:     Verify that the response has this status

    """
    rsp = csrf_response(django_client, request_url, 'put', json.dumps(data),
                        status_code=status_code,
                        content_type='application/json')
    assert rsp.get('Content-Type') == 'application/json'
    return json.loads(rsp.content)


# PUT json encoded as a string
def _csrf_put_json(django_client, request_url, data,
                   status_code=200, content_type='application/json'):
    warnings.warn(
        "This method is deprecated as of OMERO 5.4.0. Use put_json",
        DeprecationWarning)
    csrf_token = django_client.cookies['csrftoken'].value
    extra = {'HTTP_X_CSRFTOKEN': csrf_token}
    rsp = django_client.put(request_url, json.dumps(data),
                            status_code=status_code, content_type=content_type,
                            **extra)
    print rsp
    assert rsp.status_code == status_code
    assert rsp.get('Content-Type') == 'application/json'
    return json.loads(rsp.content)


# DELETE
def _delete_response(django_client, request_url, data, status_code=403,
                     content_type=MULTIPART_CONTENT, **extra):
    warnings.warn(
        "This method is deprecated as of OMERO 5.4.0. Use delete_json",
        DeprecationWarning)
    return _response(django_client, request_url, method='delete', data=data,
                     status_code=status_code, content_type=content_type,
                     **extra)


def delete_json(django_client, request_url, data=None, status_code=200):
    """
    Performs a DELETE request, and returns the JSON response as a dict.

    :param django_client:   Django test Client
    :param request_url:     The url to request
    :param data:            A dict of data to include as json content
    :param status_code:     Verify that the response has this status

    """
    rsp = csrf_response(django_client, request_url, 'delete', json.dumps(data),
                        status_code=status_code,
                        content_type='application/json')
    assert rsp.get('Content-Type') == 'application/json'
    return json.loads(rsp.content)


def _csrf_delete_response(django_client, request_url, data, status_code=200,
                          content_type=MULTIPART_CONTENT):
    warnings.warn(
        "This method is deprecated as of OMERO 5.4.0. Use delete_json",
        DeprecationWarning)
    csrf_token = django_client.cookies['csrftoken'].value
    extra = {'HTTP_X_CSRFTOKEN': csrf_token}
    return _delete_response(django_client, request_url, data=data,
                            status_code=status_code, content_type=content_type,
                            **extra)


def _csrf_delete_response_json(django_client, request_url,
                               data, status_code=200):
    warnings.warn(
        "This method is deprecated as of OMERO 5.4.0. Use delete_json",
        DeprecationWarning)
    rsp = _csrf_delete_response(django_client, request_url, data, status_code)
    assert rsp.get('Content-Type') == 'application/json'
    return json.loads(rsp.content)


# GET
def get(django_client, request_url, data=None, status_code=200, csrf=False):
    """
    Performs a GET request and returns the response.

    :param django_client:   Django test Client
    :param request_url:     The url to request
    :param data:            A dictionary of data, used to build a query string
    :param status_code:     Verify that the response has this status
    :param csrf:            If true, add csrf token to query string
    """
    if csrf:
        if data is None:
            data = {}
        else:
            # avoid mutating dict we're passed
            data = data.copy()
        csrf_token = django_client.cookies['csrftoken'].value
        data['csrfmiddlewaretoken'] = csrf_token
    if data is not None:
        query_string = urlencode(data.items(), doseq=True)
        request_url = '%s?%s' % (request_url, query_string)
    return _response(django_client, request_url, 'get',
                     status_code=status_code)


def get_json(django_client, request_url, data=None, status_code=200,
             csrf=False):
    """
    Performs a GET request and returns the JSON response as a dict.

    :param django_client:   Django test Client
    :param request_url:     The url to request
    :param data:            A dictionary of data, used to build a query string
    :param status_code:     Verify that the response has this status
    :param csrf:            If true, add csrf token to query string

    """
    rsp = get(django_client, request_url, data, status_code, csrf)
    assert rsp.get('Content-Type') == 'application/json'
    return json.loads(rsp.content)


def _get_response(django_client, request_url, query_string, status_code=405):
    warnings.warn(
        "This method is deprecated as of OMERO 5.4.0. Use get",
        DeprecationWarning)
    query_string = urlencode(query_string.items(), doseq=True)
    response = django_client.get('%s?%s' % (request_url, query_string))
    assert response.status_code == status_code
    return response


def _csrf_get_response(django_client, request_url, query_string,
                       status_code=200):
    warnings.warn(
        "This method is deprecated as of OMERO 5.4.0. Use get",
        DeprecationWarning)
    csrf_token = django_client.cookies['csrftoken'].value
    query_string['csrfmiddlewaretoken'] = csrf_token
    return _get_response(django_client, request_url, query_string,
                         status_code)


def _get_response_json(django_client, request_url,
                       query_string, status_code=200):
    warnings.warn(
        "This method is deprecated as of OMERO 5.4.0. Use get_json",
        DeprecationWarning)
    rsp = _get_response(django_client, request_url, query_string, status_code)
    assert rsp.get('Content-Type') == 'application/json'
    return json.loads(rsp.content)
