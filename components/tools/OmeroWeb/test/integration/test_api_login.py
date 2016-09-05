#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
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
Tests logging in with webgateway json api
"""

import pytest
from weblibrary import IWebTest, _get_response_json, _post_response_json, \
    _csrf_post_response_json
from django.core.urlresolvers import reverse, NoReverseMatch
from django.conf import settings
from django.test import Client
from omero_marshal import OME_SCHEMA_URL
import json


class TestLogin(IWebTest):
    """
    Tests login workflow: getting url, csfv tokens etc.
    """

    def test_versions(self):
        """
        Start at the base url, get versions
        """
        django_client = self.django_root_client
        request_url = reverse('api_versions')
        rsp = _get_response_json(django_client, request_url, {})
        versions = rsp['data']
        assert len(versions) == len(settings.API_VERSIONS)
        for v in versions:
            assert v['version'] in settings.API_VERSIONS

    def test_base_url(self):
        """
        Tests that the base url for a given version provides other urls
        """
        django_client = self.django_root_client
        # test the most recent version
        version = settings.API_VERSIONS[-1]
        request_url = reverse('api_base', kwargs={'api_version': version})
        rsp = _get_response_json(django_client, request_url, {})
        assert 'servers_url' in rsp
        assert 'login_url' in rsp
        assert 'projects_url' in rsp
        assert 'save_url' in rsp
        assert rsp['schema_url'] == OME_SCHEMA_URL

    def test_base_url_versions_404(self):
        """
        Tests that the base url gives 404 for invalid versions
        """
        version = '0'
        with pytest.raises(NoReverseMatch):
            reverse('api_base', kwargs={'api_version': version})

    def test_login_get(self):
        """
        Tests that we get a suitable message if we try to GET login_url
        """
        django_client = self.django_root_client
        version = settings.API_VERSIONS[-1]
        request_url = reverse('api_login', kwargs={'api_version': version})
        rsp = _get_response_json(django_client, request_url, {})
        assert (rsp['message'] ==
                "POST only with username, password, server and csrftoken")

    def test_login_csrf(self):
        """
        Tests that we can only login with CSRF
        """
        django_client = self.django_root_client
        # test the most recent version
        version = settings.API_VERSIONS[-1]
        request_url = reverse('api_login', kwargs={'api_version': version})
        rsp = _post_response_json(django_client, request_url, {},
                                  status_code=403)
        assert (rsp['message'] ==
                "CSRF Error. You need to include 'X-CSRFToken' in header")

    @pytest.mark.parametrize("credentials", [
        [{'username': 'guest', 'password': 'fake', 'server': 1},
            "Username: Guest account is not supported."],
        [{'username': 'nobody', 'password': '', 'server': 1},
            "Password: This field is required."],
        [{'password': 'fake'},
            # No username OR server. Test concatenation of 2 errors
            ("Username: This field is required. "
             "Server: This field is required.")],
        [{'username': 'nobody', 'password': 'fake', 'server': 1},
            ("Connection not available, "
             "please check your user name and password.")]
        ])
    def test_login_errors(self, credentials):
        """
        Tests that we get expected form validation errors if try to login
        without required fields, as 'guest' or with invalid username/password.
        """
        django_client = self.django_root_client
        # test the most recent version
        version = settings.API_VERSIONS[-1]
        request_url = reverse('api_login', kwargs={'api_version': version})
        data = credentials[0]
        message = credentials[1]
        rsp = _csrf_post_response_json(django_client, request_url, data,
                                       status_code=403)
        assert rsp['message'] == message

    def test_login_example(self):
        """
        Example of successful login as user would do for real,
        starting at base url and getting all other urls and info from there.
        """
        # Create test user and get username & password for use below
        user = self.new_user()
        username = password = user.getOmeName().val

        # Django client, not logged in yet
        django_client = Client()
        # Start at the /api/ url to list versions...
        request_url = reverse('api_versions')
        rsp = _get_response_json(django_client, request_url, {})
        # Pick the last version
        version = rsp['data'][-1]
        base_url = version['base_url']
        # Base url will give a bunch of other urls
        base_rsp = _get_response_json(django_client, base_url, {})
        login_url = base_rsp['login_url']
        servers_url = base_rsp['servers_url']
        login_url = base_rsp['login_url']
        token_url = base_rsp['token_url']
        # See what servers we can log in to
        servers_rsp = _get_response_json(django_client, servers_url, {})
        server_id = servers_rsp['data'][0]['id']
        # Need a CSRF token
        token_rsp = _get_response_json(django_client, token_url, {})
        token = token_rsp['data']
        # Can also get this from our session cookies
        csrf_token = django_client.cookies['csrftoken'].value
        assert token == csrf_token
        # Now we have all info we need for login.
        # Set the header, so we don't need to do this for every POST/PUT/DELETE
        # OR we could add it to each POST as 'csrfmiddlewaretoken'
        django_client = Client(HTTP_X_CSRFTOKEN=token)
        data = {
            'username': username,
            'password': password,
            'server': server_id,
            # 'csrfmiddlewaretoken': token,
        }
        login_rsp = django_client.post(login_url, data)
        login_json = json.loads(login_rsp.content)
        assert login_json['success']
        eventContext = login_json['eventContext']
        # eventContext gives a bunch of info
        memberOfGroups = eventContext['memberOfGroups']
        currentGroup = eventContext['groupId']
        userId = eventContext['userId']
        assert len(memberOfGroups) == 2      # includes 'user' group
        assert currentGroup in memberOfGroups
        assert userId > 0
