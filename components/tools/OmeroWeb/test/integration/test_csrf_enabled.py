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
Simple integration tests to ensure that the CSRF middleware is enabled.
"""

import pytest
import test.integration.library as lib

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


class TestCsrf(object):
    """
    Tests to ensure that Django CSRF middleware for OMERO.web is enabled.
    """

    def test_login(self, client):
        """
        If the CSRF middleware is enabled login attempts that do not include
        the CSRF token should fail with an HTTP 403 status code.
        """
        login_url = reverse('weblogin')
        # https://docs.djangoproject.com/en/dev/ref/contrib/csrf/#testing
        http_client = Client(enforce_csrf_checks=True)

        data = {
            'server': 1,
            'username': client.getProperty('omero.user'),
            'password': client.getProperty('omero.pass')
        }
        response = http_client.post(login_url, data)
        assert response.status_code == 403
